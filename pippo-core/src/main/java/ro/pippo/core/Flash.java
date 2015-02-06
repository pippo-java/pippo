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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Flash messages are server-side messages that are that is only available for a single request after
 * the current one (this is useful for displaying messages after a redirect).
 *
 * @author Decebal Suiu
 */
public class Flash implements Iterable<Flash.Message> {

    private List<Message> messages;

    public Flash() {
        messages = new ArrayList<>();
    }

    public void add(int level, String message) {
        messages.add(new Message(level, message));
    }

    public void error(String message) {
        add(Message.ERROR, message);
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

    public boolean hasError() {
        return !isEmpty(Message.ERROR);
    }

    public String getError() {
        return hasError() ? get(Message.ERROR).get(0) : null;
    }

    public List<String> getErrors() {
        return hasError() ? get(Message.ERROR) : Collections.EMPTY_LIST;
    }

    public boolean hasSuccess() {
        return !isEmpty(Message.SUCCESS);
    }

    public String getSuccess() {
        return hasSuccess() ? get(Message.SUCCESS).get(0) : null;
    }

    public List<String> getSuccesses() {
        return hasSuccess() ? get(Message.SUCCESS) : Collections.EMPTY_LIST;
    }

    @Override
    public Iterator<Flash.Message> iterator() {
        return messages.iterator();
    }

    @Override
    public String toString() {
        return "FlashMessages{" +
            "messages=" + messages +
            '}';
    }

    public static class Message {

        /**
         * Constant for an undefined level.
         */
        public static final int UNDEFINED = 0;

        /**
         * Constant for debug level.
         */
        public static final int DEBUG = 100;

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

        /**
         * Constant for fatal level.
         */
        public static final int FATAL = 600;

        /* Levels as strings for debugging. */
        private static final Map<Integer, String> levelStrings = new HashMap<>();

        static {
            levelStrings.put(UNDEFINED, "UNDEFINED");
            levelStrings.put(DEBUG, "DEBUG");
            levelStrings.put(INFO, "INFO");
            levelStrings.put(SUCCESS, "SUCCESS");
            levelStrings.put(WARNING, "WARNING");
            levelStrings.put(ERROR, "ERROR");
            levelStrings.put(FATAL, "FATAL");
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

        public boolean isUndefined() {
            return (getLevel() == UNDEFINED);
        }

        public boolean isDebug() {
            return isLevel(DEBUG);
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

        public boolean isFatal() {
            return isLevel(FATAL);
        }

        public boolean isLevel(int level) {
            return (getLevel() >= level);
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
