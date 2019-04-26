package service.executor;

import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.pojo.MethodDescriptionData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.compiler.CompilationException;
import exception.ContractExecutorException;
import pojo.ExternalSmartContract;
import pojo.ReturnValue;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;

import java.util.List;
import java.util.Map;

public interface ContractExecutorService {

    ReturnValue deploySmartContract(DeployContractSession session) throws ContractExecutorException;

    ReturnValue executeSmartContract(InvokeMethodSession session) throws ContractExecutorException;

    List<MethodDescriptionData> getContractsMethods(List<ByteCodeObjectData> byteCodeObjectDataList) throws ContractExecutorException;

    Map<String, Variant> getContractVariables(List<ByteCodeObjectData> contractBytecode, byte[] contractState) throws ContractExecutorException;

    List<ByteCodeObjectData> compileClass(String sourceCode) throws ContractExecutorException, CompilationException;

    ReturnValue executeExternalSmartContract(InvokeMethodSession session, Map<String, ExternalSmartContract> usedContracts, ByteCodeContractClassLoader classLoader);

    default ByteCodeContractClassLoader getSmartContractClassLoader() {
        return getClass().getClassLoader() instanceof ByteCodeContractClassLoader
            ? (ByteCodeContractClassLoader) getClass().getClassLoader()
            : new ByteCodeContractClassLoader();
    }
}
