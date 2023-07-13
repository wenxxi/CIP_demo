package org.springframework.boot.loader.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.Manifest;

public class ExplodedArchive implements Archive {
   private static final Set<String> SKIPPED_NAMES = new HashSet(Arrays.asList(".", ".."));
   private final File root;
   private final boolean recursive;
   private final File manifestFile;
   private Manifest manifest;

   public ExplodedArchive(File root) {
      this(root, true);
   }

   public ExplodedArchive(File root, boolean recursive) {
      if (root.exists() && root.isDirectory()) {
         this.root = root;
         this.recursive = recursive;
         this.manifestFile = this.getManifestFile(root);
      } else {
         throw new IllegalArgumentException("Invalid source directory " + root);
      }
   }

   private File getManifestFile(File root) {
      File metaInf = new File(root, "META-INF");
      return new File(metaInf, "MANIFEST.MF");
   }

   public URL getUrl() throws MalformedURLException {
      return this.root.toURI().toURL();
   }

   public Manifest getManifest() throws IOException {
      if (this.manifest == null && this.manifestFile.exists()) {
         FileInputStream inputStream = new FileInputStream(this.manifestFile);

         try {
            this.manifest = new Manifest(inputStream);
         } catch (Throwable var5) {
            try {
               inputStream.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         inputStream.close();
      }

      return this.manifest;
   }

   public Iterator<Archive> getNestedArchives(Archive.EntryFilter searchFilter, Archive.EntryFilter includeFilter) throws IOException {
      return new ArchiveIterator(this.root, this.recursive, searchFilter, includeFilter);
   }

   /** @deprecated */
   @Deprecated(
      since = "2.3.10",
      forRemoval = false
   )
   public Iterator<Archive.Entry> iterator() {
      return new EntryIterator(this.root, this.recursive, (Archive.EntryFilter)null, (Archive.EntryFilter)null);
   }

   protected Archive getNestedArchive(Archive.Entry entry) {
      File file = ((FileEntry)entry).getFile();
      return (Archive)(file.isDirectory() ? new ExplodedArchive(file) : new SimpleJarFileArchive((FileEntry)entry));
   }

   public boolean isExploded() {
      return true;
   }

   public String toString() {
      try {
         return this.getUrl().toString();
      } catch (Exception var2) {
         return "exploded archive";
      }
   }

   private static class ArchiveIterator extends AbstractIterator<Archive> {
      ArchiveIterator(File root, boolean recursive, Archive.EntryFilter searchFilter, Archive.EntryFilter includeFilter) {
         super(root, recursive, searchFilter, includeFilter);
      }

      protected Archive adapt(FileEntry entry) {
         File file = entry.getFile();
         return (Archive)(file.isDirectory() ? new ExplodedArchive(file) : new SimpleJarFileArchive(entry));
      }
   }

   private static class EntryIterator extends AbstractIterator<Archive.Entry> {
      EntryIterator(File root, boolean recursive, Archive.EntryFilter searchFilter, Archive.EntryFilter includeFilter) {
         super(root, recursive, searchFilter, includeFilter);
      }

      protected Archive.Entry adapt(FileEntry entry) {
         return entry;
      }
   }

   private static class FileEntry implements Archive.Entry {
      private final String name;
      private final File file;
      private final URL url;

      FileEntry(String name, File file, URL url) {
         this.name = name;
         this.file = file;
         this.url = url;
      }

      File getFile() {
         return this.file;
      }

      public boolean isDirectory() {
         return this.file.isDirectory();
      }

      public String getName() {
         return this.name;
      }

      URL getUrl() {
         return this.url;
      }
   }

   private static class SimpleJarFileArchive implements Archive {
      private final URL url;

      SimpleJarFileArchive(FileEntry file) {
         this.url = file.getUrl();
      }

      public URL getUrl() throws MalformedURLException {
         return this.url;
      }

      public Manifest getManifest() throws IOException {
         return null;
      }

      public Iterator<Archive> getNestedArchives(Archive.EntryFilter searchFilter, Archive.EntryFilter includeFilter) throws IOException {
         return Collections.emptyIterator();
      }

      /** @deprecated */
      @Deprecated(
         since = "2.3.10",
         forRemoval = false
      )
      public Iterator<Archive.Entry> iterator() {
         return Collections.emptyIterator();
      }

      public String toString() {
         try {
            return this.getUrl().toString();
         } catch (Exception var2) {
            return "jar archive";
         }
      }
   }

   private abstract static class AbstractIterator<T> implements Iterator<T> {
      private static final Comparator<File> entryComparator = Comparator.comparing(File::getAbsolutePath);
      private final File root;
      private final boolean recursive;
      private final Archive.EntryFilter searchFilter;
      private final Archive.EntryFilter includeFilter;
      private final Deque<Iterator<File>> stack = new LinkedList();
      private FileEntry current;
      private final String rootUrl;

      AbstractIterator(File root, boolean recursive, Archive.EntryFilter searchFilter, Archive.EntryFilter includeFilter) {
         this.root = root;
         this.rootUrl = this.root.toURI().getPath();
         this.recursive = recursive;
         this.searchFilter = searchFilter;
         this.includeFilter = includeFilter;
         this.stack.add(this.listFiles(root));
         this.current = this.poll();
      }

      public boolean hasNext() {
         return this.current != null;
      }

      public T next() {
         FileEntry entry = this.current;
         if (entry == null) {
            throw new NoSuchElementException();
         } else {
            this.current = this.poll();
            return this.adapt(entry);
         }
      }

      private FileEntry poll() {
         label32:
         while(!this.stack.isEmpty()) {
            FileEntry entry;
            do {
               File file;
               do {
                  if (!((Iterator)this.stack.peek()).hasNext()) {
                     this.stack.poll();
                     continue label32;
                  }

                  file = (File)((Iterator)this.stack.peek()).next();
               } while(ExplodedArchive.SKIPPED_NAMES.contains(file.getName()));

               entry = this.getFileEntry(file);
               if (this.isListable(entry)) {
                  this.stack.addFirst(this.listFiles(file));
               }
            } while(this.includeFilter != null && !this.includeFilter.matches(entry));

            return entry;
         }

         return null;
      }

      private FileEntry getFileEntry(File file) {
         URI uri = file.toURI();
         String name = uri.getPath().substring(this.rootUrl.length());

         try {
            return new FileEntry(name, file, uri.toURL());
         } catch (MalformedURLException var5) {
            throw new IllegalStateException(var5);
         }
      }

      private boolean isListable(FileEntry entry) {
         return entry.isDirectory() && (this.recursive || entry.getFile().getParentFile().equals(this.root)) && (this.searchFilter == null || this.searchFilter.matches(entry)) && (this.includeFilter == null || !this.includeFilter.matches(entry));
      }

      private Iterator<File> listFiles(File file) {
         File[] files = file.listFiles();
         if (files == null) {
            return Collections.emptyIterator();
         } else {
            Arrays.sort(files, entryComparator);
            return Arrays.asList(files).iterator();
         }
      }

      public void remove() {
         throw new UnsupportedOperationException("remove");
      }

      protected abstract T adapt(FileEntry entry);
   }
}
