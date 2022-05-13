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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.storage.dao.jpa.MetadataDAO;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

public class ActivityTagMetadataListenerTest extends AbstractCoreTest {

  private final Log               LOG = ExoLogger.getLogger(ActivityTagMetadataListenerTest.class);

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private ActivityManager         activityManager;

  private IdentityManager         identityManager;

  private MetadataService         metadataService;

  private TagService              tagService;

  private MetadataDAO             metadataDAO;

  private List<ExoSocialActivity> tearDownActivityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    metadataService = getContainer().getComponentInstanceOfType(MetadataService.class);
    tagService = getContainer().getComponentInstanceOfType(TagService.class);
    metadataDAO = getContainer().getComponentInstanceOfType(MetadataDAO.class);
    rootIdentity = identityManager.getOrCreateUserIdentity("root");
    johnIdentity = identityManager.getOrCreateUserIdentity("john");
    maryIdentity = identityManager.getOrCreateUserIdentity("mary");
    tearDownActivityList = new ArrayList<>();
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

    super.tearDown();
  }

  public void testSaveActivity() {
    String activityTitle =
                         "<div>Test tag #NoTagHere test test"
                             + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANewTagHere</a>&nbsp;.</div>";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    TagObject metadataObjectKey = new TagObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                activity.getId());
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObjectKey);
    assertNotNull(metadataItems);
    assertEquals(1, metadataItems.size());
    assertEquals(Collections.singleton(new TagName("ANewTagHere")),
                 metadataItems.stream()
                              .map(MetadataItem::getMetadata)
                              .map(Metadata::getName)
                              .map(TagName::new)
                              .collect(Collectors.toSet()));

    Set<TagName> tagNames = tagService.getTagNames(metadataObjectKey);
    assertEquals(Collections.singleton(new TagName("ANewTagHere")),
                 tagNames);
  }

  public void testUpdateActivity() {
    String activityTitle =
                         "<div>Test tag #NoTagHere test test"
                             + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANewTagHere</a>&nbsp;.</div>";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    TagObject metadataObjectKey = new TagObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                activity.getId());
    Set<TagName> tagNames = tagService.getTagNames(metadataObjectKey);
    String tagName1 = "ANewTagHere";
    assertEquals(Collections.singleton(new TagName(tagName1)),
                 tagNames);

    activity = activityManager.getActivity(activity.getId());

    String activityTitle2 =
                          "<div>Test tag #NoTagHere test test"
                              + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANewTagHere2</a>&nbsp;.</div>";
    activity.setTitle(activityTitle2);
    activityManager.updateActivity(activity);
    tagNames = tagService.getTagNames(metadataObjectKey);
    String tagName2 = "ANewTagHere2";
    assertEquals(Collections.singleton(new TagName(tagName2)),
                 tagNames);

    activity.setTitle("");
    activityManager.updateActivity(activity);
    tagNames = tagService.getTagNames(metadataObjectKey);
    assertEquals(Collections.emptySet(),
                 tagNames);

    String activityTitle3 =
                          "<div>Test tag #NoTagHere test test"
                              + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANewTagHere</a>"
                              + " <a target=\"_blank\" class=\"metadata-tag\" rel=\"noopener\">#ANewTagHere2</a>"
                              + "&nbsp;.</div>";
    activity.setTitle(activityTitle3);
    activityManager.updateActivity(activity);

    tagNames = tagService.getTagNames(metadataObjectKey);
    assertEquals(2, tagNames.size());
    assertTrue(tagNames.contains(new TagName(tagName1)));
    assertTrue(tagNames.contains(new TagName(tagName2)));
  }

}
