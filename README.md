# Deployment Analyzer

mvn clean package
java -jar target/benchmarks.jar FullProgramBenchmark -rf csv -rff full_program_results.csv
java -jar target/benchmarks.jar AlgorithmBenchmark -rf csv -rff algorithm_results.csv
py create_summary.py


<p align="center">
  <img src="https://img.shields.io/badge/TU-Dortmund-4CAF50?style=for-the-badge" alt="Algorithm Engineering"/>
  <img src="https://img.shields.io/badge/Algorithm-Engineering-003DA5?style=for-the-badge" alt="TU Dortmund"/>
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

Dieses Tool analysiert Microservice-Abhängigkeiten automatisch und berechnet die optimale Deployment-Reihenfolge mithilfe von Graphalgorithmen. Der Benutzer definiert nur die Abhängigkeiten zwischen den Services in einer YAML-Datei – die korrekte Reihenfolge, Zyklenerkennung und Parallelisierungsoptimierung übernimmt das System vollautomatisch.

### Motivation

Das Deployment von Microservices in modernen Cloud-Umgebungen ist fehleranfällig:Zyklische Abhängigkeiten blockieren Systeme, falsche Startreihenfolgen verursachen CrashLoopBackOff und hohe Cloud-Kosten, und bei 1000+ Services ist manuelle  Analyse nicht mehr praktikabel.
Diese Arbeit löst dieses Problem durch einen graphbasierten Ansatz mit fünf Algorithmen – von der Reihenfolgeberechnung bis zur automatischen Zyklusauflösung.

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
- Nachgewiesene Skalierbarkeit bei über 1000+ Services
- Experimenteller Vergleich der Algorithmen

### Ablauf

```
YAML Datei (nur Abhängigkeiten angeben)
        ↓
Graph aufbauen
        ↓
Kahn oder DFS → Reihenfolge berechnen
        ↓
Zyklus gefunden?
    ↙         ↘
  JA           NEIN
   ↓               ↓
Tarjan          Level-BFS
→ Zyklus        → Parallele
  lokalisieren    Gruppen
   ↓
Feedback Arc Set
→ Lösungsvorschlag 
→ Level-BFS erneut
```

---

## Algorithmen

| Algorithmus | Zweck | Komplexität |
|---|---|---|
| Kahn's Algorithmus | Topologische Sortierung (BFS) | O(V+E) |
| DFS Topologische Sortierung | Topologische Sortierung (DFS) | O(V+E) |
| Tarjan's Algorithmus | Zyklenerkennung (SCC) | O(V+E) |
| Feedback Arc Set | Zyklusauflösung (Greedy) | O(V·E) |
| Level-BFS | Parallelisierungsoptimierung | O(V+E) |

---

## Voraussetzungen

- Java 21
- Maven 3.9+

---

## Installation

```bash
git clone https://github.com/Mohammad-El-Zein/deployment-analyzer.git
cd deployment-analyzer
mvn compile
```

---

## Verwendung

### 1. Deployment Analyse

Standardmäßig wird `simple.yaml` (5 Services) analysiert.

Um eine andere YAML Datei zu analysieren, ändere den Pfad in `Main.java`:

```java
String filePath = "src/main/resources/examples/simple.yaml";
// Andere Optionen:
// String filePath = "src/main/resources/examples/medium.yaml";           // 20 Services
// String filePath = "src/main/resources/examples/large.yaml";            // 50 Services
// String filePath = "src/main/resources/examples/xlarge.yaml";           // 100 Services
// String filePath = "src/main/resources/examples/xxlarge.yaml";          // 1000 Services
// String filePath = "src/main/resources/examples/cycle.yaml";            // 3 Services mit Zyklus
// String filePath = "src/main/resources/examples/xlarge-withCycle.yaml"; // 104 Services mit Zyklus
```

Dann ausführen:

```bash
mvn exec:java
```

### 2. Evaluation (Kahn vs DFS Vergleich)

In `pom.xml` ändern:

```xml
com.deployment.EvaluationRunner
```

Dann:

```bash
mvn exec:java
```

### 3. YAML Dateien generieren

Die Testdateien für 50, 100 und 1000 Services sind bereits im Repository enthalten.

Falls du eine eigene Größe generieren möchtest, füge in `GraphGenerator.java` am Ende der `main()` Methode folgende Zeile hinzu:

```java
generate(deine_gewuenschte_anzahl, "gewuenschter_name.yaml");
```

Dann in `pom.xml` ändern:

```xml
com.deployment.GraphGenerator
```

Und ausführen:

```bash
mvn exec:java
```

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
  database:
    depends_on: []
```

---

## Beispiel Output – Kein Zyklus (simple.yaml)

```
╔════════════════════════════════════╗
║       DEPLOYMENT ANALYZER          ║
╚════════════════════════════════════╝

Lese YAML: src/main/resources/examples/simple.yaml

#=====================================#
#          GRAPH UEBERSICHT           #
#=====================================#
 Services: 5
-----------------------------------------------------
 auth-service  -> [api-gateway]        (In-Degree: 2)
 database      -> [auth-service]       (In-Degree: 0)
 api-gateway   -> [frontend]           (In-Degree: 1)
 frontend      -> []                   (In-Degree: 1)
 redis         -> [auth-service]       (In-Degree: 0)
-----------------------------------------------------

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
 Laufzeit : 308900 ns (0.309 ms)
 Speicher : 274.09 KB
--------------------------------------

#=====================================#
#     DFS TOPOLOGISCHE SORTIERUNG     #
#=====================================#
 [OK] Kein Zyklus gefunden!
--------------------------------------
 Deployment-Reihenfolge:
   1. redis
   2. database
   3. auth-service
   4. api-gateway
   5. frontend
--------------------------------------
 Laufzeit : 131900 ns (0.132 ms)
 Speicher : 167.50 KB
--------------------------------------

#=====================================#
#        VERGLEICH: KAHN vs DFS       #
#=====================================#
 Graph-Groesse: 5 Services

                 Kahn                 DFS
---------------------------------------------------------
 Laufzeit        308900 ns            131900 ns
 in ms           0.309 ms             0.132 ms
 Speicher        274.09 KB            167.50 KB
 Zyklus          Nein                 Nein
--------------------------------------
 Schneller        : DFS
 Weniger Speicher : DFS
--------------------------------------

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
 Algorithmus-Laufzeit: 59300 ns (0.059 ms)
--------------------------------------

╔════════════════════════════════════╗
║      Analyse abgeschlossen         ║
╚════════════════════════════════════╝
```

---

## Beispiel Output – Zyklus gefunden (cycle.yaml)

```
#=====================================#
#      KAHN'S ALGORITHMUS (BFS)       #
#=====================================#
 [FEHLER] Zyklus gefunden!
 Deployment nicht moeglich.
--------------------------------------

#=====================================#
#        TARJAN'S ALGORITHMUS         #
#=====================================#
 [FEHLER] Zyklen gefunden: 1
--------------------------------------
 Zyklus 1: [user-service, auth-service]
--------------------------------------

#=====================================#
#          FEEDBACK ARC SET           #
#=====================================#
 Entferne folgende Abhaengigkeiten:
   [X] user-service -> auth-service
 [OK] Nach Entfernung: Kein Zyklus mehr!
--------------------------------------

#=====================================#
#      LEVEL-BFS PARALLELISIERUNG     #
#=====================================#
 Level 0 -> [database, auth-service]
 Level 1 -> [user-service]
--------------------------------------
```

---

## Projektstruktur

```
deployment-analyzer/
├── src/
│   ├── main/java/com/deployment/
│   │   ├── algorithm/
│   │   │   ├── DFSTopologicalSort.java
│   │   │   ├── FeedbackArcSet.java
│   │   │   ├── KahnAlgorithm.java
│   │   │   ├── LevelBFS.java
│   │   │   └── TarjanAlgorithm.java
│   │   ├── model/
│   │   │   ├── Graph.java
│   │   │   └── Service.java
│   │   ├── parser/
│   │   │   └── YamlParser.java
│   │   ├── validator/
│   │   │   └── Validator.java
│   │   ├── EvaluationRunner.java
│   │   ├── GraphGenerator.java
│   │   ├── Main.java
│   │   └── ResultPrinter.java
│   └── resources/examples/
│       ├── simple.yaml            (5 Services)
│       ├── medium.yaml            (20 Services)
│       ├── large.yaml             (50 Services)
│       ├── xlarge.yaml            (100 Services)
│       ├── xxlarge.yaml           (1000 Services)
│       ├── cycle.yaml             (3 Services mit Zyklus)
│       └── xlarge-withCycle.yaml  (104 Services mit Zyklus)
└── pom.xml
```

---

## Testdaten

| Datei | Services | Zyklus | Parallelersparnis |
|---|---|---|---|
| simple.yaml | 5 | Nein | 20% |
| medium.yaml | 20 | Nein | 55% |
| large.yaml | 50 | Nein | 90% |
| xlarge.yaml | 100 | Nein | 95% |
| xxlarge.yaml | 1000 | Nein | 99% |
| cycle.yaml | 3 | Ja | 33% |
| xlarge-withCycle.yaml | 104 | Ja | 95% |

---

## Evaluation Ergebnisse (Kahn vs DFS)

| Services | Kahn (avg) | DFS (avg) | Schneller |
|---|---|---|---|
| 5 | 0.109 ms | 0.061 ms | DFS |
| 20 | 0.150 ms | 0.120 ms | DFS |
| 50 | 0.185 ms | 0.180 ms | DFS |
| 100 | 0.265 ms | 0.196 ms | DFS |
| 1000 | 1.941 ms | 1.070 ms | DFS |

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


