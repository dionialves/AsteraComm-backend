package com.dionialves.AsteraComm.cdr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CdrServiceTest {

    @Mock
    private CdrRepository cdrRepository;

    @InjectMocks
    private CdrService cdrService;

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
    }

    @Test
    void getAll_shouldDelegateToRepository() {
        Page<CdrRecord> page = new PageImpl<>(List.of());
        when(cdrRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<CdrRecord> result = cdrService.getAll(null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        verify(cdrRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAll_withAllFilters_shouldPassSpecificationToRepository() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 1, 31, 23, 59);
        Page<CdrRecord> page = new PageImpl<>(List.of(testCdr));
        when(cdrRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<CdrRecord> result = cdrService.getAll("1001", "1002", "ANSWERED", from, to, PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cdrRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAll_withPartialFilters_shouldStillDelegateToRepository() {
        Page<CdrRecord> page = new PageImpl<>(List.of());
        when(cdrRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<CdrRecord> result = cdrService.getAll("1001", null, null, null, null, PageRequest.of(0, 20));

        assertThat(result).isNotNull();
        verify(cdrRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void findByUniqueId_shouldReturnRecord_whenExists() {
        when(cdrRepository.findByUniqueId("1700000000.1")).thenReturn(Optional.of(testCdr));

        Optional<CdrRecord> result = cdrService.findByUniqueId("1700000000.1");

        assertThat(result).isPresent();
        assertThat(result.get().getUniqueId()).isEqualTo("1700000000.1");
        assertThat(result.get().getSrc()).isEqualTo("1001");
        assertThat(result.get().getDisposition()).isEqualTo("ANSWERED");
    }

    @Test
    void findByUniqueId_shouldReturnEmpty_whenNotExists() {
        when(cdrRepository.findByUniqueId("inexistente")).thenReturn(Optional.empty());

        Optional<CdrRecord> result = cdrService.findByUniqueId("inexistente");

        assertThat(result).isEmpty();
    }
}
