package org.springframework.boot.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;
import org.springframework.boot.loader.util.SystemPropertyUtils;

public class PropertiesLauncher extends Launcher {
   private static final Class<?>[] PARENT_ONLY_PARAMS = new Class[]{ClassLoader.class};
   private static final Class<?>[] URLS_AND_PARENT_PARAMS = new Class[]{URL[].class, ClassLoader.class};
   private static final Class<?>[] NO_PARAMS = new Class[0];
   private static final URL[] NO_URLS = new URL[0];
   private static final String DEBUG = "loader.debug";
   public static final String MAIN = "loader.main";
   public static final String PATH = "loader.path";
   public static final String HOME = "loader.home";
   public static final String ARGS = "loader.args";
   public static final String CONFIG_NAME = "loader.config.name";
   public static final String CONFIG_LOCATION = "loader.config.location";
   public static final String SET_SYSTEM_PROPERTIES = "loader.system";
   private static final Pattern WORD_SEPARATOR = Pattern.compile("\\W+");
   private static final String NESTED_ARCHIVE_SEPARATOR;
   private final File home;
   private List<String> paths = new ArrayList();
   private final Properties properties = new Properties();
   private final Archive parent;
   private volatile ClassPathArchives classPathArchives;

   public PropertiesLauncher() {
      try {
         this.home = this.getHomeDirectory();
         this.initializeProperties();
         this.initializePaths();
         this.parent = this.createArchive();
      } catch (Exception var2) {
         throw new IllegalStateException(var2);
      }
   }

   protected File getHomeDirectory() {
      try {
         return new File(this.getPropertyWithDefault("loader.home", "${user.dir}"));
      } catch (Exception var2) {
         throw new IllegalStateException(var2);
      }
   }

   private void initializeProperties() throws Exception {
      List<String> configs = new ArrayList();
      if (this.getProperty("loader.config.location") != null) {
         configs.add(this.getProperty("loader.config.location"));
      } else {
         String[] names = this.getPropertyWithDefault("loader.config.name", "loader").split(",");
         String[] var3 = names;
         int var4 = names.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String name = var3[var5];
            File var10001 = this.getHomeDirectory();
            configs.add("file:" + var10001 + "/" + name + ".properties");
            configs.add("classpath:" + name + ".properties");
            configs.add("classpath:BOOT-INF/classes/" + name + ".properties");
         }
      }

      Iterator var9 = configs.iterator();

      while(true) {
         if (var9.hasNext()) {
            String config = (String)var9.next();
            InputStream resource = this.getResource(config);

            label46: {
               try {
                  if (resource != null) {
                     this.debug("Found: " + config);
                     this.loadResource(resource);
                     break label46;
                  }

                  this.debug("Not found: " + config);
               } catch (Throwable var8) {
                  if (resource != null) {
                     try {
                        resource.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (resource != null) {
                  resource.close();
               }
               continue;
            }

            if (resource != null) {
               resource.close();
            }

            return;
         }

         return;
      }
   }

   private void loadResource(InputStream resource) throws Exception {
      this.properties.load(resource);
      Iterator var2 = Collections.list(this.properties.propertyNames()).iterator();

      Object key;
      String value;
      while(var2.hasNext()) {
         key = var2.next();
         value = this.properties.getProperty((String)key);
         String value = SystemPropertyUtils.resolvePlaceholders(this.properties, value);
         if (value != null) {
            this.properties.put(key, value);
         }
      }

      if ("true".equals(this.getProperty("loader.system"))) {
         this.debug("Adding resolved properties to System properties");
         var2 = Collections.list(this.properties.propertyNames()).iterator();

         while(var2.hasNext()) {
            key = var2.next();
            value = this.properties.getProperty((String)key);
            System.setProperty((String)key, value);
         }
      }

   }

   private InputStream getResource(String config) throws Exception {
      if (config.startsWith("classpath:")) {
         return this.getClasspathResource(config.substring("classpath:".length()));
      } else {
         config = this.handleUrl(config);
         return this.isUrl(config) ? this.getURLResource(config) : this.getFileResource(config);
      }
   }

   private String handleUrl(String path) throws UnsupportedEncodingException {
      if (path.startsWith("jar:file:") || path.startsWith("file:")) {
         path = URLDecoder.decode(path, "UTF-8");
         if (path.startsWith("file:")) {
            path = path.substring("file:".length());
            if (path.startsWith("//")) {
               path = path.substring(2);
            }
         }
      }

      return path;
   }

   private boolean isUrl(String config) {
      return config.contains("://");
   }

   private InputStream getClasspathResource(String config) {
      while(config.startsWith("/")) {
         config = config.substring(1);
      }

      config = "/" + config;
      this.debug("Trying classpath: " + config);
      return this.getClass().getResourceAsStream(config);
   }

   private InputStream getFileResource(String config) throws Exception {
      File file = new File(config);
      this.debug("Trying file: " + config);
      return file.canRead() ? new FileInputStream(file) : null;
   }

   private InputStream getURLResource(String config) throws Exception {
      URL url = new URL(config);
      if (this.exists(url)) {
         URLConnection con = url.openConnection();

         try {
            return con.getInputStream();
         } catch (IOException var6) {
            if (con instanceof HttpURLConnection) {
               HttpURLConnection httpURLConnection = (HttpURLConnection)con;
               httpURLConnection.disconnect();
            }

            throw var6;
         }
      } else {
         return null;
      }
   }

   private boolean exists(URL url) throws IOException {
      URLConnection connection = url.openConnection();
      boolean var10 = false;

      boolean var5;
      HttpURLConnection httpURLConnection;
      label116: {
         label117: {
            boolean var12;
            try {
               var10 = true;
               connection.setUseCaches(connection.getClass().getSimpleName().startsWith("JNLP"));
               if (connection instanceof HttpURLConnection httpConnection) {
                  httpConnection.setRequestMethod("HEAD");
                  int responseCode = httpConnection.getResponseCode();
                  if (responseCode == 200) {
                     var5 = true;
                     var10 = false;
                     break label116;
                  }

                  if (responseCode == 404) {
                     var5 = false;
                     var10 = false;
                     break label117;
                  }
               }

               var12 = connection.getContentLength() >= 0;
               var10 = false;
            } finally {
               if (var10) {
                  if (connection instanceof HttpURLConnection httpURLConnection) {
                     httpURLConnection.disconnect();
                  }

               }
            }

            if (connection instanceof HttpURLConnection httpURLConnection) {
               httpURLConnection.disconnect();
            }

            return var12;
         }

         if (connection instanceof HttpURLConnection httpURLConnection) {
            httpURLConnection.disconnect();
         }

         return var5;
      }

      if (connection instanceof HttpURLConnection httpURLConnection) {
         httpURLConnection.disconnect();
      }

      return var5;
   }

   private void initializePaths() throws Exception {
      String path = this.getProperty("loader.path");
      if (path != null) {
         this.paths = this.parsePathsProperty(path);
      }

      this.debug("Nested archive paths: " + this.paths);
   }

   private List<String> parsePathsProperty(String commaSeparatedPaths) {
      List<String> paths = new ArrayList();
      String[] var3 = commaSeparatedPaths.split(",");
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String path = var3[var5];
         path = this.cleanupPath(path);
         path = path != null && !path.isEmpty() ? path : "/";
         paths.add(path);
      }

      if (paths.isEmpty()) {
         paths.add("lib");
      }

      return paths;
   }

   protected String[] getArgs(String... args) throws Exception {
      String loaderArgs = this.getProperty("loader.args");
      if (loaderArgs != null) {
         String[] defaultArgs = loaderArgs.split("\\s+");
         String[] additionalArgs = args;
         args = new String[defaultArgs.length + args.length];
         System.arraycopy(defaultArgs, 0, args, 0, defaultArgs.length);
         System.arraycopy(additionalArgs, 0, args, defaultArgs.length, additionalArgs.length);
      }

      return args;
   }

   protected String getMainClass() throws Exception {
      String mainClass = this.getProperty("loader.main", "Start-Class");
      if (mainClass == null) {
         throw new IllegalStateException("No 'loader.main' or 'Start-Class' specified");
      } else {
         return mainClass;
      }
   }

   protected ClassLoader createClassLoader(Iterator<Archive> archives) throws Exception {
      String customLoaderClassName = this.getProperty("loader.classLoader");
      if (customLoaderClassName == null) {
         return super.createClassLoader(archives);
      } else {
         Set<URL> urls = new LinkedHashSet();

         while(archives.hasNext()) {
            urls.add(((Archive)archives.next()).getUrl());
         }

         ClassLoader loader = new LaunchedURLClassLoader((URL[])urls.toArray(NO_URLS), this.getClass().getClassLoader());
         this.debug("Classpath for custom loader: " + urls);
         ClassLoader loader = this.wrapWithCustomClassLoader(loader, customLoaderClassName);
         this.debug("Using custom class loader: " + customLoaderClassName);
         return loader;
      }
   }

   private ClassLoader wrapWithCustomClassLoader(ClassLoader parent, String className) throws Exception {
      Class<ClassLoader> type = Class.forName(className, true, parent);
      ClassLoader classLoader = this.newClassLoader(type, PARENT_ONLY_PARAMS, parent);
      if (classLoader == null) {
         classLoader = this.newClassLoader(type, URLS_AND_PARENT_PARAMS, NO_URLS, parent);
      }

      if (classLoader == null) {
         classLoader = this.newClassLoader(type, NO_PARAMS);
      }

      if (classLoader == null) {
         throw new IllegalArgumentException("Unable to create class loader for " + className);
      } else {
         return classLoader;
      }
   }

   private ClassLoader newClassLoader(Class<ClassLoader> loaderClass, Class<?>[] parameterTypes, Object... initargs) throws Exception {
      try {
         Constructor<ClassLoader> constructor = loaderClass.getDeclaredConstructor(parameterTypes);
         constructor.setAccessible(true);
         return (ClassLoader)constructor.newInstance(initargs);
      } catch (NoSuchMethodException var5) {
         return null;
      }
   }

   private String getProperty(String propertyKey) throws Exception {
      return this.getProperty(propertyKey, (String)null, (String)null);
   }

   private String getProperty(String propertyKey, String manifestKey) throws Exception {
      return this.getProperty(propertyKey, manifestKey, (String)null);
   }

   private String getPropertyWithDefault(String propertyKey, String defaultValue) throws Exception {
      return this.getProperty(propertyKey, (String)null, defaultValue);
   }

   private String getProperty(String propertyKey, String manifestKey, String defaultValue) throws Exception {
      if (manifestKey == null) {
         manifestKey = propertyKey.replace('.', '-');
         manifestKey = toCamelCase(manifestKey);
      }

      String property = SystemPropertyUtils.getProperty(propertyKey);
      String value;
      if (property != null) {
         value = SystemPropertyUtils.resolvePlaceholders(this.properties, property);
         this.debug("Property '" + propertyKey + "' from environment: " + value);
         return value;
      } else if (this.properties.containsKey(propertyKey)) {
         value = SystemPropertyUtils.resolvePlaceholders(this.properties, this.properties.getProperty(propertyKey));
         this.debug("Property '" + propertyKey + "' from properties: " + value);
         return value;
      } else {
         try {
            if (this.home != null) {
               label72: {
                  ExplodedArchive archive = new ExplodedArchive(this.home, false);

                  String var8;
                  label58: {
                     try {
                        Manifest manifest = archive.getManifest();
                        if (manifest != null) {
                           String value = manifest.getMainAttributes().getValue(manifestKey);
                           if (value != null) {
                              this.debug("Property '" + manifestKey + "' from home directory manifest: " + value);
                              var8 = SystemPropertyUtils.resolvePlaceholders(this.properties, value);
                              break label58;
                           }
                        }
                     } catch (Throwable var10) {
                        try {
                           archive.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }

                        throw var10;
                     }

                     archive.close();
                     break label72;
                  }

                  archive.close();
                  return var8;
               }
            }
         } catch (IllegalStateException var11) {
         }

         Manifest manifest = this.createArchive().getManifest();
         if (manifest != null) {
            String value = manifest.getMainAttributes().getValue(manifestKey);
            if (value != null) {
               this.debug("Property '" + manifestKey + "' from archive manifest: " + value);
               return SystemPropertyUtils.resolvePlaceholders(this.properties, value);
            }
         }

         return defaultValue != null ? SystemPropertyUtils.resolvePlaceholders(this.properties, defaultValue) : defaultValue;
      }
   }

   protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
      ClassPathArchives classPathArchives = this.classPathArchives;
      if (classPathArchives == null) {
         classPathArchives = new ClassPathArchives();
         this.classPathArchives = classPathArchives;
      }

      return classPathArchives.iterator();
   }

   public static void main(String[] args) throws Exception {
      PropertiesLauncher launcher = new PropertiesLauncher();
      args = launcher.getArgs(args);
      launcher.launch(args);
   }

   public static String toCamelCase(CharSequence string) {
      if (string == null) {
         return null;
      } else {
         StringBuilder builder = new StringBuilder();
         Matcher matcher = WORD_SEPARATOR.matcher(string);

         int pos;
         for(pos = 0; matcher.find(); pos = matcher.end()) {
            builder.append(capitalize(string.subSequence(pos, matcher.end()).toString()));
         }

         builder.append(capitalize(string.subSequence(pos, string.length()).toString()));
         return builder.toString();
      }
   }

   private static String capitalize(String str) {
      char var10000 = Character.toUpperCase(str.charAt(0));
      return "" + var10000 + str.substring(1);
   }

   private void debug(String message) {
      if (Boolean.getBoolean("loader.debug")) {
         System.out.println(message);
      }

   }

   private String cleanupPath(String path) {
      path = path.trim();
      if (path.startsWith("./")) {
         path = path.substring(2);
      }

      String lowerCasePath = path.toLowerCase(Locale.ENGLISH);
      if (!lowerCasePath.endsWith(".jar") && !lowerCasePath.endsWith(".zip")) {
         if (path.endsWith("/*")) {
            path = path.substring(0, path.length() - 1);
         } else if (!path.endsWith("/") && !path.equals(".")) {
            path = path + "/";
         }

         return path;
      } else {
         return path;
      }
   }

   void close() throws Exception {
      if (this.classPathArchives != null) {
         this.classPathArchives.close();
      }

      if (this.parent != null) {
         this.parent.close();
      }

   }

   static {
      NESTED_ARCHIVE_SEPARATOR = "!" + File.separator;
   }

   private class ClassPathArchives implements Iterable<Archive> {
      private final List<Archive> classPathArchives = new ArrayList();
      private final List<JarFileArchive> jarFileArchives = new ArrayList();

      ClassPathArchives() throws Exception {
         Iterator var2 = PropertiesLauncher.this.paths.iterator();

         while(var2.hasNext()) {
            String path = (String)var2.next();
            Iterator var4 = this.getClassPathArchives(path).iterator();

            while(var4.hasNext()) {
               Archive archive = (Archive)var4.next();
               this.addClassPathArchive(archive);
            }
         }

         this.addNestedEntries();
      }

      private void addClassPathArchive(Archive archive) throws IOException {
         if (!(archive instanceof ExplodedArchive)) {
            this.classPathArchives.add(archive);
         } else {
            this.classPathArchives.add(archive);
            this.classPathArchives.addAll(this.asList(archive.getNestedArchives((Archive.EntryFilter)null, new ArchiveEntryFilter())));
         }
      }

      private List<Archive> getClassPathArchives(String path) throws Exception {
         String root = PropertiesLauncher.this.cleanupPath(PropertiesLauncher.this.handleUrl(path));
         List<Archive> lib = new ArrayList();
         File file = new File(root);
         if (!"/".equals(root)) {
            if (!this.isAbsolutePath(root)) {
               file = new File(PropertiesLauncher.this.home, root);
            }

            if (file.isDirectory()) {
               PropertiesLauncher.this.debug("Adding classpath entries from " + file);
               Archive archivex = new ExplodedArchive(file, false);
               lib.add(archivex);
            }
         }

         Archive archive = this.getArchive(file);
         if (archive != null) {
            PropertiesLauncher var10000 = PropertiesLauncher.this;
            URL var10001 = archive.getUrl();
            var10000.debug("Adding classpath entries from archive " + var10001 + root);
            lib.add(archive);
         }

         List<Archive> nestedArchives = this.getNestedArchives(root);
         if (nestedArchives != null) {
            PropertiesLauncher.this.debug("Adding classpath entries from nested " + root);
            lib.addAll(nestedArchives);
         }

         return lib;
      }

      private boolean isAbsolutePath(String root) {
         return root.contains(":") || root.startsWith("/");
      }

      private Archive getArchive(File file) throws IOException {
         if (this.isNestedArchivePath(file)) {
            return null;
         } else {
            String name = file.getName().toLowerCase(Locale.ENGLISH);
            return !name.endsWith(".jar") && !name.endsWith(".zip") ? null : this.getJarFileArchive(file);
         }
      }

      private boolean isNestedArchivePath(File file) {
         return file.getPath().contains(PropertiesLauncher.NESTED_ARCHIVE_SEPARATOR);
      }

      private List<Archive> getNestedArchives(String path) throws Exception {
         Archive parent = PropertiesLauncher.this.parent;
         String root = path;
         if ((path.equals("/") || !path.startsWith("/")) && !((Archive)parent).getUrl().toURI().equals(PropertiesLauncher.this.home.toURI())) {
            int index = path.indexOf(33);
            File file;
            if (index != -1) {
               file = new File(PropertiesLauncher.this.home, path.substring(0, index));
               if (path.startsWith("jar:file:")) {
                  file = new File(path.substring("jar:file:".length(), index));
               }

               parent = this.getJarFileArchive(file);

               for(root = path.substring(index + 1); root.startsWith("/"); root = root.substring(1)) {
               }
            }

            if (root.endsWith(".jar")) {
               file = new File(PropertiesLauncher.this.home, root);
               if (file.exists()) {
                  parent = this.getJarFileArchive(file);
                  root = "";
               }
            }

            if (root.equals("/") || root.equals("./") || root.equals(".")) {
               root = "";
            }

            Archive.EntryFilter filter = new PrefixMatchingArchiveFilter(root);
            List<Archive> archives = this.asList(((Archive)parent).getNestedArchives((Archive.EntryFilter)null, filter));
            if ((root == null || root.isEmpty() || ".".equals(root)) && !path.endsWith(".jar") && parent != PropertiesLauncher.this.parent) {
               archives.add(parent);
            }

            return archives;
         } else {
            return null;
         }
      }

      private void addNestedEntries() {
         try {
            Iterator<Archive> archives = PropertiesLauncher.this.parent.getNestedArchives((Archive.EntryFilter)null, JarLauncher.NESTED_ARCHIVE_ENTRY_FILTER);

            while(archives.hasNext()) {
               this.classPathArchives.add((Archive)archives.next());
            }
         } catch (IOException var2) {
         }

      }

      private List<Archive> asList(Iterator<Archive> iterator) {
         List<Archive> list = new ArrayList();

         while(iterator.hasNext()) {
            list.add((Archive)iterator.next());
         }

         return list;
      }

      private JarFileArchive getJarFileArchive(File file) throws IOException {
         JarFileArchive archive = new JarFileArchive(file);
         this.jarFileArchives.add(archive);
         return archive;
      }

      public Iterator<Archive> iterator() {
         return this.classPathArchives.iterator();
      }

      void close() throws IOException {
         Iterator var1 = this.jarFileArchives.iterator();

         while(var1.hasNext()) {
            JarFileArchive archive = (JarFileArchive)var1.next();
            archive.close();
         }

      }
   }

   private static final class ArchiveEntryFilter implements Archive.EntryFilter {
      private static final String DOT_JAR = ".jar";
      private static final String DOT_ZIP = ".zip";

      public boolean matches(Archive.Entry entry) {
         return entry.getName().endsWith(".jar") || entry.getName().endsWith(".zip");
      }
   }

   private static final class PrefixMatchingArchiveFilter implements Archive.EntryFilter {
      private final String prefix;
      private final ArchiveEntryFilter filter = new ArchiveEntryFilter();

      private PrefixMatchingArchiveFilter(String prefix) {
         this.prefix = prefix;
      }

      public boolean matches(Archive.Entry entry) {
         if (entry.isDirectory()) {
            return entry.getName().equals(this.prefix);
         } else {
            return entry.getName().startsWith(this.prefix) && this.filter.matches(entry);
         }
      }
   }
}
