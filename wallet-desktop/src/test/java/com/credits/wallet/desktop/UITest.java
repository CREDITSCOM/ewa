package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.util.Callback;
import com.credits.general.util.ObjectKeeper;
import com.credits.wallet.desktop.service.ContractInteractionService;
import com.credits.wallet.desktop.testUtils.FakeData;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import static com.credits.wallet.desktop.AppState.account;
import static com.credits.wallet.desktop.testUtils.FakeData.addressBase58;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Igor Goryunov on 29.09.2018
 */
public class UITest {

    App app;
    String walletAddress;
    String addressOne;
    String addressTwo;
    String addressThree;

    /*
    TransactionFlowResultData successResponse = new TransactionFlowResultData(new ApiResponseData(SUCCESS, "Success"),1312, Converter
        .decodeFromBASE58(addressOne), Converter.decodeFromBASE58(addressTwo), new Variant(V_STRING, "success variant response"));
    */
    @Mock
    AppStateInitializer mockInitializer;
    @Mock
    NodeApiService mockNodeApiService;
    @Mock
    ContractExecutorApiService mockContractExecutorService;
    @Mock
    ContractInteractionService mockContractInteractionService;
    @Mock
    Properties mockProperties;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockInitializer.initializeNodeApiService()).thenReturn(mockNodeApiService);
        when(mockInitializer.initializeContractExecutorApiService()).thenReturn(mockContractExecutorService);
        when(mockInitializer.initializeContractInteractionService()).thenReturn(mockContractInteractionService);
        when(mockInitializer.loadProperties()).thenReturn(mockProperties);
        doCallRealMethod().when(mockInitializer).init();

        AppState.account = walletAddress;
        AppState.coinsKeeper = new ObjectKeeper<>(account, "coins");
        AppState.favoriteContractsKeeper = new ObjectKeeper<>(AppState.account, "favorite");
        when(mockNodeApiService.getBalance(anyString())).thenReturn(new BigDecimal("1000.123456789012345678"));

        app = new App();
        app.appStateInitializer = mockInitializer;
        walletAddress = addressBase58;
        addressOne = "11111111111111111111111111111111111111111111";
        addressTwo = "22222222222222222222222222222222222222222222";
        addressThree = "33333333333333333333333333333333333333333333";
        NodeApiServiceImpl.account = walletAddress;
    }

    @Ignore
    @Test
    public void allForms() throws Exception {
        mockInitializer.startForm = VistaNavigator.HISTORY;
        AppState.account = walletAddress;
        //balances
        doAnswer(returnBalance(new BigDecimal("2443113.00192177821876551"))).when(mockContractInteractionService).getSmartContractBalance(anyString(), any());

        when(mockNodeApiService.getTransactionsState(any(), any())).thenReturn(FakeData.transactionsStateGetResult);

        //transactions
        when(mockNodeApiService.getTransactions(any(), anyLong(), anyLong())).thenReturn(FakeData.transactionsDataList);
//        when(mockNodeApiService.transactionFlow(any())).thenReturn(successResponse);
        when(mockNodeApiService.getWalletTransactionsCount(any())).thenReturn(new Long(1));
        when(mockNodeApiService.getWalletId(walletAddress)).thenReturn(1);
        when(mockNodeApiService.getWalletId(addressTwo)).thenReturn(2);
        when(mockNodeApiService.getWalletId(addressThree)).thenReturn(0);

        //smart-contracts
        when(mockNodeApiService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
        when(mockNodeApiService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);
        runApp();
    }

    @Ignore
    @Test
    public void smartContractsForm() throws Exception {
        mockInitializer.startForm = VistaNavigator.SMART_CONTRACT;
        when(mockNodeApiService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
        when(mockNodeApiService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);
        runApp();
    }

    @Ignore
    @Test
    public void deployForm() throws Exception {
        mockInitializer.startForm = VistaNavigator.SMART_CONTRACT_DEPLOY;
        AppState.account = walletAddress;
        runApp();
    }

    @SuppressWarnings("unchecked")
    private Answer<Void> returnBalance(BigDecimal balance){
        return answer -> {
            ((Callback<BigDecimal>) answer.getArgument(1)).onSuccess(balance);
            return null;
        };
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
