package com.diego.library.loan.mapper;

import com.diego.library.loan.dto.LoanResponse;
import com.diego.library.loan.entity.Loan;

public class LoanMapper {

    public static LoanResponse toResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setMemberId(loan.getMember().getId());
        response.setBookId(loan.getBook().getId());
        response.setLoanDate(loan.getLoanDate());
        response.setReturnDate(loan.getReturnDate());
        return response;
    }
}
