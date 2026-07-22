package isj01.limits.guard.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import isj01.limits.guard.dto.TransactionCreateDto;
import isj01.limits.guard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionRestController {

    private final TransactionService service;

    @PostMapping
    public ResponseEntity<Long> create(@RequestBody @Valid TransactionCreateDto createDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(createDto));
    }

}
