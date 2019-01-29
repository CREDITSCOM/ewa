package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.VistaNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.credits.wallet.desktop.AppState.DELAY_AFTER_FULL_SYNC;
import static com.credits.wallet.desktop.AppState.DELAY_BEFORE_FULL_SYNC;
import static com.credits.wallet.desktop.AppState.nodeApiService;

/**
 * Created by goncharov-eg on 23.11.2017.
 */
public class HeaderController extends AbstractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeaderController.class);

    public AbstractController parentController;

    @FXML
    public ProgressBar sync;

    @FXML
    public Label syncPercent;
    
    private ScheduledExecutorService headerExecService = Executors.newScheduledThreadPool(1);
    private Runnable runnable;
    private Future future;
    private boolean flag = false;
    @FXML
    private void handleLogout() {
        closeSession();
        VistaNavigator.loadVista(VistaNavigator.WELCOME, this);
    }

    public void handleWallet() {
        VistaNavigator.loadVista(VistaNavigator.WALLET, this);
    }

    public void handleTransaction() {
        VistaNavigator.loadVista(VistaNavigator.HISTORY, this);
    }

    public void handleSmartExecute() {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT, this);
    }

    public void handleSmartDeploy() {
        VistaNavigator.loadVista(VistaNavigator.SMART_CONTRACT_DEPLOY, this);
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        headerExecService = Executors.newScheduledThreadPool(1);
        runnable = () -> {
            try {
                int synchronizePercent = nodeApiService.getSynchronizePercent();
                sync.setProgress(synchronizePercent);
                syncPercent.setText(synchronizePercent + "%");
                if(synchronizePercent ==100 && !flag) {
                    changeDelay();
                }
            } catch (Exception e) {
                sync.setProgress(0);
                syncPercent.setText("0%");
            }
        };
        future = headerExecService.scheduleWithFixedDelay(runnable, 0, DELAY_BEFORE_FULL_SYNC, TimeUnit.SECONDS);
    }

    private void changeDelay() {
        flag = future.cancel(false);
        System.out.println("Synchronized is 100% : " + flag);
        future = headerExecService.scheduleWithFixedDelay(runnable, 0, DELAY_AFTER_FULL_SYNC, TimeUnit.SECONDS);
    }

    @Override
    public void formDeinitialize() {
        parentController.formDeinitialize();
        headerExecService.shutdown();
    }
}
