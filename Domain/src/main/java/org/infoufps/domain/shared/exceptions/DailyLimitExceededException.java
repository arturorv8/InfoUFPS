package org.infoufps.domain.shared.exceptions;

public class DailyLimitExceededException extends DomainException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}

