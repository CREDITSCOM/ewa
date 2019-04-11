package exception;

public class ExternalSmartContractException extends ContractExecutorException {
    public ExternalSmartContractException(String message, Throwable e) {
        super(message, e);
    }

    public ExternalSmartContractException(String message) {
        super(message);
    }
}
