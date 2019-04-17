package exception;

public class ExternalSmartContractException extends ContractExecutorException {
    private static final long serialVersionUID = -4766463108632071702L;

    public ExternalSmartContractException(String message, Throwable e) {
        super(message, e);
    }

    public ExternalSmartContractException(String message) {
        super(message);
    }
}
