package org.exoplatform.social.notification.impl;

import static org.exoplatform.social.notification.impl.SpaceWebNotificationServiceImpl.APPLICATION_SUB_ITEM_IDS;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_ALL_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_UNREAD_EVENT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;

@RunWith(MockitoJUnitRunner.class)
public class SpaceWebNotificationServiceTest {

  private static final String                    ACTIVE_FEATURE       = "SpaceWebNotifications";

  private static final String                    METADATA_TYPE_NAME   = "unread";

  private static final String                    activityId           = "activityId";

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
  private ExoFeatureService                      featureService;

  private static SpaceWebNotificationServiceImpl spaceWebNotificationService;

  @Before
  public void setUp() throws Exception {
    when(featureService.isActiveFeature(ACTIVE_FEATURE)).thenReturn(true);
    spaceWebNotificationService = new SpaceWebNotificationServiceImpl(PortalContainer.getInstance(),
                                                                      metadataService,
                                                                      featureService,
                                                                      pluginSettingService,
                                                                      listenerService);
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
    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);

    verify(metadataService, times(1)).createMetadataItem(eq(metadataObject), eq(metadataKey), any(), eq(userIdentityId));
    verify(listenerService, times(1)).broadcast(NOTIFICATION_UNREAD_EVENT_NAME, spaceWebNotificationItem, userIdentityId);
  }

  @Test
  public void testMarkAsUnreadWithSubItems() throws Exception {
    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(METADATA_OBJECT_TYPE,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);
    String itemId1 = "subItem1";
    spaceWebNotificationItem.addApplicationSubItem(itemId1);

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(METADATA_OBJECT_TYPE,
                                                       activityId,
                                                       null,
                                                       spaceId);
    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);

    verify(metadataService, times(1)).createMetadataItem(eq(metadataObject),
                                                         eq(metadataKey),
                                                         argThat(new ArgumentMatcher<Map<String, String>>() {
                                                           @Override
                                                           public boolean matches(Map<String, String> properties) {
                                                             return properties != null && properties.size() == 1
                                                                 && itemId1.equals(properties.values().iterator().next());
                                                           }
                                                         }),
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
    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);

    verify(metadataService, times(1)).createMetadataItem(eq(metadataObject),
                                                         eq(metadataKey),
                                                         argThat(new ArgumentMatcher<Map<String, String>>() {
                                                           @Override
                                                           public boolean matches(Map<String, String> properties) {
                                                             return properties != null && properties.size() == 1
                                                                 && (itemId1 + "," + itemId2).equals(properties.values()
                                                                                                               .iterator()
                                                                                                               .next());
                                                           }
                                                         }),
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
    verify(metadataService, times(1)).deleteMetadataBySpaceIdAndAudienceId(spaceId, userIdentityId);
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
    spaceWebNotificationService.markAsRead(spaceWebNotificationItem);

    verify(metadataService, times(1)).deleteMetadataItem(metadataItemId, true);
    verify(listenerService, times(1)).broadcast(NOTIFICATION_READ_EVENT_NAME, spaceWebNotificationItem, userIdentityId);

  }

}
