package com.credits.wallet.desktop;

import com.credits.client.executor.exception.ContractExecutorClientException;
import com.credits.client.node.exception.NodeClientException;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generate.Variant;
import com.credits.general.util.ObjectKeeper;
import com.credits.general.util.exception.ConverterException;
import com.credits.wallet.desktop.testUtils.FakeData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import static com.credits.client.node.service.ApiResponseCode.SUCCESS;
import static com.credits.general.thrift.generate.Variant._Fields.V_STRING;
import static com.credits.wallet.desktop.testUtils.FakeData.addressBase58;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by Igor Goryunov on 29.09.2018
 */
public class UITest {


    App app;
    String walletAddress;
    String addressOne;
    String addressTwo;
    String addressThree;

    ApiResponseData successResponse =
        new ApiResponseData((byte) SUCCESS.code, "Success", new Variant(V_STRING, "Success"));

    @Before
    public void setUp() {
        app = new App();
        walletAddress = addressBase58;
        addressOne = "11111111111111111111111111111111111111111111";
        addressTwo = "22222222222222222222222222222222222222222222";
        addressThree = "33333333333333333333333333333333333333333333";
    }

    @Ignore
    @Test
    public void correctBehavior() throws InterruptedException, NodeClientException, ConverterException {
        AppStateInitializer spyInitializer = spy(AppStateInitializer.class);
        when(spyInitializer.loadProperties()).thenReturn(mock(Properties.class));
        NodeApiService mockApiService = mock(NodeApiService.class);

        NodeApiServiceImpl.sourceMap = FakeData.sourceMap;
        //balances
        when(mockApiService.getBalance(any())).thenReturn(new BigDecimal("1000.123456789012345678"));
/*
        when(mockLevelDbService.getBalance(addressOne)).thenReturn(new BigDecimal("0"));
        when(mockLevelDbService.getBalance(addressTwo)).thenReturn(new BigDecimal("100"));
        when(mockLevelDbService.getBalance(addressThree)).thenReturn(new BigDecimal(new Random(System.currentTimeMillis()).nextDouble()));
*/
        when(mockApiService.getTransactionsState(any(), any())).thenReturn(FakeData.transactionsStateGetResult);
        //transactions
        when(mockApiService.getTransactions(any(), anyLong(), anyLong())).thenReturn(FakeData.transactionsDataList);
        when(mockApiService.createTransaction(any(), anyBoolean())).thenReturn(successResponse);
        when(mockApiService.getWalletTransactionsCount(any())).thenReturn(new Long(1));
        when(mockApiService.getWalletId(walletAddress)).thenReturn(1);
        when(mockApiService.getWalletId(addressTwo)).thenReturn(2);
        when(mockApiService.getWalletId(addressThree)).thenReturn(0);

        //smart-contracts
        when(mockApiService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
        when(mockApiService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);

        doReturn(mockApiService).when(spyInitializer).initializeNodeApiService();
        app.appStateInitializer = spyInitializer;

        runApp();
    }


    @Ignore
    @Test
    public void deployForm()
        throws InterruptedException, NodeClientException, ConverterException, ContractExecutorClientException {
        AppStateInitializer spyInitializer = spy(AppStateInitializer.class);
        when(spyInitializer.loadProperties()).thenReturn(mock(Properties.class));
        NodeApiService mockLevelDbService = mock(NodeApiService.class);
        when(mockLevelDbService.getBalance(any())).thenReturn(new BigDecimal("1000.123456789012345678"));

        //fixme add mocks
//        ApiResponseData resp = new ApiResponseData(0, SUCCESS);
//        Variant ret_val = new Variant();
//        ret_val.setV_double(1000.12345D);
//        resp.setScExecRetVal(ret_val);
//        when(mockLevelDbService.executeSmartContract(anyLong(), anyString(), anyString(), any())).thenReturn(resp);

        //smart-contracts
//        when(mockNodeApiService.getTransactions(any(), anyLong(), anyLong())).thenReturn(FakeData.transactionsDataList);
//        when(mockNodeApiService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
//        when(mockNodeApiService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);

//        doReturn(mockNodeApiService).when(spyInitializer).initializeNodeApiService();

        spyInitializer.startForm = VistaNavigator.SMART_CONTRACT_DEPLOY;
        app.appStateInitializer = spyInitializer;
        AppState.account = walletAddress;
        AppState.smartContractsKeeper = new ObjectKeeper<>(NodeApiServiceImpl.account, "obj");
        NodeApiServiceImpl.account = AppState.account;
        runApp();
    }

    private void runApp() throws InterruptedException {
        new JFXPanel();
        Platform.runLater(() -> {
            try {
                app.start(new Stage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        Thread.currentThread().join();
    }

}
