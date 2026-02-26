package com.diego.library.member.controller;

import com.diego.library.member.dto.MemberRequest;
import com.diego.library.member.dto.MemberResponse;
import com.diego.library.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    private MockMvc mockMvc;

    private MemberRequest validRequest;
    private MemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        validRequest = new MemberRequest();
        validRequest.setFirstName("Diego");
        validRequest.setLastName("Garcia");
        validRequest.setEmail("diego@email.com");

        memberResponse = new MemberResponse();
        memberResponse.setId(1L);
        memberResponse.setFirstName("Diego");
        memberResponse.setLastName("Garcia");
        memberResponse.setEmail("diego@email.com");
        memberResponse.setActive(true);
        memberResponse.setCreatedAt(LocalDateTime.now());
    }

    // ──────────────────────────────────────────
    // POST /api/members
    // ──────────────────────────────────────────

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        when(memberService.create(any(MemberRequest.class))).thenReturn(memberResponse);

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Diego"))
                .andExpect(jsonPath("$.lastName").value("Garcia"))
                .andExpect(jsonPath("$.email").value("diego@email.com"))
                .andExpect(jsonPath("$.active").value(true));

        verify(memberService).create(any(MemberRequest.class));
    }

    @Test
    void create_shouldReturn400_whenFirstNameIsBlank() throws Exception {
        validRequest.setFirstName("");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    @Test
    void create_shouldReturn400_whenLastNameIsBlank() throws Exception {
        validRequest.setLastName("");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        validRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    @Test
    void create_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    // ──────────────────────────────────────────
    // GET /api/members/{id}
    // ──────────────────────────────────────────

    @Test
    void findById_shouldReturn200_whenMemberExists() throws Exception {
        when(memberService.findById(1L)).thenReturn(memberResponse);

        mockMvc.perform(get("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Diego"))
                .andExpect(jsonPath("$.email").value("diego@email.com"));

        verify(memberService).findById(1L);
    }

    @Test
    void findById_shouldReturn404_whenMemberNotFound() throws Exception {
        when(memberService.findById(99L)).thenThrow(new RuntimeException("Member not found"));

        mockMvc.perform(get("/api/members/99"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // GET /api/members
    // ──────────────────────────────────────────

    @Test
    void findAll_shouldReturn200_withListOfMembers() throws Exception {
        when(memberService.findAll()).thenReturn(List.of(memberResponse));

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("Diego"));

        verify(memberService).findAll();
    }

    @Test
    void findAll_shouldReturn200_withEmptyList() throws Exception {
        when(memberService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ──────────────────────────────────────────
    // PUT /api/members/{id}
    // ──────────────────────────────────────────

    @Test
    void update_shouldReturn200_whenRequestIsValid() throws Exception {
        when(memberService.update(eq(1L), any(MemberRequest.class))).thenReturn(memberResponse);

        mockMvc.perform(put("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("Diego"));

        verify(memberService).update(eq(1L), any(MemberRequest.class));
    }

    @Test
    void update_shouldReturn400_whenEmailIsInvalid() throws Exception {
        validRequest.setEmail("bad-email");

        mockMvc.perform(put("/api/members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(memberService);
    }

    @Test
    void update_shouldReturn404_whenMemberNotFound() throws Exception {
        when(memberService.update(eq(99L), any(MemberRequest.class)))
                .thenThrow(new RuntimeException("Member not found"));

        mockMvc.perform(put("/api/members/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // DELETE /api/members/{id}
    // ──────────────────────────────────────────

    @Test
    void delete_shouldReturn200_whenMemberExists() throws Exception {
        doNothing().when(memberService).delete(1L);

        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isNoContent());

        verify(memberService).delete(1L);
    }

    @Test
    void delete_shouldReturn204_whenMemberExists() throws Exception {
        doNothing().when(memberService).delete(1L);
        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isNoContent());

        verify(memberService).delete(1L);
    }
}
