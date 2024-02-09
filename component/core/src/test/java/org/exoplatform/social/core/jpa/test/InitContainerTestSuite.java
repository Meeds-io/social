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

import org.exoplatform.social.core.jpa.search.ComplementaryFilterSearchConnectorTest;
import org.exoplatform.social.core.upgrade.UserPasswordHashMigrationTest;
import org.exoplatform.social.core.utils.MentionUtilsTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.attachment.AttachmentServiceTest;
import org.exoplatform.social.core.application.SpaceActivityPublisherTest;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingServiceTest;
import org.exoplatform.social.core.binding.spi.RDBMSGroupSpaceBindingStorageTest;
import org.exoplatform.social.core.jpa.storage.ActivityManagerRDBMSTest;
import org.exoplatform.social.core.jpa.storage.IdentityStorageTest;
import org.exoplatform.social.core.jpa.storage.ProfileLabelStorageTest;
import org.exoplatform.social.core.jpa.storage.ProfileSettingStorageTest;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImplTest;
import org.exoplatform.social.core.jpa.storage.RDBMSRelationshipManagerTest;
import org.exoplatform.social.core.jpa.storage.RDBMSSpaceStorageTest;
import org.exoplatform.social.core.jpa.storage.RelationshipStorageTest;
import org.exoplatform.social.core.jpa.storage.SpaceActivityRDBMSPublisherTest;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAOTest;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAOTest;
import org.exoplatform.social.core.jpa.storage.dao.ProfileLabelDAOTest;
import org.exoplatform.social.core.jpa.storage.dao.SpaceDAOTest;
import org.exoplatform.social.core.jpa.storage.dao.SpaceExternalInvitationDAOTest;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAOTest;
import org.exoplatform.social.core.jpa.storage.dao.StreamItemDAOTest;
import org.exoplatform.social.core.listeners.ActivityMetadataListenerTest;
import org.exoplatform.social.core.listeners.ActivityTagMetadataListenerTest;
import org.exoplatform.social.core.manager.ActivityManagerTest;
import org.exoplatform.social.core.manager.IdentityManagerTest;
import org.exoplatform.social.core.manager.RelationshipManagerTest;
import org.exoplatform.social.core.metadata.thumbnail.ImageThumbnailServiceImplTest;
import org.exoplatform.social.core.processor.AttachmentActivityProcessorTest;
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
import org.exoplatform.social.core.jpa.search.ProfileSearchConnectorTest;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnectorTest;

import io.meeds.social.cms.service.CMSServiceTest;
import io.meeds.social.core.richeditor.RichEditorConfigurationServiceTest;
import io.meeds.social.link.plugin.LinkSettingTranslationPluginTest;
import io.meeds.social.link.plugin.LinkTranslationPluginTest;
import io.meeds.social.link.service.LinkServiceTest;
import io.meeds.social.observer.plugin.ActivityOberverPluginTest;
import io.meeds.social.observer.service.ObserverServiceTest;
import io.meeds.social.translation.service.TranslationServiceTest;

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
  ObserverServiceTest.class,
  CMSServiceTest.class,
  LinkServiceTest.class,
  LinkSettingTranslationPluginTest.class,
  LinkTranslationPluginTest.class,
  ImageThumbnailServiceImplTest.class,
  RichEditorConfigurationServiceTest.class,
  TranslationServiceTest.class,
  UserPasswordHashMigrationTest.class,
  ActivityOberverPluginTest.class,
  AttachmentActivityProcessorTest.class,
  AttachmentServiceTest.class,
  MentionUtilsTest.class,
  ProfileSearchConnectorTest.class,
  ComplementaryFilterSearchConnectorTest.class,
  ProfileIndexingServiceConnectorTest.class
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
