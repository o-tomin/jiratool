package com.jiratool.command;


import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.jiratool.data.SmartItem;
import com.jiratool.util.CommonUtils;
import org.joda.time.DateTime;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JqlActionView implements Command {

    private static final int DATE_KEY = 1;
    private static final int ISSUE_KEY_KEY = 2;
    private static final int ISSUE_SUMMERY_KEY = 3;
    private static final int TIME_SPEND_KEY = 4;
    private static final int TIME_SPEND_PER_DAY_KEY = 5;

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
    private final List<SmartItem> ACTION_ITEMS = new ArrayList<>();

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

        ACTION_ITEMS.addAll(issueList.stream()
                .flatMap(issue -> CommonUtils.copy(issue.getWorklogs()).stream())
                .filter(worklog -> Objects.equals(worklog.getAuthor().getName(), myName))
                .sorted(Comparator.comparing(Worklog::getStartDate, DateTime::compareTo))
                .map(worklog -> toActionItem(issues, worklog))
                .collect(Collectors.toList()));

//        ACTION_ITEMS.stream().
//                collect(Collectors.groupingBy(CommonUtils::classifier))


        ACTION_ITEMS.forEach(this::printActionItems);
        System.out.printf(tableLineSeparator);
    }

    private SmartItem toActionItem(Iterable<Issue> issues, Worklog worklog) {
        Issue issue = findIssueByWorklog(issues, worklog);
        SmartItem smartItem = new SmartItem(this.actionItemId.addAndGet(1));
        smartItem.add(DATE_KEY, worklog.getStartDate());
        smartItem.add(ISSUE_KEY_KEY, issue.getKey());
        smartItem.add(ISSUE_SUMMERY_KEY, issue.getSummary());
        smartItem.add(TIME_SPEND_KEY, worklog.getMinutesSpent());
        smartItem.add(TIME_SPEND_PER_DAY_KEY, null);
        return smartItem;
    }

    private void printActionItems(SmartItem smartItem) {
        List<String> actionItemId = CommonUtils.wrapToList(String.valueOf(smartItem.getId()), ACTION_ITEM_ID_LENGTH);
        List<String> printableDate = CommonUtils.wrapToList(convertDateTimeToPrintable((DateTime) smartItem.get(DATE_KEY)), DATE_LENGTH);
        List<String> issueKey = CommonUtils.wrapToList((String) smartItem.get(ISSUE_KEY_KEY), ISSUE_KEY_LENGTH);
        List<String> summery = CommonUtils.wrapToList((String) smartItem.get(ISSUE_SUMMERY_KEY), ISSUE_SUMMERY_LENGTH);
        List<String> timeSpent = CommonUtils.wrapToList(CommonUtils.minutesToPrintableTime((Integer) smartItem.get(TIME_SPEND_KEY)), TIME_SPEND_LENGTH);
        List<String> timeSpentPerDay = Collections.emptyList();
        if (smartItem.contains(TIME_SPEND_PER_DAY_KEY))
            timeSpentPerDay = CommonUtils.wrapToList(CommonUtils.minutesToPrintableTime((Integer) smartItem.get(TIME_SPEND_PER_DAY_KEY)), TIME_SPEND_PER_DAY_LENGTH);

        int linesToAllocate = IntStream.of(actionItemId.size(), printableDate.size(), issueKey.size(), summery.size(), timeSpent.size(), timeSpentPerDay.size()).max().orElse(0);
        for (int i = 0; i< linesToAllocate; i++) {
            System.out.printf(tableLineFormat,
                    CommonUtils.getOrReturn(actionItemId, i, ""),
                    CommonUtils.getOrReturn(printableDate, i, ""),
                    CommonUtils.getOrReturn(issueKey, i, ""),
                    CommonUtils.getOrReturn(summery, i, ""),
                    CommonUtils.getOrReturn(timeSpent, i, ""),
                    CommonUtils.getOrReturn(timeSpentPerDay, i, ""));
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
