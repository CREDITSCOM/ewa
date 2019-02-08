package com.credits.wallet.desktop.controller;

import com.credits.general.pojo.ByteCodeObjectData;
import com.credits.general.util.ByteArrayContractClassLoader;
import com.credits.general.util.GeneralConverter;
import com.credits.general.util.compiler.model.CompilationPackage;
import com.credits.wallet.desktop.struct.DeploySmartListItem;
import com.credits.wallet.desktop.utils.DeployControllerUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.building.SourceCodeBuilder;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.credits.wallet.desktop.utils.DeployControllerUtils.getContractFromTemplate;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.initErrorTableView;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.initSplitPane;
import static com.credits.wallet.desktop.utils.DeployControllerUtils.initTabCodeArea;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.BASIC_STANDARD_CLASS;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.DEFAULT_STANDARD_CLASS;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.EXTENSION_STANDARD_CLASS;

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


    private Tab prevSelectTab;
    SmartContractDeployController parentController;


    @Override
    public void initializeForm(Map<String, Object> objects) {
        initNewSmartTab();
        initSmartTab();
        initTestTab();
        initDeployContractList();
    }

    private void initTestTab() {
        testCodeArea = initTabCodeArea(testSourceCodeBox, testTreeViewController, this,
            parentController);
    }

    private void initSmartTab() {
        initSplitPane(smartInnerSplitPane, smartErrorPanel, smartErrorTableView);
        initErrorTableView(smartErrorPanel, smartErrorTableView, smartCodeArea);
        smartCodeArea = initTabCodeArea(smartSourceCodeBox, smartTreeViewController, this,
            parentController);
    }

    private void initNewSmartTab() {
        className.clear();
        ObservableList<String> items = cbContractType.getItems();
        items.clear();
        items.add(DEFAULT_STANDARD_CLASS);
        items.add(BASIC_STANDARD_CLASS);
        items.add(EXTENSION_STANDARD_CLASS);
        cbContractType.getSelectionModel().select(0);
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
            if(oldValue!=null) {
                saveCodeFromTextArea(oldValue);
            }
            changeTab(newValue);
            session.lastSmartIndex = listView.getSelectionModel().getSelectedIndex();
        });
    }

    private void changeTab(DeploySmartListItem currentItem) {
        if (currentItem.state.equals(DeploySmartListItem.ItemState.NEW)) {
            initNewSmartTab();
            newSmartTab.setDisable(false);
            smartTab.setDisable(true);
            testTab.setDisable(true);
            tabPane.getSelectionModel().select(newSmartTab);
            parentController.feeDeployPane.setVisible(false);
        } else {
            smartCodeArea.replaceText(currentItem.sourceCode);
            testCodeArea.replaceText(currentItem.testSourceCode);
            newSmartTab.setDisable(true);
            smartTab.setDisable(false);
            testTab.setDisable(false);
            tabPane.getSelectionModel().select(smartTab);
            smartTreeViewController.refreshTreeView(smartCodeArea);
            testTreeViewController.refreshTreeView(testCodeArea);
            parentController.feeDeployPane.setVisible(true);
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
            curClassName = "Contract";
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
            String contractSourceCode = smartCodeArea.getText();
            String testSourceCode = testCodeArea.getText();
            ArrayList<String> sourceCodes = new ArrayList<>();
            sourceCodes.add(contractSourceCode);
            sourceCodes.add(testSourceCode);

            ByteArrayContractClassLoader classLoader = new ByteArrayContractClassLoader();
            Class<?> testClass = compileClasses(classLoader, sourceCodes);
            JUnitCore junit = new JUnitCore();
            junit.addListener(new TextListener(System.out));
            junit.run(new FilterRunner(testClass, Collections.singletonList(methodName)));
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

    }

    private static Class<?> compileSmartContractByteCode(ByteArrayContractClassLoader classLoader,
        List<ByteCodeObjectData> smartContractByteCodeData) {
        Class<?> testClass = null;
        for (ByteCodeObjectData compilationUnit : smartContractByteCodeData) {
            Class<?> tempContractClass =
                classLoader.buildClass(compilationUnit.getName(), compilationUnit.getByteCode());
            if (compilationUnit.getName().contains("Test")) {
                testClass = tempContractClass;
            }
        }
        return testClass;
    }

    public Class<?> compileClasses(ByteArrayContractClassLoader classLoader, ArrayList<String> sourceCode) {
        CompilationPackage compilationPackage = SourceCodeBuilder.compileSourceCode(sourceCode).getCompilationPackage();
        if (compilationPackage.isCompilationStatusSuccess()) {
            List<ByteCodeObjectData> byteCodeObjectDataList =
                GeneralConverter.compilationPackageToByteCodeObjects(compilationPackage);
            return compileSmartContractByteCode(classLoader, byteCodeObjectDataList);
        } else {
            return null;
        }
    }
}
