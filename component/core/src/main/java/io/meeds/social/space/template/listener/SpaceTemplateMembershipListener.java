/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.space.template.listener;

import io.meeds.social.space.template.model.SpaceTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.Collections;
import java.util.List;

import static io.meeds.social.space.template.service.SpaceTemplateService.SPACE_TEMPLATE_CREATED_EVENT;
import static io.meeds.social.space.template.service.SpaceTemplateService.SPACE_TEMPLATE_UPDATED_EVENT;

@Asynchronous
@Component
public class SpaceTemplateMembershipListener extends Listener<String, SpaceTemplate> {

  private static final String[] LISTENER_EVENTS = { SPACE_TEMPLATE_CREATED_EVENT, SPACE_TEMPLATE_UPDATED_EVENT };

  private static final Log      LOG             = ExoLogger.getLogger(SpaceTemplateMembershipListener.class);

  @Autowired
  private ListenerService       listenerService;

  @Autowired
  private OrganizationService   organizationService;

  @Autowired
  private IdentityRegistry      identityRegistry;

  @Autowired
  private ConversationRegistry  conversationRegistry;

  @PostConstruct
  public void init() {
    for (String event : LISTENER_EVENTS) {
      this.listenerService.addListener(event, this);
    }
  }

  @Override
  public void onEvent(Event event) throws Exception {
    switch (event.getEventName()) {
      case SPACE_TEMPLATE_CREATED_EVENT -> handleSpaceTemplateCreation(event);
      case SPACE_TEMPLATE_UPDATED_EVENT -> handleSpaceTemplateUpdate(event);
    }
  }

  private void handleSpaceTemplateUpdate(Event event) {
    SpaceTemplate storedSpaceTemplate = (SpaceTemplate) event.getSource();
    SpaceTemplate updatedSpaceTemplate = (SpaceTemplate) event.getData();
    if (storedSpaceTemplate != null && updatedSpaceTemplate != null) {
      List<NestedMembership> storedMembership = getEnclosingSpaceTemplateMemberships(storedSpaceTemplate);
      List<NestedMembership> updatedMembership = getEnclosingSpaceTemplateMemberships(updatedSpaceTemplate);
      List<NestedMembership> toAdd = updatedMembership.stream().filter(m -> !storedMembership.contains(m)).toList();
      List<NestedMembership> toRemove = storedMembership.stream().filter(m -> !updatedMembership.contains(m)).toList();
      clearEnclosingMembershipCache(toAdd);
      clearEnclosingMembershipCache(toRemove);
    }
  }

  private void handleSpaceTemplateCreation(Event event) {
    SpaceTemplate createdSpaceTemplate = (SpaceTemplate) event.getData();
    if (createdSpaceTemplate != null) {
      List<NestedMembership> enclosingMemberships = getEnclosingSpaceTemplateMemberships(createdSpaceTemplate);
      clearEnclosingMembershipCache(enclosingMemberships);
    }
  }

  private List<NestedMembership> getEnclosingSpaceTemplateMemberships(SpaceTemplate spaceTemplate) {
    String spaceTemplateGroupId = spaceTemplate.getGroupId();
    if (CollectionUtils.isNotEmpty(spaceTemplate.getEnclosingMemberships())) {
      return spaceTemplate.getEnclosingMemberships().stream().map(expression -> {
        return NestedMembership.parseEnclosingMembership(expression, spaceTemplateGroupId);
      }).toList();
    }
    return Collections.emptyList();
  }

  private void clearEnclosingMembershipCache(List<NestedMembership> enclosingMemberships) {
    for (NestedMembership enclosingMembership : enclosingMemberships) {
      organizationService.getGroupHandler().clearGroupCache(enclosingMembership.getNestedGroupId());
      refreshIdentitiesMemberships(enclosingMembership);
    }
  }

  private void refreshIdentitiesMemberships(NestedMembership nestedMembership) {
    List<Identity> identities;
    try {
      identities = identityRegistry.getIdentities()
                                   .stream()
                                   .map(identity -> (Identity) identity)
                                   .filter(identity -> hasMatchingMembership(identity, nestedMembership))
                                   .toList();
    } catch (Exception exception) {
      LOG.error("Error while fetching cached identities", exception);
      return;
    }
    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(identities)) {
      for (Identity identity : identities) {
        identityRegistry.unregister(identity.getUserId());
        conversationRegistry.unregisterByUserId(identity.getUserId());
      }
    }
  }

  private boolean hasMatchingMembership(Identity identity, NestedMembership nestedMembership) {
    if (identity.getMemberships() == null) {
      return false;
    }
    return identity.getMemberships()
                   .stream()
                   .anyMatch(m -> m.getGroup().equals(nestedMembership.getNestedGroupId())
                       && (nestedMembership.isIncludeAllMembershipTypes()
                           || m.getMembershipType().equals(nestedMembership.getNestedMembershipType())));
  }
}
