import json
import csv

def load_jmh_json(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        return json.load(f)


def compute_mean(entry):
    """Berechnet den Mittelwert aus allen rawData
    Messwerten (alle Forks zusammengefasst)."""
    raw_data = entry['primaryMetric']['rawData']
    all_values = []
    for fork_values in raw_data:
        all_values.extend(fork_values)
    return sum(all_values) / len(all_values)


def extract_means(jmh_data, benchmark_name_filter):
    """Gibt zurueck: {graphSize: {'score': mean, 'unit': unit}}"""
    result = {}
    for entry in jmh_data:
        if benchmark_name_filter not in entry['benchmark']:
            continue
        graph_size = entry['params']['graphSize']
        mean_value = compute_mean(entry)
        unit = entry['primaryMetric']['scoreUnit']
        result[graph_size] = {'score': mean_value, 'unit': unit}
    return result


def print_and_collect_summary(kahn_data, dfs_data, size_order,
                                title, rows_out, category):
    print("\n" + "=" * 80)
    print(title)
    print("=" * 80)

    unit = 'ms/op'
    for d in kahn_data.values():
        unit = d['unit']
        break

    header = f"{'Graphgroesse':<15}{'Kahn (' + unit + ')':<20}{'DFS (' + unit + ')':<20}{'Schneller':<12}"
    print(header)
    print("-" * 80)

    for size in size_order:
        if size not in kahn_data or size not in dfs_data:
            continue

        kahn_score = kahn_data[size]['score']
        dfs_score = dfs_data[size]['score']

        faster = "Kahn" if kahn_score < dfs_score else "DFS"

        print(f"{size:<15}{kahn_score:<20.3f}{dfs_score:<20.3f}{faster:<12}")

        rows_out.append({
            'category': category,
            'graphSize': size,
            'kahn': f"{kahn_score:.6f}",
            'dfs': f"{dfs_score:.6f}",
            'unit': unit,
            'faster': faster
        })

    print("=" * 80)


def main():

    size_order = ['simple', 'medium', 'large', 'xlarge',
              'xxlarge', 'xxxlarge', 'xxxxlarge',
              'showcase-10-services']

    json_file = 'all_raw_results.json'

    try:
        all_json = load_jmh_json(json_file)
    except FileNotFoundError:
        print(f"{json_file} nicht gefunden!")
        print("Fuehre zuerst RunAllBenchmarks aus.")
        return

    rows = []

    # --- Isolierte Algorithmen ---
    kahn_algo = extract_means(all_json, 'kahnAlgorithm')
    dfs_algo = extract_means(all_json, 'dfsAlgorithm')

    if kahn_algo and dfs_algo:
        print_and_collect_summary(
            kahn_algo, dfs_algo, size_order,
            "ZUSAMMENFASSUNG: Kahn vs DFS Algorithmus-Vergleich",
            rows, "algorithm"
        )

    # --- Gesamtprogramm ---
    kahn_full = extract_means(all_json, 'fullProgramWithKahn')
    dfs_full = extract_means(all_json, 'fullProgramWithDFS')

    if kahn_full and dfs_full:
        print_and_collect_summary(
            kahn_full, dfs_full, size_order,
            "ZUSAMMENFASSUNG: Gesamtlaufzeit des Programms",
            rows, "fullprogram"
        )

    # In eine einfache CSV schreiben (fuer Main.java)
    summary_file = 'summary_results.csv'
    with open(summary_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(
            f, fieldnames=['category', 'graphSize', 'kahn',
                           'dfs', 'unit', 'faster'])
        writer.writeheader()
        writer.writerows(rows)

    print(f"\nZusammenfassung gespeichert in: {summary_file}")


if __name__ == "__main__":
    main()