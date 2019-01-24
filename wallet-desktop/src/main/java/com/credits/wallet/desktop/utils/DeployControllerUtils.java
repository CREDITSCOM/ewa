package com.credits.wallet.desktop.utils;

import com.credits.client.node.pojo.TokenStandartData;
import com.credits.general.util.sourceCode.GeneralSourceCodeUtils;
import com.credits.wallet.desktop.struct.ParseResultStruct;
import com.credits.wallet.desktop.utils.sourcecode.ParseCodeUtils;
import com.credits.wallet.desktop.utils.sourcecode.codeArea.CreditsCodeArea;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DeployControllerUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(DeployControllerUtils.class);

    public static synchronized void refreshClassMembersTree(TreeView<Label> treeView, CreditsCodeArea codeArea) {
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
                    BodyDeclaration selected =
                        bodyDeclarations.get(treeView.getSelectionModel().getSelectedIndices().get(0));
                    try {
                        int lineNumber = ParseCodeUtils.getLineNumber(sourceCode, selected);
                        codeArea.setCaretPositionOnLine(lineNumber);
                    } catch (Exception ignored) {
                    }
                }
            });

        });
    }

    public static void refreshTreeView(TreeView<Label> treeView, CreditsCodeArea codeArea) {
        Thread t = new Thread(() -> DeployControllerUtils.refreshClassMembersTree(treeView,codeArea));
        t.setDaemon(true);
        t.start();
    }

    public static TokenStandartData getTokenStandard(Class<?> contractClass) {
        TokenStandartData tokenStandart = TokenStandartData.NotAToken;
        try {
            Class<?>[] interfaces = contractClass.getInterfaces();
            if (interfaces.length > 0) {
                Class<?> basicStandard = Class.forName("BasicStandard");
                Class<?> extendedStandard = Class.forName("ExtensionStandard");
                for (Class<?> _interface : interfaces) {
                    if (_interface.equals(basicStandard)) {
                        tokenStandart = TokenStandartData.CreditsBasic;
                    }
                    if (_interface.equals(extendedStandard)) {
                        tokenStandart = TokenStandartData.CreditsExtended;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.debug("can't find standard classes. Reason {}", e.getMessage());
        }
        return tokenStandart;
    }

    public static String getContractFromTemplate(String template) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get("src/main/resources/template/"+template+".template"));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

}
