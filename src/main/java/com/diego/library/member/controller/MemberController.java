package com.diego.library.member.controller;

import com.diego.library.member.dto.MemberRequest;
import com.diego.library.member.dto.MemberResponse;
import com.diego.library.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@Valid @RequestBody MemberRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public MemberResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    public List<MemberResponse> findAll() {
        return service.findAll();
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id,
                                 @Valid @RequestBody MemberRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
