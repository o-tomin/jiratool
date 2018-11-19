package com.jiratool.command;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.util.regex.Pattern;

public class LogIn implements Command {

    private String name;
    private String password;

    public LogIn(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        final boolean[] name = {false};
        final boolean[] password = {false};
        Pattern.compile(";")
                .splitAsStream(data)
                .flatMap( arg -> Pattern.compile("(?<==)").splitAsStream(arg))
                .map(String::trim)
                .forEach(arg -> {

                    if(arg.startsWith("requestInput")){

                    }

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

    @Override
    public synchronized void execute() {
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = URI.create(System.getProperty("jira.url", ""));
        JiraRestClient client = factory.createWithBasicHttpAuthentication(uri, this.name, this.password);
        Context.getInstance().add("client", client);
        Context.getInstance().add("name", this.name);
    }

}
