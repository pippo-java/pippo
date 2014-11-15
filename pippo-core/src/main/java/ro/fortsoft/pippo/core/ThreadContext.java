/*
 * Copyright 2014 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pippo.core;

/**
 * Holds thread local state for Pippo data.
 *
 * @author Decebal Suiu
 */
public class ThreadContext {

    private Application application;

    private static final ThreadLocal<ThreadContext> threadLocal = new ThreadLocal<>();

    private ThreadContext() {
    }

    public static ThreadContext get() {
        return get(false);
    }

    public static Application getApplication() {
        ThreadContext context = get(false);
        return (context != null) ? context.application : null;
    }

    public static void setApplication(Application application) {
        ThreadContext context = get(true);
        context.application = application;
    }

    public static ThreadContext detach() {
        ThreadContext value = threadLocal.get();
        threadLocal.remove();

        return value;
    }

    public static void restore(ThreadContext threadContext) {
        if (threadContext == null) {
            threadLocal.remove();
        } else {
            threadLocal.set(threadContext);
        }
    }

    private static ThreadContext get(boolean createIfDoesNotExist) {
        ThreadContext context = threadLocal.get();
        if (context == null) {
            if (createIfDoesNotExist) {
                context = new ThreadContext();
                threadLocal.set(context);
            } else {
				/*
				 * There is no ThreadContext set, but the threadLocal.get() operation has registered
				 * the threadLocal in this Thread's ThreadLocal map. We must now remove it.
				 */
                threadLocal.remove();
            }
        }

        return context;
    }

}
