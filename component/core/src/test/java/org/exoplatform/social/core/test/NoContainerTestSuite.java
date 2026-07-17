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
package org.exoplatform.social.core.test;

import io.meeds.social.cms.listener.PageContentBlockIndexingListenerTest;
import io.meeds.social.cms.service.PageContentBlockPluginServiceImplTest;
import io.meeds.social.cms.service.PageUrlResolverServiceImplTest;
import io.meeds.social.cms.storage.elasticsearch.PageContentIndexingConnectorTest;
import io.meeds.social.cms.storage.elasticsearch.PageContentSearchConnectorTest;
import io.meeds.social.space.invitation.storage.SpaceInvitationLinkLinkStorageTest;
import io.meeds.social.space.listener.SpaceGroupCacheListenerTest;
import io.meeds.social.space.listener.SpaceInvitationLinkLinkJoinListenerTest;
import io.meeds.social.upgrade.RemoveProfilePropertyUpgradePlugin;
import io.meeds.social.space.plugin.SpaceGroupDecoratorPluginTest;
import io.meeds.social.space.template.listener.SpaceTemplateMembershipListenerTest;
import io.meeds.social.space.template.plugin.decorator.SpaceTemplateGroupDecoratorPluginTest;
import org.exoplatform.social.core.space.spi.SpaceServiceMockTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.social.core.activity.ActivityIndexingServiceConnectorTest;
import org.exoplatform.social.core.activity.ActivitySearchConnectorTest;
import org.exoplatform.social.core.activity.filter.ActivityIteratorTest;
import org.exoplatform.social.core.identity.IdentityResultTest;
import org.exoplatform.social.core.identity.model.GlobalIdTest;
import org.exoplatform.social.core.identity.model.IdentityTest;
import org.exoplatform.social.core.image.ImageUtilsTest;
import org.exoplatform.social.core.plugin.ActivityAttachmentPluginTest;
import org.exoplatform.social.core.processor.ActivityResourceBundlePluginTest;
import org.exoplatform.social.core.profile.UserProfileComparatorTest;
import org.exoplatform.social.core.relationship.RelationshipTest;
import org.exoplatform.social.core.service.GettingStartedServiceTest;
import org.exoplatform.social.core.storage.StorageUtilsTest;

import io.meeds.social.authorization.AuthorizationManagerTest;
import io.meeds.social.category.service.CategoryServiceUnitTest;
import io.meeds.social.coediting.service.CoeditingServiceTest;
import io.meeds.social.core.identity.service.UserExportServiceTest;
import io.meeds.social.core.identity.service.UserImportServiceTest;
import io.meeds.social.core.plugin.SiteAttachmentPluginTest;
import io.meeds.social.databind.service.DatabindServiceTest;
import io.meeds.social.search.SpaceSearchConnectorTest;
import io.meeds.social.space.administration.service.SpaceAdministrationServiceTest;
import io.meeds.social.space.service.SpaceDirectoryServiceTest;
import io.meeds.social.space.storage.SpaceDirectoryStorageTest;
import io.meeds.social.space.template.plugin.attachment.SpaceTemplateBannerAttachmentPluginTest;
import io.meeds.social.space.template.plugin.databind.SpaceTemplateDatabindPluginTest;
import io.meeds.social.space.template.plugin.translation.SpaceTemplateTranslationPluginTest;
import io.meeds.social.space.template.service.SpaceTemplateServiceTest;
import io.meeds.social.space.template.storage.SpaceTemplateStorageTest;
import io.meeds.social.upgrade.SpaceNavigationIconUpgradePluginTest;
import io.meeds.social.user.plugin.UserAclPluginTest;

@RunWith(Suite.class)
@SuiteClasses({
    GlobalIdTest.class,
    IdentityTest.class,
    ImageUtilsTest.class,
    ActivityResourceBundlePluginTest.class,
    RelationshipTest.class,
    StorageUtilsTest.class,
    ActivityIteratorTest.class,
    IdentityResultTest.class,
    GettingStartedServiceTest.class,
    ActivityAttachmentPluginTest.class,
    UserProfileComparatorTest.class,
    ActivityIndexingServiceConnectorTest.class,
    ActivitySearchConnectorTest.class,
    SpaceNavigationIconUpgradePluginTest.class,
    AuthorizationManagerTest.class,
    SpaceAdministrationServiceTest.class,
    SpaceSearchConnectorTest.class,
    SpaceTemplateBannerAttachmentPluginTest.class,
    SpaceTemplateTranslationPluginTest.class,
    SpaceTemplateGroupDecoratorPluginTest.class,
    SpaceGroupDecoratorPluginTest.class,
    SpaceTemplateMembershipListenerTest.class,
    SpaceGroupCacheListenerTest.class,
    SpaceTemplateDatabindPluginTest.class,
    SpaceTemplateStorageTest.class,
    SpaceTemplateServiceTest.class,
    CategoryServiceUnitTest.class,
    CoeditingServiceTest.class,
    SpaceDirectoryServiceTest.class,
    SpaceDirectoryStorageTest.class,
    SpaceServiceMockTest.class,
    DatabindServiceTest.class,
    SiteAttachmentPluginTest.class,
    UserExportServiceTest.class,
    UserImportServiceTest.class,
    UserAclPluginTest.class,
    RemoveProfilePropertyUpgradePlugin.class,
    SpaceInvitationLinkLinkJoinListenerTest.class,
    SpaceInvitationLinkLinkStorageTest.class,
    PageContentBlockPluginServiceImplTest.class,
    PageUrlResolverServiceImplTest.class,
    PageContentBlockIndexingListenerTest.class,
    PageContentIndexingConnectorTest.class,
    PageContentSearchConnectorTest.class
})
public class NoContainerTestSuite {

}
