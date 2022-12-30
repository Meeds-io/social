package org.exoplatform.social.core.storage.impl;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class StorageUtils {
  //
  private static final Log   LOG               = ExoLogger.getLogger(StorageUtils.class.getName());

  //
  public static final String ASTERISK_STR      = "*";

  public static final String PERCENT_STR       = "%";

  public static final char   ASTERISK_CHAR     = '*';

  public static final String SPACE_STR         = " ";

  public static final String EMPTY_STR         = "";

  public static final String SLASH_STR         = "/";

  public static final String COLON_STR         = ":";

  public static final String SOC_RELATIONSHIP  = "soc:relationship";

  public static final String SOC_RELCEIVER     = "soc:receiver";

  public static final String SOC_SENDER        = "soc:sender";

  public static final String SOC_IGNORED       = "soc:ignored";

  public static final String SOC_FROM          = "soc:from";

  public static final String SOC_TO            = "soc:to";

  public static final String SOC_ACTIVITY_INFO = "soc:activityInfo";

  public static final String SOC_PREFIX        = "soc:";

  public static final DateTimeFormatter RFC_3339_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
                                                                              .withResolverStyle(ResolverStyle.LENIENT);

  private final static long  DAY_MILISECONDS   = 86400000;                                         // a
                                                                                                   // day
                                                                                                   // =
                                                                                                   // 24h
                                                                                                   // x
                                                                                                   // 60m
                                                                                                   // x
                                                                                                   // 60s
                                                                                                   // x
                                                                                                   // 1000
                                                                                                   // milisecond.

  public static String processUsernameSearchPattern(final String userName) {
    String modifiedUserName = userName;
    if (modifiedUserName.length() > 0) {
      modifiedUserName =
                       ((EMPTY_STR.equals(modifiedUserName)) || (modifiedUserName.length() == 0))
                                                                                                  ? ASTERISK_STR
                                                                                                  : modifiedUserName;

      modifiedUserName =
                       (modifiedUserName.charAt(0) != ASTERISK_CHAR) ? ASTERISK_STR + modifiedUserName : modifiedUserName;

      modifiedUserName =
                       (modifiedUserName.charAt(modifiedUserName.length() - 1) != ASTERISK_CHAR)
                                                                                                 ? modifiedUserName +=
                                                                                                                    ASTERISK_STR
                                                                                                 : modifiedUserName;

      modifiedUserName =
                       (modifiedUserName.indexOf(ASTERISK_STR) >= 0)
                                                                     ? modifiedUserName.replace(ASTERISK_STR, "." + ASTERISK_STR)
                                                                     : modifiedUserName;

      modifiedUserName =
                       (modifiedUserName.indexOf(PERCENT_STR) >= 0)
                                                                    ? modifiedUserName.replace(PERCENT_STR, "." + ASTERISK_STR)
                                                                    : modifiedUserName;

      Pattern.compile(modifiedUserName);
    }
    return userName;
  }

  public static String addAsteriskToStringInput(final String input) {
    if (input.length() != 0) {
      if (input.indexOf(ASTERISK_STR) == -1) {
        return ASTERISK_STR + input + ASTERISK_STR;
      }
      return input;
    }
    return EMPTY_STR;
  }

  /**
   * Process Unified Search Condition
   * 
   * @param searchCondition the input search condition
   * @return List of conditions
   * @since 4.0.x
   */
  public static List<String> processUnifiedSearchCondition(String searchCondition) {
    String[] spaceConditions = searchCondition.split(" ");
    List<String> result = new ArrayList<String>(spaceConditions.length);

    for (String conditionValue : spaceConditions) {
      result.add(conditionValue);
    }
    return result;
  }

  /**
   * Gets common item number from two list
   * 
   * @param m the first list
   * @param n the second list
   * @return number of common item
   */
  public static <T> int getCommonItemNumber(final List<T> m, final List<T> n) {
    if (m == null || n == null) {
      return 0;
    }
    List<T> copy = new ArrayList<T>(m);
    copy.removeAll(n);

    return (m.size() - copy.size());
  }

  /**
   * Sort one map by its value
   * 
   * @param map the input map
   * @param asc indicate sort by ASC (true) or DESC (false)
   * @return the sorted map
   * @since 4.0.x
   */
  public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map, final boolean asc) {
    //
    List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
    //
    Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
      public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        if (asc)
          return (o1.getValue()).compareTo(o2.getValue());
        else
          return (o1.getValue()).compareTo(o2.getValue()) / -1;
      }
    });

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list) {
      result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  /**
   * Sort list of spaces by space's display name
   * 
   * @param list
   * @param asc
   * @return sorted list
   */
  public static List<Space> sortSpaceByName(List<Space> list, final boolean asc) {
    //
    Collections.sort(list, new Comparator<Space>() {
      public int compare(Space o1, Space o2) {
        if (asc)
          return (o1.getDisplayName()).compareTo(o2.getDisplayName());
        else
          return (o1.getDisplayName()).compareTo(o2.getDisplayName()) / -1;
      }
    });

    return list;
  }

  /**
   * Sort list of identities by full name
   * 
   * @param list
   * @param asc
   * @return sorted list
   */
  public static List<Identity> sortIdentitiesByFullName(List<Identity> list, final boolean asc) {
    //
    Collections.sort(list, new Comparator<Identity>() {
      public int compare(Identity o1, Identity o2) {
        if (asc)
          return (o1.getProfile().getFullName()).compareTo(o2.getProfile().getFullName());
        else
          return (o1.getProfile().getFullName()).compareTo(o2.getProfile().getFullName()) / -1;
      }
    });

    return list;
  }

  /**
   * Sort a list of activity by updated time
   * 
   * @param list
   * @return
   */
  public static List<ExoSocialActivity> sortActivitiesByTime(List<ExoSocialActivity> list, int limit) {
    //
    Collections.sort(list, new Comparator<ExoSocialActivity>() {
      public int compare(ExoSocialActivity a1, ExoSocialActivity a2) {
        return ((Long) a1.getUpdated().getTime()).compareTo((Long) a2.getUpdated().getTime()) / -1;
      }
    });

    return list.size() > limit ? list.subList(0, limit - 1) : list;
  }

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

  /*
   * Gets added element when compares between l1 and l2
   * @param l1
   * @param l2
   * @return
   */
  public static String[] sub(String[] l1, String[] l2) {

    if (l1 == null) {
      return new String[] {};
    }

    if (l2 == null) {
      return l1;
    }

    List<String> l = new ArrayList(Arrays.asList(l1));
    l.removeAll(Arrays.asList(l2));
    return l.toArray(new String[] {});
  }

  /**
   * @param aroundDays
   * @param lazilyCreatedTime
   * @return
   */
  public static boolean isActiveUser(int aroundDays, long lazilyCreatedTime) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - aroundDays);
    long limitTime = cal.getTimeInMillis();
    return lazilyCreatedTime >= limitTime;
  }

  /**
   * Compares oldDate and newDate. return TRUE if given newDate the after one
   * day or more the given oldDate
   * 
   * @param oldDate
   * @param newDate
   * @return TRUE: the day after oldDate
   */
  public static boolean afterDayOrMore(long oldDate, long newDate) {
    long diffValue = newDate - oldDate;
    return diffValue >= DAY_MILISECONDS;
  }

  /**
   * Gets the list of activity's id from the activities
   * 
   * @param activities
   * @return list of ids
   */
  public static List<String> getIds(List<ExoSocialActivity> activities) {
    List<String> ids = new LinkedList<String>();
    for (ExoSocialActivity a : activities) {
      ids.add(a.getId());
    }

    return ids;
  }

  public static String toRFC3339Date(Date dateTime) {
    if (dateTime == null) {
      return null;
    }
    ZonedDateTime zonedDateTime = dateTime.toInstant().atZone(ZoneOffset.UTC);
    return zonedDateTime.format(RFC_3339_FORMATTER);
  }

  public static Date parseRFC3339Date(String dateString) {
    if (org.apache.commons.lang3.StringUtils.isBlank(dateString)) {
      return null;
    }
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, RFC_3339_FORMATTER);
    return Date.from(zonedDateTime.toInstant());
  }
}
