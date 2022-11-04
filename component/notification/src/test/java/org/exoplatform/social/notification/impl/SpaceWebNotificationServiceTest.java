package org.exoplatform.social.notification.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SpaceWebNotificationServiceTest {

  private static final String                    ACTIVE_FEATURE     = "SpaceWebNotifications";

  private static final String                    METADATA_TYPE_NAME = "unread";

  private static final String                    activityId         = "activityId";

  private static final long                      userIdentityId     = 15L;

  private static final long                      spaceId            = 12L;

  private boolean                                enabled;

  @Mock
  private MetadataService                        metadataService;

  @Mock
  private ListenerService                        listenerService;

  @Mock
  private ExoFeatureService                      featureService;

  private static SpaceWebNotificationServiceImpl spaceWebNotificationService;

  @Before
  public void setUp() throws Exception {
    enabled = featureService.isActiveFeature(ACTIVE_FEATURE);
    spaceWebNotificationService = new SpaceWebNotificationServiceImpl(metadataService, featureService, listenerService);
  }

  @Test
  public void testMarkAsUnread() throws Exception {
    if (!enabled) {
      return;
    }

    SpaceWebNotificationItem spaceWebNotificationItem = mock(SpaceWebNotificationItem.class);
    MetadataObject metadataObject = mock(MetadataObject.class);
    MetadataKey metadataKey = mock(MetadataKey.class);
    String metadataObjectType = ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;

    when(spaceWebNotificationItem.getUserId()).thenReturn(userIdentityId);
    when(spaceWebNotificationItem.getApplicationName()).thenReturn(metadataObjectType);
    when(spaceWebNotificationItem.getSpaceId()).thenReturn(spaceId);
    when(spaceWebNotificationItem.getUserId()).thenReturn(userIdentityId);

    when(metadataKey.getType()).thenReturn(METADATA_TYPE_NAME);
    when(metadataKey.getName()).thenReturn(String.valueOf(userIdentityId));
    when(metadataKey.getAudienceId()).thenReturn(userIdentityId);
    when(metadataObject.getType()).thenReturn(metadataObjectType);
    when(metadataObject.getId()).thenReturn(activityId);
    when(metadataObject.getSpaceId()).thenReturn(spaceId);

    MetadataItem metadataItem = metadataService.createMetadataItem(metadataObject, metadataKey, userIdentityId);

    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);
    assertNotNull(metadataItem);
    assertTrue(metadataItem.getId() > 0);
    assertEquals(spaceId, metadataItem.getObject().getSpaceId());
    assertEquals(activityId, metadataItem.getObject().getId());
    assertEquals(metadataObjectType, metadataItem.getObject().getType());
    assertEquals(METADATA_TYPE_NAME, metadataItem.getMetadata().getTypeName());
    assertEquals(String.valueOf(userIdentityId), metadataItem.getMetadata().getName());
    assertEquals(userIdentityId, metadataItem.getMetadata().getAudienceId());
  }

  @Test
  public void testMarkAsRead() throws Exception {
    SpaceWebNotificationItem spaceWebNotificationItem = mock(SpaceWebNotificationItem.class);
    if (spaceWebNotificationItem == null) {
      return;
    }
    when(userIdentityId).thenReturn(spaceWebNotificationItem.getUserId());

    MetadataObject metadataObject = mock(MetadataObject.class);
    MetadataKey metadataKey = mock(MetadataKey.class);
    String metadataObjectType = ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;

    when(spaceWebNotificationItem.getUserId()).thenReturn(userIdentityId);
    when(spaceWebNotificationItem.getApplicationName()).thenReturn(metadataObjectType);
    when(spaceWebNotificationItem.getSpaceId()).thenReturn(spaceId);
    when(spaceWebNotificationItem.getUserId()).thenReturn(userIdentityId);

    when(metadataKey.getType()).thenReturn(METADATA_TYPE_NAME);
    when(metadataKey.getName()).thenReturn(String.valueOf(userIdentityId));
    when(metadataKey.getAudienceId()).thenReturn(userIdentityId);
    when(metadataObject.getType()).thenReturn(metadataObjectType);
    when(metadataObject.getId()).thenReturn(activityId);
    when(metadataObject.getSpaceId()).thenReturn(spaceId);

    spaceWebNotificationService.markAsRead(spaceWebNotificationItem);

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    assertTrue(CollectionUtils.isNotEmpty(metadataItems));
    assertEquals(1, metadataItems.size());
    metadataService.deleteMetadataItem(metadataItems.get(0).getId(), false);
    assertEquals(0, metadataItems.size());
  }

}
