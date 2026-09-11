package com.deployment.model;

/**
 * Repräsentiert einen Microservice als Knoten im Abhängigkeitsgraph
 * also jeder Service ist ein knoten und die Abhängigkeiten sind gerichtete Kanten
 */
public class Service {
    
    // Name des Services z.B. "database"
    private String name;
    
    // Wenn ein neuer Service erstellt wird, muss er einen Namen haben
    public Service(String name) {
        this.name = name;
    }
    
    // Getter, gibt den Namen des Services zurück, zb service.getName() -> "database"
    public String getName() {
        return name;
    }
    
    // toString Methode, damit wir den Service einfach als String ausgeben können, 
    @Override
    public String toString() {
        return name;
    }
    
    //vergleicht zwei Services, damit wir z.B. in Sets oder Maps Services vergleichen können,
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Service)) return false;
        Service other = (Service) obj;
        return this.name.equals(other.name);
    }
    
    //hashCode Methode, damit Services in Hash-basierten Collections wie HashSet oder HashMap korrekt funktionieren,
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}