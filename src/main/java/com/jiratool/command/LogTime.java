package com.jiratool.command;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInputBuilder;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.regex.Pattern;

public class LogTime implements Command{

    private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");

    private String issueId;
    private DateTime startDate;
    private String comment;
    private int minutes;

    public LogTime(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        final boolean[] issueId = {false};
        final boolean[] startDate = {false};
        final boolean[] comment = {false};
        final boolean[] minutes = {false};
        Pattern.compile(";")
                .splitAsStream(data)
                .flatMap( arg -> Pattern.compile("(?<==)").splitAsStream(arg))
                .forEach(arg -> {

                    if (issueId[0]) {
                        this.issueId = arg;
                        issueId[0] = false;
                    }
                    else if(startDate[0]) {
                        this.startDate = formatter.parseDateTime(arg);
                        startDate[0] = false;
                    }
                    else if(comment[0]) {
                        this.comment = arg;
                        comment[0] = false;
                    }
                    else if(minutes[0]) {
                        this.minutes = Integer.parseInt(arg);
                        startDate[0] = false;
                    }

                    switch (arg.toLowerCase()) {
                        case "issueid=":    issueId[0] = true; break;
                        case "startdate=":   startDate[0] = true; break;
                        case "comment=":    comment[0] = true; break;
                        case "minutes=":    minutes[0] = true; break;
                    }
                });
    }

    @Override
    public synchronized void execute() {
        JiraRestClient client = (JiraRestClient) Context.getInstance().get("client");
        IssueRestClient issueClient = client.getIssueClient();
        Issue issue = client.getIssueClient().getIssue(issueId).claim();
        WorklogInput worklogInput = new WorklogInputBuilder(issue.getSelf())
                .setStartDate(startDate)
                .setComment(comment != null ? comment : "")
                .setMinutesSpent(minutes)
                .build();
        //issueClient.addWorklog(issue.getWorklogUri(), worklogInput).claim();
    }
}
