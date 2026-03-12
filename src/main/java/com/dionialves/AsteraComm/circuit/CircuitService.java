package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.circuit.dto.CircuitCreateDTO;
import com.dionialves.AsteraComm.customer.Customer;
import com.dionialves.AsteraComm.customer.CustomerRepository;
import com.dionialves.AsteraComm.did.DIDRepository;
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
    private final DIDRepository didRepository;
    private final CustomerRepository customerRepository;
    private final AsteriskProvisioningService asteriskProvisioningService;

    public Page<CircuitProjection> getAll(String search, Pageable pageable) {
        return circuitRepository.findAllCircuits(search, pageable);
    }

    public Optional<Circuit> findByNumber(String number) {
        return circuitRepository.findById(number);
    }

    @Transactional
    public Circuit create(CircuitCreateDTO dto) {
        String code = circuitRepository.findMaxCode()
                .map(max -> String.valueOf(Long.parseLong(max) + 1))
                .orElse("100000");

        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        Circuit circuit = new Circuit();
        circuit.setNumber(code);
        circuit.setPassword(dto.password());
        circuit.setTrunkName(dto.trunkName());
        circuit.setCustomer(customer);
        Circuit saved = circuitRepository.save(circuit);

        asteriskProvisioningService.provision(saved);

        return saved;
    }

    @Transactional
    public Circuit update(String number, CircuitCreateDTO dto) {
        Circuit circuit = circuitRepository.findById(number)
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado"));

        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        String previousTrunkName = circuit.getTrunkName();

        circuit.setPassword(dto.password());
        circuit.setTrunkName(dto.trunkName());
        circuit.setCustomer(customer);
        Circuit saved = circuitRepository.save(circuit);

        asteriskProvisioningService.reprovision(saved, previousTrunkName);

        return saved;
    }

    @Transactional
    public void delete(String number) {
        Circuit circuit = circuitRepository.findById(number)
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado"));

        if (didRepository.existsByCircuitNumber(number)) {
            throw new BusinessException("Circuito possui DID(s) vinculado(s). Desvincule os DIDs antes de excluir.");
        }

        asteriskProvisioningService.deprovision(circuit);
        circuitRepository.delete(circuit);
    }
}
