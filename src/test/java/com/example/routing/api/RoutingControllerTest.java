package com.example.routing.api;

import com.example.routing.domain.exception.RouteNotFoundException;
import com.example.routing.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoutingController.class)
class RoutingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoutingService routingService;

    @Test
    void getRoute_success() throws Exception {
        when(routingService.findRoute("CZE", "ITA"))
                .thenReturn(List.of("CZE", "AUT", "ITA"));

        mockMvc.perform(get("/routing/CZE/ITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route[0]").value("CZE"))
                .andExpect(jsonPath("$.route[2]").value("ITA"));
    }

    @Test
    void getRoute_noRoute_returns400() throws Exception {
        when(routingService.findRoute("USA", "AUS"))
                .thenThrow(new RouteNotFoundException("USA", "AUS"));

        mockMvc.perform(get("/routing/USA/AUS"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRoute_unknownCountry_returns400WithMessage() throws Exception {
        when(routingService.findRoute("XXX", "ITA"))
                .thenThrow(new IllegalArgumentException("Unknown origin country: XXX"));

        mockMvc.perform(get("/routing/XXX/ITA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
