/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.service.test;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesTest;
import org.exoplatform.social.rest.impl.comment.CommentRestResourcesTest;
import org.exoplatform.social.rest.impl.identity.IdentityRestResourcesTest;
import org.exoplatform.social.rest.impl.relationship.RelationshipsRestResourcesTest;
import org.exoplatform.social.rest.impl.space.SpaceRestResourcesTest;
import org.exoplatform.social.rest.impl.spacemembership.SpaceMembershipRestResourcesTest;
import org.exoplatform.social.rest.impl.spacesadministration.SpacesAdministrationRestResourcesTest;
import org.exoplatform.social.rest.impl.userrelationship.UsersRelationshipsRestResourcesTest;
import org.exoplatform.social.rest.impl.users.UserRestResourcesTest;
import org.exoplatform.social.service.rest.ActivitiesRestServiceTest;
import org.exoplatform.social.service.rest.GroupSpaceBindingRestServiceTest;
import org.exoplatform.social.service.rest.IdentityRestServiceTest;
import org.exoplatform.social.service.rest.notification.IntranetNotificationsRestServiceTest;
import org.exoplatform.social.service.rest.LinkShareRestServiceTest;
import org.exoplatform.social.service.rest.NotificationsRestServiceTest;
import org.exoplatform.social.service.rest.RestCheckerTest;
import org.exoplatform.social.service.rest.SecurityManagerTest;
import org.exoplatform.social.service.rest.SpaceRestServiceTest;
import org.exoplatform.social.service.rest.UtilTest;
import org.exoplatform.social.service.rest.api.ActivityResourcesTest;
import org.exoplatform.social.service.rest.api.ActivityStreamResourcesTest;
import org.exoplatform.social.service.rest.api.IdentityResourcesTest;
import org.exoplatform.social.service.rest.api.VersionResourcesTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  SpaceRestServiceTest.class,
  ActivityResourcesTest.class,
  ActivityStreamResourcesTest.class,
  IdentityResourcesTest.class,
  VersionResourcesTest.class,
  ActivitiesRestServiceTest.class,
  IdentityRestServiceTest.class,
  LinkShareRestServiceTest.class,
  //PeopleRestServiceTest.class,  //skipped until add integration test
  RestCheckerTest.class,
  SecurityManagerTest.class,
  UtilTest.class,
  IntranetNotificationsRestServiceTest.class,
  NotificationsRestServiceTest.class,
  ActivityRestResourcesTest.class,
  CommentRestResourcesTest.class,
  IdentityRestResourcesTest.class,
  RelationshipsRestResourcesTest.class,
  SpaceRestResourcesTest.class,
  SpaceMembershipRestResourcesTest.class,
  SpacesAdministrationRestResourcesTest.class,
  UsersRelationshipsRestResourcesTest.class,
  UserRestResourcesTest.class,
  GroupSpaceBindingRestServiceTest.class
  })
@ConfigTestCase(AbstractServiceTest.class)
public class InitContainerTestSuite extends BaseExoContainerTestSuite {
  
  @BeforeClass
  public static void setUp() throws Exception {
    initConfiguration(InitContainerTestSuite.class);
    beforeSetup();
  }

  @AfterClass
  public static void tearDown() {
    afterTearDown();
  }
}
