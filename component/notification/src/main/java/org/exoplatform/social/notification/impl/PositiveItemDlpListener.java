package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.dlp.domain.DlpPositiveItemEntity;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.notification.plugin.DlpAdminDetectedItemPlugin;
import org.exoplatform.social.notification.plugin.DlpUserDetectedItemPlugin;
import org.exoplatform.social.notification.plugin.DlpUserRestoredItemPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class PositiveItemDlpListener extends Listener<DlpPositiveItemService, Object> {

    public static final String DLP_DETECT_ITEM_EVENT = "dlp.listener.event.detect.item";

    public static final String DLP_RESTORE_ITEM_EVENT = "dlp.listener.event.restore.item";

    /**
     * dlp positive item detected/restored
     *
     * @param event
     */
    @Override
    public void onEvent(Event<DlpPositiveItemService, Object> event) {
        if (DLP_DETECT_ITEM_EVENT.equals(event.getEventName())) {
            NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.DLP_DETECTED_ITEM_ID, event.getData());
            ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(DlpUserDetectedItemPlugin.ID)))
                                         .with(ctx.makeCommand(PluginKey.key(DlpAdminDetectedItemPlugin.ID)))
                                         .execute(ctx);
        } else if (DLP_RESTORE_ITEM_EVENT.equals(event.getEventName())) {
            NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                         .append(SocialNotificationUtils.DLP_RESTORED_ITEM_TITLE, ((DlpPositiveItemEntity) event.getData()).getTitle())
                                         .append(SocialNotificationUtils.DLP_RESTORED_ITEM_AUTHOR, ((DlpPositiveItemEntity) event.getData()).getAuthor());
            ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(DlpUserRestoredItemPlugin.ID)))
                                         .execute(ctx);
        }
    }
}
