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
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.dlp.dto.DlpPositiveItem;
import org.exoplatform.commons.dlp.service.DlpPositiveItemService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

import java.util.*;
import java.util.stream.Collectors;

public class DlpAdminDetectedItemPlugin extends BaseNotificationPlugin {

    public static final String ID = "DlpAdminDetectedItemPlugin";

    private static final Log LOGGER = ExoLogger.getExoLogger(DlpAdminDetectedItemPlugin.class);

    private DlpPositiveItemService dlpPositiveItemService;
    
    private OrganizationService organizationService;
    
    private UserACL userACL;

    private SettingService settingService;

    public DlpAdminDetectedItemPlugin(InitParams initParams, DlpPositiveItemService dlpPositiveItemService, OrganizationService organizationService, UserACL userACL, SettingService settingService) {
        super(initParams);
        this.dlpPositiveItemService = dlpPositiveItemService;
        this.organizationService = organizationService;
        this.userACL = userACL;
        this.settingService = settingService;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public NotificationInfo makeNotification(NotificationContext ctx) {
        long dlpItemId = ctx.value(SocialNotificationUtils.DLP_DETECTED_ITEM_ID);
        DlpPositiveItem dlpPositiveItem = null;
        try {
            dlpPositiveItem = dlpPositiveItemService.getDlpPositiveItemById(dlpItemId);
            return NotificationInfo.instance().key(getId())
                    .with("itemTitle", dlpPositiveItem.getTitle())
                    .to(getRecipients());
        } catch (Exception e) {
            ctx.setException(e);
        }

        return null;
    }

    @Override
    public boolean isValid(NotificationContext ctx) {
        return true;
    }

    private List<String> getRecipients() throws Exception {
        HashSet<String> allGroupsMembers = new HashSet<>();

        // Get admin members group
        ListAccess<User> adminMembersAccess = organizationService.getUserHandler().findUsersByGroupId(userACL.getAdminGroups());
        int totalAdminGroupMembersSize = adminMembersAccess.getSize();
        User[] users = adminMembersAccess.load(0, totalAdminGroupMembersSize);
        List<String> adminMembers = Arrays.stream(users)
                                          .map(User::getUserName)
                                          .collect(Collectors.toList());
        allGroupsMembers.addAll(adminMembers);

        // Get others members of groups
        SettingValue<?> settingValue = settingService.get(Context.GLOBAL, Scope.APPLICATION.id("DlpPermissions"), "exo:dlpPermissions");
        if(settingValue == null ||settingValue.getValue().toString().isEmpty()) return new ArrayList<String>(allGroupsMembers);
        List<String> permissionsList = Arrays.asList(settingValue.getValue().toString().split(","));

        for (String permission : permissionsList) {
            if(permission.equals("/platform/administrators")) continue;
            try {
                ListAccess<User> membersAccess = organizationService.getUserHandler().findUsersByGroupId(permission);
                int totalGroupMembersSize = adminMembersAccess.getSize();
                User[] membersUsers = membersAccess.load(0, totalGroupMembersSize);
                List<String> members = Arrays.stream(membersUsers)
                                             .map(User::getUserName)
                                             .collect(Collectors.toList());
                allGroupsMembers.addAll(members);
            } catch (Exception e){
                LOGGER.error("Error when getting group");
            }
        }

        return new ArrayList<String>(allGroupsMembers);
    }
}
