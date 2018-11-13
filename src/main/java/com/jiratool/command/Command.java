package com.jiratool.command;

import java.util.HashMap;

@FunctionalInterface
public interface Command {

    void execute();

    final class Context {
        private final HashMap<String, Object> cash;

        private Context() {
            cash = new HashMap<>();
        }

        public synchronized Object get(String key) {
            return cash.get(key);
        }

        public synchronized void add(String key, Object o) {
            cash.put(key, o);
        }

        private static class SingletonHelper {
            private static final Context INSTANCE = new Context();
        }

        public static synchronized Context getInstance() {
            return SingletonHelper.INSTANCE;
        }

    }
}
