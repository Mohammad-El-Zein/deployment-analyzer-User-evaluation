# Deployment Analyzer

<p align="center">
  <img src="https://img.shields.io/badge/TU-Dortmund-4CAF50?style=for-the-badge" alt="TU Dortmund"/>
  <img src="https://img.shields.io/badge/Algorithm-Engineering-003DA5?style=for-the-badge" alt="Algorithm Engineering"/>
</p>

<p align="center">
  <strong>Entwicklung und Evaluation eines graphbasierten Algorithmus zur automatischen Bestimmung von Deployment-Reihenfolgen in Microservice-Architekturen</strong>
</p>

<p align="center">
  Bachelorarbeit – TU Dortmund<br>
  Mohammad El Zein | Matrikelnummer: 259506<br>
  Betreuer: Prof. Dr. Kevin Buchin<br>
</p>

---

## Beschreibung

Dieses Tool analysiert Microservice-Abhängigkeiten automatisch und berechnet die optimale Deployment-Reihenfolge mithilfe von Graphalgorithmen. Der Benutzer definiert die Abhängigkeiten zwischen den Services entweder direkt in einer YAML-Datei oder – für den Praxiseinsatz – automatisch ableitbar aus bereits bestehenden Docker-Compose- oder Kubernetes-Manifesten. Die korrekte Reihenfolge, Zyklenerkennung und Parallelisierungsoptimierung übernimmt das System vollautomatisch.

### Motivation

Das Deployment von Microservices in modernen Cloud-Umgebungen ist fehleranfällig: Zyklische Abhängigkeiten blockieren Systeme, falsche Startreihenfolgen verursachen CrashLoopBackOff und hohe Cloud-Kosten, und bei 1000+ Services ist manuelle Analyse nicht mehr praktikabel. Diese Arbeit löst dieses Problem durch einen graphbasierten Ansatz mit fünf Algorithmen – von der Reihenfolgeberechnung bis zur automatischen Zyklusauflösung.

### Was aktuelle Tools leisten – und wo sie scheitern

| Kriterium | Docker Compose | Kubernetes / Azure / AWS | Helm |
|---|---|---|---|
| **Reihenfolge** | ✅ Automatisch | ❌ Alles parallel | ⚠️ Teilweise (nach Typ) |
| **Zyklus erkennen** | ✅ Ja | ❌ Nein | ❌ Nein |
| **Zyklus lösen** | ❌ Abbruch | ❌ Nein | ❌ Nein |
| **Parallelisierung** | ✅ Ja | ❌ Unkontrolliert | ❌ Unkontrolliert (App) |
| **Skalierbarkeit 1000+** | ❌ Riesige YAML | ❌ CrashLoop / Hohe Kosten | ❌ Träge / Timeout |
| **Bei Zyklus** | ❌ Kaskaden-Abbruch | ❌ Unendliche Schleife | ❌ Timeout (5-10 Min) |
| **Transparenz** | ❌ Black Box | ❌ Black Box | ❌ Black Box |

### Kein bestehendes Tool bietet alles zusammen

- Automatische Reihenfolge **+** Zyklus erkennen **+** Zyklus lösen
- Optimierte kontrollierte Parallelisierung (Start-Schichten)
- Transparenz (White Box) – welcher Algorithmus läuft?
- Nachgewiesene Skalierbarkeit bei über 2000 Services
- Experimenteller Vergleich der Algorithmen mit wissenschaftlichem Benchmarking (JMH – Java Microbenchmark Harness)
- Berücksichtigung geschützter (nicht löschbarer) Abhängigkeiten bei der Zyklusauflösung
- Maschinenlesbarer Output (JSON) und visuelle Graph-Darstellung (SVG)
- Automatische Ableitung des Abhängigkeitsgraphen aus bestehenden Docker-Compose- oder Kubernetes-Manifesten (regelbasiert, ohne LLM-Einsatz)

### Ablauf

```
Docker Compose /            Eigene YAML-Datei
Kubernetes Manifeste              |
        ↓                         |
UniversalYamlParser                |
(automatische Ableitung)          |
        ↓                         |
        └──────────┬──────────────┘
                   ↓
            Graph aufbauen
                   ↓
     Kahn oder DFS → Reihenfolge berechnen
                   ↓
            Zyklus gefunden?
             ↙            ↘
           JA              NEIN
            ↓                 ↓
        Tarjan            Level-BFS
      → Zyklus            → Parallele
        lokalisieren        Gruppen
            ↓                  ↓
     Feedback Arc Set   SVG-Visualisierung
     (respektiert           +
      geschützte Kanten)  JSON-Export
            ↓
         Lösbar?
        ↙       ↘
      JA        NEIN
       ↓          ↓
   Level-BFS   Abbruch mit
   + SVG       Fehlermeldung
   + JSON
```

---

## Algorithmen

| Algorithmus | Zweck | Komplexität |
|---|---|---|
| Kahn's Algorithmus | Topologische Sortierung (BFS) | O(V+E) |
| DFS Topologische Sortierung | Topologische Sortierung (DFS) | O(V+E) |
| Tarjan's Algorithmus | Zyklenerkennung (SCC) | O(V+E) |
| Feedback Arc Set | Zyklusauflösung (Greedy, respektiert geschützte Kanten) | Exponentiell (Worst Case), praktisch klein durch SCC-Eingrenzung |
| Level-BFS | Parallelisierungsoptimierung | O(V+E) |

**Warum Tarjan zusätzlich zu Kahn/DFS?** Kahn und DFS erkennen lediglich *ob* ein Zyklus existiert. Tarjan identifiziert präzise *welche* Knoten eine Strongly Connected Component bilden – diese Information ist zwingend notwendig, damit Feedback Arc Set gezielt innerhalb der betroffenen Komponente nach entfernbaren Kanten suchen kann.

---

## Voraussetzungen

- **Java 21**
- **Maven 3.9+**
- **Python 3.x** (für Zusammenfassungen und Boxplots)
- **Graphviz** (für die Graph-Visualisierung) – [Download](https://graphviz.org/download/) (bei der Installation "Add Graphviz to PATH" auswählen)

Python-Abhängigkeiten installieren:

```bash
py -m pip install -r requirements.txt
```

---
## Installation

```bash
git clone <repository-url>
cd deployment-analyzer
mvn clean compile
```

---

## Verwendung

Es gibt mehrere unabhängige Skripte für unterschiedliche Anwendungsfälle. Alle liegen im Hauptverzeichnis und benötigen keine weiteren manuellen Schritte.

### 1. Deployment-Konfiguration erstellen

Es gibt zwei Wege, die Abhängigkeiten für den Deployment Analyzer bereitzustellen:

**Option A – Manuell schreiben:**

Eine eigene YAML-Datei im internen Format erstellen (siehe Abschnitt "YAML Format" weiter unten).

**Option B – Automatisch aus Docker Compose oder Kubernetes ableiten:**

Wenn bereits Docker-Compose- oder Kubernetes-Dateien existieren, muss nichts manuell geschrieben werden. Es reicht, die vorhandenen Dateien in den passenden Ordner zu legen:

- Docker Compose Dateien → `src/main/resources/examples/docker-manifests/`
- Kubernetes Deployment-Dateien → `src/main/resources/examples/kubernetes-manifests/`

#### Automatische Ableitung aus Docker Compose

```bash
convert_docker.bat
```

Liest alle Dateien aus `src/main/resources/examples/docker-manifests/` und erzeugt daraus automatisch `converted-docker.yaml` im internen Format.

**Hinweis:** Da `depends_on` bereits ein natives Docker-Compose-Feld ist, muss hier **nichts** zusätzlich geschrieben werden – die bestehende Datei wird einfach eingelesen. Nur wenn eine Abhängigkeit als geschützt markiert werden soll (`protected_dependencies`), muss eine zusätzliche Zeile (in Haupt docker Datei) in Form eines Labels ergänzt werden:

```yaml
api-gateway:
    image: api-gateway:latest
    depends_on:
      - auth-service
    labels:  // nur diese Zeile
      - "deployment-analyzer.protected-dependencies=auth-service"
```

#### Automatische Ableitung aus Kubernetes

```bash
convert_kubernetes.bat
```

Liest alle Dateien aus `src/main/resources/examples/kubernetes-manifests/` und erzeugt daraus automatisch `converted-kubernetes.yaml` im internen Format.

**Hinweis:** Kubernetes kennt kein natives Abhängigkeits-Konzept. Daher muss hier – im Gegensatz zu Docker Compose – eine Annotation ergänzt werden, die angibt, von welchen Services der jeweilige Service abhängt:

```yaml
metadata:
  name: auth-service
  annotations:
    deployment-analyzer/depends-on: "database,redis"
```

Soll eine dieser Abhängigkeiten zusätzlich geschützt werden, wird eine weitere Annotation ergänzt:

```yaml
metadata:
  name: auth-service
  annotations:
    deployment-analyzer/depends-on: "database,redis"
    deployment-analyzer/protected-dependencies: "database"
```

#### Ergebnis nutzen

Nach der Konvertierung den erzeugten Dateinamen in `Main.java` eintragen (Zeile 24):

```java
String filePath = "converted-docker.yaml";
// oder
String filePath = "converted-kubernetes.yaml";
```

---

### 2. Nur die Programm-Demo ausführen (schnell, keine Benchmarks)

Zeigt Graphaufbau, Deployment-Reihenfolge, Zyklenbehandlung, SVG-Visualisierung und JSON-Export für die in `Main.java` konfigurierte YAML-Datei.

```bash
run_demo.bat
```

Erzeugt:
- `dependency_graph.svg` – visuelle Darstellung des Abhängigkeitsgraphen
- `deployment_result.json` – maschinenlesbares Ergebnis (Reihenfolge, Parallelgruppen, Zyklen)

### 3. Nur den isolierten Algorithmus-Vergleich (Kahn vs. DFS)

Misst ausschließlich die Laufzeit der Sortieralgorithmen selbst, ohne YAML-Parsing/Graph-Konstruktion.

```bash
run_algorithm_benchmark.bat
```

Dauer: ca. 3–5 Minuten. Erzeugt `all_raw_results.json`, `summary_results.csv`, `boxplot_algorithm.png`, `boxplot_algorithm_log.png`.

### 4. Nur die Gesamtprogramm-Messung

Misst die komplette Laufzeit (YAML lesen + Graph aufbauen + Sortieren + Level-BFS), einmal mit Kahn, einmal mit DFS als Sortieralgorithmus.

```bash
run_full_program_benchmark.bat
```

Dauer: ca. 3–5 Minuten. Erzeugt `all_raw_results.json`, `summary_results.csv`, `boxplot_full_program_log.png`.

### 5. Komplette Evaluation (alles zusammen)

Führt beide Benchmarks, alle Zusammenfassungen, Boxplots und die Demo automatisch nacheinander aus.

```bash
run_evaluation.bat
```

Dauer: ca. 10 Minuten.

---

### Eigene YAML-Datei analysieren

Ändere den Pfad in `Main.java`:

```java
String filePath = "src/main/resources/examples/medium.yaml";
```

Verfügbare Standarddateien (mit hinterlegten JMH-Referenzwerten):

```java
// simple.yaml    →    5 Services
// medium.yaml    →   20 Services
// large.yaml     →   50 Services
// xlarge.yaml    →  100 Services
// xxlarge.yaml   →  500 Services
// xxxlarge.yaml  → 1000 Services
// xxxxlarge.yaml → 2000 Services
// cycle.yaml               → 5 Services mit Zyklus
// cycle-protected.yaml      → Zyklus mit teilweise geschützten Kanten
// cycle-unresolvable.yaml   → Zyklus, der wegen geschützter Kanten nicht lösbar ist
```

Bei eigenen, nicht registrierten YAML-Dateien greift automatisch eine einfache Laufzeitmessung als Fallback (mit entsprechendem Hinweis im Terminal).

---

## YAML Format

```yaml
version: "3"
services:
  frontend:
    depends_on:
      - api-gateway
  api-gateway:
    depends_on:
      - auth-service
  auth-service:
    depends_on:
      - database
    protected_dependencies:
      - database          # Diese Abhängigkeit darf vom
                           # Feedback Arc Set NICHT entfernt
                           # werden, falls sie Teil eines
                           # Zyklus ist
  database:
    depends_on: []
```

`protected_dependencies` ist optional und markiert essenzielle Abhängigkeiten, die bei der automatischen Zyklusauflösung nicht zur Disposition stehen. Besteht ein Zyklus ausschließlich aus geschützten Kanten, meldet das Programm dies explizit und bricht kontrolliert ab, statt fehlerhaft fortzufahren.

---

## Automatische Graph-Ableitung aus Docker Compose / Kubernetes

Anstatt Abhängigkeiten manuell in einer separaten YAML-Datei zu pflegen, kann der Abhängigkeitsgraph automatisch aus bereits bestehenden Deployment-Manifesten abgeleitet werden. Dies vermeidet doppelte Datenhaltung ("Single Source of Truth") und spart manuelle Arbeit – ohne den Einsatz von LLMs, rein regelbasiert und damit schnell, ressourcenschonend und verlässlich.

### Docker Compose

Docker Compose besitzt mit `depends_on` bereits ein natives Abhängigkeits-Feld – der Benutzer muss dafür **nichts** zusätzlich schreiben:

```yaml
version: "3"
services:
  auth-service:
    image: auth-service:latest
    depends_on:
      - database
```

Geschützte Abhängigkeiten werden über ein zusätzliches Label markiert (Docker Compose kennt dieses Konzept nicht nativ):

```yaml
services:
  api-gateway:
    image: api-gateway:latest
    depends_on:
      - auth-service
    labels:
      - "deployment-analyzer.protected-dependencies=auth-service"
```

### Kubernetes

Kubernetes kennt kein natives Abhängigkeits-Konzept. Abhängigkeiten werden daher explizit über Annotationen angegeben – analog zu `depends_on` in Docker Compose:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  annotations:
    deployment-analyzer/depends-on: "database,redis"
    deployment-analyzer/protected-dependencies: "database"
spec:
  template:
    spec:
      containers:
        - name: auth-service
          image: auth-service:latest
```

Jeder Service liegt dabei üblicherweise in einer eigenen Datei (Kubernetes-Konvention) – der `UniversalYamlParser` liest alle Dateien eines Ordners automatisch ein und baut daraus den vollständigen Graphen.
### Erkennungslogik

Der `UniversalYamlParser` erkennt das Format jeder Datei automatisch:

- Enthält die Datei den Schlüssel `services` → Docker Compose Format
- Enthält die Datei `kind: Deployment` → Kubernetes Format

Beide Formate werden zu derselben internen Abhängigkeitsstruktur zusammengeführt, die anschließend identisch zu einer manuell geschriebenen YAML-Datei weiterverarbeitet wird (Kahn, DFS, Tarjan, Feedback Arc Set, Level-BFS, SVG- und JSON-Export laufen unverändert).

---

## Beispiel Output – Kein Zyklus

```
╔════════════════════════════════════╗
║       DEPLOYMENT ANALYZER          ║
╚════════════════════════════════════╝

Lese YAML: src/main/resources/examples/simple.yaml

#=====================================#
#          GRAPH UEBERSICHT           #
#=====================================#
 Services: 5

 Siehe dependency_graph.svg fuer die visuelle Darstellung
 Siehe deployment_result.json fuer das maschinenlesbare Ergebnis
--------------------------------------

#=====================================#
#      KAHN'S ALGORITHMUS (BFS)       #
#=====================================#
 [OK] Kein Zyklus gefunden
--------------------------------------
 Deployment-Reihenfolge:
   1. database
   2. redis
   3. auth-service
   4. api-gateway
   5. frontend
--------------------------------------
 Speicher : 274.09 KB
--------------------------------------

#=====================================#
#        VERGLEICH: KAHN vs DFS       #
#=====================================#
 Graph-Groesse: 5 Services

 [JMH] Wissenschaftlich praezise Werte (aus summary_results.csv):
-------------------------------------------------------
                 Kahn                      DFS
-------------------------------------------------------
 Laufzeit        0.293 us/op               0.379 us/op
-------------------------------------------------------
 Schneller (JMH): Kahn
-------------------------------------------------------

#=====================================#
#      LEVEL-BFS PARALLELISIERUNG     #
#=====================================#
 Parallele Deployment-Gruppen:
   Level 0   -> [database, redis]
   Level 1   -> [auth-service]
   Level 2   -> [api-gateway]
   Level 3   -> [frontend]
--------------------------------------
 Zeitvergleich:
   Sequenziell : 5 Zeiteinheiten
   Parallel    : 4 Zeiteinheiten
   Ersparnis   : 1 Zeiteinheiten (20%)
--------------------------------------

#=====================================#
#   GESAMTPROGRAMM: KAHN vs DFS        #
#=====================================#
 [JMH] Gesamtlaufzeit (aus summary_results.csv):
-------------------------------------------------------
                 mit Kahn                  mit DFS
-------------------------------------------------------
 Laufzeit        0.147 ms/op               0.149 ms/op
-------------------------------------------------------
 Schneller (Gesamtprogramm): Kahn-Version
-------------------------------------------------------

 DOT Datei geschrieben: dependency_graph.dot
 Graph-Visualisierung erstellt: dependency_graph.svg
 JSON Ergebnis geschrieben: deployment_result.json

╔════════════════════════════════════╗
║      Analyse abgeschlossen         ║
╚════════════════════════════════════╝
```

---
## Beispiel Output – Zyklus mit geschützten Kanten (unlösbar)

```
#=====================================#
#        TARJAN'S ALGORITHMUS         #
#=====================================#
 [FEHLER] Zyklen gefunden: 1
 Zyklus 1: [user-service, auth-service]

#=====================================#
#          FEEDBACK ARC SET           #
#=====================================#
 [FEHLER] Folgende Zyklen KONNTEN NICHT
 aufgeloest werden - alle beteiligten
 Kanten sind als 'protected_dependencies'
 markiert:
   - [user-service, auth-service]

 URSACHE: Der Benutzer hat Abhaengigkeiten
 als 'protected_dependencies' geschuetzt,
 die gleichzeitig einen Zyklus bilden.
 Ein Zyklus kann nicht bestehen bleiben
 und gleichzeitig aufgeloest werden.

 => DEPLOYMENT NICHT MOEGLICH.
 Bitte entfernen Sie den Schutz von
 mindestens einer Abhaengigkeit im Zyklus,
 oder aendern Sie die Architektur.

╔════════════════════════════════════╗
║   Analyse abgebrochen - Deployment ║
║   aufgrund geschuetzter Zyklen     ║
║   nicht moeglich!                  ║
╚════════════════════════════════════╝
```

---

## Graph-Visualisierung

Jeder Lauf erzeugt automatisch `dependency_graph.svg`. Da SVG vektorbasiert ist, bleibt die Darstellung auch bei sehr großen Graphen (500+ Services) beim Hineinzoomen im Browser scharf lesbar.

**Farbcodierung der Knoten** (nach In-Degree):

| Farbe | Bedeutung |
|---|---|
| Weiß | In-Degree 0 (Basis-Service, keine Abhängigkeiten) |
| Hellgrün | In-Degree 1 |
| Gold | In-Degree 2 |
| Orange | In-Degree 3 |
| Orangerot | In-Degree 4+ |

**Farbcodierung der Kanten:**

| Darstellung | Bedeutung |
|---|---|
| Schwarz | Normale Abhängigkeit |
| Rot, dick | Geschützte Abhängigkeit (`protected_dependencies`) |
| Grau, gestrichelt | Vom Feedback Arc Set entfernte Kante (Zyklusauflösung) |

`A -> B` bedeutet: A muss vor B deployt werden (B hängt von A ab).

---

## Maschinenlesbarer Output (JSON)

Jeder Lauf erzeugt `deployment_result.json` mit folgender Struktur:

```json
{
  "services": 50,
  "hasCycle": false,
  "sccsWithCycles": [],
  "removedEdges": [],
  "deploymentOrder": ["database", "redis", "auth-service", "..."],
  "parallelGroups": {
    "level0": ["database", "redis"],
    "level1": ["auth-service"],
    "level2": ["api-gateway"]
  }
}
```

Dieses Format kann von externen Tools (z. B. Deployment-Skripten oder CI/CD-Pipelines) automatisch eingelesen werden, um Services in korrekter, teilweise paralleler Reihenfolge auszurollen.
## Wissenschaftliche Evaluation (JMH)

Die Laufzeitmessung erfolgt mit dem **Java Microbenchmark Harness (JMH)**, dem Standard-Framework für Microbenchmarking in Java. JMH führt automatisches Warmup durch, um JIT-Compiler-Effekte zu eliminieren, und nutzt mehrere unabhängige JVM-Forks zur Erfassung der Varianz. Jede Konfiguration wird über 10 Iterationen mit vorherigem 5-fachem Warmup und 2 Forks gemessen – insgesamt 20 Einzelmessungen pro Datenpunkt.

### Zwei Messebenen

1. **Isolierte Algorithmen** (`AlgorithmBenchmark.java`): Misst ausschließlich Kahn bzw. DFS auf einem bereits im Speicher befindlichen Graphen.
2. **Gesamtprogramm** (`FullProgramBenchmark.java`): Misst den kompletten Ablauf inklusive YAML-Parsing und Graph-Konstruktion.

Diese Trennung zeigt: Während bei isolierter Messung klare Laufzeitunterschiede zwischen Kahn und DFS bestehen, verschwindet dieser Unterschied bei Betrachtung des Gesamtprogramms nahezu vollständig – der Sortieralgorithmus selbst macht nur einen geringen Anteil der Gesamtlaufzeit aus.

### Reproduzierbarkeit

Alle Messungen sind vollautomatisch reproduzierbar über die bereitgestellten `.bat`-Skripte (siehe Abschnitt Verwendung). Es ist keine manuelle Konfiguration über die Skripte hinaus notwendig.

---

## Projektstruktur

```
deployment-analyzer/
├── src/
│   ├── main/java/com/deployment/
│   │   ├── algorithm/
│   │   │   ├── DFSTopologicalSort.java
│   │   │   ├── FeedbackArcSet.java       (mit protected_dependencies Support)
│   │   │   ├── KahnAlgorithm.java
│   │   │   ├── LevelBFS.java
│   │   │   └── TarjanAlgorithm.java
│   │   ├── benchmark/
│   │   │   ├── AlgorithmBenchmark.java
│   │   │   ├── FullProgramBenchmark.java
│   │   │   ├── RunAllBenchmarks.java
│   │   │   ├── RunAlgorithmBenchmark.java
│   │   │   ├── RunFullProgramBenchmark.java
│   │   │   ├── GraphSizeConfig.java
│   │   │   └── CsvResultReader.java
│   │   ├── export/
│   │   │   ├── GraphvizExporter.java
│   │   │   └── JsonExporter.java
│   │   ├── model/
│   │   │   ├── Graph.java
│   │   │   └── Service.java
│   │   ├── parser/
│   │   │   ├── YamlParser.java
│   │   │   └── UniversalYamlParser.java    (Docker Compose + Kubernetes)
│   │   ├── validator/
│   │   │   └── Validator.java
│   │   ├── ConvertToInternalFormat.java     (Standalone-Konvertierungstool)
│   │   ├── GraphGenerator.java
│   │   ├── Main.java
│   │   └── ResultPrinter.java
│   └── resources/examples/
│       ├── simple.yaml                     (5 Services, eigenständig definiert)
│       ├── showcase-10-services.yaml       (10 Services, eigenständig definiert)
│       ├── medium.yaml                     (20 Services, eigenständig definiert)
│       ├── large.yaml                      (50 Services, generiert)
│       ├── xlarge.yaml                     (100 Services, generiert)
│       ├── xxlarge.yaml                    (500 Services, generiert)
│       ├── xxxlarge.yaml                   (1000 Services, generiert)
│       ├── xxxxlarge.yaml                  (2000 Services, generiert)
│       ├── cycle.yaml                      (5 Services, Zyklus, frei lösbar)
│       ├── cycle-protected.yaml            (Zyklus, teilweise geschützt)
│       ├── cycle-unresolvable.yaml         (Zyklus, vollständig geschützt)
│       ├── docker-manifests/               (Docker Compose Testdaten)
│       └── kubernetes-manifests/           (Kubernetes Testdaten)
├── create_summary.py
├── create_boxplots.py
├── correctness_checker.py
├── cycle_resolution_checker.py
├── run_demo.bat
├── run_algorithm_benchmark.bat
├── run_full_program_benchmark.bat
├── run_evaluation.bat
├── convert_docker.bat
├── convert_kubernetes.bat
├── requirements.txt
├── README.md
└── pom.xml
```

---

## Technologien

```
Java 21  •  Maven 3.9  •  SnakeYAML 2.0  •  JMH 1.37  •  Graphviz  •  Python (matplotlib)
```

---

## Autor

<table>
  <tr>
    <td align="center">
      <strong>Mohammad El Zein</strong><br>
      Bachelor Informatik @ TU Dortmund<br><br>
      <a href="https://github.com/Mohammad-El-Zein">
        <img src="https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white" alt="GitHub"/>
      </a>
    </td>
  </tr>
</table>

---

## Lizenz

Dieses Projekt wurde im Rahmen einer Bachelorarbeit an der TU Dortmund erstellt.

**TU Dortmund – Lehrstuhl Algorithm Engineering**