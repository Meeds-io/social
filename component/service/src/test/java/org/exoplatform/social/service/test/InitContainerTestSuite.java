/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.service.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesTest;
import org.exoplatform.social.rest.impl.comment.CommentRestResourcesTest;
import org.exoplatform.social.rest.impl.favorite.FavoriteRestTest;
import org.exoplatform.social.rest.impl.identity.IdentityRestResourcesTest;
import org.exoplatform.social.rest.impl.relationship.RelationshipsRestResourcesTest;
import org.exoplatform.social.rest.impl.site.SiteRestTest;
import org.exoplatform.social.rest.impl.space.SpaceRestResourcesTest;
import org.exoplatform.social.rest.impl.spacemembership.SpaceMembershipRestResourcesTest;
import org.exoplatform.social.rest.impl.spacesadministration.SpacesAdministrationRestResourcesTest;
import org.exoplatform.social.rest.impl.users.UserRestResourcesTest;
import org.exoplatform.social.service.rest.GroupSpaceBindingRestServiceTest;
import org.exoplatform.social.service.rest.IdentityRestServiceTest;
import org.exoplatform.social.service.rest.NotificationsRestServiceTest;
import org.exoplatform.social.service.rest.RestCheckerTest;
import org.exoplatform.social.service.rest.UtilTest;
import org.exoplatform.social.service.rest.api.VersionResourcesTest;
import org.exoplatform.social.service.rest.notification.IntranetNotificationsRestServiceTest;

import io.meeds.social.link.rest.LinkRestTest;
import io.meeds.social.observer.rest.ObserverRestTest;
import io.meeds.social.translation.rest.TranslationRestResourcesTest;

@RunWith(Suite.class)
@SuiteClasses({
  VersionResourcesTest.class,
  IdentityRestServiceTest.class,
  //PeopleRestServiceTest.class,  //skipped until add integration test
  RestCheckerTest.class,
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
  UserRestResourcesTest.class,
  GroupSpaceBindingRestServiceTest.class,
  FavoriteRestTest.class,
  TranslationRestResourcesTest.class,
  ObserverRestTest.class,
  LinkRestTest.class,
  SiteRestTest.class,
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
