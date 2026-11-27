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
package org.exoplatform.social.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.meeds.portal.theme.constant.TextLevel;
import io.meeds.portal.theme.util.ThemeColorCalculator;

/**
 * The theme editor's live contrast feedback (styling-inputs
 * {@code ColorContrast.js}) is a copy of the server's calculator, which the
 * stylesheet and the mails use: the constants they share are read from the
 * script and compared with {@link ThemeColorCalculator}, and the script's
 * luminance, contrast ratio and Auto choice, evaluated with the constants and
 * the comparison read from it, give the server's ratio and Auto text colour on
 * the platform's backgrounds.
 */
class ColorContrastTest {

  private static final List<String> BACKGROUNDS = List.of(ThemeColorCalculator.WHITE,
                                                          ThemeColorCalculator.LIGHT_PAGE_BACKGROUND,
                                                          ThemeColorCalculator.DARK_PAGE_BACKGROUND,
                                                          ThemeColorCalculator.DARK_APP_BACKGROUND,
                                                          ThemeColorCalculator.DARK_DRAWER_BACKGROUND,
                                                          "#3F8487",
                                                          "#476A9C",
                                                          "#777777",
                                                          "#FFCC00",
                                                          ThemeColorCalculator.BLACK);

  private static String             script;

  private static double[]           weights;

  private static double[]           linearisation;

  private static double[]           ratioOffsets;

  private static String             autoComparison;

  @BeforeAll
  static void readScript() throws IOException {
    script = Files.readString(Paths.get("src/main/webapp/vue-apps/styling-inputs/js/ColorContrast.js"), StandardCharsets.UTF_8);
    weights = numbers("return ([0-9.]+) \\* r \\+ ([0-9.]+) \\* g \\+ ([0-9.]+) \\* b;");
    linearisation = numbers("channel <= ([0-9.]+) \\? channel / ([0-9.]+) : Math\\.pow\\(\\(channel \\+ ([0-9.]+)\\) / ([0-9.]+), ([0-9.]+)\\)");
    ratioOffsets = numbers("return \\(Math\\.max\\(first, second\\) \\+ ([0-9.]+)\\) / \\(Math\\.min\\(first, second\\) \\+ ([0-9.]+)\\);");
    Matcher auto = Pattern.compile("return contrastRatio\\(first, background\\) (>=|>|<=|<) contrastRatio\\(second, background\\) \\? first : second;")
                          .matcher(script);
    assertTrue(auto.find(), "ColorContrast.js no longer chooses the Auto text by comparing the two contrast ratios");
    autoComparison = auto.group(1);
  }

  @Test
  void anAutoTieKeepsTheLightColourAsTheServerDoes() {
    // ThemeColorCalculator#autoText: contrast(light) >= contrast(dark) ? light : dark
    assertEquals(">=", autoComparison, "the script's Auto choice on a tie");
  }

  @Test
  void theReadableRatioIsTheServers() {
    assertEquals(ThemeColorCalculator.READABLE_RATIO, numbers("export const READABLE_RATIO = ([0-9.]+);")[0]);
  }

  @Test
  void theTextLevelsAreTheServersDefaults() {
    for (TextLevel level : TextLevel.values()) {
      Matcher matcher = Pattern.compile(level.name().toLowerCase() + ": \\['(#[0-9A-Fa-f]{6})', '(#[0-9A-Fa-f]{6})'\\]").matcher(script);
      assertTrue(matcher.find(), "ColorContrast.js declares the " + level + " text level");
      assertEquals(level.getLightDefault(), matcher.group(1), level + " light default");
      assertEquals(level.getDarkDefault(), matcher.group(2), level + " dark default");
    }
  }

  @Test
  void theScriptsFormulaGivesTheServersRatioAndAutoText() {
    for (String background : BACKGROUNDS) {
      for (TextLevel level : TextLevel.values()) {
        for (String text : List.of(level.getLightDefault(), level.getDarkDefault())) {
          assertEquals(ThemeColorCalculator.contrast(text, background),
                       contrast(text, background),
                       1e-9,
                       text + " on " + background);
        }
        String light = level.getLightDefault();
        String dark = level.getDarkDefault();
        String scriptAuto = compare(contrast(light, background), contrast(dark, background)) ? light : dark;
        assertEquals(ThemeColorCalculator.autoText(level, background), scriptAuto, level + " Auto text on " + background);
      }
    }
  }

  private static double contrast(String first, String second) {
    double l1 = luminance(first);
    double l2 = luminance(second);
    return (Math.max(l1, l2) + ratioOffsets[0]) / (Math.min(l1, l2) + ratioOffsets[1]);
  }

  private static boolean compare(double firstRatio, double secondRatio) {
    return switch (autoComparison) {
    case ">=" -> firstRatio >= secondRatio;
    case ">" -> firstRatio > secondRatio;
    case "<=" -> firstRatio <= secondRatio;
    default -> firstRatio < secondRatio;
    };
  }

  private static double luminance(String color) {
    double luminance = 0;
    for (int channel = 0; channel < 3; channel++) {
      double value = Integer.parseInt(color.substring(1 + channel * 2, 3 + channel * 2), 16) / 255d;
      double linear = value <= linearisation[0] ? value / linearisation[1]
                                                : Math.pow((value + linearisation[2]) / linearisation[3], linearisation[4]);
      luminance += weights[channel] * linear;
    }
    return luminance;
  }

  private static double[] numbers(String regex) {
    Matcher matcher = Pattern.compile(regex).matcher(script);
    assertTrue(matcher.find(), "ColorContrast.js no longer matches " + regex);
    double[] values = new double[matcher.groupCount()];
    for (int group = 1; group <= matcher.groupCount(); group++) {
      values[group - 1] = Double.parseDouble(matcher.group(group));
    }
    return values;
  }

}
