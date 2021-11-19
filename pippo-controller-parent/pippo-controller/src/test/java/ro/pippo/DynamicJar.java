/*
 * Copyright (C) 2016-present the original author or authors.
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
package ro.pippo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.tools.JavaFileObject;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import ro.pippo.core.util.IoUtils;

/**
 * Represents a {@code JAR} file.
 * <p>
 * The {@code MANIFEST.MF} file is created on the fly from the information supplied in {@link Builder}.
 * <p>
 * Based on {@code PluginJar}.
 * @see <a href="https://github.com/pf4j/pf4j/blob/master/pf4j/src/test/java/org/pf4j/plugin/PluginJar.java">PluginJar</a>
 */
public class DynamicJar {

    private final Path path;
    private final String mainClass;

    protected DynamicJar(Builder builder) {
        this.path = builder.path;
        this.mainClass = builder.mainClass;
    }

    /**
     * Path to JAR file.
     * @see #file()
     * @see #url()
     */
    public Path path() {
        return path;
    }

    /**
     * Path to JAR file.
     * @see #path()
     * @see #url()
     */
    public File file() {
        return path.toFile();
    }

    /**
     * Path to JAR file.
     * @see #file()
     * @see #path()
     */
    public URL url() throws MalformedURLException {
        return path.toUri().toURL();
    }

    /**
     * Path of the folder where the JAR file is located.
     * It can be useful when using the {@link #extract()} option.
     * @see #baseDirPath()
     */
    public URL baseDirURL() throws MalformedURLException {
        return path.getParent().toUri().toURL();
    }

    /**
     * Path of the folder where the JAR file is located.
     * It can be useful when using the {@link #extract()} option.
     * @see #baseDirURL()
     */
    public Path baseDirPath() {
        return path.getParent();
    }

    /**
     * Main-class.
     */
    public String mainClass() {
        return mainClass;
    }

    public static Manifest createManifest(Map<String, String> map) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            attributes.put(new Attributes.Name(entry.getKey()), entry.getValue());
        }
        return manifest;
    }

    public static class Builder {

        private final Path path;
        private String mainClass;
        private Map<String, String> manifestAttributes = new LinkedHashMap<>();
        private Set<JavaFileObject> classes = new LinkedHashSet<>();
        private boolean extract = false;

        /**
         * Instantiate a build to create a JAR file.
         * @param path to create the JAR file
         */
        public Builder(Path path) {
            this.path = path;
        }

        /**
         * Define a Main-class.
         */
        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        /**
         * Add extra attributes to the {@code manifest} file.
         */
        public Builder manifestAttributes(Map<String, String> manifestAttributes) {
            manifestAttributes.putAll(manifestAttributes);
            return this;
        }

        /**
         * Add extra attribute to the {@code manifest} file.
         */
        public Builder manifestAttribute(String name, String value) {
            manifestAttributes.put(name, value);
            return this;
        }

        /**
         * Add class to build.
         * @param classPath source path (not a .class)
         */
        public Builder clazz(String classPath) {
            return clazz(JavaFileObjects.forResource(classPath));
        }

        /**
         * Add class to build.
         * @param clazz {@link JavaFileObject} instance
         */
        public Builder clazz(JavaFileObject clazz) {
            classes.add(clazz);
            return this;
        }

        /**
         * Extract the JAR file in the same folder (and keep the original JAR file).
         * @see DynamicJar#baseDirPath()
         * @see DynamicJar#baseDirURL()
         */
        public Builder extract() {
            extract = true;
            return this;
        }

        /**
         * Build the JAR file.
         */
        public DynamicJar build() throws IOException {
            try (OutputStream outputStream = new FileOutputStream(path.toFile())) {
                Manifest manifest = createManifest();
                try (JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
                    Compilation compilation = Compiler.javac().compile(classes);
                    ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
                    for (JavaFileObject generated : generatedFiles) {
                        String generatedPath = generated.getName().replaceFirst("/CLASS_OUTPUT/", "");
                        JarEntry classEntry = new JarEntry(generatedPath);
                        jarOutputStream.putNextEntry(classEntry);
                        IoUtils.copy(generated.openInputStream(), jarOutputStream);
                        jarOutputStream.closeEntry();
                    }
                }
            }

            if (extract) {
                extract(path , path.getParent());
            }

            return new DynamicJar(this);
        }

        private Manifest createManifest() {
            Map<String, String> map = new LinkedHashMap<>();
            map.put("Created-By", "Pippo Test Runtime");
            if (mainClass != null) {
                map.put("Main-Class", mainClass);
            }
            if (manifestAttributes != null) {
                map.putAll(manifestAttributes);
            }
            return DynamicJar.createManifest(map);
        }

        private void extract(Path jarPath, Path destDir) throws IOException {
            JarFile jar = new JarFile(jarPath.toFile());
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry entry = enumEntries.nextElement();
                File f = destDir.resolve(entry.getName()).toFile();
                f.getParentFile().mkdirs();
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);
                IoUtils.copy(is, fos);
            }
        }

    }

}
