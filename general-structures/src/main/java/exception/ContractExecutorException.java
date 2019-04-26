package exception;

import com.credits.general.exception.CreditsException;

public class ContractExecutorException extends CreditsException {
    private static final long serialVersionUID = 2490513804467148620L;

    public ContractExecutorException(String message, Throwable e) {
        super(message, e);
    }

    public ContractExecutorException(String message) {
        super(message);
    }
}
