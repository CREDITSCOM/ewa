package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.struct.MethodSimpleDeclaration;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeViewController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(TreeViewController.class);

    @FXML
    public TreeView<String> treeView;
    @FXML
    public DeployTabController parentController;

    public void refreshTreeView(CreditsCodeArea codeArea) {
        Thread t = new Thread(() -> refreshClassMembersTree(codeArea));
        t.setDaemon(true);
        t.start();
    }

    public synchronized void refreshClassMembersTree(CreditsCodeArea codeArea) {
        Platform.runLater(() -> {
            treeView.setRoot(null);
            String sourceCode = codeArea.getText();
            ParseResultStruct build =
                new ParseResultStruct.Builder(sourceCode).fields().constructors().methods().build();

            List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
            bodyDeclarations.addAll(build.fields);
            bodyDeclarations.addAll(build.constructors.stream().map(MethodSimpleDeclaration::getMethodDeclaration).collect(Collectors.toList()));
            bodyDeclarations.addAll(build.methods.stream().map(MethodSimpleDeclaration::getMethodDeclaration).collect(Collectors.toList()));

            //String className = GeneralSourceCodeUtils.parseClassName(sourceCode);

            TreeItem<String> treeRoot = new TreeItem<>();


            bodyDeclarations.forEach(classMember -> {
                TreeItem<String> treeItem = null;
                if (classMember instanceof MethodDeclaration && !((MethodDeclaration) classMember).isConstructor()) {
                    boolean isTest = false;
                    for (Object modifier : classMember.modifiers()) {
                        if (modifier instanceof MarkerAnnotation) {
                            if (modifier.toString().equals("@Test")) {
                                isTest = true;
                                break;
                            }
                        }
                    }
                    if (isTest) {
                        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/img/play2.png")));
                        Button button = new Button();
                        button.setMaxSize(20, 20);
                        button.setCursor(Cursor.HAND);
                        button.setPickOnBounds(false);
                        button.setText(((MethodDeclaration) classMember).getName().toString());
                        button.setStyle("-fx-text-fill:transparent;-fx-background-color: transparent");
                        button.setOnMouseClicked(event -> {
                            parentController.doTestMethod(((Button) event.getTarget()).getText());
                        });
                        button.setGraphic(icon);
                        treeItem = new TreeItem<>(classMember.toString(), button);
                    }
                }
                if (treeItem == null) {
                    treeItem = new TreeItem<>(classMember.toString());
                }
                treeItem.setValue(treeItem.getValue().replaceAll("(@\\w*)|((^)? ?public )", ""));
                treeRoot.getChildren().add(treeItem);
            });


            treeRoot.setExpanded(true);
            treeView.setRoot(treeRoot);
            treeView.setShowRoot(false);

            treeView.setOnMouseClicked(event -> {
                if (event.isPrimaryButtonDown() || event.getButton() == MouseButton.PRIMARY) {
                    Integer index = treeView.getSelectionModel().getSelectedIndices().get(0);
                    if (index >= 0) {
                        BodyDeclaration selected =
                            bodyDeclarations.get(treeView.getSelectionModel().getSelectedIndices().get(0));
                        try {
                            int lineNumber = ParseCodeUtils.getLineNumber(sourceCode, selected);
                            codeArea.setCaretPositionOnLine(lineNumber);
                        } catch (Exception ignored) {
                        }
                    }
                }
            });

        });
    }


    @Override
    public void formDeinitialize() {

    }

    @Override
    public void initializeForm(Map<String, Object> objects) {

    }
}
