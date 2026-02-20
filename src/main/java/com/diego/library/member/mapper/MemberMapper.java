package com.diego.library.member.mapper;

import com.diego.library.member.dto.MemberRequest;
import com.diego.library.member.dto.MemberResponse;
import com.diego.library.member.entity.Member;

public class MemberMapper {

    public static Member toEntity(MemberRequest request) {
        Member member = new Member();
        member.setFirstName(request.getFirstName());
        member.setLastName(request.getLastName());
        member.setEmail(request.getEmail());
        return member;
    }

    public static MemberResponse toResponse(Member member) {
        MemberResponse response = new MemberResponse();
        response.setId(member.getId());
        response.setFirstName(member.getFirstName());
        response.setLastName(member.getLastName());
        response.setEmail(member.getEmail());
        response.setActive(member.isActive());
        response.setCreatedAt(member.getCreatedAt());
        return response;
    }
}
