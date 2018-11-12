package com.jiratool.command;

import java.util.regex.Pattern;

public class LogIn implements Command {

    private  String name;
    private  String password;

    private LogIn(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        final boolean[] name = {false};
        final boolean[] password = {false};
        Pattern.compile(";")
                .splitAsStream(data)
                .flatMap( arg -> Pattern.compile("(?<==)").splitAsStream(arg))
                .forEach(arg -> {

                    if (name[0]) {
                        this.name = arg;
                        name[0] = false;
                    }
                    else if(password[0]) {
                        this.password = arg;
                        password[0] = false;
                    }
                    switch (arg.toLowerCase()) {
                        case "name=": name[0] = true;
                        case "password=": password[0] = true;
                    }
                });
    }
}
