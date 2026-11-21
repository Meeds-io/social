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
package io.meeds.social.portlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class TopbarLogoJspTest {

  private static final Path        JSP              = Path.of("src/main/webapp/WEB-INF/jsp/portlet/topbarLogo.jsp");

  // the values that are a boolean or a number, printed as they are
  private static final Set<String> UNESCAPED_VALUES = Set.of("isFavorite",
                                                             "muted",
                                                             "isMember",
                                                             "membersNumber",
                                                             "canRedactOnSpace",
                                                             "displayMobileCompanyLogo",
                                                             "displayCompanyName",
                                                             "displaySiteName",
                                                             "isSitePage");

  private static final Pattern     EXPRESSION       = Pattern.compile("<%=(.*?)%>", Pattern.DOTALL);

  @Test
  void everyTextValueOfTheTopbarScriptIsEscaped() throws Exception {
    String script = script();
    List<String> expressions = new ArrayList<>();
    Matcher matcher = EXPRESSION.matcher(script);
    while (matcher.find()) {
      expressions.add(matcher.group(1).strip());
    }
    assertFalse(expressions.isEmpty());
    for (String expression : expressions) {
      assertTrue(UNESCAPED_VALUES.contains(expression) || expression.startsWith("js(") && expression.endsWith(")"),
                 "printed without escaping: " + expression);
    }
  }

  @Test
  void theEscaperWritesTheQuotesOfEachTextValue() throws Exception {
    String script = script();
    // a quote around a print would turn the literal js() writes back into code
    assertFalse(script.contains("'<%=js("), "a text value between single quotes");
    assertFalse(script.contains("\"<%=js("), "a text value between double quotes");
    assertEquals(-1, script.indexOf('`'));
  }

  @Test
  void theEscaperIsTheSharedScriptStringEscaper() throws Exception {
    String jsp = Files.readString(JSP, StandardCharsets.UTF_8);
    assertTrue(jsp.contains("return Utils.toScriptString(value == null ? \"\" : value);"));
  }

  private static String script() throws Exception {
    String jsp = Files.readString(JSP, StandardCharsets.UTF_8);
    int start = jsp.indexOf("<script type=\"text/javascript\">");
    int end = jsp.indexOf("</script>", start);
    assertTrue(start >= 0 && end > start);
    return jsp.substring(start, end);
  }

}
