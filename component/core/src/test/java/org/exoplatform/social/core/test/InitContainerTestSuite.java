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
package org.exoplatform.social.core.test;

import org.exoplatform.social.core.metadata.thumbnail.ImageThumbnailServiceImplTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;
import org.exoplatform.social.core.application.SpaceActivityPublisherTest;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingServiceTest;
import org.exoplatform.social.core.binding.spi.RDBMSGroupSpaceBindingStorageTest;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImplTest;
import org.exoplatform.social.core.listeners.*;
import org.exoplatform.social.core.manager.*;
import org.exoplatform.social.core.processor.MetadataActivityProcessorTest;
import org.exoplatform.social.core.processor.OSHtmlSanitizerProcessorTest;
import org.exoplatform.social.core.processor.TemplateParamsProcessorTest;
import org.exoplatform.social.core.search.SortingTest;
import org.exoplatform.social.core.service.LinkProviderTest;
import org.exoplatform.social.core.space.SpaceLifeCycleTest;
import org.exoplatform.social.core.space.SpaceUtilsTest;
import org.exoplatform.social.core.space.spi.SpaceServiceTest;
import org.exoplatform.social.core.space.spi.SpaceTemplateServiceTest;
import org.exoplatform.social.metadata.MetadataServiceTest;
import org.exoplatform.social.metadata.favorite.FavoriteServiceTest;
import org.exoplatform.social.metadata.tag.TagServiceTest;

@SuiteClasses({
//FIXME regression JCR to RDBMS migration
//    SpaceLastVisitedTest.class,
//FIXME regression JCR to RDBMS migration
//    WhatsHotTest.class,
    RDBMSActivityStorageImplTest.class,
    ActivityManagerTest.class,
//FIXME regression JCR to RDBMS migration
//    ActivityStorageTest.class,
//FIXME regression JCR to RDBMS migration
//    ActivityStorageImplTestCase.class,
    IdentityManagerTest.class,
//FIXME regression JCR to RDBMS migration
//    IdentityStorageTest.class,
    SpaceServiceTest.class,
//FIXME regression JCR to RDBMS migration
//    SpaceStorageTest.class,
    RelationshipManagerTest.class,
//FIXME regression JCR to RDBMS migration
//    RelationshipStorageTest.class,
//FIXME regression JCR to RDBMS migration
//    RelationshipPublisherTest.class,
//FIXME regression JCR to RDBMS migration
//    SpaceUtilsRestTest.class,
    SpaceUtilsTest.class,
    SpaceActivityPublisherTest.class,
    SpaceLifeCycleTest.class,
    SocialUserProfileEventListenerImplTest.class,
    OSHtmlSanitizerProcessorTest.class,
    TemplateParamsProcessorTest.class,
//FIXME regression JCR to RDBMS migration
//    ProfileUpdatesPublisherTest.class,
//FIXME regression JCR to RDBMS migration
//    MentionsProcessorTest.class,
    LinkProviderTest.class,
//FIXME regression JCR to RDBMS migration
//    SpaceUtilsWildCardMembershipTest.class,
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
    ImageThumbnailServiceImplTest.class
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
