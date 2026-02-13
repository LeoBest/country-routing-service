package com.example.routing.domain.exception;

public class RouteNotFoundException extends RuntimeException {

    public RouteNotFoundException(String origin, String destination) {
        super("No land route found from %s to %s".formatted(origin, destination));
    }
}
