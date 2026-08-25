/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.security.plugin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.utils.MailUtils;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.web.security.security.SecureRandomService;

import io.meeds.web.security.plugin.OtpPlugin;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

@Service
public class EmailOtpPlugin implements OtpPlugin {

  private static final Pattern     I18N_LABEL_PATTERN          = Pattern.compile("\\$\\{([a-zA-Z0-9\\.]+)\\}");

  private static final String      OTP_CACHE_NAME              = "otp.email";

  private static final String      LANG_PARAM                  = "$LANG";

  private static final String      USER_FULL_NAME_PARAM        = "$USER_FULL_NAME";

  private static final String      SITE_NAME_PARAM             = "$SITE_NAME";

  private static final String      COMPANY_LINK_PARAM          = "$COMPANY_LINK";

  private static final String      CODE_EXPIRATION_LABEL_PARAM = "$CODE_EXPIRATION_LABEL";

  private static final String      PRIMARY_COLOR_PARAM         = "$PRIMARY_COLOR";

  @Autowired
  private CacheService             cacheService;

  @Autowired
  private SecureRandomService      secureRandomService;

  @Autowired
  private OrganizationService      organizationService;

  @Autowired
  private ResourceBundleService    resourceBundleService;

  @Autowired
  private BrandingService          brandingService;

  @Autowired
  private MailService              mailService;

  /**
   * OTP Tentatives Cache in minutes
   */
  @Value("${meeds.apiKey.otp.email.ttl:5}")
  @Getter
  @Setter
  private long                     otpTtl;

  @Value("${meeds.apiKey.otp.email.length:5}")
  @Getter
  @Setter
  private long                     otpLength;

  @Value("${meeds.apiKey.otp.email.templatePath:assets/otp-email.html}")
  @Getter
  @Setter
  private String                   emailBodyPath;

  private SecureRandom             secureRandom;

  @Setter // Used in Tests only
  private String                   emailBodyTemplate;

  @Setter
  private ExoCache<String, String> otpCache;

  @Override
  public String getName() {
    return "email";
  }

  @Override
  public boolean canUse(String userName) {
    return true;
  }

  @Override
  public boolean validateOtp(String userName, String otpCode) {
    return StringUtils.equals(otpCode, getOtpCache().get(userName));
  }

  @Override
  @SneakyThrows
  public void generateOtpCode(String userName) {
    String email = getUserMail(userName);
    String userFullName = getUserFullName(userName);
    String lang = getUserLang(userName);
    String otpCode = generateOtpCode();
    getOtpCache().put(userName, otpCode);
    sendEmail(email, userFullName, String.valueOf(otpCode), lang);
  }

  private void sendEmail(String to, String userFullName, String otpCode, String lang) throws Exception {
    String emailSubject = getEmailSubject(lang);
    String emailBody = getEmailBody(userFullName, lang, otpCode);
    String from = getSenderFullEmail();
    MimeMessage mimeMessage = new MimeMessage(mailService.getMailSession());
    mimeMessage.setFrom(from);
    mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(to));
    mimeMessage.setSubject(StringEscapeUtils.unescapeHtml4(emailSubject), "UTF-8");
    mimeMessage.setSentDate(new Date());
    mimeMessage.setContent(emailBody, "text/html; charset=utf-8");
    mailService.sendMessage(mimeMessage);
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

  private String generateOtpCode() {
    return String.valueOf(getSecureRandom().nextLong((long) Math.pow(10d, otpLength - 1d),
                                                     (long) Math.pow(10d, otpLength)));
  }

  private String getUserMail(String userName) throws Exception {
    User user = organizationService.getUserHandler().findUserByName(userName);
    if (user == null) {
      throw new IllegalArgumentException();
    }
    return user.getEmail();
  }

  private String getUserFullName(String userName) throws Exception {
    User user = organizationService.getUserHandler().findUserByName(userName);
    if (user == null) {
      throw new IllegalArgumentException();
    }
    return user.getDisplayName();
  }

  private String getUserLang(String userName) throws Exception {
    UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(userName);
    if (userProfile != null
        && userProfile.getAttribute(Constants.USER_LANGUAGE) != null) {
      return userProfile.getAttribute(Constants.USER_LANGUAGE);
    } else {
      return ResourceBundleService.DEFAULT_CROWDIN_LANGUAGE;
    }
  }

  private String getEmailSubject(String lang) {
    return resourceBundleService.getSharedString("otp.email.subject", LocaleUtils.toLocale(lang));
  }

  private String getEmailBody(String userFullName, String lang, String otpCode) {
    String content = getEmailBody();
    Matcher matcher = I18N_LABEL_PATTERN.matcher(content);
    while (matcher.find()) {
      String i18nKey = matcher.group(1);
      String label = resourceBundleService.getSharedString(i18nKey, LocaleUtils.toLocale(lang));
      content = content.replace(matcher.group(), label);
    }
    content = content.replace("####", otpCode);
    content = content.replace(LANG_PARAM, lang);
    content = content.replace(USER_FULL_NAME_PARAM, userFullName);
    content = content.replace(SITE_NAME_PARAM, brandingService.getCompanyName());
    content = content.replace(COMPANY_LINK_PARAM, LinkProviderUtils.getBaseUrl());
    content = content.replace(CODE_EXPIRATION_LABEL_PARAM, getCodeExpirationLabel(lang));
    content = content.replace(PRIMARY_COLOR_PARAM, brandingService.getThemeStyle().get("primaryColor"));
    return content;
  }

  private String getCodeExpirationLabel(String lang) {
    return resourceBundleService.getSharedString("otp.email.label.codeExpirationMessage", LocaleUtils.toLocale(lang))
                                .replace("{0}", String.valueOf(otpTtl));
  }

  @SneakyThrows
  private String getEmailBody() {
    if (emailBodyTemplate == null) {
      try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(emailBodyPath)) {
        emailBodyTemplate = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
      }
    }
    return emailBodyTemplate;
  }

  private SecureRandom getSecureRandom() {
    if (secureRandom == null) {
      secureRandom = secureRandomService.getSecureRandom();
    }
    return secureRandom;
  }

  private ExoCache<String, String> getOtpCache() {
    if (otpCache == null) {
      otpCache = cacheService.getCacheInstance(OTP_CACHE_NAME);
      otpCache.setLiveTime(otpTtl * 60);
    }
    return otpCache;
  }

}
