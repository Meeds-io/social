package org.exoplatform.social.core.utils;

import static org.exoplatform.social.core.BaseActivityProcessorPlugin.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

public class MentionUtils {

  private static final String    DEFAULT_TITLE_TEMPLATE_PARAM = "default_title";

  private static final String    COMMENT_TEMPLATE_PARAM       = "comment";

  private static final Log       LOG                          = ExoLogger.getLogger(MentionUtils.class);

  private static final Pattern   USER_MENTION_PATTERN         = Pattern.compile("@((?![\\s<>/%'\"=])[\\p{ASCII}])+([<\\s]|$)");

  private static final Pattern   ROLE_MENTION_PATTERN         =
                                                      Pattern.compile("@((?![\\s<>/%'\"=])[\\p{ASCII}])+:(\\d+)([<\\s]|$)");

  private static IdentityManager identityManager;

  private MentionUtils() {
    // Static methods, thus no need to constructor
  }

  public static Set<String> getMentionedUsernames(String message) {
    if (message == null || message.trim().isEmpty()) {
      return Collections.emptySet();
    }

    Set<String> usernames = new HashSet<>();
    Matcher matcher = USER_MENTION_PATTERN.matcher(message);
    while (matcher.find()) {
      String username = matcher.group().substring(1).trim();
      if (username.endsWith("<")) {
        username = username.length() > 1 ? username.substring(0, username.length() - 1) : "";
      }
      if (StringUtils.isBlank(username)
          || !isUserEnabled(username)) {
        continue;
      }
      usernames.add(username);
    }
    return usernames;
  }

  public static Set<String> getMentionedRoles(String message, String identityId) {
    if (message == null || message.trim().isEmpty()) {
      return Collections.emptySet();
    }

    Set<String> roles = new HashSet<>();
    Matcher matcher = ROLE_MENTION_PATTERN.matcher(message);
    while (matcher.find()) {
      String[] roleParts = matcher.group().substring(1).trim().split(":");
      String role = roleParts[0];
      String groupIdentityId = roleParts[1];
      if (role.endsWith("<")) {
        role = role.length() > 1 ? role.substring(0, role.length() - 1) : "";
      }
      if (StringUtils.isBlank(role)
          || !StringUtils.equals(identityId, groupIdentityId)) {
        continue;
      }
      roles.add(role);
    }
    return roles;
  }

  public static String substituteUsernames(String message) {
    return substituteUsernames(getIdentityManager(), null, message, null);
  }

  public static String substituteUsernames(String message, Locale locale) {
    return substituteUsernames(getIdentityManager(), null, message, locale);
  }

  public static String substituteUsernames(String portalOwner, String message) {
    return substituteUsernames(getIdentityManager(), portalOwner, message, null);
  }

  public static String substituteUsernames(String portalOwner, String message, Locale locale) {
    return substituteUsernames(getIdentityManager(), portalOwner, message, locale);
  }

  public static void substituteUsernames(ExoSocialActivity activity, String portalOwner) {
    substituteUsernames(activity, getTemplateParamKeysToFilter(activity), portalOwner);
  }

  public static void substituteUsernames(ExoSocialActivity activity, List<String> templateParamKeys, String portalOwner) {
    activity.setTitle(substituteUsernames(portalOwner, activity.getTitle()));
    activity.setBody(substituteUsernames(portalOwner, activity.getBody()));
    Map<String, String> templateParams = activity.getTemplateParams();
    if (MapUtils.isNotEmpty(templateParams)) {
      for (String key : templateParamKeys) {
        templateParams.put(key, substituteUsernames(portalOwner, templateParams.get(key)));
      }
      templateParams.computeIfPresent(COMMENT_TEMPLATE_PARAM, (key, value) -> substituteUsernames(portalOwner, value));
      templateParams.computeIfPresent(DEFAULT_TITLE_TEMPLATE_PARAM, (key, value) -> substituteUsernames(portalOwner, value));
    }
  }

  public static String substituteUsernames(IdentityManager identityManager, String portalOwner, String message) {
    return substituteUsernames(identityManager, portalOwner, message, null);
  }

  public static String substituteUsernames(IdentityManager identityManager, String portalOwner, String message, Locale locale) {
    if (message == null || message.trim().isEmpty()) {
      return message;
    }
    Matcher matcher = USER_MENTION_PATTERN.matcher(message);
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      String username = matcher.group().substring(1).trim();
      if (username.endsWith("<")) {
        username = username.length() > 1 ? username.substring(0, username.length() - 1) : "";
      }
      if (StringUtils.isBlank(username)) {
        continue;
      }
      appendUsernameReplacement(identityManager, matcher, buf, username, portalOwner);
    }
    if (buf.length() > 0) {
      matcher.appendTail(buf);
      message = buf.toString().trim();
    }
    return substituteRoles(identityManager, message, locale);
  }

  public static String substituteRoles(IdentityManager identityManager, String message, Locale locale) {
    if (StringUtils.isBlank(message)) {
      return message;
    }
    Matcher matcher = ROLE_MENTION_PATTERN.matcher(message);
    String identityId = null;
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      String[] roleParts = matcher.group().substring(1).trim().split(":");
      String role = roleParts[0];
      identityId = roleParts[1];
      if (role.endsWith("<")) {
        role = role.length() > 1 ? role.substring(0, role.length() - 1) : "";
      }
      Identity identity = identityManager.getIdentity(identityId);
      if (StringUtils.isBlank(role)
          || !isIdentityEnabled(identity)) {
        continue;
      }
      appendRoleReplacement(matcher, buf, role, identityId, locale);
    }
    if (buf.length() > 0) {
      matcher.appendTail(buf);
      message = buf.toString().trim();
    }
    return message;
  }

  public static void substituteRoleWithLocale(ExoSocialActivity activity, Locale locale) {
    activity.setTitle(substituteRoleWithLocale(activity.getTitle(), locale));
    activity.setBody(substituteRoleWithLocale(activity.getBody(), locale));
    Map<String, String> templateParams = activity.getTemplateParams();
    if (MapUtils.isNotEmpty(templateParams)) {
      templateParams.computeIfPresent(COMMENT_TEMPLATE_PARAM, (key, value) -> substituteRoleWithLocale(value, locale));
      templateParams.computeIfPresent(DEFAULT_TITLE_TEMPLATE_PARAM, (key, value) -> substituteRoleWithLocale(value, locale));

      List<String> templateParamKeys = getTemplateParamKeysToFilter(activity);
      if (MapUtils.isNotEmpty(templateParams)) {
        for (String key : templateParamKeys) {
          templateParams.computeIfPresent(key, (mapKey, value) -> substituteRoleWithLocale(value, locale));
        }
      }
    }
  }

  public static String substituteRoleWithLocale(String message, Locale locale) {
    if (StringUtils.isNotBlank(message)
        && locale != null
        && !LinkProvider.getDefaultLocale().equals(locale)) {
      Locale defaultLocale = LinkProvider.getDefaultLocale();
      message = message.replace(LinkProvider.getGroupRoleLabel("member", defaultLocale),
                                LinkProvider.getGroupRoleLabel("member", locale));
      message = message.replace(LinkProvider.getGroupRoleLabel("manager", defaultLocale),
                                LinkProvider.getGroupRoleLabel("manager", locale));
      message = message.replace(LinkProvider.getGroupRoleLabel("redactor", defaultLocale),
                                LinkProvider.getGroupRoleLabel("redactor", locale));
      message = message.replace(LinkProvider.getGroupRoleLabel("publisher", defaultLocale),
                                LinkProvider.getGroupRoleLabel("publisher", locale));
    }
    return message;
  }

  private static void appendUsernameReplacement(IdentityManager identityManager,
                                                Matcher matcher,
                                                StringBuffer buf,
                                                String username,
                                                String portalOwner) {
    try {
      Identity identity = identityManager.getOrCreateUserIdentity(username);
      if (identity == null || identity.isDeleted() || !identity.isEnable()) {
        return;
      }
      String profileLink = LinkProvider.getProfileLink(username, portalOwner);
      // Insert replacement
      if (StringUtils.isNotBlank(profileLink)) {
        matcher.appendReplacement(buf, profileLink + " ");
      }
    } catch (Exception e) {
      LOG.warn("Error while retrieving link for profile of user {}", username, e);
    }
  }

  private static void appendRoleReplacement(Matcher matcher,
                                            StringBuffer buf,
                                            String role,
                                            String identityId,
                                            Locale locale) {
    try {
      String roleLink = LinkProvider.getGroupRoleLink(role, identityId, locale);
      if (StringUtils.isNotBlank(roleLink)) {
        matcher.appendReplacement(buf, roleLink + " ");
      }
    } catch (Exception e) {
      LOG.warn("Error while retrieving link for role {} ans pace with identity id {}",
               role,
               identityId,
               e);
    }
  }

  private static boolean isUserEnabled(String username) {
    Identity identity = getIdentityManager().getOrCreateUserIdentity(username);
    return isIdentityEnabled(identity);
  }

  private static boolean isIdentityEnabled(Identity identity) {
    return identity != null && !identity.isDeleted() && identity.isEnable();
  }

  private static List<String> getTemplateParamKeysToFilter(ExoSocialActivity activity) {
    Map<String, String> templateParams = activity.getTemplateParams();
    ArrayList<String> keys = new ArrayList<>();

    if (templateParams != null && templateParams.containsKey(TEMPLATE_PARAM_TO_PROCESS)) {
      String[] templateParamKeys = activity.getTemplateParams().get(TEMPLATE_PARAM_TO_PROCESS).split(TEMPLATE_PARAM_LIST_DELIM);
      for (String key : templateParamKeys) {
        if (key.endsWith("\\")) {
          key = key.replace("\\", "");
        }
        if (templateParams.containsKey(key.replace("\\", ""))) {
          keys.add(key);
        }
      }
    }
    return keys;
  }

  private static IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }

}
