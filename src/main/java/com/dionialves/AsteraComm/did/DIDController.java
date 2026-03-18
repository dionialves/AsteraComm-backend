package com.dionialves.AsteraComm.did;

import com.dionialves.AsteraComm.did.dto.DIDCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dids")
public class DIDController {

    private final DIDService didService;

    @GetMapping
    public Page<DID> findAll(
            @RequestParam(defaultValue = "") String search,
            @PageableDefault(size = 10) Pageable pageable) {
        return didService.getAll(search, pageable);
    }

    @GetMapping("/free")
    public List<DID> findFree() {
        return didService.getFree();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DID> findById(@PathVariable Long id) {
        return didService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DID> create(@RequestBody DIDCreateDTO dto) {
        DID did = didService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(did);
    }

    @PutMapping("/{id}/link/{circuitNumber}")
    public ResponseEntity<DID> linkToCircuit(@PathVariable Long id, @PathVariable String circuitNumber) {
        return ResponseEntity.ok(didService.linkToCircuit(id, circuitNumber));
    }

    @PutMapping("/{id}/unlink")
    public ResponseEntity<DID> unlinkFromCircuit(@PathVariable Long id) {
        return ResponseEntity.ok(didService.unlinkFromCircuit(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        didService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
