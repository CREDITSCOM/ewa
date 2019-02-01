package com.credits.wallet.desktop.controller;

import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeViewController extends AbstractController {

    private static Logger LOGGER = LoggerFactory.getLogger(TreeViewController.class);

    @FXML
    public TreeView<Label> treeView;

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
            bodyDeclarations.addAll(build.constructors);
            bodyDeclarations.addAll(build.methods);

            String className = GeneralSourceCodeUtils.parseClassName(sourceCode);
            Label labelRoot = new Label(className);
            TreeItem<Label> treeRoot = new TreeItem<>(labelRoot);


            bodyDeclarations.forEach(classMember -> {
                Label label = new Label(classMember.toString());
                TreeItem<Label> treeItem = new TreeItem<>();
                treeItem.setValue(label);
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
