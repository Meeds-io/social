package org.exoplatform.social.notification.channel;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class SpaceWebChannelTest extends AbstractCoreTest {

  private SpaceWebChannel             spaceWebChannel;

  private SpaceWebNotificationService spaceWebNotificationService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceWebNotificationService = mock(SpaceWebNotificationService.class);
    spaceWebChannel = new SpaceWebChannel(spaceWebNotificationService);
  }

  @Test
  public void testGetId() {
    assertNotNull(spaceWebChannel.getId());
    assertEquals("SPACE_WEB_CHANNEL", spaceWebChannel.getId());
  }

  @Test
  public void testGetKey() {
    assertNotNull(spaceWebChannel.getKey());
    assertEquals(ChannelKey.key("SPACE_WEB_CHANNEL"), spaceWebChannel.getKey());
  }

  @Test
  public void testGetTemplateBuilderInChannel() {
    assertNull(spaceWebChannel.getTemplateBuilderInChannel(null));
    assertNull(spaceWebChannel.getTemplateBuilderInChannel(PluginKey.key("test")));
  }

  @Test
  public void testDispatch() throws Exception {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    spaceWebChannel.dispatch(ctx, "root");
    verify(spaceWebNotificationService, times(1)).dispatch(any(), anyString());
    doThrow(Exception.class).when(spaceWebNotificationService).dispatch(any(), anyString());
    spaceWebChannel.dispatch(ctx, "root");
    verify(spaceWebNotificationService, times(2)).dispatch(any(), anyString());
  }

}
