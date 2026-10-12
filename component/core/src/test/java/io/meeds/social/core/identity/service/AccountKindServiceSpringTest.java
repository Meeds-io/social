/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.core.identity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.OrganizationService;

import io.meeds.social.core.identity.plugin.AccountKindPlugin;

/**
 * Pins how the account kind plugins reach {@link AccountKindService} in a
 * real Spring context: the service is a {@code @Service} bean, Spring hands
 * its constructor every {@link AccountKindPlugin} bean, and an empty list when
 * there is none.
 */
public class AccountKindServiceSpringTest {

  /**
   * Every plugin bean of the context is collected by the service.
   */
  @Test
  public void testThePluginBeansAreCollected() {
    AccountKindPlugin bot = plugin("bot", "/platform/bots");
    AccountKindPlugin drone = plugin("drone", "/platform/drones");
    try (AnnotationConfigApplicationContext context = newContext()) {
      context.registerBean("botAccountKindPlugin", AccountKindPlugin.class, () -> bot);
      context.registerBean("droneAccountKindPlugin", AccountKindPlugin.class, () -> drone);
      context.refresh();
      AccountKindService service = context.getBean(AccountKindService.class);
      assertEquals(2, service.getPlugins().size());
      assertSame(bot, service.getPluginByKind("bot"));
      assertSame(drone, service.getPluginByGroup("/platform/drones"));
    }
  }

  /**
   * A context without any plugin bean, as when no add-on contributes a kind,
   * builds the service with no plugin.
   */
  @Test
  public void testNoPluginBeanBuildsTheServiceWithNoPlugin() {
    try (AnnotationConfigApplicationContext context = newContext()) {
      context.refresh();
      assertTrue(context.getBean(AccountKindService.class).getPlugins().isEmpty());
    }
  }

  /**
   * The service is a {@code @Service} bean, the only kind of bean the
   * Kernel/Spring integration publishes to the kernel and to the other web
   * applications. Kills the annotation-removed mutant, which a context
   * registering the class by hand cannot see.
   */
  @Test
  public void testTheServiceIsAServiceBean() {
    assertTrue(AccountKindService.class.isAnnotationPresent(Service.class));
  }

  /**
   * Builds a context holding the service and its collaborators, not
   * refreshed yet.
   *
   * @return the context
   */
  private AnnotationConfigApplicationContext newContext() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.registerBean(OrganizationService.class, () -> mock(OrganizationService.class));
    context.registerBean(UserACL.class, () -> mock(UserACL.class));
    context.register(AccountKindService.class);
    return context;
  }

  /**
   * Builds a plugin declaring a kind and its group.
   *
   * @param kind the kind
   * @param groupId the group
   * @return the plugin
   */
  private AccountKindPlugin plugin(String kind, String groupId) {
    AccountKindPlugin plugin = mock(AccountKindPlugin.class);
    when(plugin.getKind()).thenReturn(kind);
    when(plugin.getGroupId()).thenReturn(groupId);
    return plugin;
  }

}
