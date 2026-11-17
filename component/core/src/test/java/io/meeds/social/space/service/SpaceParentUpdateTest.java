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

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Pageable;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAO;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.SpaceDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceException.Code;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.SpaceStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.web.security.security.RemindPasswordTokenService;

import io.meeds.social.space.constant.SpaceMembershipStatus;
import io.meeds.social.space.model.MembershipChange;
import io.meeds.social.space.model.SpaceParentChange;
import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.space.security.model.SpaceSettingsChange;
import io.meeds.social.space.security.service.SpaceSecurityService;
import io.meeds.social.space.template.entity.SpaceTemplateEntity;
import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.storage.SpaceTemplateStorage;
import io.meeds.social.space.template.utils.EntityMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.SneakyThrows;

/**
 * Changing a space's parent through an update: decided on the stored parent,
 * authorized as a sub-space creation, refused for the space itself or one of
 * its descendants, and never written from the parent a cached space carries.
 */
public class SpaceParentUpdateTest extends AbstractCoreTest {

  private static final String  MANAGER          = "john";

  private static final String  OUTSIDER         = "demo";

  private static final String  MEMBER           = "mary";

  private static final String  ADMIN            = "root";

  private static final String  KEPT_DESCRIPTION = "kept";

  private SpaceStorage         spaceStorage;

  private SpaceDAO             spaceDAO;

  private SpaceTemplateStorage spaceTemplateStorage;

  private SpaceTemplate        templateX;

  private SpaceTemplate        templateY;

  private SpaceTemplate        templateZ;

  private SpaceTemplate        childTemplate;

  private List<String>         defaultAllowedSubspaceTemplates;

  private List<String>         defaultPermissions;

  private Integer              defaultSubspacesMaxLimit;

  private List<String>         defaultAdminPermissions;

  private Space                spaceA;

  private Space                spaceB;

  private Space                spaceC;

  private Space                spaceD;

  private Space                spaceE;

  private Space                subspaceK;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceStorage = getService(SpaceStorage.class);
    spaceDAO = getService(SpaceDAO.class);
    spaceTemplateStorage = getService(SpaceTemplateStorage.class);
    createIdentity(MANAGER);
    createIdentity(OUTSIDER);
    createIdentity(MEMBER);

    SpaceTemplate defaultTemplate = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged()).getFirst();
    defaultAllowedSubspaceTemplates = defaultTemplate.getAllowedSubspaceTemplates();
    defaultPermissions = defaultTemplate.getPermissions();
    defaultSubspacesMaxLimit = defaultTemplate.getSubspacesMaxLimit();
    defaultAdminPermissions = defaultTemplate.getAdminPermissions();
    // Templates that never allow themselves, which the service refuses: X, the
    // default one, allows Y and the child template; Y allows X and Z; Z allows
    // X. A space of each may hold A, so every move of A under one of its
    // descendants reaches the cycle check
    templateX = useDefaultTemplateAsTemplateX();
    templateY = createTemplate();
    templateZ = createTemplate();
    childTemplate = createTemplate();

    // A, D and E, created before their templates were allowed as sub-space
    // templates, have no parent; A > B > C; K, of the child template, is a
    // sub-space of A
    spaceA = saveSpace("Space A", templateX, null);
    spaceD = saveSpace("Space D", templateY, null);
    spaceE = saveSpace("Space E", templateX, null);
    allow(templateX, templateY, childTemplate);
    allow(templateY, templateX, templateZ);
    allow(templateZ, templateX);
    spaceB = saveSpace("Space B", templateY, spaceA.getSpaceId());
    spaceC = saveSpace("Space C", templateZ, spaceB.getSpaceId());
    subspaceK = saveSpace("Subspace K", childTemplate, spaceA.getSpaceId());
    restartTransaction();
  }

  @Override
  protected void tearDown() throws Exception {
    // the tests move parents around: the spaces are deleted in creation order
    // only once no row points at another
    restartTransaction();
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    entityManager.getTransaction().begin();
    entityManager.createQuery("UPDATE SocSpaceEntity sp SET sp.parentSpaceId = NULL").executeUpdate();
    entityManager.getTransaction().commit();
    restartTransaction();
    if (templateX != null) {
      templateX.setAllowedSubspaceTemplates(defaultAllowedSubspaceTemplates);
      templateX.setPermissions(defaultPermissions);
      templateX.setSubspacesMaxLimit(defaultSubspacesMaxLimit);
      templateX.setAdminPermissions(defaultAdminPermissions);
      spaceTemplateStorage.updateSpaceTemplate(templateX);
    }
    for (SpaceTemplate template : new SpaceTemplate[] { templateY, templateZ, childTemplate }) {
      if (template != null) {
        spaceTemplateStorage.deleteSpaceTemplate(template.getId());
      }
    }
    super.tearDown();
  }

  public void testAttachUnderAParent() throws Exception {
    spaceService.updateSpace(edited(spaceD), spaceA.getSpaceId(), null, MANAGER);

    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));
  }

  public void testDetach() throws Exception {
    // A's template no longer lists B's, which then needs no parent
    allow(templateX, childTemplate);

    spaceService.updateSpace(edited(spaceB), SpaceService.DETACH_PARENT_SPACE_ID, null, MANAGER);

    assertEquals(0L, storedParent(spaceB));
  }

  public void testNullAndZeroKeepTheParentAndAuthorizeNothing() throws Exception {
    spaceService.updateSpace(edited(spaceB), null, null, OUTSIDER);
    assertEquals(spaceA.getSpaceId(), storedParent(spaceB));

    spaceService.updateSpace(edited(spaceB), 0L, null, OUTSIDER);
    assertEquals(spaceA.getSpaceId(), storedParent(spaceB));
  }

  public void testAMemberOfTheParentOnItsCommittedRowsMayAttach() throws Exception {
    Space cachedSpaceA = spaceService.getSpaceById(spaceA.getId());
    assertFalse(spaceService.isMember(cachedSpaceA, MEMBER));
    // the member row, written with no eviction of the space cache
    getService(EntityManagerService.class).getEntityManager().clear();
    getService(SpaceMemberDAO.class).create(new SpaceMemberEntity(spaceDAO.find(spaceA.getSpaceId()),
                                                                  MEMBER,
                                                                  SpaceMembershipStatus.MEMBER,
                                                                  Instant.now()));
    restartTransaction();
    assertFalse(spaceService.isMember(spaceService.getSpaceById(spaceA.getId()), MEMBER));

    spaceService.updateSpace(edited(spaceD), spaceA.getSpaceId(), null, MEMBER);

    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));
  }

  public void testAManagerOfTheParentOnItsCommittedRowsMayAttach() throws Exception {
    // a manager row alone, written with no eviction of the space cache
    getService(EntityManagerService.class).getEntityManager().clear();
    getService(SpaceMemberDAO.class).create(new SpaceMemberEntity(spaceDAO.find(spaceA.getSpaceId()),
                                                                  MEMBER,
                                                                  SpaceMembershipStatus.MANAGER,
                                                                  Instant.now()));
    restartTransaction();

    spaceService.updateSpace(edited(spaceD), spaceA.getSpaceId(), null, MEMBER);

    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));
  }

  public void testAPlatformAdministratorMayAttach() throws Exception {
    createIdentity(ADMIN);

    spaceService.updateSpace(edited(spaceD), spaceA.getSpaceId(), null, ADMIN);

    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));
  }

  public void testAnAdministratorOfTheParentsTemplateMayAttach() throws Exception {
    org.exoplatform.services.security.Identity outsider = getService(UserACL.class).getUserIdentity(OUTSIDER);
    String group = outsider.getMemberships().iterator().next().getGroup();
    templateX.setAdminPermissions(List.of(group));
    spaceTemplateStorage.updateSpaceTemplate(templateX);

    spaceService.updateSpace(edited(spaceD), spaceA.getSpaceId(), null, OUTSIDER);

    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));
  }

  public void testParentTheUserMayNotUseIsRefusedAndNothingSaved() throws Exception {
    assertRefused(Code.SUBSPACES_PERMISSIONS, spaceD, spaceA.getSpaceId(), OUTSIDER);

    assertEquals(0L, storedParent(spaceD));
  }

  public void testTheSpaceAndItsDescendantsAreRefusedAsItsParent() throws Exception {
    assertRefused(Code.SUBSPACES_INVALID_PARENT, spaceA, spaceA.getSpaceId(), MANAGER);
    assertRefused(Code.SUBSPACES_INVALID_PARENT, spaceA, spaceB.getSpaceId(), MANAGER);
    assertRefused(Code.SUBSPACES_INVALID_PARENT, spaceA, spaceC.getSpaceId(), MANAGER);

    assertEquals(0L, storedParent(spaceA));
  }

  public void testAStoredCycleEndsTheWalk() throws Exception {
    setStoredParent(spaceD, spaceE.getSpaceId());
    setStoredParent(spaceE, spaceD.getSpaceId());

    spaceService.updateSpace(edited(spaceA), spaceD.getSpaceId(), null, MANAGER);

    assertEquals(spaceD.getSpaceId(), storedParent(spaceA));
  }

  public void testDetachingASpaceOfASubspaceTemplateIsRefused() throws Exception {
    assertRefused(Code.SUBSPACES_PARENT_REQUIRED, subspaceK, SpaceService.DETACH_PARENT_SPACE_ID, MANAGER);

    assertEquals(spaceA.getSpaceId(), storedParent(subspaceK));
  }

  public void testAParentChangedSinceTheDecisionIsAConcurrentUpdate() throws Exception {
    SpaceStorageException refusal = assertThrows(SpaceStorageException.class,
                                                 () -> spaceStorage.saveSpace(edited(spaceD),
                                                                              new SpaceParentChange(spaceA.getSpaceId(),
                                                                                                    spaceE.getSpaceId())));

    assertEquals(SpaceStorageException.Type.CONCURRENT_UPDATE, refusal.getType());
    assertEquals(0L, storedParent(spaceD));
    assertEquals(KEPT_DESCRIPTION, storedDescription(spaceD));
  }

  public void testAParentGoneSinceTheDecisionIsRefusedAndNothingSaved() throws Exception {
    SpaceStorageException refusal = assertThrows(SpaceStorageException.class,
                                                 () -> spaceStorage.saveSpace(edited(spaceD),
                                                                              new SpaceParentChange(0, Long.MAX_VALUE)));

    assertEquals(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE, refusal.getType());
    assertEquals(0L, storedParent(spaceD));
    assertEquals(KEPT_DESCRIPTION, storedDescription(spaceD));
  }

  public void testTheParentACachedSpaceCarriesIsNeverWritten() throws Exception {
    Space cachedSpaceD = spaceService.getSpaceById(spaceD.getId());
    assertEquals(0L, cachedSpaceD.getParentSpaceId() == null ? 0L : cachedSpaceD.getParentSpaceId().longValue());
    setStoredParent(spaceD, spaceA.getSpaceId());

    spaceService.updateSpace(spaceService.getSpaceById(spaceD.getId()), null, null, MANAGER);
    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));

    // an avatar or banner update saves the space it holds
    spaceStorage.saveSpace(spaceService.getSpaceById(spaceD.getId()), false);
    assertEquals(spaceA.getSpaceId(), storedParent(spaceD));
  }

  public void testTheWalkReadsTheStoredParentsNeverTheCachedOnes() throws Exception {
    Space cachedSpaceE = spaceService.getSpaceById(spaceE.getId());
    assertNull(cachedSpaceE.getParentSpaceId());
    setStoredParent(spaceE, spaceD.getSpaceId());

    assertRefused(Code.SUBSPACES_INVALID_PARENT, spaceD, spaceE.getSpaceId(), MANAGER);

    assertEquals(0L, storedParent(spaceD));
  }

  public void testTheCheckRefusesAsTheUpdateAndWritesNothing() throws Exception {
    assertCheckRefused(Code.SUBSPACES_INVALID_PARENT, spaceA, spaceA.getSpaceId(), MANAGER);
    assertCheckRefused(Code.SUBSPACES_INVALID_PARENT, spaceA, spaceC.getSpaceId(), MANAGER);
    assertCheckRefused(Code.SUBSPACES_PERMISSIONS, spaceD, spaceA.getSpaceId(), OUTSIDER);
    assertCheckRefused(Code.SUBSPACES_PARENT_REQUIRED, subspaceK, SpaceService.DETACH_PARENT_SPACE_ID, MANAGER);

    // on the stored parents, never the cached ones
    assertNull(spaceService.getSpaceById(spaceE.getId()).getParentSpaceId());
    setStoredParent(spaceE, spaceD.getSpaceId());
    assertCheckRefused(Code.SUBSPACES_INVALID_PARENT, spaceD, spaceE.getSpaceId(), MANAGER);

    // an allowed change is decided, and left to the update
    spaceService.checkSpaceParentChange(spaceService.getSpaceById(spaceB.getId()), spaceE.getSpaceId(), MANAGER);
    spaceService.checkSpaceParentChange(spaceService.getSpaceById(spaceB.getId()), null, OUTSIDER);
    assertEquals(spaceA.getSpaceId(), storedParent(spaceB));
  }

  /**
   * A lock the database does not grant in time reaches the callers as a
   * concurrent update, which they answer with a retry, whichever writer or
   * ancestor it was taken for, and nothing is stored. Mutant: letting the
   * persistence exception through answers a server error.
   */
  public void testALockNotGrantedIsAConcurrentUpdateAndStoresNothing() throws Exception {
    SpaceDAO lockingDAO = spy(spaceDAO);
    doThrow(new LockTimeoutException("lock wait")).when(lockingDAO).findForUpdate(spaceD.getSpaceId());
    doThrow(new PessimisticLockException("deadlock")).when(lockingDAO).findParentSpaceIdForUpdate(spaceA.getSpaceId());
    SpaceStorage storage = new SpaceStorage(lockingDAO,
                                            getService(SpaceMemberDAO.class),
                                            getService(IdentityStorage.class),
                                            getService(IdentityDAO.class),
                                            getService(ActivityDAO.class),
                                            getService(SpaceExternalInvitationDAO.class),
                                            getService(FavoriteService.class),
                                            getService(RemindPasswordTokenService.class));

    assertConcurrentUpdate(() -> storage.saveSpace(edited(spaceD), new SpaceParentChange(0, spaceE.getSpaceId())));
    assertConcurrentUpdate(() -> storage.renameSpace(edited(spaceD)));
    assertConcurrentUpdate(() -> storage.applySettingsChange(spaceD.getSpaceId(),
                                                             new SpaceSettingsChange(Map.of(SpaceSecuritySettings.LIMIT_ACCESS_MANAGEMENT_KEY,
                                                                                            "true"))));
    assertConcurrentUpdate(() -> storage.changeMembership(spaceD.getSpaceId(),
                                                          MEMBER,
                                                          MembershipChange.of(Set.of(SpaceMembershipStatus.MEMBER), Set.of())));
    assertConcurrentUpdate(() -> storage.ignoreSpace(spaceD.getId(), MEMBER));
    // E's own row is locked; the walk from B up to A is refused at A
    assertConcurrentUpdate(() -> storage.saveSpace(edited(spaceE), new SpaceParentChange(0, spaceB.getSpaceId())));

    assertEquals(KEPT_DESCRIPTION, storedDescription(spaceD));
    assertEquals(KEPT_DESCRIPTION, storedDescription(spaceE));
    assertEquals(0L, storedParent(spaceE));
    assertFalse(spaceSecurityService().getSettings(spaceD.getSpaceId()).isLimitAccessManagementOn());
    assertTrue(spaceStorage.getMemberStatuses(spaceD.getSpaceId(), MEMBER).isEmpty());
  }

  /**
   * Two updates naming each other as parent store no cycle. HSQLDB in LOCKS
   * mode locks whole tables, so the space row lock alone serialises the two
   * updates here: the walk's row locks on the ancestors, which keep the cycle
   * out where locks are taken per row, are not exercised by this test (a walk
   * by plain reads passes it too) and are verified on PostgreSQL on the
   * acceptance server.
   */
  public void testTwoUpdatesNamingEachOtherStoreNoCycle() throws Exception {
    List<Throwable> failures = runConcurrently(() -> spaceService.updateSpace(edited(spaceD),
                                                                              spaceE.getSpaceId(),
                                                                              null,
                                                                              MANAGER),
                                               () -> spaceService.updateSpace(edited(spaceE),
                                                                              spaceD.getSpaceId(),
                                                                              null,
                                                                              MANAGER));

    assertEquals(1, failures.size());
    Throwable failure = failures.get(0);
    assertTrue(String.valueOf(failure),
               (failure instanceof SpaceException spaceException && spaceException.getCode() == Code.SUBSPACES_INVALID_PARENT)
                   || isLockFailure(failure));
    assertFalse(storedParent(spaceD) == spaceE.getSpaceId() && storedParent(spaceE) == spaceD.getSpaceId());
  }

  private void assertConcurrentUpdate(org.junit.function.ThrowingRunnable write) {
    SpaceStorageException refusal = assertThrows(SpaceStorageException.class, write);
    assertEquals(SpaceStorageException.Type.CONCURRENT_UPDATE, refusal.getType());
  }

  private SpaceSecurityService spaceSecurityService() {
    return getService(SpaceSecurityService.class);
  }

  private void assertCheckRefused(Code code, Space space, long requestedParentSpaceId, String username) throws Exception {
    long storedParent = storedParent(space);
    SpaceException refusal = assertThrows(SpaceException.class,
                                          () -> spaceService.checkSpaceParentChange(spaceService.getSpaceById(space.getId()),
                                                                                    requestedParentSpaceId,
                                                                                    username));
    assertEquals(code, refusal.getCode());
    assertEquals(storedParent, storedParent(space));
  }

  private void assertRefused(Code code, Space space, long requestedParentSpaceId, String username) {
    SpaceException refusal = assertThrows(SpaceException.class,
                                          () -> spaceService.updateSpace(edited(space), requestedParentSpaceId, null, username));
    assertEquals(code, refusal.getCode());
    assertEquals(KEPT_DESCRIPTION, storedDescription(space));
  }

  /**
   * The space as a client sends it, its description changed: a refusal must
   * store none of it.
   */
  private Space edited(Space space) {
    Space edited = spaceService.getSpaceById(space.getId());
    edited.setDescription("changed by the request");
    return edited;
  }

  private long storedParent(Space space) throws Exception {
    restartTransaction();
    return spaceStorage.getCommittedParentSpaceId(space.getSpaceId());
  }

  private String storedDescription(Space space) {
    restartTransaction();
    EntityManager entityManager = getService(EntityManagerService.class).getEntityManager();
    return (String) entityManager.createQuery("SELECT sp.description FROM SocSpaceEntity sp WHERE sp.id = :id")
                                 .setParameter("id", space.getSpaceId())
                                 .getSingleResult();
  }

  /**
   * Writes a parent into the space's row with no eviction of the space cache.
   */
  private void setStoredParent(Space space, Long parentSpaceId) {
    restartTransaction();
    SpaceEntity entity = spaceDAO.find(space.getSpaceId());
    entity.setParentSpaceId(parentSpaceId);
    spaceDAO.update(entity);
    restartTransaction();
  }

  private Space saveSpace(String displayName, SpaceTemplate template, Long parentSpaceId) {
    Space space = new Space();
    space.setDisplayName(displayName);
    space.setPrettyName(displayName.replace(' ', '_').toLowerCase());
    space.setRegistration(Space.OPEN);
    space.setDescription(KEPT_DESCRIPTION);
    space.setVisibility(Space.PUBLIC);
    space.setTemplateId(template.getId());
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setUrl(space.getPrettyName());
    space.setManagers(new String[] { MANAGER });
    space.setMembers(new String[] { MANAGER });
    space.setInvitedUsers(new String[] {});
    space.setPendingUsers(new String[] {});
    return spaceStorage.saveSpace(space, parentSpaceId, true);
  }

  /**
   * The default template, under a row of its own, which the spaces' foreign
   * key needs, open to everyone and with no sub-space limit
   */
  @SneakyThrows
  private SpaceTemplate useDefaultTemplateAsTemplateX() {
    SpaceTemplate template = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged()).getFirst();
    SpaceTemplateDAO dao = new SpaceTemplateDAO();
    if (dao.find(template.getId()) == null) {
      SpaceTemplateEntity entity = EntityMapper.toEntity(template);
      entity.setId(null);
      while (dao.create(entity).getId() < template.getId()) {
        entity = EntityMapper.toEntity(template);
        entity.setId(null);
      }
      restartTransaction();
    }
    template.setAllowedSubspaceTemplates(null);
    template.setPermissions(List.of("Everyone"));
    template.setSubspacesMaxLimit(0);
    return spaceTemplateStorage.updateSpaceTemplate(template);
  }

  /**
   * Lets a template's spaces hold the spaces of other templates, with no
   * limit
   */
  @SneakyThrows
  private void allow(SpaceTemplate template, SpaceTemplate... subspaceTemplates) {
    template.setAllowedSubspaceTemplates(Arrays.stream(subspaceTemplates)
                                               .map(subspaceTemplate -> subspaceTemplate.getId() + ":0")
                                               .toList());
    spaceTemplateStorage.updateSpaceTemplate(template);
  }

  /**
   * A template under a row of its own, allowing no sub-space template
   */
  @SneakyThrows
  private SpaceTemplate createTemplate() {
    SpaceTemplate template = spaceTemplateStorage.getSpaceTemplates(Pageable.unpaged()).getFirst();
    SpaceTemplateEntity entity = EntityMapper.toEntity(template);
    entity.setId(null);
    entity.setAllowedSubspaceTemplates(null);
    entity.setSubspacesMaxLimit(0);
    SpaceTemplate child = EntityMapper.fromEntity(new SpaceTemplateDAO().create(entity));
    restartTransaction();
    child.setPermissions(List.of("Everyone"));
    return spaceTemplateStorage.createSpaceTemplate(child);
  }

  /**
   * Runs both tasks at once, each in its own request and transaction, with
   * HSQLDB in LOCKS mode, where a SELECT ... FOR UPDATE waits for the holder's
   * commit as on PostgreSQL and MySQL; HSQLDB locks the whole table where they
   * lock the row.
   *
   * @return the failures of the tasks
   */
  private List<Throwable> runConcurrently(ThrowingRunnable first, ThrowingRunnable second) throws Exception {
    setTransactionControl("LOCKS");
    try {
      ExoContainer container = getContainer();
      CountDownLatch start = new CountDownLatch(1);
      ExecutorService executor = Executors.newFixedThreadPool(2);
      List<Throwable> failures = new ArrayList<>();
      try {
        List<Future<Throwable>> futures = new ArrayList<>();
        for (ThrowingRunnable task : List.of(first, second)) {
          futures.add(executor.submit(() -> {
            ExoContainerContext.setCurrentContainer(container);
            start.await();
            RequestLifeCycle.begin(container);
            try {
              task.run();
              return null;
            } catch (Throwable e) { // NOSONAR the failure is the outcome under test
              return e;
            } finally {
              RequestLifeCycle.end();
            }
          }));
        }
        start.countDown();
        for (Future<Throwable> future : futures) {
          Throwable failure = future.get(60, TimeUnit.SECONDS);
          if (failure != null) {
            failures.add(failure);
          }
        }
      } finally {
        executor.shutdownNow();
      }
      return failures;
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

  /**
   * A write that lost the race on a row lock: the deadlock or the lock wait the
   * database reports, or a parent changed since the decision.
   */
  private static boolean isLockFailure(Throwable failure) {
    Throwable cause = failure;
    while (cause != null) {
      if (cause instanceof jakarta.persistence.LockTimeoutException
          || cause instanceof jakarta.persistence.PessimisticLockException
          || (cause instanceof SpaceStorageException storageException
              && storageException.getType() == SpaceStorageException.Type.CONCURRENT_UPDATE)) {
        return true;
      }
      cause = cause.getCause() == cause ? null : cause.getCause();
    }
    return false;
  }

  public static class SpaceTemplateDAO extends GenericDAOJPAImpl<SpaceTemplateEntity, Long> {
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws Exception;
  }
}
