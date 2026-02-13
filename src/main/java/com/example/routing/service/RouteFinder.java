package com.example.routing.service;

import java.util.List;

public interface RouteFinder {

    List<String> findRoute(String origin, String destination);
}
