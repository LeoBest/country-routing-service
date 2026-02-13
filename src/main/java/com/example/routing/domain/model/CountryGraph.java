package com.example.routing.domain.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record CountryGraph(Map<String, Set<String>> adjacencyList) {
    
    public CountryGraph {
        adjacencyList = Collections.unmodifiableMap(adjacencyList);
    }

    public boolean hasCountry(String countryCode) {
        return adjacencyList.containsKey(countryCode);
    }

    public Set<String> getNeighbors(String countryCode) {
        return adjacencyList.getOrDefault(countryCode, Set.of());
    }

    public int size() {
        return adjacencyList.size();
    }
}
