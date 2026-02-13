package com.example.routing.service;

import com.example.routing.domain.exception.RouteNotFoundException;
import com.example.routing.domain.model.CountryGraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BfsRouteFinder implements RouteFinder {

    private final CountryGraph graph;

    @Override
    public List<String> findRoute(String origin, String destination) {
        if (origin.equals(destination)) {
            return List.of(origin);
        }

        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(destination)) {
                return reconstructPath(parent, destination);
            }

            for (String neighbor : graph.getNeighbors(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        throw new RouteNotFoundException(origin, destination);
    }

    private List<String> reconstructPath(Map<String, String> parent, String destination) {
        var path = new ArrayDeque<String>();
        for (String c = destination; c != null; c = parent.get(c)) {
            path.addFirst(c);
        }
        return new ArrayList<>(path);
    }
}
