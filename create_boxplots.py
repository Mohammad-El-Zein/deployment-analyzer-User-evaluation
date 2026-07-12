import json
import matplotlib.pyplot as plt

SIZE_TO_SERVICES = {
    'simple': 5,
    'medium': 20,
    'large': 50,
    'xlarge': 100,
    'xxlarge': 500,
    'xxxlarge': 1000,
    'xxxxlarge': 2000
}

def load_jmh_json(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        return json.load(f)


def extract_raw_data(jmh_data, algorithm_name):
    """Extrahiert Rohdaten fuer einen bestimmten
    Benchmark-Methodennamen (z.B. 'kahnAlgorithm' 
    oder 'fullProgramWithKahn')"""
    result = {}
    for entry in jmh_data:
        if algorithm_name not in entry['benchmark']:
            continue
        graph_size = entry['params']['graphSize']
        raw_data = entry['primaryMetric']['rawData']
        all_values = []
        for fork_values in raw_data:
            all_values.extend(fork_values)
        result[graph_size] = all_values
    return result


def add_service_count_labels(ax, positions, valid_sizes, y_position):
    """Fuegt die Service-Anzahl als Text zentriert 
    unter jeder Kahn/DFS Gruppe hinzu"""
    i = 0
    for size in valid_sizes:
        kahn_pos = positions[i]
        dfs_pos = positions[i + 1]
        center = (kahn_pos + dfs_pos) / 2
        services = SIZE_TO_SERVICES.get(size, "?")
        
        ax.text(center, y_position, f"{services} Services",
                ha='center', va='top', fontsize=10,
                fontweight='bold', transform=ax.get_xaxis_transform())
        i += 2


def create_boxplot(kahn_data, dfs_data, size_order, title, filename, ylabel):
    
    valid_sizes = [s for s in size_order 
                   if s in kahn_data and s in dfs_data]
    
    plot_data = []
    labels = []
    colors = []
    positions = []
    pos = 1
    
    for size in valid_sizes:
        plot_data.append(kahn_data[size])
        labels.append(f"{size}\nKahn")
        colors.append('#4CAF50')
        positions.append(pos)
        pos += 1
        
        plot_data.append(dfs_data[size])
        labels.append(f"{size}\nDFS")
        colors.append('#2196F3')
        positions.append(pos)
        pos += 2
    
    fig, ax = plt.subplots(figsize=(20, 10))
    
    bp = ax.boxplot(plot_data, positions=positions, 
                     patch_artist=True, widths=0.8)
    
    for patch, color in zip(bp['boxes'], colors):
        patch.set_facecolor(color)
        patch.set_alpha(0.7)
    
    ax.set_xticks(positions)
    ax.set_xticklabels(labels, fontsize=10)
    add_service_count_labels(ax, positions, valid_sizes, y_position=-0.08)
    ax.set_ylabel(ylabel, fontsize=12)
    ax.set_title(title, fontsize=14, fontweight='bold')
    ax.grid(axis='y', linestyle='--', alpha=0.5)
    
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(facecolor='#4CAF50', alpha=0.7, label='Kahn'),
        Patch(facecolor='#2196F3', alpha=0.7, label='DFS')
    ]
    ax.legend(handles=legend_elements, loc='upper left')
    
    plt.tight_layout()
    plt.savefig(filename, dpi=300)
    print(f"Boxplot gespeichert: {filename}")
    plt.close()


def create_boxplot_log_scale(kahn_data, dfs_data, size_order, title, filename, ylabel):
    
    valid_sizes = [s for s in size_order 
                   if s in kahn_data and s in dfs_data]
    
    plot_data = []
    labels = []
    colors = []
    positions = []
    pos = 1
    
    for size in valid_sizes:
        plot_data.append(kahn_data[size])
        labels.append(f"{size}\nKahn")
        colors.append('#4CAF50')
        positions.append(pos)
        pos += 1
        
        plot_data.append(dfs_data[size])
        labels.append(f"{size}\nDFS")
        colors.append('#2196F3')
        positions.append(pos)
        pos += 2
    
    fig, ax = plt.subplots(figsize=(20, 10))
    
    bp = ax.boxplot(plot_data, positions=positions, 
                     patch_artist=True, widths=0.8)
    
    for patch, color in zip(bp['boxes'], colors):
        patch.set_facecolor(color)
        patch.set_alpha(0.7)
    
    ax.set_yscale('log')
    ax.set_xticks(positions)
    ax.set_xticklabels(labels, fontsize=10)
    add_service_count_labels(ax, positions, valid_sizes, y_position=-0.08)
    ax.set_ylabel(ylabel + " (log-Skala)", fontsize=12)
    ax.set_title(title, fontsize=14, fontweight='bold')
    ax.grid(axis='y', linestyle='--', alpha=0.5)
    
    from matplotlib.patches import Patch
    legend_elements = [
        Patch(facecolor='#4CAF50', alpha=0.7, label='Kahn'),
        Patch(facecolor='#2196F3', alpha=0.7, label='DFS')
    ]
    ax.legend(handles=legend_elements, loc='upper left')
    
    plt.tight_layout()
    plt.savefig(filename, dpi=300)
    print(f"Boxplot gespeichert: {filename}")
    plt.close()


def main():
    
    size_order = ['simple', 'medium', 'large', 'xlarge', 
                  'xxlarge', 'xxxlarge', 'xxxxlarge']
    
    # EINE kombinierte JSON Datei mit BEIDEN Benchmark-Typen
    json_file = 'all_raw_results.json'
    
    try:
        all_json = load_jmh_json(json_file)
        
        # --- Isolierte Algorithmen ---
        kahn_data = extract_raw_data(all_json, 'kahnAlgorithm')
        dfs_data = extract_raw_data(all_json, 'dfsAlgorithm')
        
        if kahn_data and dfs_data:
            print("Erstelle Boxplot fuer isolierte Algorithmen...")
            
            create_boxplot(
                kahn_data, dfs_data, size_order,
                "Laufzeitvergleich: Kahn vs. DFS (isoliert)",
                "boxplot_algorithm.png",
                "Laufzeit (us)"
            )
            
            create_boxplot_log_scale(
                kahn_data, dfs_data, size_order,
                "Laufzeitvergleich: Kahn vs. DFS (isoliert, log-Skala)",
                "boxplot_algorithm_log.png",
                "Laufzeit (us)"
            )
        else:
            print("Keine Daten fuer isolierte Algorithmen gefunden.")
        
        # --- Gesamtprogramm ---
        kahn_full = extract_raw_data(all_json, 'fullProgramWithKahn')
        dfs_full = extract_raw_data(all_json, 'fullProgramWithDFS')
        
        if kahn_full and dfs_full:
            print("Erstelle Boxplot fuer Gesamtprogramm...")
            
            create_boxplot_log_scale(
                kahn_full, dfs_full, size_order,
                "Laufzeitvergleich: Gesamtprogramm (Kahn vs. DFS Version)",
                "boxplot_full_program_log.png",
                "Laufzeit (ms)"
            )
        else:
            print("Keine Daten fuer Gesamtprogramm gefunden.")
        
    except FileNotFoundError:
        print(f"{json_file} nicht gefunden!")
        print("Fuehre zuerst RunAllBenchmarks aus.")


if __name__ == "__main__":
    main()