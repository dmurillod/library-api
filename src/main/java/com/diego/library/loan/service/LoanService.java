package com.diego.library.loan.service;

import com.diego.library.book.entity.Book;
import com.diego.library.book.repository.BookRepository;
import com.diego.library.loan.entity.Loan;
import com.diego.library.loan.repository.LoanRepository;
import com.diego.library.member.entity.Member;
import com.diego.library.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository,
                       MemberRepository memberRepository,
                       BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
    }

    public Loan createLoan(Long memberId, Long bookId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!member.isActive()) {
            throw new RuntimeException("Member is inactive");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available");
        }

        book.setAvailable(false);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setMember(member);
        loan.setBook(book);

        return loanRepository.save(loan);
    }

    public Loan returnBook(Long loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.getReturnDate() != null) {
            throw new RuntimeException("Book already returned");
        }

        loan.setReturnDate(LocalDateTime.now());

        Book book = loan.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        return loanRepository.save(loan);
    }

    public Loan findById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    public List<Loan> findAll() {
        return loanRepository.findAll();
    }
}
