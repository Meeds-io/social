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
package org.exoplatform.social.core.test;

import org.exoplatform.social.core.service.GettingStartedServiceTest;
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
import org.exoplatform.social.core.processor.ActivityResourceBundlePluginTest;
import org.exoplatform.social.core.processor.I18NActivityProcessorTest;
import org.exoplatform.social.core.profile.UserProfileComparatorTest;
import org.exoplatform.social.core.relationship.RelationshipTest;
import org.exoplatform.social.core.search.SearchServiceTest;
import org.exoplatform.social.core.storage.StorageUtilsTest;

@RunWith(Suite.class)
@SuiteClasses({
    GlobalIdTest.class,
    IdentityTest.class,
    ImageUtilsTest.class,
    ActivityResourceBundlePluginTest.class,
    I18NActivityProcessorTest.class,
    RelationshipTest.class,
    StorageUtilsTest.class,
    ActivityIteratorTest.class,
    IdentityResultTest.class,
    GettingStartedServiceTest.class,
    UserProfileComparatorTest.class,
    SearchServiceTest.class,
    ActivityIndexingServiceConnectorTest.class,
    ActivitySearchConnectorTest.class,
})
public class NoContainerTestSuite {

}
