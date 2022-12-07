package org.exoplatform.social.notification.impl;

import static org.exoplatform.social.notification.impl.SpaceWebNotificationServiceImpl.NOTIFICATION_READ_EVENT_NAME;
import static org.exoplatform.social.notification.impl.SpaceWebNotificationServiceImpl.NOTIFICATION_UNREAD_EVENT_NAME;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.settings.ExoFeatureService;
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

  private static final String                    ACTIVE_FEATURE     = "SpaceWebNotifications";

  private static final String                    METADATA_TYPE_NAME = "unread";

  private static final String                    activityId         = "activityId";

  private static final long                      userIdentityId     = 15L;

  private static final long                      spaceId            = 12L;

  @Mock
  private MetadataService                        metadataService;

  @Mock
  private ListenerService                        listenerService;

  @Mock
  private ExoFeatureService                      featureService;

  private static SpaceWebNotificationServiceImpl spaceWebNotificationService;

  @Before
  public void setUp() throws Exception {
    when(featureService.isActiveFeature(ACTIVE_FEATURE)).thenReturn(true);
    spaceWebNotificationService = new SpaceWebNotificationServiceImpl(metadataService, featureService, listenerService);
  }

  @Test
  public void testMarkAsUnread() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsRead(null));

    String metadataObjectType = ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;

    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(metadataObjectType,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(metadataObjectType,
                                                       activityId,
                                                       null,
                                                       spaceId);
    spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);

    verify(metadataService, times(1)).createMetadataItem(metadataObject, metadataKey, userIdentityId);
    verify(listenerService, times(1)).broadcast(NOTIFICATION_UNREAD_EVENT_NAME, spaceWebNotificationItem, userIdentityId);
  }

  @Test
  public void testMarkAsRead() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsRead(null));

    String metadataObjectType = ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;

    SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(metadataObjectType,
                                                                                     activityId,
                                                                                     userIdentityId,
                                                                                     spaceId);

    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(metadataObjectType,
                                                       activityId,
                                                       null,
                                                       spaceId);
    Metadata metadata = new Metadata(7l,
                                     new MetadataType(6l, METADATA_TYPE_NAME),
                                     metadataObjectType,
                                     spaceId,
                                     userIdentityId,
                                     System.currentTimeMillis(),
                                     null);
    long metadataItemId = 5l;
    when(metadataService.getMetadataItemsByMetadataAndObject(metadataKey,
                                                             metadataObject)).thenReturn(Arrays.asList(new MetadataItem(metadataItemId, metadata, metadataObject, userIdentityId, System.currentTimeMillis(), null)));
    spaceWebNotificationService.markAsRead(spaceWebNotificationItem);

    verify(metadataService, times(1)).deleteMetadataItem(metadataItemId, true);
    verify(listenerService, times(1)).broadcast(NOTIFICATION_READ_EVENT_NAME, spaceWebNotificationItem, userIdentityId);

  }

}
