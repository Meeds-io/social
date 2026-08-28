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

import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.core.mail.BrandedEmailSender;
import io.meeds.web.security.service.OtpService;

import lombok.Setter;
import lombok.SneakyThrows;

@Service
public class AccountDeactivationService {

  public static final String   ACCOUNT_DEACTIVATION_REQUESTED_EVENT = "social.account.deactivation.requested";

  public static final String   ACCOUNT_DELETION_REQUESTED_EVENT     = "social.account.deletion.requested";

  /**
   * Coordinates of the per-user marker recording a confirmed deletion request:
   * one row per requester under its user context, valued with the request time
   * in epoch milliseconds. The deletion processing job reads them back through
   * SettingService#getContextsByTypeAndScopeAndSettingName.
   */
  public static final Scope    DELETION_REQUEST_SCOPE               = Scope.APPLICATION.id("AccountDeletion");

  public static final String   DELETION_REQUEST_SETTING_NAME        = "accountDeletionRequestTime";

  /**
   * The only creation source qualifying an account as managed by the platform
   * itself, as stamped by the users management UI
   * (org.exoplatform.portal.rest.UserRestResourcesV1#CREATION_SOURCE_UI).
   */
  public static final String   UI_CREATION_SOURCE                   = "ui";

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
  private SettingService         settingService;

  @Autowired
  private BrandedEmailSender     brandedEmailSender;

  @Autowired
  private BrandingService        brandingService;

  @Autowired
  private ResourceBundleService  resourceBundleService;

  @Value("${exo.accountDeactivation.confirmation.email.templatePath:assets/account-deactivation-confirmation-email-content.html}")
  @Setter
  private String                 emailBodyPath;

  @Value("${exo.accountDeletion.confirmation.email.templatePath:assets/account-deletion-confirmation-email-content.html}")
  @Setter
  private String                 deletionEmailBodyPath;

  /**
   * The deactivation is offered only for accounts whose lifecycle belongs to
   * the platform: externally synchronized accounts (LDAP) are excluded, and
   * among the others only those created through the platform UI qualify. An
   * account with no creation source predates the stamping of that attribute:
   * it keeps the option rather than losing it on an unknown provenance.
   */
  @SneakyThrows
  public boolean isDeactivationAllowed(String username) {
    if (!securitySettingService.getRegistrationSetting().isAccountDeactivationEnabled()) {
      return false;
    }
    User user = organizationService.getUserHandler().findUserByName(username);
    if (user == null || !user.isInternalStore()) {
      return false;
    }
    String creationSource = user.getCreationSource();
    return StringUtils.isBlank(creationSource) || StringUtils.equals(creationSource, UI_CREATION_SOURCE);
  }

  /**
   * The deletion request rides the deactivation one: it is offered only when
   * the admin enabled it on top of the deactivation, to users already
   * qualifying for the deactivation.
   */
  public boolean isDeletionAllowed(String username) {
    return securitySettingService.getRegistrationSetting().isAccountDeletionEnabled()
           && isDeactivationAllowed(username);
  }

  /**
   * Deactivates the account of the designated user after validating the OTP
   * code: this is the single authoritative OTP validation of the flow. When
   * the deletion is also requested, the request time is durably recorded
   * before the account gets disabled, so a recording failure never silently
   * downgrades a deletion request to a plain deactivation; without a deletion
   * request, any stale marker left by a previously re-enabled account is
   * cleared instead.
   *
   * @throws IllegalStateException when the deactivation isn't allowed for the
   *           user (admin option off, externally synchronized account or
   *           account whose creation source isn't managed by the platform), or
   *           when the deletion is requested while the admin deletion option
   *           is off
   * @throws IllegalAccessException when the OTP code is blank, invalid or the
   *           validation tentatives are exhausted
   */
  @SneakyThrows
  public void requestDeactivation(String username,
                                  String otpMethod,
                                  String otpCode,
                                  boolean deleteRequested) throws IllegalAccessException {
    if (!isDeactivationAllowed(username)) {
      throw new IllegalStateException(String.format("Account deactivation isn't allowed for user %s", username));
    }
    if (deleteRequested && !securitySettingService.getRegistrationSetting().isAccountDeletionEnabled()) {
      throw new IllegalStateException(String.format("Account deletion isn't allowed for user %s", username));
    }
    otpService.validateOtp(username, otpMethod, otpCode);
    String identityId = identityManager.getOrCreateUserIdentity(username).getId();
    if (deleteRequested) {
      settingService.set(Context.USER.id(username),
                         DELETION_REQUEST_SCOPE,
                         DELETION_REQUEST_SETTING_NAME,
                         SettingValue.create(String.valueOf(System.currentTimeMillis())));
    } else {
      settingService.remove(Context.USER.id(username), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    }
    sendUserConfirmationEmail(username, deleteRequested);
    organizationService.getUserHandler().setEnabled(username, false, true);
    broadcastEvent(ACCOUNT_DEACTIVATION_REQUESTED_EVENT, username, identityId);
    if (deleteRequested) {
      broadcastEvent(ACCOUNT_DELETION_REQUESTED_EVENT, username, identityId);
    }
    identityRegistry.unregister(username);
    conversationRegistry.unregisterByUserId(username);
  }

  /**
   * Sends the confirmation email to the user, before the account gets
   * disabled. When the deletion was also requested, the email additionally
   * reminds that the account will be deleted in 30 days. The sending is best
   * effort: a mail server failure doesn't prevent the deactivation
   * deliberately requested by the user.
   */
  protected void sendUserConfirmationEmail(String username, boolean deleteRequested) {
    try {
      String lang = brandedEmailSender.getUserLang(username);
      String emailSubject = resourceBundleService.getSharedString("social.accountDeactivation.confirmation.email.subject",
                                                                  LocaleUtils.toLocale(lang))
                                                 .replace("{0}", brandingService.getCompanyName());
      brandedEmailSender.sendEmail(username, emailSubject, deleteRequested ? deletionEmailBodyPath : emailBodyPath, Map.of());
    } catch (Exception e) {
      LOG.warn("Error sending the account deactivation confirmation email to user {}", username, e);
    }
  }

  private void broadcastEvent(String eventName, String username, String identityId) {
    try {
      listenerService.broadcast(eventName, username, identityId);
    } catch (Exception e) {
      LOG.warn("Error broadcasting event {} for user {} with identity id {}", eventName, username, identityId, e);
    }
  }

}
