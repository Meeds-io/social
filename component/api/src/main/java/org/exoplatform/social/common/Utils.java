package org.exoplatform.social.common;

import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

public class Utils {

  private static final String         SIZE_SPLIT_CHAR   = "x";

  private Utils() {
    // static class, no need to constructor, thus it's 'private'
  }

  /**
   * As the similarity is provided in the search term, we need to extract the
   * keyword that user enter in the search form
   * 
   * @param input the search value include the similarity
   * @return the search condition after process
   */
  public static String processUnifiedSearchCondition(String input) {
    if (StringUtils.isEmpty(input)) {
      return input;
    } else if (input.indexOf("~") < 0 || input.indexOf("\\~") > 0) {
      return input.trim();
    }
    StringBuilder builder = new StringBuilder();
    // The similarity is added for each word in the search condition, ex :
    // space~0.5 test~0.5
    // then we need to process each word separately
    String[] tab = input.split(" ");
    for (String s : tab) {
      if (s.isEmpty())
        continue;
      if (s.indexOf("~") > -1) {
        String searchTerm = s.substring(0, s.lastIndexOf("~"));
        builder.append(searchTerm).append(" ");
      } else {
        builder.append(s).append(" ");
      }

    }
    return builder.toString().trim();
  }

  /**
   * Filter all invalid character (anything except word, number, space and
   * search wildcard) from Space search conditional.
   * 
   * @since 1.2.2
   * @param input String
   * @return
   */

  public static String removeSpecialCharacterInSpaceFilter(String input) {
    // We don't remove the character "'" because it's a normal character in
    // french
    String result = input.replaceAll("[^\\pL\\pM\\p{Nd}\\p{Nl}\\p{Pc}[\\p{InEnclosedAlphanumerics}&&\\p{So}]\\?\\*%0-9\\']", " ");
    result = result.replaceAll("\\s+", " ");
    return result.trim();
  }

  /**
   * Utility for cleaning space name
   *
   * @param str
   * @return cleaned string
   */
  public static String cleanString(String str) {
    if (str == null) {
      throw new IllegalArgumentException("String argument must not be null.");
    }

    str = Normalizer.normalize(str, Normalizer.Form.NFKD);

    // the character ? seems to not be changed to d by the transliterate
    // function

    StringBuilder cleanedStr = new StringBuilder(str.toLowerCase().trim());
    // delete special character
    char lastChar = '_';
    for (int i = cleanedStr.length() -1; i >= 0; i--) {
      char c = cleanedStr.charAt(i);
      if ((c < 'a' || c > 'z') && (c < '0' || c > '9')) {
        if (lastChar == '_') {
          cleanedStr.setCharAt(i, String.valueOf(c % 10).charAt(0));
        } else {
          lastChar = '_';
          cleanedStr.setCharAt(i, '_');
        }
      } else {
        lastChar = c;
      }
    }
    return cleanedStr.toString().toLowerCase();
  }

  public static int[] parseDimension(String size) {
    int[] dimension = new int[2];
    if (size.contains(SIZE_SPLIT_CHAR) && !size.startsWith(SIZE_SPLIT_CHAR)) {
      dimension[0] = Integer.parseInt(size.split(SIZE_SPLIT_CHAR)[0]);
    }
    if (size.contains(SIZE_SPLIT_CHAR) && !size.endsWith(SIZE_SPLIT_CHAR)) {
      dimension[1] = Integer.parseInt(size.split(SIZE_SPLIT_CHAR)[1]);
    }
    return dimension;
  }
}
