package com.dionialves.AsteraComm.cdr;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CdrControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CdrService cdrService;

    @InjectMocks
    private CdrController cdrController;

    private CdrRecord testCdr;

    @BeforeEach
    void setUp() {
        testCdr = new CdrRecord();
        testCdr.setUniqueId("1700000000.1");
        testCdr.setSrc("1001");
        testCdr.setDst("1002");
        testCdr.setCalldate(LocalDateTime.of(2026, 1, 15, 10, 30, 0));
        testCdr.setDuration(120);
        testCdr.setBillsec(115);
        testCdr.setDisposition("ANSWERED");

        mockMvc = MockMvcBuilders.standaloneSetup(cdrController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAll_shouldReturn200_paginated() throws Exception {
        var page = new PageImpl<CdrRecord>(List.of(), PageRequest.of(0, 20), 0);
        when(cdrService.getAll(any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/cdrs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getAll_withSrcFilter_shouldPassParam() throws Exception {
        var page = new PageImpl<CdrRecord>(List.of(), PageRequest.of(0, 20), 0);
        when(cdrService.getAll(eq("1001"), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/cdrs?src=1001"))
                .andExpect(status().isOk());

        verify(cdrService).getAll(eq("1001"), any(), any(), any(), any(), any());
    }

    @Test
    void getAll_withAllFilters_shouldPassAllParams() throws Exception {
        var page = new PageImpl<CdrRecord>(List.of(), PageRequest.of(0, 20), 0);
        when(cdrService.getAll(eq("1001"), eq("1002"), eq("ANSWERED"), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/cdrs?src=1001&dst=1002&disposition=ANSWERED"))
                .andExpect(status().isOk());

        verify(cdrService).getAll(eq("1001"), eq("1002"), eq("ANSWERED"), any(), any(), any());
    }

    @Test
    void getAll_withDateRange_shouldPassDateParams() throws Exception {
        var page = new PageImpl<CdrRecord>(List.of(), PageRequest.of(0, 20), 0);
        when(cdrService.getAll(any(), any(), any(),
                eq(LocalDateTime.of(2026, 1, 1, 0, 0, 0)),
                eq(LocalDateTime.of(2026, 1, 31, 23, 59, 59)),
                any())).thenReturn(page);

        mockMvc.perform(get("/api/cdrs?from=2026-01-01T00:00:00&to=2026-01-31T23:59:59"))
                .andExpect(status().isOk());

        verify(cdrService).getAll(any(), any(), any(),
                eq(LocalDateTime.of(2026, 1, 1, 0, 0, 0)),
                eq(LocalDateTime.of(2026, 1, 31, 23, 59, 59)),
                any());
    }

    @Test
    void getByUniqueId_shouldReturn200_whenExists() throws Exception {
        when(cdrService.findByUniqueId("1700000000.1")).thenReturn(Optional.of(testCdr));

        mockMvc.perform(get("/api/cdrs/{uniqueid}", "1700000000.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueid").value("1700000000.1"))
                .andExpect(jsonPath("$.src").value("1001"))
                .andExpect(jsonPath("$.dst").value("1002"))
                .andExpect(jsonPath("$.disposition").value("ANSWERED"));
    }

    @Test
    void getByUniqueId_shouldReturn404_whenNotExists() throws Exception {
        when(cdrService.findByUniqueId("inexistente")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cdrs/{uniqueid}", "inexistente"))
                .andExpect(status().isNotFound());
    }
}
