package com.credits.thrift;

import com.credits.client.executor.thrift.generated.ExecuteByteCodeResult;
import com.credits.client.executor.thrift.generated.MethodHeader;
import com.credits.client.executor.thrift.generated.SetterMethodResult;
import com.credits.client.executor.thrift.generated.SmartContractBinary;
import com.credits.general.thrift.generated.Variant;
import com.credits.utils.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pojo.ExternalSmartContract;
import pojo.session.DeployContractSession;
import pojo.session.InvokeMethodSession;
import service.executor.ContractExecutorService;

import java.nio.ByteBuffer;
import java.util.*;

import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.general.util.GeneralConverter.*;
import static com.credits.utils.ContractExecutorServiceUtils.SUCCESS_API_RESPONSE;
import static com.credits.utils.ContractExecutorServiceUtils.failureApiResponse;
import static java.util.Objects.requireNonNull;

public class ExecuteByteCodeSession {
    private final static Logger logger = LoggerFactory.getLogger(ExecuteByteCodeResult.class);

    private final long accessId;
    private final ByteBuffer initiatorAddress;
    private final String initiatorAddressBase58;
    private final String contractAddressBase58;
    private final SmartContractBinary invokedContract;
    private final List<MethodHeader> methodHeaders;
    private final long executionTime;
    private final boolean isDeployContractSession;
    private final ContractExecutorService ceService;
    private final Map<String, ExternalSmartContract> usedContracts = new HashMap<>();

    public ExecuteByteCodeSession(ContractExecutorService ceService, long accessId,
                                  ByteBuffer initiatorAddress,
                                  SmartContractBinary invokedContract,
                                  List<MethodHeader> methodHeaders, long executionTime) {
        this.ceService = ceService;
        this.accessId = accessId;
        this.initiatorAddress = initiatorAddress;
        this.invokedContract = invokedContract;
        this.methodHeaders = methodHeaders;
        this.executionTime = executionTime;
        var classObject = invokedContract.getObject();
        isDeployContractSession = classObject == null || classObject.instance == null || classObject.instance.array().length== 0;
        if (!isDeployContractSession && (methodHeaders == null || methodHeaders.size() == 0)) {
            throw new IllegalArgumentException("method headers list can't be null or empty");
        }
        initiatorAddressBase58 = encodeToBASE58(initiatorAddress.array());
        contractAddressBase58 = encodeToBASE58(invokedContract.getContractAddress());
    }

    public ExecuteByteCodeResult perform() {
        final var executionResult = isDeployContractSession
                ? executeDeploy(createDeploySession())
                : executeMethodsSequential();

        return new ExecuteByteCodeResult(SUCCESS_API_RESPONSE, executionResult, wrapMapArgsToByteBuffer(usedContracts));
    }

    @Override
    public String toString() {
        return "ExecuteByteCodeSession{" +
                "accessId=" + accessId +
                ", initiatorAddressBase58='" + initiatorAddressBase58 + '\'' +
                ", contractAddressBase58='" + contractAddressBase58 + '\'' +
                ", invokedContract=" + invokedContract +
                ", methodHeaders=" + methodHeaders +
                ", executionTime=" + executionTime +
                ", isDeployContractSession=" + isDeployContractSession +
                ", usedContracts=" + usedContracts +
                '}';
    }

    private List<SetterMethodResult> executeDeploy(DeployContractSession deploySession) {
        StopWatch stopWatch = new StopWatch().start();
        final var result = ceService.deploySmartContract(deploySession);
        final var spentTime = stopWatch.stop();
        return List.of(new SetterMethodResult(SUCCESS_API_RESPONSE,
                                              ByteBuffer.wrap(result.newContractState),
                                              result.executeResults.get(0).result,
                                              spentTime));
    }

    private List<SetterMethodResult> executeMethodsSequential() {
        return methodHeaders.stream()
                .reduce(new ArrayList<>(methodHeaders.size()),
                        (results, methodHeader) -> {
                            final var stopWatch = new StopWatch();
                            try {
                                requireNonNull(methodHeader, "method header can't be null");

                                stopWatch.start();
                                final var executionResult = ceService.executeSmartContract(createInvokeMethodSession(methodHeader));
                                final var spentTime = stopWatch.stop();

                                results.add(new SetterMethodResult(SUCCESS_API_RESPONSE,
                                                                   ByteBuffer.wrap(executionResult.newContractState),
                                                                   executionResult.executeResults.get(0).result,
                                                                   spentTime));

                            } catch (Throwable e) {
                                results.add(new SetterMethodResult(failureApiResponse(e),
                                                                   ByteBuffer.allocate(0),
                                                                   new Variant(V_STRING, e.getMessage()),
                                                                   stopWatch.stop()));

                            }
                            return results;
                        },
                        (r1, r2) -> r1);
    }

    private DeployContractSession createDeploySession() {
        return new DeployContractSession(
                accessId,
                initiatorAddressBase58,
                contractAddressBase58,
                byteCodeObjectsToByteCodeObjectsData(invokedContract.object.getByteCodeObjects()),
                executionTime);
    }

    private InvokeMethodSession createInvokeMethodSession(MethodHeader methodHeader) {
        var session = new InvokeMethodSession(
                accessId,
                initiatorAddressBase58,
                contractAddressBase58,
                byteCodeObjectsToByteCodeObjectsData(invokedContract.getObject().getByteCodeObjects()),
                invokedContract.getObject().getInstance(),
                methodHeader.getMethodName(),
                toVariantArray(methodHeader.getParams()),
                executionTime);
        session.usedContracts.putAll(usedContracts);
        return session;
    }

    private Map<ByteBuffer, ByteBuffer> wrapMapArgsToByteBuffer(Map<String, ExternalSmartContract> externalContracts) {
        return externalContracts.keySet().stream().reduce(
                new HashMap<>(),
                (newMap, address) -> {
                    var externalContractAddress = decodeFromBASE58(address);
                    if (!Arrays.equals(externalContractAddress, initiatorAddress.array())) {
                        newMap.put(
                                ByteBuffer.wrap(externalContractAddress),
                                ByteBuffer.wrap(externalContracts.get(address).getContractData().getContractState()));
                    }
                    return newMap;
                },
                (map1, map2) -> map1);
    }

    private Variant[][] toVariantArray(List<Variant> variantList) {
        var params = new Variant[1][];
        params[0] = variantList.toArray(Variant[]::new);
        return params;
    }
}
