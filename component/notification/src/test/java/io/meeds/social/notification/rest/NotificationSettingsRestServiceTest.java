/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.notification.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.mockito.ArgumentMatcher;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import io.meeds.social.notification.rest.model.UserNotificationSettings;

import jakarta.servlet.http.HttpServletRequest;

import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.test.mock.MockHttpServletRequest;
import org.exoplatform.settings.jpa.JPAUserSettingServiceImpl;
import org.exoplatform.social.service.rest.BaseRestServicesTestCase;

public class NotificationSettingsRestServiceTest extends BaseRestServicesTestCase { // NOSONAR

  private static final String       USER_1            = "testuser1";

  private static final String       USER_2            = "testuser2";

  private static final String       CHANNEL_ID        = "channelId";

  private static final String       GROUP_PROVIDER_ID = "groupId";

  private static final String       PLUGIN_ID         = "pluginId";

  private static final PluginInfo   PLUGIN_PROVIDER   = new PluginInfo();

  private static final PluginConfig PLUGIN_CONFIG     = new PluginConfig();
  static {
    PLUGIN_PROVIDER.setType(PLUGIN_ID);
    PLUGIN_CONFIG.setPluginId(PLUGIN_ID);
    PLUGIN_CONFIG.setGroupId(GROUP_PROVIDER_ID);
  }

  private static final List<PluginInfo> PLUGINS        = Collections.singletonList(PLUGIN_PROVIDER);

  private static final GroupProvider    GROUP_PROVIDER = new GroupProvider(GROUP_PROVIDER_ID);
  static {
    GROUP_PROVIDER.setPluginInfos(PLUGINS);
  }

  private static final List<GroupProvider>   GROUPS   = Collections.singletonList(GROUP_PROVIDER);

  private static final List<GroupProvider>   EMPTY_GROUP_PROVIDER =
                                                                  Collections.singletonList(new GroupProvider(GROUP_PROVIDER_ID));

  private static final List<AbstractChannel> CHANNELS = Collections.singletonList(newChannel());

  private ChannelManager                     channelManager;

  private UserSettingService                 userSettingService;

  private UserACL                            userACL;

  private PluginSettingService               pluginSettingService;

  @Override
  protected Class<?> getComponentClass() {
    return NotificationSettingsRestService.class;
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    pluginSettingService = mock(PluginSettingService.class);
    channelManager = mock(ChannelManager.class);
    userSettingService = mock(UserSettingService.class);
    userACL = mock(UserACL.class);

    when(userACL.isSuperUser()).thenReturn(false);
    when(userACL.getAdminGroups()).thenReturn("admins");

    when(channelManager.getChannels()).thenReturn(CHANNELS);

    when(pluginSettingService.getGroupPlugins()).thenReturn(GROUPS);
    when(pluginSettingService.getPluginConfig(eq(PLUGIN_ID))).thenReturn(PLUGIN_CONFIG);
    when(pluginSettingService.isChannelActive(CHANNEL_ID)).thenReturn(true);

    UserSetting userSetting = new UserSetting();
    userSetting.setChannelActive(CHANNEL_ID);
    userSetting.setEnabled(true);
    userSetting.setUserId(USER_1);
    userSetting.setChannelPlugins(CHANNEL_ID, Collections.singletonList(PLUGIN_ID));
    when(userSettingService.get(eq(USER_1))).thenReturn(userSetting);

    getContainer().unregisterComponent(PluginSettingService.class);
    getContainer().unregisterComponent(ChannelManager.class);
    getContainer().unregisterComponent(UserSettingService.class);
    getContainer().unregisterComponent(JPAUserSettingServiceImpl.class);
    getContainer().unregisterComponent(UserACL.class);

    getContainer().registerComponentInstance(PluginSettingService.class.getName(), pluginSettingService);
    getContainer().registerComponentInstance(ChannelManager.class.getName(), channelManager);
    getContainer().registerComponentInstance(UserSettingService.class.getName(), userSettingService);
    getContainer().registerComponentInstance(UserACL.class.getName(), userACL);
  }

  @Override
  public void tearDown() throws Exception {
    getContainer().unregisterComponent(PluginSettingService.class.getName());
    getContainer().unregisterComponent(ChannelManager.class.getName());
    getContainer().unregisterComponent(UserSettingService.class.getName());
    getContainer().unregisterComponent(UserACL.class.getName());
    super.tearDown();
  }

  public void testUnauthorizedNotSameUserGetSettings() throws Exception {
    // Given
    String path = getPath(USER_1, "");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "GET",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_2);

    // When
    ContainerResponse resp = launcher.service("GET",
                                              path,
                                              "",
                                              null,
                                              null,
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 401, resp.getStatus()); // NOSONAR
  }

  public void testAdminGetSettings() throws Exception {
    // Given
    String path = getPath(USER_1, "");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "GET",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_2);
    when(userACL.isUserInGroup(eq("admins"))).thenReturn(true);

    // When
    ContainerResponse resp = launcher.service("GET",
                                              path,
                                              "",
                                              null,
                                              null,
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 200, resp.getStatus());
    UserNotificationSettings notificationSettings = (UserNotificationSettings) resp.getEntity();
    assertNotNull(notificationSettings);
  }

  public void testGetSettingsSameUser() throws Exception {
    // Given
    String path = getPath(USER_1, "");
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "GET",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_1);

    // When
    ContainerResponse resp = launcher.service("GET",
                                              path,
                                              "",
                                              null,
                                              null,
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 200, resp.getStatus());
    UserNotificationSettings notificationSettings = (UserNotificationSettings) resp.getEntity();
    assertNotNull(notificationSettings);
    assertNotNull(notificationSettings.getChannels());
    assertEquals(1, notificationSettings.getChannels().size());
    assertNotNull(notificationSettings.getGroups());
    assertEquals(1, notificationSettings.getGroups().size());
    assertNotNull(notificationSettings.getGroups().get(0));
    assertEquals(GROUP_PROVIDER_ID, notificationSettings.getGroups().get(0).getGroupId());
    assertNotNull(notificationSettings.getGroups().get(0).getGroupId());
    assertNotNull(notificationSettings.getChannelStatus());
    assertEquals(1, notificationSettings.getChannelStatus().size());
    assertTrue(notificationSettings.getChannelStatus().get(CHANNEL_ID));
  }

  public void testGetSettingsSameUserEmptyPlugins() throws Exception {
    // Given
    when(pluginSettingService.getGroupPlugins()).thenReturn(EMPTY_GROUP_PROVIDER);

    // Ensure no error is raised
    testGetSettingsSameUser();
  }

  public void testSaveDisableChannel() throws Exception {
    // Given
    String path = getPath(USER_1, "channel/" + CHANNEL_ID);
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "PATCH",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_1);

    // When
    ContainerResponse resp = launcher.service("PATCH",
                                              path,
                                              "",
                                              getFormHeaders(),
                                              ("enable=false").getBytes(),
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 204, resp.getStatus());
    verify(userSettingService, times(1)).save(any());
  }

  public void testSaveSettings() throws Exception { // NOSONAR
    // Given
    String path = getPath(USER_1, "plugin/" + PLUGIN_ID);
    MockHttpServletRequest httpRequest = new MockHttpServletRequest(path,
                                                                    null,
                                                                    0,
                                                                    "PATCH",
                                                                    null);

    EnvironmentContext envctx = new EnvironmentContext();
    envctx.put(HttpServletRequest.class, httpRequest);

    startSessionAs(USER_1);

    // When
    ContainerResponse resp = launcher.service("PATCH",
                                              path,
                                              "",
                                              getFormHeaders(),
                                              ("channels=" + CHANNEL_ID + "=true&digest=Weekly").getBytes(),
                                              envctx);

    // Then
    assertEquals(String.valueOf(resp.getEntity()), 204, resp.getStatus());
    verify(userSettingService, times(1)).save(argThat(userSetting -> 
        userSetting.isActive(CHANNEL_ID, PLUGIN_ID) && userSetting.isChannelGloballyActive(CHANNEL_ID)
            && userSetting.isInWeekly(PLUGIN_ID)));
  }

  private MultivaluedMap<String, String> getFormHeaders() {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    return headers;
  }

  private String getPath(String username, String prefix) {
    return "/notifications/settings/" + username + "/" + prefix;
  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  private static AbstractChannel newChannel() {
    return new AbstractChannel() {

      @Override
      public void registerTemplateProvider(TemplateProvider provider) {
        throw new UnsupportedOperationException();
      }

      @Override
      protected AbstractTemplateBuilder getTemplateBuilderInChannel(PluginKey key) {
        throw new UnsupportedOperationException();
      }

      @Override
      public ChannelKey getKey() {
        return ChannelKey.key(CHANNEL_ID);
      }

      @Override
      public String getId() {
        return CHANNEL_ID;
      }

      @Override
      public void dispatch(NotificationContext ctx, String userId) {
        throw new UnsupportedOperationException();
      }
    };
  }
}
