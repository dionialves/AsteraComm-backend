package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.circuit.dto.CircuitCreateDTO;
import com.dionialves.AsteraComm.exception.BusinessException;
import com.dionialves.AsteraComm.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CircuitService {

    private final CircuitRepository circuitRepository;
    private final AsteriskProvisioningService asteriskProvisioningService;

    public Page<CircuitProjection> getAll(String search, Pageable pageable) {
        return circuitRepository.findAllCircuits(search, pageable);
    }

    public Optional<Circuit> findByNumber(String number) {
        return circuitRepository.findById(number);
    }

    @Transactional
    public Circuit create(CircuitCreateDTO dto) {
        if (circuitRepository.existsById(dto.number())) {
            throw new BusinessException("Circuito já existe com este número");
        }

        Circuit circuit = new Circuit();
        circuit.setNumber(dto.number());
        circuit.setPassword(dto.password());
        Circuit saved = circuitRepository.save(circuit);

        asteriskProvisioningService.provision(saved);

        return saved;
    }

    @Transactional
    public Circuit update(String number, CircuitCreateDTO dto) {
        Circuit circuit = circuitRepository.findById(number)
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado"));

        circuit.setPassword(dto.password());
        Circuit saved = circuitRepository.save(circuit);

        asteriskProvisioningService.reprovision(saved);

        return saved;
    }

    @Transactional
    public void delete(String number) {
        Circuit circuit = circuitRepository.findById(number)
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado"));

        asteriskProvisioningService.deprovision(circuit);
        circuitRepository.delete(circuit);
    }
}
