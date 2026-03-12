package com.dionialves.AsteraComm.cdr;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CdrService {

    private final CdrRepository cdrRepository;

    public Page<CdrRecord> getAll(String src, String dst, String disposition,
                                  LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return cdrRepository.findAll(CdrSpec.withFilters(src, dst, disposition, from, to), pageable);
    }

    public Optional<CdrRecord> findByUniqueId(String uniqueId) {
        return cdrRepository.findByUniqueId(uniqueId);
    }
}
