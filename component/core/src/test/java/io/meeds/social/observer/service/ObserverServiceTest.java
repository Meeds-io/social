/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

package io.meeds.social.observer.service;

import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

import io.meeds.social.observe.model.ObserverObject;
import io.meeds.social.observe.plugin.ObserverPlugin;
import io.meeds.social.observe.service.ObserverService;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/exo.social.component.core-local-root-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.core-local-configuration.xml"), })
public class ObserverServiceTest extends AbstractKernelTest { // NOSONAR

  private static final String NOT_FOUND_OBJECT_ID = "notFound";

  private static final String OBJECT_TYPE         = "fake";

  private ObserverService     observerService;

  protected String            objectType          = OBJECT_TYPE;

  protected String            objectId;

  protected List<String>      allowedObjectId;

  protected long[]            allowedIdentityIds;

  protected long              spaceId             = Math.round(Math.random() * 10000);

  protected long              audienceId          = Math.round(Math.random() * 20000);

  protected long              identityId          = Math.round(Math.random() * 30000);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    observerService = getContainer().getComponentInstanceOfType(ObserverService.class);
    observerService.removePlugin(OBJECT_TYPE);
    observerService.addPlugin(new TestObserverPlugin());
    spaceId++;
    audienceId++;
    identityId++;
    objectType = OBJECT_TYPE;
    objectId = spaceId + "-" + audienceId;
    allowedObjectId = new ArrayList<>();
    allowedObjectId.add(objectId);
    allowedIdentityIds = new long[0];
    begin();
  }

  @Override
  protected void tearDown() throws Exception {
    end();
    super.tearDown();
  }

  public void testCreateObserver() throws IllegalAccessException, ObjectAlreadyExistsException, ObjectNotFoundException {
    assertFalse(observerService.isObserved(identityId, objectType, objectId));
    assertThrows(IllegalAccessException.class, () -> observerService.createObserver(identityId, OBJECT_TYPE, objectId));
    allowedIdentityIds = new long[] { identityId };
    assertThrows(ObjectNotFoundException.class,
                 () -> observerService.createObserver(identityId, OBJECT_TYPE, NOT_FOUND_OBJECT_ID));
    observerService.createObserver(identityId, OBJECT_TYPE, objectId);
    assertTrue(observerService.isObserved(identityId, objectType, objectId));
    assertThrows(ObjectAlreadyExistsException.class, () -> observerService.createObserver(identityId, OBJECT_TYPE, objectId));
  }

  public void testDeleteObserver() throws IllegalAccessException, ObjectAlreadyExistsException, ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class,
                 () -> observerService.deleteObserver(identityId, OBJECT_TYPE, NOT_FOUND_OBJECT_ID));

    assertFalse(observerService.isObserved(identityId, objectType, objectId));
    allowedIdentityIds = new long[] { identityId };
    observerService.createObserver(identityId, OBJECT_TYPE, objectId);
    assertTrue(observerService.isObserved(identityId, objectType, objectId));
    observerService.deleteObserver(identityId, OBJECT_TYPE, objectId);
    assertFalse(observerService.isObserved(identityId, objectType, objectId));
    observerService.createObserver(identityId, OBJECT_TYPE, objectId);
    assertTrue(observerService.isObserved(identityId, objectType, objectId));
    observerService.deleteObserver(identityId, OBJECT_TYPE, objectId);
    assertFalse(observerService.isObserved(identityId, objectType, objectId));
  }

  public void testIsObservable() {
    assertFalse(observerService.isObservable(identityId, OBJECT_TYPE, NOT_FOUND_OBJECT_ID));
    assertFalse(observerService.isObservable(identityId, OBJECT_TYPE, objectId));
    allowedIdentityIds = new long[] { identityId };
    assertTrue(observerService.isObservable(identityId, OBJECT_TYPE, objectId));
    assertFalse(observerService.isObservable(identityId, OBJECT_TYPE, NOT_FOUND_OBJECT_ID));
  }

  public void testGetObservedObjects() {
    long identityId1 = ++identityId;
    long identityId2 = ++identityId;
    allowedIdentityIds = new long[] { identityId1, identityId2 };
    List<ObserverObject> observedObjects = observerService.getObservedObjects(identityId1, 0, 10);
    assertNotNull(observedObjects);
    assertTrue(observedObjects.isEmpty());

    String[] objectId1s = { "obj1" + identityId1, "obj2" + identityId1 };
    Arrays.stream(objectId1s).forEach(id -> {
      allowedObjectId.add(id);
      try {
        observerService.createObserver(identityId1, OBJECT_TYPE, id);
        observerService.createObserver(identityId2, OBJECT_TYPE, id);
      } catch (Exception e) {
        fail(e);
      }
    });
    String[] objectId2s = { "obj1" + identityId2, "obj2" + identityId2 };
    Arrays.stream(objectId2s).forEach(id -> {
      allowedObjectId.add(id);
      try {
        observerService.createObserver(identityId2, OBJECT_TYPE, id);
      } catch (Exception e) {
        fail(e);
      }
    });

    observedObjects = observerService.getObservedObjects(identityId1, 0, 10);
    assertNotNull(observedObjects);
    assertEquals(2, observedObjects.size());

    observedObjects = observerService.getObservedObjects(identityId2, 0, 10);
    assertNotNull(observedObjects);
    assertEquals(4, observedObjects.size());
  }

  public void testGetObserverIdentityIds() {
    long identityId1 = ++identityId;
    long identityId2 = ++identityId;
    allowedIdentityIds = new long[] { identityId1, identityId2 };
    List<Long> observerIdentityIds = observerService.getObserverIdentityIds(objectType, objectId);
    assertNotNull(observerIdentityIds);
    assertTrue(observerIdentityIds.isEmpty());

    String[] objectId1s = { "obj1" + identityId1, "obj2" + identityId1 };
    Arrays.stream(objectId1s).forEach(id -> {
      allowedObjectId.add(id);
      try {
        observerService.createObserver(identityId1, OBJECT_TYPE, id);
        observerService.createObserver(identityId2, OBJECT_TYPE, id);
      } catch (Exception e) {
        fail(e);
      }
    });
    String[] objectId2s = { "obj1" + identityId2, "obj2" + identityId2 };
    Arrays.stream(objectId2s).forEach(id -> {
      allowedObjectId.add(id);
      try {
        observerService.createObserver(identityId2, OBJECT_TYPE, id);
      } catch (Exception e) {
        fail(e);
      }
    });

    observerIdentityIds = observerService.getObserverIdentityIds(objectType, objectId1s[0]);
    assertNotNull(observerIdentityIds);
    assertEquals(2, observerIdentityIds.size());

    observerIdentityIds = observerService.getObserverIdentityIds(objectType, objectId1s[1]);
    assertNotNull(observerIdentityIds);
    assertEquals(2, observerIdentityIds.size());

    observerIdentityIds = observerService.getObserverIdentityIds(objectType, objectId2s[0]);
    assertNotNull(observerIdentityIds);
    assertEquals(1, observerIdentityIds.size());

    observerIdentityIds = observerService.getObserverIdentityIds(objectType, objectId2s[1]);
    assertNotNull(observerIdentityIds);
    assertEquals(1, observerIdentityIds.size());
  }

  public class TestObserverPlugin extends ObserverPlugin {

    @Override
    public boolean canObserve(long identityId, String id) throws ObjectNotFoundException {
      if (!allowedObjectId.contains(id)) {
        throw new ObjectNotFoundException("Not viewable");
      }
      return Arrays.binarySearch(allowedIdentityIds, identityId) >= 0;
    }

    @Override
    public long getAudienceId(String id) throws ObjectNotFoundException {
      if (!allowedObjectId.contains(id)) {
        throw new ObjectNotFoundException("No audience");
      }
      return audienceId;
    }

    @Override
    public long getSpaceId(String id) throws ObjectNotFoundException {
      if (!allowedObjectId.contains(id)) {
        throw new ObjectNotFoundException("No space");
      }
      return spaceId;
    }

    @Override
    public String getObjectType() {
      return objectType;
    }
  }
}
