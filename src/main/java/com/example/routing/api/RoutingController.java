package com.example.routing.api;

import com.example.routing.api.dto.ErrorResponse;
import com.example.routing.api.dto.RouteResponse;
import com.example.routing.domain.exception.RouteNotFoundException;
import com.example.routing.service.RoutingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/routing")
public class RoutingController {

    private final RoutingService routingService;

    @GetMapping("/{origin}/{destination}")
    public ResponseEntity<RouteResponse> getRoute(
            @PathVariable String origin,
            @PathVariable String destination) {

        List<String> route = routingService.findRoute(origin, destination);
        return ResponseEntity.ok(new RouteResponse(route));
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<Void> handleRouteNotFound(RouteNotFoundException e) {
        log.warn("Route not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Invalid request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }
}
