import csv
import re

def parse_benchmark_csv(filepath):
    """Liest die JMH CSV und gibt strukturierte Daten zurück"""
    results = {}
    
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
    
    # Nur Zeilen bis zur ersten Kommentar-Zeile oder Leerzeile nutzen
    clean_lines = []
    for line in lines:
        stripped = line.strip()
        if stripped.startswith('#') or stripped == '':
            break
        clean_lines.append(line)
    
    reader = csv.DictReader(clean_lines)
    for row in reader:
        benchmark_name = row['Benchmark']
        graph_size = row['Param: graphSize']
        score = float(row['Score'].replace(',', '.'))
        error = float(row['Score Error (99,9%)'].replace(',', '.'))
        unit = row['Unit']
        
        if 'Kahn' in benchmark_name or 'kahn' in benchmark_name:
            algo = 'Kahn'
        elif 'DFS' in benchmark_name or 'dfs' in benchmark_name:
            algo = 'DFS'
        else:
            algo = 'Unknown'
        
        if graph_size not in results:
            results[graph_size] = {}
        
        results[graph_size][algo] = {
            'score': score,
            'error': error,
            'unit': unit
        }
    
    return results


def create_summary_table(results, size_order, is_full_program):
    """Erstellt eine schöne Vergleichstabelle"""
    
    lines = []
    lines.append("\n" + "="*80)
    
    if is_full_program:
        lines.append("ZUSAMMENFASSUNG: Gesamtlaufzeit des Programms")
        lines.append("(einmal mit Kahn, einmal mit DFS als Sortieralgorithmus)")
    else:
        lines.append("ZUSAMMENFASSUNG: Kahn vs DFS Algorithmus-Vergleich")
    
    lines.append("="*80)
    
    # Ermittle die echte Unit aus den Daten
    unit = 'ms/op'
    for size_data in results.values():
        for algo_data in size_data.values():
            unit = algo_data.get('unit', 'ms/op')
            break
        break
    
    # Header MIT der echten Unit
    header = f"{'Graphgroesse':<15}{'Kahn (' + unit + ')':<22}{'DFS (' + unit + ')':<22}{'Schneller':<12}"
    lines.append(header)
    lines.append("-"*80)
    
    for size in size_order:
        if size not in results:
            continue
            
        data = results[size]
        
        kahn_score = data.get('Kahn', {}).get('score', 0)
        kahn_error = data.get('Kahn', {}).get('error', 0)
        dfs_score = data.get('DFS', {}).get('score', 0)
        dfs_error = data.get('DFS', {}).get('error', 0)
        
        kahn_str = f"{kahn_score:.3f} +/- {kahn_error:.3f}"
        dfs_str = f"{dfs_score:.3f} +/- {dfs_error:.3f}"
        
        if kahn_score < dfs_score:
            winner = "Kahn"
        elif dfs_score < kahn_score:
            winner = "DFS"
        else:
            winner = "Gleich"
        
        line = f"{size:<15}{kahn_str:<22}{dfs_str:<22}{winner:<12}"
        lines.append(line)
    
    lines.append("="*80)
    
    return "\n".join(lines)


def append_summary_to_csv(csv_filepath, summary_text):
    """Fügt die Zusammenfassung am Ende der CSV Datei hinzu"""
    
    with open(csv_filepath, 'a', encoding='utf-8') as f:
        f.write("\n\n")
        f.write("# " + summary_text.replace("\n", "\n# "))


def main():
    
    # Graphgrößen in der richtigen Reihenfolge
    size_order = ['simple', 'medium', 'large', 'xlarge', 'xxlarge', 'xxxlarge', 'xxxxlarge']
    
    # CSV Dateien die verarbeitet werden sollen
    # (Dateiname, ist_es_das_gesamte_programm)
    csv_files = [
        ('full_program_results.csv', True),
        ('algorithm_results.csv', False)
    ]
    
    for csv_file, is_full_program in csv_files:
        try:
            print(f"\nVerarbeite: {csv_file}")
            
            results = parse_benchmark_csv(csv_file)
            summary = create_summary_table(results, size_order, is_full_program)
            
            print(summary)
            
            # Zusammenfassung an CSV anhängen
            append_summary_to_csv(csv_file, summary)
            
            print(f"\nZusammenfassung wurde an {csv_file} angehaengt!")
            
        except FileNotFoundError:
            print(f"Datei nicht gefunden: {csv_file}")
        except Exception as e:
            print(f"Fehler bei {csv_file}: {e}")


if __name__ == "__main__":
    main()