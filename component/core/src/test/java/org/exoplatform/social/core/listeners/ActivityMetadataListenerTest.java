/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.MetadataTypePlugin;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

public class ActivityMetadataListenerTest extends AbstractCoreTest {

  private static final Log        LOG                = ExoLogger.getLogger(ActivityMetadataListenerTest.class);

  private static final String     METADATA_TYPE_NAME = "testMetadataListener";

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private MetadataService         metadataService;

  private MetadataDAO             metadataDAO;

  private List<ExoSocialActivity> tearDownActivityList;

  private List<Space>             tearDownSpaceList;

  @Override
  public void setUp() throws Exception {
    try {
      super.setUp();
    } catch (Exception e) {
      LOG.error("Error initializing parent Test class", e);
    }
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    if (metadataService.getMetadataTypeByName(METADATA_TYPE_NAME) == null) {
      MetadataTypePlugin userMetadataTypePlugin = new MetadataTypePlugin(newParam(2654, METADATA_TYPE_NAME)) {
        @Override
        public boolean isAllowMultipleItemsPerObject() {
          return true;
        }

        @Override
        public boolean isShareable() {
          return true;
        }
      };
      metadataService.addMetadataTypePlugin(userMetadataTypePlugin);
    }

    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    tearDownActivityList = new ArrayList<>();
    tearDownSpaceList = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    metadataDAO.deleteAll();

    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    super.tearDown();
  }

  public void testDeleteActivity() throws ObjectAlreadyExistsException {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME,
                                              "testMetadata2",
                                              audienceId);
    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       activity.getId());
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    activityManager.deleteActivity(activity);

    restartTransaction();

    metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(0, metadataItems.size());
  }

  public void testDeleteComment() throws ObjectAlreadyExistsException {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(activityTitle);
    comment.setUserId(userId);
    comment.setPosterId(userId);
    activityManager.saveComment(activity, comment);

    long creatorId = Long.parseLong(johnIdentity.getId());
    long audienceId = creatorId;
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME,
                                              "testMetadata2",
                                              audienceId);
    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       comment.getId());
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, creatorId);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    activityManager.deleteComment(activity, comment);

    restartTransaction();

    metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(0, metadataItems.size());
  }

  public void testShareActivity() throws Exception {
    Space originalSpace = createSpace("OriginalSpace",
                                      johnIdentity.getRemoteId(),
                                      johnIdentity.getRemoteId(),
                                      maryIdentity.getRemoteId());
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(originalSpace.getPrettyName());

    ExoSocialActivity originalActivity = new ExoSocialActivityImpl();
    originalActivity.setTitle("TestSourceActivitySpace");
    originalActivity.setUserId(johnIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, originalActivity);

    MetadataObject metadataObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                       originalActivity.getId());
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME,
                                              "testMetadata2",
                                              Long.parseLong(spaceIdentity.getId()));
    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject,
                                                                   metadataKey,
                                                                   Long.parseLong(johnIdentity.getId()));
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    originalActivity = activityManager.getActivity(originalActivity.getId());
    assertTrue(CollectionUtils.isEmpty(originalActivity.getShareActions()));

    Space targetSpace = createSpace("TargetSharedActivitySpace",
                                    maryIdentity.getRemoteId(),
                                    johnIdentity.getRemoteId(),
                                    maryIdentity.getRemoteId());
    Identity targetSpaceIdentity = identityManager.getOrCreateSpaceIdentity(targetSpace.getPrettyName());
    restartTransaction();

    org.exoplatform.services.security.Identity marySecurityIdentity = new org.exoplatform.services.security.Identity("mary");

    ExoSocialActivity shareActivityTemplate = new ExoSocialActivityImpl();
    shareActivityTemplate.setTitle("Shared Activity");
    shareActivityTemplate.setUserId(maryIdentity.getId());
    List<ExoSocialActivity> sharedActivities = activityManager.shareActivity(shareActivityTemplate,
                                                                             originalActivity.getId(),
                                                                             Collections.singletonList(targetSpace.getPrettyName()),
                                                                             marySecurityIdentity);
    assertNotNull(sharedActivities);
    assertEquals(1, sharedActivities.size());
    ExoSocialActivity sharedActivity = sharedActivities.get(0);
    assertNotEquals(originalActivity.getId(), sharedActivity.getId());
    restartTransaction();

    MetadataObject sharedActivityMetadataObject =
                                                new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                                   sharedActivity.getId());
    metadataItems = metadataService.getMetadataItemsByObject(sharedActivityMetadataObject);
    assertNotNull(metadataItem);
    assertEquals(1, metadataItems.size());

    MetadataItem sharedMetadataItem = metadataItems.get(0);
    assertNotEquals(metadataItem.getId(), sharedMetadataItem.getId());
    assertEquals(sharedActivityMetadataObject, sharedMetadataItem.getObject());
    assertEquals(Long.parseLong(targetSpaceIdentity.getId()), sharedMetadataItem.getMetadata().getAudienceId());
    assertEquals(Long.parseLong(maryIdentity.getId()), sharedMetadataItem.getMetadata().getCreatorId());
    assertEquals(metadataKey.getName(), sharedMetadataItem.getMetadata().getName());
    assertEquals(metadataKey.getType(), sharedMetadataItem.getMetadata().getTypeName());
    assertEquals(Long.parseLong(maryIdentity.getId()), sharedMetadataItem.getCreatorId());
  }

  private InitParams newParam(long id, String name) {
    InitParams params = new InitParams();
    MetadataType metadataType = new MetadataType(id, name);
    ObjectParameter parameter = new ObjectParameter();
    parameter.setName("metadataType");
    parameter.setObject(metadataType);
    params.addParameter(parameter);
    return params;
  }

  @SuppressWarnings("deprecation")
  private Space createSpace(String spaceName, String creator, String... members) throws Exception {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    String[] managers = new String[] { creator };
    String[] spaceMembers = members == null ? new String[] { creator } : members;
    space.setManagers(managers);
    space.setMembers(spaceMembers);
    spaceService.saveSpace(space, true); // NOSONAR
    tearDownSpaceList.add(space);
    return space;
  }

}
