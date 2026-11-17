/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.notification.plugin;

import junit.framework.TestCase;
import org.exoplatform.commons.notification.NotificationUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialNotificationUtilsTest extends TestCase {

    public void testProcessImageTitle() {
        String chromeNotification = "<p>test text for notification with Image</p><p></p><p><img alt=\"\" class=\"pull-left\" data-plugin-name=\"selectImage\" referrerpolicy=\"no-referrer\" src=\"/portal/rest/images/repository/collaboration/054bd7057f0001012279c31b78a3bfdb\" /> </p>\n" +
                "<p> here is another image</p>\n" +
                "<p><img alt=\"\" class=\"pull-left\" data-plugin-name=\"selectImage\" referrerpolicy=\"no-referrer\" src=\"/portal/rest/images/repository/collaboration/054bd72f7f000101419e3a68603fdd72\" /></p>";
        String edgeNotification = "<p>A first image from Edge <img class=\"pull-left\" alt=\"\" src=\"/portal/rest/images/repository/collaboration/04ba00e37f0001015e7c15dc245675e8\" referrerpolicy=\"no-referrer\" data-plugin-name=\"selectImage\" /></p>\n" +
                "<p> here is another image</p>\n" +
                "<img class=\"pull-left\" alt=\"\" src=\"/portal/rest/images/repository/collaboration/04ba00e37f0001015e7c15dc245675e8\" referrerpolicy=\"no-referrer\" data-plugin-name=\"selectImage\" />";
        String placeHolder = "Image Inline";
        String chromeNotificationProcessed = "<p>test text for notification with Image</p><p></p><p><i> [Image Inline] </i> </p>\n" +
                "<p> here is another image</p>\n" +
                "<p><i> [Image Inline] </i></p>";
        String edgeNotificationProcessed = "<p>A first image from Edge <i> [Image Inline] </i></p>\n" +
                "<p> here is another image</p>\n" +
                "<i> [Image Inline] </i>";
        assertEquals(SocialNotificationUtils.processImageTitle(chromeNotification, placeHolder), chromeNotificationProcessed);
        assertEquals(SocialNotificationUtils.processImageTitle(edgeNotification, placeHolder), edgeNotificationProcessed);
    }

    public void testAttachedFileLabelIsOnlyShownWhenThereIsNoText() {
        org.exoplatform.social.core.activity.model.ExoSocialActivityImpl comment = new org.exoplatform.social.core.activity.model.ExoSocialActivityImpl();
        comment.setId("12");
        assertEquals("a text is quoted as it is", "<p>Hello</p>", SocialNotificationUtils.withAttachedFiles("<p>Hello</p>", comment, "en"));
        assertEquals("an entity is a text", "&nbsp;x", SocialNotificationUtils.withAttachedFiles("&nbsp;x", comment, "en"));
        assertEquals("no activity, no files", "", SocialNotificationUtils.withAttachedFiles("", null, "en"));
        assertEquals("an inline image is not waited for", "<p><img src=\"x\"></p>", SocialNotificationUtils.withAttachedFiles("<p><img src=\"x\"></p>", comment, "en"));
        java.util.Map<String, String> params = new java.util.HashMap<>();
        params.put("docTitle", "budget.pdf|@|a<b>.png");
        comment.setTemplateParams(params);
        try (org.mockito.MockedStatic<org.exoplatform.commons.notification.template.TemplateUtils> templates =
            org.mockito.Mockito.mockStatic(org.exoplatform.commons.notification.template.TemplateUtils.class)) {
            templates.when(() -> org.exoplatform.commons.notification.template.TemplateUtils.getResourceBundle(org.mockito.ArgumentMatchers.eq("Notification.label.AttachedFile"),
                                                                                                                 org.mockito.ArgumentMatchers.any(java.util.Locale.class),
                                                                                                                 org.mockito.ArgumentMatchers.eq("locale.social.Webui")))
                     .thenReturn("Attached file");
            long start = System.currentTimeMillis();
            assertEquals("only the generic label, without file names", "<i> [Attached file] </i>", SocialNotificationUtils.withAttachedFiles("<p></p>", comment, "en"));
            assertTrue("nothing is read or waited for", System.currentTimeMillis() - start < 1000);
        }
        assertEquals("a text wins over the files", "Hi", SocialNotificationUtils.withAttachedFiles("Hi", comment, "en"));
    }
}
