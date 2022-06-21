/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification;

import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.mock.MockNotificationService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.notification-dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.notification-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.notification-local-configuration.xml"),
})
public abstract class AbstractCoreTest extends BaseExoTestCase {
  protected IdentityManager identityManager;
  protected ActivityManagerImpl activityManager;
  protected SpaceService spaceService;
  protected RelationshipManagerImpl relationshipManager;
  protected PluginContainer pluginService;
  protected MockNotificationService notificationService;
  protected PluginSettingService pluginSettingService;
  protected ExoFeatureService exoFeatureService;
  protected OrganizationService organizationService;

  protected Identity rootIdentity;
  protected Identity johnIdentity;
  protected Identity maryIdentity;
  protected Identity demoIdentity;
  protected Identity ghostIdentity;

  protected List<ExoSocialActivity> tearDownActivityList;
  protected List<Space>  tearDownSpaceList;
  protected List<Identity>  tearDownIdentityList;
  protected List<Relationship>  tearDownRelationshipList;
  
  @Override
  protected void setUp() throws Exception {
    begin();

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownRelationshipList = new ArrayList<Relationship>();

    identityManager = getService(IdentityManager.class);
    activityManager = getService(ActivityManagerImpl.class);
    spaceService = getService(SpaceService.class);
    relationshipManager = getService(RelationshipManagerImpl.class);
    pluginService = getService(PluginContainer.class);
    notificationService = getService(MockNotificationService.class);
    pluginSettingService = getService(PluginSettingService.class);
    exoFeatureService = getService(ExoFeatureService.class);
    organizationService = getService(OrganizationService.class);
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://exoplatform.com");
    //
    checkAndCreateDefaultUsers();

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");
    ghostIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "ghost");

    notificationService.clearAll();
  }

  @Override
  protected void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityManager.deleteActivity(activity.getId());
    }

    for (Space sp : tearDownSpaceList) {
      spaceService.deleteSpace(sp);
    }

    for (Relationship relationship : tearDownRelationshipList) {
      relationshipManager.remove(relationship);
    }

    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }

    notificationService.clearAll();

    end();
  }
  
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }
  
  private void checkAndCreateDefaultUsers() {
    //
    UserHandler handler = getService(OrganizationService.class).getUserHandler();
    String[] users = new String[] { "root", "demo", "mary", "john", "ghost"};
    try {
      for (String userName : users) {
        if (handler.findUserByName(userName) == null) {
          User user = handler.createUserInstance(userName);
          user.setPassword(userName);
          user.setFirstName(userName);
          user.setLastName(userName);
          user.setEmail(userName + "@plf.com");
          handler.createUser(user, true);
        }
        //
        handler.setEnabled(userName, true, true);
      }
    } catch (Exception e) {
      ExoLogger.getExoLogger(getClass()).debug(e);
    }
  }

  // Fork from Junit 3.8.2
  @Override
  /**
   * Override to run the test and assert its state.
   * @throws Throwable if any exception is thrown
   */
  protected void runTest() throws Throwable {
    String fName = getName();
    assertNotNull("TestCase.fName cannot be null", fName); // Some VMs crash when calling getMethod(null,null);
    Method runMethod= null;
    try {
      // use getMethod to get all public inherited
      // methods. getDeclaredMethods returns all
      // methods of this class but excludes the
      // inherited ones.
      runMethod= getClass().getMethod(fName, (Class[])null);
    } catch (NoSuchMethodException e) {
      fail("Method \""+fName+"\" not found");
    }
    if (!Modifier.isPublic(runMethod.getModifiers())) {
      fail("Method \""+fName+"\" should be public");
    }

    try {
      runMethod.invoke(this);
    }
    catch (InvocationTargetException e) {
      e.fillInStackTrace();
      throw e.getTargetException();
    }
    catch (IllegalAccessException e) {
      e.fillInStackTrace();
      throw e;
    }
    
  }

  /**
   * Makes the activity for Test Case
   * @param owner
   * @param activityTitle
   * @return
   */
  protected ExoSocialActivity makeActivity(Identity owner, String activityTitle) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(owner.getId());
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    tearDownActivityList.add(activity);

    return activity;
  }

  /**
   * Makes the activity for Test Case
   * @param owner
   * @param activityTitle
   * @return
   */
  protected ExoSocialActivity makeActivityOnStream(Identity owner, String activityTitle) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(owner.getId());
    activityManager.saveActivityNoReturn(activity);
    tearDownActivityList.add(activity);

    return activity;
  }

  /**
   * Edit the activity for Test Case
   * 
   * @param activity
   * @param activityTitle
   * @return
   */
  protected ExoSocialActivity editActivity(ExoSocialActivity activity, String activityTitle) {
    activity.setTitle(activityTitle);
    activityManager.updateActivity(activity);

    return activity;
  }

  protected String getFullName(String userId) {
    Identity identity  = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId);
    assertNotNull("Can't find identity of " + userId, identity);
    assertNotNull("Can't find profile of " + userId, identity.getProfile());
    return identity.getProfile().getFullName();
  }
}
