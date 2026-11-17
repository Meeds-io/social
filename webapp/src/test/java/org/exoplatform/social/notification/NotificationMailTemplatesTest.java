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
package org.exoplatform.social.notification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import groovy.text.GStringTemplateEngine;

/**
 * The notification mail templates are written on the recipient's palette:
 * they compile with the engine the mail channel uses, and no palette colour
 * is left as a literal.
 */
class NotificationMailTemplatesTest {

  private static final Path    TEMPLATES     = Paths.get("src/main/webapp/WEB-INF/notification/templates");

  // the light literals the palette replaces; white on a coloured button stays as written, and the
  // footer label's link colour is replaced where the label is printed
  private static final Pattern PALETTE_LITERAL = Pattern.compile("(?i)(bgcolor=\"|background-color:\\s*)#(ffffff|f9f9f9|f1f1f1|567ab6)"
      + "|(?<![a-z-])(?<!replace\\(\"color: )color:\\s*#(333333|999999)|(?<!replace\\(\"color: )#2f5e92|border[a-z-]*:\\s*\\d+px solid\\s*#(d8d8d8|224886)"
      // the blue gradient of the former buttons, which would hide the theme's primary colour
      + "|#(638acd|426393)"
      // the light frame of the avatars
      + "|#c7c7c7");

  private static final Pattern FOOTER_WITHOUT_PALETTE = Pattern.compile("appRes\\(\"Notification\\.label\\.footer\",[^)]*\\)(?!\\.replace\\(\"color: #2f5e92;\", \"color: \\$\\{palette\\.link\\};\"\\))");

  @Test
  void everyTemplateCompilesAndReadsThePalette() throws Exception {
    List<Path> templates = templates();
    assertFalse(templates.isEmpty());
    for (Path template : templates) {
      String text = Files.readString(template, StandardCharsets.UTF_8);
      new GStringTemplateEngine().createTemplate(text);
      assertFalse(PALETTE_LITERAL.matcher(text).find(), template + " keeps a palette colour as a literal");
      // the footer label carries its link colour in every translation: each print takes the palette's
      assertFalse(FOOTER_WITHOUT_PALETTE.matcher(text).find(), template + " prints the footer label with its literal link colour");
      if (text.contains("${palette.")) {
        assertTrue(text.contains("MailPalette.of(_templateContext)"), template + " reads the palette");
      }
    }
  }

  private List<Path> templates() throws IOException {
    try (Stream<Path> files = Files.walk(TEMPLATES)) {
      return files.filter(path -> path.toString().endsWith(".gtmpl")).toList();
    }
  }

}
