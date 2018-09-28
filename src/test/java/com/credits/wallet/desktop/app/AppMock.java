package com.credits.wallet.desktop.app;
import com.credits.common.utils.Converter;
import com.credits.crypto.Ed25519;
import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.AppStateInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

import static org.mockito.Mockito.*;

public class AppMock extends Application {

    private AppStateInitializer appStateInitializer;

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppStateInitializer appStateInitializer = spy(AppStateInitializer.class);
        when(appStateInitializer.getLevelDbService()).thenReturn(new FakeLevelDbService());
        appStateInitializer.init();
        try {
            String pubKey = "GWe8WZYLBxAqsfPZgejnysXQm5Q697VSsyr3x59RvYBf";
            String privKey = "3kFytL3Ysgn8aGkLMEtsw8bsG12An9P7aPFfXUYnK9JCFvuWWUwtFaRUzBTyTXFmF7XWsrC3AN7p9GAhSoJtRt6q";
            AppState.account = pubKey;
            byte[] publicKeyByteArr = Converter.decodeFromBASE58(pubKey);
            byte[] privateKeyByteArr = Converter.decodeFromBASE58(privKey);
            AppState.publicKey = Ed25519.bytesToPublicKey(publicKeyByteArr);
            AppState.privateKey = Ed25519.bytesToPrivateKey(privateKeyByteArr);
        } catch (Exception e) {

        }
        App.showForm("/fxml/form6.fxml", "Wallet");
    }
}
