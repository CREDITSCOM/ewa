package com.credits.wallet.desktop.testUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WalletTestUtils {
    public static String readSourceCode(String resourcePath) throws IOException {
        String sourceCodePath = String.format("%s/src/test/resources/contracts/%s", Paths.get("").toAbsolutePath(), resourcePath);
        return new String(Files.readAllBytes(Paths.get(sourceCodePath)));
    }

}
