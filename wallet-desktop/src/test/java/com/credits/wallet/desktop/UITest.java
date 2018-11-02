package com.credits.wallet.desktop;

import com.credits.client.executor.service.ContractExecutorApiService;
import com.credits.client.node.service.NodeApiService;
import com.credits.client.node.service.NodeApiServiceImpl;
import com.credits.general.pojo.ApiResponseData;
import com.credits.general.thrift.generated.Variant;
import com.credits.general.util.Callback;
import com.credits.wallet.desktop.service.ContractInteractionService;
import com.credits.wallet.desktop.testUtils.FakeData;
import com.credits.wallet.desktop.utils.CoinsUtils;
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

import static com.credits.general.pojo.ApiResponseCode.SUCCESS;
import static com.credits.general.thrift.generated.Variant._Fields.V_STRING;
import static com.credits.wallet.desktop.testUtils.FakeData.addressBase58;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
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

    ApiResponseData successResponse = new ApiResponseData(SUCCESS, "Success", new Variant(V_STRING, "Success"));

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
        mockInitializer.startForm = VistaNavigator.SMART_CONTRACT;
        NodeApiServiceImpl.sourceMap = FakeData.sourceMap;
        CoinsUtils.saveCoinsToFile(FakeData.coins);

        //balances
        doAnswer(returnBalance(new BigDecimal("2443113.00192177821876551"))).when(mockContractInteractionService).getSmartContractBalance(anyString(), any());

        when(mockNodeApiService.getBalance(anyString())).thenReturn(new BigDecimal("1000.123456789012345678"));
        when(mockNodeApiService.getTransactionsState(any(), any())).thenReturn(FakeData.transactionsStateGetResult);

        //transactions
        when(mockNodeApiService.getTransactions(any(), anyLong(), anyLong())).thenReturn(FakeData.transactionsDataList);
        when(mockNodeApiService.transactionFlow(any())).thenReturn(successResponse);
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
