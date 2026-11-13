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
package io.meeds.social.core.profileproperty.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.cache.invalidation.AsyncInvalidationExoCache;
import org.exoplatform.services.cache.invalidation.HashCode;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertyOptionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertyOptionEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

import io.meeds.common.TransactionCallbacks;

/**
 * Two nodes of a cluster, each with its own {@link CachedProfileSettingStorage}
 * over its own {@link AsyncInvalidationExoCache} per cache name, as a cluster
 * profile builds them (kernel CacheServiceImpl), wrapping one replicated cache
 * per name shared by both nodes. The shared cache passes every value through
 * {@link HashCode}'s externalization, so that a node receives a hash and never
 * the other node's value, as across a real cluster. The store is an in-memory
 * stand-in for the settings DAO.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CachedProfileSettingStorageClusterTest {

  private final Map<Long, ProfilePropertySettingEntity>   store              = new ConcurrentHashMap<>();

  private final AtomicLong                                sequence           = new AtomicLong();

  private final Map<String, ExoCache>                     replicatedCaches   = new HashMap<>();

  private final Map<String, Map<String, ExoCache>>        nodeCaches         = new HashMap<>();

  private final AtomicReference<Thread>                   blockedLoader      = new AtomicReference<>();

  private final CountDownLatch                            loaderBlocked      = new CountDownLatch(1);

  private final CountDownLatch                            releaseLoader      = new CountDownLatch(1);

  private final AtomicReference<Runnable>                 duringUpdate       = new AtomicReference<>();

  private final Set<Long>                                 failingLoads       = ConcurrentHashMap.newKeySet();

  private final List<Runnable>                            afterCompletionCallbacks = new ArrayList<>();

  private volatile boolean                                queriesFail;

  private ProfilePropertySettingDAO                       settingDAO;

  private MockedStatic<TransactionCallbacks>              transactionCallbacks;

  private CachedProfileSettingStorage                     node1;

  private CachedProfileSettingStorage                     node2;

  @Before
  public void setUp() {
    settingDAO = mock(ProfilePropertySettingDAO.class);
    when(settingDAO.find(any())).thenAnswer(invocation -> load(invocation.getArgument(0)));
    when(settingDAO.findOrderedSettings()).thenAnswer(invocation -> failIfQueriesFail() ? null : store.values()
                                                                         .stream()
                                                                         .sorted(Comparator.comparing(ProfilePropertySettingEntity::getOrder,
                                                                                                      Comparator.nullsLast(Comparator.naturalOrder())))
                                                                         .map(this::copy)
                                                                         .toList());
    when(settingDAO.findProfileSettingByName(anyString())).thenAnswer(invocation -> failIfQueriesFail() ? null : store.values()
                                                                                         .stream()
                                                                                         .filter(e -> e.getPropertyName()
                                                                                                       .equals(invocation.getArgument(0)))
                                                                                         .findFirst()
                                                                                         .map(this::copy)
                                                                                         .orElse(null));
    when(settingDAO.hasChildProperties(any())).thenAnswer(invocation -> store.values()
                                                                             .stream()
                                                                             .anyMatch(e -> Objects.equals(e.getParentId(),
                                                                                                           invocation.getArgument(0))));
    when(settingDAO.create(any())).thenAnswer(invocation -> {
      ProfilePropertySettingEntity entity = invocation.getArgument(0);
      entity.setId(sequence.incrementAndGet());
      store.put(entity.getId(), copy(entity));
      return entity;
    });
    when(settingDAO.update(any())).thenAnswer(invocation -> {
      Runnable runnable = duringUpdate.getAndSet(null);
      if (runnable != null) {
        runnable.run();
      }
      ProfilePropertySettingEntity entity = invocation.getArgument(0);
      store.put(entity.getId(), copy(entity));
      return entity;
    });
    when(settingDAO.clearListingFlags(any())).thenAnswer(invocation -> {
      ProfilePropertySettingEntity entity = store.get(invocation.getArgument(0));
      if (entity == null || (!entity.isListedOnAccountForm() && !entity.isListedOnRegistration())) {
        return false;
      }
      entity.setListedOnAccountForm(false);
      entity.setListedOnRegistration(false);
      entity.setUpdatedDate(new Date());
      return true;
    });
    doAnswer(invocation -> {
      ProfilePropertySettingEntity entity = invocation.getArgument(0);
      store.remove(entity.getId());
      return null;
    }).when(settingDAO).delete(any());

    transactionCallbacks = mockStatic(TransactionCallbacks.class);
    transactionCallbacks.when(() -> TransactionCallbacks.afterCompletion(any()))
                        .thenAnswer(invocation -> afterCompletionCallbacks.add(invocation.getArgument(0)));

    node1 = newNode("node1");
    node2 = newNode("node2");
  }

  @After
  public void tearDown() {
    transactionCallbacks.close();
  }

  @Test
  public void testACreatedSettingIsSeenAtOnceByTheOtherNode() {
    ProfilePropertySetting existing = node1.saveProfilePropertySetting(newSetting("existing"), true);
    assertEquals(1, node2.getPropertySettings().size());
    assertNull(node2.findProfileSettingByName("created"));

    ProfilePropertySetting created = node1.saveProfilePropertySetting(newSetting("created"), true);

    assertEquals(List.of(existing.getId(), created.getId()),
                 node2.getPropertySettings().stream().map(ProfilePropertySetting::getId).toList());
    assertNotNull("A miss cached by the other node must be evicted", node2.findProfileSettingByName("created"));
    assertNotNull(node2.getProfileSettingById(created.getId()));
  }

  @Test
  public void testAnUnlistedOrNonEditableSettingIsSeenAtOnceByTheOtherNode() {
    ProfilePropertySetting setting = newSetting("department");
    setting.setListedOnAccountForm(true);
    setting.setListedOnRegistration(true);
    setting = node1.saveProfilePropertySetting(setting, true);
    loadEveryKey(node2, setting);
    assertTrue(node2.getProfileSettingById(setting.getId()).isListedOnAccountForm());

    setting.setListedOnAccountForm(false);
    node1.saveProfilePropertySetting(setting, false);
    assertFalse(node2.getProfileSettingById(setting.getId()).isListedOnAccountForm());
    assertFalse(node2.findProfileSettingByName("department").isListedOnAccountForm());
    assertFalse(node2.getPropertySettings().get(0).isListedOnAccountForm());

    loadEveryKey(node2, setting);
    setting.setEditable(false);
    node1.saveProfilePropertySetting(setting, false);
    assertFalse(node2.getProfileSettingById(setting.getId()).isEditable());
    assertFalse(node2.findProfileSettingByName("department").isEditable());
    assertFalse(node2.getPropertySettings().get(0).isEditable());
  }

  @Test
  public void testAReParentedSettingIsSeenAtOnceByTheOtherNode() {
    ProfilePropertySetting oldParent = node1.saveProfilePropertySetting(newSetting("oldParent"), true);
    ProfilePropertySetting newParent = node1.saveProfilePropertySetting(newSetting("newParent"), true);
    ProfilePropertySetting child = newSetting("child");
    child.setParentId(oldParent.getId());
    child = node1.saveProfilePropertySetting(child, true);
    loadEveryKey(node2, oldParent);
    loadEveryKey(node2, newParent);
    loadEveryKey(node2, child);
    assertTrue(node2.getProfileSettingById(oldParent.getId()).isHasChildProperties());
    assertFalse(node2.getProfileSettingById(newParent.getId()).isHasChildProperties());

    child.setParentId(newParent.getId());
    node1.saveProfilePropertySetting(child, false);

    assertFalse("The old parent's id must be evicted", node2.getProfileSettingById(oldParent.getId()).isHasChildProperties());
    assertTrue("The new parent's id must be evicted", node2.getProfileSettingById(newParent.getId()).isHasChildProperties());
    assertEquals(newParent.getId(), node2.findProfileSettingByName("child").getParentId());
  }

  @Test
  public void testARenamedSettingIsSeenAtOnceByTheOtherNodeUnderBothNames() {
    ProfilePropertySetting setting = node1.saveProfilePropertySetting(newSetting("oldName"), true);
    loadEveryKey(node2, setting);
    assertNull(node2.findProfileSettingByName("newName"));

    setting.setPropertyName("newName");
    node1.saveProfilePropertySetting(setting, false);

    assertNull("The old name must be evicted", node2.findProfileSettingByName("oldName"));
    assertNotNull("A miss cached for the new name must be evicted", node2.findProfileSettingByName("newName"));
  }

  @Test
  public void testAClearedParentIsSeenAtOnceByTheOtherNode() {
    ProfilePropertySetting parent = newSetting("parent");
    parent.setListedOnAccountForm(true);
    parent = node1.saveProfilePropertySetting(parent, true);
    loadEveryKey(node2, parent);

    assertTrue(node1.clearListingFlags(parent.getId()));

    assertFalse(node2.getProfileSettingById(parent.getId()).isListedOnAccountForm());
    assertFalse(node2.findProfileSettingByName("parent").isListedOnAccountForm());
  }

  @Test
  public void testADeletedSettingIsSeenAtOnceByTheOtherNode() {
    ProfilePropertySetting kept = node1.saveProfilePropertySetting(newSetting("kept"), true);
    ProfilePropertySetting deleted = node1.saveProfilePropertySetting(newSetting("deleted"), true);
    loadEveryKey(node2, kept);
    loadEveryKey(node2, deleted);

    node1.deleteProfilePropertySetting(deleted.getId());

    assertEquals(List.of(kept.getId()), node2.getPropertySettings().stream().map(ProfilePropertySetting::getId).toList());
    assertNull(node2.getProfileSettingById(deleted.getId()));
    assertNull(node2.findProfileSettingByName("deleted"));
  }

  @Test
  public void testADeletedIdStillInTheCachedListYieldsNoNullEntry() {
    ProfilePropertySetting kept = node1.saveProfilePropertySetting(newSetting("kept"), true);
    ProfilePropertySetting deleted = node1.saveProfilePropertySetting(newSetting("deleted"), true);
    assertEquals(2, node2.getPropertySettings().size());

    // The setting is gone from the store and from this node's settings cache,
    // while the node still holds the id list it loaded before
    store.remove(deleted.getId());
    nodeCaches.get("node2").get(CachedProfileSettingStorage.PROFILE_SETTINGS_CACHE_NAME).removeLocal(deleted.getId());

    List<ProfilePropertySetting> settings = node2.getPropertySettings();
    assertEquals(List.of(kept.getId()), settings.stream().map(ProfilePropertySetting::getId).toList());
  }

  /**
   * A node that reloads a key from the store after the saving node's eviction
   * reached it, and before that node's commit, caches the setting as it was
   * before the save: the eviction run again once the transaction completes
   * removes it. Another node's write of the same key consumed the replicated
   * entry in between, so the saving node reads the key again for its
   * eviction to be published.
   */
  @Test
  public void testAReadBeforeTheCommitIsEvictedOnceTheTransactionCompletes() {
    ProfilePropertySetting setting = node1.saveProfilePropertySetting(newSetting("department"), true);
    runAfterCompletionCallbacks();
    ProfilePropertySettingEntity beforeSave = copy(store.get(setting.getId()));
    loadEveryKey(node1, setting);
    loadEveryKey(node2, setting);

    setting.setListedOnAccountForm(true);
    node1.saveProfilePropertySetting(setting, false);
    replicatedCaches.get(CachedProfileSettingStorage.PROFILE_SETTINGS_CACHE_NAME).remove(setting.getId());
    ProfilePropertySettingEntity saved = store.put(setting.getId(), beforeSave);
    assertFalse("Read before the commit", node2.getProfileSettingById(setting.getId()).isListedOnAccountForm());
    store.put(setting.getId(), saved);

    runAfterCompletionCallbacks();

    assertTrue(node2.getProfileSettingById(setting.getId()).isListedOnAccountForm());
  }

  /**
   * The reads made before a write only collect and load the keys to evict:
   * a read that fails, as each read through the caches does outside a request
   * lifecycle (a conversion of a detached entity, a query on a closed entity
   * manager), never fails the write.
   */
  @Test
  public void testAFailedReadBeforeAWriteDoesNotFailTheWrite() {
    ProfilePropertySetting deleted = node1.saveProfilePropertySetting(newSetting("deleted"), true);
    ProfilePropertySetting updated = node1.saveProfilePropertySetting(newSetting("updated"), true);
    failingLoads.add(deleted.getId());
    failingLoads.add(updated.getId());
    queriesFail = true;

    node1.deleteProfilePropertySetting(deleted.getId());
    updated.setListedOnAccountForm(true);
    node1.saveProfilePropertySetting(updated, false);
    assertTrue(node1.clearListingFlags(updated.getId()));

    assertFalse(store.containsKey(deleted.getId()));
    assertFalse(store.get(updated.getId()).isListedOnAccountForm());
  }

  @Test
  public void testALoadBlockedWhileASaveInvalidatesIsNotWrittenBack() throws Exception {
    ProfilePropertySetting setting = node1.saveProfilePropertySetting(newSetting("department"), true);
    loadEveryKey(node1, setting);
    Long id = setting.getId();
    AtomicReference<ProfilePropertySetting> staleLoad = new AtomicReference<>();
    Thread loader = new Thread(() -> staleLoad.set(node1.getProfileSettingById(id)), "blocked-loader");
    duringUpdate.set(() -> {
      // Another node's eviction arrives after this node read the key, and a
      // reader starts a load that reads the store before the write
      nodeCaches.get("node1").get(CachedProfileSettingStorage.PROFILE_SETTINGS_CACHE_NAME).removeLocal(id);
      blockedLoader.set(loader);
      loader.start();
      awaitLatch(loaderBlocked);
    });

    setting.setListedOnAccountForm(true);
    node1.saveProfilePropertySetting(setting, false);
    releaseLoader.countDown();
    loader.join(TimeUnit.SECONDS.toMillis(10));

    assertNotNull(staleLoad.get());
    assertFalse("The blocked load read the store before the write", staleLoad.get().isListedOnAccountForm());
    assertTrue("The blocked load's value must not be cached once the save invalidated it",
               node1.getProfileSettingById(id).isListedOnAccountForm());
  }

  private boolean failIfQueriesFail() {
    if (queriesFail) {
      throw new IllegalStateException("Session/EntityManager is closed");
    }
    return false;
  }

  private void runAfterCompletionCallbacks() {
    List<Runnable> callbacks = new ArrayList<>(afterCompletionCallbacks);
    afterCompletionCallbacks.clear();
    callbacks.forEach(Runnable::run);
  }

  private CachedProfileSettingStorage newNode(String nodeName) {
    CacheService cacheService = mock(CacheService.class);
    when(cacheService.getCacheInstance(anyString())).thenAnswer(invocation -> {
      String cacheName = invocation.getArgument(0);
      ExoCache cache = new AsyncInvalidationExoCache(replicatedCaches.computeIfAbsent(cacheName,
                                                                                      SerializingReplicatedCache::new));
      nodeCaches.computeIfAbsent(nodeName, key -> new HashMap<>()).put(cacheName, cache);
      return cache;
    });
    return new CachedProfileSettingStorage(cacheService, settingDAO, mock(ProfilePropertyOptionDAO.class));
  }

  private ProfilePropertySettingEntity load(Long id) {
    ProfilePropertySettingEntity entity = copy(store.get(id));
    if (entity != null && failingLoads.contains(id)) {
      entity = new DetachedEntity(entity);
    }
    if (Thread.currentThread() == blockedLoader.get()) {
      loaderBlocked.countDown();
      awaitLatch(releaseLoader);
    }
    return entity;
  }

  private void loadEveryKey(CachedProfileSettingStorage node, ProfilePropertySetting setting) {
    node.getPropertySettings();
    node.getProfileSettingById(setting.getId());
    node.findProfileSettingByName(setting.getPropertyName());
  }

  private ProfilePropertySetting newSetting(String name) {
    ProfilePropertySetting setting = new ProfilePropertySetting();
    setting.setPropertyName(name);
    setting.setPropertyType("text");
    setting.setActive(true);
    setting.setEditable(true);
    setting.setVisible(true);
    setting.setOrder(sequence.get() + 1);
    return setting;
  }

  private ProfilePropertySettingEntity copy(ProfilePropertySettingEntity entity) {
    if (entity == null) {
      return null;
    }
    ProfilePropertySettingEntity copy = new ProfilePropertySettingEntity();
    copy.setId(entity.getId());
    copy.setPropertyName(entity.getPropertyName());
    copy.setPropertyType(entity.getPropertyType());
    copy.setActive(entity.isActive());
    copy.setEditable(entity.isEditable());
    copy.setVisible(entity.isVisible());
    copy.setParentId(entity.getParentId());
    copy.setOrder(entity.getOrder());
    copy.setRequired(entity.isRequired());
    copy.setMultiValued(entity.isMultiValued());
    copy.setGroupSynchronized(entity.isGroupSynchronized());
    copy.setDropdownList(entity.isDropdownList());
    copy.setUpdatedDate(entity.getUpdatedDate());
    copy.setListedOnAccountForm(entity.isListedOnAccountForm());
    copy.setListedOnRegistration(entity.isListedOnRegistration());
    copy.setPropertyOptions(new ArrayList<>());
    return copy;
  }

  private static void awaitLatch(CountDownLatch latch) {
    try {
      if (!latch.await(10, TimeUnit.SECONDS)) {
        throw new IllegalStateException("Timed out");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }

  /**
   * An entity read outside a request lifecycle: its lazy options can no longer
   * load, while its own columns can be read and it can be deleted.
   */
  private static class DetachedEntity extends ProfilePropertySettingEntity {

    private static final long serialVersionUID = 1L;

    DetachedEntity(ProfilePropertySettingEntity entity) {
      setId(entity.getId());
      setPropertyName(entity.getPropertyName());
      setParentId(entity.getParentId());
      setOrder(entity.getOrder());
      setUpdatedDate(entity.getUpdatedDate());
    }

    @Override
    public List<ProfilePropertyOptionEntity> getPropertyOptions() {
      throw new IllegalStateException("Cannot lazily initialize the options: no session");
    }
  }

  /**
   * The cache a cluster profile replicates: a node puts a hash of its value,
   * the other nodes receive the hash after its externalization, without the
   * value.
   */
  private static class SerializingReplicatedCache extends ConcurrentFIFOExoCache<Serializable, HashCode<Object>> {

    SerializingReplicatedCache(String name) {
      super(name, 1000);
    }

    @Override
    public void put(Serializable key, HashCode<Object> value) {
      super.put(key, externalize(value));
    }

    private static HashCode<Object> externalize(HashCode<Object> value) {
      try {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
          output.writeObject(value);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
          return (HashCode<Object>) input.readObject();
        }
      } catch (IOException | ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }
    }
  }

}
