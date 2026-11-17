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
package org.exoplatform.social.core.storage.cache;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.mockito.stubbing.Answer;

import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

import jakarta.persistence.EntityManager;

/**
 * The space cache maps a space's committed row, never the state of an entity
 * the thread's persistence context holds, and an eviction invalidates the
 * loads in flight: of the space, of its refs, of the space lists and of the
 * space identity lists.
 */
public class CachedSpaceStorageCommittedReadTest extends AbstractCoreTest {

  private static final String OLD_DESCRIPTION = "old";

  private static final String NEW_DESCRIPTION = "new";

  private CachedSpaceStorage  cachedSpaceStorage;

  private SpaceDAO            spaceDAO;

  private Space               space;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    cachedSpaceStorage = getService(CachedSpaceStorage.class);
    spaceDAO = getService(SpaceDAO.class);
    createIdentity("john");
    space = cachedSpaceStorage.saveSpace(newSpace("committed_read"), true);
    restartTransaction();
  }

  public void testAMissMapsTheCommittedRowNotTheHeldEntity() throws Exception {
    holdTheSpaceThenCommitANewDescription();
    cachedSpaceStorage.clearSpaceCached(space.getId());

    assertEquals(NEW_DESCRIPTION, cachedSpaceStorage.getSpaceById(space.getSpaceId()).getDescription());
  }

  public void testARefMissMapsTheCommittedRowNotTheHeldEntity() throws Exception {
    holdTheSpaceThenCommitANewDescription();
    cachedSpaceStorage.clearSpaceCached(space.getId());

    assertEquals(NEW_DESCRIPTION, cachedSpaceStorage.getSpaceByPrettyName(space.getPrettyName()).getDescription());
    cachedSpaceStorage.clearSpaceCached(space.getId());
    assertEquals(NEW_DESCRIPTION, cachedSpaceStorage.getSpaceByGroupId(space.getGroupId()).getDescription());
  }

  public void testTheThreadsOwnChangeIsReadInsideItsTransaction() {
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    entityManager.getTransaction().begin();
    try {
      SpaceEntity held = spaceDAO.find(space.getSpaceId());
      held.setDescription(NEW_DESCRIPTION);

      assertEquals(NEW_DESCRIPTION, spaceDAO.findCommitted(space.getSpaceId()).getDescription());
    } finally {
      entityManager.getTransaction().rollback();
    }
  }

  public void testAGoneRowIsNullWithoutMarkingTheTransactionForRollback() {
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    entityManager.getTransaction().begin();
    try {
      assertNull(spaceDAO.findCommitted(Long.MAX_VALUE));
      assertFalse(entityManager.getTransaction().getRollbackOnly());
    } finally {
      entityManager.getTransaction().rollback();
    }
  }

  public void testAnEvictionInvalidatesALoadInFlight() throws Exception {
    CountDownLatch loaded = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    SpaceDAO blockingSpaceDAO = spy(spaceDAO);
    doAnswer(blockOnce(loaded, release)).when(blockingSpaceDAO).findCommitted(anyLong());
    CachedSpaceStorage storage = newCachedSpaceStorage(blockingSpaceDAO);
    storage.clearSpaceCached(space.getId());

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      // a load reads the old row, and is held before it stores it
      Future<Space> inFlight = submitInContainer(executor, () -> storage.getSpaceById(space.getSpaceId()));
      assertTrue(loaded.await(30, TimeUnit.SECONDS));

      // the row changes, and the writer evicts the space
      commitNewDescription();
      storage.clearSpaceCached(space.getId());
      release.countDown();
      assertEquals(OLD_DESCRIPTION, inFlight.get(30, TimeUnit.SECONDS).getDescription());
    } finally {
      release.countDown();
      executor.shutdownNow();
    }

    restartTransaction();
    assertEquals(NEW_DESCRIPTION, storage.getSpaceById(space.getSpaceId()).getDescription());
  }

  public void testAnEvictionOnAMissInvalidatesARefLoadInFlight() throws Exception {
    CountDownLatch loaded = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    SpaceDAO blockingSpaceDAO = spy(spaceDAO);
    doAnswer(blockOnce(loaded, release)).when(blockingSpaceDAO).findIdByPrettyName(space.getPrettyName());
    CachedSpaceStorage storage = newCachedSpaceStorage(blockingSpaceDAO);
    storage.clearSpaceCached(space.getId());

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      // a load resolves the pretty name to the space, and is held before it stores it
      Future<Space> inFlight = submitInContainer(executor, () -> storage.getSpaceByPrettyName(space.getPrettyName()));
      assertTrue(loaded.await(30, TimeUnit.SECONDS));

      // the space is renamed, and evicted while the cache does not hold it
      commitNewPrettyName("renamed_space");
      storage.clearSpaceCached(space.getId());
      release.countDown();
      inFlight.get(30, TimeUnit.SECONDS);
    } finally {
      release.countDown();
      executor.shutdownNow();
    }

    restartTransaction();
    assertNull(storage.getSpaceByPrettyName(space.getPrettyName()));
  }

  public void testAnEvictionOfACachedSpaceInvalidatesARefLoadInFlight() throws Exception {
    CountDownLatch loaded = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    SpaceDAO blockingSpaceDAO = spy(spaceDAO);
    doAnswer(blockOnce(loaded, release)).when(blockingSpaceDAO).findIdByPrettyName(space.getPrettyName());
    CachedSpaceStorage storage = newCachedSpaceStorage(blockingSpaceDAO);
    storage.clearSpaceCached(space.getId());
    // the cache holds the space under its old pretty name
    assertEquals(space.getPrettyName(), storage.getSpaceById(space.getSpaceId()).getPrettyName());

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      // a load resolves the pretty name to the space, and is held before it stores it
      Future<Space> inFlight = submitInContainer(executor, () -> storage.getSpaceByPrettyName(space.getPrettyName()));
      assertTrue(loaded.await(30, TimeUnit.SECONDS));

      // the space is renamed, and evicted with the refs of its cached state
      commitNewPrettyName("renamed_space");
      storage.clearSpaceCached(space.getId());
      release.countDown();
      inFlight.get(30, TimeUnit.SECONDS);
    } finally {
      release.countDown();
      executor.shutdownNow();
    }

    restartTransaction();
    assertNull(storage.getSpaceByPrettyName(space.getPrettyName()));
  }

  public void testAWriteInvalidatesASpaceListLoadInFlight() throws Exception {
    CountDownLatch loaded = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    SpaceDAO blockingSpaceDAO = spy(spaceDAO);
    doAnswer(blockOnce(loaded, release)).when(blockingSpaceDAO).getLastSpaces(anyInt());
    CachedSpaceStorage storage = newCachedSpaceStorage(blockingSpaceDAO);
    storage.clearSpaceCached(space.getId());

    Space created;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      // a list load reads the one space, and is held before it stores the list
      Future<List<Space>> inFlight = submitInContainer(executor, () -> storage.getLastSpaces(10));
      assertTrue(loaded.await(30, TimeUnit.SECONDS));

      // another space is created
      created = storage.saveSpace(newSpace("committed_read_second"), true);
      release.countDown();
      assertEquals(1, inFlight.get(30, TimeUnit.SECONDS).size());
    } finally {
      release.countDown();
      executor.shutdownNow();
    }

    restartTransaction();
    assertEquals(Set.of(created.getId(), space.getId()),
                 storage.getLastSpaces(10).stream().map(Space::getId).collect(Collectors.toSet()));
  }

  public void testAnEvictionInvalidatesASpaceIdentityListLoadInFlight() throws Exception {
    CountDownLatch loaded = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    RDBMSIdentityStorageImpl blockingIdentityStorage = spy(getService(RDBMSIdentityStorageImpl.class));
    doAnswer(blockOnce(loaded, release)).when(blockingIdentityStorage)
                                        .getSpaceMemberIdentitiesByProfileFilter(any(),
                                                                                 any(),
                                                                                 any(),
                                                                                 anyLong(),
                                                                                 anyLong());
    CachedIdentityStorage identityStorage = new CachedIdentityStorage(blockingIdentityStorage,
                                                                      getService(SocialStorageCacheService.class),
                                                                      getService(FileService.class));
    ProfileFilter profileFilter = new ProfileFilter();
    identityStorage.clearSpaceIdentityLists();

    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      // a load reads the space members, and is held before it stores the list
      Future<List<Identity>> inFlight = submitInContainer(executor, () -> membersOf(identityStorage, profileFilter));
      assertTrue(loaded.await(30, TimeUnit.SECONDS));

      // a membership write evicts the space identity lists
      identityStorage.clearSpaceIdentityLists();
      release.countDown();
      inFlight.get(30, TimeUnit.SECONDS);
    } finally {
      release.countDown();
      executor.shutdownNow();
    }

    // the next read loads the members again instead of the list read before the eviction
    membersOf(identityStorage, profileFilter);
    verify(blockingIdentityStorage, times(2)).getSpaceMemberIdentitiesByProfileFilter(any(),
                                                                                     any(),
                                                                                     any(),
                                                                                     anyLong(),
                                                                                     anyLong());
  }

  private List<Identity> membersOf(CachedIdentityStorage identityStorage, ProfileFilter profileFilter) {
    return identityStorage.getSpaceMemberIdentitiesByProfileFilter(space,
                                                                    profileFilter,
                                                                    SpaceMemberFilterListAccess.Type.MEMBER,
                                                                    0,
                                                                    10);
  }

  private CachedSpaceStorage newCachedSpaceStorage(SpaceDAO dao) {
    return new CachedSpaceStorage(dao,
                                  getService(SpaceMemberDAO.class),
                                  getService(IdentityStorage.class),
                                  getService(IdentityDAO.class),
                                  getService(ActivityDAO.class),
                                  getService(SpaceExternalInvitationDAO.class),
                                  getService(SocialStorageCacheService.class),
                                  getService(FavoriteService.class),
                                  getService(RemindPasswordTokenService.class));
  }

  private static Space newSpace(String prettyName) {
    Space newSpace = new Space();
    newSpace.setDisplayName(prettyName);
    newSpace.setPrettyName(prettyName);
    newSpace.setRegistration(Space.OPEN);
    newSpace.setDescription(OLD_DESCRIPTION);
    newSpace.setVisibility(Space.PUBLIC);
    newSpace.setGroupId("/spaces/" + prettyName);
    newSpace.setUrl(prettyName);
    newSpace.setManagers(new String[] { "john" });
    newSpace.setMembers(new String[] { "john" });
    newSpace.setInvitedUsers(new String[] {});
    newSpace.setPendingUsers(new String[] {});
    return newSpace;
  }

  /**
   * Answers the real call, and holds the first caller after it read, until
   * released
   */
  private static Answer<Object> blockOnce(CountDownLatch loaded, CountDownLatch release) {
    AtomicBoolean first = new AtomicBoolean(true);
    return invocation -> {
      Object result = invocation.callRealMethod();
      if (first.getAndSet(false)) {
        loaded.countDown();
        release.await(30, TimeUnit.SECONDS);
      }
      return result;
    };
  }

  private <T> Future<T> submitInContainer(ExecutorService executor, Callable<T> task) {
    ExoContainer container = getContainer();
    return executor.submit(() -> {
      ExoContainerContext.setCurrentContainer(container);
      RequestLifeCycle.begin(container);
      try {
        return task.call();
      } finally {
        RequestLifeCycle.end();
      }
    });
  }

  private void commitNewPrettyName(String prettyName) {
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    boolean active = entityManager.getTransaction().isActive();
    if (!active) {
      entityManager.getTransaction().begin();
    }
    entityManager.createQuery("UPDATE SocSpaceEntity sp SET sp.prettyName = :prettyName WHERE sp.id = :id")
                 .setParameter("prettyName", prettyName)
                 .setParameter("id", space.getSpaceId())
                 .executeUpdate();
    entityManager.getTransaction().commit();
    if (active) {
      entityManager.getTransaction().begin();
    }
  }

  /**
   * The thread's persistence context holds the space with the old description,
   * then another transaction commits a new one.
   */
  private void holdTheSpaceThenCommitANewDescription() throws Exception {
    restartTransaction();
    assertEquals(OLD_DESCRIPTION, spaceDAO.find(space.getSpaceId()).getDescription());
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      submitInContainer(executor, () -> {
        commitNewDescription();
        return null;
      }).get(30, TimeUnit.SECONDS);
    } finally {
      executor.shutdownNow();
    }
    // the held entity is still the old one
    assertEquals(OLD_DESCRIPTION, spaceDAO.find(space.getSpaceId()).getDescription());
  }

  private void commitNewDescription() {
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    boolean active = entityManager.getTransaction().isActive();
    if (!active) {
      entityManager.getTransaction().begin();
    }
    entityManager.createQuery("UPDATE SocSpaceEntity sp SET sp.description = :description WHERE sp.id = :id")
                 .setParameter("description", NEW_DESCRIPTION)
                 .setParameter("id", space.getSpaceId())
                 .executeUpdate();
    entityManager.getTransaction().commit();
    if (active) {
      entityManager.getTransaction().begin();
    }
  }
}
