/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http:www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.manager;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.*;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.ActivityStreamType;
import org.exoplatform.social.core.activity.ActivitySystemTypePlugin;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * 
 * @author hoat_le
 */
public class ActivityManagerTest extends AbstractCoreTest {
  private final Log               LOG = ExoLogger.getLogger(ActivityManagerTest.class);

  private List<ExoSocialActivity> tearDownActivityList;

  private List<Space>             tearDownSpaceList;

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private Identity                demoIdentity;

  private Identity                ghostIdentity;

  private Identity                raulIdentity;

  private Identity                jameIdentity;

  private Identity                paulIdentity;

  private IdentityManager         identityManager;

  private ActivityManager         activityManager;

  private ActivityStorage         activityStorage;

  private SpaceService            spaceService;

  private UserACL                 userACL;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    activityStorage = getContainer().getComponentInstanceOfType(ActivityStorage.class);
    userACL = getContainer().getComponentInstanceOfType(UserACL.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    tearDownActivityList = new ArrayList<>();
    tearDownSpaceList = new ArrayList<>();
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost");
    raulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul");
    jameIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jame");
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul");

    PropertyManager.setProperty("exo.activity-type.cs-calendar:spaces.enabled", "false");
    PropertyManager.setProperty("exo.activity-type.MY_ACTIVITY.enabled", "false");
    ((ActivityManagerImpl) activityManager).initActivityTypes();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();

    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }

    // RealtimeListAccess<ExoSocialActivity> demoActivities =
    // activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    // assertEquals(0, demoActivities.getSize());

    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(jameIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);

    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    super.tearDown();
  }

  /**
   * Test
   * {@link ActivityManager#saveActivityNoReturn(Identity, ExoSocialActivity)}
   */
  public void testSaveActivityNoReturn() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    // test for reserving order of map values for i18n activity
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("key1", "value 1");
    templateParams.put("key2", "value 2");
    templateParams.put("key3", "value 3");
    activity.setTemplateParams(templateParams);
    activity.setTitle(activityTitle);
    activity.setUserId(userId);

    //
    activity.isHidden(false);
    activity.isLocked(true);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    Map<String, String> gotTemplateParams = activity.getTemplateParams();
    List<String> values = new ArrayList<>(gotTemplateParams.values());
    assertEquals("value 1", values.get(0));
    assertEquals("value 2", values.get(1));
    assertEquals("value 3", values.get(2));

    //
    assertTrue(activity.isLocked());
    assertFalse(activity.isHidden());
  }

  /**
   * Test {@link ActivityManager#saveActivityNoReturn(ExoSocialActivity)}
   */
  public void testSaveActivityNoReturnNotStreamOwner() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    tearDownActivityList.add(activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
  }

  public void testShouldNotEditActivityWhenHasJoinedComment() throws Exception {
    // Given
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    String activityTitle = "activity title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = createActivity();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    activity = activityManager.getActivity(activity.getId());
    ExoSocialActivity comment = createComment(activity, "exosocial:spaces", "has_joined");

    // When
    boolean isEditable = activityManager.isActivityEditable(comment, identity);

    // Then
    assertFalse(isEditable);

    // Finally
    activityManager.deleteComment(activity, comment);
    activityManager.deleteActivity(activity);
  }

  public void testShouldNotEditActivityWhenSpaceDescriptionEditedComment() throws Exception {
    // Given
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    String activityTitle = "activity title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = createActivity();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    activity = activityManager.getActivity(activity.getId());
    ExoSocialActivity comment = createComment(activity, "exosocial:spaces", "space_description_edited");

    // When
    boolean isEditable = activityManager.isActivityEditable(comment, identity);

    // Then
    assertFalse(isEditable);

    // Finally
    activityManager.deleteComment(activity, comment);
    activityManager.deleteActivity(activity);
  }

  public void testShouldEditActivityWhenPostComment() throws Exception {
    // Given
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    String activityTitle = "activity title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = createActivity();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    activity = activityManager.getActivity(activity.getId());
    ExoSocialActivity comment = createComment(activity, "exosocial:spaces", "text comment");

    // When
    boolean isEditable = activityManager.isActivityEditable(comment, identity);

    // Then
    assertTrue(isEditable);

    // Finally
    activityManager.deleteComment(activity, comment);
    activityManager.deleteActivity(activity);
  }

  public void testShouldNotEditActivityWhenWikiSpacePostedComment() throws Exception {
    // Given
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity("root");
    String activityTitle = "activity title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = createActivity();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    activity = activityManager.getActivity(activity.getId());

    InitParams params = new InitParams();
    ValuesParam param = new ValuesParam();
    param.setName(ActivitySystemTypePlugin.SYSTEM_TYPES_PARAM);
    param.setValues(Collections.singletonList("ks-wiki:spaces"));
    params.addParameter(param);
    ActivitySystemTypePlugin plugin = new ActivitySystemTypePlugin(params);
    activityManager.addSystemActivityDefinition(plugin);

    ExoSocialActivity comment = createComment(activity, "ks-wiki:spaces", "text comment");

    // When
    boolean isEditable = activityManager.isActivityEditable(comment, identity);

    // Then
    assertFalse(isEditable);

    // Finally
    activityManager.deleteComment(activity, comment);
    activityManager.deleteActivity(activity);
  }

  public void testCanPostOnStream() throws Exception {
    org.exoplatform.services.security.Identity rootACLIdentity = new org.exoplatform.services.security.Identity("root");
    org.exoplatform.services.security.Identity johnACLIdentity = new org.exoplatform.services.security.Identity("john",
                                                                                                                Collections.singleton(new MembershipEntry("/platform/administrators",
                                                                                                                                                          "*")));
    org.exoplatform.services.security.Identity demoACLIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesACLIdentity = new org.exoplatform.services.security.Identity("james");
    org.exoplatform.services.security.Identity maryACLIdentity = new org.exoplatform.services.security.Identity("mary");

    ConversationState.setCurrent(new ConversationState(demoACLIdentity));

    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName(ActivityManagerImpl.ENABLE_USER_COMPOSER);
    params.addParameter(param);

    param.setValue("false");
    ActivityManager activityManager = new ActivityManagerImpl(activityStorage, identityManager, spaceService, userACL, params);
    assertFalse(activityManager.isEnableUserComposer());

    // User can't post on his own stream
    assertFalse(activityManager.canPostActivityInStream(demoACLIdentity, demoIdentity));
    // super user can't post his own stream
    assertFalse(activityManager.canPostActivityInStream(rootACLIdentity, rootIdentity));
    // super user can't post on stream of others
    assertFalse(activityManager.canPostActivityInStream(rootACLIdentity, demoIdentity));

    param.setValue("true");
    activityManager = new ActivityManagerImpl(activityStorage, identityManager, spaceService, userACL, params);
    assertTrue(activityManager.isEnableUserComposer());

    // User can't post on his own stream
    assertTrue(activityManager.canPostActivityInStream(demoACLIdentity, demoIdentity));
    // super user can't post on others stream
    assertFalse(activityManager.canPostActivityInStream(rootACLIdentity, demoIdentity));

    Space space = createSpace("spaceTest", "demo");
    space.setMembers(new String[]{"demo", "james"});
    spaceService.updateSpace(space);

    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    // Super Manager can redact
    assertTrue(activityManager.canPostActivityInStream(rootACLIdentity, spaceIdentity));
    // Platform Manager can redact
    assertTrue(activityManager.canPostActivityInStream(johnACLIdentity, spaceIdentity));
    // Space Manager can redact
    assertTrue(activityManager.canPostActivityInStream(demoACLIdentity, spaceIdentity));
    // Member can redact
    assertTrue(activityManager.canPostActivityInStream(jamesACLIdentity, spaceIdentity));
    // Outside space can't redact
    assertFalse(activityManager.canPostActivityInStream(maryACLIdentity, spaceIdentity));

    space.setMembers(new String[]{"demo", "james", "mary"});
    space.setRedactors(new String[]{"james"});
    spaceService.updateSpace(space);

    // Super Manager can redact
    assertTrue(activityManager.canPostActivityInStream(rootACLIdentity, spaceIdentity));
    // Platform Manager can redact
    assertTrue(activityManager.canPostActivityInStream(johnACLIdentity, spaceIdentity));
    // Space Manager can redact
    assertTrue(activityManager.canPostActivityInStream(demoACLIdentity, spaceIdentity));
    // Redactor can redact
    assertTrue(activityManager.canPostActivityInStream(jamesACLIdentity, spaceIdentity));
    // space member can't redact
    assertFalse(activityManager.canPostActivityInStream(maryACLIdentity, spaceIdentity));
  }

  public void testShareActivity() throws Exception { // NOSONAR
    Space originalSpace = createSpace("OriginalSpace", "john", "john", "demo");
    ExoSocialActivity originalActivity = new ExoSocialActivityImpl();
    originalActivity.setTitle("Test original Activity");
    originalActivity.setUserId(johnIdentity.getId());

    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(originalSpace.getPrettyName());
    activityManager.saveActivityNoReturn(spaceIdentity, originalActivity);

    originalActivity = activityManager.getActivity(originalActivity.getId());
    assertTrue(CollectionUtils.isEmpty(originalActivity.getShareActions()));

    Space targetSpace = createSpace("TargetSpace", "james", "james", "mary", "demo");
    String shareMessage = "share Message";
    String type = "sharedActivityTest"; // No matter, will be removed from API
                                        // anyway

    org.exoplatform.services.security.Identity johnSecurityIdentity = new org.exoplatform.services.security.Identity("john");
    org.exoplatform.services.security.Identity demoSecurityIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesSecurityIdentity = new org.exoplatform.services.security.Identity("james");

    ExoSocialActivity shareActivityTemplate = new ExoSocialActivityImpl();
    shareActivityTemplate.setTitle(shareMessage);
    shareActivityTemplate.setType(type);
    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList(targetSpace.getPrettyName()),
                                    johnSecurityIdentity);
      fail("John is not member of target space");
    } catch (IllegalAccessException e) {
      // Expected
    }

    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    "25556",
                                    Arrays.asList(targetSpace.getPrettyName()),
                                    demoSecurityIdentity);
      fail("Activity id shouldn't be found");
    } catch (ObjectNotFoundException e) {
      // Expected
    }

    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList(targetSpace.getPrettyName()),
                                    jamesSecurityIdentity);
      fail("James shouldn't be able to access original activity");
    } catch (IllegalAccessException e) {
      // Expected
    }

    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList("5496632"),
                                    demoSecurityIdentity);
      fail("Fake space pretty name shouldn't be found");
    } catch (ObjectNotFoundException e) {
      // Expected
    }

    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList("5496632"),
                                    demoSecurityIdentity);
      fail("Fake space pretty name shouldn't be found");
    } catch (ObjectNotFoundException e) {
      // Expected
    }
    
    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    null,
                                    Arrays.asList("5496632"),
                                    demoSecurityIdentity);
      fail("Activity id should be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    new ArrayList<>(),
                                    demoSecurityIdentity);
      fail("Spaces should be mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    
    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList("5496632"),
                                    null);
      fail("User identity is mandatory");
    } catch (IllegalAccessException e) {
      // Expected
    }

    originalActivity = activityManager.getActivity(originalActivity.getId());
    assertTrue(CollectionUtils.isEmpty(originalActivity.getShareActions()));

    List<ExoSocialActivity> sharedActivities = activityManager.shareActivity(shareActivityTemplate,
                                                                             originalActivity.getId(),
                                                                             Arrays.asList(targetSpace.getPrettyName()),
                                                                             demoSecurityIdentity);
    assertNotNull(sharedActivities);
    assertEquals(1, sharedActivities.size());

    ExoSocialActivity sharedactivity = sharedActivities.get(0);
    assertNotNull(sharedactivity);
    assertNotNull(sharedactivity.getId());
    assertNotNull(sharedactivity.getTitle());
    assertEquals(type, sharedactivity.getType());
    assertEquals(shareMessage, sharedactivity.getTitle());
    assertEquals(targetSpace.getPrettyName(), sharedactivity.getStreamOwner());
    assertNotNull(sharedactivity.getTemplateParams());
    assertEquals(originalActivity.getId(), sharedactivity.getTemplateParams().get(ActivityManager.SHARED_ACTIVITY_ID_PARAM));

    // Verify again from storage
    sharedactivity = activityManager.getActivity(sharedactivity.getId());
    assertNotNull(sharedactivity.getTitle());
    assertEquals(type, sharedactivity.getType());
    assertEquals(shareMessage, sharedactivity.getTitle());
    assertEquals(targetSpace.getPrettyName(), sharedactivity.getStreamOwner());
    assertNotNull(sharedactivity.getTemplateParams());
    assertEquals(originalActivity.getId(), sharedactivity.getTemplateParams().get(ActivityManager.SHARED_ACTIVITY_ID_PARAM));

    originalActivity = activityManager.getActivity(originalActivity.getId());
    Set<ActivityShareAction> shareActions = originalActivity.getShareActions();

    assertTrue(CollectionUtils.isNotEmpty(shareActions));
    assertEquals(1, shareActions.size());
    ActivityShareAction shareAction = shareActions.iterator().next();
    assertEquals(originalActivity.getId(), String.valueOf(shareAction.getActivityId()));
    assertEquals(demoIdentity.getId(), String.valueOf(shareAction.getUserIdentityId()));
    assertEquals(shareMessage, String.valueOf(shareAction.getMessage()));
    assertNotNull(shareAction.getSharedActivityIds());
    assertEquals(1, shareAction.getSharedActivityIds().size());
    assertEquals(sharedactivity.getId(), String.valueOf(shareAction.getSharedActivityIds().iterator().next()));
    assertNotNull(shareAction.getSpaceIds());
    assertEquals(1, shareAction.getSpaceIds().size());
    assertEquals(targetSpace.getId(), String.valueOf(shareAction.getSpaceIds().iterator().next()));

    sharedActivities = activityManager.shareActivity(null,
                                                     originalActivity.getId(),
                                                     Arrays.asList(targetSpace.getPrettyName()),
                                                     demoSecurityIdentity);
    assertNotNull(sharedActivities);
    assertEquals(1, sharedActivities.size());

    targetSpace.setRedactors(new String[] { "james" });
    spaceService.updateSpace(targetSpace);

    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList(targetSpace.getPrettyName()),
                                    demoSecurityIdentity);
      fail("Demo is not redactor of target space anymore");
    } catch (IllegalAccessException e) {
      // Expected
    }

    targetSpace.setRedactors(new String[] { "james", "demo" });
    spaceService.updateSpace(targetSpace);

    sharedActivities = activityManager.shareActivity(null,
                                                     originalActivity.getId(),
                                                     Arrays.asList(targetSpace.getPrettyName()),
                                                     demoSecurityIdentity);
    assertNotNull(sharedActivities);
    assertEquals(1, sharedActivities.size());
  }

  /**
   * Test {@link ActivityManager#getActivity(String)}
   */
  public void testGetActivity() {
    String activityTitle = "title";
    String userId = rootIdentity.getId();

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);

    activityManager.saveActivityNoReturn(rootIdentity, activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
  }

  /**
   * Tests {@link ActivityManager#getParentActivity(ExoSocialActivity)}.
   */
  public void testGetParentActivity() {
    populateActivityMass(demoIdentity, 1);
    RealtimeListAccess<ExoSocialActivity> activitiesWithListAccess = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertNotNull(activitiesWithListAccess);
    assertTrue(activitiesWithListAccess.getSize() > 0);

    ExoSocialActivity demoActivity = activitiesWithListAccess.load(0, 1)[0];
    assertNotNull("demoActivity must be false", demoActivity);
    assertNull(activityManager.getParentActivity(demoActivity));

    // comment
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(demoActivity, comment);
    ExoSocialActivity gotComment = activityManager.getCommentsWithListAccess(demoActivity).load(0, 1)[0];
    assertNotNull("gotComment must not be null", gotComment);
    ExoSocialActivity parentActivity = activityManager.getParentActivity(gotComment);
    assertNotNull("parentActivity must not be null", parentActivity);
    assertEquals("parentActivity.getId() must return: " + demoActivity.getId(),
                 demoActivity.getId(),
                 parentActivity.getId());
    assertEquals("parentActivity.getTitle() must return: " + demoActivity.getTitle(),
                 demoActivity.getTitle(),
                 parentActivity.getTitle());
    assertEquals("parentActivity.getUserId() must return: " + demoActivity.getUserId(),
                 demoActivity.getUserId(),
                 parentActivity.getUserId());
  }

  /**
   * Test {@link ActivityManager#updateActivity(ExoSocialActivity)}
   */
  public void testUpdateActivity() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    tearDownActivityList.add(activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());

    String newTitle = "new activity title";
    activity.setTitle(newTitle);
    activityManager.updateActivity(activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + newTitle, newTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
  }

  /**
   * {@link ActivityManager#deleteActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   */
  public void testDeleteActivity() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    activity = activityManager.getActivity(activity.getId());

    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());

    activityManager.deleteActivity(activity);
  }

  /**
   * Test {@link ActivityManager#deleteActivity(String)}
   */
  public void testDeleteActivityWithId() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    activity = activityManager.getActivity(activity.getId());

    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());

    activityManager.deleteActivity(activity.getId());
  }

  /**
   * Test {@link ActivityManager#hideActivity(String)}
   */
  public void testHideActivity() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    activity = activityManager.getActivity(activity.getId());

    assertNotNull("activity must not be null", activity);

    RealtimeListAccess<ExoSocialActivity> activities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, activities.getSize());

    activityManager.hideActivity(activity.getId());

    activities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(0, activities.getSize());

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("Should be able to access activity even when hidden", activity);
  }

  /**
   * Test {@link ActivityManager#pinActivity(String, String)} Test
   * {@link ActivityManager#unpinActivity(String)}
   */
  public void testPinActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    Space space = createSpace("spaceTestPin", "john");
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    // when
    activityManager.pinActivity(activity.getId(), johnIdentity.getId());

    ActivityFilter activityFilter = new ActivityFilter();
    activityFilter.setPinned(true);
    activityFilter.setStreamType(ActivityStreamType.USER_STREAM);

    RealtimeListAccess<ExoSocialActivity> activitiesListAccess =
                                                               activityManager.getActivitiesByFilterWithListAccess(johnIdentity,
                                                                                                                   activityFilter);
    List<ExoSocialActivity> activities = activitiesListAccess.loadAsList(0, activitiesListAccess.getSize());
    // then
    assertEquals(1, activities.size());
    assertTrue(activities.get(0).isPinned());

    // when
    activityManager.unpinActivity(activity.getId());

    activitiesListAccess = activityManager.getActivitiesByFilterWithListAccess(johnIdentity, activityFilter);
    activities = activitiesListAccess.loadAsList(0, activitiesListAccess.getSize());
    // then
    assertEquals(0, activities.size());
  }

  /**
   * Test {@link ActivityManager#canPinActivity(ExoSocialActivity, Identity)}
   */
  public void testCanActivity() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    Space space = createSpace("spaceTestPin", "john");
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);
    // when
    boolean maryCanPinActivity = activityManager.canPinActivity(activity, maryIdentity);
    boolean demoCanPinActivity = activityManager.canPinActivity(activity, demoIdentity);

    // then
    assertFalse(maryCanPinActivity);
    assertFalse(demoCanPinActivity);

    // when
    String[] managers = new String[] { "mary" };
    String[] redactors = new String[] { "demo" };
    space.setRedactors(redactors);
    space.setManagers(managers);
    spaceService.updateSpace(space);
    maryCanPinActivity = activityManager.canPinActivity(activity, maryIdentity);
    demoCanPinActivity = activityManager.canPinActivity(activity, demoIdentity);

    // then
    assertTrue(maryCanPinActivity);
    assertTrue(demoCanPinActivity);
  }

  /**
   * {@link ActivityManager#deleteComment(ExoSocialActivity, ExoSocialActivity)}
   */
  public void testDeleteComment() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);

    ExoSocialActivity maryComment = new ExoSocialActivityImpl();
    maryComment.setTitle("mary comment");
    maryComment.setUserId(maryIdentity.getId());
    activityManager.saveComment(demoActivity, maryComment);

    activityManager.deleteComment(demoActivity, maryComment);

    assertEquals("activityManager.getComments(demoActivity).size() must return: 0",
                 0,
                 activityManager.getCommentsWithListAccess(demoActivity).getSize());
  }

  /**
   * {@link ActivityManager#getActivityStreamOwnerIdentity(String)}
   */
  public void testGetActivityStreamOwnerIdentity() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);

    Identity streamOwnerIdentity = activityManager.getActivityStreamOwnerIdentity(demoActivity.getId());
    assertNotNull(streamOwnerIdentity);
    assertEquals(demoIdentity.getId(), streamOwnerIdentity.getId());

    ExoSocialActivity maryComment = new ExoSocialActivityImpl();
    maryComment.setTitle("mary comment");
    maryComment.setUserId(maryIdentity.getId());
    activityManager.saveComment(demoActivity, maryComment);

    streamOwnerIdentity = activityManager.getActivityStreamOwnerIdentity(maryComment.getId());
    assertNotNull(streamOwnerIdentity);
    assertEquals(demoIdentity.getId(), streamOwnerIdentity.getId());
  }

  /**
   * Test {@link ActivityManager#saveLike(ExoSocialActivity, Identity)}
   */
  public void testSaveLike() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("&\"demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0,
                 demoActivity.getLikeIdentityIds().length);

    activityManager.saveLike(demoActivity, johnIdentity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 1", 1, demoActivity.getLikeIdentityIds().length);
    assertEquals("&amp;&#34;demo activity", demoActivity.getTitle());
  }

  /**
   * Test {@link ActivityManager#saveLike(ExoSocialActivity, Identity)} for case
   * not change the template param after liked.
   */
  public void testSaveLikeNotChangeTemplateParam() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0,
                 demoActivity.getLikeIdentityIds().length);

    activityManager.saveLike(demoActivity, johnIdentity);

    ExoSocialActivity likedActivity = activityManager.getActivity(demoActivity.getId());

    assertEquals(1, likedActivity.getLikeIdentityIds().length);
    assertEquals(templateParams.get("link"), likedActivity.getTemplateParams().get("link"));
  }

  /**
   * Test {@link ActivityManager#deleteLike(ExoSocialActivity, Identity)}
   */
  public void testDeleteLike() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds() must return: 0",
                 0,
                 demoActivity.getLikeIdentityIds().length);

    activityManager.saveLike(demoActivity, johnIdentity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 1", 1, demoActivity.getLikeIdentityIds().length);

    activityManager.deleteLike(demoActivity, johnIdentity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);

    activityManager.deleteLike(demoActivity, maryIdentity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);

    activityManager.deleteLike(demoActivity, rootIdentity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    assertEquals("demoActivity.getLikeIdentityIds().length must return: 0", 0, demoActivity.getLikeIdentityIds().length);
  }

  /**
   * Test {@link ActivityManager#deleteLike(ExoSocialActivity, Identity)} for
   * case not change the template param after liked.
   */
  public void testDeleteLikeNotChangeTemplateParam() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    tearDownActivityList.add(demoActivity);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    activityManager.saveLike(demoActivity, johnIdentity);
    ExoSocialActivity likedActivity = activityManager.getActivity(demoActivity.getId());

    assertEquals(1, likedActivity.getLikeIdentityIds().length);

    demoActivity = activityManager.getActivity(demoActivity.getId());
    activityManager.deleteLike(demoActivity, johnIdentity);
    ExoSocialActivity deleteLikeActivity = activityManager.getActivity(demoActivity.getId());

    assertEquals(0, deleteLikeActivity.getLikeIdentityIds().length);
    assertEquals(templateParams.get("link"), deleteLikeActivity.getTemplateParams().get("link"));
  }

  /**
   * Test {@link ActivityManager#getCommentsWithListAccess(ExoSocialActivity)}
   */
  public void testGetCommentWithHtmlContent() {
    String htmlString = "<span><strong>foo</strong>bar<script>zed</script></span>";
    String htmlRemovedString = "<strong>foo</strong>bar";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("blah blah");
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(htmlString);
    comment.setUserId(rootIdentity.getId());
    comment.setBody(htmlString);
    activityManager.saveComment(activity, comment);
    assertNotNull("comment.getId() must not be null", comment.getId());

    List<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 1);
    assertEquals(1, comments.size());
    assertEquals(htmlRemovedString, comments.get(0).getBody());
    assertEquals(htmlRemovedString, comments.get(0).getTitle());
    tearDownActivityList.add(activity);
  }

  public void testGetComments() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    ;
    activity.setTitle("blah blah");
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < 10; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      ;
      comment.setTitle("comment blah blah");
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment);
      assertNotNull("comment.getId() must not be null", comment.getId());

      comments.add(comment);
    }

    ExoSocialActivity assertActivity = activityManager.getActivity(activity.getId());
    String[] commentIds = assertActivity.getReplyToId();
    for (int i = 1; i < commentIds.length; i++) {
      assertEquals(comments.get(i - 1).getId(), commentIds[i - 1]);
    }
    tearDownActivityList.add(activity);
  }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteComment(String, String)}
   */
  public void testDeleteCommentWithId() {
    final String title = "Activity Title";
    {
      ExoSocialActivity activity1 = new ExoSocialActivityImpl();
      activity1.setUserId(demoIdentity.getId());
      activity1.setTitle(title);
      activityManager.saveActivityNoReturn(demoIdentity, activity1);

      final int numberOfComments = 10;
      final String commentTitle = "Activity Comment";
      for (int i = 0; i < numberOfComments; i++) {
        ExoSocialActivity comment = new ExoSocialActivityImpl();
        ;
        comment.setUserId(demoIdentity.getId());
        comment.setTitle(commentTitle + i);
        activityManager.saveComment(activity1, comment);
      }

      RealtimeListAccess<ExoSocialActivity> commentsWithListAccess = activityManager.getCommentsWithListAccess(activity1);
      List<ExoSocialActivity> storedCommentList = commentsWithListAccess.loadAsList(0, commentsWithListAccess.getSize());

      assertEquals("storedCommentList.size() must return: " + numberOfComments, numberOfComments, storedCommentList.size());

      // delete random 2 comments
      int index1 = new Random().nextInt(numberOfComments - 1);
      int index2 = index1;
      while (index2 == index1) {
        index2 = new Random().nextInt(numberOfComments - 1);
      }

      ExoSocialActivity tobeDeletedComment1 = storedCommentList.get(index1);
      ExoSocialActivity tobeDeletedComment2 = storedCommentList.get(index2);

      activityManager.deleteComment(activity1.getId(), tobeDeletedComment1.getId());
      activityManager.deleteComment(activity1.getId(), tobeDeletedComment2.getId());

      List<ExoSocialActivity> afterDeletedCommentList = commentsWithListAccess.loadAsList(0, commentsWithListAccess.getSize());

      assertEquals("afterDeletedCommentList.size() must return: " + (numberOfComments - 2),
                   numberOfComments - 2,
                   afterDeletedCommentList.size());

      tearDownActivityList.add(activity1);

    }
  }

  public void testDisableActivityType() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);

    // Disabled activity Type
    activity.setType("cs-calendar:spaces");

    activityManager.saveActivityNoReturn(johnIdentity, activity);
    assertNull(activity.getId());

    // Disabled Custom activity Type
    activity.setType("MY_ACTIVITY");

    activityManager.saveActivityNoReturn(johnIdentity, activity);
    assertNull(activity.getId());

    // enabled activity Types
    activity.setType("DEFAULT_ACTIVITY");
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    assertNotNull(activity.getId());
    activity = activityManager.getActivity(activity.getId());
    assertNotNull(activity);
    tearDownActivityList.add(activity);
  }

  public void testCommentOnDisabledActivity() {
    String activityTitle = "disabled activity";
    String commentTitle = "Comment title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);

    // Disabled activity Type
    activity.setType("cs-calendar:spaces");
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    assertNull(activity.getId());

    // demo comments on john's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(demoIdentity.getId());
    // add comment on disabled activity
    activityManager.saveComment(activity, comment);
    // OK
    assertNull(comment.getId());
  }

  public void testCommentWithDisabledCommentActivityType() {
    String activityTitle = "disabled activity";
    String commentTitle = "Comment title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activity.setType("EnableType");
    activityManager.saveActivityNoReturn(johnIdentity, activity);
    assertNotNull(activity.getId());

    // demo comments on john's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    // Disabled activity Type
    comment.setType("cs-calendar:spaces");
    comment.setUserId(demoIdentity.getId());
    // add comment on disabled activity
    activityManager.saveComment(activity, comment);
    // OK
    assertNull(comment.getId());
  }

  /**
   * Populates activity.
   * 
   * @param user
   * @param number
   */
  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      ;
      activity.setTitle("title " + i);
      activity.setUserId(user.getId());
      try {
        activityManager.saveActivityNoReturn(user, activity);
        tearDownActivityList.add(activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }

  /**
   * Creates activity.
   * 
   * @return ExoSocialActivity
   */
  private ExoSocialActivity createActivity() {
    String activityTitle = "activity title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);
    return activityManager.getActivity(activity.getId());
  }

  /**
   * Creates comment.
   * 
   * @param activity
   * @param type
   * @param titleId
   * @return ExoSocialActivity
   */
  private ExoSocialActivity createComment(ExoSocialActivity activity, String type, String titleId) {
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setUserId(rootIdentity.getId());
    comment.setTemplateParams(new HashMap<String, String>());
    comment.setTitle("comment title");
    comment.setType(type);
    comment.setTitleId(titleId);
    activityManager.saveComment(activity, comment);
    return activityManager.getActivity(comment.getId());
  }

  @SuppressWarnings("deprecation")
  private Space createSpace(String spaceName, String creator, String ...members) throws Exception {
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
