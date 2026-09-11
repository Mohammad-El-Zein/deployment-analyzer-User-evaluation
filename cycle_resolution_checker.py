"""
cycle_resolution_checker.py

Prueft automatisiert, ob das Feedback-Arc-Set-Verfahren
einen urspruenglich zyklischen Graphen korrekt in einen
azyklischen Graphen (DAG) ueberfuehrt hat.

Vorgehen:
    1. Liest die urspruenglichen Abhaengigkeiten aus der YAML-Datei.
    2. Liest aus dem Konsolen-Log, welche Kanten das Feedback-
       Arc-Set-Verfahren entfernt hat (Abschnitt
       "FEEDBACK ARC SET").
    3. Entfernt diese Kanten vom urspruenglichen Kantenset.
    4. Versucht, den verbleibenden Graphen mittels einer
       eigenstaendigen Kahn-Implementierung topologisch zu
       sortieren. Gelingt dies vollstaendig (alle Knoten werden
       eingeordnet), ist der Graph nachweisbar azyklisch.

Nutzung:
    python cycle_resolution_checker.py <yaml_datei> <log_datei>
"""

import re
import sys
from collections import defaultdict, deque

import yaml


def load_dependencies(yaml_path):
    with open(yaml_path, "r", encoding="utf-8") as f:
        data = yaml.safe_load(f)
    services = data.get("services", {})
    dependencies = {}
    for service_name, service_data in services.items():
        deps = []
        if service_data and "depends_on" in service_data:
            deps = service_data["depends_on"] or []
        dependencies[service_name] = deps
    return dependencies


def build_edges(dependencies):
    edges = set()
    for service, deps in dependencies.items():
        for dep in deps:
            edges.add((dep, service))
    return edges


def extract_removed_edges(log_text):
    """Extrahiert die vom Feedback-Arc-Set-Verfahren entfernten
    Kanten aus dem Abschnitt 'FEEDBACK ARC SET' im Konsolen-Log.
    Erwartetes Zeilenformat: '   [X] dep -> service'."""

    section_start = log_text.find("FEEDBACK ARC SET")
    if section_start == -1:
        raise ValueError("Abschnitt 'FEEDBACK ARC SET' nicht gefunden.")

    after_title = section_start + len("FEEDBACK ARC SET")
    own_border = log_text.find("#=====", after_title)
    content_start = (own_border + len("#=====")
                      if own_border != -1 else after_title)
    next_section = log_text.find("#=====", content_start)
    section_text = (log_text[content_start:next_section]
                    if next_section != -1
                    else log_text[content_start:])

    pattern = re.compile(r"\[X\]\s+(\S+)\s*->\s*(\S+)")
    removed = set(pattern.findall(section_text))
    return removed


def is_acyclic(nodes, edges):
    """Eigenstaendige Kahn-Implementierung zur Verifikation.
    Gibt (True, []) zurueck, wenn der Graph azyklisch ist,
    sonst (False, verbleibende_knoten)."""

    in_degree = {n: 0 for n in nodes}
    adjacency = defaultdict(list)
    for dep, service in edges:
        adjacency[dep].append(service)
        in_degree[service] += 1

    queue = deque(n for n in nodes if in_degree[n] == 0)
    processed = []

    while queue:
        current = queue.popleft()
        processed.append(current)
        for neighbor in adjacency[current]:
            in_degree[neighbor] -= 1
            if in_degree[neighbor] == 0:
                queue.append(neighbor)

    remaining = [n for n in nodes if n not in processed]
    return (len(remaining) == 0), remaining


def main():
    if len(sys.argv) != 3:
        print("Nutzung: python cycle_resolution_checker.py "
              "<yaml_datei> <log_datei>")
        sys.exit(1)

    yaml_path, log_path = sys.argv[1], sys.argv[2]

    dependencies = load_dependencies(yaml_path)
    nodes = list(dependencies.keys())
    original_edges = build_edges(dependencies)

    with open(log_path, "r", encoding="utf-8", errors="replace") as f:
        log_text = f.read()

    removed_edges = extract_removed_edges(log_text)

    print(f"Pruefe {yaml_path} ({len(nodes)} Services, "
          f"{len(original_edges)} urspruengliche Abhaengigkeiten)")
    print(f"Vom Feedback-Arc-Set-Verfahren entfernte Kanten: "
          f"{len(removed_edges)}")
    for dep, service in sorted(removed_edges):
        print(f"    - {dep} -> {service}")
    print()

    remaining_edges = original_edges - removed_edges
    acyclic, remaining_nodes = is_acyclic(nodes, remaining_edges)

    if acyclic:
        print(f"[OK] Nach Entfernung der {len(removed_edges)} Kante(n) "
              f"ist der Graph nachweisbar azyklisch "
              f"(alle {len(nodes)} Knoten topologisch sortierbar).")
        sys.exit(0)
    else:
        print(f"[FEHLER] Graph weiterhin zyklisch! "
              f"{len(remaining_nodes)} Knoten konnten nicht "
              f"eingeordnet werden: {remaining_nodes}")
        sys.exit(1)


if __name__ == "__main__":
    main()