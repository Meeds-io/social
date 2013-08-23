/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.AbstractPluginTest;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public class LikePluginTest extends AbstractPluginTest {
  
  public void testSimpleCase() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    
    //STEP 2 like activity
    activityManager.saveLike(activity, demoIdentity);
    
    List<NotificationInfo> list = assertMadeNotifications(1);
    NotificationInfo likeNotification = list.get(0);
    
    //STEP 3 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(likeNotification.setTo("root"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "Demo gtn likes one of your activities<br/>");
    assertBody(info, "New like on your activity stream");
  }
  
  public void testPluginONOFF() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, "root post an activity");
    //asserEquals = 1 because mary post activity on root stream
    assertMadeNotifications(1);
    notificationService.clearAll();
    
    //STEP 2 like activity
    activityManager.saveLike(activity, demoIdentity);
    assertMadeNotifications(1);
    notificationService.clearAll();
    
    //Turn off the plugin
    turnOFF(getPlugin());
    
    ExoSocialActivity newActivity = makeActivity(johnIdentity, "root post an activity");
    //asserEquals = 1 because mary post activity on root stream
    assertMadeNotifications(1);
    notificationService.clearAll();
    
    activityManager.saveLike(newActivity, demoIdentity);
    assertMadeNotifications(0);
    
    //turn on the plugin then turn off the feature
    turnON(getPlugin());
    
    activityManager.saveLike(newActivity, maryIdentity);
    assertMadeNotifications(1);
  }
  
  public void testFeatureONOFF() throws Exception {
    //STEP 1 post activity
    ExoSocialActivity activity = makeActivity(maryIdentity, "root post an activity");
    //asserEquals = 1 because mary post activity on root stream
    assertMadeNotifications(1);
    notificationService.clearAll();
    
    //STEP 2 like activity
    activityManager.saveLike(activity, demoIdentity);
    assertMadeNotifications(1);
    notificationService.clearAll();
    
    //turn off the feature == all plugins off
    turnFeatureOff();
    
    ExoSocialActivity newActivity = makeActivity(johnIdentity, "root post an activity");
    //postActivityPlugin off
    assertMadeNotifications(0);
    
    activityManager.saveLike(newActivity, demoIdentity);
    //likePlugin off
    assertMadeNotifications(0);
    
    //turn on the feature
    turnFeatureOn();
    
    activityManager.saveLike(newActivity, maryIdentity);
    assertMadeNotifications(1);
  }
  
  public void testDigest() throws Exception {
    ExoSocialActivity activity = makeActivity(rootIdentity, "root post an activity");
    activityManager.saveLike(activity, maryIdentity);
    activityManager.saveLike(activity, demoIdentity);
    activityManager.saveLike(activity, johnIdentity);
    //
    List<NotificationInfo> list = assertMadeNotifications(3);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    list.set(0, list.get(0).setTo(rootIdentity.getRemoteId()));
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    getPlugin().buildDigest(ctx, writer);
    assertDigest(writer, "Demo gtn, John Anthony, Mary Kelly like your activity : root post an activity.");
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(LikePlugin.ID));
  }

}
