package com.dionialves.AsteraComm.domain.asterisk.endpoint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dionialves.AsteraComm.domain.asterisk.endpoint.dto.EndpointCreateDTO;

@RestController
@RequestMapping("/api/circuits")
public class EndpointController {

    private final EndpointService endpointService;

    public EndpointController(EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @GetMapping
    public Page<EndpointProjection> findAll(
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return endpointService.getAllEndpointData(search, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Endpoint> findById(@PathVariable String id) {
        return endpointService.findByid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EndpointCreateDTO dto) {
        try {
            Endpoint endpoint = endpointService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(endpoint);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody EndpointCreateDTO dto) {
        try {
            Endpoint endpoint = endpointService.update(id, dto);
            return ResponseEntity.ok(endpoint);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            endpointService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
