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
package ro.pippo.core.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.pippo.core.HttpConstants;
import ro.pippo.core.PippoRuntimeException;
import ro.pippo.core.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Serves a directory.
 *
 * It will display a static welcome file for directory requests.
 * It will render file lists (either by template or generated) if there is no welcome file.
 * It will properly respond to HEAD requests.
 *
 * @author James Moger
 */
public class DirectoryHandler implements RouteHandler {

    private static final Logger log = LoggerFactory.getLogger(DirectoryHandler.class);

    public static final String PATH_PARAMETER = "path";

    private final String urlPath;
    private final String uriPattern;
    private final File directory;

    private String timestampPattern = "yyyy-MM-dd HH:mm Z";
    private String fileSizePattern = "#,000";
    private String directoryTemplate;
    private boolean chunked;

    private Comparator<DirEntry> comparator;

    public DirectoryHandler(String urlPath, File directory) {
        this.urlPath = urlPath;
        String normalizedPath = getNormalizedPath(urlPath);
        if (normalizedPath.length() > 0) {
            this.uriPattern = String.format("/%s/?{%s: .*}", getNormalizedPath(urlPath), PATH_PARAMETER);
        } else {
            this.uriPattern = String.format("/{%s: .*}", PATH_PARAMETER);
        }

        this.directory = directory.getAbsoluteFile();

        comparator = new DirectoryBeforeComparator();
    }

    public DirectoryHandler(String urlPath, String directory) {
        this(urlPath, new File(directory));
    }

    public String getUrlPath() {
        return urlPath;
    }

    public String getUriPattern() {
        return uriPattern;
    }

    public String getTimestampPattern() {
        return timestampPattern;
    }

    public DirectoryHandler setTimestampPattern(String pattern) {
        this.timestampPattern = pattern;

        return this;
    }

    public String getFileSizePattern() {
        return fileSizePattern;
    }

    public DirectoryHandler setFileSizePattern(String pattern) {
        this.fileSizePattern = pattern;

        return this;
    }

    public String getDirectoryTemplate() {
        return directoryTemplate;
    }

    public DirectoryHandler setDirectoryTemplate(String template) {
        this.directoryTemplate = template;

        return this;
    }

    public boolean getChunkedTransfer() {
        return chunked;
    }

    public DirectoryHandler setChunkedTransfer(boolean chunked) {
        this.chunked = chunked;

        return this;
    }

    public Comparator<DirEntry> getComparator() {
        return comparator;
    }

    public DirectoryHandler setComparator(Comparator<DirEntry> comparator) {
        this.comparator = comparator;

        return this;
    }

    @Override
    public final void handle(RouteContext routeContext) {
        String resourcePath = getResourcePath(routeContext);
        log.trace("Request resource '{}'", resourcePath);

        handle(routeContext, resourcePath);

        routeContext.next();
    }

    protected void handle(RouteContext routeContext, String resourcePath) {
        try {
            Path requestedPath = new File(directory, resourcePath).toPath().normalize().toAbsolutePath();
            if (!requestedPath.startsWith(directory.getAbsolutePath())) {
                log.warn("Request for '{}' which is not located in '{}'", requestedPath, directory);
            } else if (StringUtils.isNullOrEmpty(resourcePath) || "/".equals(resourcePath)) {
                handleDirectoryRequest(routeContext, directory);
            } else {
                // look for requested file
                File file = requestedPath.toFile();
                if (file.exists()) {
                    if (file.isFile()) {
                        routeContext.getResponse().contentLength(file.length());
                        URL url = requestedPath.toUri().toURL();
                        switch (routeContext.getRequestMethod()) {
                            case HttpConstants.Method.HEAD:
                                setResponseHeaders(url, routeContext);
                                routeContext.getResponse().commit();
                                break;
                            case HttpConstants.Method.GET:
                                streamResource(url, routeContext);
                                break;
                            default:
                                log.warn("Unsupported request method {} for {}",
                                    routeContext.getRequestMethod(), routeContext.getRequestUri());
                        }
                    } else {
                        handleDirectoryRequest(routeContext, file);
                    }
                } else {
                    log.warn("{} not found for request path {}", requestedPath, routeContext.getRequestUri());
                }
            }
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected File getIndexFile(File dir) {
        String[] welcomeFiles = new String[]{"index.html", "index.htm"};
        for (String welcomeFile : welcomeFiles) {
            File file = new File(dir, welcomeFile);
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    protected void handleDirectoryRequest(RouteContext routeContext, File dir) throws MalformedURLException {
        File index = getIndexFile(dir);
        if (index != null) {
            routeContext.getResponse().contentLength(index.length());
            URL url = index.toURI().toURL();
            streamResource(url, routeContext);
            return;
        }

        sendDirectoryListing(routeContext, dir);
    }

    protected File[] getFiles(File directory) {
        return Optional.ofNullable(directory.listFiles()).orElse(new File[0]);
    }

    protected List<DirEntry> getDirEntries(RouteContext routeContext, File dir, String absoluteDirUri) {
        List<DirEntry> list = new ArrayList<>();
        for (File file : getFiles(dir)) {
            String fileUrl = routeContext.getRequest().getApplicationPath()
                + StringUtils.removeEnd(StringUtils.addStart(absoluteDirUri, "/"), "/")
                + StringUtils.addStart(file.getName(), "/");
            list.add(new DirEntry(fileUrl, file));
        }

        if (comparator != null) {
            list.sort(comparator);
        }

        if (!directory.equals(dir)) {
            File upDir = new File(dir, "../");
            list.add(0, new DirEntry(routeContext.getRequest().getApplicationPath()
                + StringUtils.removeEnd(StringUtils.addStart(absoluteDirUri, "/"), "/")
                + StringUtils.addStart(upDir.getName(), "/"), upDir));
        }

        return list;
    }

    protected String getResourcePath(RouteContext routeContext) {
        return getNormalizedPath(routeContext.getParameter(PATH_PARAMETER).toString());
    }

    protected String getNormalizedPath(String path) {
        if (path.length() > 0 && '/' == path.charAt(0)) {
            path = path.substring(1);
        }
        if (path.length() > 0 && '/' == path.charAt(path.length() - 1)) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    protected void setResponseHeaders(URL resourceUrl, RouteContext routeContext) {
        try {
            long lastModified = resourceUrl.openConnection().getLastModified();
            routeContext.getApplication().getHttpCacheToolkit().addEtag(routeContext, lastModified);

            String filename = resourceUrl.getFile();
            String mimeType = routeContext.getApplication().getMimeTypes().getContentType(filename);
            if (!StringUtils.isNullOrEmpty(mimeType)) {
                routeContext.getResponse().contentType(mimeType);
            }

        } catch (Exception e) {
            throw new PippoRuntimeException(e, "Failed to stream resource {}", resourceUrl);
        }
    }

    protected void streamResource(URL resourceUrl, RouteContext routeContext) {
        try {
            setResponseHeaders(resourceUrl, routeContext);
            if (routeContext.getResponse().getStatus() == HttpConstants.StatusCode.NOT_MODIFIED) {
                // do not stream anything out, simply return 304
                routeContext.getResponse().commit();
            } else {
                sendResource(resourceUrl, routeContext);
            }
        } catch (IOException e) {
            String message = e.getMessage();
            if (!StringUtils.isNullOrEmpty(message)) {
                log.warn("Error sending resource {} to {}: {}",
                    resourceUrl, routeContext.getRequest().getClientIp(), message);
            } else {
                throw new PippoRuntimeException(e, "Failed to stream resource {}", resourceUrl);
            }
        } catch (Exception e) {
            throw new PippoRuntimeException(e, "Failed to stream resource {}", resourceUrl);
        }
    }

    protected void sendResource(URL resourceUrl, RouteContext routeContext) throws IOException {
        String filename = resourceUrl.getFile();
        String mimeType = routeContext.getApplication().getMimeTypes().getContentType(filename);
        if (!StringUtils.isNullOrEmpty(mimeType)) {
            // stream the resource
            log.debug("Streaming as resource '{}'", resourceUrl);
            routeContext.getResponse().ok().chunked(chunked).resource(resourceUrl.openStream());
        } else {
            // stream the file
            log.debug("Streaming as file '{}'", resourceUrl);
            routeContext.getResponse().ok().chunked(chunked).file(filename, resourceUrl.openStream());
        }
    }

    private void sendDirectoryListing(RouteContext routeContext, File dir) {
        String absoluteDirUri = getUrlPath() + StringUtils.addStart(directory.toPath().relativize(dir.toPath()).toString(), "/");
        if (StringUtils.isNullOrEmpty(directoryTemplate)) {
            // Generate primitive, default directory listing
            String page = generateDefaultDirectoryListing(routeContext, dir, absoluteDirUri);
            routeContext.html().send(page);
        } else {
            // Render directory listing template
            int numFiles = 0;
            int numDirs = 0;
            long diskUsage = 0;
            List<DirEntry> dirEntries = getDirEntries(routeContext, dir, absoluteDirUri);
            for (DirEntry dirEntry : dirEntries) {
                if (dirEntry.isFile()) {
                    numFiles++;
                    diskUsage += dirEntry.getSize();
                } else if (dirEntry.isDirectory() && !dirEntry.getName().contains("..")) {
                    numDirs++;
                }
            }

            routeContext.setLocal("dirUrl", absoluteDirUri);
            routeContext.setLocal("dirPath", absoluteDirUri.substring(getUrlPath().length()));
            routeContext.setLocal("dirEntries", dirEntries);
            routeContext.setLocal("numDirs", numDirs);
            routeContext.setLocal("numFiles", numFiles);
            routeContext.setLocal("diskUsage", diskUsage);
            routeContext.render(directoryTemplate);
        }
    }

    protected String generateDefaultDirectoryListing(RouteContext routeContext, File dir, String absoluteDirUri) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><table>");
        SimpleDateFormat df = new SimpleDateFormat(timestampPattern);
        NumberFormat nf = new DecimalFormat(fileSizePattern);
        for (DirEntry dirEntry : getDirEntries(routeContext, dir, absoluteDirUri)) {
            sb.append(StringUtils.format("<tr><td><a href=\"{}\">{}</a></td><td>{}</td><td>{}</td></tr>\n",
                dirEntry.getUrl(),
                dirEntry.getName(),
                dirEntry.isFile() ? nf.format(dirEntry.getSize()) : "",
                df.format(dirEntry.getLastModified())));
        }
        sb.append("</table></body></html>");

        return sb.toString();
    }

    public static class DirEntry {

        private final String url;
        private final File file;

        public DirEntry(String url, File file) {
            this.url = url;
            this.file = file;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return file.getName();
        }

        public long getLength() {
            return file.length();
        }

        public long getSize() {
            return file.length();
        }

        public Date getLastModified() {
            return new Date(file.lastModified());
        }

        public boolean isFile() {
            return file.isFile();
        }

        public boolean isDirectory() {
            return file.isDirectory();
        }

    }

    public static class DirectoryBeforeComparator implements Comparator<DirEntry> {

        @Override
        public int compare(DirEntry o1, DirEntry o2) {
            if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            }

            if (!o1.isDirectory() && o2.isDirectory()) {
                return 1;
            }

            return o1.file.compareTo(o2.file);
        }

    }

}
