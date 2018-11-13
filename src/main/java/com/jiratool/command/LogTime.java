package com.jiratool.command;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.util.concurrent.Promise;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LogTime implements Command{

    public LogTime(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        throw new NotImplementedException();
    }

    @Override
    public synchronized void execute() {
        JiraRestClient client = (JiraRestClient) Context.getInstance().get("client");
        Promise<Issue> issuePromise = client.getIssueClient().getIssue("MCMR-16448");
        Issue issue = issuePromise.claim();
        // Worklog worklog = new Worklog()
    }
}
