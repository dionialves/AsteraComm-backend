package com.dionialves.AsteraComm.call;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CallService {

    private final CallRepository callRepository;

    public Page<Call> getAll(String callerNumber, String dst, String disposition,
                             LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return callRepository.findAll(
                CallSpec.withFilters(callerNumber, dst, disposition, from, to),
                pageable
        );
    }

    public Optional<Call> findByUniqueId(String uniqueId) {
        return callRepository.findByUniqueId(uniqueId);
    }
}
