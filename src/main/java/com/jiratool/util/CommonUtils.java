package com.jiratool.util;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CommonUtils {

    public static <K, T> ImmutableMap<K, T> iMap(Supplier<Map<K, T>> map)
    {
        ImmutableMap<K, T> immutableMap = ImmutableMap.copyOf(map.get());
        return immutableMap;
    }

    public static <T> List<T> copy(Iterable<T> iterable) {
        List<T> dest = new ArrayList<>();
        iterable.iterator().forEachRemaining(dest::add);
        return dest;
    }

    public static String minutesToPrintableTime(int minutes) {
        int hours = 0;
        int days = 0;
        int weeks = 0;

        while (minutes >= 60) {
            minutes -= 60;
            hours++;
        }
        while (hours >= 8) {
            hours -= 8;
            days++;
        }
        while (days >= 5) {
            days -= 5;
            weeks++;
        }

        String formattedTime = minutes != 0 ? String.format("%dm", minutes) : "";
        formattedTime = hours != 0 ? String.format("%dh", hours) + formattedTime : formattedTime;
        formattedTime = days != 0 ? String.format("%dd", days) + formattedTime : formattedTime;
        formattedTime = weeks != 0 ? String.format("%dw", weeks) + formattedTime : formattedTime;

        return formattedTime;
    }

    public static String append(String dest, String src, int times) {
        StringBuffer sb = new StringBuffer(dest);
        for (int i = 0; i < times; i++) {
            sb.append(src);
        }
        return sb.toString();
    }

    public static int timesToWrap(String str, int maxLength) {
        int length = str.length();
        int times = 0;
        while ((length -= maxLength) > 0) {
            times++;
        }
        return times;
    }

    public static String wrap(String str, int wrapperLength, int times) {
        String wrapped = "";
        int previous = 0;
        int next = wrapperLength;
        while (times-- > 0) {
            wrapped += str.substring(previous, next).concat(System.lineSeparator());
            previous = next;
            next += wrapperLength;
        }
        wrapped += str.substring(previous, str.length());
        return wrapped;
    }

    public static String wrap(String toWrap, int wrapperLength) {
        return wrap(toWrap, wrapperLength, timesToWrap(toWrap, wrapperLength));
    }

    public static List<String> wrapToList(String str, int wrapperLength, int times) {
        List<String> wrapped = new ArrayList<>();
        int previous = 0;
        int next = wrapperLength;
        while (times-- > 0) {
            wrapped.add(str.substring(previous, next));
            previous = next;
            next += wrapperLength;
        }
        wrapped.add(str.substring(previous, str.length()));
        return wrapped;
    }

    public static List<String> wrapToList(String toWrap, int wrapperLength) {
        return wrapToList(toWrap, wrapperLength, timesToWrap(toWrap, wrapperLength));
    }

    public static <T> T getOrReturn(List<T> list, int idx, T t) {
        if (idx < list.size()) {
            return list.get(idx);
        } else {
            return t;
        }
    }

//    public static void main(String[] args) {
//        System.out.println(wrapToList("1112223334445556667", 3));
//    }
}
