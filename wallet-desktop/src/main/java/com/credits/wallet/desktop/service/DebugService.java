package com.credits.wallet.desktop.service;

import com.credits.wallet.desktop.utils.FormUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class DebugService {
    private static Logger LOGGER = LoggerFactory.getLogger(DebugService.class);

    String className;
    String text;

    private Process dbgProcess;
    private OutputStream dbgStdIn;
    private InputStream dbgStdOut;
    private InputStream dbgStdErr;

    public DebugService(String className, String text) {
        this.className = className;
        this.text = text;
    }

    public String compile() {
        try {
            try (FileWriter writer = new FileWriter(className + ".java")) {
                writer.write(text);
            }

            // Compile class
            Process process = Runtime.getRuntime().exec("javac -g " + className + ".java");
            InputStream stderr = process.getErrorStream();

            StringBuilder error = new StringBuilder();
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(stderr));
            while ((line = reader.readLine()) != null) {
                error.append("+").append(line).append("\n");
            }

            if (error.length() == 0)
                return "";
            else
                return "Compilation errors:\n" + error;
        } catch (IOException e) {
            LOGGER.error("failed!", e);
            return "Error compiling smart contract " + e.toString();
        }
    }

    public String start() {
        try {
            dbgProcess = Runtime.getRuntime().exec("jdb " + className);
            dbgStdIn = dbgProcess.getOutputStream();
            dbgStdOut = dbgProcess.getInputStream();
            dbgStdErr = dbgProcess.getErrorStream();
            return "";
        } catch (IOException e) {
            LOGGER.error("failed!", e);
            return "Error starting debug "+e.toString();
        }
    }

    public void destroy() {
        dbgProcess.destroy();
    }

    public String execCmd(String cmd) {
        try {
            LOGGER.info("SC DEBUG: Executing jdb command " + cmd);

            dbgStdIn.write((cmd + "\n*\n").getBytes());
            dbgStdIn.flush();

            String result = "";
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(dbgStdOut));
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("unrecognized command: '*'"))
                    break;
                result = result + line + "\n";
            }

            LOGGER.info("SC DEBUG: RESULT^\n" + result);
            LOGGER.info("SC DEBUG: RESULTv");

            if (result.contains("Exception occurred"))
                FormUtils.showError(result);

            return result;
        } catch (IOException e) {
            LOGGER.error("failed!", e);
            return "Error executing debug commang\n" + cmd + "\n" + e.toString();
        }
    }

    public Integer cursorPosition() {
        String res = execCmd("where");
        int ind = res.indexOf(".java:");
        res = res.substring(ind + 6);
        ind = res.indexOf(")");
        res = res.substring(0, ind).trim();
        return Integer.valueOf(res);
    }
}
