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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.meeds.social.space.template.service.SpaceTemplateService.SPACE_TEMPLATE_CREATED_EVENT;
import static io.meeds.social.space.template.service.SpaceTemplateService.SPACE_TEMPLATE_UPDATED_EVENT;

@Asynchronous
@Component
public class SpaceTemplateMembershipListener extends Listener<String, SpaceTemplate> {

  private static final String[] LISTENER_EVENTS = { SPACE_TEMPLATE_CREATED_EVENT, SPACE_TEMPLATE_UPDATED_EVENT };

  private static final Log      LOG             = ExoLogger.getLogger(SpaceTemplateMembershipListener.class);

  private static final int      PAGE_SIZE       = 100;

  @Autowired
  private ListenerService       listenerService;

  @Autowired
  private OrganizationService   organizationService;

  @Autowired
  private ConversationRegistry  conversationRegistry;

  @Autowired
  private SpaceService          spaceService;

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
      clearEnclosingMembershipCache(updatedSpaceTemplate,
                                    Stream.concat(toAdd.stream(), toRemove.stream()).toList());
    }
  }

  private void handleSpaceTemplateCreation(Event event) {
    SpaceTemplate createdSpaceTemplate = (SpaceTemplate) event.getData();
    if (createdSpaceTemplate != null) {
      List<NestedMembership> enclosingMemberships = getEnclosingSpaceTemplateMemberships(createdSpaceTemplate);
      clearEnclosingMembershipCache(createdSpaceTemplate, enclosingMemberships);
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

  private void clearEnclosingMembershipCache(SpaceTemplate spaceTemplate, List<NestedMembership> enclosingMemberships) {
    if (enclosingMemberships.isEmpty()) {
      return;
    }
    for (NestedMembership enclosingMembership : enclosingMemberships) {
      organizationService.getGroupHandler().clearGroupCache(enclosingMembership.getNestedGroupId());
    }
    findMembers(spaceTemplate).forEach(conversationRegistry::invalidateUser);
  }

  /**
   * The users the template's enclosing memberships reach, read from the
   * store, not from the identities this node holds, so that a user signed in
   * on another node only is invalidated as well, on every node: the members of
   * every space created from the template, whose space group is nested into
   * the template group without a stored membership, and the template group's
   * own members, of any membership type.
   */
  private Set<String> findMembers(SpaceTemplate spaceTemplate) {
    Set<String> userIds = new HashSet<>();
    try {
      Group group = organizationService.getGroupHandler().findGroupById(spaceTemplate.getGroupId());
      if (group != null) {
        ListAccess<Membership> memberships = organizationService.getMembershipHandler().findAllMembershipsByGroup(group);
        int size = memberships.getSize();
        for (int offset = 0; offset < size; offset += PAGE_SIZE) {
          Arrays.stream(memberships.load(offset, Math.min(PAGE_SIZE, size - offset)))
                .map(Membership::getUserName)
                .forEach(userIds::add);
        }
      }
      SpaceFilter spaceFilter = new SpaceFilter();
      spaceFilter.setTemplateIds(List.of(spaceTemplate.getId()));
      ListAccess<Space> spaces = spaceService.getAllSpacesByFilter(spaceFilter);
      int size = spaces.getSize();
      for (int offset = 0; offset < size; offset += PAGE_SIZE) {
        for (Space space : spaces.load(offset, Math.min(PAGE_SIZE, size - offset))) {
          addAll(userIds, space.getMembers());
          addAll(userIds, space.getManagers());
        }
      }
    } catch (Exception exception) {
      LOG.error("Error while reading the members of the spaces of template {}", spaceTemplate.getId(), exception);
    }
    return userIds;
  }

  private void addAll(Set<String> userIds, String[] users) {
    if (users != null) {
      userIds.addAll(Arrays.asList(users));
    }
  }

}
