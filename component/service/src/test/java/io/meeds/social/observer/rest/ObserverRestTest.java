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
package io.meeds.social.observer.rest;

import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;
import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.social.service.test.AbstractResourceTest;

import io.meeds.social.observe.model.ObserverObject;
import io.meeds.social.observe.plugin.ObserverPlugin;
import io.meeds.social.observe.service.ObserverService;

public class ObserverRestTest extends AbstractResourceTest { // NOSONAR

  private static final String            BASE_URL            = "/social/observers";              // NOSONAR

  private static MockedStatic<RestUtils> REST_UTILS;                                             // NOSONAR

  private static final String            NOT_FOUND_OBJECT_ID = "notFound";

  private static final String            OBJECT_TYPE         = "fake";

  protected String                       objectType          = OBJECT_TYPE;

  protected String                       objectId;

  protected List<String>                 allowedObjectId;

  protected long[]                       allowedIdentityIds;

  protected long                         spaceId             = Math.round(Math.random() * 10000);

  protected long                         audienceId          = Math.round(Math.random() * 20000);

  protected long                         identityId          = Math.round(Math.random() * 30000);

  private ObserverRest                   observerRest;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ObserverService observerService = getContainer().getComponentInstanceOfType(ObserverService.class);
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
    observerRest = new ObserverRest(observerService);
    registry(observerRest);

    ExoContainerContext.setCurrentContainer(getContainer());
    restartTransaction();
    begin();
    REST_UTILS = mockStatic(RestUtils.class); // NOSONAR
  }

  @Override
  protected void tearDown() throws Exception {
    removeResource(observerRest.getClass());
    end();
    super.tearDown();
    REST_UTILS.close(); // NOSONAR
  }

  @Test
  public void testCreateObserver() {
    assertFalse(isObserved(identityId, objectType, objectId));
    ContainerResponse response = createObserver(identityId, OBJECT_TYPE, objectId);
    assertEquals(401, response.getStatus());

    allowedIdentityIds = new long[] { identityId };
    response = createObserver(identityId, OBJECT_TYPE, NOT_FOUND_OBJECT_ID);
    assertEquals(404, response.getStatus());

    response = createObserver(identityId, OBJECT_TYPE, objectId);
    assertEquals(204, response.getStatus());

    assertTrue(isObserved(identityId, objectType, objectId));

    response = createObserver(identityId, OBJECT_TYPE, objectId);
    assertEquals(204, response.getStatus());
    response = createObserver(identityId, OBJECT_TYPE, objectId);
    assertEquals(204, response.getStatus());
  }

  public void testDeleteObserver() {
    ContainerResponse response = deleteObserver(identityId, OBJECT_TYPE, NOT_FOUND_OBJECT_ID);
    assertEquals(404, response.getStatus());

    assertFalse(isObserved(identityId, objectType, objectId));
    allowedIdentityIds = new long[] { identityId };
    createObserver(identityId, OBJECT_TYPE, objectId);
    assertTrue(isObserved(identityId, objectType, objectId));

    response = deleteObserver(identityId, OBJECT_TYPE, objectId);
    assertEquals(204, response.getStatus());

    assertFalse(isObserved(identityId, objectType, objectId));

    createObserver(identityId, OBJECT_TYPE, objectId);
    assertTrue(isObserved(identityId, objectType, objectId));

    response = deleteObserver(identityId, OBJECT_TYPE, objectId);
    assertEquals(204, response.getStatus());
    assertFalse(isObserved(identityId, objectType, objectId));
  }

  public void testGetObservedObjects() {
    long identityId1 = ++identityId;
    long identityId2 = ++identityId;
    allowedIdentityIds = new long[] { identityId1, identityId2 };
    List<ObserverObject> observedObjects = getObservedObjects(identityId1);
    assertNotNull(observedObjects);
    assertTrue(observedObjects.isEmpty());

    String[] objectId1s = { "obj1" + identityId1, "obj2" + identityId1 };
    Arrays.stream(objectId1s).forEach(id -> {
      allowedObjectId.add(id);
      try {
        createObserver(identityId1, OBJECT_TYPE, id);
        createObserver(identityId2, OBJECT_TYPE, id);
      } catch (Exception e) {
        fail(e);
      }
    });
    String[] objectId2s = { "obj1" + identityId2, "obj2" + identityId2 };
    Arrays.stream(objectId2s).forEach(id -> {
      allowedObjectId.add(id);
      try {
        createObserver(identityId2, OBJECT_TYPE, id);
      } catch (Exception e) {
        fail(e);
      }
    });

    observedObjects = getObservedObjects(identityId1);
    assertNotNull(observedObjects);
    assertEquals(2, observedObjects.size());

    observedObjects = getObservedObjects(identityId2);
    assertNotNull(observedObjects);
    assertEquals(4, observedObjects.size());
  }

  private String getUrl() {
    return BASE_URL;
  }

  private String getUrl(String objectType, String objectId) {
    return getUrl() + "/" + objectType + "/" + objectId;
  }

  @SuppressWarnings("unchecked")
  private List<ObserverObject> getObservedObjects(long identityId) {
    REST_UTILS.reset();
    REST_UTILS.when(RestUtils::getCurrentUserIdentityId).thenReturn(identityId);
    try {
      ContainerResponse response = getResponse("GET", getUrl(), null);
      return (List<ObserverObject>) response.getEntity();
    } catch (Exception e) {
      throw new IllegalStateException("Error while checking observer", e);
    }
  }

  private boolean isObserved(long identityId, String objectType, String objectId) {
    REST_UTILS.reset();
    REST_UTILS.when(RestUtils::getCurrentUserIdentityId).thenReturn(identityId);
    try {
      ContainerResponse response = getResponse("GET", getUrl(objectType, objectId), null);
      return response != null && response.getStatus() < 300 && response.getEntity() != null
          && StringUtils.equals(response.getEntity().toString(), "true");
    } catch (Exception e) {
      throw new IllegalStateException("Error while checking observer", e);
    }
  }

  private ContainerResponse createObserver(long identityId, String objectType, String objectId) {
    REST_UTILS.reset();
    REST_UTILS.when(RestUtils::getCurrentUserIdentityId).thenReturn(identityId);
    try {
      return getResponse("POST", getUrl(objectType, objectId), null);
    } catch (Exception e) {
      throw new IllegalStateException("Error while creating observer", e);
    }
  }

  private ContainerResponse deleteObserver(long identityId, String objectType, String objectId) {
    REST_UTILS.reset();
    REST_UTILS.when(RestUtils::getCurrentUserIdentityId).thenReturn(identityId);
    try {
      return getResponse("DELETE", getUrl(objectType, objectId), null);
    } catch (Exception e) {
      throw new IllegalStateException("Error while deleting observer", e);
    }
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
