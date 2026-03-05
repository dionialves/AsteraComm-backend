package com.dionialves.AsteraComm.asterisk.endpoint;

import com.dionialves.AsteraComm.asterisk.endpoint.dto.EndpointCreateDTO;
import com.dionialves.AsteraComm.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EndpointControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EndpointService endpointService;

    @InjectMocks
    private EndpointController endpointController;

    private Endpoint testEndpoint;

    @BeforeEach
    void setUp() {
        testEndpoint = new Endpoint();
        testEndpoint.setId("1001");
        testEndpoint.setCallerid("1001");

        mockMvc = MockMvcBuilders.standaloneSetup(endpointController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAll_shouldReturn200_paginated() throws Exception {
        var page = new PageImpl<EndpointProjection>(List.of(), PageRequest.of(0, 10), 0);
        when(endpointService.getAllEndpointData(anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/api/circuits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAll_withSearch_shouldPassParam() throws Exception {
        var page = new PageImpl<EndpointProjection>(List.of(), PageRequest.of(0, 10), 0);
        when(endpointService.getAllEndpointData(eq("1001"), any())).thenReturn(page);

        mockMvc.perform(get("/api/circuits?search=1001"))
                .andExpect(status().isOk());

        verify(endpointService).getAllEndpointData(eq("1001"), any());
    }

    @Test
    void getById_shouldReturn200_whenExists() throws Exception {
        when(endpointService.findByid("1001")).thenReturn(Optional.of(testEndpoint));

        mockMvc.perform(get("/api/circuits/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1001"));
    }

    @Test
    void getById_shouldReturn404_whenNotExists() throws Exception {
        when(endpointService.findByid("9999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/circuits/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_withValidBody() throws Exception {
        when(endpointService.create(any(EndpointCreateDTO.class))).thenReturn(testEndpoint);

        mockMvc.perform(post("/api/circuits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":\"1001\",\"password\":\"secret\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void update_shouldReturn200_withValidBody() throws Exception {
        when(endpointService.update(eq("1001"), any(EndpointCreateDTO.class))).thenReturn(testEndpoint);

        mockMvc.perform(put("/api/circuits/1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":\"1001\",\"password\":\"newpassword\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(endpointService).delete("1001");

        mockMvc.perform(delete("/api/circuits/1001"))
                .andExpect(status().isNoContent());
    }
}
