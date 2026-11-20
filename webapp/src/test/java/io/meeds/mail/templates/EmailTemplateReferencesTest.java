/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.mail.templates;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * The notification templates reach the email classes of the portal by their
 * full names, and Groovy resolves a name only when a mail is rendered. This
 * test reads every template of the repository and resolves each name it uses
 * against the real class, and the static member that follows it, so that a
 * class or a method that moved or no longer exists fails the build instead of
 * failing the mail when it is sent.
 */
public class EmailTemplateReferencesTest {

  private static final Pattern REFERENCE = Pattern.compile("(io\\.meeds\\.portal\\.mail\\.(?:[a-z]+\\.)*[A-Z][A-Za-z0-9]*)(?:\\.([A-Za-z_][A-Za-z0-9_]*))?");

  @Test
  public void testTemplatesNameClassesAndMembersThatExist() throws IOException {
    List<Path> templates = templates();
    assertFalse("No template was found in the repository", templates.isEmpty());
    List<String> unresolved = new ArrayList<>();
    for (Path template : templates) {
      Matcher matcher = REFERENCE.matcher(Files.readString(template, StandardCharsets.UTF_8));
      while (matcher.find()) {
        String problem = resolve(matcher.group(1), matcher.group(2));
        if (problem != null) {
          unresolved.add(template.getFileName() + ": " + matcher.group() + " (" + problem + ")");
        }
      }
    }
    assertTrue("Templates name email classes or members that do not exist: " + unresolved, unresolved.isEmpty());
  }

  private static String resolve(String className, String member) {
    Class<?> type;
    try {
      type = Class.forName(className);
    } catch (ClassNotFoundException | LinkageError e) {
      return "no such class";
    }
    if (member == null) {
      return null;
    }
    for (java.lang.reflect.Method method : type.getMethods()) {
      if (method.getName().equals(member)) {
        return null;
      }
    }
    for (java.lang.reflect.Field field : type.getFields()) {
      if (field.getName().equals(member)) {
        return null;
      }
    }
    for (Class<?> nested : type.getClasses()) {
      if (nested.getSimpleName().equals(member)) {
        return null;
      }
    }
    return "no such public member";
  }

  private static List<Path> templates() throws IOException {
    Path root = Paths.get("").toAbsolutePath();
    while (root.getParent() != null && Files.exists(root.getParent().resolve("pom.xml"))) {
      root = root.getParent();
    }
    try (Stream<Path> files = Files.walk(root)) {
      return files.filter(file -> file.toString().endsWith(".gtmpl"))
                  .filter(file -> !file.toString().contains("/target/") && !file.toString().contains("/node_modules/"))
                  .toList();
    }
  }

}
