package org.exoplatform.social.core;

public interface CacheEntry {

  default long getCacheTime() {
    return System.currentTimeMillis();
  }

  default void setCacheTime(long cacheTime) {
    // Nothing to do
  }

}
