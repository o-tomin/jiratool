package com.jiratool;

import com.jiratool.command.Command;
import com.jiratool.command.LogIn;
import com.jiratool.command.LogTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Application {

    private static final String PROPERTIES_FILE = "global.properties";

    public static void main(String[] args) {
        System.out.println("[INFO] Jira Tool v.0.1 started.");
        validateInput(args);
        System.out.println("[INFO] Argument(-s) are valid.");
        loadProperties();
        System.out.println("[INFO] Global properties loaded successfully.");
        List<String> commandList = new ArrayList<>();
        loadCommandFile(commandList, args[0]);
        System.out.println("[INFO] Commands loaded successfully.");
        List<Command> commandObjectList = new ArrayList<>();
        initCommands(commandObjectList, commandList);
        System.out.println("[INFO] Commands initialized successfully.");
        Thread commandsExecutorThread = new Thread(() -> commandObjectList.forEach(Command::execute));
        commandsExecutorThread.setName("commandsExecutorThread");
        System.out.println("[INFO] Commands will be executed now!!!");
        commandsExecutorThread.start();

    }

    private static void validateInput(String[] args) {
        if (args.length == 0) {
            printHintAndExit();
        }
    }

    private static void printHintAndExit() {
        System.err.println("[ERROR] Path to commands file was expected.");
        System.err.println("[INFO] Closing application...");
        System.exit(-1);
    }

    private static void loadProperties() {
        try {
            InputStream is = new Object().getClass().getResourceAsStream(PROPERTIES_FILE);
            if (is == null) {
                is = new FileInputStream(new File("C:\\Users\\oleks_000\\IdeaProjects\\JiraTool\\src\\main\\resources\\global.properties"));
            }
            System.getProperties().load(is);
        } catch (IOException e) {
            onFatalError(e);
        }
    }

    private static void loadCommandFile(List<String> toFill, String path) {
        if (toFill == null) {
            toFill = new ArrayList<>();
        }
        try {
            if( !toFill.addAll(Files.readAllLines(Paths.get(path), Charset.defaultCharset()))) {
                System.err.println("[ERROR] Can't load commands file");
                throw new IOException("Can't load commands file");
            }
        } catch (IOException e) {
            onFatalError(e);
        }
    }

    private static void initCommands(List<Command> toFill, List<String> commands) {
        if (toFill == null) {
            toFill = new ArrayList<>();
        }
        for (int i = 0; i < commands.size(); i++) {
            String[] line = commands.get(i).split("\\$");
            toFill.add(initCommand(line[0].trim(), line[1].trim(), i + 1));
        }
    }

    private static Command initCommand(String command, String data, int line) {
        switch (command.toLowerCase()) {
            case "login" : return new LogIn(data);
            case "logtime" : return new LogTime(data);
            default: return () ->
                System.out.println(
                        String.format("[WARN] Unknown command executed: {line=%d;command=%s;data=%s}", line, command, data));
        }
    }

    private static <T extends Throwable> void onFatalError(T fatalError) {
        System.err.println("[FATAL] Cant load default properties");
        fatalError.printStackTrace();
        System.err.println("[INFO] Closing application...");
        System.exit(-1);
    }
}
