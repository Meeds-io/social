package org.exoplatform.social.core.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ActivityAttachmentPluginTest {

  private ActivityAttachmentPlugin activityAttachmentplugin;

  @Mock
  private ActivityManager          activityManager;

  @Before
  public void setUp() throws Exception {
    activityAttachmentplugin = new ActivityAttachmentPlugin(activityManager);
  }

  @Test
  public void testHasAccessPermission() throws ObjectNotFoundException {
    String activityId = "10";
    org.exoplatform.services.security.Identity userIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    when(activityManager.getActivity(activityId)).thenReturn(activity);
    when(activityManager.isActivityEditable(activity, userIdentity)).thenReturn(true);
    assertTrue(activityAttachmentplugin.hasEditPermission(userIdentity, activityId));
  }

  @Test
  public void testHasEditPermission() throws ObjectNotFoundException {
    String activityId = "10";
    org.exoplatform.services.security.Identity userIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    when(activityManager.getActivity(activityId)).thenReturn(activity);
    when(activityManager.isActivityEditable(activity, userIdentity)).thenReturn(true);
    assertTrue(activityAttachmentplugin.hasEditPermission(userIdentity, activityId));
  }

}
