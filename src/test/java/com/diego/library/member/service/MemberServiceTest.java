package com.diego.library.member.service;

import com.diego.library.member.dto.MemberRequest;
import com.diego.library.member.dto.MemberResponse;
import com.diego.library.member.entity.Member;
import com.diego.library.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    private MemberRepository repository;
    private MemberService service;

    @BeforeEach
    void setUp() {
        repository = mock(MemberRepository.class);
        service = new MemberService(repository);
    }

    @Test
    void create_shouldSaveMember_whenEmailNotExists() {
        MemberRequest request = new MemberRequest();
        request.setFirstName("Diego");
        request.setLastName("Murillo");
        request.setEmail("diego@example.com");

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(repository.save(any(Member.class))).thenAnswer(invocation -> {
            Member m = invocation.getArgument(0);
            m.setId(1L); // simulamos ID generado por DB
            return m;
        });

        MemberResponse response = service.create(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Diego", response.getFirstName());
        assertEquals("Murillo", response.getLastName());
        assertEquals("diego@example.com", response.getEmail());

        verify(repository, times(1)).existsByEmail(request.getEmail());
        verify(repository, times(1)).save(any(Member.class));
    }

    @Test
    void create_shouldThrow_whenEmailExists() {
        MemberRequest request = new MemberRequest();
        request.setFirstName("Diego");
        request.setLastName("Murillo");
        request.setEmail("diego@example.com");

        when(repository.existsByEmail(request.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(request));
        assertEquals("Email already exists", ex.getMessage());

        verify(repository, never()).save(any(Member.class));
    }

    @Test
    void findById_shouldReturnMember_whenExists() {
        Member member = new Member();
        member.setFirstName("Diego");
        member.setLastName("Murillo");
        member.setEmail("diego@example.com");
        member.setActive(true);
        member.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(member));

        MemberResponse response = service.findById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.findById(1L));
        assertEquals("Member not found", ex.getMessage());
    }

    @Test
    void findAll_shouldReturnOnlyActiveMembers() {
        Member active = new Member();
        active.setFirstName("Active");
        active.setActive(true);
        active.setId(1L);

        Member inactive = new Member();
        inactive.setFirstName("Inactive");
        inactive.setActive(false);
        inactive.setId(2L);

        when(repository.findAll()).thenReturn(Arrays.asList(active, inactive));

        List<MemberResponse> responses = service.findAll();

        assertEquals(1, responses.size());
        assertEquals("Active", responses.get(0).getFirstName());
    }

    @Test
    void update_shouldModifyMember_whenExists() {
        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Old");
        member.setLastName("Name");
        member.setEmail("old@example.com");
        member.setActive(true);

        MemberRequest request = new MemberRequest();
        request.setFirstName("New");
        request.setLastName("Name");
        request.setEmail("new@example.com");

        when(repository.findById(1L)).thenReturn(Optional.of(member));
        when(repository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberResponse response = service.update(1L, request);

        assertEquals("New", response.getFirstName());
        assertEquals("new@example.com", response.getEmail());
    }

    @Test
    void update_shouldThrow_whenMemberNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        MemberRequest request = new MemberRequest();
        request.setFirstName("New");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.update(1L, request));
        assertEquals("Member not found", ex.getMessage());
    }

    @Test
    void delete_shouldSetMemberInactive_whenExists() {
        Member member = new Member();
        member.setId(1L);
        member.setActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(member));
        when(repository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(1L);

        assertFalse(member.isActive());
        verify(repository, times(1)).save(member);
    }

    @Test
    void delete_shouldThrow_whenMemberNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.delete(1L));
        assertEquals("Member not found", ex.getMessage());
    }
}