/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.oauth.utils;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.UserProfile;

/**
 * Various utils method related to persistence
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OAuthPersistenceUtils {

  public static final int DEFAULT_CHUNK_LENGTH = 250;

  // Private constructor for utils class
  private OAuthPersistenceUtils() {
  }

  /**
   * Save some potentially long attribute (For example OAuth accessToken) into
   * given UserProfile. The point is that attribute needs to be divided and
   * saved into more "children" attributes (chunks) of userProfile because it's
   * too long.
   *
   * @param longAttribute             long attribute to save
   * @param userProfile               user profile where attribute will be saved
   * @param attributePrefix           prefix to save. For example if prefix is
   *                                    "myPrefix" then longAttribute will be
   *                                    saved into chunks (attributes) like
   *                                    "myPrefix.1", "myPrefix.2", ... etc.
   * @param useSuffixInFirstAttribute Whether to use suffix in very first
   *                                    attribute. If true, the first attribute
   *                                    will be "myPrefix.1". If false, it will
   *                                    be just "myPrefix". The only point of
   *                                    this parameter is backwards
   *                                    compatibility
   * @param lengthOfOneChunk          length of single chunk
   */
  public static void saveLongAttribute(String longAttribute, UserProfile userProfile, String attributePrefix,
                                       boolean useSuffixInFirstAttribute, int lengthOfOneChunk) {
    List<String> chunks = new ArrayList<String>();
    while (longAttribute.length() > lengthOfOneChunk) {
      chunks.add(longAttribute.substring(0, lengthOfOneChunk));
      longAttribute = longAttribute.substring(lengthOfOneChunk);
    }
    chunks.add(longAttribute);

    int chunkCounter = 1;
    for (String chunk : chunks) {
      String chunkAttrName = attributePrefix;
      if (useSuffixInFirstAttribute || chunkCounter > 1) {
        chunkAttrName = chunkAttrName + "." + chunkCounter;
      }

      userProfile.setAttribute(chunkAttrName, chunk);
      chunkCounter++;
    }

    // Null existing chunks if previous attribute was much longer than new one
    boolean chunkRemoved;
    do {
      chunkRemoved = false;
      String chunkAttrName = attributePrefix;
      if (useSuffixInFirstAttribute || chunkCounter > 1) {
        chunkAttrName = chunkAttrName + "." + chunkCounter;
      }

      if (userProfile.getAttribute(chunkAttrName) != null) {
        userProfile.setAttribute(chunkAttrName, null);
        chunkRemoved = true;
      }
      chunkCounter++;
    } while (chunkRemoved);
  }

  /**
   * Remove very long attribute. For parameters description see
   * {@link #saveLongAttribute(String, UserProfile, String, boolean, int)}
   */
  public static void removeLongAttribute(UserProfile userProfile, String attributePrefix, boolean useSuffixInFirstAttribute) {
    boolean chunkRemoved;
    int chunkCounter = 1;
    do {
      chunkRemoved = false;
      String chunkAttrName = attributePrefix;
      if (useSuffixInFirstAttribute || chunkCounter > 1) {
        chunkAttrName = chunkAttrName + "." + chunkCounter;
      }

      if (userProfile.getAttribute(chunkAttrName) != null) {
        userProfile.setAttribute(chunkAttrName, null);
        chunkRemoved = true;
      }
      chunkCounter++;
    } while (chunkRemoved);
  }

  /**
   * Get very long attribute. For parameters description see
   * {@link #saveLongAttribute(String, UserProfile, String, boolean, int)}
   * 
   * @return very long attribute from all chunks
   */
  public static String getLongAttribute(UserProfile userProfile, String attributePrefix, boolean useSuffixInFirstAttribute) {
    boolean chunkFound;
    int chunkCounter = 1;
    StringBuilder result = new StringBuilder();
    do {
      chunkFound = false;
      String chunkAttrName = attributePrefix;
      if (useSuffixInFirstAttribute || chunkCounter > 1) {
        chunkAttrName = chunkAttrName + "." + chunkCounter;
      }

      if (userProfile.getAttribute(chunkAttrName) != null) {
        result.append(userProfile.getAttribute(chunkAttrName));
        chunkFound = true;
      }
      chunkCounter++;
    } while (chunkFound);

    String str = result.toString();
    return (str.length() > 0) ? str : null;
  }

  /**
   * Parse chunkLength from value params (same code used in all processors)
   */
  public static int getChunkLength(InitParams params) {
    String chunkLengthParam = null;
    if (params.getValueParam("chunkLength") != null) {
      chunkLengthParam = params.getValueParam("chunkLength").getValue();
    }
    if (chunkLengthParam != null && !chunkLengthParam.isEmpty()) {
      return Integer.parseInt(chunkLengthParam);
    } else {
      return OAuthPersistenceUtils.DEFAULT_CHUNK_LENGTH;
    }
  }
}
