package com.credits.wallet.desktop;

import com.credits.common.exception.CreditsCommonException;
import com.credits.common.utils.Converter;
import com.credits.leveldb.client.data.ApiResponseData;
import com.credits.leveldb.client.exception.CreditsNodeException;
import com.credits.leveldb.client.exception.LevelDbClientException;
import com.credits.leveldb.client.service.LevelDbService;
import com.credits.thrift.generated.Variant;
import com.credits.wallet.desktop.testUtils.FakeData;
import com.credits.wallet.desktop.testUtils.JavaFXThreadingRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.Random;

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
public class UITest {

    @Rule
    public JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    App app;
    byte[] walletAddress;
    byte[] addressOne;
    byte[] addressTwo;
    byte[] addressThree;

    ApiResponseData successResponse = new ApiResponseData(API_RESPONSE_SUCCESS_CODE, "Success", new Variant(V_STRING, "Success"));

    @Before
    public void setUp() throws CreditsCommonException {
        app = new App();
        walletAddress = Converter.decodeFromBASE58(addressBase58);
        addressOne = Converter.decodeFromBASE58("11111111111111111111111111111111111111111111");
        addressTwo = Converter.decodeFromBASE58("22222222222222222222222222222222222222222222");
        addressThree = Converter.decodeFromBASE58("33333333333333333333333333333333333333333333");
    }

    @Test
    @Ignore
    public void correctBehavior() throws LevelDbClientException, CreditsNodeException {
        AppStateInitializer spyInitializer = spy(AppStateInitializer.class);
        when(spyInitializer.loadProperties()).thenReturn(mock(Properties.class));
        LevelDbService mockLevelDbService = mock(LevelDbService.class);

        //balances
        when(mockLevelDbService.getBalance(walletAddress)).thenReturn(new BigDecimal("1000.123456789012345678"));
        when(mockLevelDbService.getBalance(addressOne)).thenReturn(new BigDecimal("0"));
        when(mockLevelDbService.getBalance(addressTwo)).thenReturn(new BigDecimal("100"));
        when(mockLevelDbService.getBalance(addressThree)).thenReturn(new BigDecimal(new Random(System.currentTimeMillis()).nextDouble()));

        //transactions
        when(mockLevelDbService.getTransactions(any(), anyLong(), anyLong())).thenReturn(FakeData.transactionsDataList);
        when(mockLevelDbService.createTransaction(any(),anyBoolean())).thenReturn(successResponse);
        when(mockLevelDbService.asyncCreateTransaction(any(),anyBoolean(),any())).thenReturn(successResponse);
        when(mockLevelDbService.getWalletTransactionsCount(any())).thenReturn(new Long(1));
        when(mockLevelDbService.getWalletId(walletAddress)).thenReturn(1);
        when(mockLevelDbService.getWalletId(addressTwo)).thenReturn(2);
        when(mockLevelDbService.getWalletId(addressThree)).thenReturn(0);

        //smart-contracts
        when(mockLevelDbService.getSmartContract(any())).thenReturn(FakeData.smartContractDataList.get(1));
        when(mockLevelDbService.getSmartContracts(any())).thenReturn(FakeData.smartContractDataList);

        doReturn(mockLevelDbService).when(spyInitializer).initializeLevelDbService();
        app.appStateInitializer = spyInitializer;
        app.start(null);
    }
}
