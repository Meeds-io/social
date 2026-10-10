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
package org.exoplatform.social.core.storage.impl;

import java.text.Normalizer;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONValue;

public class StorageUtils {

  public static final String            ASTERISK_STR       = "*";

  public static final String            EMPTY_STR          = "";

  public static final DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
                                                                              .withResolverStyle(ResolverStyle.LENIENT);

  private static final Pattern          PLACEHOLDER_PATTERN   = Pattern.compile("@[a-zA-Z_]+@");

  private static final Pattern          DIACRITICS_PATTERN    = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

  /**
   * Gets sub list from the provided list with start and end index.
   * 
   * @param list the identity list
   * @param startIndex start index to get
   * @param toIndex end index to get
   * @return sub list of the provided list
   */
  public static <T> List<T> subList(List<T> list, int startIndex, int toIndex) {
    int totalSize = list.size();

    if (startIndex >= totalSize)
      return Collections.emptyList();

    //
    if (toIndex >= totalSize) {
      toIndex = totalSize;
    }

    return list.subList(startIndex, toIndex);
  }

  public static String toRFC3339Date(Date dateTime) {
    if (dateTime == null) {
      return null;
    }
    ZonedDateTime zonedDateTime = dateTime.toInstant().atZone(ZoneOffset.UTC);
    return zonedDateTime.format(RFC_3339_FORMATTER);
  }

  public static Date parseRFC3339Date(String dateString) {
    if (StringUtils.isBlank(dateString)) {
      return null;
    }
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, RFC_3339_FORMATTER);
    return Date.from(zonedDateTime.toInstant());
  }

  /**
   * Prepares a searched text for an analyzed Elasticsearch query (a match
   * query): the Latin combining diacritics are removed, as the index analyzers'
   * asciifolding does, because the ngram fields' search analyzer (space display
   * name, category name) folds nothing. The text is decomposed (NFD) to find
   * them and recomposed (NFC) afterwards, so that a Hangul syllable or a kana
   * with a sound mark, which NFD also decomposes, is searched as typed. Nothing
   * else is changed: the field's search analyzer splits the text into the
   * tokens the index holds, and a text with no token matches nothing.
   *
   * @param text searched text, as typed
   * @return the text to search, trimmed; {@code null} when the text is null
   */
  public static String normalizeSearchText(String text) {
    if (text == null) {
      return null;
    }
    String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
    String stripped = DIACRITICS_PATTERN.matcher(decomposed).replaceAll("");
    return Normalizer.normalize(stripped, Normalizer.Form.NFC).trim();
  }

  /**
   * Escapes a value written between the quotes of a JSON string of a query
   * built as text, so that a quote, a backslash or a control character in the
   * value reaches Elasticsearch as that character.
   *
   * @param value value to write
   * @return the escaped value, {@code null} when the value is null
   */
  public static String escapeJsonValue(String value) {
    return value == null ? null : JSONValue.escape(value);
  }

  /**
   * Escapes a searched text for the JSON string of an analyzed query:
   * {@link #normalizeSearchText(String)} then {@link #escapeJsonValue(String)}.
   *
   * @param text searched text, as typed
   * @return the text to write between the quotes of the query JSON string
   */
  public static String escapeSearchText(String text) {
    return escapeJsonValue(normalizeSearchText(text));
  }

  /**
   * Fills a query template in one pass: each {@code @name@} placeholder of the
   * template is replaced by its value, and a placeholder's name found inside a
   * value, such as a searched {@code @limit@}, is written as is. A placeholder
   * the values do not name is replaced by an empty string.
   *
   * @param template query template holding {@code @name@} placeholders
   * @param values value of each placeholder, by name without the {@code @}
   * @return the filled query
   */
  public static String fillQueryTemplate(String template, Map<String, String> values) {
    Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
    StringBuilder query = new StringBuilder(template.length());
    while (matcher.find()) {
      String name = matcher.group();
      String value = values.get(name.substring(1, name.length() - 1));
      matcher.appendReplacement(query, Matcher.quoteReplacement(value == null ? EMPTY_STR : value));
    }
    matcher.appendTail(query);
    return query.toString();
  }
}
