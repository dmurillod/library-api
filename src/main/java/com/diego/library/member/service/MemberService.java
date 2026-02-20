package com.diego.library.member.service;

import com.diego.library.member.dto.MemberRequest;
import com.diego.library.member.dto.MemberResponse;
import com.diego.library.member.entity.Member;
import com.diego.library.member.mapper.MemberMapper;
import com.diego.library.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MemberResponse create(MemberRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Member member = MemberMapper.toEntity(request);
        Member saved = repository.save(member);

        return MemberMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MemberResponse findById(Long id) {

        Member member = repository.findById(id)
                .filter(Member::isActive)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        return MemberMapper.toResponse(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return repository.findAll()
                .stream()
                .filter(Member::isActive)
                .map(MemberMapper::toResponse)
                .toList();
    }

    @Transactional
    public MemberResponse update(Long id, MemberRequest request) {

        Member member = repository.findById(id)
                .filter(Member::isActive)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());

        return MemberMapper.toResponse(repository.save(member));
    }

    @Transactional
    public void delete(Long id) {

        Member member = repository.findById(id)
                .filter(Member::isActive)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setActive(false);
        repository.save(member);
    }
}