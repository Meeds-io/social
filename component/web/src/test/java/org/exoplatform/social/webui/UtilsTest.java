/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.webui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONTokener;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The values the portal head script receives: the preview space id is a
 * number or nothing, and every string is a literal that evaluates to the value
 * and cannot end the script block it is printed in.
 */
class UtilsTest {

  @Test
  void previewSpaceIdIsReadFromAsciiDigitsOnly() {
    assertEquals(42L, Utils.getPreviewSpaceId(requestWith("42")));
    assertEquals(7L, Utils.getPreviewSpaceId(requestWith("007")));
    assertEquals(Long.MAX_VALUE, Utils.getPreviewSpaceId(requestWith(String.valueOf(Long.MAX_VALUE))));
  }

  @Test
  void previewSpaceIdIsNullWhenAbsentOrEmpty() {
    assertNull(Utils.getPreviewSpaceId(requestWith(null)));
    assertNull(Utils.getPreviewSpaceId(requestWith("")));
  }

  @Test
  void previewSpaceIdIsNullForAnyNonDigitCharacter() {
    assertNull(Utils.getPreviewSpaceId(requestWith(" 42")));
    assertNull(Utils.getPreviewSpaceId(requestWith("42 ")));
    assertNull(Utils.getPreviewSpaceId(requestWith("+42")));
    assertNull(Utils.getPreviewSpaceId(requestWith("-42")));
    assertNull(Utils.getPreviewSpaceId(requestWith("1e3")));
    assertNull(Utils.getPreviewSpaceId(requestWith("42abc")));
  }

  @Test
  void previewSpaceIdIsNullForNonAsciiDigits() {
    // Arabic-Indic digits: Character.isDigit and Long.parseLong accept them
    assertNull(Utils.getPreviewSpaceId(requestWith("\u0664\u0662")));
  }

  @Test
  void previewSpaceIdIsNullWhenItOverflowsALong() {
    assertNull(Utils.getPreviewSpaceId(requestWith("9223372036854775808")));
  }

  @Test
  void scriptStringEvaluatesToTheValue() {
    assertScriptString("home");
    assertScriptString("");
    assertScriptString("a\"b\\c'd");
    assertScriptString("x</script><!--y");
    assertScriptString("line\u2028separator\nnew line");
    assertEquals("\"null\"", Utils.toScriptString(null));
    assertEquals("\"42\"", Utils.toScriptString(42L));
  }

  @Test
  void scriptStringHoldsNoLessThanSign() {
    String literal = Utils.toScriptString("x</script><!--<script>y");
    assertFalse(literal.contains("<"), literal);
  }

  @Test
  void scriptArrayHoldsEachValueAsAScriptString() {
    assertEquals("[]", Utils.toScriptArray(List.of()));
    List<String> values = List.of("activity", "x\"y</script><!--z", "");
    String literal = Utils.toScriptArray(values);
    JSONTokener tokener = new JSONTokener(literal);
    assertEquals(values, ((JSONArray) tokener.nextValue()).toList(), literal);
    assertFalse(tokener.more(), literal);
    assertFalse(literal.contains("<"), literal);
  }

  private void assertScriptString(String value) {
    String literal = Utils.toScriptString(value);
    assertEquals('"', literal.charAt(0), literal);
    assertEquals('"', literal.charAt(literal.length() - 1), literal);
    JSONTokener tokener = new JSONTokener(literal);
    assertEquals(value, tokener.nextValue(), literal);
    assertFalse(tokener.more(), literal);
    assertFalse(literal.contains("<"), literal);
    assertFalse(literal.contains("\u2028"), literal);
  }

  private HttpServletRequest requestWith(String previewSpaceId) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter(Utils.PREVIEW_SPACE_ID_PARAM)).thenReturn(previewSpaceId);
    return request;
  }
}
