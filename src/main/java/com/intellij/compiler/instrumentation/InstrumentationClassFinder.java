/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import sun.misc.Resource;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Eugene Zhuravlev
 *         Date: 2/16/12
 */
public class InstrumentationClassFinder {
  private static final PseudoClass[] EMPTY_PSEUDOCLASS_ARRAY = new PseudoClass[0];
  private static final String CLASS_RESOURCE_EXTENSION = ".class";
  private final Map<String, PseudoClass> myLoaded = new HashMap<String, PseudoClass>(); // className -> class object
  private final ClassFinderClasspath myClasspath;

  public InstrumentationClassFinder(final URL[] cp) {
    myClasspath = new ClassFinderClasspath(cp);
  }

  public PseudoClass loadClass(final String name) throws IOException, ClassNotFoundException {
    final String internalName = name.replace('.', '/'); // normalize
    final PseudoClass aClass = myLoaded.get(internalName);
    if (aClass != null) {
      return aClass;
    }

    final InputStream is;
    final String resourceName = internalName + CLASS_RESOURCE_EXTENSION;
    // look into classpath
    final Resource resource = myClasspath.getResource(resourceName, false);
    if (resource == null) {
        is = ClassLoader.getSystemResourceAsStream(resourceName);
    } else {
      is = resource.getInputStream();
    }
    if (is == null) {
      throw new ClassNotFoundException("Class not found: " + internalName);
    }

    try {
      final PseudoClass result = loadPseudoClass(is);
      myLoaded.put(internalName, result);
      return result;
    } finally {
      is.close();
    }
  }

  private PseudoClass loadPseudoClass(InputStream is) throws IOException {
    final ClassReader reader = new ClassReader(is);
    final V visitor = new V();

    reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    return new PseudoClass(visitor.myName, visitor.mySuperclassName, visitor.myInterfaces, visitor.myModifiers);
  }

  public final class PseudoClass {
    private final String myName;
    private final String mySuperClass;
    private final String[] myInterfaces;
    private final int myModifiers;

    private PseudoClass(final String name, final String superClass, final String[] interfaces, final int modifiers) {
      myName = name;
      mySuperClass = superClass;
      myInterfaces = interfaces;
      myModifiers = modifiers;
    }

    public boolean isInterface() {
      return (myModifiers & Opcodes.ACC_INTERFACE) > 0;
    }

    public String getName() {
      return myName;
    }

    public PseudoClass getSuperClass() throws IOException, ClassNotFoundException {
      final String superClass = mySuperClass;
      return superClass != null ? loadClass(superClass) : null;
    }

    private PseudoClass[] getInterfaces() throws IOException, ClassNotFoundException {
      if (myInterfaces == null) {
        return EMPTY_PSEUDOCLASS_ARRAY;
      }

      final PseudoClass[] result = new PseudoClass[myInterfaces.length];

      for (int i = 0; i < result.length; i++) {
        result[i] = loadClass(myInterfaces[i]);
      }

      return result;
    }

    public boolean equals(final Object o) {
      if (this == o) return true;
      //noinspection SimplifiableIfStatement
      if (o == null || getClass() != o.getClass()) return false;

      return getName().equals(((PseudoClass) o).getName());
    }

    private boolean isSubclassOf(final PseudoClass x) throws IOException, ClassNotFoundException {
      for (PseudoClass c = this; c != null; c = c.getSuperClass()) {
        final PseudoClass superClass = c.getSuperClass();

        if (superClass != null && superClass.equals(x)) {
          return true;
        }
      }

      return false;
    }

    private boolean implementsInterface(final PseudoClass x) throws IOException, ClassNotFoundException {
      for (PseudoClass c = this; c != null; c = c.getSuperClass()) {
        final PseudoClass[] tis = c.getInterfaces();
        for (final PseudoClass ti : tis) {
          if (ti.equals(x) || ti.implementsInterface(x)) {
            return true;
          }
        }
      }
      return false;
    }

    public boolean isAssignableFrom(final PseudoClass x) throws IOException, ClassNotFoundException {
      if (this.equals(x)) {
        return true;
      }
      if (x.isSubclassOf(this)) {
        return true;
      }
      if (x.implementsInterface(this)) {
        return true;
      }
      //noinspection RedundantIfStatement
      if (x.isInterface() && "java/lang/Object".equals(getName())) {
        return true;
      }
      return false;
    }

  }

  private static class V extends ClassVisitor {
    public String mySuperclassName = null;
    public String[] myInterfaces = null;
    public String myName = null;
    public int myModifiers;

    private V() {
      super(Opcodes.ASM5);
    }

    public void visit(int version, int access, String pName, String signature, String pSuperName, String[] pInterfaces) {
      mySuperclassName = pSuperName;
      myInterfaces = pInterfaces;
      myName = pName;
      myModifiers = access;
    }
  }

  static class ClassFinderClasspath {
    private static final String FILE_PROTOCOL = "file";

    private final Stack<URL> myUrls = new Stack<URL>();
    private final List<Loader> myLoaders = new ArrayList<Loader>();
    private final Map<URL, Loader> myLoadersMap = new HashMap<URL, Loader>();

    public ClassFinderClasspath(URL[] urls) {
      if (urls.length > 0) {
        for (int i = urls.length - 1; i >= 0; i--) {
          myUrls.push(urls[i]);
        }
      }
    }

    public Resource getResource(String s, boolean flag) {
      int i = 0;
      for (Loader loader; (loader = getLoader(i)) != null; i++) {
        Resource resource = loader.getResource(s, flag);
        if (resource != null) {
          return resource;
        }
      }

      return null;
    }

    private synchronized Loader getLoader(int i) {
      while (myLoaders.size() < i + 1) {
        URL url;
        synchronized (myUrls) {
          if (myUrls.empty()) {
            return null;
          }
          url = myUrls.pop();
        }

        if (myLoadersMap.containsKey(url)) {
          continue;
        }

        Loader loader;
        try {
          loader = getLoader(url);
          if (loader == null) {
            continue;
          }
        } catch (IOException ioexception) {
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
      } catch (URISyntaxException thisShouldNotHappen) {
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

      protected Loader(URL url) {
        myURL = url;
      }


      protected URL getBaseURL() {
        return myURL;
      }

      public abstract Resource getResource(final String name, boolean flag);

    }

    private static class FileLoader extends Loader {
      private final File myRootDir;

      FileLoader(URL url) throws IOException {
        super(url);
        if (!FILE_PROTOCOL.equals(url.getProtocol())) {
          throw new IllegalArgumentException("url");
        } else {
          final String s = unescapePercentSequences(url.getFile().replace('/', File.separatorChar));
          myRootDir = new File(s);
        }
      }

      public Resource getResource(final String name, boolean check) {
        URL url = null;
        File file = null;

        try {
          url = new URL(getBaseURL(), name);
          if (!url.getFile().startsWith(getBaseURL().getFile())) {
            return null;
          }

          file = new File(myRootDir, name.replace('/', File.separatorChar));
          if (!check || file.exists()) {     // check means we load or process resource so we check its existence via old way
            return new FileResource(name, url, file, !check);
          }
        } catch (Exception exception) {
          if (!check && file != null && file.exists()) {
            try {   // we can not open the file if it is directory, Resource still can be created
              return new FileResource(name, url, file, false);
            } catch (IOException ignored) {
            }
          }
        }
        return null;
      }

      private class FileResource extends Resource {
        private final String myName;
        private final URL myUrl;
        private final File myFile;

        public FileResource(String name, URL url, File file, boolean willLoadBytes) throws IOException {
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

      JarLoader(URL url) throws IOException {
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
          String s = unescapePercentSequences(myURL.getFile().replace('/', File.separatorChar));
          if (!new File(s).exists()) {
            throw new FileNotFoundException(s);
          } else {
            return new ZipFile(s);
          }
        }

        return null;
      }

      public Resource getResource(String name, boolean flag) {
        try {
          final ZipFile file = acquireZipFile();
          if (file != null) {
            final ZipEntry entry = file.getEntry(name);
            if (entry != null) {
              return new JarResource(entry, new URL(getBaseURL(), name));
            }
          }
        } catch (Exception e) {
          return null;
        }
        return null;
      }

      private class JarResource extends Resource {
        private final ZipEntry myEntry;
        private final URL myUrl;

        public JarResource(ZipEntry name, URL url) {
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
          } catch (IOException e) {
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


  private static String unescapePercentSequences(String s) {
    if (s.indexOf('%') == -1) {
      return s;
    }
    StringBuilder decoded = new StringBuilder();
    final int len = s.length();
    int i = 0;
    while (i < len) {
      char c = s.charAt(i);
      if (c == '%') {
        List<Integer> bytes = new ArrayList<Integer>();
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
          } catch (UnsupportedEncodingException ignored) {
          }
        }
      }

      decoded.append(c);
      i++;
    }
    return decoded.toString();
  }

  private static int decode(char c) {
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

}
