/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.storage.impl.StorageUtils;

import junit.framework.TestCase;

public class StorageUtilsTest extends TestCase {

  public void testSubList() throws Exception {
    List<String> list = new ArrayList<String>();
    
    for (int i = 0; i < 20; i++) {
      list.add(""+i);
    }
    
    List<String> loaded = StorageUtils.subList(list, 0, 10);
    
    assertEquals(10, loaded.size());
    
    loaded = StorageUtils.subList(list, 0, 25);
    
    assertEquals(20, loaded.size());
    
    loaded = StorageUtils.subList(list, 19 , 25);
    
    assertEquals(1, loaded.size());
    
    loaded = StorageUtils.subList(list, 10, 25);
    
    assertEquals(10, loaded.size());
    
    loaded = StorageUtils.subList(list, 15 , 15);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 20 , 25);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 25 , 10);
    
    assertEquals(0, loaded.size());
    
    loaded = StorageUtils.subList(list, 25 , 30);
    
    assertEquals(0, loaded.size());
    
  }

  /**
   * Only the Latin combining diacritics are removed (what the index analyzers'
   * asciifolding does, which the ngram fields' search analyzer lacks); every
   * other character stays, since the field's search analyzer tokenizes the
   * text the way the index was tokenized.
   */
  public void testNormalizeSearchText() {
    assertNull(StorageUtils.normalizeSearchText(null));
    assertEquals("", StorageUtils.normalizeSearchText(" \t"));
    assertEquals("\"", StorageUtils.normalizeSearchText("\""));
    assertEquals("test\\", StorageUtils.normalizeSearchText("test\\"));
    assertEquals("a/b", StorageUtils.normalizeSearchText("a/b"));
    assertEquals("@limit@", StorageUtils.normalizeSearchText("@limit@"));
    assertEquals("test-tes", StorageUtils.normalizeSearchText("t\u00e9st-t\u00e9s"));
    assertEquals("Cafe CREME", StorageUtils.normalizeSearchText("Caf\u00e9 CR\u00c8ME"));
    assertEquals("l'activite", StorageUtils.normalizeSearchText("l'activit\u00e9"));
    assertEquals("www.meeds.io test_test 3.14", StorageUtils.normalizeSearchText(" www.meeds.io test_test 3.14 "));
    assertEquals("a\tb\r\nc\u3000d", StorageUtils.normalizeSearchText("a\tb\r\nc\u3000d"));
    // Devanagari vowel signs are combining marks of their own block, kept
    assertEquals("\u0939\u093f\u0928\u094d\u0926\u0940", StorageUtils.normalizeSearchText("\u0939\u093f\u0928\u094d\u0926\u0940"));
    // a Hangul syllable and a kana with a sound mark are decomposed by NFD and recomposed
    assertEquals("\ud55c\uad6d\uc5b4", StorageUtils.normalizeSearchText("\ud55c\uad6d\uc5b4"));
    assertEquals("\u304c\u3063\u3053\u3046", StorageUtils.normalizeSearchText("\u304c\u3063\u3053\u3046"));
    assertEquals("\ud55c\uad6d\uc5b4 test", StorageUtils.normalizeSearchText("\ud55c\uad6d\uc5b4 t\u00e9st"));
  }

  public void testEscapeSearchText() {
    assertNull(StorageUtils.escapeSearchText(null));
    assertEquals("test\\\"", StorageUtils.escapeSearchText("t\u00e9st\""));
    assertEquals("a\\\\b", StorageUtils.escapeSearchText("a\\b"));
  }

  public void testEscapeJsonValue() {
    assertNull(StorageUtils.escapeJsonValue(null));
    assertEquals("", StorageUtils.escapeJsonValue(""));
    assertEquals("release\\\"", StorageUtils.escapeJsonValue("release\""));
    assertEquals("a\\\\b", StorageUtils.escapeJsonValue("a\\b"));
    assertEquals("a\\tb\\nc", StorageUtils.escapeJsonValue("a\tb\nc"));
    assertEquals("a\\/b", StorageUtils.escapeJsonValue("a/b"));
  }

  /**
   * One pass: a placeholder name inside a value is written as is, a
   * placeholder with no value is removed.
   */
  public void testFillQueryTemplate() {
    String template = "{\"size\": @limit@, \"q\": \"@term@\", @optional@ \"x\": \"@term@\"}";
    String filled = StorageUtils.fillQueryTemplate(template, Map.of("limit", "10", "term", "@limit@ $1 \\"));
    assertEquals("{\"size\": 10, \"q\": \"@limit@ $1 \\\",  \"x\": \"@limit@ $1 \\\"}", filled);
    assertEquals("no placeholder", StorageUtils.fillQueryTemplate("no placeholder", Map.of()));
    assertEquals("", StorageUtils.fillQueryTemplate("", Map.of("a", "b")));
  }

}
