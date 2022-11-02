package org.exoplatform.social.notification.web.template;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.ActivitySpaceWebNotificationPlugin;
import org.mockito.Mockito;

public class ActivitySpaceWebNotificationBuilderTest extends AbstractPluginTest {

  private static ActivitySpaceWebNotificationPlugin activitySpaceWebNotificationPlugin;

  private static ChannelManager                     channelManager;

  private static IdentityManager                    identityManager;

  private static ActivityManager                    activityManager;

  private static InitParams                         initParam;

  @Override
  public void beforeClass() {
    identityManager = Mockito.mock(IdentityManager.class);
    activityManager = Mockito.mock(ActivityManager.class);
    channelManager = Mockito.mock(ChannelManager.class);
    initParam = Mockito.mock(InitParams.class);
    activitySpaceWebNotificationPlugin = new ActivitySpaceWebNotificationPlugin(activityManager, identityManager, initParam);
  }

  @Override
  protected void setUp() throws Exception {
    //super.setUp();
    Mockito.reset(activityManager, identityManager, initParam);
    channelManager = getService(ChannelManager.class);
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    AbstractChannel channel = channelManager.getChannel(ChannelKey.key(WebChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivitySpaceWebNotificationPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(ActivitySpaceWebNotificationPlugin.ID));
  }

  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(ActivitySpaceWebNotificationPlugin.ID));
  }

  public void testGetSpaceApplicationItem() {

  }

}
