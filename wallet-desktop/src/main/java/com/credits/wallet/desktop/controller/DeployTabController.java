package com.credits.wallet.desktop.controller;

import com.credits.general.classload.ByteCodeContractClassLoader;
import com.credits.general.exception.CreditsException;
import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.util.Callback;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.scapi.v0.BasicStandard;
import com.credits.scapi.v0.ExtensionStandard;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import com.credits.wallet.desktop.utils.DeployControllerUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.CompilationResult;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import org.junit.Ignore;
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.credits.client.node.service.NodeApiServiceImpl.handleCallback;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.getContractFromTemplate;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.initErrorTableView;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.initSplitPane;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.initTabCodeArea;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.DEFAULT_STANDARD_CLASS;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class DeployTabController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(DeployTabController.class);

    public static final String DEFAULT_TEST = "DefaultTest";
    //SMART TAB
    @FXML
    public SplitPane smartMainSplitPane;
    @FXML
    public SplitPane smartInnerSplitPane;
    @FXML
    public VBox smartSourceCodeBox;
    @FXML
    public CreditsCodeArea smartCodeArea;
    @FXML
    public VBox smartErrorPanel;
    @FXML
    public TextArea testConsole;
    @FXML
    public Tab smartBottomErrorTab;
    @FXML
    public Tab testBottomErrorTab;
    @FXML
    public TabPane smartBottomTabPane;
    @FXML
    public TabPane testBottomTabPane;
    @FXML
    public Button testBuildButton;
    @FXML
    public Tab testBottomConsoleTab;
    @FXML
    public Label deployContractListLabel;
    @FXML
    public Button hideButton;
    @FXML
    private TreeViewController smartTreeViewController;
    @FXML
    public TableView<BuildSourceCodeError> smartErrorTableView;
    //TEST TAB
    @FXML
    public SplitPane testMainSplitPane;
    @FXML
    public SplitPane testInnerSplitPane;
    @FXML
    public CreditsCodeArea testCodeArea;
    @FXML
    public VBox testSourceCodeBox;
    @FXML
    public VBox testErrorPane1;
    @FXML
    public TreeViewController testTreeViewController;
    @FXML
    public TableView<BuildSourceCodeError> testErrorTableView;

    @FXML
    private ContextMenu contextMenu = new ContextMenu();
    @FXML
    public ComboBox<String> cbContractType;
    @FXML
    public ListView<DeploySmartListItem> deployContractList;
    @FXML
    public TabPane tabPane;
    @FXML
    public Tab testTab;
    @FXML
    public Tab smartTab;
    @FXML
    public Tab newSmartTab;
    @FXML
    public TextField className;
    @FXML
    public Button smartDeployButton;
    @FXML
    public Button smartBuildButton;
    @FXML
    public TextField feeField;
    @FXML
    public TextField usdSmarts;
    @FXML
    private Label actualOfferedMaxFeeLabel;


    OutputStream out;

    SmartContractDeployController parentController;


    @Override
    public void initializeForm(Map<String, Object> objects) {
        initConsoleOutput();
        initNewSmartTab();
        initSmartTab();
        initTestTab();
        initDeployContractList();
    }

    private void initConsoleOutput() {
        out = new OutputStream() {
            @Override
            public void write(int b) {
                appendTextToTextArea(testConsole, String.valueOf((char) b));
            }
        };
    }

    private void initTestTab() {
        initSplitPane(testInnerSplitPane, testErrorPane1, testErrorTableView);
        testCodeArea = initTabCodeArea(testSourceCodeBox, testTreeViewController, this, parentController);
        initErrorTableView(testErrorPane1, testErrorTableView, testCodeArea);
    }

    private void initSmartTab() {
        initSplitPane(smartInnerSplitPane, smartErrorPanel, smartErrorTableView);
        smartCodeArea = initTabCodeArea(smartSourceCodeBox, smartTreeViewController, this, parentController);
        initErrorTableView(smartErrorPanel, smartErrorTableView, smartCodeArea);
        FormUtils.initFeeField(feeField, actualOfferedMaxFeeLabel);
    }

    private void initNewSmartTab() {
        className.clear();
        ObservableList<String> items = cbContractType.getItems();
        items.clear();
        items.add(DEFAULT_STANDARD_CLASS);
        items.add(BasicStandard.class.getSimpleName());
        items.add(ExtensionStandard.class.getSimpleName());
        cbContractType.getSelectionModel().select(0);
        tabPane.getTabs().remove(newSmartTab);
    }

    private void initDeployContractList() {
        addDeleteOnKeyboardEvent(deployContractList);
        addDeleteOnSecondButtonEvent(deployContractList);
        addListViewSelectEvent(deployContractList);
        initStartListViewState(deployContractList);
    }

    private void addDeleteOnKeyboardEvent(ListView<DeploySmartListItem> listView) {
        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN);
        listView.setOnKeyPressed(event -> {
            if (keyCombinationShiftC.match(event)) {
                deleteCurrentListItem(listView);
            }
        });
    }

    private void initStartListViewState(ListView<DeploySmartListItem> listView) {
        ArrayList<DeploySmartListItem> deploySmartListItems =
            session.deployContractsKeeper.getKeptObject().orElseGet(ArrayList::new);
        if (deploySmartListItems.isEmpty()) {
            DeploySmartListItem deploySmartItem = new DeploySmartListItem("", "",
                DeployControllerUtils.checkContractNameExist("Contract", listView.getItems()),
                DeploySmartListItem.ItemState.NEW);
            listView.getItems().add(deploySmartItem);
            listView.getSelectionModel().selectFirst();
            session.deployContractsKeeper.keepObject(new ArrayList<>(listView.getItems()));
        } else {
            listView.getItems().addAll(deploySmartListItems);
            listView.getSelectionModel().select(session.lastSmartIndex);
        }
    }

    private void addListViewSelectEvent(ListView<DeploySmartListItem> listView) {
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                saveCodeFromTextArea(oldValue);
            }
            changeTab(newValue);
            session.lastSmartIndex = listView.getSelectionModel().getSelectedIndex();
        });
    }

    private void changeTab(DeploySmartListItem currentItem) {
        parentController.cleanCompilationPackage(false);
        if (currentItem != null) {
            if (currentItem.state.equals(DeploySmartListItem.ItemState.NEW)) {
                initNewSmartTab();
                /*newSmartTab.setDisable(false);
                smartTab.setDisable(true);
                testTab.setDisable(true);*/
                Platform.runLater(() -> {
                    tabPane.getTabs().clear();
                    tabPane.getTabs().addAll(newSmartTab);
                    tabPane.getSelectionModel().select(newSmartTab);
                });
            } else {
                smartCodeArea.replaceText(currentItem.sourceCode);
                testCodeArea.replaceText(currentItem.testSourceCode);
                Platform.runLater(() -> {
                    tabPane.getTabs().clear();
                    String tabName;
                    if(currentItem.name.indexOf("(")>0) {
                        tabName = currentItem.name.substring(0, currentItem.name.indexOf("("));
                    } else {
                        tabName = currentItem.name;
                    }
                    smartTab.setText(tabName);
                    testTab.setText(tabName +"Test");
                    tabPane.getTabs().addAll(smartTab,testTab);
                /*newSmartTab.setDisable(true);
                smartTab.setDisable(false);
                testTab.setDisable(false);*/
                    tabPane.getSelectionModel().select(smartTab);
                    smartTreeViewController.refreshTreeView(smartCodeArea);
                    testTreeViewController.refreshTreeView(testCodeArea);
                });
            }
        }
    }

    private void addDeleteOnSecondButtonEvent(ListView<DeploySmartListItem> listView) {
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Platform.runLater(() -> {
                    contextMenu.getItems().clear();
                    contextMenu.hide();
                    MenuItem removeItem = new MenuItem("Delete");
                    contextMenu.getItems().add(removeItem);
                    removeItem.setOnAction(event1 -> {
                        deleteCurrentListItem(listView);
                    });
                    contextMenu.show(listView, event.getScreenX(), event.getScreenY());
                });
            }
        });
    }

    private void deleteCurrentListItem(ListView<DeploySmartListItem> listView) {
        listView.getItems().remove(getCurrentListItem(deployContractList));
        session.deployContractsKeeper.keepObject(new ArrayList<>(listView.getItems()));
        if (listView.getItems().size() == 0) {
            handleAddContract();
        }
    }


    public void handleAddContract() {
        DeploySmartListItem deploySmartItem = new DeploySmartListItem("", "",
            DeployControllerUtils.checkContractNameExist("NewContract", deployContractList.getItems()),
            DeploySmartListItem.ItemState.NEW);
        deployContractList.getItems().add(deploySmartItem);
        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        deployContractList.getSelectionModel().selectLast();
    }


    @FXML
    public void handleGenerateSmart() {
        String selectedType = cbContractType.getSelectionModel().getSelectedItem();
        String curClassName;
        if (className.getText().isEmpty()) {
            curClassName = "MySmartContract";
        } else {
            curClassName = className.getText();

            if ((!SourceVersion.isIdentifier(curClassName) && !SourceVersion.isKeyword(curClassName)) ||
                !curClassName.matches("^[a-zA-Z0-9]+$")) {
                FormUtils.showInfo("ClassName is not valid");
                return;
            }
        }
        String contractFromTemplate = getContractFromTemplate(selectedType);
        String sourceCode = null;
        String testSourceCode = null;
        if (contractFromTemplate != null) {
            sourceCode = String.format(contractFromTemplate, curClassName, curClassName);
        }
        String contractFromTemplate1 = getContractFromTemplate(DEFAULT_TEST);
        if (contractFromTemplate1 != null) {
            testSourceCode =
                String.format(contractFromTemplate1, curClassName, curClassName, curClassName, session.account);
        }
        DeploySmartListItem item = getCurrentListItem(deployContractList);
        item.sourceCode = sourceCode;
        item.testSourceCode = testSourceCode;
        item.name = DeployControllerUtils.checkContractNameExist(curClassName, deployContractList.getItems());
        item.state = DeploySmartListItem.ItemState.SAVED;
        deployContractList.refresh();
        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        changeTab(item);
    }


    private void saveCodeFromTextArea(DeploySmartListItem item) {
        item.sourceCode = smartCodeArea.getText();
        item.testSourceCode = testCodeArea.getText();
    }


    private DeploySmartListItem getCurrentListItem(ListView<DeploySmartListItem> listView) {
        return listView.getSelectionModel().getSelectedItem();
    }

    @Override
    public void formDeinitialize() {
        DeploySmartListItem item = getCurrentListItem(deployContractList);
        saveCodeFromTextArea(item);
        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        smartCodeArea.cleanAll();
        testCodeArea.cleanAll();
    }

    @FXML
    public void handleBuildSmart() {
        parentController.handleBuild(smartCodeArea, smartErrorTableView, smartErrorPanel, smartBottomTabPane,
            smartBottomErrorTab);
    }

    @FXML
    public void handleDeploySmart() {
        parentController.handleDeploy();
    }

    public static class FilterRunner extends BlockJUnit4ClassRunner {

        private List<String> testsToRun;

        FilterRunner(Class<?> klass, List<String> testsToRun) throws InitializationError {
            super(klass);
            this.testsToRun = testsToRun;
        }

        @Override
        protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
            Description description = describeChild(method);
            if (method.getAnnotation(Ignore.class) != null || !testsToRun.contains(method.getName())) {
                notifier.fireTestIgnored(description);
            } else {
                runLeaf(methodBlock(method), description, notifier);
            }
        }

    }

    public void doTestMethod(String methodName) {
        try {
            Class<?> testClass = compileTestClasses();
            if (testClass != null) {
                testConsole.clear();
                JUnitCore junit = new JUnitCore();
                junit.addListener(new TextListener(new PrintStream(out, true)));
                junit.run(new FilterRunner(testClass, Collections.singletonList(methodName)));
                testBottomTabPane.getSelectionModel().select(testBottomConsoleTab);
            } else {
                testBottomTabPane.getSelectionModel().select(testBottomErrorTab);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @FXML
    public void handleBuildTest() {
        testBuildButton.setDisable(true);
        supplyAsync(this::compileTestClasses).whenComplete(handleCallback(handleBuildResult()));

    }


    private Callback<Class> handleBuildResult() {
        return new Callback<Class>() {
            @Override
            public void onSuccess(Class resultData) throws CreditsException {
                testBuildButton.setDisable(false);
                testErrorTableView.getItems().clear();
                testErrorTableView.refresh();
            }

            @Override
            public void onError(Throwable e) {
                testBuildButton.setDisable(false);
                LOGGER.error("failed!", e);
            }
        };
    }

    public void appendTextToTextArea(TextArea textArea, String str) {
        Platform.runLater(() -> textArea.appendText(str));
    }


    private static Class<?> compileSmartContractByteCode(
        ByteCodeContractClassLoader classLoader,
        List<ByteCodeObjectData> smartContractByteCodeData) {
        Class<?> testClass = null;
        for (ByteCodeObjectData compilationUnit : smartContractByteCodeData) {
            Class<?> tempContractClass =
                classLoader.loadClass(compilationUnit.getName(), compilationUnit.getByteCode());
            if (compilationUnit.getName().contains("Test")) {
                testClass = tempContractClass;
            }
        }
        return testClass;
    }

    public Class<?> compileTestClasses() {
        String contractSourceCode = smartCodeArea.getText();
        String testSourceCode = testCodeArea.getText();
        List<String> sourceCodes = new ArrayList<>();
        sourceCodes.add(contractSourceCode);
        sourceCodes.add(testSourceCode);
        ByteCodeContractClassLoader classLoader = new ByteCodeContractClassLoader();
        CompilationResult compilationResult = SourceCodeBuilder.compileSourceCode(sourceCodes);
        if (parentController.checkNotError(testErrorPane1, testErrorTableView, compilationResult, testBottomTabPane,
            testBottomErrorTab)) {
            CompilationPackage compilationPackage = compilationResult.getCompilationPackage();
            List<ByteCodeObjectData> byteCodeObjectDataList =
                GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
            return compileSmartContractByteCode(classLoader, byteCodeObjectDataList);
        }
        return null;
    }

    private double oldTabPaneWidth;
    private double oldTabPaneX;
    private double oldHideButtonX;
    private ImageView oldHideButtonImage;
    public void hideList() {
        Platform.runLater(() -> {
            if (deployContractList.isVisible()) {
                deployContractListLabel.setVisible(false);
                deployContractList.setVisible(false);
                oldTabPaneWidth = tabPane.getWidth();
                oldTabPaneX = tabPane.getLayoutX();
                oldHideButtonX = hideButton.getLayoutX();
                oldHideButtonImage = (ImageView) hideButton.getGraphic();
                tabPane.setPrefWidth(tabPane.getWidth()+(tabPane.getLayoutX()-deployContractListLabel.getLayoutX()));
                hideButton.setLayoutX(deployContractListLabel.getLayoutX());
                ImageView value = new ImageView(new Image(getClass().getResourceAsStream("/img/vi.png")));
                value.setFitWidth(oldHideButtonImage.getFitWidth());
                value.setFitHeight(oldHideButtonImage.getFitHeight());
                hideButton.setGraphic(value);
                tabPane.setLayoutX(hideButton.getLayoutX()+hideButton.getPrefWidth()+10);
            } else {
                deployContractListLabel.setVisible(true);
                deployContractList.setVisible(true);
                hideButton.setGraphic(oldHideButtonImage);
                hideButton.setLayoutX(oldHideButtonX);
                tabPane.setPrefWidth(oldTabPaneWidth);
                tabPane.setLayoutX(oldTabPaneX);
            }
        });
    }



}
