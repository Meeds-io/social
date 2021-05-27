package org.exoplatform.social.core.utils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionUtils {


    private static final Pattern MENTION_PATTERN                 = Pattern.compile("@([^\\s<]+)|@([^\\s<]+)$");

    private static final Log LOG                             = ExoLogger.getLogger(MentionUtils.class);

    public static String substituteUsernames(String portalOwner, String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }
        //
        Matcher matcher = MENTION_PATTERN.matcher(message);

        // Replace all occurrences of pattern in input
        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            // Get the match result
            String username = matcher.group().substring(1);
            if (username == null || username.isEmpty()) {
                continue;
            }
            IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
            if (identity == null || identity.isDeleted() || !identity.isEnable()) {
                continue;
            }
            try {
                username = LinkProvider.getProfileLink(username, portalOwner);
            } catch (Exception e) {
                LOG.warn("Error while retrieving link for profile of user {}", username, e);
                continue;
            }
            // Insert replacement
            if (username != null) {
                matcher.appendReplacement(buf, username);
            }
        }
        if (buf.length() > 0) {
            matcher.appendTail(buf);
            return buf.toString();
        }
        return message;
    }
}
