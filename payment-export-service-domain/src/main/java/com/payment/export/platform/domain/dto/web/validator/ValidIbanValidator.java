package com.payment.export.platform.domain.dto.web.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.iban4j.IbanFormatException;
import org.iban4j.IbanUtil;
import org.iban4j.InvalidCheckDigitException;
import org.iban4j.UnsupportedCountryException;

public class ValidIbanValidator implements ConstraintValidator<ValidIban, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return false;
        }

        try {
            IbanUtil.validate(value.trim());
            return true;
        } catch (IbanFormatException | InvalidCheckDigitException | UnsupportedCountryException exception) {
            return false;
        }
    }
}

