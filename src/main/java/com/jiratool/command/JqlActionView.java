package com.jiratool.command;


import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.jiratool.util.CommonUtils;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class JqlActionView implements Command {

    private static final HashMap<String, Object> VIEW_CASH = new HashMap<>();

    private static final int ACTION_ITEM_ID_LENGTH = 3;
    private static final int DATE_LENGTH = 10;
    private static final int ISSUE_KEY_LENGTH = 10;
    private static final int ISSUE_SUMMERY_LENGTH = 50;
    private static final int TIME_SPEND_LENGTH = 10;
    private static final int TIME_SPEND_PER_DAY_LENGTH = 16;

    private static final String ACTION_ITEM_ID_HEADER = "id";
    private static final String DATE_HEADER = "Date";
    private static final String ISSUE_KEY_HEADER = "Issue key";
    private static final String ISSUE_SUMMERY_HEADER = "Summery";
    private static final String TIME_SPEND_HEADER = "Time spend";
    private static final String TIME_SPEND_PER_DAY_HEADER = "Time spend / day";

    private String tableLineSeparator;
    private String tableLineFormat;
    private final List<String> TABLE = new ArrayList<>();

    private AtomicInteger actionItemId = new AtomicInteger(0);

    private String jiraQuery;

    public JqlActionView(String data) {
        this.jiraQuery = data;
        this.tableLineSeparator = CommonUtils.append("+-", "-", ACTION_ITEM_ID_LENGTH)
                .concat(CommonUtils.append("-+-", "-", DATE_LENGTH))
                .concat(CommonUtils.append("-+-", "-", ISSUE_KEY_LENGTH))
                .concat(CommonUtils.append("-+-", "-", ISSUE_SUMMERY_LENGTH))
                .concat(CommonUtils.append("-+-", "-", TIME_SPEND_LENGTH))
                .concat(CommonUtils.append("-+-", "-", TIME_SPEND_PER_DAY_LENGTH))
                .concat("-+%n");
        this.tableLineFormat =                "| %-"
                + ACTION_ITEM_ID_LENGTH +   "s | %-"
                + DATE_LENGTH +             "s | %-"
                + TIME_SPEND_LENGTH +       "s | %-"
                + ISSUE_SUMMERY_LENGTH +    "s | %-"
                + TIME_SPEND_LENGTH +       "s | %-"
                + TIME_SPEND_PER_DAY_LENGTH +    "s |%n";
    }

    public void execute() {
        JiraRestClient client = (JiraRestClient) Context.getInstance().get("client");
        SearchResult res = client.getSearchClient().searchJql(jiraQuery,
                10, 0, new HashSet<String>(){{add("*all");}}).claim();
        Iterable<Issue> issues = res.getIssues();
        fillTable(issues);
    }

    private void fillTable(Iterable<Issue> issues) {
        System.out.printf(tableLineSeparator);
        System.out.printf(tableLineFormat,
                ACTION_ITEM_ID_HEADER, DATE_HEADER, ISSUE_KEY_HEADER, ISSUE_SUMMERY_HEADER, TIME_SPEND_HEADER, TIME_SPEND_PER_DAY_HEADER);
        System.out.printf(tableLineSeparator);

        String myName = (String) Context.getInstance().get("name");
        List<Issue> issueList = new ArrayList<>();
        issues.iterator().forEachRemaining(issueList::add);

        issueList.stream()
                .flatMap(issue -> CommonUtils.copy(issue.getWorklogs()).stream())
                .filter(worklog -> Objects.equals(worklog.getAuthor().getName(), myName))
                .sorted(Comparator.comparing(Worklog::getStartDate, DateTime::compareTo))
                .forEach(worklog -> this.fillTable(issues, worklog));

        TABLE.forEach(System.out::print);
        System.out.printf(tableLineSeparator);
    }



    private void fillTable(Iterable<Issue> issues, Worklog worklog) {
        Issue issue = findIssueByWorklog(issues, worklog);

        List<String> actionItemId = CommonUtils.wrapToList(String.valueOf(this.actionItemId.addAndGet(1)), ACTION_ITEM_ID_LENGTH);
        List<String> printableDate = CommonUtils.wrapToList(convertDateTimeToPrintable(worklog.getStartDate()), DATE_LENGTH);
        List<String> issueKey = CommonUtils.wrapToList(issue.getKey(), ISSUE_KEY_LENGTH);
        List<String> summery = CommonUtils.wrapToList(issue.getSummary(), ISSUE_SUMMERY_LENGTH);
        List<String> timeSpent = CommonUtils.wrapToList(CommonUtils.minutesToPrintableTime(worklog.getMinutesSpent()), TIME_SPEND_LENGTH);

        int linesToAllocate = IntStream.of(actionItemId.size(), printableDate.size(), issueKey.size(), summery.size(), timeSpent.size()).max().orElse(0);
        for (int i = 0; i< linesToAllocate; i++) {
            TABLE.add(String.format(tableLineFormat,
                    CommonUtils.getOrReturn(actionItemId, i, ""),
                    CommonUtils.getOrReturn(printableDate, i, ""),
                    CommonUtils.getOrReturn(issueKey, i, ""),
                    CommonUtils.getOrReturn(summery, i, ""),
                    CommonUtils.getOrReturn(timeSpent, i, ""),
                    ""));
        }
    }

    private Issue findIssueByWorklog(Iterable<Issue> issues, Worklog worklog) {
        for (Issue issue : issues) {
            if (Objects.equals(issue.getSelf(), worklog.getIssueUri()))
                return issue;
        }
        return null;
    }

    private String convertDateTimeToPrintable(DateTime dateTime) {
        String dayOfWeek = dateTime.dayOfWeek().getAsShortText();
        String dayOfMonth = dateTime.dayOfMonth().getAsShortText();
        String month = dateTime.monthOfYear().getAsShortText();
        return String.format("%s %s %s", dayOfWeek, dayOfMonth, month);
    }

//    private static class ColumnDescription {
//        public Supplier<Integer> dataLength;
//        public Supplier<String> headerName;
//
//        public ColumnDescription(Supplier<Integer> dataLength, Supplier<String> headerName) {
//            this.dataLength = dataLength;
//            this.headerName = headerName;
//        }
//
//        public Integer getDataLength() {
//            return dataLength.get();
//        }
//
//        public String getHeaderName() {
//            return headerName.get();
//        }
//    }
}
