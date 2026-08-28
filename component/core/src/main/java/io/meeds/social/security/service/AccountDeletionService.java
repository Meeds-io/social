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

import static io.meeds.social.security.service.AccountDeactivationService.DELETION_REQUEST_SCOPE;
import static io.meeds.social.security.service.AccountDeactivationService.DELETION_REQUEST_SETTING_NAME;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.common.ContainerTransactional;

import lombok.Setter;

/**
 * Executes the account deletion requests recorded by
 * {@link AccountDeactivationService}, once their grace delay elapsed. The
 * processing is idempotent without any cluster lock: for each due account the
 * request marker is removed in its own committed lifecycle first, so a
 * concurrent or replayed run finds nothing to do, and the deletion itself
 * (social identity soft-deleted then IDM user removed, mirroring the users
 * administration deletion) is a no-op once the IDM user is gone.
 */
@Service
public class AccountDeletionService {

  public static final String ACCOUNT_DELETED_EVENT = "social.account.deleted";

  private static final int   PAGE_SIZE             = 20;

  private static final Log   LOG                   = ExoLogger.getLogger(AccountDeletionService.class);

  @Autowired
  private SettingService     settingService;

  @Autowired
  private OrganizationService organizationService;

  @Autowired
  private IdentityManager    identityManager;

  @Autowired
  private IndexingService    indexingService;

  @Autowired
  private ListenerService    listenerService;

  /**
   * Grace delay, in days, between the confirmed deletion request and its
   * execution.
   */
  @Value("${exo.accountDeletion.delay.days:30}")
  @Setter
  private long               delayDays;

  /**
   * Processes all the recorded deletion requests: collects the pending
   * requesters first (the enumeration must not be paged over while its rows
   * get deleted), then handles each account independently so one failure
   * never prevents the other deletions.
   */
  public void processPendingDeletionRequests() {
    List<String> usernames = collectPendingRequests();
    for (String username : usernames) {
      try {
        processPendingDeletionRequestOf(username);
      } catch (Exception e) {
        LOG.error("Error processing the account deletion request of user {}, the account is left deactivated", username, e);
      }
    }
  }

  public void processPendingDeletionRequestOf(String username) throws Exception { // NOSONAR
    if (prepareDeletion(username)) {
      deleteAccount(username);
    }
  }

  @ContainerTransactional
  public List<String> collectPendingRequests() {
    List<String> usernames = new ArrayList<>();
    int offset = 0;
    List<Context> contexts;
    do {
      contexts = settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(),
                                                                        DELETION_REQUEST_SCOPE.getName(),
                                                                        DELETION_REQUEST_SCOPE.getId(),
                                                                        DELETION_REQUEST_SETTING_NAME,
                                                                        offset,
                                                                        PAGE_SIZE);
      contexts.stream()
              .map(Context::getId)
              .filter(StringUtils::isNotBlank)
              .forEach(usernames::add);
      offset += PAGE_SIZE;
    } while (contexts.size() == PAGE_SIZE);
    return usernames;
  }

  /**
   * Re-checks the eligibility of one recorded request and, when the account
   * has to be deleted, removes the request marker so the deletion happens at
   * most once even without any cluster lock. The marker is also cleaned up
   * for requests that can never execute anymore (revoked by an admin
   * re-activation, user already gone, externally synchronized account or
   * unreadable marker). Deliberately not re-checked: the admin deletion
   * option — the request was confirmed by its user under an enabled policy,
   * turning the option off only prevents new requests.
   *
   * @return true when the account deletion must proceed
   */
  @ContainerTransactional
  public boolean prepareDeletion(String username) throws Exception {
    SettingValue<?> settingValue = settingService.get(Context.USER.id(username),
                                                      DELETION_REQUEST_SCOPE,
                                                      DELETION_REQUEST_SETTING_NAME);
    if (settingValue == null || settingValue.getValue() == null) {
      return false;
    }
    long requestTime;
    try {
      requestTime = Long.parseLong(settingValue.getValue().toString());
    } catch (NumberFormatException e) {
      LOG.warn("Unreadable account deletion request time '{}' for user {}, discarding the request",
               settingValue.getValue(),
               username);
      removeDeletionRequest(username);
      return false;
    }
    if (System.currentTimeMillis() - requestTime < delayDays * 86_400_000L) {
      return false;
    }
    User user = organizationService.getUserHandler().findUserByName(username, UserStatus.ANY);
    if (user == null) {
      LOG.info("User {} requested the deletion of an account that no longer exists, discarding the request", username);
      removeDeletionRequest(username);
      return false;
    }
    if (user.isEnabled()) {
      LOG.info("User {} was re-activated after requesting the deletion of the account, revoking the request", username);
      removeDeletionRequest(username);
      return false;
    }
    if (!user.isInternalStore()) {
      LOG.warn("User {} requested the deletion of an externally synchronized account, discarding the request", username);
      removeDeletionRequest(username);
      return false;
    }
    removeDeletionRequest(username);
    return true;
  }

  /**
   * Deletes the account the way the users administration does: the social
   * identity is soft-deleted first, then the IDM user is removed without
   * broadcast — this ordering prevents any concurrent by-username resolution
   * from resurrecting the deleted identity while the IDM user still exists.
   * The user content (activities, comments, documents...) is preserved by
   * design and keeps being displayed.
   */
  @ContainerTransactional
  public void deleteAccount(String username) throws Exception {
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    identityManager.hardDeleteIdentity(identity);
    organizationService.getUserHandler().removeUser(username, false);
    indexingService.unindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
    broadcastEvent(username, identity.getId());
    LOG.info("Account of user {} deleted following the deletion request confirmed more than {} day(s) ago",
             username,
             delayDays);
  }

  private void removeDeletionRequest(String username) {
    settingService.remove(Context.USER.id(username), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
  }

  private void broadcastEvent(String username, String identityId) {
    try {
      listenerService.broadcast(ACCOUNT_DELETED_EVENT, username, identityId);
    } catch (Exception e) {
      LOG.warn("Error broadcasting event {} for user {} with identity id {}", ACCOUNT_DELETED_EVENT, username, identityId, e);
    }
  }

}
