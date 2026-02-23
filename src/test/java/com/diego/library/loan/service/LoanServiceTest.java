package com.diego.library.loan.service;

import com.diego.library.book.entity.Book;
import com.diego.library.book.repository.BookRepository;
import com.diego.library.loan.entity.Loan;
import com.diego.library.loan.repository.LoanRepository;
import com.diego.library.member.entity.Member;
import com.diego.library.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private LoanService loanService;

    private Member activeMember;
    private Member inactiveMember;
    private Book availableBook;
    private Book unavailableBook;
    private Loan loan;

    @BeforeEach
    void setUp() {
        activeMember = new Member();
        activeMember.setId(1L);
        activeMember.setActive(true);

        inactiveMember = new Member();
        inactiveMember.setId(2L);
        inactiveMember.setActive(false);

        availableBook = new Book();
        availableBook.setId(1L);
        availableBook.setAvailable(true);

        unavailableBook = new Book();
        unavailableBook.setId(2L);
        unavailableBook.setAvailable(false);

        loan = new Loan();
        loan.setId(1L);
        loan.setMember(activeMember);
        loan.setBook(availableBook);
        loan.setReturnDate(null);
    }

    // ──────────────────────────────────────────
    // createLoan
    // ──────────────────────────────────────────

    @Test
    void createLoan_shouldReturnLoan_whenMemberAndBookAreValid() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.createLoan(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getMember()).isEqualTo(activeMember);
        assertThat(result.getBook()).isEqualTo(availableBook);
        assertThat(availableBook.isAvailable()).isFalse();

        verify(bookRepository).save(availableBook);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void createLoan_shouldThrow_whenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.createLoan(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Member not found");

        verifyNoInteractions(bookRepository, loanRepository);
    }

    @Test
    void createLoan_shouldThrow_whenMemberIsInactive() {
        when(memberRepository.findById(2L)).thenReturn(Optional.of(inactiveMember));

        assertThatThrownBy(() -> loanService.createLoan(2L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Member is inactive");

        verifyNoInteractions(bookRepository, loanRepository);
    }

    @Test
    void createLoan_shouldThrow_whenBookNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.createLoan(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book not found");

        verifyNoInteractions(loanRepository);
    }

    @Test
    void createLoan_shouldThrow_whenBookIsNotAvailable() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(activeMember));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(unavailableBook));

        assertThatThrownBy(() -> loanService.createLoan(1L, 2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book is not available");

        verifyNoInteractions(loanRepository);
    }

    // ──────────────────────────────────────────
    // returnBook
    // ──────────────────────────────────────────

    @Test
    void returnBook_shouldSetReturnDateAndMarkBookAvailable() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan result = loanService.returnBook(1L);

        assertThat(result.getReturnDate()).isNotNull();
        assertThat(result.getReturnDate()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(loan.getBook().isAvailable()).isTrue();

        verify(bookRepository).save(availableBook);
        verify(loanRepository).save(loan);
    }

    @Test
    void returnBook_shouldThrow_whenLoanNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.returnBook(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Loan not found");
    }

    @Test
    void returnBook_shouldThrow_whenBookAlreadyReturned() {
        loan.setReturnDate(LocalDateTime.now().minusDays(1));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnBook(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Book already returned");

        verifyNoMoreInteractions(bookRepository, loanRepository);
    }

    // ──────────────────────────────────────────
    // findById
    // ──────────────────────────────────────────

    @Test
    void findById_shouldReturnLoan_whenExists() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Loan result = loanService.findById(1L);

        assertThat(result).isEqualTo(loan);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Loan not found");
    }

    // ──────────────────────────────────────────
    // findAll
    // ──────────────────────────────────────────

    @Test
    void findAll_shouldReturnAllLoans() {
        when(loanRepository.findAll()).thenReturn(List.of(loan));

        List<Loan> result = loanService.findAll();

        assertThat(result).hasSize(1).contains(loan);
        verify(loanRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoLoansExist() {
        when(loanRepository.findAll()).thenReturn(List.of());

        List<Loan> result = loanService.findAll();

        assertThat(result).isEmpty();
    }
}
