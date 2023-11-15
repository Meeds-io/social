package org.exoplatform.social.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

public class MentionUtils {

  private static final Log     LOG             = ExoLogger.getLogger(MentionUtils.class);

  private static final Pattern MENTION_PATTERN = Pattern.compile("@((?![\\s<>/%'\"=])[\\p{ASCII}])+([<\\s]|$)");

  private MentionUtils() {
    // Static methods, thus no need to constructor
  }

  public static String substituteUsernames(String portalOwner, String message) {
    return substituteUsernames(ExoContainerContext.getService(IdentityManager.class), portalOwner, message);
  }

  public static String substituteUsernames(IdentityManager identityManager, String portalOwner, String message) {
    if (message == null || message.trim().isEmpty()) {
      return message;
    }
    //
    Matcher matcher = MENTION_PATTERN.matcher(message);

    // Replace all occurrences of pattern in input
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      String username = matcher.group().substring(1).trim();
      if (username.endsWith("<")) {
        username = username.length() > 1 ? username.substring(0, username.length() - 1) : "";
      }
      if (StringUtils.isBlank(username)) {
        continue;
      }
      appendReplacement(identityManager, matcher, buf, username, portalOwner);
    }
    if (buf.length() > 0) {
      matcher.appendTail(buf);
      return buf.toString().trim();
    }
    return message;
  }

  private static void appendReplacement(IdentityManager identityManager,
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

}
