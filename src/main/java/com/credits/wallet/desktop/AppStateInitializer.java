package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.executor.service.ContractExecutorApiServiceImpl;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.wallet.desktop.utils.FormUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Properties;

public class AppStateInitializer {
    private static final String ERR_NO_PROPERTIES = "File settings.properties not found";

    private static final String ERR_NO_API_ADDR = "The server address could not be determined. Check api.addr parameter in the settings.properties file";

    private static final String ERR_NO_CONTRACT_EXECUTOR =
            "Parameters for java contract generated could not be determined. Check contract.generated.host, contract.generated.port, contract.generated.dir  parameters in the settings.properties file";

    public String startForm = VistaNavigator.WELCOME;

    Properties properties;

    public void init() {
        properties = loadProperties();

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        AppState.decimalSeparator = Character.toString(symbols.getDecimalSeparator());
        AppState.creditMonitorURL = properties.getProperty("creditmonitor.url");
        AppState.nodeApiService = initializeNodeApiService();
        AppState.contractExecutorService = initializeContractExecutorApiService();
    }

    Properties loadProperties() {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream("settings.properties");
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(ERR_NO_PROPERTIES, e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return properties;
    }

    NodeApiService initializeNodeApiService() {
        String apiAddr = properties.getProperty("api.host");
        String apiPort = properties.getProperty("api.port");

        if (apiAddr == null || apiAddr.isEmpty() || apiPort == null || apiPort.isEmpty()) {
            FormUtils.showError(ERR_NO_API_ADDR);
        }
        return NodeApiServiceImpl.getInstance(apiAddr, apiPort == null ? 9090 : Integer.parseInt(apiPort));
    }

    ContractExecutorApiService initializeContractExecutorApiService() {
        String executorHost = properties.getProperty("executor.host");
        String executorPort = properties.getProperty("executor.port");

        if (executorHost == null || executorHost.isEmpty() || executorPort == null || executorPort.isEmpty()) {
            FormUtils.showError(ERR_NO_API_ADDR);
        }
        return ContractExecutorApiServiceImpl.getInstance(executorHost, executorPort == null ? 9090 : Integer.parseInt(executorPort));
    }
}
