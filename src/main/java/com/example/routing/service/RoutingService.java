package com.example.routing.service;

import com.example.routing.domain.model.CountryGraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final CountryGraph graph;
    private final RouteFinder routeFinder;

    public List<String> findRoute(String origin, String destination) {
        validateCountries(origin, destination);
        return routeFinder.findRoute(origin, destination);
    }

    private void validateCountries(String origin, String destination) {
        if (!graph.hasCountry(origin)) {
            throw new IllegalArgumentException("Unknown origin country: " + origin);
        }
        if (!graph.hasCountry(destination)) {
            throw new IllegalArgumentException("Unknown destination country: " + destination);
        }
    }
}
