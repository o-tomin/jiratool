package com.jiratool.command;


import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Worklog;

public class JqlActionView implements Command {

    private String jiraQuery;

    public JqlActionView(String data) {
        this.jiraQuery = data;
    }

    public void execute() {
        JiraRestClient client = (JiraRestClient) Context.getInstance().get("client");
        SearchResult res = client.getSearchClient().searchJql(jiraQuery).claim();
        Iterable<Issue> issues = res.getIssues();
        printToConsole(issues);
    }

    private void printToConsole(Iterable<Issue> issues) {
        String format = "| %-2d | %-8s | %-10s | %-40s | %-10s |";
        System.out.printf("+----+----------+------------+------------------------------------------+------------+");
        System.out.printf("| id | Date     | Issue id   | Issue Summery                            | Time spend |");
        System.out.printf("+----+----------+------------+------------------------------------------+------------+");
        int i = 1;
        for (Issue issue : issues) {
            Iterable<Worklog> worklogs = issue.getWorklogs();
        }
    }


}
