package com.credits.wallet.desktop;

import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by goncharov-eg on 26.01.2018.
 */
public class Utils {
    public static void showError(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.UTILITY);
        alert.setTitle("Error");
        alert.setHeaderText("Error!");
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static String callAPI(String addrSuffix, String errorText) {
        try {
            HttpClient client = new DefaultHttpClient();

            HttpGet get = new HttpGet(AppState.apiAddr+addrSuffix);
            HttpResponse response = client.execute(get);

            InputStream is = response.getEntity().getContent();
            final int bufferSize = 1024;
            final char[] buffer = new char[bufferSize];
            final StringBuilder out = new StringBuilder();
            Reader in = new InputStreamReader(is, "UTF-8");
            int rsz = in.read(buffer, 0, buffer.length);
            while (rsz>=0) {
                out.append(buffer, 0, rsz);
                rsz = in.read(buffer, 0, buffer.length);
            }
            return out.toString();

        } catch (Exception e) {
            Utils.showError(errorText + " " + e.toString());
            return null;
        }
    }
}
