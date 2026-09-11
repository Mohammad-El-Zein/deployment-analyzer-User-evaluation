"""
correctness_checker.py

Prueft automatisiert, ob eine berechnete Deployment-
Reihenfolge (Kahn und DFS) die grundlegende Korrektheits-
Invariante einer topologischen Sortierung erfuellt:

    Fuer jede Abhaengigkeit "service depends_on dep"
    (interne Kante: dep -> service) muss dep in der
    Reihenfolge VOR service erscheinen.

Nutzung:
    1. Fuehre dein Programm aus und leite die Konsolen-
       ausgabe in eine Log-Datei um, z.B.:
           mvn exec:java -q > medium_log.txt
    2. Rufe dieses Skript auf:
           python correctness_checker.py <yaml_datei> <log_datei>

Beispiel:
    python correctness_checker.py \
        src/main/resources/examples/medium.yaml \
        medium_log.txt
"""

import re
import sys
import yaml


def load_dependencies(yaml_path):
    """Liest die YAML-Datei und gibt eine Abbildung
    Service -> Liste seiner Abhaengigkeiten zurueck,
    identisch zum Format, das YamlParser.java einliest."""
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
    """Baut die gerichteten Kanten gemaess der in
    Abschnitt 4.1 festgelegten Konvention: dep -> service
    (dep muss vor service stehen)."""
    edges = []
    for service, deps in dependencies.items():
        for dep in deps:
            edges.append((dep, service))
    return edges


def extract_order(log_text, section_title):
    """Extrahiert die nummerierte Deployment-Reihenfolge
    aus dem Konsolen-Log fuer einen gegebenen Abschnitt
    (z.B. "KAHN'S ALGORITHMUS (BFS)" oder
    "DFS TOPOLOGISCHE SORTIERUNG").

    Die Kopfzeile jedes Abschnitts ist von einer eigenen
    '#=====#' Umrandung eingerahmt (Titel, dann direkt
    darunter die untere Umrandung dieses Kastens). Der
    eigentliche Inhalt beginnt daher erst NACH dieser
    ersten Umrandungszeile; abgeschnitten wird beim
    Beginn des naechsten Abschnitts (zweite '#=====#'
    Vorkommnis nach dem Titel)."""

    section_start = log_text.find(section_title)
    if section_start == -1:
        raise ValueError(
            f"Abschnitt '{section_title}' nicht im Log gefunden.")

    after_title = section_start + len(section_title)

    # Untere Umrandung des aktuellen Kastens ueberspringen
    own_border = log_text.find("#=====", after_title)
    if own_border == -1:
        content_start = after_title
    else:
        content_start = own_border + len("#=====")

    # Obere Umrandung des naechsten Abschnitts als Grenze suchen
    next_section = log_text.find("#=====", content_start)
    section_text = (log_text[content_start:next_section]
                    if next_section != -1
                    else log_text[content_start:])

    # Nummerierte Zeilen wie "   1. database" extrahieren
    pattern = re.compile(r"^\s*\d+\.\s+(\S+)\s*$", re.MULTILINE)
    order = pattern.findall(section_text)

    if not order:
        raise ValueError(
            f"Keine Deployment-Reihenfolge in Abschnitt "
            f"'{section_title}' gefunden.")

    return order


def check_order(order, edges, algorithm_name):
    """Prueft fuer jede Kante (dep, service), ob dep vor
    service in der gegebenen Reihenfolge erscheint.
    Gibt eine Liste verletzter Kanten zurueck."""

    position = {name: idx for idx, name in enumerate(order)}
    violations = []

    for dep, service in edges:
        if dep not in position or service not in position:
            violations.append(
                (dep, service, "Knoten fehlt in der Reihenfolge"))
            continue
        if position[dep] >= position[service]:
            violations.append(
                (dep, service,
                 f"{dep} (Position {position[dep]}) steht nicht "
                 f"vor {service} (Position {position[service]})"))

    if violations:
        print(f"  [FEHLER] {algorithm_name}: "
              f"{len(violations)} Verletzung(en) gefunden:")
        for dep, service, reason in violations:
            print(f"    - {dep} -> {service}: {reason}")
    else:
        print(f"  [OK] {algorithm_name}: "
              f"alle {len(edges)} Abhaengigkeiten korrekt eingehalten.")

    return violations


def main():
    if len(sys.argv) != 3:
        print("Nutzung: python correctness_checker.py "
              "<yaml_datei> <log_datei>")
        sys.exit(1)

    yaml_path = sys.argv[1]
    log_path = sys.argv[2]

    dependencies = load_dependencies(yaml_path)
    edges = build_edges(dependencies)

    with open(log_path, "r", encoding="utf-8", errors="replace") as f:
        log_text = f.read()

    print(f"Pruefe {yaml_path} ({len(dependencies)} Services, "
          f"{len(edges)} Abhaengigkeiten)")
    print()

    kahn_order = extract_order(log_text, "KAHN'S ALGORITHMUS (BFS)")
    dfs_order = extract_order(log_text, "DFS TOPOLOGISCHE SORTIERUNG")

    kahn_violations = check_order(kahn_order, edges, "Kahn")
    dfs_violations = check_order(dfs_order, edges, "DFS")

    print()
    if not kahn_violations and not dfs_violations:
        print("ERGEBNIS: Beide Reihenfolgen sind gueltige "
              "topologische Sortierungen.")
        sys.exit(0)
    else:
        print("ERGEBNIS: Mindestens eine Verletzung gefunden!")
        sys.exit(1)


if __name__ == "__main__":
    main()