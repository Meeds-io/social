/*
 * Copyright (C) 2020 eXo Platform SAS.
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
package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;

public class DlpUserRestoredItemPlugin extends BaseNotificationPlugin {

    public static final String ID = "DlpUserRestoredItemPlugin";

    
    public DlpUserRestoredItemPlugin(InitParams initParams) {
        super(initParams);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public NotificationInfo makeNotification(NotificationContext ctx) {
        String dlpItemTitle = ctx.value(SocialNotificationUtils.DLP_RESTORED_ITEM_TITLE);
        String dlpItemAuthor = ctx.value(SocialNotificationUtils.DLP_RESTORED_ITEM_AUTHOR);
        return NotificationInfo.instance().key(getId())
                .with("itemTitle", dlpItemTitle)
                .to(dlpItemAuthor);
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
        return true;
    }
}