package org.springframework.boot.loader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;

public abstract class ExecutableArchiveLauncher extends Launcher {
   private static final String START_CLASS_ATTRIBUTE = "Start-Class";
   protected static final String BOOT_CLASSPATH_INDEX_ATTRIBUTE = "Spring-Boot-Classpath-Index";
   protected static final String DEFAULT_CLASSPATH_INDEX_FILE_NAME = "classpath.idx";
   private final Archive archive;
   private final ClassPathIndexFile classPathIndex;

   public ExecutableArchiveLauncher() {
      try {
         this.archive = this.createArchive();
         this.classPathIndex = this.getClassPathIndex(this.archive);
      } catch (Exception var2) {
         throw new IllegalStateException(var2);
      }
   }

   protected ExecutableArchiveLauncher(Archive archive) {
      try {
         this.archive = archive;
         this.classPathIndex = this.getClassPathIndex(this.archive);
      } catch (Exception var3) {
         throw new IllegalStateException(var3);
      }
   }

   protected ClassPathIndexFile getClassPathIndex(Archive archive) throws IOException {
      if (archive instanceof ExplodedArchive) {
         String location = this.getClassPathIndexFileLocation(archive);
         return ClassPathIndexFile.loadIfPossible(archive.getUrl(), location);
      } else {
         return null;
      }
   }

   private String getClassPathIndexFileLocation(Archive archive) throws IOException {
      Manifest manifest = archive.getManifest();
      Attributes attributes = manifest != null ? manifest.getMainAttributes() : null;
      String location = attributes != null ? attributes.getValue("Spring-Boot-Classpath-Index") : null;
      return location != null ? location : this.getArchiveEntryPathPrefix() + "classpath.idx";
   }

   protected String getMainClass() throws Exception {
      Manifest manifest = this.archive.getManifest();
      String mainClass = null;
      if (manifest != null) {
         mainClass = manifest.getMainAttributes().getValue("Start-Class");
      }

      if (mainClass == null) {
         throw new IllegalStateException("No 'Start-Class' manifest entry specified in " + this);
      } else {
         return mainClass;
      }
   }

   protected ClassLoader createClassLoader(Iterator<Archive> archives) throws Exception {
      List<URL> urls = new ArrayList(this.guessClassPathSize());

      while(archives.hasNext()) {
         urls.add(((Archive)archives.next()).getUrl());
      }

      if (this.classPathIndex != null) {
         urls.addAll(this.classPathIndex.getUrls());
      }

      return this.createClassLoader((URL[])urls.toArray(new URL[0]));
   }

   private int guessClassPathSize() {
      return this.classPathIndex != null ? this.classPathIndex.size() + 10 : 50;
   }

   protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
      Archive.EntryFilter searchFilter = this::isSearchCandidate;
      Iterator<Archive> archives = this.archive.getNestedArchives(searchFilter, (entry) -> {
         return this.isNestedArchive(entry) && !this.isEntryIndexed(entry);
      });
      if (this.isPostProcessingClassPathArchives()) {
         archives = this.applyClassPathArchivePostProcessing(archives);
      }

      return archives;
   }

   private boolean isEntryIndexed(Archive.Entry entry) {
      return this.classPathIndex != null ? this.classPathIndex.containsEntry(entry.getName()) : false;
   }

   private Iterator<Archive> applyClassPathArchivePostProcessing(Iterator<Archive> archives) throws Exception {
      List<Archive> list = new ArrayList();

      while(archives.hasNext()) {
         list.add((Archive)archives.next());
      }

      this.postProcessClassPathArchives(list);
      return list.iterator();
   }

   protected boolean isSearchCandidate(Archive.Entry entry) {
      return this.getArchiveEntryPathPrefix() == null ? true : entry.getName().startsWith(this.getArchiveEntryPathPrefix());
   }

   protected abstract boolean isNestedArchive(Archive.Entry entry);

   protected boolean isPostProcessingClassPathArchives() {
      return true;
   }

   protected void postProcessClassPathArchives(List<Archive> archives) throws Exception {
   }

   protected String getArchiveEntryPathPrefix() {
      return null;
   }

   protected boolean isExploded() {
      return this.archive.isExploded();
   }

   protected final Archive getArchive() {
      return this.archive;
   }
}
