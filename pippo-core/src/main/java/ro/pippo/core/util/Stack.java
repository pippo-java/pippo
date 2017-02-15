/*
 * Copyright (C) 2016 the original author or authors.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * A stack class implemented as a wrapper around a {@link java.util.LinkedList}
 * that silently discards {@code null} items.
 *
 * @author Decebal Suiu
 */
public class Stack<E> implements Iterable<E> {

    private LinkedList<E> list = new LinkedList<>();

    /**
     * Adds the given item to the top of the stack.
     */
    public void push(E item) {
        if (item != null) {
            list.addFirst(item);
        }
    }

    /**
     * Adds the given item to the top of the stack if the stack is not empty.
     */
    public void pushIfNotEmpty(E item) {
        if (!isEmpty()) {
            push(item);
        }
    }

    /**
     * Removes the top item from the stack and returns it.
     * Returns {@code null} if the stack is empty.
     */
    public E pop() {
        try {
            return list.removeFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Returns the top item from the stack without popping it.
     * Returns {@code null} if the stack is empty.
     */
    public E peek() {
        try {
            return list.getFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Returns the number of items currently in the stack.
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns whether the stack is empty or not.
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

}
