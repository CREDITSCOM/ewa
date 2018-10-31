package com.credits.wallet.desktop.utils.sourcecode;

import com.credits.general.exception.CreditsException;
import com.credits.general.util.Utils;


public class Validator {

    public static void validateInteger(String value) throws ValidationException {
        if (Utils.isEmpty(value)) {
            throw new ValidationException("Value is empty");
        }
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Invalid Integer value: %s", value));
        }

    }

    public static void validateLong(String value) throws ValidationException {
        if (Utils.isEmpty(value)) {
            throw new ValidationException("Value is empty");
        }
        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Invalid Long value: %s", value));
        }
    }

    public static void validateDouble(String value) throws ValidationException {
        if (Utils.isEmpty(value)) {
            throw new ValidationException("Value is empty");
        }
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ValidationException(String.format("Invalid Double value: %s", value));
        }
    }

    public static class ValidationException extends CreditsException{

        public ValidationException(String errorMessage) {
            super(errorMessage);
        }

        public ValidationException(Exception e) {
            super(e);
        }
    }
}
