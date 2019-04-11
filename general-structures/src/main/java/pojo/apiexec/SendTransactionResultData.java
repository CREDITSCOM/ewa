package pojo.apiexec;

import com.credits.general.pojo.ApiResponseData;
import com.credits.general.pojo.ByteCodeObjectData;

import java.util.Arrays;
import java.util.List;

public class SendTransactionResultData extends ApiResponseData {
    private static final long serialVersionUID = 4691482244448740353L;

    private final List<ByteCodeObjectData> byteCodeObjects;
    private final byte[] contractState;

    public SendTransactionResultData(ApiResponseData apiResponseData, List<ByteCodeObjectData> byteCodeObjects, byte[] contractState) {
        super(apiResponseData);
        this.byteCodeObjects = byteCodeObjects;
        this.contractState = contractState;
    }

    public List<ByteCodeObjectData> getByteCodeObjects() {
        return byteCodeObjects;
    }

    public byte[] getContractState() {
        return contractState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SendTransactionResultData)) return false;
        if (!super.equals(o)) return false;

        SendTransactionResultData that = (SendTransactionResultData) o;

        if (getByteCodeObjects() != null ? !getByteCodeObjects().equals(that.getByteCodeObjects()) : that.getByteCodeObjects() != null)
            return false;
        return Arrays.equals(getContractState(), that.getContractState());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getByteCodeObjects() != null ? getByteCodeObjects().hashCode() : 0);
        result = 31 * result + Arrays.hashCode(getContractState());
        return result;
    }

    @Override
    public String toString() {
        return "GetSmartCodeResultData{" +
                "byteCodeObjects=" + byteCodeObjects +
                ", contractState=" + Arrays.toString(contractState) +
                '}';
    }
}
