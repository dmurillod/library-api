package com.diego.library.loan.dto;

import jakarta.validation.constraints.NotNull;

public class LoanRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private Long bookId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
