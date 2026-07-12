package com.deployment;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generiert YAML Dateien mit beliebig vielen Services
 * für die Evaluation in Kapitel 6
 */
public class GraphGenerator {

    /**
     * Generiert eine YAML Datei mit n Services
     */
    public static void generate(
        int serviceCount,
        String fileName) {

    StringBuilder yaml = new StringBuilder();
    yaml.append("version: \"3\"\n");
    yaml.append("services:\n");

    int generated = 0;

    // Schicht 1: Datenbanken (15% der Services)
    int dbCount = Math.max(1, serviceCount * 15 / 100);
    List<String> databases = new ArrayList<>();
    for (int i = 1; i <= dbCount; i++) {
        String name = "database-" + i;
        databases.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on: []\n");
        generated++;
    }

    // Schicht 2: Caches (10% der Services)
    int cacheCount = Math.max(1, serviceCount * 10 / 100);
    List<String> caches = new ArrayList<>();
    for (int i = 1; i <= cacheCount; i++) {
        String name = "cache-" + i;
        caches.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on: []\n");
        generated++;
    }

    // Schicht 3: Message Broker (5% der Services)
    int brokerCount = Math.max(1, serviceCount * 5 / 100);
    List<String> brokers = new ArrayList<>();
    for (int i = 1; i <= brokerCount; i++) {
        String name = "broker-" + i;
        brokers.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on: []\n");
        generated++;
    }

    // Schicht 4: Auth Services (10% der Services)
    int authCount = Math.max(1, serviceCount * 10 / 100);
    List<String> authServices = new ArrayList<>();
    for (int i = 1; i <= authCount; i++) {
        String name = "auth-service-" + i;
        authServices.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on:\n");
        yaml.append("      - ")
            .append(databases.get(i % databases.size()))
            .append("\n");
        yaml.append("      - ")
            .append(caches.get(i % caches.size()))
            .append("\n");
        generated++;
    }

    // Schicht 5: Core Services (20% der Services)
    int coreCount = Math.max(2, serviceCount * 20 / 100);
    List<String> coreServices = new ArrayList<>();
    for (int i = 1; i <= coreCount; i++) {
        String name = "core-service-" + i;
        coreServices.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on:\n");
        yaml.append("      - ")
            .append(authServices.get(i % authServices.size()))
            .append("\n");
        yaml.append("      - ")
            .append(databases.get(i % databases.size()))
            .append("\n");
        generated++;
    }

    // Schicht 6: Business Services (25% der Services)
    int businessCount = Math.max(2, serviceCount * 25 / 100);
    List<String> businessServices = new ArrayList<>();
    for (int i = 1; i <= businessCount; i++) {
        String name = "business-service-" + i;
        businessServices.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on:\n");
        yaml.append("      - ")
            .append(coreServices.get(i % coreServices.size()))
            .append("\n");
        yaml.append("      - ")
            .append(brokers.get(i % brokers.size()))
            .append("\n");
        generated++;
    }

    // Schicht 7: API Gateways (5% der Services)
    int gatewayCount = Math.max(1, serviceCount * 5 / 100);
    List<String> gateways = new ArrayList<>();
    for (int i = 1; i <= gatewayCount; i++) {
        String name = "api-gateway-" + i;
        gateways.add(name);
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on:\n");
        for (String auth : authServices) {
            yaml.append("      - ").append(auth).append("\n");
        }
        yaml.append("      - ")
            .append(coreServices.get(0))
            .append("\n");
        generated++;
    }

    // Schicht 8: Frontend Services (Rest der Services)
    int frontendCount = serviceCount - generated;
    for (int i = 1; i <= frontendCount; i++) {
        String name = "frontend-" + i;
        yaml.append("  ").append(name).append(":\n");
        yaml.append("    depends_on:\n");
        yaml.append("      - ")
            .append(gateways.get(i % gateways.size()))
            .append("\n");
        generated++;
    }

    // Datei schreiben
    String path =
        "src/main/resources/examples/" + fileName;

    try (FileWriter writer = new FileWriter(path)) {
        writer.write(yaml.toString());
        System.out.println(
            "Generator: " + fileName
            + " erstellt mit "
            + generated + " Services!");
    } catch (IOException e) {
        System.out.println(
            "Fehler: " + e.getMessage());
    }
}

    public static void main(String[] args) {
        System.out.println(
            "Generiere YAML Dateien...\n");

        generate(50,   "large.yaml");
        generate(100,  "xlarge.yaml");
        generate(500,  "xxlarge.yaml");
        generate(1000, "xxxlarge.yaml");
        generate(2000, "xxxxlarge.yaml");



        System.out.println(
            "\nAlle Dateien erstellt!");
    }
}