package com.example.routing.service;

import com.example.routing.domain.exception.RouteNotFoundException;
import com.example.routing.domain.model.CountryGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoutingServiceTest {

    private static final CountryGraph GRAPH = new CountryGraph(Map.of(
            "CZE", Set.of("AUT"),
            "AUT", Set.of("CZE", "ITA"),
            "ITA", Set.of("AUT")
    ));

    private final RoutingService service = new RoutingService(GRAPH, new BfsRouteFinder(GRAPH));

    @Test
    void findRoute_success() {
        assertThat(service.findRoute("CZE", "ITA"))
                .containsExactly("CZE", "AUT", "ITA");
    }

    @Test
    void findRoute_noRoute() {
        CountryGraph isolatedGraph = new CountryGraph(Map.of(
                "USA", Set.of(),
                "AUS", Set.of()
        ));
        RoutingService isolatedService = new RoutingService(isolatedGraph, new BfsRouteFinder(isolatedGraph));

        assertThatThrownBy(() -> isolatedService.findRoute("USA", "AUS"))
                .isInstanceOf(RouteNotFoundException.class);
    }

    @ParameterizedTest
    @CsvSource({"XXX, ITA", "CZE, XXX"})
    void findRoute_unknownCountry(String origin, String destination) {
        assertThatThrownBy(() -> service.findRoute(origin, destination))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
