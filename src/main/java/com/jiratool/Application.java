package com.jiratool;

import com.google.common.io.Files;
import com.jiratool.command.Command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        if (args.length < 0) {
            throw new IllegalArgumentException();
        }

        List<String> commands = null;
        try {
            commands = Files.readLines(new File(args[0]), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (commands == null) {
            throw new IllegalArgumentException("Commands file initialization problem");
        }

        List<Command> parsedCommands = initCommands(commands);
    }

    private static List<Command> initCommands(List<String> commands) {
        return null;
    }
}
