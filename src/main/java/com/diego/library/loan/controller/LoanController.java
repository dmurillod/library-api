package com.diego.library.loan.controller;

import com.diego.library.loan.dto.LoanRequest;
import com.diego.library.loan.dto.LoanResponse;
import com.diego.library.loan.mapper.LoanMapper;
import com.diego.library.loan.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    @PostMapping
    public LoanResponse create(@Valid @RequestBody LoanRequest request) {
        return LoanMapper.toResponse(
                service.createLoan(request.getMemberId(), request.getBookId())
        );
    }

    @PutMapping("/{id}/return")
    public LoanResponse returnBook(@PathVariable Long id) {
        return LoanMapper.toResponse(
                service.returnBook(id)
        );
    }

    @GetMapping("/{id}")
    public LoanResponse findById(@PathVariable Long id) {
        return LoanMapper.toResponse(service.findById(id));
    }

    @GetMapping
    public List<LoanResponse> findAll() {
        return service.findAll()
                .stream()
                .map(LoanMapper::toResponse)
                .collect(Collectors.toList());
    }
}
