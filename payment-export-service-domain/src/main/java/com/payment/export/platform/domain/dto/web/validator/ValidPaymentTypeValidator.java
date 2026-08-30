package com.payment.export.platform.domain.dto.web.validator;

import com.payment.export.platform.domain.dto.PaymentType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPaymentTypeValidator implements ConstraintValidator<ValidPaymentType, PaymentType> {

    @Override
    public boolean isValid(PaymentType value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return value == PaymentType.CT || value == PaymentType.DD;
    }
}

