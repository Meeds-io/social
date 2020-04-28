/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.common.xmlprocessor;

import java.util.List;

import junit.framework.TestCase;

/**
 * Unit Test for {@link Tokenizer}.
 */
public class TokenizerTest extends TestCase {

  public void testTokenize() {
    List<String> result;

    result = Tokenizer.tokenize("");
    assertEquals(0, result.size());

    result = Tokenizer.tokenize("hello");
    assertEquals(1, result.size());
    assertEquals("hello", result.get(0));

    result = Tokenizer.tokenize("a < asd >");
    assertEquals(2, result.size());
    assertEquals("a ", result.get(0));
    assertEquals("< asd >", result.get(1));

    result = Tokenizer.tokenize("<a> a </a>");
    assertEquals(3, result.size());
    assertEquals("<a>", result.get(0));
    assertEquals(" a ", result.get(1));
    assertEquals("</a>", result.get(2));

    result = Tokenizer.tokenize("<a href=\"hello\"> a </a>");
    assertEquals(3, result.size());
    assertEquals("<a href=\"hello\">", result.get(0));
    assertEquals(" a ", result.get(1));
    assertEquals("</a>", result.get(2));

    result = Tokenizer.tokenize("<a href='hello'> a </a>");
    assertEquals(3, result.size());
    assertEquals("<a href='hello'>", result.get(0));
    assertEquals(" a ", result.get(1));
    assertEquals("</a>", result.get(2));
  }

}
