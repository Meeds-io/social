package org.exoplatform.social.notification.impl;

import static org.exoplatform.social.notification.impl.SpaceWebNotificationServiceImpl.APPLICATION_SUB_ITEM_IDS;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_ALL_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_UNREAD_EVENT_NAME;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.SpaceWebNotificationPlugin;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.identity.model.Identity;

@RunWith(MockitoJUnitRunner.class)
public class SpaceWebNotificationServiceTest extends AbstractCoreTest {

  private static final String                    ACTIVITY_ID_PROP     = "activityId";

  private static final String                    METADATA_TYPE_NAME   = "unread";

  private static final String                    activityId           = "5556";

  private static final String                    METADATA_OBJECT_TYPE =
                                                                      ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;

  private static final long                      userIdentityId       = 15L;

  private static final long                      spaceId              = 12L;

  @Mock
  private MetadataService                        metadataService;

  @Mock
  private ListenerService                        listenerService;

  @Mock
  private PluginSettingService                   pluginSettingService;

  @Mock
  IdentityManager                                identityManager;                                                                 // NOSONAR

  @Mock
  SpaceService                                   spaceService; 
  
  private static SpaceWebNotificationServiceImpl spaceWebNotificationService;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    spaceWebNotificationService = new SpaceWebNotificationServiceImpl(getContainer(),
                                                                      metadataService,
                                                                      pluginSettingService,
                                                                      listenerService,
                                                                      identityManager,
                                                                      spaceService);
  }

  @Test
  public void testMarkAsUnread() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsRead(null));

    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(METADATA_OBJECT_TYPE,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(METADATA_OBJECT_TYPE,
                                                       activityId,
                                                       null,
                                                       spaceId);
    spaceWebNotificationService = new SpaceWebNotificationServiceImpl(getContainer(),
                                                                      metadataService,
                                                                      pluginSettingService,
                                                                      listenerService,
                                                                      identityManager,
                                                                      spaceService);

    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsUnread(null));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsUnread(new SpaceWebNotificationItem(null,
                                                                                             "itemId",
                                                                                             userIdentityId,
                                                                                             spaceId)));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsUnread(new SpaceWebNotificationItem("appName",
                                                                                             null,
                                                                                             userIdentityId,
                                                                                             spaceId)));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsUnread(new SpaceWebNotificationItem("appName",
                                                                                             "itemId",
                                                                                             0,
                                                                                             spaceId)));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsUnread(new SpaceWebNotificationItem("appName",
                                                                                             "itemId",
                                                                                             userIdentityId,
                                                                                             0)));

    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);
    verify(metadataService, times(1)).createMetadataItem(eq(metadataObject), eq(metadataKey), any(), eq(userIdentityId));
    verify(listenerService, times(1)).broadcast(NOTIFICATION_UNREAD_EVENT_NAME, spaceWebNotificationItem, userIdentityId);
  }

  @Test
  public void testDispatchNotification() throws Exception {
    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(METADATA_OBJECT_TYPE,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     0);

    SpaceWebNotificationPlugin spaceWebNotificationPlugin = mock(SpaceWebNotificationPlugin.class);
    spaceWebNotificationService.addPlugin(spaceWebNotificationPlugin);

    String username = "testuser";
    NotificationInfo notificationInfo = mock(NotificationInfo.class);
    BaseNotificationPlugin notificationPlugin = mock(BaseNotificationPlugin.class);
    when(notificationPlugin.getId()).thenReturn("notificationPlugin");
    PluginKey pluginKey = new PluginKey(notificationPlugin);
    when(notificationInfo.getKey()).thenReturn(pluginKey);

    when(spaceWebNotificationPlugin.getSpaceApplicationItem(notificationInfo, username)).thenReturn(spaceWebNotificationItem);
    when(spaceWebNotificationPlugin.isManagedPlugin(pluginKey)).thenReturn(true);

    spaceWebNotificationService.dispatch(notificationInfo, username);

    verify(spaceWebNotificationPlugin, times(1)).getSpaceApplicationItem(notificationInfo, username);
    verifyNoInteractions(metadataService, listenerService);

    spaceWebNotificationItem.setSpaceId(spaceId);

    spaceWebNotificationService.dispatch(notificationInfo, username);

    verify(metadataService, times(1)).createMetadataItem(any(), any(), any(), anyLong());
    verify(listenerService, times(1)).broadcast(NOTIFICATION_UNREAD_EVENT_NAME, spaceWebNotificationItem, userIdentityId);
  }

  @Test
  public void testMarkAsUnreadWithSubItems() throws Exception {
    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(METADATA_OBJECT_TYPE,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);
    String itemId1 = "subItem1";
    spaceWebNotificationItem.setActivityId(activityId);
    spaceWebNotificationItem.addApplicationSubItem(itemId1);

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(METADATA_OBJECT_TYPE,
                                                       activityId,
                                                       null,
                                                       spaceId);
    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);

    verify(metadataService, times(1)).createMetadataItem(eq(metadataObject),
                                                         eq(metadataKey),
                                                         argThat(
                                                           props -> 
                                                             props != null && props.size() == 2
                                                                 && itemId1.equals(props.get("applicationSubItemIds"))
                                                                 && spaceWebNotificationItem.getActivityId().equals(props.get(ACTIVITY_ID_PROP))
                                                         ),
                                                         eq(userIdentityId));
    verify(listenerService, times(1)).broadcast(NOTIFICATION_UNREAD_EVENT_NAME, spaceWebNotificationItem, userIdentityId);
  }

  @Test
  public void testMarkAsUnreadWithExistingSubItems() throws Exception {
    String itemId1 = "subItem1";
    Map<String, String> properties = Collections.singletonMap(APPLICATION_SUB_ITEM_IDS, itemId1);
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(METADATA_OBJECT_TYPE,
                                                       activityId,
                                                       null,
                                                       spaceId);
    MetadataItem metadataItem = new MetadataItem(1l,
                                                 newMetadata(),
                                                 metadataObject,
                                                 userIdentityId,
                                                 spaceId,
                                                 properties);
    when(metadataService.getMetadataItemsByMetadataAndObject(metadataKey,
                                                             metadataObject))
                                                                             .thenReturn(Collections.singletonList(metadataItem));
    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(METADATA_OBJECT_TYPE,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);
    String itemId2 = "subItem2";
    spaceWebNotificationItem.addApplicationSubItem(itemId2);
    spaceWebNotificationItem.setActivityId(activityId);
    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);

    verify(metadataService, times(1)).createMetadataItem(eq(metadataObject),
                                                         eq(metadataKey),
                                                         argThat(
                                                           props -> 
                                                             props != null && props.size() == 2
                                                                 && (itemId1 + "," + itemId2).equals(props.get("applicationSubItemIds"))
                                                                 && spaceWebNotificationItem.getActivityId().equals(props.get(ACTIVITY_ID_PROP))
                                                         ),
                                                         eq(userIdentityId));

    verify(listenerService, times(1)).broadcast(NOTIFICATION_UNREAD_EVENT_NAME, spaceWebNotificationItem, userIdentityId);
  }

  private Metadata newMetadata() {
    return new Metadata(7l,
                        new MetadataType(6l, METADATA_TYPE_NAME),
                        METADATA_OBJECT_TYPE,
                        spaceId,
                        userIdentityId,
                        System.currentTimeMillis(),
                        null);
  }

  @Test
  public void testMarkAllAsRead() throws Exception {
    spaceWebNotificationService.markAllAsRead(userIdentityId, spaceId);
    verify(metadataService,
           times(1)).deleteByMetadataTypeAndSpaceIdAndCreatorId(SpaceWebNotificationServiceImpl.METADATA_TYPE_NAME,
                                                                spaceId,
                                                                userIdentityId);
    verify(listenerService, times(1)).broadcast(NOTIFICATION_ALL_READ_EVENT_NAME,
                                                new SpaceWebNotificationItem(null, null, userIdentityId, spaceId),
                                                userIdentityId);
  }

  @Test
  public void testCountUnreadItemsByApplication() throws Exception {
    Map<String, Long> expectedResult = Collections.singletonMap("activity", 5l);
    when(metadataService.countMetadataItemsByMetadataTypeAndAudienceId(METADATA_TYPE_NAME,
                                                                       userIdentityId,
                                                                       spaceId)).thenReturn(expectedResult);
    Map<String, Long> result = spaceWebNotificationService.countUnreadItemsByApplication(userIdentityId, spaceId);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testCountUnreadActivities() {
    Map<Long, Long> expectedResult = Collections.singletonMap(spaceId, 5l);
    String username = "test";
    Identity identity = mock(Identity.class);
    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(identity);
    when(identity.getId()).thenReturn(String.valueOf(userIdentityId));
    when(spaceService.getMemberSpacesIds(username, 0, -1)).thenReturn(Collections.singletonList(String.valueOf(spaceId)));
    when(metadataService.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(METADATA_TYPE_NAME,
                                                                                 userIdentityId,
                                                                                 Collections.singletonList(spaceId))).thenReturn(expectedResult);

    long result = spaceWebNotificationService.countUnreadActivities(username);
    assertEquals(5l, result);
  }

  @Test
  public void testCountUnreadActivitiesBySpace() throws IllegalAccessException, ObjectNotFoundException {
    Map<Long, Long> expectedResult = Collections.singletonMap(spaceId, 5l);
    when(metadataService.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(METADATA_TYPE_NAME,
                                                                                 userIdentityId,
                                                                                 Collections.singletonList(spaceId))).thenReturn(expectedResult);
    String username = "test";
    Identity identity = mock(Identity.class);
    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(identity);
    when(identity.getId()).thenReturn(String.valueOf(userIdentityId));

    assertThrows(ObjectNotFoundException.class, () -> spaceWebNotificationService.countUnreadActivitiesBySpace(username, spaceId));

    Space space = mock(Space.class);
    when(spaceService.getSpaceById(String.valueOf(spaceId))).thenReturn(space);

    assertThrows(IllegalAccessException.class, () -> spaceWebNotificationService.countUnreadActivitiesBySpace(username, spaceId));

    when(spaceService.isMember(space, username)).thenReturn(true);
    long result = spaceWebNotificationService.countUnreadActivitiesBySpace(username, spaceId);
    assertEquals(5l, result);
  }

  @Test
  public void testGetUnreadActivityIds() {
    String username = "test";
    Identity identity = mock(Identity.class);
    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(identity);
    when(identity.getId()).thenReturn(String.valueOf(userIdentityId));
    List<String> spaceIds = Collections.singletonList(String.valueOf(spaceId));
    when(spaceService.getMemberSpacesIds(username, 0, -1)).thenReturn(spaceIds);

    MetadataItem metadataItem = mock(MetadataItem.class);
    when(metadataItem.getProperties()).thenReturn(Collections.singletonMap(ACTIVITY_ID_PROP, activityId));
    when(metadataService.getMetadataItemsByMetadataNameAndTypeAndSpaceIds(String.valueOf(userIdentityId),
                                                                          METADATA_TYPE_NAME,
                                                                          spaceIds.stream().map(Long::parseLong).toList(),
                                                                          0,
                                                                          10)).thenReturn(Collections.singletonList(metadataItem));

    List<Long> result = spaceWebNotificationService.getUnreadActivityIds(username, 0, 10);
    assertEquals(Collections.singletonList(Long.parseLong(activityId)), result);
  }

  @Test
  public void testGetUnreadActivityIdsBySpace() throws IllegalAccessException, ObjectNotFoundException {
    List<String> spaceIds = Collections.singletonList(String.valueOf(spaceId));
    MetadataItem metadataItem = mock(MetadataItem.class);
    when(metadataItem.getProperties()).thenReturn(Collections.singletonMap(ACTIVITY_ID_PROP, activityId));
    when(metadataService.getMetadataItemsByMetadataNameAndTypeAndSpaceIds(String.valueOf(userIdentityId),
                                                                          METADATA_TYPE_NAME,
                                                                          spaceIds.stream().map(Long::parseLong).toList(),
                                                                          0,
                                                                          10)).thenReturn(Collections.singletonList(metadataItem));
    String username = "test";
    Identity identity = mock(Identity.class);
    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(identity);
    when(identity.getId()).thenReturn(String.valueOf(userIdentityId));
    
    assertThrows(ObjectNotFoundException.class, () -> spaceWebNotificationService.getUnreadActivityIdsBySpace(username, spaceId, 0, 10));
    
    Space space = mock(Space.class);
    when(spaceService.getSpaceById(String.valueOf(spaceId))).thenReturn(space);

    assertThrows(IllegalAccessException.class, () -> spaceWebNotificationService.getUnreadActivityIdsBySpace(username, spaceId, 0, 10));
    
    when(spaceService.isMember(space, username)).thenReturn(true);
    List<Long> result = spaceWebNotificationService.getUnreadActivityIdsBySpace(username, spaceId, 0, 10);
    assertEquals(Collections.singletonList(Long.parseLong(activityId)), result);
  }

  @Test
  public void testCountUnreadItemsBySpace() throws Exception {
    List<String> spaceIds = new ArrayList<>();

    Identity rootIdentity = new Identity();
    rootIdentity.setId("1");
    rootIdentity.setProviderId("organization");
    rootIdentity.setRemoteId("root");

    when(identityManager.getOrCreateUserIdentity("root")).thenReturn(rootIdentity);

    Space space = new Space();
    space.setId("1");
    spaceService.addMember(space, rootIdentity.getRemoteId());
    spaceIds = spaceService.getMemberSpacesIds("root", 0, -1);
    List<Long> spacesId = spaceIds.stream().map(e -> Long.parseLong(e)).collect(Collectors.toList());
    Map<Long, Long> expectedResult = Collections.singletonMap(Long.parseLong(space.getId()), 5l);

    when(metadataService.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(METADATA_TYPE_NAME,
                                                                                 Long.parseLong(rootIdentity.getId()),
                                                                                 spacesId)).thenReturn(expectedResult);

    Map<Long, Long> result = spaceWebNotificationService.countUnreadItemsBySpace(rootIdentity.getRemoteId());
    assertEquals(expectedResult, result);

  }

  @Test
  public void testMarkAsRead() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsRead(null));

    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(METADATA_OBJECT_TYPE,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(METADATA_OBJECT_TYPE,
                                                       activityId,
                                                       null,
                                                       spaceId);
    Metadata metadata = newMetadata();
    long metadataItemId = 5l;
    when(metadataService.getMetadataItemsByMetadataAndObject(metadataKey,
                                                             metadataObject)).thenReturn(Arrays.asList(new MetadataItem(metadataItemId, metadata, metadataObject, userIdentityId, System.currentTimeMillis(), null)));

    spaceWebNotificationService = new SpaceWebNotificationServiceImpl(getContainer(),
                                                                      metadataService,
                                                                      pluginSettingService,
                                                                      listenerService,
                                                                      identityManager,
                                                                      spaceService);

    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsRead(null));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsRead(new SpaceWebNotificationItem(null,
                                                                                           "itemId",
                                                                                           userIdentityId,
                                                                                           spaceId)));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsRead(new SpaceWebNotificationItem("appName",
                                                                                           null,
                                                                                           userIdentityId,
                                                                                           spaceId)));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsRead(new SpaceWebNotificationItem("appName",
                                                                                           "itemId",
                                                                                           0,
                                                                                           spaceId)));
    assertThrows(IllegalArgumentException.class, // NOSONAR
                 () -> spaceWebNotificationService.markAsRead(new SpaceWebNotificationItem("appName",
                                                                                           "itemId",
                                                                                           userIdentityId,
                                                                                           0)));

    spaceWebNotificationService.markAsRead(spaceWebNotificationItem);

    verify(metadataService, times(1)).deleteMetadataItem(metadataItemId, true);
    verify(listenerService, times(1)).broadcast(NOTIFICATION_READ_EVENT_NAME, spaceWebNotificationItem, userIdentityId);

  }

}
