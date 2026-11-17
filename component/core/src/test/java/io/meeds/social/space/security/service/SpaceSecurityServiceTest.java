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
package io.meeds.social.space.security.service;

import static org.junit.Assert.assertThrows;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.plugin.SpaceExtendedPropertiesLifeCycleEvent;
import io.meeds.social.space.security.exception.SpaceSecurityRefusedException;
import io.meeds.social.space.security.model.SpaceSecuritySettings;

/**
 * Pins the space security policy against the real cache and database: every
 * case lets the cached space disagree with the committed rows, written
 * without eviction, and runs as mary, who is not a platform administrator.
 * The spaces have no template, as the test database holds no template row:
 * the template's delegated administrators are pinned by
 * {@link SpaceSecurityServiceTemplateAdministrationTest}.
 */
public class SpaceSecurityServiceTest extends AbstractCoreTest {

  private static final String  USER        = "mary";

  private static final String  MANAGER     = "demo";

  private static final String  ADMIN       = "root";

  private SpaceSecurityService spaceSecurityService;

  private SpaceStorage         spaceStorage;

  private SpaceDAO             spaceDAO;

  private SpaceMemberDAO       spaceMemberDAO;

  private EntityManagerService entityManagerService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceSecurityService = getService(SpaceSecurityService.class);
    spaceStorage = getService(SpaceStorage.class);
    spaceDAO = getService(SpaceDAO.class);
    spaceMemberDAO = getService(SpaceMemberDAO.class);
    entityManagerService = getService(EntityManagerService.class);
    createIdentity(ADMIN);
    createIdentity("john");
    createIdentity(USER);
    createIdentity(MANAGER);
  }

  public void testGetSettingsReadsTheCommittedRow() throws Exception {
    Space space = createSpace(new String[0]);
    assertFalse(spaceSecurityService.getDisplaySettings(cachedSpace(space)).isKeepDocumentsInsideOn());

    writeExtendedProperties(space, Map.of(SpaceSecuritySettings.KEEP_DOCUMENTS_INSIDE_KEY, "true"));

    assertFalse(spaceSecurityService.getDisplaySettings(cachedSpace(space)).isKeepDocumentsInsideOn());
    assertTrue(spaceSecurityService.getSettings(space.getSpaceId()).isKeepDocumentsInsideOn());
  }

  public void testGetSettingsDefaults() throws Exception {
    Space space = createSpace(new String[0]);
    writeExtendedProperties(space, null);

    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertEquals(Boolean.FALSE, settings.getLimitAccessManagement());
    assertEquals(Boolean.FALSE, settings.getKeepDocumentsInside());
    assertEquals(Boolean.TRUE, settings.getExternalMembersAllowed());
  }

  public void testGetSettingsOfUnknownSpace() {
    assertThrows(ObjectNotFoundException.class, () -> spaceSecurityService.getSettings(Long.MAX_VALUE));
  }

  public void testLimitAccessManagementOffAllowsAnyone() throws Exception {
    Space space = createSpace(new String[0]);
    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertTrue(spaceSecurityService.canManageDocumentsAccess(space, settings, USER));
  }

  /** Case (a): the cached space lists the user as a manager, the MANAGER row is gone. */
  public void testRefusesAManagerWhoseManagerRowIsDeleted() throws Exception {
    Space space = createSpace(new String[] { USER });
    limitAccessManagement(space);
    Space cachedSpace = cachedSpace(space);
    assertTrue(spaceService.isManager(cachedSpace, USER));

    deleteRow(space, USER, SpaceMembershipStatus.MANAGER);

    assertTrue(spaceService.isManager(cachedSpace(space), USER));
    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertFalse(spaceSecurityService.canManageDocumentsAccess(cachedSpace, settings, USER));
    assertFalse(spaceSecurityService.canManageSpace(cachedSpace, USER));
    SpaceSecurityRefusedException exception =
                                            assertThrows(SpaceSecurityRefusedException.class,
                                                         () -> spaceSecurityService.updateSettings(space.getSpaceId(),
                                                                                                   keepDocumentsInside(true),
                                                                                                   USER));
    assertEquals("space.security.notAllowed", exception.getMessageCode());
    assertFalse(spaceSecurityService.getSettings(space.getSpaceId()).isKeepDocumentsInsideOn());
  }

  /** Case (b): the cached space lists the user as neither, the rows are then inserted. */
  public void testAllowsAManagerWhoseRowsAreInserted() throws Exception {
    Space space = createSpace(new String[0]);
    limitAccessManagement(space);
    Space cachedSpace = cachedSpace(space);
    assertFalse(spaceService.isMember(cachedSpace, USER));

    insertRow(space, USER, SpaceMembershipStatus.MANAGER);
    insertRow(space, USER, SpaceMembershipStatus.MEMBER);

    assertFalse(spaceService.isMember(cachedSpace(space), USER));
    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertTrue(spaceSecurityService.canManageDocumentsAccess(cachedSpace, settings, USER));
    assertTrue(spaceSecurityService.updateSettings(space.getSpaceId(), keepDocumentsInside(true), USER)
                                   .isKeepDocumentsInsideOn());
  }

  /** Case (c): the cached space doesn't list the user as a publisher, the PUBLISHER row is inserted. */
  public void testAllowsAPublisherWhoseRowIsInserted() throws Exception {
    Space space = createSpace(new String[0]);
    limitAccessManagement(space);
    Space cachedSpace = cachedSpace(space);
    assertFalse(spaceService.isPublisher(cachedSpace, USER));

    insertRow(space, USER, SpaceMembershipStatus.MEMBER);
    insertRow(space, USER, SpaceMembershipStatus.PUBLISHER);

    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertTrue(spaceSecurityService.canManageDocumentsAccess(cachedSpace(space), settings, USER));
  }

  /** Case (d): the cached space lists the user as a publisher, the PUBLISHER row is gone. */
  public void testRefusesAPublisherWhosePublisherRowIsDeleted() throws Exception {
    Space space = createSpace(new String[0]);
    space.setMembers(new String[] { MANAGER, USER });
    space.setPublishers(new String[] { USER });
    space = spaceStorage.saveSpace(space, false);
    limitAccessManagement(space);
    Space cachedSpace = cachedSpace(space);
    assertTrue(spaceService.isPublisher(cachedSpace, USER));

    deleteRow(space, USER, SpaceMembershipStatus.PUBLISHER);

    assertTrue(spaceService.isPublisher(cachedSpace(space), USER));
    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertFalse(spaceSecurityService.canManageDocumentsAccess(cachedSpace, settings, USER));
  }

  /** Case (i): a user holding only a MANAGER row is a space admin. */
  public void testAllowsAUserHoldingOnlyAManagerRow() throws Exception {
    Space space = createSpace(new String[0]);
    limitAccessManagement(space);

    insertRow(space, USER, SpaceMembershipStatus.MANAGER);

    assertEquals(Set.of(SpaceMembershipStatus.MANAGER), spaceSecurityService.getMemberStatuses(space.getSpaceId(), USER));
    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertTrue(spaceSecurityService.canManageDocumentsAccess(cachedSpace(space), settings, USER));
    assertTrue(spaceSecurityService.updateSettings(space.getSpaceId(), keepDocumentsInside(true), USER)
                                   .isKeepDocumentsInsideOn());
  }

  public void testRefusesAMemberAndAllowsAPlatformAdministrator() throws Exception {
    Space space = createSpace(new String[0]);
    limitAccessManagement(space);
    insertRow(space, USER, SpaceMembershipStatus.MEMBER);
    insertRow(space, USER, SpaceMembershipStatus.REDACTOR);

    SpaceSecuritySettings settings = spaceSecurityService.getSettings(space.getSpaceId());
    assertFalse(spaceSecurityService.canManageDocumentsAccess(cachedSpace(space), settings, USER));
    assertTrue(spaceSecurityService.canManageDocumentsAccess(cachedSpace(space), settings, ADMIN));
  }

  public void testExternalMembersSettingNeedsAPlatformAdministrator() throws Exception {
    Space space = createSpace(new String[0]);
    SpaceSecuritySettings changes = new SpaceSecuritySettings(true, null, false);

    assertThrows(SpaceSecurityRefusedException.class,
                 () -> spaceSecurityService.updateSettings(space.getSpaceId(), changes, MANAGER));
    SpaceSecuritySettings stored = spaceSecurityService.getSettings(space.getSpaceId());
    assertFalse(stored.isLimitAccessManagementOn());
    assertTrue(stored.getExternalMembersAllowed());

    stored = spaceSecurityService.updateSettings(space.getSpaceId(), changes, ADMIN);
    assertTrue(stored.isLimitAccessManagementOn());
    assertFalse(stored.getExternalMembersAllowed());
  }

  public void testUpdateSettingsReportsTheStoredChange() throws Exception {
    Space space = createSpace(new String[0]);
    List<SpaceExtendedPropertiesLifeCycleEvent> events = new ArrayList<>();
    SpaceLifeCycleListener listener = new SpaceLifeCycleListener() {
      @Override
      public void extendedPropertiesUpdated(SpaceExtendedPropertiesLifeCycleEvent event) {
        events.add(event);
      }
    };
    spaceService.registerSpaceLifeCycleListener(listener);
    try {
      spaceSecurityService.updateSettings(space.getSpaceId(), keepDocumentsInside(true), MANAGER);
      assertEquals(1, events.size());
      assertEquals(List.of(SpaceSecuritySettings.KEEP_DOCUMENTS_INSIDE_KEY), events.get(0).getChangedProperties());
      assertTrue(spaceSecurityService.getDisplaySettings(events.get(0).getSpace()).isKeepDocumentsInsideOn());

      events.clear();
      spaceSecurityService.updateSettings(space.getSpaceId(), keepDocumentsInside(true), MANAGER);
      assertTrue(events.isEmpty());
    } finally {
      spaceService.unregisterSpaceLifeCycleListener(listener);
    }
  }

  public void testUpdateSpaceReportsNoSecurityKeyAndKeepsItsValue() throws Exception {
    Space space = createSpace(new String[0]);
    Space staleSpace = spaceService.getSpaceById(space.getId());
    spaceSecurityService.updateSettings(space.getSpaceId(), keepDocumentsInside(true), MANAGER);
    List<SpaceExtendedPropertiesLifeCycleEvent> events = new ArrayList<>();
    SpaceLifeCycleListener listener = new SpaceLifeCycleListener() {
      @Override
      public void extendedPropertiesUpdated(SpaceExtendedPropertiesLifeCycleEvent event) {
        events.add(event);
      }
    };
    spaceService.registerSpaceLifeCycleListener(listener);
    try {
      Map<String, String> properties = new HashMap<>();
      properties.put(SpaceSecuritySettings.KEEP_DOCUMENTS_INSIDE_KEY, "false");
      properties.put(SpaceSecuritySettings.LIMIT_ACCESS_MANAGEMENT_KEY, "true");
      properties.put("other.key", "value");
      staleSpace.setExtendedProperties(properties);
      staleSpace.setEditor(MANAGER);
      spaceService.updateSpace(staleSpace);

      assertEquals(1, events.size());
      assertEquals(List.of("other.key"), events.get(0).getChangedProperties());
      SpaceSecuritySettings stored = spaceSecurityService.getSettings(space.getSpaceId());
      assertTrue(stored.isKeepDocumentsInsideOn());
      assertFalse(stored.isLimitAccessManagementOn());
    } finally {
      spaceService.unregisterSpaceLifeCycleListener(listener);
    }
  }

  public void testSaveAndRenameKeepTheStoredSettings() throws Exception {
    Space space = createSpace(new String[0]);
    Space staleSpace = spaceService.getSpaceById(space.getId());
    spaceSecurityService.updateSettings(space.getSpaceId(), new SpaceSecuritySettings(true, true, null), MANAGER);

    staleSpace.setDescription("saved through a stale space");
    spaceStorage.saveSpace(staleSpace, false);
    SpaceSecuritySettings stored = spaceSecurityService.getSettings(space.getSpaceId());
    assertTrue(stored.isLimitAccessManagementOn());
    assertTrue(stored.isKeepDocumentsInsideOn());
    assertEquals("saved through a stale space", spaceService.getSpaceById(space.getId()).getDescription());

    staleSpace.setPrettyName("renamed_space");
    staleSpace.setDisplayName("renamed space");
    spaceStorage.renameSpace(staleSpace);
    stored = spaceSecurityService.getSettings(space.getSpaceId());
    assertTrue(stored.isLimitAccessManagementOn());
    assertTrue(stored.isKeepDocumentsInsideOn());
  }

  public void testGetCommittedMembers() throws Exception {
    Space space = createSpace(new String[0]);
    createUserAndIdentity("securityTestMember1");
    createUserAndIdentity("securityTestManagerOnly");
    Identity disabled = createUserAndIdentity("securityTestDisabledMember");
    createUserAndIdentity("securityTestInvitedOnly");
    insertRow(space, "securityTestMember1", SpaceMembershipStatus.MEMBER);
    insertRow(space, "securityTestManagerOnly", SpaceMembershipStatus.MANAGER);
    insertRow(space, "securityTestDisabledMember", SpaceMembershipStatus.MEMBER);
    insertRow(space, "securityTestInvitedOnly", SpaceMembershipStatus.INVITED);
    disabled.setEnable(false);
    identityManager.updateIdentity(disabled);
    // a disabled user named after the space is not its enabled space identity
    Identity sameName = createUserAndIdentity(space.getPrettyName());
    insertRow(space, space.getPrettyName(), SpaceMembershipStatus.MEMBER);
    sameName.setEnable(false);
    identityManager.updateIdentity(sameName);
    assertNotNull(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()));
    // an enabled user whose identity is deleted
    Identity deleted = createUserAndIdentity("securityTestDeletedMember");
    insertRow(space, "securityTestDeletedMember", SpaceMembershipStatus.MEMBER);
    deleted.setDeleted(true);
    identityManager.updateIdentity(deleted);
    assertTrue(identityManager.getIdentity(deleted.getId()).isDeleted());

    Set<String> members = spaceSecurityService.getCommittedMembers(space.getSpaceId(),
                                                                   List.of("securityTestMember1",
                                                                           "securityTestManagerOnly",
                                                                           "securityTestDisabledMember",
                                                                           "securityTestInvitedOnly",
                                                                           space.getPrettyName(),
                                                                           "securityTestDeletedMember",
                                                                           "unknown"));
    assertEquals(Set.of("securityTestMember1", "securityTestManagerOnly"), members);
  }

  private Space createSpace(String[] managers) throws Exception {
    Space space = new Space();
    space.setDisplayName("security space");
    space.setPrettyName("security_space");
    space.setRegistration(Space.OPEN);
    space.setVisibility(Space.PUBLIC);
    space.setGroupId("/spaces/security_space");
    space.setUrl(space.getPrettyName());
    List<String> spaceManagers = new ArrayList<>(List.of(MANAGER));
    spaceManagers.addAll(List.of(managers));
    space.setManagers(spaceManagers.toArray(new String[0]));
    space.setMembers(spaceManagers.toArray(new String[0]));
    space.setEditor(MANAGER);
    space = spaceStorage.saveSpace(space, true);
    restartTransaction();
    return space;
  }

  private Space cachedSpace(Space space) {
    return spaceStorage.getSpaceById(space.getSpaceId());
  }

  private void limitAccessManagement(Space space) throws Exception {
    spaceSecurityService.updateSettings(space.getSpaceId(), new SpaceSecuritySettings(true, null, null), ADMIN);
    restartTransaction();
  }

  private SpaceSecuritySettings keepDocumentsInside(boolean value) {
    return new SpaceSecuritySettings(null, value, null);
  }

  private void insertRow(Space space, String username, SpaceMembershipStatus status) {
    entityManagerService.getEntityManager().clear();
    SpaceEntity spaceEntity = spaceDAO.find(space.getSpaceId());
    spaceMemberDAO.create(new SpaceMemberEntity(spaceEntity, username, status, Instant.now()));
    restartTransaction();
  }

  private void deleteRow(Space space, String username, SpaceMembershipStatus status) {
    entityManagerService.getEntityManager().clear();
    SpaceMemberEntity member = spaceMemberDAO.getSpaceMemberShip(username, space.getSpaceId(), status);
    assertNotNull(member);
    member.getSpace().getMembers().remove(member);
    spaceMemberDAO.delete(member);
    restartTransaction();
  }

  private void writeExtendedProperties(Space space, Map<String, String> properties) {
    entityManagerService.getEntityManager().clear();
    SpaceEntity spaceEntity = spaceDAO.find(space.getSpaceId());
    spaceEntity.setExtendedProperties(properties == null ? null : new HashMap<>(properties));
    spaceDAO.update(spaceEntity);
    restartTransaction();
  }

}
