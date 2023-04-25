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
package org.exoplatform.social.core.jpa.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.core.jpa.storage.*;
import org.exoplatform.social.core.jpa.storage.dao.*;
import org.exoplatform.social.core.listeners.ActivityMetadataListenerTest;
import org.exoplatform.social.core.listeners.ActivityTagMetadataListenerTest;
import org.exoplatform.social.core.listeners.SocialUserProfileEventListenerImplTest;
import org.exoplatform.social.core.manager.ActivityManagerTest;
import org.exoplatform.social.core.manager.IdentityManagerTest;
import org.exoplatform.social.core.manager.RelationshipManagerTest;
import org.exoplatform.social.core.metadata.thumbnail.ImageThumbnailServiceImplTest;
import org.exoplatform.social.core.processor.I18NActivityProcessorTest;
import org.exoplatform.social.core.processor.MetadataActivityProcessorTest;
import org.exoplatform.social.core.processor.OSHtmlSanitizerProcessorTest;
import org.exoplatform.social.core.processor.TemplateParamsProcessorTest;
import org.exoplatform.social.core.profile.ProfilePropertyServiceTest;
import org.exoplatform.social.core.search.SearchServiceTest;
import org.exoplatform.social.core.search.SortingTest;
import org.exoplatform.social.core.service.LinkProviderTest;
import org.exoplatform.social.core.service.ProfileLabelServiceTest;
import org.exoplatform.social.core.space.SpaceLifeCycleTest;
import org.exoplatform.social.core.space.SpaceUtilsTest;
import org.exoplatform.social.core.space.spi.SpaceServiceTest;
import org.exoplatform.social.core.space.spi.SpaceTemplateServiceTest;
import org.exoplatform.social.metadata.MetadataServiceTest;
import org.exoplatform.social.metadata.favorite.FavoriteServiceTest;
import org.exoplatform.social.metadata.tag.TagServiceTest;

import io.meeds.social.core.richeditor.RichEditorConfigurationServiceTest;

import org.exoplatform.social.core.application.SpaceActivityPublisherTest;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingServiceTest;
import org.exoplatform.social.core.binding.spi.RDBMSGroupSpaceBindingStorageTest;

@SuiteClasses({
  ActivityDAOTest.class,
  IdentityDAOTest.class,
  StreamItemDAOTest.class,
  ActivityManagerRDBMSTest.class,
  SpaceActivityRDBMSPublisherTest.class,
  RelationshipStorageTest.class,
  RDBMSRelationshipManagerTest.class,
  IdentityStorageTest.class,
  RDBMSSpaceStorageTest.class,
  ProfileSettingStorageTest.class,
  ProfileLabelStorageTest.class,
  ProfilePropertyServiceTest.class,
  ProfileLabelServiceTest.class,
  SpaceMemberDAOTest.class,
  SpaceExternalInvitationDAOTest.class,
  SpaceDAOTest.class,
  ProfileLabelDAOTest.class,
  I18NActivityProcessorTest.class,
  SearchServiceTest.class,
  RDBMSActivityStorageImplTest.class,
  ActivityManagerTest.class,
  IdentityManagerTest.class,
  SpaceServiceTest.class,
  RelationshipManagerTest.class,
  SpaceUtilsTest.class,
  SpaceActivityPublisherTest.class,
  SpaceLifeCycleTest.class,
  SocialUserProfileEventListenerImplTest.class,
  OSHtmlSanitizerProcessorTest.class,
  TemplateParamsProcessorTest.class,
  LinkProviderTest.class,
  SpaceTemplateServiceTest.class,
  SortingTest.class,
  GroupSpaceBindingServiceTest.class,
  RDBMSGroupSpaceBindingStorageTest.class,
  MetadataServiceTest.class,
  FavoriteServiceTest.class,
  TagServiceTest.class,
  ActivityMetadataListenerTest.class,
  ActivityTagMetadataListenerTest.class,
  MetadataActivityProcessorTest.class,
  ImageThumbnailServiceImplTest.class,
  RichEditorConfigurationServiceTest.class,
})
@ConfigTestCase(AbstractCoreTest.class)
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
