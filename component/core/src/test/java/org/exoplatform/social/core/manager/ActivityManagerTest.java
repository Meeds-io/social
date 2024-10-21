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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityTypePlugin;
import org.exoplatform.social.core.activity.ActivityFilter;
import org.exoplatform.social.core.activity.ActivityStreamType;
import org.exoplatform.social.core.activity.ActivitySystemTypePlugin;
import org.exoplatform.social.core.activity.model.ActivityShareAction;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedIdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * 
 * @author hoat_le
 */
public class ActivityManagerTest extends AbstractCoreTest {

  private static final Log        LOG               = ExoLogger.getLogger(ActivityManagerTest.class);

  private SpaceStorage            spaceStorage;

  private ActivityManager         activityManager;

  private RelationshipManager     relationshipManager;

  private ActivityStorage         activityStorage;

  private UserACL                 userACL;

  private ProfileSearchConnector  mockProfileSearch = Mockito.mock(ProfileSearchConnector.class);

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private Identity                demoIdentity;

  private Identity                ghostIdentity;

  private Identity                raulIdentity;

  private Identity                jamesIdentity;

  private Identity                paulIdentity;

  private Identity                jameIdentity;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    activityStorage = getContainer().getComponentInstanceOfType(ActivityStorage.class);
    userACL = getContainer().getComponentInstanceOfType(UserACL.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    spaceStorage = getService(SpaceStorage.class);
    CachedIdentityStorage identityStorage = getService(CachedIdentityStorage.class);
    ((RDBMSIdentityStorageImpl) identityStorage.getStorage()).setProfileSearchConnector(mockProfileSearch);

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost");
    raulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul");
    jamesIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "james");
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul");

    rootIdentity = createIdentity("root");
    johnIdentity = createIdentity("john");
    maryIdentity = createIdentity("mary");
    demoIdentity = createIdentity("demo");
    ghostIdentity = createIdentity("ghost");
    raulIdentity = createIdentity("raul");
    jameIdentity = createIdentity("jame");
    paulIdentity = createIdentity("paul");

    PropertyManager.setProperty("exo.activity-type.cs-calendar:spaces.enabled", "false");
    PropertyManager.setProperty("exo.activity-type.MY_ACTIVITY.enabled", "false");
    ((ActivityManagerImpl) activityManager).initActivityTypes();
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
  public void testSaveActivitiesNoReturnNotStreamOwner() {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);

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

    Space space = createSpace("spaceTest", "demo", "james");

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

    space.setMembers(new String[] { "demo", "james", "mary" });
    space.setRedactors(new String[] { "james" });
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

    Space targetSpace = createSpace("TargetSpace", "james", "demo");
    String shareMessage = "share Message";
    String type = "sharedActivityTest"; // No matter, will be removed from API
                                        // anyway

    org.exoplatform.services.security.Identity johnSecurityIdentity = new org.exoplatform.services.security.Identity("john");
    org.exoplatform.services.security.Identity demoSecurityIdentity = new org.exoplatform.services.security.Identity("demo");
    org.exoplatform.services.security.Identity jamesSecurityIdentity = new org.exoplatform.services.security.Identity("james");
    org.exoplatform.services.security.Identity marySecurityIdentity = new org.exoplatform.services.security.Identity("mary");

    ExoSocialActivity shareActivityTemplate = new ExoSocialActivityImpl();
    shareActivityTemplate.setTitle(shareMessage);
    shareActivityTemplate.setType(type);
    try {
      activityManager.shareActivity(shareActivityTemplate,
                                    originalActivity.getId(),
                                    Arrays.asList(targetSpace.getPrettyName()),
                                    marySecurityIdentity);
      fail("Mary is not member of target space");
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

    spaceService.updateSpace(targetSpace);
    spaceService.addRedactor(targetSpace, "james");

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
  public void testGetParentActivities() {
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
  public void testUpdateActivities() {
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
  public void testDeleteActivities() {
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
  public void testDeleteActivitiesWithId() {
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
    Space space = createSpace("spaceTestPin", "john", "demo", "james");
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);
    // when
    boolean maryCanPinActivity = activityManager.canPinActivity(activity, maryIdentity);
    boolean demoCanPinActivity = activityManager.canPinActivity(activity, demoIdentity);
    boolean jamesCanPinActivity = activityManager.canPinActivity(activity, jamesIdentity);

    // then
    assertFalse(maryCanPinActivity);
    assertFalse(demoCanPinActivity);
    assertFalse(jamesCanPinActivity);

    // when
    String[] managers = new String[] { "mary" };
    String[] redactors = new String[] { "demo" };
    String[] publishers = new String[] { "james" };
    space.setRedactors(redactors);
    space.setManagers(managers);
    space.setPublishers(publishers);
    spaceService.updateSpace(space);
    assertTrue(activityManager.canPinActivity(activity, maryIdentity));
    assertFalse(activityManager.canPinActivity(activity, demoIdentity));
    assertTrue(activityManager.canPinActivity(activity, jamesIdentity));
  }

  /**
   * {@link ActivityManager#deleteComment(ExoSocialActivity, ExoSocialActivity)}
   */
  public void testDeleteComment() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
  public void testSaveLikes() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("&\"demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
  public void testSaveLikesNotChangeTemplateParam() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
  public void testDeleteLikes() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
  public void testDeleteLikesNotChangeTemplateParam() {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
  }

  public void testGetCommentList() {
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

  public void testActivityEditable() {
    ActivityStorage storage = Mockito.mock(ActivityStorage.class);
    IdentityManager identityManager = Mockito.mock(IdentityManager.class);
    UserACL acl = Mockito.mock(UserACL.class);
    Mockito.when(acl.getAdminGroups()).thenReturn("/platform/administrators");

    // prepare activity
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(activity.isComment()).thenReturn(false);
    Mockito.when(activity.getPosterId()).thenReturn("1");
    // prepare comment
    ExoSocialActivity comment = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(comment.isComment()).thenReturn(true);
    Mockito.when(comment.getPosterId()).thenReturn("1");
    // prepare viewer
    org.exoplatform.services.security.Identity owner = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(owner.getUserId()).thenReturn("demo");
    Mockito.when(identityManager.getOrCreateUserIdentity("demo"))
           .thenReturn(new Identity("1"));
    org.exoplatform.services.security.Identity admin = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(admin.getUserId()).thenReturn("john");
    Mockito.when(identityManager.getOrCreateUserIdentity("john"))
           .thenReturn(new Identity("2"));
    Mockito.when(admin.getGroups()).thenReturn(new HashSet<>(Arrays.asList("/platform/administrators")));
    org.exoplatform.services.security.Identity mary = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(mary.getUserId()).thenReturn("mary");
    Mockito.when(identityManager.getOrCreateUserIdentity("mary"))
           .thenReturn(new Identity("3"));

    // no configuration
    // by default: edit activity/comment are all enabled
    ActivityManager manager = new ActivityManagerImpl(storage, identityManager, spaceService, relationshipManager, acl, null);
    // owner
    assertTrue(manager.isActivityEditable(activity, owner));
    assertTrue(manager.isActivityEditable(comment, owner));
    // do not allow edit automatic comment
    Mockito.when(comment.getType()).thenReturn("TestActivityType");

    InitParams params = new InitParams();
    ValuesParam param = new ValuesParam();
    param.setName(ActivitySystemTypePlugin.SYSTEM_TYPES_PARAM);
    param.setValues(Collections.singletonList("TestActivityType"));
    params.addParameter(param);
    ActivitySystemTypePlugin plugin = new ActivitySystemTypePlugin(params);
    manager.addSystemActivityDefinition(plugin);

    assertFalse(manager.isActivityEditable(comment, owner));

    // manager is not able to edit other activity
    assertFalse(manager.isActivityEditable(activity, admin));
    // not manager
    assertFalse(manager.isActivityEditable(activity, mary));

    // InitParams configuration
    params = Mockito.mock(InitParams.class);
    Mockito.when(params.containsKey(ActivityManagerImpl.ENABLE_MANAGER_EDIT_COMMENT)).thenReturn(true);
    Mockito.when(params.containsKey(ActivityManagerImpl.ENABLE_EDIT_COMMENT)).thenReturn(true);
    ValueParam falseVal = new ValueParam();
    falseVal.setValue("false");
    // not enable edit comment
    Mockito.when(params.getValueParam(ActivityManagerImpl.ENABLE_MANAGER_EDIT_COMMENT)).thenReturn(falseVal);
    Mockito.when(params.getValueParam(ActivityManagerImpl.ENABLE_EDIT_COMMENT)).thenReturn(falseVal);
    manager = new ActivityManagerImpl(storage, identityManager, spaceService, relationshipManager, acl, params);
    //
    assertFalse(manager.isActivityEditable(comment, admin));
    assertFalse(manager.isActivityEditable(comment, owner));
    assertTrue(manager.isActivityEditable(activity, owner));
    assertFalse(manager.isActivityEditable(activity, admin));

    // not enable edit activity
    Mockito.when(params.containsKey(ActivityManagerImpl.ENABLE_MANAGER_EDIT_ACTIVITY)).thenReturn(true);
    Mockito.when(params.containsKey(ActivityManagerImpl.ENABLE_EDIT_ACTIVITY)).thenReturn(true);
    Mockito.when(params.getValueParam(ActivityManagerImpl.ENABLE_MANAGER_EDIT_ACTIVITY)).thenReturn(falseVal);
    Mockito.when(params.getValueParam(ActivityManagerImpl.ENABLE_EDIT_ACTIVITY)).thenReturn(falseVal);
    manager = new ActivityManagerImpl(storage, identityManager, spaceService, relationshipManager, acl, params);
    //
    assertFalse(manager.isActivityEditable(activity, owner));
    assertFalse(manager.isActivityEditable(activity, admin));
  }

  public void testActivityViewable() {
    ActivityStorage storage = Mockito.mock(ActivityStorage.class);
    IdentityManager identityManager = Mockito.mock(IdentityManager.class);
    RelationshipManager relationshipManager = Mockito.mock(RelationshipManager.class);
    UserACL acl = Mockito.mock(UserACL.class);
    Mockito.when(acl.getAdminGroups()).thenReturn("/platform/administrators");

    // prepare activity
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(activity.isComment()).thenReturn(false);
    Mockito.when(activity.getPosterId()).thenReturn("1");
    // prepare comment
    ExoSocialActivity comment = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(comment.isComment()).thenReturn(true);
    Mockito.when(comment.getPosterId()).thenReturn("1");
    Mockito.when(identityManager.getIdentity("1")).thenReturn(rootIdentity);

    // prepare viewer
    org.exoplatform.services.security.Identity owner = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(owner.getUserId()).thenReturn("demo");
    Mockito.when(identityManager.getOrCreateUserIdentity("demo"))
           .thenReturn(new Identity("1"));
    org.exoplatform.services.security.Identity admin = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(admin.getUserId()).thenReturn("john");
    Mockito.when(identityManager.getOrCreateUserIdentity("john")).thenReturn(johnIdentity);
    Mockito.when(admin.getGroups()).thenReturn(Collections.singleton("/platform/administrators"));
    Mockito.when(admin.getMemberships()).thenReturn(Collections.singleton(new MembershipEntry("/platform/administrators")));
    Mockito.when(admin.isMemberOf(acl.getAdminGroups())).thenReturn(true);
    org.exoplatform.services.security.Identity mary = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(mary.getUserId()).thenReturn("mary");
    Mockito.when(identityManager.getOrCreateUserIdentity("mary")).thenReturn(maryIdentity);

    // no configuration
    // by default: edit activity/comment are all enabled
    ActivityManager manager = new ActivityManagerImpl(storage, identityManager, spaceService, relationshipManager, acl, null);
    // owner
    assertTrue(manager.isActivityViewable(activity, owner));
    assertTrue(manager.isActivityViewable(comment, owner));
    Mockito.when(comment.getType()).thenReturn("TestActivityType");
    assertTrue(manager.isActivityViewable(comment, owner));
    assertTrue(manager.isActivityViewable(activity, admin));
    assertFalse(manager.isActivityViewable(activity, mary));

    Mockito.when(relationshipManager.getStatus(rootIdentity, maryIdentity)).thenReturn(Type.CONFIRMED);

    assertTrue(manager.isActivityViewable(activity, mary));
  }

  public void testActivityTypePlugin() {
    ActivityStorage storage = Mockito.mock(ActivityStorage.class);
    IdentityManager identityManager = Mockito.mock(IdentityManager.class);
    UserACL acl = Mockito.mock(UserACL.class);
    Mockito.when(acl.getAdminGroups()).thenReturn("/platform/administrators");

    String specificActivityType = "SpecificActivityType";

    // prepare activity
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(activity.isComment()).thenReturn(false);
    Mockito.when(activity.getType()).thenReturn(specificActivityType);
    Mockito.when(activity.getPosterId()).thenReturn("1");
    Mockito.when(activity.getTitle()).thenReturn("Activity title");
    // prepare viewer
    org.exoplatform.services.security.Identity owner = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(owner.getUserId()).thenReturn("demo");
    Mockito.when(identityManager.getOrCreateUserIdentity("demo"))
           .thenReturn(new Identity("1"));

    // no configuration
    // by default: edit activity/comment are all enabled
    ActivityManager manager = new ActivityManagerImpl(storage, identityManager, spaceService, relationshipManager, acl, null);
    // owner
    assertTrue(manager.isActivityEditable(activity, owner));
    assertTrue(manager.isActivityViewable(activity, owner));
    assertTrue(manager.isActivityDeletable(activity, owner));
    assertEquals("Activity title", manager.getActivityTitle(activity));

    AtomicBoolean viewwable = new AtomicBoolean(false);
    AtomicBoolean editable = new AtomicBoolean(false);
    AtomicBoolean deletable = new AtomicBoolean(false);
    manager.addActivityTypePlugin(new ActivityTypePlugin(null) {
      @Override
      public String getActivityType() {
        return specificActivityType;
      }

      @Override
      public boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity userAclIdentity) {
        return editable.get();
      }

      @Override
      public boolean isActivityDeletable(ExoSocialActivity activity, org.exoplatform.services.security.Identity userAclIdentity) {
        return deletable.get();
      }

      @Override
      public boolean isActivityViewable(ExoSocialActivity activity, org.exoplatform.services.security.Identity userAclIdentity) {
        return viewwable.get();
      }

      @Override
      public String getActivityTitle(ExoSocialActivity activity) {
        return "Activity Plugin title";
      }
    });

    assertFalse(manager.isActivityEditable(activity, owner));
    editable.set(true);
    assertTrue(manager.isActivityEditable(activity, owner));

    assertFalse(manager.isActivityViewable(activity, owner));
    viewwable.set(true);
    assertTrue(manager.isActivityViewable(activity, owner));

    assertFalse(manager.isActivityDeletable(activity, owner));
    deletable.set(true);
    assertTrue(manager.isActivityDeletable(activity, owner));
    assertEquals("Activity Plugin title", manager.getActivityTitle(activity));
  }

  public void testActivityNotificationEnabling() {
    ActivityStorage storage = Mockito.mock(ActivityStorage.class);
    IdentityManager identityManager = Mockito.mock(IdentityManager.class);
    RelationshipManager relationshipManager = Mockito.mock(RelationshipManager.class);
    ActivityManager activityManager = new ActivityManagerImpl(storage,
                                                              identityManager,
                                                              spaceService,
                                                              relationshipManager,
                                                              null,
                                                              null);
    String activityType = "TestActivityType";
    String activityType2 = "TestActivityType2";
    String activityType3 = "TestActivityType3";

    addActivityTypePlugin(activityManager, activityType, false);
    addActivityTypePlugin(activityManager, activityType2, true);

    assertFalse(activityManager.isNotificationEnabled(null));
    assertFalse(activityManager.isNotificationEnabled(null, null));

    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    assertTrue(activityManager.isNotificationEnabled(activity));
    assertTrue(activityManager.isNotificationEnabled(activity, null));
    when(activity.getType()).thenReturn(activityType);
    assertFalse(activityManager.isNotificationEnabled(activity, null));
    when(activity.getType()).thenReturn(activityType2);
    assertTrue(activityManager.isNotificationEnabled(activity, null));
    assertFalse(activityManager.isNotificationEnabled(activity, "root3"));
    assertTrue(activityManager.isNotificationEnabled(activity, "root2"));
    when(activity.getType()).thenReturn(activityType3);
    assertTrue(activityManager.isNotificationEnabled(activity, null));
    when(activity.isHidden()).thenReturn(true);
    assertFalse(activityManager.isNotificationEnabled(activity, null));
  }

  public void testActivityDeletable() {
    ActivityStorage storage = Mockito.mock(ActivityStorage.class);
    IdentityManager identityManager = Mockito.mock(IdentityManager.class);
    SpaceService spaceService = Mockito.mock(SpaceService.class);
    UserACL acl = Mockito.mock(UserACL.class);

    // no configuration
    // by default: edit activity/comment are all enabled
    ActivityManagerImpl activityManager = new ActivityManagerImpl(storage,
                                                                  identityManager,
                                                                  spaceService,
                                                                  relationshipManager,
                                                                  acl,
                                                                  null);
    Mockito.when(acl.getAdminGroups()).thenReturn("/platform/administrators");

    Mockito.when(spaceService.isSuperManager(Mockito.eq("john"))).thenReturn(true);
    Mockito.when(spaceService.isSuperManager(Mockito.eq("mary"))).thenReturn(false);
    Mockito.when(spaceService.isSuperManager(Mockito.eq("james"))).thenReturn(false);

    // prepare activity
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(activity.isComment()).thenReturn(false);
    Mockito.when(activity.getPosterId()).thenReturn("1");
    ActivityStreamImpl activityStream = new ActivityStreamImpl();
    activityStream.setType(ActivityStream.Type.USER);
    activityStream.setPrettyId("demo");
    Mockito.when(activity.getActivityStream()).thenReturn(activityStream);

    // prepare comment
    ExoSocialActivity comment = Mockito.mock(ExoSocialActivity.class);
    Mockito.when(comment.isComment()).thenReturn(true);
    Mockito.when(comment.getPosterId()).thenReturn("1");

    // prepare viewer
    org.exoplatform.services.security.Identity owner = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(owner.getUserId()).thenReturn("demo");
    Mockito.when(identityManager.getOrCreateUserIdentity("demo"))
           .thenReturn(new Identity("1"));
    org.exoplatform.services.security.Identity admin = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(admin.getUserId()).thenReturn("john");
    Mockito.when(identityManager.getOrCreateUserIdentity("john"))
           .thenReturn(new Identity("2"));
    Mockito.when(admin.getGroups()).thenReturn(new HashSet<>(Arrays.asList("/platform/administrators")));
    org.exoplatform.services.security.Identity mary = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(mary.getUserId()).thenReturn("mary");
    Mockito.when(identityManager.getOrCreateUserIdentity("mary"))
           .thenReturn(new Identity("3"));
    org.exoplatform.services.security.Identity james = Mockito.mock(org.exoplatform.services.security.Identity.class);
    Mockito.when(james.getUserId()).thenReturn("james");
    Mockito.when(identityManager.getOrCreateUserIdentity("james"))
           .thenReturn(new Identity("4"));

    // owner
    assertTrue(activityManager.isActivityDeletable(activity, owner));
    assertTrue(activityManager.isActivityDeletable(comment, owner));

    // manager is able to delete other user's activity
    assertTrue(activityManager.isActivityDeletable(activity, admin));

    // member is not able to delete other user's activity
    assertFalse(activityManager.isActivityDeletable(activity, mary));
    assertFalse(activityManager.isActivityDeletable(comment, mary));

    // Test on space activity
    activityStream.setType(ActivityStream.Type.SPACE);
    activityStream.setPrettyId("demo_space");

    Space space = new Space();
    Mockito.when(spaceService.getSpaceByPrettyName("demo_space")).thenReturn(space);

    Mockito.when(spaceService.canManageSpace(space, "john")).thenReturn(true);
    Mockito.when(spaceService.canManageSpace(space, "mary")).thenReturn(true);
    Mockito.when(spaceService.canManageSpace(space, "james")).thenReturn(false);

    assertTrue(activityManager.isActivityDeletable(activity, owner));
    assertTrue(activityManager.isActivityDeletable(activity, mary));
    assertTrue(activityManager.isActivityDeletable(activity, admin));
    assertFalse(activityManager.isActivityDeletable(activity, james));

    Map<String, String> templateParams = new HashMap<>();
    when(activity.getTemplateParams()).thenReturn(templateParams);
    templateParams.put(ActivityManagerImpl.REMOVABLE, "false");
    assertFalse(activityManager.isActivityDeletable(activity, owner));
    assertTrue(activityManager.isActivityDeletable(activity, admin));
    assertTrue(activityManager.isActivityDeletable(activity, mary));
    assertFalse(activityManager.isActivityDeletable(activity, james));

    templateParams.put(ActivityManagerImpl.REMOVABLE, "true");
    assertTrue(activityManager.isActivityDeletable(activity, owner));
    assertTrue(activityManager.isActivityDeletable(activity, admin));
    assertTrue(activityManager.isActivityDeletable(activity, mary));
    assertFalse(activityManager.isActivityDeletable(activity, james));
  }

  public void testSaveActivitiesNoReturn() throws Exception {
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

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + activityTitle, activityTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
    Map<String, String> gotTemplateParams = activity.getTemplateParams();
    assertEquals("value 1", gotTemplateParams.get("key1"));
    assertEquals("value 2", gotTemplateParams.get("key2"));
    assertEquals("value 3", gotTemplateParams.get("key3"));

    //
    assertTrue(activity.isLocked());
    assertFalse(activity.isHidden());
  }

  /**
   * Test {@link ActivityManager#saveActivity(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveActivityNoReturnNotStreamOwner() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(activity);

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
    ExoSocialActivity demoActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertNotNull("demoActivity must be false", demoActivity);
    assertNull(activityManager.getParentActivity(demoActivity));

    // comment
    ExoSocialActivityImpl comment = new ExoSocialActivityImpl();
    comment.setTitle("comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(demoActivity, comment);
    ExoSocialActivity gotComment = activityManager.getCommentsWithListAccess(demoActivity).load(0, 1)[0];
    assertNotNull("gotComment must not be null", gotComment);
    //
    ExoSocialActivity parentActivity = activityManager.getParentActivity(gotComment);
    assertNotNull(parentActivity);
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

  public void testGetActivitiiesByUser() throws ActivityStorageException {
    String activityTitle = "title";
    String userId = rootIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    //
    activity = activityManager.getActivity(String.valueOf(activity.getId()));

    assertNotNull(activity);
    assertEquals(activityTitle, activity.getTitle());
    assertEquals(userId, activity.getUserId());

    RealtimeListAccess<ExoSocialActivity> activities = activityManager.getActivitiesWithListAccess(rootIdentity);

    assertEquals(1, activities.load(0, 10).length);
    LOG.info("Create 100 activities...");
    //
    for (int i = 0; i < 100; i++) {
      activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + " " + i);
      activity.setUserId(userId);
      //
      activityManager.saveActivityNoReturn(rootIdentity, activity);
    }
    activities = activityManager.getActivitiesWithListAccess(rootIdentity);
    //
    LOG.info("Loadding 20 activities...");
    assertEquals(20, activities.load(0, 20).length);
    //
    List<ExoSocialActivity> exoActivities = Arrays.asList(activities.load(0, activities.getSize()));
    LOG.info("Loadding 101 activities...");
    assertEquals(101, exoActivities.size());
  }

  /**
   * Test {@link ActivityManager#updateActivity(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testUpdateActivity() throws Exception {
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

    String newTitle = "new activity title";
    activity.setTitle(newTitle);
    activityManager.updateActivity(activity);

    activity = activityManager.getActivity(activity.getId());
    assertNotNull("activity must not be null", activity);
    assertEquals("activity.getTitle() must return: " + newTitle, newTitle, activity.getTitle());
    assertEquals("activity.getUserId() must return: " + userId, userId, activity.getUserId());
  }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteActivity(org.exoplatform.social.core.activity.model.ExoSocialActivity)}
   * 
   * @throws Exception
   */
  public void testDeleteActivity() throws Exception {
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

  public void testIsActivityExists() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    assertTrue(activityManager.isActivityExists(activity.getId()));

    activity = activityManager.getActivity(activity.getId());
    activityManager.deleteActivity(activity);

    assertFalse(activityManager.isActivityExists(activity.getId()));
  }

  /**
   * Test {@link ActivityManager#deleteActivity(String)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteActivityWithId() throws Exception {
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
   * Test
   * {@link ActivityManager#saveComment(ExoSocialActivity, ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testSaveSubComment() throws Exception {
    String activityTitle = "activity title";
    String userId = johnIdentity.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(userId);
    activityManager.saveActivityNoReturn(johnIdentity, activity);

    String commentTitle = "Comment title";

    // demo comments on john's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(commentTitle);
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);
    assertEquals(activity.getId(), comment.getParentId());

    assertTrue(StringUtils.isNotBlank(comment.getId()));

    ExoSocialActivity subComment = new ExoSocialActivityImpl();
    subComment.setTitle(commentTitle);
    subComment.setUserId(maryIdentity.getId());
    subComment.setParentCommentId(comment.getId());
    activityManager.saveComment(activity, subComment);
    assertEquals(activity.getId(), subComment.getParentId());
    assertEquals(comment.getId(), subComment.getParentCommentId());
    assertNotNull(subComment.getId());

    List<ExoSocialActivity> subComments = activityManager.getSubComments(comment);
    assertEquals(1, subComments.size());
    assertEquals(subComment.getId(), subComments.get(0).getId());

    ExoSocialActivity gotParentActivity = activityManager.getParentActivity(subComment);
    assertNotNull(gotParentActivity);
    assertEquals(activity.getId(), gotParentActivity.getId());
    String[] replyToIds = gotParentActivity.getReplyToId();
    assertEquals(2, replyToIds.length);
    for (String replyToId : replyToIds) {
      assertTrue(replyToId.equals(comment.getId()) || replyToId.equals(subComment.getId()));
    }
  }

  /**
   * Test {@link ActivityManager#getCommentsWithListAccess(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetCommentsWithListAccess() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    int total = 10;

    for (int i = 0; i < total; i++) {
      ExoSocialActivity maryComment = new ExoSocialActivityImpl();
      maryComment.setUserId(maryIdentity.getId());
      maryComment.setTitle("mary comment");
      activityManager.saveComment(demoActivity, maryComment);
    }

    RealtimeListAccess<ExoSocialActivity> maryComments = activityManager.getCommentsWithListAccess(demoActivity);
    assertNotNull("maryComments must not be null", maryComments);
    assertEquals("maryComments.getSize() must return: 10", total, maryComments.getSize());

  }

  /**
   * Test {@link ActivityManager#getCommentsWithListAccess(ExoSocialActivity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetCommentsAndSubCommentsWithListAccess() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    int total = 10;
    int totalWithSubComments = total + total * total;

    for (int i = 0; i < total; i++) {
      ExoSocialActivity maryComment = new ExoSocialActivityImpl();
      maryComment.setUserId(maryIdentity.getId());
      maryComment.setTitle("mary comment");
      activityManager.saveComment(demoActivity, maryComment);
      for (int j = 0; j < total; j++) {
        ExoSocialActivity johnComment = new ExoSocialActivityImpl();
        johnComment.setUserId(johnIdentity.getId());
        johnComment.setTitle("john comment" + i + j);
        johnComment.setParentCommentId(maryComment.getId());
        activityManager.saveComment(demoActivity, johnComment);
      }
    }

    RealtimeListAccess<ExoSocialActivity> maryComments = activityManager.getCommentsWithListAccess(demoActivity);
    assertNotNull("maryComments must not be null", maryComments);
    assertEquals("maryComments.getSize() must return: 10", total, maryComments.getSize());

    RealtimeListAccess<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(demoActivity, true);
    assertNotNull("comments must not be null", comments);
    assertEquals("comments.getSize() must return: 10", total, comments.getSize());

    ExoSocialActivity[] commentsArray = comments.load(0, total);
    assertEquals("commentsArray.length must return: 110", totalWithSubComments, commentsArray.length);
    int index = 0;
    for (int i = 0; i < total; i++) {
      ExoSocialActivity maryComment = commentsArray[index++];
      assertEquals("Title of comment should be 'mary comment', iteration = " + i, "mary comment", maryComment.getTitle());
      for (int j = 0; j < total; j++) {
        ExoSocialActivity johnComment = commentsArray[index++];
        assertEquals("Title of comment should be 'john comment " + i + j + "'", "john comment" + i + j, johnComment.getTitle());
      }
    }
  }

  /**
   * Test
   * {@link ActivityManager#getCommentsWithListAccess(ExoSocialActivity, boolean, boolean)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetCommentsAndSubCommentsWithListAccessDescending() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    int total = 10;
    int totalWithSubComments = total + total * total;

    for (int i = 0; i < total; i++) {
      ExoSocialActivity maryComment = new ExoSocialActivityImpl();
      maryComment.setUserId(maryIdentity.getId());
      maryComment.setTitle("mary comment");
      activityManager.saveComment(demoActivity, maryComment);
      for (int j = 0; j < total; j++) {
        ExoSocialActivity johnComment = new ExoSocialActivityImpl();
        johnComment.setUserId(johnIdentity.getId());
        johnComment.setTitle("john comment" + i + j);
        johnComment.setParentCommentId(maryComment.getId());
        activityManager.saveComment(demoActivity, johnComment);
      }
    }

    RealtimeListAccess<ExoSocialActivity> maryComments = activityManager.getCommentsWithListAccess(demoActivity);
    assertNotNull("maryComments must not be null", maryComments);
    assertEquals("maryComments.getSize() must return: 10", total, maryComments.getSize());

    RealtimeListAccess<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(demoActivity, true, true);
    assertNotNull("comments must not be null", comments);
    assertEquals("comments.getSize() must return: 10", total, comments.getSize());

    ExoSocialActivity[] commentsArray = comments.load(0, total);
    assertEquals("commentsArray.length must return: 110", totalWithSubComments, commentsArray.length);
    int index = 0;
    for (int i = 0; i < total; i++) {
      ExoSocialActivity maryComment = commentsArray[index++];
      assertEquals("Title of comment should be 'mary comment', iteration = " + (total - i - 1),
                   "mary comment",
                   maryComment.getTitle());
      for (int j = 0; j < total; j++) {
        ExoSocialActivity johnComment = commentsArray[index++];
        assertEquals("Title of comment should be 'john comment " + (total - i - 1) + j + "'",
                     "john comment" + (total - i - 1) + j,
                     johnComment.getTitle());
      }
    }
  }

  /**
   * Test {@link ActivityManager#saveLike(ExoSocialActivity, Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3s
   */
  public void testSaveLike() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("&\"demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
   * 
   * @throws Exception
   * @since 4.0.5
   */
  public void testSaveLikeNotChangeTemplateParam() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testDeleteLike() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
   * 
   * @throws Exception
   * @since 4.0.5
   */
  public void testDeleteLikeNotChangeTemplateParam() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("title");
    demoActivity.setUserId(demoActivity.getId());

    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("link", "http://exoplatform.com?test=<script>");
    demoActivity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

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
   * Test {@link ActivityManager#getActivitiesWithListAccess(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesWithListAccess() throws Exception {
    int total = 10;
    for (int i = 0; i < total; i++) {
      ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
      demoActivity.setTitle("demo activity");
      demoActivity.setUserId(demoActivity.getId());
      activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    }

    RealtimeListAccess<ExoSocialActivity> demoListAccess = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertNotNull("demoListAccess must not be null", demoListAccess);
    assertEquals("demoListAccess.getSize() must return: 10", 10, demoListAccess.getSize());
  }

  public void testGetActivitiesOfUserSpacesWithListAccess() throws Exception {
    Space space = this.getSpaceInstance(0);
    Identity spaceIdentity = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);

    int totalNumber = 10;

    // demo posts activities to space
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
    }

    space = spaceService.getSpaceById(space.getId());
    assertNotNull("space must not be null", space);
    assertEquals("space.getDisplayName() must return: my space 0", "my space 0", space.getDisplayName());
    assertEquals("space.getDescription() must return: add new space 0", "add new space 0", space.getDescription());

    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 10", 10, demoActivities.getSize());

    Space space2 = this.getSpaceInstance(1);
    Identity spaceIdentity2 = this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space2.getPrettyName(), false);

    // demo posts activities to space2
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityManager.saveActivityNoReturn(spaceIdentity2, activity);
    }

    space2 = spaceService.getSpaceById(space2.getId());
    assertNotNull("space2 must not be null", space2);
    assertEquals("space2.getDisplayName() must return: my space 1", "my space 1", space2.getDisplayName());
    assertEquals("space2.getDescription() must return: add new space 1", "add new space 1", space2.getDescription());

    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 20", 20, demoActivities.getSize());

    demoActivities = activityManager.getActivitiesOfUserSpacesWithListAccess(maryIdentity);
    assertNotNull("demoActivities must not be null", demoActivities);
    assertEquals("demoActivities.getSize() must return: 0", 0, demoActivities.getSize());

  }

  public void testGetActivityFeedWithListAccess() throws Exception {
    populateActivityMass(demoIdentity, 3);
    populateActivityMass(maryIdentity, 3);
    populateActivityMass(johnIdentity, 2);

    Space space = this.getSpaceInstance(0);
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    populateActivityMass(spaceIdentity, 5);

    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals("demoActivityFeed.getSize() must be 8", 8, demoActivityFeed.getSize());
    assertEquals(8, demoActivityFeed.load(0, 10).length);

    relationshipManager.delete(new Relationship(demoIdentity, maryIdentity));
    restartTransaction();

    relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    restartTransaction();
    assertEquals(8, activityManager.getActivityFeedWithListAccess(demoIdentity).getSize());

    relationshipManager.confirm(demoIdentity, maryIdentity);
    restartTransaction();

    // add 1 activity to make sure cache is updated
    populateActivityMass(demoIdentity, 1);

    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(14, demoActivityFeed.getSize());
    assertEquals(14, demoActivityFeed.load(0, 15).length);
    assertEquals(10, demoActivityFeed.load(5, 15).length);

    RealtimeListAccess<ExoSocialActivity> maryActivityFeed = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals(9, maryActivityFeed.getSize());
    assertEquals(9, maryActivityFeed.load(0, 10).length);

    // Create demo's activity on space
    createActivityToOtherIdentity(demoIdentity, spaceIdentity, 5);

    // after that the feed of demo with have 16
    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(19, demoActivityFeed.getSize());

    // demo's Space feed must be be 10
    RealtimeListAccess<ExoSocialActivity> demoActivitiesSpaceFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(demoIdentity);
    assertEquals(10, demoActivitiesSpaceFeed.getSize());
    assertEquals(10, demoActivitiesSpaceFeed.load(0, 10).length);

    // the feed of mary must be the same because mary not the member of space
    maryActivityFeed = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals(9, maryActivityFeed.getSize());

    // john not friend of demo but member of space
    RealtimeListAccess<ExoSocialActivity> johnSpaceActivitiesFeed = activityManager.getActivitiesOfUserSpacesWithListAccess(johnIdentity);
    assertEquals(10, johnSpaceActivitiesFeed.getSize());
  }

  public void testGetActivityByStreamTypeWithListAccess() throws ObjectAlreadyExistsException {
    populateActivityMass(demoIdentity, 1);
    populateActivityMass(maryIdentity, 2);
    populateActivityMass(johnIdentity, 3);

    Space space = getSpaceInstance(1);
    Identity spaceIdentity = identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    populateActivityMass(spaceIdentity, 4);

    Space space1 = getSpaceInstance(2);
    Identity space1Identity = identityManager.getOrCreateSpaceIdentity(space1.getPrettyName());
    populateActivityMass(space1Identity, 3);

    FavoriteService favoriteService = ExoContainerContext.getService(FavoriteService.class);
    Favorite favoriteSpace = new Favorite(Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE,
                                          space.getId(),
                                          null,
                                          Long.parseLong(demoIdentity.getId()));
    favoriteService.createFavorite(favoriteSpace);
    Favorite favoriteActivity = new Favorite(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                             "1",
                                             null,
                                             Long.parseLong(demoIdentity.getId()));
    favoriteService.createFavorite(favoriteActivity);

    // Get my posted activities
    ActivityFilter activityFilter = new ActivityFilter();
    activityFilter.setStreamType(ActivityStreamType.USER_STREAM);
    RealtimeListAccess<ExoSocialActivity> streamTypeActivities =
                                                               activityManager.getActivitiesByFilterWithListAccess(demoIdentity,
                                                                                                                   activityFilter);
    assertEquals(1, streamTypeActivities.load(0, 10).length);

    // Get favorite activities
    activityFilter.setStreamType(ActivityStreamType.USER_FAVORITE_STREAM);
    streamTypeActivities = activityManager.getActivitiesByFilterWithListAccess(demoIdentity, activityFilter);
    assertEquals(1, streamTypeActivities.getSize());

    // Get manage spaces activities
    activityFilter = new ActivityFilter();
    activityFilter.setStreamType(ActivityStreamType.MANAGE_SPACES_STREAM);
    streamTypeActivities = activityManager.getActivitiesByFilterWithListAccess(demoIdentity, activityFilter);
    assertEquals(7, streamTypeActivities.load(0, 10).length);

    // Get favorite space activities
    activityFilter = new ActivityFilter();
    activityFilter.setStreamType(ActivityStreamType.FAVORITE_SPACES_STREAM);
    streamTypeActivities = activityManager.getActivitiesByFilterWithListAccess(demoIdentity, activityFilter);
    assertEquals(4, streamTypeActivities.load(0, 10).length);
  }

  public void testLoadMoreActivities() throws Exception {
    this.populateActivityMass(demoIdentity, 30);
    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(30, demoActivityFeed.getSize());
    assertEquals(10, demoActivityFeed.load(0, 10).length);
    assertEquals(20, demoActivityFeed.load(0, 20).length);
    assertEquals(10, demoActivityFeed.load(20, 10).length);
    assertEquals(15, demoActivityFeed.load(15, 20).length);
  }

  /**
   * @throws ActivityStorageException
   */
  public void testGetComments() throws ActivityStorageException {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("blah blah");
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    for (int i = 0; i < 3; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment);
      assertNotNull("comment.getId() must not be null", comment.getId());

      comments.add(comment);
    }
    restartTransaction();

    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity);
    assertEquals(3, listAccess.getSize());
    List<ExoSocialActivity> listComments = listAccess.loadAsList(0, 5);
    assertEquals(3, listComments.size());
    assertEquals("comment 0", listComments.get(0).getTitle());
    assertEquals("comment 1", listComments.get(1).getTitle());
    assertEquals("comment 2", listComments.get(2).getTitle());

    ExoSocialActivity assertActivity = activityManager.getActivity(activity.getId());
    String[] commentIds = assertActivity.getReplyToId();
    for (int i = 1; i < commentIds.length; i++) {
      assertEquals(comments.get(i - 1).getId(), commentIds[i - 1]);
    }
  }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#getActivitiesOfConnections(Identity)}
   * 
   * @throws Exception
   */
  public void testGetActivitiesOfConnections() throws Exception {
    this.populateActivityMass(johnIdentity, 10);

    ListAccess<ExoSocialActivity> demoConnectionActivities =
                                                           activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(0, demoConnectionActivities.load(0, 20).length);
    assertEquals(0, demoConnectionActivities.getSize());

    relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, demoIdentity);
    restartTransaction();

    // add 1 activity to make sure cache is updated
    this.populateActivityMass(johnIdentity, 1);

    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(12, demoConnectionActivities.load(0, 20).length);
    assertEquals(12, demoConnectionActivities.getSize());

    populateActivityMass(maryIdentity, 10);

    relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(maryIdentity, demoIdentity);
    restartTransaction();

    // add 1 activity to make sure cache is updated
    this.populateActivityMass(maryIdentity, 1);

    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(24, demoConnectionActivities.load(0, 30).length);
    assertEquals(24, demoConnectionActivities.getSize());
  }

  public void testGetActivitiesOfConnectionswithOffsetLimit() {
    this.populateActivityMass(johnIdentity, 10);

    RealtimeListAccess<ExoSocialActivity> demoConnectionActivities =
                                                                   activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertNotNull("demoConnectionActivities must not be null", demoConnectionActivities.load(0, 20));
    assertEquals("demoConnectionActivities.size() must return: 0", 0, demoConnectionActivities.getSize());

    Relationship demoJohnRelationship = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoIdentity, johnIdentity);
    restartTransaction();

    // add 1 activity to make sure cache is updated
    populateActivityMass(johnIdentity, 1);

    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(12, demoConnectionActivities.load(0, 20).length);

    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(12, demoConnectionActivities.getSize());

    populateActivityMass(maryIdentity, 10);

    Relationship demoMaryRelationship = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoIdentity, maryIdentity);
    restartTransaction();

    // add 1 activity to make sure cache is updated
    populateActivityMass(maryIdentity, 1);

    demoConnectionActivities = activityManager.getActivitiesOfConnectionsWithListAccess(demoIdentity);
    assertEquals(24, demoConnectionActivities.load(0, 30).length);
    assertEquals(24, demoConnectionActivities.getSize());

    relationshipManager.delete(demoJohnRelationship);
    relationshipManager.delete(demoMaryRelationship);
  }

  public void testRemoveLikeSubComment() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    // demo comment on root's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("demo comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);

    // mary reply on demo's comment
    ExoSocialActivity commentReply = new ExoSocialActivityImpl();
    commentReply.setTitle("mary comment reply");
    commentReply.setUserId(maryIdentity.getId());
    commentReply.setParentCommentId(comment.getId());
    activityManager.saveComment(activity, commentReply);

    // check feed of demo
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    // check feed of mary
    RealtimeListAccess<ExoSocialActivity> maryActivities = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals(1, maryActivities.getSize());
    assertEquals(1, maryActivities.load(0, 10).length);

    // check my activities of demo
    demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    // check my activities of mary
    maryActivities = activityManager.getActivitiesWithListAccess(maryIdentity);
    assertEquals(1, maryActivities.getSize());
    assertEquals(1, maryActivities.load(0, 10).length);

    RealtimeListAccess<ExoSocialActivity> johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(0, johnActivities.getSize());
    assertEquals(0, johnActivities.load(0, 10).length);

    // john like mary comment
    activityManager.saveLike(commentReply, johnIdentity);

    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, johnActivities.getSize());
    assertEquals(1, johnActivities.load(0, 10).length);

    activityManager.deleteLike(commentReply, johnIdentity);

    // check my activities of demo
    johnActivities = activityManager.getActivitiesWithListAccess(johnIdentity);
    assertEquals(0, johnActivities.getSize());
    assertEquals(0, johnActivities.load(0, 10).length);
  }

  /**
   * Test {@link ActivityManager#getActivitiesCount(Identity)}
   * 
   * @throws Exception
   * @since 1.2.0-Beta3
   */
  public void testGetActivitiesCount() throws Exception {
    int count = activityManager.getActivitiesWithListAccess(rootIdentity).getSize();
    assertEquals("count must be: 0", 0, count);

    populateActivityMass(rootIdentity, 30);
    count = activityManager.getActivitiesWithListAccess(rootIdentity).getSize();
    assertEquals("count must be: 30", 30, count);
  }

  public void testSaveManyComments() throws Exception {
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    demoActivity.setUserId(demoActivity.getId());
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    // john comments on demo's activity
    ExoSocialActivity comment1 = new ExoSocialActivityImpl();
    comment1.setTitle("john comment 1");
    comment1.setUserId(johnIdentity.getId());
    activityManager.saveComment(demoActivity, comment1);

    ListAccess<ExoSocialActivity> listAccess = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, listAccess.getSize());
    assertEquals(1, listAccess.load(0, 10).length);

    ExoSocialActivity comment2 = new ExoSocialActivityImpl();
    comment2.setTitle("john comment 2");
    comment2.setUserId(johnIdentity.getId());
    activityManager.saveComment(demoActivity, comment2);

    listAccess = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, listAccess.getSize());
    assertEquals(1, listAccess.load(0, 10).length);
  }

  public void testGetLastIdenties() throws Exception {
    Mockito.when(mockProfileSearch.search(Mockito.nullable(Identity.class),
                                          Mockito.nullable(ProfileFilter.class),
                                          Mockito.nullable(Type.class),
                                          Mockito.anyLong(),
                                          Mockito.anyLong()))
           .thenReturn(Arrays.asList(paulIdentity.getId()))
           .thenReturn(Arrays.asList(paulIdentity.getId()))
           .thenReturn(Arrays.asList(paulIdentity.getId(),
                                     jameIdentity.getId(),
                                     raulIdentity.getId(),
                                     ghostIdentity.getId(),
                                     demoIdentity.getId()));
    List<Identity> lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    Identity id1 = lastIds.get(0);
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    OrganizationService os = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    User user1 = os.getUserHandler().createUserInstance("newId1");
    os.getUserHandler().createUser(user1, false);
    Identity newId1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId1", false);

    Mockito.when(mockProfileSearch.search(Mockito.nullable(Identity.class),
                                          Mockito.nullable(ProfileFilter.class),
                                          Mockito.nullable(Relationship.Type.class),
                                          Mockito.anyLong(),
                                          Mockito.anyLong()))
           .thenReturn(Arrays.asList(newId1.getId()))
           .thenReturn(Arrays.asList(paulIdentity.getId()));

    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId1, lastIds.get(0));
    identityManager.deleteIdentity(newId1);
    assertTrue(identityManager.getIdentity(newId1.getId(), false).isDeleted());
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
    User user2 = os.getUserHandler().createUserInstance("newId2");
    os.getUserHandler().createUser(user2, false);
    Identity newId2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId2", true);

    Mockito.when(mockProfileSearch.search(Mockito.nullable(Identity.class),
                                          Mockito.nullable(ProfileFilter.class),
                                          Mockito.nullable(Relationship.Type.class),
                                          Mockito.anyLong(),
                                          Mockito.anyLong()))
           .thenReturn(Arrays.asList(newId2.getId(),
                                     paulIdentity.getId(),
                                     jameIdentity.getId(),
                                     raulIdentity.getId(),
                                     ghostIdentity.getId()))
           .thenReturn(Arrays.asList(paulIdentity.getId(),
                                     jameIdentity.getId(),
                                     raulIdentity.getId(),
                                     ghostIdentity.getId(),
                                     demoIdentity.getId()));

    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    identityManager.deleteIdentity(newId2);
    assertTrue(identityManager.getIdentity(newId2.getId(), true).isDeleted());
    lastIds = identityManager.getLastIdentities(5);
    assertEquals(5, lastIds.size());
    assertEquals(id1, lastIds.get(0));

    newId1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId1", false);
    newId2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "newId2", true);

    Mockito.when(mockProfileSearch.search(Mockito.nullable(Identity.class),
                                          Mockito.nullable(ProfileFilter.class),
                                          Mockito.nullable(Relationship.Type.class),
                                          Mockito.anyLong(),
                                          Mockito.anyLong()))
           .thenReturn(Arrays.asList(newId2.getId()))
           .thenReturn(Arrays.asList(newId2.getId(), newId1.getId()))
           .thenReturn(Arrays.asList(newId2.getId()))
           .thenReturn(Arrays.asList(newId2.getId(), paulIdentity.getId()))
           .thenReturn(Arrays.asList(paulIdentity.getId()));

    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(2);
    assertEquals(2, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    assertEquals(newId1, lastIds.get(1));
    identityManager.deleteIdentity(newId1);
    os.getUserHandler().removeUser("newId1", false);
    assertTrue(identityManager.getIdentity(newId1.getId(), true).isDeleted());
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    lastIds = identityManager.getLastIdentities(2);
    assertEquals(2, lastIds.size());
    assertEquals(newId2, lastIds.get(0));
    assertFalse(newId1.equals(lastIds.get(1)));
    identityManager.deleteIdentity(newId2);
    os.getUserHandler().removeUser("newId2", false);
    assertTrue(identityManager.getIdentity(newId2.getId(), false).isDeleted());
    lastIds = identityManager.getLastIdentities(1);
    assertEquals(1, lastIds.size());
    assertEquals(id1, lastIds.get(0));
  }

  public void testMentionActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    ExoSocialActivity got = activityManager.getActivity(activity.getId());
    assertEquals(0, got.getMentionedIds().length);

    RealtimeListAccess<ExoSocialActivity> demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(0, demoActivityFeed.getSize());
    assertEquals(0, demoActivityFeed.load(0, 10).length);

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("mary mention @demo @john");
    comment.setUserId(maryIdentity.getId());
    activityManager.saveComment(activity, comment);

    got = activityManager.getActivity(activity.getId());
    assertEquals(2, got.getMentionedIds().length);

    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(1, demoActivityFeed.getSize());
    assertEquals(1, demoActivityFeed.load(0, 10).length);

    activityManager.deleteComment(activity, comment);

    got = activityManager.getActivity(activity.getId());
    assertEquals(0, got.getMentionedIds().length);

    demoActivityFeed = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(0, demoActivityFeed.getSize());
    assertEquals(0, demoActivityFeed.load(0, 10).length);
  }

  public void testSpaceRoleMention() throws Exception {
    Space space = this.getSpaceInstance(0);
    Identity spaceIdentity = this.identityManager.getOrCreateSpaceIdentity(space.getPrettyName());
    restartTransaction();

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello @member:" + spaceIdentity.getId());
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    ExoSocialActivity got = activityManager.getActivity(activity.getId());
    assertEquals(space.getMembers().length, got.getMentionedIds().length);
    assertTrue(StringUtils.containsIgnoreCase(got.getTitle(), "member"));
    assertFalse(StringUtils.containsIgnoreCase(got.getTitle(), "@member"));

    activity = new ExoSocialActivityImpl();
    activity.setTitle("hello @manager:" + spaceIdentity.getId());
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    got = activityManager.getActivity(activity.getId());
    assertEquals(space.getManagers().length, got.getMentionedIds().length);
    assertTrue(StringUtils.containsIgnoreCase(got.getTitle(), "manager"));
    assertFalse(StringUtils.containsIgnoreCase(got.getTitle(), "@manager"));

    activity = new ExoSocialActivityImpl();
    activity.setTitle("hello @publisher:" + spaceIdentity.getId());
    activity.setUserId(maryIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    got = activityManager.getActivity(activity.getId());
    assertEquals(space.getPublishers().length, got.getMentionedIds().length);
    assertEquals(demoIdentity.getId(), got.getMentionedIds()[0]);
    assertTrue(StringUtils.containsIgnoreCase(got.getTitle(), "publisher"));
    assertFalse(StringUtils.containsIgnoreCase(got.getTitle(), "@publisher"));

    spaceService.addRedactor(space, "john");
    activity = new ExoSocialActivityImpl();
    activity.setTitle("hello @redactor:" + spaceIdentity.getId());
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    got = activityManager.getActivity(activity.getId());
    assertEquals(space.getRedactors().length, got.getMentionedIds().length);
    assertEquals(johnIdentity.getId(), got.getMentionedIds()[0]);
    assertTrue(StringUtils.containsIgnoreCase(got.getTitle(), "redactor"));
    assertFalse(StringUtils.containsIgnoreCase(got.getTitle(), "@redactor"));
  }

  public void testLikeCommentActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    // demo comment on root's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("demo comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);

    // check feed of demo
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    // check my activities of demo
    demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    // john like root activity
    activityManager.saveLike(comment, johnIdentity);

    RealtimeListAccess<ExoSocialActivity> johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, johnActivities.getSize());
    assertEquals(1, johnActivities.load(0, 10).length);

    // check my activities of demo
    johnActivities = activityManager.getActivitiesWithListAccess(johnIdentity);
    assertEquals(1, johnActivities.getSize());
    assertEquals(1, johnActivities.load(0, 10).length);
  }

  public void testLikeSubCommentActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("hello");
    activity.setUserId(rootIdentity.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    // demo comment on root's activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("demo comment");
    comment.setUserId(demoIdentity.getId());
    activityManager.saveComment(activity, comment);

    // mary reply on demo's comment
    ExoSocialActivity commentReply = new ExoSocialActivityImpl();
    commentReply.setTitle("mary comment reply");
    commentReply.setUserId(maryIdentity.getId());
    commentReply.setParentCommentId(comment.getId());
    activityManager.saveComment(activity, commentReply);

    // check feed of demo
    RealtimeListAccess<ExoSocialActivity> demoActivities = activityManager.getActivityFeedWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    // check feed of mary
    RealtimeListAccess<ExoSocialActivity> maryActivities = activityManager.getActivityFeedWithListAccess(maryIdentity);
    assertEquals(1, maryActivities.getSize());
    assertEquals(1, maryActivities.load(0, 10).length);

    // check my activities of demo
    demoActivities = activityManager.getActivitiesWithListAccess(demoIdentity);
    assertEquals(1, demoActivities.getSize());
    assertEquals(1, demoActivities.load(0, 10).length);

    // check my activities of mary
    maryActivities = activityManager.getActivitiesWithListAccess(maryIdentity);
    assertEquals(1, maryActivities.getSize());
    assertEquals(1, maryActivities.load(0, 10).length);

    RealtimeListAccess<ExoSocialActivity> johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(0, johnActivities.getSize());
    assertEquals(0, johnActivities.load(0, 10).length);

    // john like mary comment
    activityManager.saveLike(commentReply, johnIdentity);

    johnActivities = activityManager.getActivityFeedWithListAccess(johnIdentity);
    assertEquals(1, johnActivities.getSize());
    assertEquals(1, johnActivities.load(0, 10).length);
  }

  private void createActivityToOtherIdentity(Identity posterIdentity,
                                             Identity targetIdentity,
                                             int number) {

    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();

      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      try {
        activityManager.saveActivityNoReturn(targetIdentity, activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }

  /**
   * Gets an instance of the space.
   * 
   * @param spaceService
   * @param number
   * @return
   * @throws Exception
   * @since 1.2.0-GA
   */
  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setGroupId(SpaceUtils.SPACE_GROUP + "/" + space.getPrettyName());
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { "demo", "john" };
    String[] members = new String[] { "raul", "ghost", "demo", "john" };
    String[] publishers = new String[] { "demo" };
    String[] redactors = new String[] { "john" };
    String[] invitedUsers = new String[] { "mary", "paul" };
    String[] pendingUsers = new String[] { "jame" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setRedactors(redactors);
    space.setPublishers(publishers);
    spaceStorage.saveSpace(space, true);
    return space;
  }

  private void addActivityTypePlugin(ActivityManager activityManager, String activityType, boolean enabled) {
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName(ActivityTypePlugin.ACTIVITY_TYPE_PARAM);
    param.setValue(activityType);
    params.addParameter(param);
    param = new ValueParam();
    param.setName(ActivityTypePlugin.ENABLE_NOTIFICATION_PARAM);
    param.setValue(String.valueOf(enabled));
    params.addParameter(param);
    activityManager.addActivityTypePlugin(new ActivityTypePlugin(params) {
      @Override
      public boolean isEnableNotification(ExoSocialActivity activity, String username) {
        return !StringUtils.equals(username, "root3") && super.isEnableNotification();
      }
    });
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
      activity.setTitle("title " + i);
      activity.setUserId(user.getId());
      try {
        activityManager.saveActivityNoReturn(user, activity);
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

  private Space createSpace(String spaceName, String creator, String... members) {
    Space space = new Space();
    space.setDisplayName(spaceName);
    space.setPrettyName(spaceName);
    space.setGroupId("/spaces/" + space.getPrettyName());
    space.setRegistration(Space.OPEN);
    space.setDescription("description of space" + spaceName);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    String[] managers = new String[] { creator };
    space.setManagers(managers);
    Space createdSpace = spaceService.createSpace(space);
    if (ArrayUtils.isNotEmpty(members)) {
      Arrays.stream(members).forEach(u -> spaceService.addMember(createdSpace, u));
    }
    if (ArrayUtils.isNotEmpty(createdSpace.getRedactors())) {
      Arrays.stream(createdSpace.getRedactors()).forEach(u -> spaceService.removeRedactor(createdSpace, u));
    }
    return createdSpace;
  }

  protected Identity createIdentity(String username) {
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, username);
    profile.setProperty(Profile.LAST_NAME, username);
    identity.setProfile(profile);
    identityManager.updateProfile(profile);
    if (identity.isDeleted() || !identity.isEnable()) {
      identity.setDeleted(false);
      identity.setEnable(true);
      identityManager.updateIdentity(identity);
    }
    return identityManager.getOrCreateUserIdentity(username);
  }

}
