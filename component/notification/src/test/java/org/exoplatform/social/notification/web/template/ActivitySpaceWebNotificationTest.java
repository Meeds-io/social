package org.exoplatform.social.notification.web.template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.ActivitySpaceWebNotificationPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

@RunWith(MockitoJUnitRunner.class)
public class ActivitySpaceWebNotificationTest {

  private static final String                       ACTIVITY_NOTIFICATION_PLUGIN_ID = "ACTIVITY_NOTIFICATION";

  private static final String                       USERNAME                        = "username";

  @Mock
  private IdentityManager                           identityManager;

  @Mock
  private ActivityManager                           activityManager;

  @Mock
  private InitParams                                initParams;

  private static ActivitySpaceWebNotificationPlugin activitySpaceWebNotificationPlugin;

  @Before
  public void setUp() throws Exception {
    ValuesParam pluginIdsValues = new ValuesParam();
    pluginIdsValues.setValues(Arrays.asList(ACTIVITY_NOTIFICATION_PLUGIN_ID));
    when(initParams.getValuesParam("notification.plugin.ids")).thenReturn(pluginIdsValues);
    activitySpaceWebNotificationPlugin = new ActivitySpaceWebNotificationPlugin(activityManager, identityManager, initParams);
  }

  @Test
  public void testGetSpaceApplicationItem() {
    NotificationInfo notificationInfo = mock(NotificationInfo.class);
    Identity userIdentity = mock(Identity.class);
    String activityId = "activityId";
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    MetadataObject metadataObject = mock(MetadataObject.class);
    long spaceId = 12l;
    long userIdentityId = 15l;
    String metadataObjectType = ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;
    String metadataObjectId = activityId;

    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(userIdentity);
    when(userIdentity.getId()).thenReturn(String.valueOf(userIdentityId));
    when(notificationInfo.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey())).thenReturn(activityId);
    when(activityManager.getActivity(activityId)).thenReturn(activity);
    when(activity.getMetadataObject()).thenReturn(metadataObject);
    when(metadataObject.getType()).thenReturn(metadataObjectType);
    when(metadataObject.getId()).thenReturn(metadataObjectId);
    when(metadataObject.getSpaceId()).thenReturn(spaceId);

    SpaceWebNotificationItem spaceApplicationItem = activitySpaceWebNotificationPlugin.getSpaceApplicationItem(notificationInfo, USERNAME);
    assertNotNull(spaceApplicationItem);
    assertEquals(activityId, spaceApplicationItem.getApplicationItemId());
    assertEquals(metadataObjectType, spaceApplicationItem.getApplicationName());
    assertEquals(spaceId, spaceApplicationItem.getSpaceId());
    assertEquals(userIdentityId, spaceApplicationItem.getUserId());
  }

}
