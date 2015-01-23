/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.compiler.instrumentation;

import sun.misc.Resource;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ClassFinderClasspath {
    private static final String FILE_PROTOCOL = "file";

    private final Stack<URL> myUrls = new Stack<URL>();
    private final List<Loader> myLoaders = new ArrayList<Loader>();
    private final Map<URL, Loader> myLoadersMap = new HashMap<URL, Loader>();

    public ClassFinderClasspath(final URL[] urls) {
        if (urls.length > 0) {
            for (int i = urls.length - 1; i >= 0; i--) {
                myUrls.push(urls[i]);
            }
        }
    }

    private static String unescapePercentSequences(final String s) {
        if (s.indexOf('%') == -1) {
            return s;
        }
        final StringBuilder decoded = new StringBuilder();
        final int len = s.length();
        int i = 0;
        while (i < len) {
            final char c = s.charAt(i);
            if (c == '%') {
                final List<Integer> bytes = new ArrayList<Integer>();
                while (i + 2 < len && s.charAt(i) == '%') {
                    final int d1 = decode(s.charAt(i + 1));
                    final int d2 = decode(s.charAt(i + 2));
                    if (d1 != -1 && d2 != -1) {
                        bytes.add(((d1 & 0xf) << 4 | d2 & 0xf));
                        i += 3;
                    } else {
                        break;
                    }
                }
                if (!bytes.isEmpty()) {
                    final byte[] bytesArray = new byte[bytes.size()];
                    for (int j = 0; j < bytes.size(); j++) {
                        bytesArray[j] = (byte) bytes.get(j).intValue();
                    }
                    try {
                        decoded.append(new String(bytesArray, "UTF-8"));
                        continue;
                    } catch (final UnsupportedEncodingException ignored) {
                    }
                }
            }

            decoded.append(c);
            i++;
        }
        return decoded.toString();
    }

    private static int decode(final char c) {
        if ((c >= '0') && (c <= '9')) {
            return c - '0';
        }
        if ((c >= 'a') && (c <= 'f')) {
            return c - 'a' + 10;
        }
        if ((c >= 'A') && (c <= 'F')) {
            return c - 'A' + 10;
        }
        return -1;
    }

    public Resource getResource(final String s) {
        int i = 0;
        for (Loader loader; (loader = getLoader(i)) != null; i++) {
            final Resource resource = loader.getResource(s);
            if (resource != null) {
                return resource;
            }
        }

        return null;
    }

    private synchronized Loader getLoader(final int i) {
        while (myLoaders.size() < i + 1) {
            final URL url;
            synchronized (myUrls) {
                if (myUrls.empty()) {
                    return null;
                }
                url = myUrls.pop();
            }

            if (myLoadersMap.containsKey(url)) {
                continue;
            }

            final Loader loader;
            try {
                loader = getLoader(url);
                if (loader == null) {
                    continue;
                }
            } catch (final IOException ioexception) {
                continue;
            }

            myLoaders.add(loader);
            myLoadersMap.put(url, loader);
        }

        return myLoaders.get(i);
    }

    private Loader getLoader(final URL url) throws IOException {
        String s;
        try {
            s = url.toURI().getSchemeSpecificPart();
        } catch (final URISyntaxException thisShouldNotHappen) {
            thisShouldNotHappen.printStackTrace();
            s = url.getFile();
        }

        Loader loader = null;
        if (s != null && new File(s).isDirectory()) {
            if (FILE_PROTOCOL.equals(url.getProtocol())) {
                loader = new FileLoader(url);
            }
        } else {
            loader = new JarLoader(url);
        }

        return loader;
    }


    private abstract static class Loader {
        protected static final String JAR_PROTOCOL = "jar";
        protected static final String FILE_PROTOCOL = "file";

        private final URL myURL;

        protected Loader(final URL url) {
            myURL = url;
        }


        protected URL getBaseURL() {
            return myURL;
        }

        public abstract Resource getResource(final String name);

    }

    private static class FileLoader extends Loader {
        private final File myRootDir;

        FileLoader(final URL url) {
            super(url);
            if (!FILE_PROTOCOL.equals(url.getProtocol())) {
                throw new IllegalArgumentException("url");
            } else {
                final String s = unescapePercentSequences(url.getFile().replace('/', File.separatorChar));
                myRootDir = new File(s);
            }
        }

        public Resource getResource(final String name) {
            URL url = null;
            File file = null;

            try {
                url = new URL(getBaseURL(), name);
                if (!url.getFile().startsWith(getBaseURL().getFile())) {
                    return null;
                }

                file = new File(myRootDir, name.replace('/', File.separatorChar));
                // check means we load or process resource so we check its existence via old way
                return new FileLoader.FileResource(name, url, file, true);
            } catch (final Exception exception) {
                if (file != null && file.exists()) {
                    try {   // we can not open the file if it is directory, Resource still can be created
                        return new FileLoader.FileResource(name, url, file, false);
                    } catch (final IOException ignored) {
                    }
                }
            }
            return null;
        }

        private class FileResource extends Resource {
            private final String myName;
            private final URL myUrl;
            private final File myFile;

            public FileResource(final String name, final URL url, final File file, final boolean willLoadBytes) throws IOException {
                myName = name;
                myUrl = url;
                myFile = file;
                if (willLoadBytes) getByteBuffer(); // check for existence by creating cached file input stream
            }

            public String getName() {
                return myName;
            }

            public URL getURL() {
                return myUrl;
            }

            public URL getCodeSourceURL() {
                return getBaseURL();
            }

            public InputStream getInputStream() throws IOException {
                return new BufferedInputStream(new FileInputStream(myFile));
            }

            public int getContentLength() throws IOException {
                return -1;
            }

            public String toString() {
                return myFile.getAbsolutePath();
            }
        }

        public String toString() {
            return "FileLoader [" + myRootDir + "]";
        }
    }

    private class JarLoader extends Loader {
        private final URL myURL;
        private ZipFile myZipFile;

        JarLoader(final URL url) throws IOException {
            super(new URL(JAR_PROTOCOL, "", -1, url + "!/"));
            myURL = url;
        }

        private ZipFile acquireZipFile() throws IOException {
            ZipFile zipFile = myZipFile;
            if (zipFile == null) {
                zipFile = doGetZipFile();
                myZipFile = zipFile;
            }
            return zipFile;
        }

        private ZipFile doGetZipFile() throws IOException {
            if (FILE_PROTOCOL.equals(myURL.getProtocol())) {
                final String s = unescapePercentSequences(myURL.getFile().replace('/', File.separatorChar));
                if (!new File(s).exists()) {
                    throw new FileNotFoundException(s);
                } else {
                    return new ZipFile(s);
                }
            }

            return null;
        }

        public Resource getResource(final String name) {
            try {
                final ZipFile file = acquireZipFile();
                if (file != null) {
                    final ZipEntry entry = file.getEntry(name);
                    if (entry != null) {
                        return new JarLoader.JarResource(entry, new URL(getBaseURL(), name));
                    }
                }
            } catch (final Exception e) {
                return null;
            }
            return null;
        }

        private class JarResource extends Resource {
            private final ZipEntry myEntry;
            private final URL myUrl;

            public JarResource(final ZipEntry name, final URL url) {
                myEntry = name;
                myUrl = url;
            }

            public String getName() {
                return myEntry.getName();
            }

            public URL getURL() {
                return myUrl;
            }

            public URL getCodeSourceURL() {
                return myURL;
            }

            public InputStream getInputStream() throws IOException {
                try {
                    final ZipFile file = acquireZipFile();
                    if (file == null) {
                        return null;
                    }

                    final InputStream inputStream = file.getInputStream(myEntry);
                    if (inputStream == null) {
                        return null; // if entry was not found
                    }
                    return new FilterInputStream(inputStream) {
                    };
                } catch (final IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public int getContentLength() {
                return (int) myEntry.getSize();
            }
        }

        public String toString() {
            return "JarLoader [" + myURL + "]";
        }
    }
}
