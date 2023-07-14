package org.springframework.boot.loader;

import org.springframework.boot.loader.archive.Archive;

public class JarLauncher extends ExecutableArchiveLauncher {
   static final Archive.EntryFilter NESTED_ARCHIVE_ENTRY_FILTER = (entry) -> {
      return entry.isDirectory() ? entry.getName().equals("BOOT-INF/classes/") : entry.getName().startsWith("BOOT-INF/lib/");
   };

   public JarLauncher() {
   }

   protected JarLauncher(Archive archive) {
      super(archive);
   }

   protected boolean isPostProcessingClassPathArchives() {
      return false;
   }

   protected boolean isNestedArchive(Archive.Entry entry) {
      return NESTED_ARCHIVE_ENTRY_FILTER.matches(entry);
   }

   protected String getArchiveEntryPathPrefix() {
      return "BOOT-INF/";
   }

   public static void main(String[] args) throws Exception {
      (new JarLauncher()).launch(args);
   }
}
