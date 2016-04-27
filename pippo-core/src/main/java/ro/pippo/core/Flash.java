/*
 * Copyright (C) 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.pippo.core;

import ro.pippo.core.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Flash messages are server-side messages that are only available for a single request after
 * the current one (this is useful for displaying messages after a redirect).
 *
 * @author Decebal Suiu
 */
public class Flash implements Iterable<Flash.Message>, Serializable {

    private List<Message> messages;

    public Flash() {
        messages = new ArrayList<>();
    }

    public void add(int level, String message) {
        messages.add(new Message(level, message));
    }

    public List<String> get(int level) {
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>();
        for (Message message : messages) {
            if (message.isLevel(level)) {
                list.add(message.getMessage());
            }
        }

        return list;
    }

    public boolean isEmpty() {
        return messages.isEmpty();
    }

    public boolean isEmpty(int level) {
        return get(level).isEmpty();
    }

    @Override
    public Iterator<Flash.Message> iterator() {
        return messages.iterator();
    }

    // some helpers

    public void error(String message) {
        add(Message.ERROR, message);
    }

    public void error(String message, Object... args) {
        add(Message.ERROR, StringUtils.format(message, args));
    }

    public boolean hasError() {
        return !isEmpty(Message.ERROR);
    }

    public String getError() {
        return hasError() ? get(Message.ERROR).get(0) : null;
    }

    public List<String> getErrorList() {
        return hasError() ? get(Message.ERROR) : Collections.emptyList();
    }

    public void success(String message) {
        add(Message.SUCCESS, message);
    }

    public void success(String message, Object... args) {
        add(Message.SUCCESS, StringUtils.format(message, args));
    }

    public boolean hasSuccess() {
        return !isEmpty(Message.SUCCESS);
    }

    public String getSuccess() {
        return hasSuccess() ? get(Message.SUCCESS).get(0) : null;
    }

    public List<String> getSuccessList() {
        return hasSuccess() ? get(Message.SUCCESS) : Collections.emptyList();
    }

    public void warning(String message) {
        add(Message.WARNING, message);
    }

    public void warning(String message, Object... args) {
        add(Message.WARNING, StringUtils.format(message, args));
    }

    public boolean hasWarning() {
        return !isEmpty(Message.WARNING);
    }

    public String getWarning() {
        return hasWarning() ? get(Message.WARNING).get(0) : null;
    }

    public List<String> getWarningList() {
        return hasWarning() ? get(Message.WARNING) : Collections.emptyList();
    }

    public void info(String message) {
        add(Message.INFO, message);
    }

    public void info(String message, Object... args) {
        add(Message.INFO, StringUtils.format(message, args));
    }

    public boolean hasInfo() {
        return !isEmpty(Message.INFO);
    }

    public String getInfo() {
        return hasInfo() ? get(Message.INFO).get(0) : null;
    }

    public List<String> getInfoList() {
        return hasInfo() ? get(Message.INFO) : Collections.emptyList();
    }

    @Override
    public String toString() {
        return "FlashMessages{" +
            "messages=" + messages +
            '}';
    }

    public static class Message implements Serializable {

        /**
         * Constant for info level.
         */
        public static final int INFO = 200;

        /**
         * Constant for success level (it indicates the outcome of an operation)
         */
        public static final int SUCCESS = 300;

        /**
         * Constant for warning level.
         */
        public static final int WARNING = 400;

        /**
         * Constant for error level.
         */
        public static final int ERROR = 500;

        /* Levels as strings for debugging. */
        private static final Map<Integer, String> levelStrings = new HashMap<>();

        static {
            levelStrings.put(INFO, "INFO");
            levelStrings.put(SUCCESS, "SUCCESS");
            levelStrings.put(WARNING, "WARNING");
            levelStrings.put(ERROR, "ERROR");
        }

        private int level;
        private String message;

        public Message(int level, String message) {
            this.level = level;
            this.message = message;
        }

        public int getLevel() {
            return level;
        }

        public String getLevelAsString() {
            return levelStrings.get(getLevel());
        }

        public String getMessage() {
            return message;
        }

        public boolean isInfo() {
            return isLevel(INFO);
        }

        public boolean isSuccess() {
            return isLevel(SUCCESS);
        }

        public boolean isWarning() {
            return isLevel(WARNING);
        }

        public boolean isError() {
            return isLevel(ERROR);
        }

        public boolean isLevel(int level) {
            return (getLevel() == level);
        }

        @Override
        public String toString() {
            return "Message{" +
                "level=" + level +
                ", message='" + message + '\'' +
                '}';
        }

    }

}
