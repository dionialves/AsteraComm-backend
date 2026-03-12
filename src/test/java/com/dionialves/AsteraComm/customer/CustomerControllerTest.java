package com.dionialves.AsteraComm.customer;

import com.dionialves.AsteraComm.customer.dto.CustomerCreateDTO;
import com.dionialves.AsteraComm.exception.BusinessException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("Acme Corp");
        testCustomer.setEnabled(true);
        testCustomer.setCreatedAt(LocalDateTime.now());
        testCustomer.setUpdatedAt(LocalDateTime.now());

        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAll_shouldReturn200_withPage() throws Exception {
        var page = new PageImpl<>(List.of(testCustomer), PageRequest.of(0, 10), 1);
        when(customerService.getAll(any())).thenReturn(page);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Acme Corp"));
    }

    @Test
    void getById_shouldReturn200_whenExists() throws Exception {
        when(customerService.findById(1L)).thenReturn(Optional.of(testCustomer));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void getById_shouldReturn404_whenNotExists() throws Exception {
        when(customerService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201_withValidBody() throws Exception {
        when(customerService.create(any(CustomerCreateDTO.class))).thenReturn(testCustomer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Acme Corp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Corp"));
    }

    @Test
    void update_shouldReturn200_withValidBody() throws Exception {
        when(customerService.update(eq(1L), any(CustomerCreateDTO.class))).thenReturn(testCustomer);

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"enabled\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void update_shouldReturn404_whenNotExists() throws Exception {
        when(customerService.update(eq(99L), any(CustomerCreateDTO.class)))
                .thenThrow(new NotFoundException("Cliente não encontrado"));

        mockMvc.perform(put("/api/customers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"enabled\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenNoCircuits() throws Exception {
        doNothing().when(customerService).delete(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_shouldReturn400_whenCustomerHasCircuits() throws Exception {
        doThrow(new BusinessException("Cliente possui circuito(s) vinculado(s). Use a desativação."))
                .when(customerService).delete(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_shouldReturn404_whenNotExists() throws Exception {
        doThrow(new NotFoundException("Cliente não encontrado"))
                .when(customerService).delete(99L);

        mockMvc.perform(delete("/api/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void disable_shouldReturn200_onSuccess() throws Exception {
        when(customerService.disable(1L)).thenReturn(testCustomer);

        mockMvc.perform(patch("/api/customers/1/disable"))
                .andExpect(status().isOk());
    }

    @Test
    void disable_shouldReturn404_whenNotExists() throws Exception {
        when(customerService.disable(99L))
                .thenThrow(new NotFoundException("Cliente não encontrado"));

        mockMvc.perform(patch("/api/customers/99/disable"))
                .andExpect(status().isNotFound());
    }
}
