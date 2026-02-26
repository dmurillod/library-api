package com.diego.library.loan.controller;

import com.diego.library.book.entity.Book;
import com.diego.library.loan.dto.LoanRequest;
import com.diego.library.loan.entity.Loan;
import com.diego.library.loan.service.LoanService;
import com.diego.library.member.entity.Member;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanService loanService;

    private MockMvc mockMvc;

    private LoanRequest validRequest;
    private Loan loan;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        Member member = new Member();
        member.setId(1L);
        member.setFirstName("Diego");
        member.setLastName("Garcia");
        member.setEmail("diego@email.com");
        member.setActive(true);

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setIsbn("9780132350884");
        book.setAvailable(true);

        loan = new Loan();
        loan.setId(1L);
        loan.setMember(member);
        loan.setBook(book);
        loan.setLoanDate(LocalDateTime.now());
        loan.setReturnDate(null);

        validRequest = new LoanRequest();
        validRequest.setMemberId(1L);
        validRequest.setBookId(1L);
    }

    // ──────────────────────────────────────────
    // POST /api/loans
    // ──────────────────────────────────────────

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        when(loanService.createLoan(1L, 1L)).thenReturn(loan);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.bookId").value(1L))
                .andExpect(jsonPath("$.returnDate").doesNotExist());

        verify(loanService).createLoan(1L, 1L);
    }

    @Test
    void create_shouldReturn400_whenMemberIdIsNull() throws Exception {
        validRequest.setMemberId(null);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void create_shouldReturn400_whenBookIdIsNull() throws Exception {
        validRequest.setBookId(null);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void create_shouldReturn400_whenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(loanService);
    }

    @Test
    void create_shouldReturn404_whenMemberNotFound() throws Exception {
        when(loanService.createLoan(99L, 1L)).thenThrow(new RuntimeException("Member not found"));

        validRequest.setMemberId(99L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn404_whenBookNotFound() throws Exception {
        when(loanService.createLoan(1L, 99L)).thenThrow(new RuntimeException("Book not found"));

        validRequest.setBookId(99L);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // PUT /api/loans/{id}/return
    // ──────────────────────────────────────────

    @Test
    void returnBook_shouldReturn200_whenLoanExists() throws Exception {
        loan.setReturnDate(LocalDateTime.now());
        when(loanService.returnBook(1L)).thenReturn(loan);

        mockMvc.perform(put("/api/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());

        verify(loanService).returnBook(1L);
    }

    @Test
    void returnBook_shouldReturn404_whenLoanNotFound() throws Exception {
        when(loanService.returnBook(99L)).thenThrow(new RuntimeException("Loan not found"));

        mockMvc.perform(put("/api/loans/99/return"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnBook_shouldReturn404_whenBookAlreadyReturned() throws Exception {
        when(loanService.returnBook(1L)).thenThrow(new RuntimeException("Book already returned"));

        mockMvc.perform(put("/api/loans/1/return"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // GET /api/loans/{id}
    // ──────────────────────────────────────────

    @Test
    void findById_shouldReturn200_whenLoanExists() throws Exception {
        when(loanService.findById(1L)).thenReturn(loan);

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.memberId").value(1L))
                .andExpect(jsonPath("$.bookId").value(1L));

        verify(loanService).findById(1L);
    }

    @Test
    void findById_shouldReturn404_whenLoanNotFound() throws Exception {
        when(loanService.findById(99L)).thenThrow(new RuntimeException("Loan not found"));

        mockMvc.perform(get("/api/loans/99"))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────
    // GET /api/loans
    // ──────────────────────────────────────────

    @Test
    void findAll_shouldReturn200_withListOfLoans() throws Exception {
        when(loanService.findAll()).thenReturn(List.of(loan));

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].memberId").value(1L))
                .andExpect(jsonPath("$[0].bookId").value(1L));

        verify(loanService).findAll();
    }

    @Test
    void findAll_shouldReturn200_withEmptyList() throws Exception {
        when(loanService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
