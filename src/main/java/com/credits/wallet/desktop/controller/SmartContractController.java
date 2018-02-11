package com.credits.wallet.desktop.controller;

import com.credits.wallet.desktop.App;
import com.credits.wallet.desktop.AppState;
import com.credits.wallet.desktop.Utils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.util.*;

/**
 * Created by goncharov-eg on 30.01.2018.
 */
public class SmartContractController extends Controller {
    @FXML
    private javafx.scene.control.TextArea taCode;

    @FXML
    private void handleBack() {
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
            String javaCode=taCode.getText().replace("\r"," ").replace("\n", " ").replace("{", " {");
            while (javaCode.indexOf("  ")>=0)
                javaCode=javaCode.replace("  "," ");
            java.util.List<String> javaCodeWords= Arrays.asList(javaCode.split(" "));
            int ind=javaCodeWords.indexOf("class");
            if (ind>=0 && ind<javaCodeWords.size()-1)
                className=javaCodeWords.get(ind+1);
            // ---------------
            try {
                String tmpDir = System.getProperty("java.io.tmpdir");
                String tmpFileName=tmpDir+File.separator+className+".java";
                File tmpFile=new File(tmpFileName);
                FileOutputStream out = new FileOutputStream(tmpFile);
                out.write(taCode.getText().getBytes());
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
                    Utils.showError(jelement.getAsJsonObject().get("message").getAsString());
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
}