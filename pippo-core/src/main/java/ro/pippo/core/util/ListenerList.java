/*
 * Copyright (C) 2014 the original author or authors.
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
package ro.pippo.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class is a thread safe list that is designed for storing lists of listeners.
 *
 * @author Decebal Suiu
 */
public class ListenerList<T> implements Iterable<T> {

    private List<T> listeners;

    public ListenerList() {
        listeners = Collections.synchronizedList(new ArrayList<>());
    }

    public void add(T listener) {
        listeners.add(listener);
    }

    public void remove(T listener) {
        listeners.remove(listener);
    }

    public boolean isEmpty() {
        return listeners.isEmpty();
    }

    public int size() {
        return listeners.size();
    }

    @Override
    public Iterator<T> iterator() {
        return listeners.iterator();
    }

    protected void notify(Notifier<T> notifier) {
        for (T listener : listeners) {
            notifier.notify(listener);
        }
    }

    protected interface Notifier<T> {

        void notify(T listener);

    }

}
