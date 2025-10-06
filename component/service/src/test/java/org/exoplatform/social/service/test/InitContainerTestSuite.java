/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.service.test;

import io.meeds.social.security.rest.LoginRestTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesTest;
import org.exoplatform.social.rest.impl.favorite.FavoriteRestTest;
import org.exoplatform.social.rest.impl.identity.IdentityRestResourcesTest;
import org.exoplatform.social.rest.impl.relationship.RelationshipsRestResourcesTest;
import org.exoplatform.social.rest.impl.site.SiteRestTest;
import org.exoplatform.social.rest.impl.space.SpaceRestResourcesTest;
import org.exoplatform.social.rest.impl.spacemembership.SpaceMembershipRestResourcesTest;
import org.exoplatform.social.rest.impl.users.UserRestResourcesTest;
import org.exoplatform.social.service.rest.GroupSpaceBindingRestServiceTest;
import org.exoplatform.social.service.rest.NotificationsRestServiceTest;
import org.exoplatform.social.service.rest.RestCheckerTest;
import org.exoplatform.social.service.rest.UtilTest;
import org.exoplatform.social.service.rest.api.VersionResourcesTest;
import org.exoplatform.social.service.rest.notification.IntranetNotificationsRestServiceTest;

import io.meeds.social.category.rest.CategoryLinkRestTest;
import io.meeds.social.category.rest.CategoryRestTest;
import io.meeds.social.cms.rest.ContentLinkRestTest;
import io.meeds.social.observer.rest.ObserverRestTest;
import io.meeds.social.resource.rest.SkinRestTest;
import io.meeds.social.translation.rest.TranslationRestResourcesTest;

@RunWith(Suite.class)
@SuiteClasses({
  VersionResourcesTest.class,
  RestCheckerTest.class,
  UtilTest.class,
  IntranetNotificationsRestServiceTest.class,
  NotificationsRestServiceTest.class,
  ActivityRestResourcesTest.class,
  IdentityRestResourcesTest.class,
  RelationshipsRestResourcesTest.class,
  SpaceRestResourcesTest.class,
  SpaceMembershipRestResourcesTest.class,
  UserRestResourcesTest.class,
  GroupSpaceBindingRestServiceTest.class,
  FavoriteRestTest.class,
  TranslationRestResourcesTest.class,
  ObserverRestTest.class,
  SkinRestTest.class,
  SiteRestTest.class,
  CategoryRestTest.class,
  CategoryLinkRestTest.class,
  ContentLinkRestTest.class,
  LoginRestTest.class,
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
