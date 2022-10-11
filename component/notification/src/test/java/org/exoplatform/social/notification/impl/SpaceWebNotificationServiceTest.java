package org.exoplatform.social.notification.impl;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertThrows;

public class SpaceWebNotificationServiceTest extends AbstractCoreTest {

  @Test
  public void testMarkAsReadAndUnread() throws Exception {

    ExoSocialActivity activity = makeActivity(rootIdentity, "test activity");
    SpaceWebNotificationItem notificationItem = new SpaceWebNotificationItem("activity", activity.getId(), 1l, 1l);
    MetadataKey metadataKey = new MetadataKey("unread", rootIdentity.getId(), Long.valueOf(rootIdentity.getId()));
    MetadataObject metadataObject = new MetadataObject(notificationItem.getApplicationName(),
                                                       notificationItem.getApplicationItemId(),
                                                       null,
                                                       notificationItem.getSpaceId());
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    assertEquals(0, metadataItems.size());
    spaceWebNotificationService.markAsUnread(notificationItem);
    metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    assertEquals(1, metadataItems.size());
    Assert.assertEquals("unread", metadataItems.get(0).getMetadata().getType().getName());
    Assert.assertEquals(activity.getId(), metadataItems.get(0).getObject().getId());

    assertThrows(IllegalArgumentException.class, () -> spaceWebNotificationService.markAsRead(null));

    spaceWebNotificationService.markAsRead(notificationItem);
    metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    assertEquals(0, metadataItems.size());
  }
}
