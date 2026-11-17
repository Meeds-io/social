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
package org.exoplatform.social.notification.plugin.child;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.mockito.MockedStatic;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.mail.MailPalette;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

/**
 * The activity a notification mail shows is rendered in a template context of
 * its own: the recipient's palette is bound to it, so the content follows the
 * recipient's display mode.
 */
public class DefaultActivityChildPluginTest {

  @Test
  public void theActivityIsRenderedInTheRecipientsPalette() {
    BrandingService brandingService = mock(BrandingService.class);
    when(brandingService.getDisplayMode("mary")).thenReturn("dark");
    when(brandingService.getMailPalette(true)).thenReturn(new HashMap<>(Map.of("surface", "#1E2328")));
    ActivityManager activityManager = mock(ActivityManager.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(activityManager.getActivity("12")).thenReturn(activity);
    when(activityManager.getActivityTitle(activity)).thenReturn("<p style=\"color:#000000\">black</p>");
    NotificationInfo notification = NotificationInfo.instance().setTo("mary");
    notification.with(SocialNotificationUtils.ACTIVITY_ID.getKey(), "12");
    NotificationContext ctx = mock(NotificationContext.class);
    when(ctx.getNotificationInfo()).thenReturn(notification);
    AtomicReference<TemplateContext> rendered = new AtomicReference<>();

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class);
         MockedStatic<Utils> utils = mockStatic(Utils.class);
         MockedStatic<NotificationPluginUtils> pluginUtils = mockStatic(NotificationPluginUtils.class);
         MockedStatic<MentionUtils> mentionUtils = mockStatic(MentionUtils.class);
         MockedStatic<TemplateUtils> templateUtils = mockStatic(TemplateUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(BrandingService.class)).thenReturn(brandingService);
      utils.when(Utils::getActivityManager).thenReturn(activityManager);
      pluginUtils.when(() -> NotificationPluginUtils.getLanguage("mary")).thenReturn("en");
      mentionUtils.when(() -> MentionUtils.substituteRoleWithLocale(anyString(), any())).thenAnswer(call -> call.getArgument(0));
      templateUtils.when(() -> TemplateUtils.processGroovy(any(TemplateContext.class))).thenAnswer(call -> {
        rendered.set(call.getArgument(0));
        return "";
      });

      new DefaultActivityChildPlugin(new InitParams()).makeContent(ctx);
    }

    TemplateContext templateContext = rendered.get();
    assertEquals("dark", MailPalette.of(templateContext).get(MailPalette.MODE));
    assertEquals("#1E2328", MailPalette.of(templateContext).get("surface"));
    assertEquals("a black text is left to the palette's text colour", "<p>black</p>", templateContext.get("ACTIVITY"));
  }

}
