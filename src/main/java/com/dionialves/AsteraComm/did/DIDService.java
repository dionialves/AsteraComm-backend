package com.dionialves.AsteraComm.did;

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

        if (!circuitRepository.existsById(circuitNumber)) {
            throw new NotFoundException("Circuito não encontrado");
        }

        did.setCircuitNumber(circuitNumber);
        return didRepository.save(did);
    }

    @Transactional
    public DID unlinkFromCircuit(Long id) {
        DID did = didRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("DID não encontrado"));

        if (did.getCircuitNumber() == null) {
            throw new BusinessException("DID não está vinculado a nenhum circuito");
        }

        did.setCircuitNumber(null);
        return didRepository.save(did);
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
