/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.channel.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.EditActivityPlugin;

public class EditActivityMailBuilderTest extends AbstractPluginTest {
    private ChannelManager manager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = getService(ChannelManager.class);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Override
    public AbstractTemplateBuilder getTemplateBuilder() {
        AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
        assertTrue(channel != null);
        assertTrue(channel.hasTemplateBuilder(PluginKey.key(EditActivityPlugin.ID)));
        return channel.getTemplateBuilder(PluginKey.key(EditActivityPlugin.ID));
    }

    @Override
    public BaseNotificationPlugin getPlugin() {
        return pluginService.getPlugin(PluginKey.key(EditActivityPlugin.ID));
    }

    public void testSimpleCase() throws Exception {
        //STEP 1 post activity
        ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
        makeComment(activity, demoIdentity, "comment");
        activity.setTitle("edited activity");

        //STEP 2 Edit activity
        activityManager.updateActivity(activity);

        assertMadeMailDigestNotifications(1);
        List<NotificationInfo> list = assertMadeMailDigestNotifications(demoIdentity.getRemoteId(), 1);
        NotificationInfo notification = list.get(0);

        //STEP 3 assert Message info
        NotificationContext ctx = NotificationContextImpl.cloneInstance();
        ctx.setNotificationInfo(notification.setTo("root"));
        MessageInfo info = buildMessageInfo(ctx);

        assertSubject(info, "Notification.subject.EditActivityPlugin");
        assertBody(info, "edited activity");
    }

    public void testDigest() throws Exception {
        ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
        makeComment(activity, demoIdentity, "comment");
        activity.setTitle("edited activity");

        //STEP 2 Edit activity
        activityManager.updateActivity(activity);

        //
        assertMadeMailDigestNotifications(1);
        List<NotificationInfo> list = assertMadeMailDigestNotifications(demoIdentity.getRemoteId(), 1);

        NotificationContext ctx = NotificationContextImpl.cloneInstance();
        list.set(0, list.get(0).setTo(demoIdentity.getRemoteId()));
        ctx.setNotificationInfos(list);
        Writer writer = new StringWriter();
        buildDigest(ctx, writer);
        assertDigest(writer, "Root Root edited activity:edited activity");
    }
}
