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
package io.meeds.social.security.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.MailUtils;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.web.security.service.OtpService;

import lombok.Setter;
import lombok.SneakyThrows;

@Service
public class AccountDeactivationService {

  public static final String   ACCOUNT_DEACTIVATION_REQUESTED_EVENT = "social.account.deactivation.requested";

  private static final Pattern I18N_LABEL_PATTERN                   = Pattern.compile("\\$\\{([a-zA-Z0-9\\.]+)\\}");

  private static final Log     LOG                                  = ExoLogger.getLogger(AccountDeactivationService.class);

  @Autowired
  private SecuritySettingService securitySettingService;

  @Autowired
  private OrganizationService    organizationService;

  @Autowired
  private IdentityManager        identityManager;

  @Autowired
  private ListenerService        listenerService;

  @Autowired
  private IdentityRegistry       identityRegistry;

  @Autowired
  private ConversationRegistry   conversationRegistry;

  @Autowired
  private OtpService             otpService;

  @Autowired
  private MailService            mailService;

  @Autowired
  private BrandingService        brandingService;

  @Autowired
  private ResourceBundleService  resourceBundleService;

  @Value("${social.accountDeactivation.confirmation.email.templatePath:assets/account-deactivation-confirmation-email.html}")
  @Setter
  private String                 emailBodyPath;

  @Setter // Used in Tests only
  private String                 emailBodyTemplate;

  @SneakyThrows
  public boolean isDeactivationAllowed(String username) {
    if (!securitySettingService.getRegistrationSetting().isAccountDeactivationEnabled()) {
      return false;
    }
    User user = organizationService.getUserHandler().findUserByName(username);
    return user != null && user.isInternalStore();
  }

  /**
   * Deactivates the account of the designated user after validating the OTP
   * code: this is the single authoritative OTP validation of the flow.
   *
   * @throws IllegalStateException when the deactivation isn't allowed for the
   *           user (admin option off or externally synchronized account)
   * @throws IllegalAccessException when the OTP code is blank, invalid or the
   *           validation tentatives are exhausted
   * @throws UnsupportedOperationException when the account deletion is
   *           requested, until its persistence and processing job are
   *           delivered
   */
  @SneakyThrows
  public void requestDeactivation(String username,
                                  String otpMethod,
                                  String otpCode,
                                  boolean deleteRequested) throws IllegalAccessException {
    if (deleteRequested) {
      // no persistence consumes the deletion request yet: refuse it rather
      // than silently downgrading it to a plain deactivation
      throw new UnsupportedOperationException("Account deletion request isn't supported yet");
    }
    if (!isDeactivationAllowed(username)) {
      throw new IllegalStateException(String.format("Account deactivation isn't allowed for user %s", username));
    }
    otpService.validateOtp(username, otpMethod, otpCode);
    String identityId = identityManager.getOrCreateUserIdentity(username).getId();
    sendUserConfirmationEmail(username);
    organizationService.getUserHandler().setEnabled(username, false, true);
    broadcastEvent(ACCOUNT_DEACTIVATION_REQUESTED_EVENT, username, identityId);
    identityRegistry.unregister(username);
    conversationRegistry.unregisterByUserId(username);
  }

  /**
   * Sends the confirmation email to the user, before the account gets
   * disabled. The sending is best effort: a mail server failure doesn't
   * prevent the deactivation deliberately requested by the user.
   */
  protected void sendUserConfirmationEmail(String username) {
    try {
      User user = organizationService.getUserHandler().findUserByName(username);
      String lang = getUserLang(username);
      String emailSubject = resourceBundleService.getSharedString("social.accountDeactivation.confirmation.email.subject",
                                                                  LocaleUtils.toLocale(lang))
                                                 .replace("{0}", brandingService.getCompanyName());
      String emailBody = getEmailBody(user.getDisplayName(), lang);
      MimeMessage mimeMessage = new MimeMessage(mailService.getMailSession());
      mimeMessage.setFrom(getSenderFullEmail());
      mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
      mimeMessage.setSubject(StringEscapeUtils.unescapeHtml4(emailSubject), "UTF-8");
      mimeMessage.setSentDate(new Date());
      mimeMessage.setContent(emailBody, "text/html; charset=utf-8");
      mailService.sendMessage(mimeMessage);
    } catch (Exception e) {
      LOG.warn("Error sending the account deactivation confirmation email to user {}", username, e);
    }
  }

  private String getUserLang(String username) throws Exception {
    UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(username);
    if (userProfile != null && userProfile.getAttribute(Constants.USER_LANGUAGE) != null) {
      return userProfile.getAttribute(Constants.USER_LANGUAGE);
    } else {
      return ResourceBundleService.DEFAULT_CROWDIN_LANGUAGE;
    }
  }

  private String getEmailBody(String userFullName, String lang) {
    String content = getEmailBodyTemplate();
    Matcher matcher = I18N_LABEL_PATTERN.matcher(content);
    while (matcher.find()) {
      String i18nKey = matcher.group(1);
      String label = resourceBundleService.getSharedString(i18nKey, LocaleUtils.toLocale(lang));
      content = content.replace(matcher.group(), label);
    }
    return content.replace("$LANG", lang)
                  .replace("$USER_FULL_NAME", userFullName)
                  .replace("$SITE_NAME", brandingService.getCompanyName())
                  .replace("$COMPANY_LINK", CommonsUtils.getCurrentDomain())
                  .replace("$PRIMARY_COLOR", brandingService.getThemeStyle().get("primaryColor"));
  }

  @SneakyThrows
  private String getEmailBodyTemplate() {
    if (emailBodyTemplate == null) {
      try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(emailBodyPath)) {
        emailBodyTemplate = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
    }
    return emailBodyTemplate;
  }

  private String getSenderFullEmail() {
    String senderEmail;
    try {
      senderEmail = MailUtils.getSenderEmail();
    } catch (Exception e) {
      senderEmail = System.getProperty("gatein.email.smtp.from");
    }
    String senderName = brandingService.getCompanyName();
    return StringUtils.isBlank(senderName) ? senderEmail : senderName + "<" + senderEmail + ">";
  }

  private void broadcastEvent(String eventName, String username, String identityId) {
    try {
      listenerService.broadcast(eventName, username, identityId);
    } catch (Exception e) {
      LOG.warn("Error broadcasting event {} for user {} with identity id {}", eventName, username, identityId, e);
    }
  }

}
