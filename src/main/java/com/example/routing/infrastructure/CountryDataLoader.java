package com.example.routing.infrastructure;

import com.example.routing.domain.model.CountryGraph;
import com.example.routing.infrastructure.model.Country;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountryDataLoader {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${routing.data-url}")
    private String dataUrl;

    public CountryGraph loadCountryGraph() {
        try {
            log.info("Loading country data from: {}", dataUrl);

            String json = restClient
                    .get()
                    .uri(dataUrl)
                    .retrieve()
                    .body(String.class);

            Country[] countries = objectMapper.readValue(json, Country[].class);
            Map<String, Set<String>> adjacencyList = buildAdjacencyList(countries);

            int totalBorders = adjacencyList.values().stream()
                    .mapToInt(Set::size)
                    .sum();

            log.info("Successfully loaded {} countries with {} total borders", adjacencyList.size(), totalBorders);

            return new CountryGraph(adjacencyList);

        } catch (Exception e) {
            log.error("Failed to load country data from {}", dataUrl, e);
            throw new RuntimeException("Failed to load country data: " + e.getMessage(), e);
        }
    }

    private Map<String, Set<String>> buildAdjacencyList(Country[] countries) {
        Map<String, Set<String>> graph = new HashMap<>();

        for (Country country : countries) {
            Set<String> neighbors = new HashSet<>();
            if (country.borders() != null) {
                neighbors.addAll(country.borders());
            }
            graph.put(country.cca3(), neighbors);
        }

        return graph;
    }
}
