package com.example.routing.infrastructure;

import com.example.routing.domain.model.CountryGraph;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GraphConfiguration {

    private final CountryDataLoader dataLoader;

    @Bean
    public CountryGraph countryGraph() {
        CountryGraph graph = dataLoader.loadCountryGraph();
        log.info("RoutingService initialized with {} countries", graph.size());
        return graph;
    }
}
