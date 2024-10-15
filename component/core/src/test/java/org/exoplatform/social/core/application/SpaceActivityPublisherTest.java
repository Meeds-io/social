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
package org.exoplatform.social.core.application;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link SpaceActivityPublisher}
 * 
 * @author hoat_le
 */
public class SpaceActivityPublisherTest extends AbstractCoreTest {

  private static final Log        LOG = ExoLogger.getLogger(SpaceActivityPublisherTest.class);

  private ActivityManager         activityManager;

  private SpaceActivityPublisher  spaceActivityPublisher;

  private List<ExoSocialActivity> tearDownActivityList;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<>();
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    spaceActivityPublisher = getContainer().getComponentInstanceOfType(SpaceActivityPublisher.class);
    assertNotNull("spaceActivityPublisher must not be null", spaceActivityPublisher);
  }

  @Override
  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    super.tearDown();
  }

  /**
   * @throws Exception
   */
  public void testSpaceCreation() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");

    Space space = new Space();
    space.setDisplayName("Toto 3");
    space.setPrettyName(space.getDisplayName());
    space.setGroupId("/platform/users");
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.OPEN);
    String[] managers = new String[] { "root" };
    String[] members = new String[] { "root" };
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.createSpace(space);
    try {
      assertNotNull("space.getId() must not be null", space.getId());
      SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space,
                                                          rootIdentity.getRemoteId(),
                                                          SpaceLifeCycleEvent.Type.SPACE_CREATED);
      spaceActivityPublisher.spaceCreated(event);

      Thread.sleep(3000);

      Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      assertEquals(1, activityManager.getActivitiesWithListAccess(identity).getSize());
      ExoSocialActivity activity = activityManager.getActivitiesWithListAccess(identity).load(0, 1)[0];
      tearDownActivityList.add(activity);
      ActivityStream activityStream = activity.getActivityStream();

      assertNotNull("activityStream.getId() must not be null", activityStream.getId());

      assertEquals("activityStream.getPrettyId() must return: " + space.getPrettyName(),
                   space.getPrettyName(),
                   activityStream.getPrettyId());
      assertEquals(ActivityStream.Type.SPACE, activityStream.getType());

      assertEquals(SpaceIdentityProvider.NAME, activityStream.getType().toString());
    } finally {
      // clean up
      spaceService.deleteSpace(space);
      identityManager.deleteIdentity(rootIdentity);
    }
  }

}
