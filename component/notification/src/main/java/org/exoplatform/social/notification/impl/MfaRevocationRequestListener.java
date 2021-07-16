package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.notification.plugin.MfaAdminRevocationRequestPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class MfaRevocationRequestListener extends Listener<DlpPositiveItemService, Object> {

    @Override
    public void onEvent(Event<DlpPositiveItemService, Object> event) {
        NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.MFA_REVOCATION_REQUEST_REQUESTER, event.getData());
        ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(MfaAdminRevocationRequestPlugin.ID))).execute(ctx);
    }
}
