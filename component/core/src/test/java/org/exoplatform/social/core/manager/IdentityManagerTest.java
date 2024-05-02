/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.manager;

import java.io.InputStream;
import java.util.*;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;

import org.exoplatform.social.core.jpa.storage.dao.jpa.ProfilePropertySettingDAO;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.*;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

/**
 * Unit Tests for {@link IdentityManager}
 *
 * @author hoat_le
 */
// TODO :
// * Fix tests to not have to specify the order of execution like this
// * The order of tests execution changed in Junit 4.11
// (https://github.com/KentBeck/junit/blob/master/doc/ReleaseNotes4.11.md)
@FixMethodOrder(MethodSorters.JVM)
public class IdentityManagerTest extends AbstractCoreTest {

  private IdentityManager identityManager;

  private IdentityStorage identityStorage;

  private List<Space>     tearDownSpaceList;

  private List<Identity>  tearDownIdentityList;

  private ActivityManager activityManager;

  private ProfilePropertyService profilePropertyService;

  private SpaceService    spaceService;

  public void setUp() throws Exception {
    super.setUp();
    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull(identityManager);

    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    assertNotNull(spaceService);

    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull(activityManager);

    profilePropertyService = (ProfilePropertyService) getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    assertNotNull(profilePropertyService);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownSpaceList = new ArrayList<Space>();
    org.exoplatform.services.security.Identity identity = getService(IdentityRegistry.class).getIdentity("root");
    ConversationState.setCurrent(new ConversationState(identity));
  }

  public void tearDown() throws Exception {
    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    getService(ProfilePropertySettingDAO.class).deleteAll();
    super.tearDown();
  }

  /**
   * Test {@link IdentityManager#getIdentity(String)}
   */
  // FIXME regression JCR to RDBMS migration
  // public void testGetIdentityById() {
  // final String username = "root";
  // Identity foundIdentity =
  // identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
  // username,
  // true);
  //
  // // Gets Identity By Node Id
  // {
  // Identity gotIdentity = identityManager.getIdentity(foundIdentity.getId(),
  // true);
  //
  // assertNotNull(gotIdentity);
  // assertEquals(foundIdentity.getId(), gotIdentity.getId());
  // assertEquals("gotIdentity.getProviderId() must return: " +
  // OrganizationIdentityProvider.NAME,
  // OrganizationIdentityProvider.NAME,
  // gotIdentity.getProviderId());
  // assertEquals("gotIdentity.getRemoteId() must return: " + username,
  // username,
  // gotIdentity.getRemoteId());
  // // By default, when getIdentity(String nodeId) will have load profile by
  // // default (means saved).
  // assertNotNull("gotIdentity.getProfile().getId() must not return: null",
  // gotIdentity.getProfile().getId());
  //
  // assertNotNull("gotIdentity.getProfile().getProperty(Profile.FIRST_NAME)
  // must not be null",
  // gotIdentity.getProfile().getProperty(Profile.FIRST_NAME));
  // assertFalse("gotIdentity.getProfile().getFullName().isEmpty() must be
  // false", gotIdentity.getProfile().getFullName().isEmpty());
  //
  //
  // }
  //
  // tearDownIdentityList.add(identityManager.getIdentity(foundIdentity.getId(),
  // false));
  // }

  /**
   * @throws Exception
   */
  public void testGetSpaceMembers() throws Exception {

    Identity demoIdentity = populateIdentity("demo");
    Identity johnIdentity = populateIdentity("john");
    Identity maryIdentity = populateIdentity("mary");
    int number = 0;
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] spaceManagers = new String[] { demoIdentity.getRemoteId() };
    String[] members = new String[] { demoIdentity.getRemoteId() };
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(spaceManagers);
    space.setMembers(members);

    space = this.createSpaceNonInitApps(space, demoIdentity.getRemoteId(), null);

    Space savedSpace = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("savedSpace must not be null", savedSpace);

    // add member to space
    spaceService.addMember(savedSpace, johnIdentity.getRemoteId());
    spaceService.addMember(savedSpace, maryIdentity.getRemoteId());

    {
      ProfileFilter profileFilter = new ProfileFilter();
      ListAccess<Identity> spaceMembers = identityManager.getSpaceIdentityByProfileFilter(savedSpace,
                                                                                          profileFilter,
                                                                                          SpaceMemberFilterListAccess.Type.MEMBER,
                                                                                          true);
      assertEquals(3, spaceMembers.getSize());
    }

    // remove member to space
    spaceService.removeMember(savedSpace, johnIdentity.getRemoteId());
    {
      ProfileFilter profileFilter = new ProfileFilter();
      ListAccess<Identity> got = identityManager.getSpaceIdentityByProfileFilter(savedSpace,
                                                                                 profileFilter,
                                                                                 SpaceMemberFilterListAccess.Type.MEMBER,
                                                                                 true);
      assertEquals(2, got.getSize());
    }

    // clear space
    tearDownSpaceList.add(savedSpace);

  }

  /**
   * Test {@link IdentityManager#getIdentity(String, boolean)}
   */
  public void testGetIdentityByIdWithLoadProfile() {

    final String username = "root";
    Identity tobeSavedIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                     username,
                                                                     true);
    // loadProfile=false for identityId = uuid
    {
      Identity gotIdentity = identityManager.getIdentity(tobeSavedIdentity.getId(), false);

      assertNotNull(gotIdentity);
      assertEquals(tobeSavedIdentity.getId(), gotIdentity.getId());
      assertEquals("gotIdentity.getProviderId() must return: " + OrganizationIdentityProvider.NAME,
                   OrganizationIdentityProvider.NAME,
                   gotIdentity.getProviderId());
      assertEquals("gotIdentity.getRemoteId() must return: " + username,
                   username,
                   gotIdentity.getRemoteId());
    }
    tearDownIdentityList.add(identityManager.getIdentity(tobeSavedIdentity.getId(), false));
  }

  /**
   * Test {@link IdentityManager#deleteIdentity(Identity)}
   */
  public void testDeleteIdentity() {
    final String username = "demo";
    Identity tobeSavedIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                     username,
                                                                     false);

    assertNotNull("tobeSavedIdentity.getId() must not be null", tobeSavedIdentity.getId());

    assertNotNull("tobeSavedIdentity.getProfile().getId() must not be null",
                  tobeSavedIdentity.getProfile().getId());

    identityManager.deleteIdentity(tobeSavedIdentity);

    // assertNull("identityManager.getIdentity(tobeSavedIdentity.getId() must
    // return null",
    // identityManager.getIdentity(tobeSavedIdentity.getId()));
    Identity gotIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, false);
    assertNotNull("gotIdentity must not be null because " + username + " is in organizationService",
                  gotIdentity);
    assertNotNull("gotIdentity.getId() must not be null", gotIdentity.getId());
    // assertEquals("gotIdentity.getId() must be: " + globalId.toString(),
    // globalId.toString(), gotIdentity.getId());
    tearDownIdentityList.add(gotIdentity);
  }

  /**
   * Test order
   * {@link IdentityManager#getIdentitiesByProfileFilter(String, ProfileFilter, boolean)}
   */
  public void testOrderOfGetIdentitiesByProfileFilter() throws Exception {
    // Create new users
    String providerId = "organization";
    String[] FirstNameList = { "John", "Bob", "Alain" };
    String[] LastNameList = { "Smith", "Dupond", "Dupond" };
    for (int i = 0; i < 3; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);
      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, FirstNameList[i]);
      profile.setProperty(Profile.LAST_NAME, LastNameList[i]);
      profile.setProperty(Profile.FULL_NAME, FirstNameList[i] + " " + LastNameList[i]);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");

      identityStorage.saveProfile(profile);
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
    }

    ProfileFilter pf = new ProfileFilter();
    ListAccess<Identity> idsListAccess = null;
    // Test order by last name
    pf.setSorting(new Sorting(SortBy.FULLNAME, OrderBy.ASC));
    idsListAccess = identityManager.getIdentitiesByProfileFilter(providerId, pf, false);
    assertNotNull(idsListAccess);
    assertTrue(idsListAccess.getSize() >= 3);
    assertEquals("Alain Dupond", idsListAccess.load(0, 20)[0].getProfile().getFullName());
    assertEquals("Bob Dupond", idsListAccess.load(0, 20)[1].getProfile().getFullName());

    // Test order by first name if last name is equal
    Identity[] identityArray = idsListAccess.load(0, 2);
    assertEquals(tearDownIdentityList.get(2).getId(), identityArray[0].getId());

  }

  /**
   * Test {@link IdentityManager#updateProfile(Profile)}
   */
  public void testUpdateProfile() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = rootIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "CEO");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);

    Identity identityUpdated = identityManager.getOrCreateIdentity(rootIdentity.getProviderId(), rootIdentity.getRemoteId());
    assertEquals("CEO", identityUpdated.getProfile().getProperty(Profile.POSITION));

    end();
    begin();

    RealtimeListAccess<ExoSocialActivity> activitiesWithListAccess = activityManager.getActivitiesWithListAccess(rootIdentity);
    List<ExoSocialActivity> rootActivityList = activitiesWithListAccess.loadAsList(0, activitiesWithListAccess.getSize());

    tearDownIdentityList.add(rootIdentity);
  }

  /**
   * Test {@link IdentityManager#updateProfile(Profile, boolean)}
   */
  public void testUpdateProfileAndDetectChanges() throws Exception {
    ProfilePropertySetting profilePropertySetting =  new ProfilePropertySetting();
    profilePropertySetting.setPropertyName(Profile.POSITION);
    profilePropertySetting.setActive(true);
    profilePropertyService.createPropertySetting(profilePropertySetting);
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = rootIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "Changed POSITION");
    List<Integer> changes = new ArrayList<>();
    identityManager.registerProfileListener(new ProfileListenerPlugin() {
      @Override
      public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
        changes.add(1);
      }

      @Override
      public void createProfile(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void technicalUpdated(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void contactSectionUpdated(ProfileLifeCycleEvent event) {
        changes.add(2);
      }

      @Override
      public void bannerUpdated(ProfileLifeCycleEvent event) {
        changes.add(3);
      }

      @Override
      public void avatarUpdated(ProfileLifeCycleEvent event) {
        changes.add(4);
      }

      @Override
      public void aboutMeUpdated(ProfileLifeCycleEvent event) {
        changes.add(5);
      }
    });

    identityManager.updateProfile(profile, true);
    assertFalse(changes.isEmpty());
    assertTrue(changes.contains(2));

    profile.setProperty(Profile.POSITION, "Changed POSITION");
    profile.setProperty(Profile.ABOUT_ME, "Changed ABOUT_ME");
    identityManager.updateProfile(profile, true);
    assertTrue(changes.size() >= 2);
    assertTrue(changes.contains(5));

    List<Map<String, String>> experiences = new ArrayList<>();
    Map<String, String> company = new HashMap<>();
    experiences.add(company);
    company.put(Profile.EXPERIENCES_COMPANY, "oldValue");
    profile.setProperty(Profile.EXPERIENCES, experiences);
    identityManager.updateProfile(profile, true);
    assertTrue(changes.size() >= 3);
    assertTrue(changes.contains(1));
  }
  public void testUpdateProfileAndDetectChangeBanner() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Profile profile = rootIdentity.getProfile();

    List<Integer> changes = new ArrayList<>();
    identityManager.registerProfileListener(new ProfileListenerPlugin() {
      @Override
      public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
        changes.add(1);
      }

      @Override
      public void createProfile(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void technicalUpdated(ProfileLifeCycleEvent event) {
        // noop
      }

      @Override
      public void contactSectionUpdated(ProfileLifeCycleEvent event) {
        changes.add(2);
      }

      @Override
      public void bannerUpdated(ProfileLifeCycleEvent event) {
        changes.add(3);
      }

      @Override
      public void avatarUpdated(ProfileLifeCycleEvent event) {
        changes.add(4);
      }

      @Override
      public void aboutMeUpdated(ProfileLifeCycleEvent event) {
        changes.add(5);
      }
    });


    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", inputStream, System.currentTimeMillis());
    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityManager.updateProfile(profile, true);
    assertEquals(1, changes.size());
    assertTrue(changes.contains(3));

  }

  public void testUpdateProfileActivity() throws Exception {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    Profile profile = rootIdentity.getProfile();
    profile.setProperty(Profile.POSITION, "CEO");
    assertEquals(0, profile.getListUpdateTypes().size());
    identityManager.updateProfile(profile);
    assertEquals(0, profile.getListUpdateTypes().size());

    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    assertEquals(1, profile.getListUpdateTypes().size());

    Identity identityUpdated = identityManager.getOrCreateIdentity(rootIdentity.getProviderId(), rootIdentity.getRemoteId());
    assertEquals("CEO", identityUpdated.getProfile().getProperty(Profile.POSITION));
  }

  /**
   * Populate list of identities.
   */
  private void populateData() {
    populateIdentities(5, true);
  }

  /**
   * Populate list of identities.
   */
  private void populateData(String remoteId) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);
    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName " + remoteId);
    profile.setProperty(Profile.LAST_NAME, "LastName" + remoteId);
    profile.setProperty(Profile.FULL_NAME, "FirstName " + remoteId + " " + "LastName" + remoteId);
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");

    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
  }

  /**
   * Populates the list of identities by specifying the number of items and to
   * indicate if they are added to the tear-down list.
   *
   * @param numberOfItems
   * @param addedToTearDownList
   */
  private void populateIdentities(int numberOfItems, boolean addedToTearDownList) {
    String providerId = "organization";
    for (int i = 0; i < numberOfItems; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);
      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      profile.setProperty(Profile.POSITION, "developer");
      profile.setProperty(Profile.GENDER, "male");

      identityStorage.saveProfile(profile);
      identity.setProfile(profile);
      if (addedToTearDownList) {
        tearDownIdentityList.add(identity);
      }
    }
  }

  /**
   * Populate one identity with remoteId.
   * 
   * @param remoteId
   * @return
   */
  private Identity populateIdentity(String remoteId) {
    return populateIdentity(remoteId, true);
  }

  private Identity populateIdentity(String remoteId, boolean addedToTearDownList) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, remoteId);
    profile.setProperty(Profile.LAST_NAME, "gtn");
    profile.setProperty(Profile.FULL_NAME, remoteId + " " + "gtn");
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");

    identityStorage.saveProfile(profile);

    if (addedToTearDownList) {
      tearDownIdentityList.add(identity);
    }
    return identity;
  }

  /**
   *
   */
  public void testIdentityExisted() {
    // False case
    {
      String remoteId = "notfound";
      String providerId = OrganizationIdentityProvider.NAME;
      final boolean existed = identityManager.identityExisted(providerId, remoteId);
      assertFalse(existed);
    }

    // True case
    {
      // NOTE : we use root as remoteId here because root user is created in
      // portal user system
      // and IdentityManager.identityExisted() just check a portal's user either
      // exist or not..
      // ATTENTION : IdentityManager.identityExisted() depends on providerId,
      // not on identityStorage
      String remoteId = "root";
      String providerId = OrganizationIdentityProvider.NAME;
      final boolean existed = identityManager.identityExisted(providerId, remoteId);
      assertTrue(existed);
    }

  }

  /**
   * Test cache management
   */
  public void testCacheManagement() throws ActivityStorageException {
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                "root");
    Profile rootProfile = rootIdentity.getProfile();
    final String newFirstName = "New First Name";

    rootProfile.setProperty(Profile.FIRST_NAME, newFirstName);
    identityStorage.saveProfile(rootProfile);
    Identity gotRootIdentity = identityManager.getOrCreateUserIdentity("root");
    assertNotNull("gotRootIdentity.getId() must not be null", gotRootIdentity.getId());
    assertEquals("gotRootIdentity.getProfile().getProperty(Profile.FIRST_NAME) must be updated: "
        + newFirstName, newFirstName, gotRootIdentity.getProfile().getProperty(Profile.FIRST_NAME));
  }
}
