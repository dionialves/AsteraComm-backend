package com.dionialves.AsteraComm.did;

import com.dionialves.AsteraComm.asterisk.provisioning.AsteriskProvisioningService;
import com.dionialves.AsteraComm.circuit.Circuit;
import com.dionialves.AsteraComm.circuit.CircuitRepository;
import com.dionialves.AsteraComm.did.dto.DIDCreateDTO;
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
public class DIDService {

    private final DIDRepository didRepository;
    private final CircuitRepository circuitRepository;
    private final AsteriskProvisioningService asteriskProvisioningService;

    public Page<DID> getAll(Pageable pageable) {
        return didRepository.findAll(pageable);
    }

    public Optional<DID> findById(Long id) {
        return didRepository.findById(id);
    }

    @Transactional
    public DID create(DIDCreateDTO dto) {
        String number = dto.number();

        if (number == null || !number.matches("\\d+")) {
            throw new BusinessException("Número inválido: apenas dígitos são permitidos");
        }

        if (number.length() != 10) {
            throw new BusinessException("Número inválido: deve ter exatamente 10 dígitos");
        }

        if (didRepository.existsByNumber(number)) {
            throw new BusinessException("DID já cadastrado com este número");
        }

        DID did = new DID();
        did.setNumber(number);
        return didRepository.save(did);
    }

    @Transactional
    public DID linkToCircuit(Long id, String circuitNumber) {
        DID did = didRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("DID não encontrado"));

        if (did.getCircuitNumber() != null) {
            throw new BusinessException("DID já está vinculado a um circuito");
        }

        Circuit circuit = circuitRepository.findById(circuitNumber)
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado"));

        did.setCircuitNumber(circuitNumber);
        DID saved = didRepository.save(did);
        asteriskProvisioningService.provisionDid(saved, circuit);
        return saved;
    }

    @Transactional
    public DID unlinkFromCircuit(Long id) {
        DID did = didRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("DID não encontrado"));

        if (did.getCircuitNumber() == null) {
            throw new BusinessException("DID não está vinculado a nenhum circuito");
        }

        Circuit circuit = circuitRepository.findById(did.getCircuitNumber())
                .orElseThrow(() -> new NotFoundException("Circuito não encontrado"));

        did.setCircuitNumber(null);
        DID saved = didRepository.save(did);
        asteriskProvisioningService.deprovisionDid(did.getNumber(), circuit.getTrunkName());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        DID did = didRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("DID não encontrado"));

        if (did.getCircuitNumber() != null) {
            throw new BusinessException("DID não pode ser removido pois está vinculado a um circuito");
        }

        didRepository.delete(did);
    }
}
