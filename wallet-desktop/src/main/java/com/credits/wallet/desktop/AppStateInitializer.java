package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.executor.service.ContractExecutorApiServiceImpl;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.wallet.desktop.service.ContractInteractionService;
import com.credits.wallet.desktop.utils.FormUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class AppStateInitializer {
    private static final String ERR_NO_PROPERTIES = "File settings.properties not found";

    private static final String ERR_NO_NODE_API_ADDRESS = "Node API address not found. Please add node.api.host and node.api.port to property file ";

    private static final String ERR_NO_CONTRACT_EXECUTOR_API_ADDRESS = "Contract Executor address not found. Please add contract.executor.host and contract.executor.port to property file ";

    public final int DEFAULT_NODE_API_PORT = 9090;

    public final int DEFAULT_CONTRACT_EXECUTOR_API_PORT = 9080;

    public String startForm = VistaNavigator.WELCOME;

    Properties properties;

    public void init() {
        properties = loadProperties();

        AppState.creditMonitorURL = properties.getProperty("creditmonitor.url");
        AppState.nodeApiService = initializeNodeApiService();
        AppState.contractExecutorService = initializeContractExecutorApiService();
        AppState.contractInteractionService = initializeContractInteractionService();
    }

    public Properties loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("settings.properties")){
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(ERR_NO_PROPERTIES, e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return properties;
    }

    public ContractInteractionService initializeContractInteractionService() {
        return new ContractInteractionService();
    }

    public NodeApiService initializeNodeApiService() {
        String apiAddress = properties.getProperty("node.api.host");
        String apiPort = properties.getProperty("node.api.port");

        if (apiAddress == null || apiAddress.isEmpty() || apiPort == null || apiPort.isEmpty()) {
            FormUtils.showError(ERR_NO_NODE_API_ADDRESS);
        }
        return NodeApiServiceImpl.getInstance(apiAddress, apiPort == null ? DEFAULT_NODE_API_PORT : Integer.parseInt(apiPort));
    }

    public ContractExecutorApiService initializeContractExecutorApiService() {
        String executorHost = properties.getProperty("contract.executor.host");
        String executorPort = properties.getProperty("contract.executor.port");

        if (executorHost == null || executorHost.isEmpty() || executorPort == null || executorPort.isEmpty()) {
            FormUtils.showError(ERR_NO_CONTRACT_EXECUTOR_API_ADDRESS);
        }
        return ContractExecutorApiServiceImpl.getInstance(executorHost,
            executorPort == null ? DEFAULT_CONTRACT_EXECUTOR_API_PORT : Integer.parseInt(executorPort));
    }
}
