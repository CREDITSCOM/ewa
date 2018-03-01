package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.*;
import com.google.gson.*;
import javafx.concurrent.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.*;
import org.apache.http.impl.client.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.fxmisc.richtext.*;
import org.fxmisc.richtext.model.*;
import org.slf4j.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.*;

import static java.util.Arrays.*;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller implements Initializable {

    private static Logger LOGGER = LoggerFactory.getLogger(SmartContractController.class);

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private CodeArea codeArea;


    @FXML
    private Pane paneCode;

    @FXML
    private TreeView<Label> classTreeView;

    @FXML
    private Button checkButton;

    //@FXML
    //private javafx.scene.control.TextArea taCode;

    @FXML
    private void handleBack() {
        if (AppState.executor!=null) {
            AppState.executor.shutdown();
            AppState.executor=null;
        }
        App.showForm("/fxml/form6.fxml", "Wallet");
    }

    @FXML
    private void handleDeploy() {
        char[] characters="ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb=new StringBuilder();
        sb.append("CST");
        Random random=new Random();
        int max=characters.length-1;
        for (int i=0; i<29; i++)
            sb.append(characters[random.nextInt(max)]);

        String token=sb.toString();

        StringSelection selection = new StringSelection(token);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        // Call contract executor
        if (AppState.contractExecutorJava!=null) {
            // Parse className
            String className="SmartContract";

            String javaCode=codeArea.getText().replace("\r"," ").replace("\n", " ").replace("{", " {");

            while (javaCode.indexOf("  ")>=0)
                javaCode=javaCode.replace("  "," ");
            java.util.List<String> javaCodeWords= asList(javaCode.split(" "));
            int ind=javaCodeWords.indexOf("class");
            if (ind>=0 && ind<javaCodeWords.size()-1)
                className=javaCodeWords.get(ind+1);
            // ---------------
            try {
                String tmpDir = System.getProperty("java.io.tmpdir");
                String tmpFileName=tmpDir+File.separator+className+".java";
                File tmpFile=new File(tmpFileName);
                FileOutputStream out = new FileOutputStream(tmpFile);
                out.write(codeArea.getText().getBytes());
                out.close();

                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(AppState.contractExecutorJava);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                builder.addTextBody("address", AppState.account);
                builder.addBinaryBody("java", tmpFile,
                        ContentType.APPLICATION_OCTET_STREAM, className+".java");

                HttpEntity multipart = builder.build();
                httpPost.setEntity(multipart);

                CloseableHttpResponse response = client.execute(httpPost);

                if (response.getStatusLine().getStatusCode()!=200) {
                    // Show error
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent()));

                    String inputLine;
                    StringBuffer sbResponse = new StringBuffer();

                    while ((inputLine = reader.readLine()) != null) {
                        sbResponse.append(inputLine);
                    }
                    reader.close();

                    JsonElement jelement = new JsonParser().parse(sbResponse.toString());
                    String msgStr=jelement.getAsJsonObject().get("message").getAsString();
                    String errorStr=jelement.getAsJsonObject().get("error").getAsString();
                    String excStr=jelement.getAsJsonObject().get("exception").getAsString();

                    String errorMsg="";
                    if (msgStr!=null)
                        errorMsg=msgStr;
                    if (errorStr!=null)
                        errorMsg=errorMsg.trim()+" "+errorStr;
                    if (excStr!=null)
                        errorMsg=errorMsg.trim()+" "+excStr;

                    Utils.showError(errorMsg);
                }

                client.close();

                tmpFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
                Utils.showError("Error executing smart contract "+e.toString());
            }
        }
        // ----------------------

        Utils.showInfo("Token\n\n"+token+"\n\nhas generated and copied to clipboard");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (AppState.executor!=null)
            AppState.executor.shutdown();
        AppState.executor = Executors.newSingleThreadExecutor();
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges()
                .filter(ch -> !ch.getInserted().equals(ch.getRemoved())) // XXX
                .successionEnds(Duration.ofMillis(500))
                .supplyTask(this::computeHighlightingAsync)
                .awaitLatest(codeArea.richChanges())
                .filterMap(t -> {
                    if(t.isSuccess()) {
                        return Optional.of(t.get());
                    } else {
                        t.getFailure().printStackTrace();
                        return Optional.empty();
                    }
                })
                .subscribe(this::applyHighlighting);

        codeArea.setPrefHeight(paneCode.getPrefHeight());
        codeArea.setPrefWidth(paneCode.getPrefWidth());
        paneCode.getChildren().add(codeArea);
    }

    @FXML
    private void panelCodeKeyReleased() {
        refreshClassMembersTree();
    }

    private void refreshClassMembersTree() {
        String code = codeArea.getText();

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(code.toCharArray());
        parser.setResolveBindings(true);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        List typeList = cu.types();

        if (typeList.size() != 1) {
            return;
        }

        TypeDeclaration typeDeclaration = (TypeDeclaration)typeList.get(0);

        String className = (typeDeclaration).getName().getFullyQualifiedName();

        Label labelRoot = new Label(className);

        TreeItem<Label> treeRoot = new TreeItem<Label>(labelRoot);

        ASTNode root = cu.getRoot();

        root.accept(new ASTVisitor() {

            @Override
            public boolean visit(FieldDeclaration node) {
                return true;
            }

            @Override
            public void endVisit(FieldDeclaration node) {
                Label label = new Label(node.toString());

                label.setOnMouseClicked(event -> {
                    // TODO перемещение курсора в codeArea к строке начала члена класса
                    LOGGER.info("click!");
                });

                TreeItem<Label> treeItem = new TreeItem();
                treeItem.setValue(label);

                treeRoot.getChildren().add(treeItem);
            }

        });

        root.accept(new ASTVisitor() {

            @Override
            public boolean visit(MethodDeclaration node) {
                return true;
            }

            @Override
            public void endVisit(MethodDeclaration node) {
                node.setBody(null);
                Label label = new Label(node.toString());
//                return str.substring(0, str.indexOf(';'));

                label.setOnMouseClicked(event -> {
                    // TODO перемещение курсора в codeArea к строке начала члена класса
                    LOGGER.info("click!");
                });

                TreeItem<Label> treeItem = new TreeItem();
                treeItem.setValue(label);

                treeRoot.getChildren().add(treeItem);
            }

        });

        treeRoot.setExpanded(true);
        this.classTreeView.setRoot(treeRoot);
    }

    @FXML
    private void checkButtonAction() {
        String code = codeArea.getText();

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(code.toCharArray());
        parser.setResolveBindings(true);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        IProblem[] problemArr = cu.getProblems();

        if (problemArr.length > 0) {
            LOGGER.info("problems!!!");
        }
    }


    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PAREN") != null ? "paren" :
                                    matcher.group("BRACE") != null ? "brace" :
                                            matcher.group("BRACKET") != null ? "bracket" :
                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                            matcher.group("STRING") != null ? "string" :
                                                                    matcher.group("COMMENT") != null ? "comment" :
                                                                            null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String text = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
            @Override
            protected StyleSpans<Collection<String>> call() throws Exception {
                return computeHighlighting(text);
            }
        };
        AppState.executor.execute(task);
        return task;
    }
}