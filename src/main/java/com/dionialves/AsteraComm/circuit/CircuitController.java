package com.dionialves.AsteraComm.circuit;

import com.dionialves.AsteraComm.circuit.dto.CircuitCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/circuits")
public class CircuitController {

    private final CircuitService circuitService;

    @GetMapping
    public Page<CircuitProjection> findAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(size = 10, sort = "number") Pageable pageable) {
        return circuitService.getAll(search, pageable);
    }

    @GetMapping("/{number}")
    public ResponseEntity<Circuit> findByNumber(@PathVariable String number) {
        return circuitService.findByNumber(number)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Circuit> create(@RequestBody CircuitCreateDTO dto) {
        Circuit circuit = circuitService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(circuit);
    }

    @PutMapping("/{number}")
    public ResponseEntity<Circuit> update(@PathVariable String number, @RequestBody CircuitCreateDTO dto) {
        Circuit circuit = circuitService.update(number, dto);
        return ResponseEntity.ok(circuit);
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<?> delete(@PathVariable String number) {
        return circuitService.delete(number)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
