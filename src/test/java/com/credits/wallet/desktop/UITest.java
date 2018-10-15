package com.credits.wallet.desktop;

import com.credits.common.exception.CreditsCommonException;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.service.LevelDbService;
import com.credits.leveldb.client.service.LevelDbServiceImpl;
import com.credits.thrift.generated.Variant;
import com.credits.wallet.desktop.testUtils.FakeData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import static com.credits.leveldb.client.ApiClient.API_RESPONSE_SUCCESS_CODE;
import static com.credits.thrift.generated.Variant._Fields.V_STRING;
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
public class UITest{


    App app;
    String walletAddress;
    String addressOne;
    String addressTwo;
    String addressThree;

    ApiResponseData successResponse = new ApiResponseData(API_RESPONSE_SUCCESS_CODE, "Success", new Variant(V_STRING, "Success"));

    @Before
    public void setUp() {
        app = new App();
        walletAddress = addressBase58;
        addressOne = "11111111111111111111111111111111111111111111";
        addressTwo = "22222222222222222222222222222222222222222222";
        addressThree = "33333333333333333333333333333333333333333333";
    }


    @Test
    public void correctBehavior()
        throws LevelDbClientException, CreditsNodeException, CreditsCommonException, IOException, InterruptedException {
        AppStateInitializer spyInitializer = spy(AppStateInitializer.class);
        when(spyInitializer.loadProperties()).thenReturn(mock(Properties.class));
        LevelDbService mockLevelDbService = mock(LevelDbService.class);

        LevelDbServiceImpl.sourceMap = FakeData.sourceMap;
        //balances
        when(mockLevelDbService.getBalance(any())).thenReturn(new BigDecimal("1000.123456789012345678"));
/*
        when(mockLevelDbService.getBalance(addressOne)).thenReturn(new BigDecimal("0"));
        when(mockLevelDbService.getBalance(addressTwo)).thenReturn(new BigDecimal("100"));
        when(mockLevelDbService.getBalance(addressThree)).thenReturn(new BigDecimal(new Random(System.currentTimeMillis()).nextDouble()));
*/
        when(mockLevelDbService.getTransactionsState(any(),any())).thenReturn(FakeData.transactionsStateGetResult);
        //transactions
        when(mockLevelDbService.getTransactions(any(), anyLong(), anyLong())).thenReturn(FakeData.transactionsDataList);
        when(mockLevelDbService.createTransaction(any(),anyBoolean())).thenReturn(successResponse);
        when(mockLevelDbService.getWalletTransactionsCount(any())).thenReturn(new Long(1));
        when(mockLevelDbService.getWalletId(walletAddress)).thenReturn(1);
        when(mockLevelDbService.getWalletId(addressTwo)).thenReturn(2);
        when(mockLevelDbService.getWalletId(addressThree)).thenReturn(0);

        //smart-contracts
        when(mockLevelDbService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
        when(mockLevelDbService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);

        doReturn(mockLevelDbService).when(spyInitializer).initializeLevelDbService();
        app.appStateInitializer = spyInitializer;

        initializeApp();
    }


    @Test
    public void deployForm()
        throws CreditsCommonException, LevelDbClientException, CreditsNodeException, IOException, InterruptedException {
        AppStateInitializer spyInitializer = spy(AppStateInitializer.class);
        when(spyInitializer.loadProperties()).thenReturn(mock(Properties.class));
        LevelDbService mockLevelDbService = mock(LevelDbService.class);

        //smart-contracts
        when(mockLevelDbService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
        when(mockLevelDbService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);

        doReturn(mockLevelDbService).when(spyInitializer).initializeLevelDbService();
        spyInitializer.startForm = VistaNavigator.SMART_CONTRACT;
        app.appStateInitializer = spyInitializer;
        AppState.account=walletAddress;
        initializeApp();
    }

    private void initializeApp() throws InterruptedException {
        Thread thread = new Thread(() -> {
            new JFXPanel();
            Platform.runLater(() -> {
                try {
                    app.start(new Stage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
        thread.start();
        Thread.sleep(1000000);
    }

}
