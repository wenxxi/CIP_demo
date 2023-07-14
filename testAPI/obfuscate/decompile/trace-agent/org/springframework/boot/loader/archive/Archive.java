package org.springframework.boot.loader.archive;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.Manifest;

public interface Archive extends Iterable<Entry>, AutoCloseable {
   URL getUrl() throws MalformedURLException;

   Manifest getManifest() throws IOException;

   Iterator<Archive> getNestedArchives(EntryFilter searchFilter, EntryFilter includeFilter) throws IOException;

   default boolean isExploded() {
      return false;
   }

   default void close() throws Exception {
   }

   @FunctionalInterface
   public interface EntryFilter {
      boolean matches(Entry entry);
   }

   public interface Entry {
      boolean isDirectory();

      String getName();
   }
}
