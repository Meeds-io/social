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

import io.meeds.social.core.upgrade.SpaceNavigationIconUpgradePluginTest;
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

import io.meeds.social.image.plugin.ImageAttachmentPluginTest;

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
    ImageAttachmentPluginTest.class,
    UserProfileComparatorTest.class,
    ActivityIndexingServiceConnectorTest.class,
    ActivitySearchConnectorTest.class,
    SpaceNavigationIconUpgradePluginTest.class,
})
public class NoContainerTestSuite {

}
