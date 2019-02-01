package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.struct.DeploySmartListItem;
import com.credits.wallet.desktop.utils.DeployControllerUtils;
import com.credits.wallet.desktop.utils.FormUtils;
import com.credits.wallet.desktop.utils.sourcecode.building.BuildSourceCodeError;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CodeAreaUtils;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.SourceVersion;
import java.util.ArrayList;
import java.util.Map;

import static com.credits.wallet.desktop.utils.DeployControllerUtils.getContractFromTemplate;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea.DEFAULT_SOURCE_CODE;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.BASIC_STANDARD_CLASS;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.DEFAULT_STANDARD_CLASS;
import static com.credits.wallet.desktop.utils.sourcecode.codeArea.autocomplete.CreditsProposalsPopup.EXTENSION_STANDARD_CLASS;

public class DeployTabController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(DeployTabController.class);

    public static final String DEFAULT_TEST = "DefaultTest";
    @FXML
    public SplitPane tabContent;
    @FXML
    private TreeViewController treeViewsController;
    @FXML
    private ContextMenu contextMenu = new ContextMenu();
    @FXML
    public ComboBox<String> cbContractType;
    @FXML
    public ListView<DeploySmartListItem> deployContractList;
    @FXML
    public TabPane tabPane;
    @FXML
    public Tab testingTab;
    @FXML
    public Tab codeAreaTab;
    @FXML
    public Tab createCodeTab;
    @FXML
    public TextField className;
    @FXML
    public TableView<BuildSourceCodeError> errorTableView;
    @FXML
    public SplitPane splitPane;
    @FXML
    public Pane paneCode;
    @FXML
    public Pane debugPane;
    @FXML
    public CreditsCodeArea codeArea;

    SmartContractDeployController parentController;

    public void handleAddContract() {
        DeploySmartListItem deploySmartItem = new DeploySmartListItem(null, null,
            DeployControllerUtils.checkContractNameExist("NewContract", deployContractList.getItems()),
            DeploySmartListItem.ItemState.NEW);
        deployContractList.getItems().add(deploySmartItem);
        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        deployContractList.getSelectionModel().selectLast();
        deleteMainTabs();
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
        try {
            String contractFromTemplate = getContractFromTemplate(selectedType);
            String sourceCode = null;
            String testSourceCode = null;
            if (contractFromTemplate != null) {
                sourceCode = String.format(contractFromTemplate, curClassName, curClassName);
            }
            String contractFromTemplate1 = getContractFromTemplate(DEFAULT_TEST);
            if (contractFromTemplate1 != null) {
                testSourceCode = String.format(contractFromTemplate1, curClassName, curClassName);
            }
            DeploySmartListItem item = getCurrentListItem();
            item.sourceCode = sourceCode;
            item.testSourceCode = testSourceCode;
            item.name = DeployControllerUtils.checkContractNameExist(curClassName, deployContractList.getItems());
            saveTypeOfContract(item);
            deployContractList.refresh();
            session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
            initNewContractForm();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            DeploySmartListItem currentListItem = getCurrentListItem();
            currentListItem.sourceCode = DEFAULT_SOURCE_CODE;
            saveTypeOfContract(currentListItem);
        }
    }

    private void initNewContractForm() {
        className.clear();
        ObservableList<String> items = cbContractType.getItems();
        items.clear();
        items.add(DEFAULT_STANDARD_CLASS);
        items.add(BASIC_STANDARD_CLASS);
        items.add(EXTENSION_STANDARD_CLASS);
        cbContractType.getSelectionModel().select(0);
    }

    private void initCodeArea() {
        codeArea = CodeAreaUtils.initCodeArea(paneCode, false);
        treeViewsController.refreshTreeView(codeArea);
        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, (evt) -> {
            treeViewsController.refreshTreeView(codeArea);
            parentController.cleanCompilationPackage(false);
        });
    }

    private void initSplitPane() {
        for (SplitPane.Divider d : splitPane.getDividers()) {
            d.positionProperty()
                .addListener((observable, oldValue, newValue) -> errorTableView.setPrefHeight(debugPane.getHeight()));
        }
    }

    private void initDeployContractList() {
        ArrayList<DeploySmartListItem> deploySmartListItems =
            session.deployContractsKeeper.getKeptObject().orElseGet(ArrayList::new);


        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN);

        deployContractList.setOnKeyPressed(event -> {
            if (keyCombinationShiftC.match(event)) {
                deployContractList.getItems().remove(getCurrentListItem());
                session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
                if (deployContractList.getItems().size() == 0) {
                    handleAddContract();
                }
            }
        });

        deployContractList.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                Platform.runLater(() -> {
                    contextMenu.getItems().clear();
                    contextMenu.hide();
                    MenuItem removeItem = new MenuItem("Delete");
                    contextMenu.getItems().add(removeItem);

                    removeItem.setOnAction(event1 -> {
                        deployContractList.getItems().remove(getCurrentListItem());
                        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
                        if (deployContractList.getItems().size() == 0) {
                            handleAddContract();
                        }
                    });
                    contextMenu.show(deployContractList, event.getScreenX(), event.getScreenY());
                });
            }
        });

        deployContractList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            session.lastSmartIndex = deployContractList.getSelectionModel().getSelectedIndex();
            if (oldValue != null) {
                saveCodeFromTextArea(oldValue);
            }
            if (newValue.state == DeploySmartListItem.ItemState.NEW) {
                deleteMainTabs();
            } else {
                DeploySmartListItem item = getCurrentListItem();
                returnMainTabs(item);
                treeViewsController.refreshTreeView(codeArea);
            }
        });

        if (deploySmartListItems.isEmpty()) {
            DeploySmartListItem deploySmartItem = new DeploySmartListItem(null, null,
                DeployControllerUtils.checkContractNameExist("Contract", deployContractList.getItems()),
                DeploySmartListItem.ItemState.NEW);
            deployContractList.getItems().add(deploySmartItem);
            deployContractList.getSelectionModel().selectFirst();
            session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        } else {
            deployContractList.getItems().addAll(deploySmartListItems);
            deployContractList.getSelectionModel().select(session.lastSmartIndex);
        }

        if (getCurrentListItem().state.equals(DeploySmartListItem.ItemState.NEW)) {
            deleteMainTabs();
        } else {
            returnMainTabs(getCurrentListItem());
        }
        tabPane.getSelectionModel().select(0);
    }

    private void saveCodeFromTextArea(DeploySmartListItem item) {
        if (codeAreaTab.isSelected()) {
            item.sourceCode = codeArea.getText();
        } else if (testingTab.isSelected()) {
            item.testSourceCode = codeArea.getText();
        }
    }

    private void initErrorTableView() {
        TableColumn<BuildSourceCodeError, String> tabErrorsColLine = new TableColumn<>();
        tabErrorsColLine.setText("Line");
        tabErrorsColLine.setCellValueFactory(new PropertyValueFactory<>("line"));
        tabErrorsColLine.setPrefWidth(debugPane.getPrefWidth() * 0.1);

        TableColumn<BuildSourceCodeError, String> tabErrorsColText = new TableColumn<>();
        tabErrorsColText.setText("Error");
        tabErrorsColText.setCellValueFactory(new PropertyValueFactory<>("text"));
        tabErrorsColText.setPrefWidth(debugPane.getPrefWidth() * 0.88);

        errorTableView.setVisible(false);
        errorTableView.setPrefHeight(debugPane.getPrefHeight());
        errorTableView.setPrefWidth(debugPane.getPrefWidth());

        errorTableView.getColumns().add(tabErrorsColLine);
        errorTableView.getColumns().add(tabErrorsColText);

        errorTableView.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() || event.getButton() == MouseButton.PRIMARY) {
                BuildSourceCodeError tabRow = errorTableView.getSelectionModel().getSelectedItem();
                try {
                    codeArea.setCaretPositionOnLine(tabRow.getLine());
                } catch (Exception ignored) {
                }
            }
        });
    }

    private void saveTypeOfContract(DeploySmartListItem item) {
        item.state = DeploySmartListItem.ItemState.SAVED;
        returnMainTabs(item);
    }

    private void deleteMainTabs() {
        parentController.cleanCompilationPackage(true);
        tabPane.getTabs().removeAll(createCodeTab, codeAreaTab, testingTab);
        tabPane.getTabs().add(createCodeTab);
        tabPane.getSelectionModel().select(0);
    }

    private void returnMainTabs(DeploySmartListItem item) {
        parentController.cleanCompilationPackage(false);
        tabPane.getTabs().removeAll(createCodeTab, codeAreaTab, testingTab);
        codeAreaTab.setText(item.name);
        tabPane.getTabs().add(codeAreaTab);
        tabPane.getTabs().add(testingTab);
        tabPane.getSelectionModel().select(0);
        codeArea.replaceText(item.sourceCode);
        treeViewsController.refreshTreeView(codeArea);
    }


    private DeploySmartListItem getCurrentListItem() {
        return deployContractList.getSelectionModel().getSelectedItem();
    }

    @Override
    public void formDeinitialize() {
        DeploySmartListItem item = getCurrentListItem();
        saveCodeFromTextArea(item);
        session.deployContractsKeeper.keepObject(new ArrayList<>(deployContractList.getItems()));
        codeArea.cleanAll();
    }

    @Override
    public void initializeForm(Map<String, Object> objects) {
        initCodeArea();
        initNewContractForm();
        initDeployContractList();
        initSplitPane();
        initErrorTableView();
    }

    @FXML
    private void updateSelectedTab() {
        if (codeAreaTab != null && codeAreaTab.isSelected()) {
            Platform.runLater(() -> {
                testingTab.setContent(null);
                codeAreaTab.setContent(tabContent);
                DeploySmartListItem currentListItem = getCurrentListItem();
                if (currentListItem.sourceCode == null) {
                    currentListItem.sourceCode = "";
                }
                if(!codeArea.getText().isEmpty()) {
                    currentListItem.testSourceCode = codeArea.getText();
                }
                codeArea.replaceText(currentListItem.sourceCode);
                treeViewsController.refreshTreeView(codeArea);
            });
        } else if (testingTab != null && testingTab.isSelected()) {
            Platform.runLater(() -> {
                codeAreaTab.setContent(null);
                testingTab.setContent(tabContent);
                DeploySmartListItem currentListItem = getCurrentListItem();
                if (currentListItem.testSourceCode == null) {
                    currentListItem.testSourceCode = "";
                }
                if(!codeArea.getText().isEmpty()) {
                    currentListItem.sourceCode = codeArea.getText();
                }
                codeArea.replaceText(currentListItem.testSourceCode);
                treeViewsController.refreshTreeView(codeArea);
            });
            ;

        }
    }
}
