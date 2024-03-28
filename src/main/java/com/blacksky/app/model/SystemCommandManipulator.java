package com.blacksky.app.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SystemCommandManipulator {
    public static String executeSystemCommand(String command) {
        StringBuilder readMessage = new StringBuilder();
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command.split(" "));
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            readMessage = new StringBuilder();
            String readLine;
            while ( (readLine=br.readLine()) != null ) {
                readMessage.append(readLine);
                readMessage.append("\n");
            }
            br.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return readMessage.toString();
    }

    public static void delayTime(int duration) {
        try {
            Thread.sleep(duration);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void createDirectory(String directoryName) {
        try {
            Runtime.getRuntime().exec("cmd /c mkdir \""+directoryName+"\"");
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}
