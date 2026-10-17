package com.solaria.persistence.exception;

import org.springframework.http.HttpStatus;

public class ProposalConflictException extends BusinessException {
    public ProposalConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "PROPOSAL_CONFLICT");
    }
}
