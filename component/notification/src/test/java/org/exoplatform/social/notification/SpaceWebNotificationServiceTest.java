package org.exoplatform.social.notification;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.junit.BeforeClass;
import org.mockito.Mockito;

public class SpaceWebNotificationServiceTest {

  private static SpaceWebNotificationService spaceWebNotificationService;

  private static ListenerService             listenerService;

  private static MetadataService             metadataService;

  private static ExoFeatureService           exoFeatureService;

  @BeforeClass
  public void setup() {
    Mockito.reset(metadataService, exoFeatureService, listenerService);
  }

  public void testMarkAsReadActivity() {
  }

  public void testMarkAsUnreadActivity() {
  }

}
