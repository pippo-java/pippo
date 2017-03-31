/*
 * Copyright (C) 2017 the original author or authors.
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
package ro.pippo.core.reload;

import ro.pippo.core.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A {@link ClassLoader} that loads classes from files.
 * It's used for reloading mechanism.
 *
 * @author Decebal Suiu
 */
public abstract class ReloadClassLoader extends ClassLoader {

    private final String rootPackageName;

    public ReloadClassLoader(ClassLoader parentClassLoader, String rootPackageName) {
        super(parentClassLoader);

        if (rootPackageName == null) {
            throw new IllegalArgumentException("The 'rootPackageName' parameter is null");
        }

        this.rootPackageName = rootPackageName;
    }

    @Override
    public Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        if (isTarget(className)) {
            Class<?> clazz = findLoadedClass(className);
            if (clazz != null) {
                return clazz;
            }

            int index = className.lastIndexOf('.');
            if (index >= 0) {
                String packageName = className.substring(0, index);
                if (getPackage(packageName) == null) {
                    try {
                        definePackage(packageName, null, null, null, null, null, null, null);
                    } catch (IllegalArgumentException e) {
                        // ignore
                    }
                }
            }

            clazz = defineClass(className, resolve);
            if (clazz != null) {
                return clazz;
            }
        }

        return super.loadClass(className, resolve);
    }


    protected Class<?> defineClass(String className, boolean resolve) {
        String path = className.replace('.', '/') + ".class";
        InputStream is = getInputStream(path);
        if (is != null) {
            Class<?> clazz = defineClass(className, is);
            if (resolve) {
                resolveClass(clazz);
            }

            return clazz;
        }

        return null;
    }

    protected Class<?> defineClass(String className, InputStream is) {
        try {
            return defineClass(className, IoUtils.getBytes(is));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected Class<?> defineClass(String className, byte[] bytes) {
        return defineClass(className, bytes, 0, bytes.length);
    }

    protected abstract InputStream getInputStream(String path);

    protected boolean isTarget(String className) {
        if (rootPackageName.isEmpty()) {
            return true;
        }

        return className.startsWith(rootPackageName + ".");
    }

}
