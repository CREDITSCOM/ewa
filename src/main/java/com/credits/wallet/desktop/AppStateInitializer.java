package com.credits.wallet.desktop;

import com.credits.leveldb.client.ApiClient;
import com.credits.wallet.desktop.utils.FormUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Properties;

class AppStateInitializer {
    private static final String ERR_NO_PROPERTIES = "File settings.properties not found";

    private static final String ERR_NO_API_ADDR = "The server address could not be determined. Check api.addr parameter in the settings.properties file";

    private static final String ERR_NO_CONTRACT_EXECUTOR =
            "Parameters for java contract executor could not be determined. Check contract.executor.host, contract.executor.port, contract.executor.dir  parameters in the settings.properties file";

    void init() {
        Properties properties = new Properties();
        try {
            FileInputStream fis = new FileInputStream("settings.properties");
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(ERR_NO_PROPERTIES, e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        char separator = symbols.getDecimalSeparator();
        AppState.decimalSeparator = Character.toString(separator);

        AppState.creditMonitorURL = properties.getProperty("creditmonitor.url");

        String apiAddr = properties.getProperty("api.addr");
        String apiPort = properties.getProperty("api.port");

        if (apiAddr == null || apiAddr.isEmpty() || apiPort == null || apiPort.isEmpty()) {
            FormUtils.showError(ERR_NO_API_ADDR);
        } else {
            AppState.apiClient = ApiClient.getInstance(apiAddr, Integer.valueOf(apiPort));
        }
    }
}
