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
package io.meeds.social.space.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.model.MembershipChange;
import io.meeds.social.space.model.MembershipChangeResult;
import io.meeds.social.space.security.service.SpaceSecurityService;

import jakarta.persistence.EntityManager;

/**
 * Pins the member writers: each one changes its own user's rows on the space's
 * locked rows, whatever the space it is handed lists, and no other writer of
 * an existing space rewrites the member rows from its arrays.
 */
public class SpaceMembershipWritersTest extends AbstractCoreTest {

  private static final String  MANAGER = "demo";

  private static final String  USER    = "mary";

  private static final String  OTHER   = "john";

  private SpaceStorage         spaceStorage;

  private SpaceSecurityService spaceSecurityService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    spaceSecurityService = getService(SpaceSecurityService.class);
    createIdentity("root");
    createIdentity(OTHER);
    createIdentity(USER);
    createIdentity(MANAGER);
  }

  public void testAStaleSpaceRestoresNoDemotedManager() throws Exception {
    Space space = createSpace();
    spaceService.addMember(space, USER);
    spaceService.setManager(spaceService.getSpaceById(space.getId()), USER, true);
    Space staleSpace = spaceService.getSpaceById(space.getId());
    assertTrue(spaceService.isManager(staleSpace, USER));

    spaceService.setManager(spaceService.getSpaceById(space.getId()), USER, false);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));

    spaceService.addMember(copy(staleSpace), OTHER);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));

    Space avatarUpdate = copy(staleSpace);
    avatarUpdate.setAvatarLastUpdated(System.currentTimeMillis());
    spaceStorage.saveSpace(avatarUpdate, false);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));

    Space rename = copy(staleSpace);
    rename.setDisplayName("renamed members space");
    rename.setPrettyName("renamed_members_space");
    spaceStorage.renameSpace(rename);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, OTHER));
  }

  public void testADisabledMemberKeepsTheirRows() throws Exception {
    Space space = createSpace();
    Identity disabled = createUserAndIdentity("disabledMember");
    spaceService.addMember(space, "disabledMember");
    disabled.setEnable(false);
    identityManager.updateIdentity(disabled);

    spaceService.addMember(spaceService.getSpaceById(space.getId()), OTHER);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, "disabledMember"));
  }

  public void testAddMemberOfAnInvitedUser() throws Exception {
    Space space = createSpace();
    spaceService.addInvitedUser(space, USER);
    assertEquals(Set.of(SpaceMembershipStatus.INVITED), statuses(space, USER));

    spaceService.addMember(spaceService.getSpaceById(space.getId()), USER);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
  }

  public void testInvitationOfAPendingUserMakesThemAMember() throws Exception {
    Space space = createSpace(Space.VALIDATION);
    spaceService.addPendingUser(space, USER);
    assertEquals(Set.of(SpaceMembershipStatus.PENDING), statuses(space, USER));

    spaceService.addInvitedUser(spaceService.getSpaceById(space.getId()), USER);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
  }

  public void testInvitationOfAMemberChangesNothing() throws Exception {
    Space space = createSpace();
    Space staleSpace = spaceService.getSpaceById(space.getId());
    spaceService.addMember(space, USER);

    spaceService.addInvitedUser(copy(staleSpace), USER);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
  }

  public void testAGrantToANonMemberChangesNothing() throws Exception {
    Space space = createSpace();
    List<SpaceLifeCycleEvent> events = listen();
    try {
      spaceService.setManager(space, USER, true);
      spaceService.addPublisher(space, USER);
      spaceService.addRedactor(space, USER);

      assertEquals(Collections.emptySet(), statuses(space, USER));
      assertTrue(events.isEmpty());
    } finally {
      unlisten();
    }
  }

  public void testAGrantRacingARemovalReAddsNoOne() throws Exception {
    Space space = createSpace();
    spaceService.addMember(space, USER);
    Space staleSpace = spaceService.getSpaceById(space.getId());
    spaceService.removeMember(spaceService.getSpaceById(space.getId()), USER);

    spaceService.setManager(copy(staleSpace), USER, true);

    assertEquals(Collections.emptySet(), statuses(space, USER));
  }

  public void testAManagerRowAloneGetsItsMemberRow() throws Exception {
    Space space = createSpace();
    spaceService.addMember(space, USER);
    spaceService.setManager(spaceService.getSpaceById(space.getId()), USER, true);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER, SpaceMembershipStatus.MANAGER), statuses(space, USER));

    spaceService.setManager(spaceService.getSpaceById(space.getId()), USER, false);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));

    spaceService.removeMember(spaceService.getSpaceById(space.getId()), USER);
    assertEquals(Collections.emptySet(), statuses(space, USER));
  }

  public void testAManagerRowAloneGetsItsMemberRowAtTheNextWrite() throws Exception {
    Space space = createSpace();
    insertManagerRowOnly(space, USER);
    assertEquals(Set.of(SpaceMembershipStatus.MANAGER), statuses(space, USER));

    spaceService.addMember(spaceService.getSpaceById(space.getId()), OTHER);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER, SpaceMembershipStatus.MANAGER), statuses(space, USER));
  }

  /**
   * A manager row alone counts as a member: its removal removes a member, so
   * the user leaves the space. Mutant: deciding before the backfill removes a
   * manager only, and nobody leaves.
   */
  public void testRemovingAManagerRowAloneRemovesAMember() throws Exception {
    Space space = createSpace();
    insertManagerRowOnly(space, USER);

    MembershipChangeResult result = spaceStorage.changeMembership(space.getSpaceId(),
                                                                  USER,
                                                                  MembershipChange.of(Collections.emptySet(),
                                                                                      Set.of(SpaceMembershipStatus.MEMBER,
                                                                                             SpaceMembershipStatus.MANAGER)));

    assertTrue(result.removed().contains(SpaceMembershipStatus.MEMBER));
    assertEquals(Collections.emptySet(), statuses(space, USER));
  }

  /**
   * A manager row alone counts as a member: adding the user as a member adds
   * nothing, so nobody joins. Mutant: deciding before the backfill adds a
   * member who already was one.
   */
  public void testAddingAManagerRowAloneAddsNoMember() throws Exception {
    Space space = createSpace();
    insertManagerRowOnly(space, USER);

    MembershipChangeResult result = spaceStorage.changeMembership(space.getSpaceId(),
                                                                  USER,
                                                                  MembershipChange.of(Set.of(SpaceMembershipStatus.MEMBER),
                                                                                      Collections.emptySet()));

    assertEquals(Collections.emptySet(), result.added());
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER, SpaceMembershipStatus.MANAGER), statuses(space, USER));
  }

  public void testDemotingAManagerRowAloneLeavesAMemberRow() throws Exception {
    Space space = createSpace();
    insertManagerRowOnly(space, USER);

    spaceService.setManager(spaceService.getSpaceById(space.getId()), USER, false);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
  }

  public void testRemovalThroughASpaceThatPredatesTheJoin() throws Exception {
    Space space = createSpace();
    Space staleSpace = spaceService.getSpaceById(space.getId());
    spaceService.addMember(space, USER);
    spaceService.addPublisher(spaceService.getSpaceById(space.getId()), USER);
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER, SpaceMembershipStatus.PUBLISHER), statuses(space, USER));

    spaceService.removeMember(copy(staleSpace), USER);

    assertEquals(Collections.emptySet(), statuses(space, USER));
  }

  public void testAnIgnoreAfterAnApprovalKeepsTheMember() throws Exception {
    Space space = createSpace(Space.VALIDATION);
    spaceService.addPendingUser(space, USER);
    spaceService.addMember(spaceService.getSpaceById(space.getId()), USER);

    spaceService.setIgnored(space.getId(), USER);

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
  }

  public void testAnIgnoreTogglesAndDropsTheInvitation() throws Exception {
    Space space = createSpace();
    spaceService.addInvitedUser(space, USER);

    spaceService.setIgnored(space.getId(), USER);
    assertEquals(Set.of(SpaceMembershipStatus.IGNORED), statuses(space, USER));

    spaceService.setIgnored(space.getId(), USER);
    assertEquals(Collections.emptySet(), statuses(space, USER));
  }

  public void testConcurrentJoinsOfTwoUsersBothStore() throws Exception {
    Space space = createSpace();
    Space cachedSpace = spaceService.getSpaceById(space.getId());
    restartTransaction();

    runConcurrently(() -> spaceService.addMember(copy(cachedSpace), USER),
                    () -> spaceService.addMember(copy(cachedSpace), OTHER));

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, OTHER));
  }

  public void testConcurrentJoinsOfOneUserStoreOneRow() throws Exception {
    Space space = createSpace();
    Space cachedSpace = spaceService.getSpaceById(space.getId());
    restartTransaction();

    runConcurrently(() -> spaceService.addMember(copy(cachedSpace), USER),
                    () -> spaceService.addMember(copy(cachedSpace), USER));

    assertEquals(Set.of(SpaceMembershipStatus.MEMBER), statuses(space, USER));
  }

  private void insertManagerRowOnly(Space space, String username) {
    getService(EntityManagerService.class).getEntityManager().clear();
    SpaceEntity spaceEntity = getService(SpaceDAO.class).find(space.getSpaceId());
    getService(SpaceMemberDAO.class).create(new SpaceMemberEntity(spaceEntity, username, SpaceMembershipStatus.MANAGER, Instant.now()));
    restartTransaction();
  }

  private Set<SpaceMembershipStatus> statuses(Space space, String username) {
    restartTransaction();
    return spaceSecurityService.getMemberStatuses(space.getSpaceId(), username);
  }

  /**
   * Runs both tasks at once, each in its own request and transaction. The test
   * database runs HSQLDB in MVCC mode, where a SELECT ... FOR UPDATE does not
   * block another one; in LOCKS mode it waits for the holder's commit, as
   * PostgreSQL and MySQL do, so the tasks run in that mode.
   */
  private void runConcurrently(Runnable first, Runnable second) throws Exception {
    setTransactionControl("LOCKS");
    try {
      runConcurrentlyWithLocks(first, second);
    } finally {
      setTransactionControl("MVCC");
    }
  }

  private void setTransactionControl(String mode) {
    restartTransaction();
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    boolean active = entityManager.getTransaction().isActive();
    if (!active) {
      entityManager.getTransaction().begin();
    }
    entityManager.createNativeQuery("SET DATABASE TRANSACTION CONTROL " + mode).executeUpdate();
    entityManager.getTransaction().commit();
    if (active) {
      entityManager.getTransaction().begin();
    }
  }

  private void runConcurrentlyWithLocks(Runnable first, Runnable second) throws Exception {
    ExoContainer container = getContainer();
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      List<Future<?>> futures = new ArrayList<>();
      for (Runnable task : List.of(first, second)) {
        futures.add(executor.submit(() -> {
          ExoContainerContext.setCurrentContainer(container);
          start.await();
          RequestLifeCycle.begin(container);
          try {
            task.run();
          } finally {
            RequestLifeCycle.end();
          }
          return null;
        }));
      }
      start.countDown();
      for (Future<?> future : futures) {
        future.get(60, TimeUnit.SECONDS);
      }
    } finally {
      executor.shutdownNow();
    }
  }

  private List<SpaceLifeCycleEvent> listened;

  private SpaceLifeCycleListener listener;

  private List<SpaceLifeCycleEvent> listen() {
    listened = Collections.synchronizedList(new ArrayList<>());
    listener = new SpaceLifeCycleListener() {
      @Override
      public void grantedLead(SpaceLifeCycleEvent event) {
        listened.add(event);
      }

      @Override
      public void addPublisherUser(SpaceLifeCycleEvent event) {
        listened.add(event);
      }

      @Override
      public void addRedactorUser(SpaceLifeCycleEvent event) {
        listened.add(event);
      }
    };
    spaceService.registerSpaceLifeCycleListener(listener);
    return listened;
  }

  private void unlisten() {
    spaceService.unregisterSpaceLifeCycleListener(listener);
  }

  private Space copy(Space space) {
    Space copy = new Space();
    copy.setId(space.getId());
    copy.setDisplayName(space.getDisplayName());
    copy.setPrettyName(space.getPrettyName());
    copy.setGroupId(space.getGroupId());
    copy.setUrl(space.getUrl());
    copy.setRegistration(space.getRegistration());
    copy.setVisibility(space.getVisibility());
    copy.setDescription(space.getDescription());
    copy.setExtendedProperties(space.getExtendedProperties());
    copy.setManagers(space.getManagers());
    copy.setMembers(space.getMembers());
    copy.setRedactors(space.getRedactors());
    copy.setPublishers(space.getPublishers());
    copy.setInvitedUsers(space.getInvitedUsers());
    copy.setPendingUsers(space.getPendingUsers());
    copy.setCreatedTime(space.getCreatedTime());
    copy.setPublicSiteVisibility(space.getPublicSiteVisibility());
    return copy;
  }

  private Space createSpace() throws Exception {
    return createSpace(Space.OPEN);
  }

  private Space createSpace(String registration) throws Exception {
    Space space = new Space();
    space.setDisplayName("members space");
    space.setPrettyName("members_space");
    space.setRegistration(registration);
    space.setVisibility(Space.PUBLIC);
    space.setGroupId(SpaceUtils.createGroup(space.getDisplayName(), MANAGER));
    space.setUrl(space.getPrettyName());
    space.setManagers(new String[] { MANAGER });
    space.setMembers(new String[] { MANAGER });
    space.setEditor(MANAGER);
    space = spaceStorage.saveSpace(space, true);
    restartTransaction();
    return spaceService.getSpaceById(space.getId());
  }

}
