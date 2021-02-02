package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.notification.plugin.DlpAdminDetectedItemPlugin;
import org.exoplatform.social.notification.plugin.DlpUserDetectedItemPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class PositiveItemDlpListener extends Listener<DlpPositiveItemService, String> {

    /**
     * dlp positive item detected
     *
     * @param event
     */
    @Override
    public void onEvent(Event<DlpPositiveItemService, String> event) {
        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.DLP_DETECTED_ITEM_ID, event.getData());
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(DlpUserDetectedItemPlugin.ID)))
                                     .with(ctx.makeCommand(PluginKey.key(DlpAdminDetectedItemPlugin.ID)))
                                     .execute(ctx);
    }
}
