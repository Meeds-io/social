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
package io.meeds.social.core.mail;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.MailUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.ResourceBundleService;

/**
 * Sends branded HTML emails to platform users: a caller provides the resolved
 * subject, a content fragment (the paragraphs specific to its email) and
 * optional custom tokens; this sender injects the fragment in the common
 * branded layout, resolves the ${...} shared bundle keys in the user language,
 * then substitutes the standard tokens ($EMAIL_TITLE with the subject, $LANG,
 * $USER_FULL_NAME, $SITE_NAME, $COMPANY_LINK, $PRIMARY_COLOR) before sending
 * through the mail service.
 */
@Service
public class BrandedEmailSender {

  public static final String        EMAIL_TITLE_PARAM    = "$EMAIL_TITLE";

  public static final String        EMAIL_CONTENT_PARAM  = "$EMAIL_CONTENT";

  private static final String       LAYOUT_TEMPLATE_PATH = "assets/branded-email-layout.html";

  private static final Pattern      I18N_LABEL_PATTERN   = Pattern.compile("\\$\\{([a-zA-Z0-9\\.]+)\\}");

  @Autowired
  private OrganizationService       organizationService;

  @Autowired
  private ResourceBundleService     resourceBundleService;

  @Autowired
  private BrandingService           brandingService;

  @Autowired
  private MailService               mailService;

  private final Map<String, String> templatesByPath      = new ConcurrentHashMap<>();

  public void sendEmail(String username,
                        String emailSubject,
                        String contentTemplatePath,
                        Map<String, String> customTokens) throws Exception {
    User user = organizationService.getUserHandler().findUserByName(username);
    String lang = getUserLang(username);
    String emailBody = getEmailBody(user.getDisplayName(), lang, emailSubject, contentTemplatePath, customTokens);
    MimeMessage mimeMessage = new MimeMessage(mailService.getMailSession());
    mimeMessage.setFrom(getSenderAddress());
    mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
    mimeMessage.setSubject(StringEscapeUtils.unescapeHtml4(emailSubject), "UTF-8");
    mimeMessage.setSentDate(new Date());
    mimeMessage.setContent(emailBody, "text/html; charset=utf-8");
    mailService.sendMessage(mimeMessage);
  }

  public String getUserLang(String username) throws Exception {
    UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
    if (userProfile != null && userProfile.getAttribute(Constants.USER_LANGUAGE) != null) {
      return userProfile.getAttribute(Constants.USER_LANGUAGE);
    } else {
      return ResourceBundleService.DEFAULT_CROWDIN_LANGUAGE;
    }
  }

  private String getEmailBody(String userFullName,
                              String lang,
                              String emailTitle,
                              String contentTemplatePath,
                              Map<String, String> customTokens) {
    String content = getTemplate(LAYOUT_TEMPLATE_PATH).replace(EMAIL_CONTENT_PARAM, getTemplate(contentTemplatePath));
    Matcher matcher = I18N_LABEL_PATTERN.matcher(content);
    while (matcher.find()) {
      String i18nKey = matcher.group(1);
      String label = resourceBundleService.getSharedString(i18nKey, LocaleUtils.toLocale(lang));
      content = content.replace(matcher.group(), label);
    }
    for (Map.Entry<String, String> customToken : customTokens.entrySet()) {
      content = content.replace(customToken.getKey(), customToken.getValue());
    }
    return content.replace(EMAIL_TITLE_PARAM, emailTitle)
                  .replace("$LANG", lang)
                  .replace("$USER_FULL_NAME", userFullName)
                  .replace("$SITE_NAME", brandingService.getCompanyName())
                  .replace("$COMPANY_LINK", getBaseUrl())
                  .replace("$PRIMARY_COLOR", brandingService.getThemeStyle().get("primaryColor"));
  }

  private String getTemplate(String templatePath) {
    return templatesByPath.computeIfAbsent(templatePath, path -> {
      try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      } catch (Exception e) {
        throw new IllegalStateException(String.format("Error reading the email template %s", path), e);
      }
    });
  }

  private String getBaseUrl() {
    return CommonsUtils.getCurrentDomain() + "/" + PortalContainer.getCurrentPortalContainerName();
  }

  private InternetAddress getSenderAddress() throws UnsupportedEncodingException, AddressException {
    String senderEmail;
    try {
      senderEmail = MailUtils.getSenderEmail();
    } catch (Exception e) {
      senderEmail = System.getProperty("gatein.email.smtp.from");
    }
    String senderName = brandingService.getCompanyName();
    // the personal name is encoded by InternetAddress: a company name holding
    // specials (',', '(' ...) must not produce an invalid From header
    return StringUtils.isBlank(senderName) ? new InternetAddress(senderEmail)
                                           : new InternetAddress(senderEmail, senderName, StandardCharsets.UTF_8.name());
  }

}
