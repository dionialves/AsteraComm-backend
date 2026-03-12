package com.dionialves.AsteraComm.cdr;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cdrs")
public class CdrController {

    private final CdrService cdrService;

    @GetMapping
    public Page<CdrRecord> findAll(
            @RequestParam(required = false) String src,
            @RequestParam(required = false) String dst,
            @RequestParam(required = false) String disposition,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "calldate", direction = Sort.Direction.DESC) Pageable pageable) {
        return cdrService.getAll(src, dst, disposition, from, to, pageable);
    }

    @GetMapping("/{uniqueid:.+}")
    public ResponseEntity<CdrRecord> findByUniqueId(@PathVariable String uniqueid) {
        return cdrService.findByUniqueId(uniqueid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
