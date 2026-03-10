package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.circuit.dto.CircuitCreateDTO;
import com.dionialves.AsteraComm.exception.GlobalExceptionHandler;
import com.dionialves.AsteraComm.exception.NotFoundException;
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
class CircuitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CircuitService circuitService;

    @InjectMocks
    private CircuitController circuitController;

    private Circuit testCircuit;

    @BeforeEach
    void setUp() {
        testCircuit = new Circuit();
        testCircuit.setNumber("100000");
        testCircuit.setPassword("secret");
        testCircuit.setTrunkName("opasuite");

        mockMvc = MockMvcBuilders.standaloneSetup(circuitController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAll_shouldReturn200_paginated() throws Exception {
        var page = new PageImpl<CircuitProjection>(List.of(), PageRequest.of(0, 10), 0);
        when(circuitService.getAll(anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/api/circuits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAll_withSearch_shouldPassParam() throws Exception {
        var page = new PageImpl<CircuitProjection>(List.of(), PageRequest.of(0, 10), 0);
        when(circuitService.getAll(eq("100000"), any())).thenReturn(page);

        mockMvc.perform(get("/api/circuits?search=100000"))
                .andExpect(status().isOk());

        verify(circuitService).getAll(eq("100000"), any());
    }

    @Test
    void getById_shouldReturn200_whenExists() throws Exception {
        when(circuitService.findByNumber("100000")).thenReturn(Optional.of(testCircuit));

        mockMvc.perform(get("/api/circuits/100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("100000"));
    }

    @Test
    void getById_shouldReturn404_whenNotExists() throws Exception {
        when(circuitService.findByNumber("999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/circuits/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_withValidBody() throws Exception {
        when(circuitService.create(any(CircuitCreateDTO.class))).thenReturn(testCircuit);

        mockMvc.perform(post("/api/circuits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret\",\"trunkName\":\"opasuite\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void update_shouldReturn200_withValidBody() throws Exception {
        when(circuitService.update(eq("100000"), any(CircuitCreateDTO.class))).thenReturn(testCircuit);

        mockMvc.perform(put("/api/circuits/100000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newpassword\",\"trunkName\":\"opasuite\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn404_whenNotExists() throws Exception {
        when(circuitService.update(eq("999999"), any(CircuitCreateDTO.class)))
                .thenThrow(new NotFoundException("Circuito não encontrado"));

        mockMvc.perform(put("/api/circuits/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newpassword\",\"trunkName\":\"opasuite\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(circuitService).delete("100000");

        mockMvc.perform(delete("/api/circuits/100000"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn404_whenNotExists() throws Exception {
        doThrow(new NotFoundException("Circuito não encontrado"))
                .when(circuitService).delete("999999");

        mockMvc.perform(delete("/api/circuits/999999"))
                .andExpect(status().isNotFound());
    }
}
