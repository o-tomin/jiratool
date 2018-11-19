package com.jiratool.command;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Objects;

@FunctionalInterface
public interface Command {

    void execute();

    static String evelJs(String jsCode) {
        ScriptEngine scriptEngine = (ScriptEngine) Context.getInstance().get("jsengine");
        try {
            return Objects.toString(scriptEngine.eval(jsCode));
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }

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
