/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage;

import static org.junit.Assert.assertNotEquals;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.services.organization.*;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.test.AbstractCoreTest;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 17, 2010
 * Time: 9:34:56 AM
 */
public class IdentityStorageTest extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private SpaceStorage spaceStorage;
  private List<Identity> tearDownIdentityList;
  private List<Space> tearDownSpaceList;

  public void setUp() throws Exception {
    super.setUp();
    identityStorage = getService(IdentityStorage.class);
    spaceStorage = getService(SpaceStorage.class);
    assertNotNull("identityStorage must not be null", identityStorage);
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownSpaceList = new ArrayList<Space>();
  }

  /**
   * Tests {@link IdenityStorage#saveIdentity(Identity)}
   *
   */
  public void testSaveIdentity() {
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, "identity1");
    identityStorage.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    final String updatedRemoteId = "identity-updated";

    tobeSavedIdentity.setRemoteId(updatedRemoteId);

    identityStorage.saveIdentity(tobeSavedIdentity);

    Identity gotIdentity = identityStorage.findIdentityById(tobeSavedIdentity.getId());

    assertEquals(updatedRemoteId, gotIdentity.getRemoteId());
    tearDownIdentityList.add(gotIdentity);
    
  }

  /**
   * Tests {@link IdenityStorage#processEnabledIdentity(Identity)}
   */
  public void testEnableIdentity() {
    final String remoteUser = "user";
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, remoteUser);
    identityStorage.saveIdentity(identity);

    String id = identity.getId();

    //
    assertNotNull(identity.getId());
    //
    identityStorage.processEnabledIdentity(identity, false);

    identity = identityStorage.findIdentityById(id);
    assertFalse(identity.isEnable());

    //
    identityStorage.processEnabledIdentity(identity, true);

    identity = identityStorage.findIdentityById(id);
    assertTrue(identity.isEnable());

    tearDownIdentityList.add(identity);
  }

  /**
   * Tests {@link IdenityStorage#deleteIdentity(Identity)}
   *
   */
  public void testDeleteIdentity() {
    final String username = "username";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
    identityStorage.saveIdentity(tobeSavedIdentity);

    assertNotNull(tobeSavedIdentity.getId());

    identityStorage.deleteIdentity(tobeSavedIdentity);

    tobeSavedIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, username);
    assertTrue("tobeSavedIdentity must be mark as deleted", tobeSavedIdentity.isDeleted());

    // Delete identity with loaded profile
    {
      tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
      identityStorage.saveIdentity(tobeSavedIdentity);
      assertNotNull("tobeSavedIdentity.getId() must not be null.", tobeSavedIdentity.getId());
      assertNull("tobeSavedIdentity.getProfile().getId() must be null.", tobeSavedIdentity.getProfile().getId());
      Profile profile = identityStorage.loadProfile(tobeSavedIdentity.getProfile());
      tobeSavedIdentity.setProfile(profile);
      assertNotNull("tobeSavedIdentity.getProfile().getId() must not be null", tobeSavedIdentity.getProfile().getId());

      identityStorage.deleteIdentity(tobeSavedIdentity);
      assertNotNull("tobeSavedIdentity.getId() must not be null", tobeSavedIdentity.getId());
      try {
        identityStorage.findIdentityById(tobeSavedIdentity.getId());
      } catch (Exception e1) {
        assert false : "can't update avatar" + e1 ;
      }

    }
  }

  /**
   * Tests {@link IdenityStorage#findIdentityById(String)}
   *
   */
  public void testFindIdentityById() {
    final String remoteUser = "identity1";
    Identity toSaveIdentity = new Identity(OrganizationIdentityProvider.NAME, remoteUser);
    identityStorage.saveIdentity(toSaveIdentity);

    assertNotNull(toSaveIdentity.getId());

    Identity gotIdentityById = identityStorage.findIdentityById(toSaveIdentity.getId());

    assertNotNull(gotIdentityById);
    assertEquals(toSaveIdentity.getId(), gotIdentityById.getId());
    assertEquals(toSaveIdentity.getProviderId(), gotIdentityById.getProviderId());
    assertEquals(toSaveIdentity.getRemoteId(), gotIdentityById.getRemoteId());

    Identity notFoundIdentityByRemoteid = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "not-found");

    assertNull(notFoundIdentityByRemoteid);

    Identity gotIdentityByRemoteId = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteUser);

    assertNotNull(gotIdentityByRemoteId);
    assertEquals(gotIdentityByRemoteId.getId(), toSaveIdentity.getId());
    assertEquals(gotIdentityByRemoteId.getProviderId(), toSaveIdentity.getProviderId());
    assertEquals(gotIdentityByRemoteId.getRemoteId(), toSaveIdentity.getRemoteId());
    
    tearDownIdentityList.add(gotIdentityByRemoteId);
  }

  /**
   * Tests {@link IdentityStorage#sortIdentities}
   *
   */
  public void testSortIdentities() {
    List<String> remoteUsers = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      String remoteId = "sortIdentity" + i;
      remoteUsers.add(remoteId);
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, remoteId);
      identityStorage.saveIdentity(identity);
      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "user " + i);
      profile.setProperty(Profile.LAST_NAME, "user " + i);
      profile.setProperty(Profile.FULL_NAME, "user " + i);
      identityStorage.saveProfile(profile);
      tearDownIdentityList.add(identity);
    }
    List<String> sortIdentities = identityStorage.sortIdentities(remoteUsers,
                                                                 Profile.FULL_NAME,
                                                                 "asc",
                                                                 true);
    assertEquals(remoteUsers, sortIdentities);

    Identity identity1 = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "sortIdentity1");
    identity1.setEnable(false);
    identityStorage.saveIdentity(identity1);

    sortIdentities = identityStorage.sortIdentities(remoteUsers,
                                                    Profile.FULL_NAME,
                                                    "asc");
    assertEquals(9, sortIdentities.size());

    sortIdentities = identityStorage.sortIdentities(remoteUsers,
                                                    Profile.FULL_NAME,
                                                    "asc",
                                                    false);
    assertEquals(10, sortIdentities.size());
  }

  /**
   * Tests {@link IdenityStorage#findIdentity(String, String)}
   *
   */
  public void testFindIdentity() {
    final String userName = "username";

    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, userName);
    identityStorage.saveIdentity(tobeSavedIdentity);
    tearDownIdentityList.add(tobeSavedIdentity);

    Identity foundIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);

    assertNotNull(foundIdentity);
    assertNotNull(foundIdentity.getId());
    assertEquals(OrganizationIdentityProvider.NAME, foundIdentity.getProviderId());
    assertEquals(userName, foundIdentity.getRemoteId());
    tearDownIdentityList.add(foundIdentity);
  }

  /**
   * Tests {@link IdenityStorage#saveProfile(Profile)}
   *
   */
  public void testSaveProfile() {
    final String userName = "username";
    final String firstName = "FirstName";
    final String lastName = "LastName";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, userName);
    identityStorage.saveIdentity(tobeSavedIdentity);
    tearDownIdentityList.add(tobeSavedIdentity);

    Profile tobeSavedProfile = tobeSavedIdentity.getProfile();

    tobeSavedProfile.setProperty(Profile.USERNAME, userName);
    tobeSavedProfile.setProperty(Profile.FIRST_NAME, firstName);
    tobeSavedProfile.setProperty(Profile.LAST_NAME, lastName);

    assertTrue(tobeSavedProfile.hasChanged());
    identityStorage.saveProfile(tobeSavedProfile);
    assertFalse(tobeSavedProfile.hasChanged());

    assertNotNull(tobeSavedProfile.getId());

    assertEquals(userName, tobeSavedProfile.getProperty(Profile.USERNAME));
    assertEquals(firstName, tobeSavedProfile.getProperty(Profile.FIRST_NAME));
    assertEquals(lastName, tobeSavedProfile.getProperty(Profile.LAST_NAME));
    assertEquals(firstName + " " + lastName, tobeSavedProfile.getFullName());
    tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName));
  }

  /**
   * Tests {@link IdenityStorage#loadProfile(Profile)}
   *
   */
  public void testLoadProfile() throws Exception {
    final String username = "username";
    Identity tobeSavedIdentity = new Identity(OrganizationIdentityProvider.NAME, username);
    identityStorage.saveIdentity(tobeSavedIdentity);
    tearDownIdentityList.add(tobeSavedIdentity);
    Profile tobeSavedProfile = tobeSavedIdentity.getProfile();
    tobeSavedProfile.setProperty(Profile.USERNAME, username);

    assertTrue(tobeSavedProfile.hasChanged());
    tobeSavedProfile = identityStorage.loadProfile(tobeSavedProfile);
    assertFalse(tobeSavedProfile.hasChanged());

    assertNotNull(tobeSavedProfile.getId());
    assertEquals(username, tobeSavedProfile.getProperty(Profile.USERNAME));
    
    // Test in case loading an user has dot characters in name.
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, System.currentTimeMillis());
    inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", inputStream, System.currentTimeMillis());
    String userDotName = "user.name";
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, userDotName);
    Profile profile = new Profile(identity);
    identity.setProfile(profile);
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    profile.setProperty(Profile.BANNER, bannerAttachment);

    identityStorage.saveIdentity(identity);
    identityStorage.saveProfile(profile);

    profile = identityStorage.loadProfile(profile);

    String gotAvatarURL = profile.getAvatarUrl();
    assertNotNull(gotAvatarURL);
    assertEquals(LinkProvider.buildAvatarURL(OrganizationIdentityProvider.NAME, profile.getId(), true, profile.getAvatarLastUpdated()), gotAvatarURL);

    String gotBannerURL = profile.getBannerUrl();
    assertNotNull(gotBannerURL);
    assertEquals(LinkProvider.buildBannerURL(OrganizationIdentityProvider.NAME, profile.getId(), true, profile.getBannerLastUpdated()), gotBannerURL);

    tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userDotName));

    tearDownIdentityList.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, username));
  }

  public void testLoadProfileByReloadCreatedProfileNode() throws Exception {
    String providerId = "organization";
    String remoteId = "username";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);
    String profileId;
    //this code snippet will create profile node for test case
    {
      //create new profile in db without data (lazy creating)
      Profile profile = new Profile(identity);
      assertFalse(profile.hasChanged());
      profile = identityStorage.loadProfile(profile);
      assertFalse(profile.hasChanged());
      profileId = profile.getId();
    }

    //here is the testcase
    {
      Profile profile = new Profile(identity);
      assertFalse(profile.hasChanged());
      profile = identityStorage.loadProfile(profile);
      assertFalse(profile.hasChanged());
      assertEquals(profileId, profile.getId());
    }
  }


  public void testFindIdentityByExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    tearDownIdentityList.add(identity);
    final ProfileFilter filter = new ProfileFilter();
    filter.setName("First");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, 1, false);
    assertEquals(1, result.size());
  }

  public void testFindManyIdentitiesByExistName() throws Exception {
    final String providerId = "organization";

    final int total = 10;
    for (int i = 0; i <  total; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId+i);
      identityStorage.saveIdentity(identity);
      tearDownIdentityList.add(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName"+ i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      identityStorage.saveProfile(profile);
      identity.setProfile(profile);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("FirstName");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, total, false);
    assertEquals(total, result.size());
  }

  public void testGetIdentitiesSorted() throws Exception {
    final int total = 10;
    String remoteIdPrefix = "username";
    for (int i = 0; i < 7; i++) {
      String remoteId = remoteIdPrefix + i;
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, remoteId + i);
      identityStorage.saveIdentity(identity);
      tearDownIdentityList.add(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      identityStorage.saveProfile(profile);
      identity.setProfile(profile);
    }

    for (int i = 7; i < total; i++) {
      String remoteId = remoteIdPrefix + i;
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, remoteId + i);
      identityStorage.saveIdentity(identity);
      tearDownIdentityList.add(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " + "LastName" + i);
      profile.setProperty(Profile.EXTERNAL, "true");
      profile.setProperty(Profile.ENROLLMENT_DATE, new Date().getTime());
      identityStorage.saveProfile(profile);
      identity.setProfile(profile);
    }

    long offset = 0;
    long limit = Integer.MAX_VALUE;
    String providerId = OrganizationIdentityProvider.NAME;

    String sortDirection = OrderBy.ASC.name();

    String sortField = SortBy.FULLNAME.getFieldName();
    String fieldName = Profile.FULL_NAME;
    List<Identity> result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null,null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() >= total);
    assertSorted(remoteIdPrefix, fieldName, result);

    fieldName = Profile.LAST_NAME;
    sortField = SortBy.LASTNAME.getFieldName();

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() >= total);
    assertSorted(remoteIdPrefix, fieldName, result);

    fieldName = Profile.FIRST_NAME;
    sortField = SortBy.FIRSTNAME.getFieldName();

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() >= total);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() >= total);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() >= total);
    assertSorted(remoteIdPrefix, fieldName, result);

    // filter users by type 
    sortField = SortBy.FULLNAME.getFieldName();
    fieldName = Profile.FULL_NAME;
    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, "internal", null, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 7);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, "external", null, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 3);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, false, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 10);
    assertSorted(remoteIdPrefix, fieldName, result);
    
    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, true, null, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 0);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 10);
    assertSorted(remoteIdPrefix, fieldName, result);

    List<IdentityWithRelationship> result1 = identityStorage.getIdentitiesWithRelationships("1", 0, 20);
    assertTrue("Returned result count is not consistent", result1.size() == 10);
    assertSorted(remoteIdPrefix, fieldName, result);

    //filter users by enrolled status
    sortField = SortBy.FULLNAME.getFieldName();

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, "notEnrolled", offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 7);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, "noEnrollmentPossible", offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 3);
    assertSorted(remoteIdPrefix, fieldName, result);

    result = identityStorage.getIdentities(providerId, sortField, sortDirection, true, null, null, "enrolled", offset, limit);
    assertTrue("Returned result count is not consistent", result.size() == 3);
    assertSorted(remoteIdPrefix, fieldName, result);
  }

  public void testFindIdentityByNotExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "username";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " + "LastName");
    identityStorage.saveProfile(profile);
    identity.setProfile(profile);
    final ProfileFilter filter = new ProfileFilter();
    filter.setName("notfound");
    final List<Identity> result = identityStorage.getIdentitiesByProfileFilter(providerId, filter, 0, 1, false);
    assertEquals(0, result.size());
  }

  /**
   * Tests {@link IdenityStorage#getIdentitiesByProfileFilterCount(String, ProfileFilter)}
   * 
   */
  public void testGetIdentitiesByProfileFilterCount() throws Exception {
    populateData();

    ProfileFilter pf = new ProfileFilter();
    int idsCount = identityStorage.getIdentitiesByProfileFilterCount("organization", pf);
    assertEquals(5, idsCount);
    
    pf.setPosition("developer");
    pf.setName("FirstName");
    
    idsCount = identityStorage.getIdentitiesByProfileFilterCount("organization", pf);
    assertEquals(5, idsCount);
    
    pf.setName("LastN");
    idsCount = identityStorage.getIdentitiesByProfileFilterCount("organization", pf);
    assertEquals(5, idsCount);
    
    //disable username1
    Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, "username1");
    identityStorage.processEnabledIdentity(identity, false);
    assertEquals(4, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));
    
    //enable username1
    identityStorage.processEnabledIdentity(identity, true);
    assertEquals(5, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));

    //add external identity
    Identity externalIdentity = new Identity("organization", "username6");
    identityStorage.saveIdentity(externalIdentity);
    tearDownIdentityList.add(externalIdentity);

    //enrolled identity
    Identity enrolledIdentity = new Identity("organization", "username7");
    identityStorage.saveIdentity(enrolledIdentity);
    tearDownIdentityList.add(enrolledIdentity);

    //no enrollment possible identity
    Identity noEnrollmentPossibleIdentity = new Identity("organization", "username8");
    identityStorage.saveIdentity(noEnrollmentPossibleIdentity);
    tearDownIdentityList.add(noEnrollmentPossibleIdentity);

    Profile profile = new Profile(externalIdentity);
    profile.setProperty(Profile.FIRST_NAME, "FirstName6");
    profile.setProperty(Profile.LAST_NAME, "LastName6");
    profile.setProperty(Profile.FULL_NAME, "FirstName6 LastName6");
    profile.setProperty(Profile.EXTERNAL, "true");
    profile.setProperty("position", "developer");
    profile.setProperty("gender", "male");
    identity.setProfile(profile);
    identityStorage.saveProfile(profile);

    Profile enrolledProfile = new Profile(enrolledIdentity);
    enrolledProfile.setProperty(Profile.FIRST_NAME, "FirstName7");
    enrolledProfile.setProperty(Profile.LAST_NAME, "LastName7");
    enrolledProfile.setProperty(Profile.FULL_NAME, "FirstName7 LastName7");
    enrolledProfile.setProperty(Profile.ENROLLMENT_DATE, new Date().getTime());
    enrolledProfile.setProperty("position", "developer");
    enrolledProfile.setProperty("gender", "male");
    identity.setProfile(enrolledProfile);
    identityStorage.saveProfile(enrolledProfile);

    Profile noEnrollmentPossibleProfile = new Profile(noEnrollmentPossibleIdentity);
    noEnrollmentPossibleProfile.setProperty(Profile.FIRST_NAME, "FirstName8");
    noEnrollmentPossibleProfile.setProperty(Profile.LAST_NAME, "LastName8");
    noEnrollmentPossibleProfile.setProperty(Profile.FULL_NAME, "FirstName8 LastName8");
    noEnrollmentPossibleProfile.setProperty(Profile.LAST_LOGIN_TIME, new Date().getTime());
    noEnrollmentPossibleProfile.setProperty("position", "developer");
    noEnrollmentPossibleProfile.setProperty("gender", "male");
    identity.setProfile(noEnrollmentPossibleProfile);
    identityStorage.saveProfile(noEnrollmentPossibleProfile);

    assertEquals(8, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));

    // get internal users
    pf.setUserType("internal");

    assertEquals(7, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));
    
    pf.setConnected(true);
    assertEquals(1, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));

    pf.setConnected(false);
    assertEquals(6, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));

    pf.setUserType(null);
    assertEquals(7, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));


    // set enrolled users
    pf.setEnrollmentStatus("enrolled");
    assertEquals(1, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));

    // set notEnrolled users
    pf.setEnrollmentStatus("notEnrolled");
    assertEquals(5, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));

    // set no possible enrollment users
    pf.setEnrollmentStatus("noEnrollmentPossible");
    identityStorage.getIdentities(OrganizationIdentityProvider.NAME, SortBy.FULLNAME.getFieldName(), OrderBy.ASC.name(), true, null, null, "noEnrollmentPossible", 0, Integer.MAX_VALUE);
    assertEquals(1, identityStorage.getIdentitiesByProfileFilterCount("organization", pf));
  }

  /**
   * Tests {@link IdenityStorage#getIdentitiesByProfileFilterCount(String, ProfileFilter, int, int, boolean)}
   * 
   */
  public void testGetIdentitiesByProfileFilterAccessList() throws Exception {
    populateData();
    ProfileFilter pf = new ProfileFilter();
    
    List<Identity> identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 0, 20, false);
    assertEquals("Number of identities must be " + identities.size(), 5, identities.size());
    
    pf.setPosition("developer");
    pf.setName("FirstName");
    identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 0, 20, false);
    assertEquals("Number of identities must be " + identities.size(), 5, identities.size());
    
    try {
      identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, -1, 20, false);
    } catch (Exception ext) {
      assert false : "Can not get Identity by profile filter. " + ext ;
    } 
    
    try {
      identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 0, -1, false);
    } catch (Exception ext) {
      assert false : "Can not get Identity by profile filter. " + ext ;
    } 
    
    try {
      identities = identityStorage.getIdentitiesByProfileFilter("organization", pf, 30, 40, false);
    } catch (Exception ext) {
      assert false : "Can not get Identity by profile filter. " + ext ;
    } 
  }
  
  /**
   * Tests {@link IdenityStorage#findIdentityByProfileFilterCount(String, ProfileFilter)}
   * 
   */
  public void testUpdateIdentity() throws Exception {
    String providerId = OrganizationIdentityProvider.NAME;
    String newProviderId = "space";
    String userName = "userIdentity1";
    Identity identity = populateIdentity(userName, true);
    assertNotNull("Identity must not be null", identity);
    assertEquals("Identity status must be " + identity.isDeleted(), false, identity.isDeleted());
    identity.setDeleted(true);
    identityStorage.updateIdentity(identity);
    Identity updatedIdentity = identityStorage.findIdentity(providerId, userName);
    assertEquals("Identity status must be " + updatedIdentity.isDeleted(), true, updatedIdentity.isDeleted());
    identity.setProviderId(newProviderId);
    identity.setDeleted(false);
    identityStorage.updateIdentity(identity);
    updatedIdentity = identityStorage.findIdentity(newProviderId, userName);
    tearDownIdentityList.add(updatedIdentity);
    assertEquals("Identity status must be " + updatedIdentity.isDeleted(), false, updatedIdentity.isDeleted());
    assertEquals("Identity provider id must be " + updatedIdentity.getProviderId(), newProviderId, updatedIdentity.getProviderId());
  }
  
  /**
   *  Tests {@link IdenityStorage#getIdentitiesCount(String)}
   */
  public void testGetIdentitiesCount() throws Exception {
    int numberUser = 10;
    int numberDisableUser = 5;
    // create user
    List<Identity> identities = new ArrayList<Identity>();
    for (int i = 0; i < numberUser; i++) {
      Identity identity = new Identity(OrganizationIdentityProvider.NAME, "user" + i);
      identityStorage.saveIdentity(identity);
      identities.add(identity);
      tearDownIdentityList.add(identity);
    }
    assertEquals(10, identityStorage.getIdentitiesCount(OrganizationIdentityProvider.NAME));
    // disable users
    for (int i = 0; i < numberDisableUser; i++) {
      Identity identity = identities.get(i);
      identityStorage.processEnabledIdentity(identity, false);
    }
    assertEquals(numberUser - numberDisableUser, identityStorage.getIdentitiesCount(OrganizationIdentityProvider.NAME));
  }

  public void testGetSpaceMemberByProfileFilter() throws Exception {
    populateData();
    populateSpaceData();
    populateUser("username4");
    populateUser("username1");
    populateUser("username5");
    populateUser("username6");

    Space space = new Space();
    space.setApp("app");
    space.setDisplayName("my space");
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space ");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId(SpaceUtils.createGroup(space.getPrettyName(), "username4"));
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {};
    String[] members = new String[] {"username1", "username2", "username3", "abc", "acb", "bac", "bca", "cab", "cba"};
    String[] invitedUsers = new String[] {"username5"};
    String[] pendingUsers = new String[] {"username6"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);

    spaceStorage.saveSpace(space, true);
    tearDownSpaceList.add(space);
    
    ProfileFilter profileFilter = new ProfileFilter();
    ProfileFilter firstProfileFilter = new ProfileFilter();

    // Test on first character field choice
    profileFilter.setSorting(new Sorting(SortBy.FULLNAME, Sorting.OrderBy.ASC));
    List<Identity> identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, SpaceMemberFilterListAccess.Type.MEMBER, 0, 9);
    assertEquals(9, identities.size());
    assertEquals("First member in list should be 'abc'", "abc", identities.get(0).getRemoteId());
    assertEquals("Second member in list should be 'acb'", "acb", identities.get(1).getRemoteId());
    // reset first character field name to default

    // Test on Sort direction
    profileFilter.setSorting(new Sorting(Sorting.SortBy.FIRSTNAME, Sorting.OrderBy.DESC));
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, SpaceMemberFilterListAccess.Type.MEMBER, 0, 9);
    assertEquals(9, identities.size());
    assertEquals("First member in list should be 'username3'", "username3", identities.get(0).getRemoteId());
    assertEquals("Second member in list should be 'username2'", "username2", identities.get(1).getRemoteId());

    // Test by combining Sort direction, field and first character
    profileFilter.setSorting(new Sorting(Sorting.SortBy.LASTNAME, Sorting.OrderBy.DESC));
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, SpaceMemberFilterListAccess.Type.MEMBER, 0, 9);
    assertEquals(9, identities.size());
    assertEquals("First member in list should be 'username3'", "username3", identities.get(0).getRemoteId());
    assertEquals("Second member in list should be 'username2'", "username2", identities.get(1).getRemoteId());

    profileFilter.setSorting(new Sorting(Sorting.SortBy.LASTNAME, Sorting.OrderBy.ASC));
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, profileFilter, SpaceMemberFilterListAccess.Type.MEMBER, 0, 9);
    assertEquals(9, identities.size());
    assertEquals("First member in list should be 'cba'", "cba", identities.get(0).getRemoteId());
    assertEquals("Second member in list should be 'bca'", "bca", identities.get(1).getRemoteId());

    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, firstProfileFilter, SpaceMemberFilterListAccess.Type.MEMBER, 0, 2);
    assertEquals(2, identities.size());

    Identity username1Identity = identityManager.getOrCreateUserIdentity("username1");
    tearDownIdentityList.add(username1Identity);
    tearDownIdentityList.add(identityManager.getOrCreateUserIdentity("username4"));
    firstProfileFilter.setViewerIdentity(username1Identity);
    assertEquals(8, identityStorage.countSpaceMemberIdentitiesByProfileFilter(space, firstProfileFilter, SpaceMemberFilterListAccess.Type.MEMBER));

    addUserToGroupWithMembership("username4", space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, new ProfileFilter(), SpaceMemberFilterListAccess.Type.MANAGER, 0, 10);
    assertEquals(1, identities.size());

    assertEquals(1, identityStorage.countSpaceMemberIdentitiesByProfileFilter(space, new ProfileFilter(), SpaceMemberFilterListAccess.Type.INVITED));
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, new ProfileFilter(), SpaceMemberFilterListAccess.Type.INVITED, 0, 10);
    assertEquals(1, identities.size());

    assertEquals(1, identityStorage.countSpaceMemberIdentitiesByProfileFilter(space, new ProfileFilter(), SpaceMemberFilterListAccess.Type.PENDING));
    identities = identityStorage.getSpaceMemberIdentitiesByProfileFilter(space, new ProfileFilter(), SpaceMemberFilterListAccess.Type.PENDING, 0, 10);
    assertEquals(1, identities.size());
  }

  public void testGetAvatarInputStreamById() throws Exception {
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, System.currentTimeMillis());

    /*
      test on identity with @OrganizationIdentityProvider.NAME as providerId.
     */
    String userName = "userIdentity2";
    Identity identity = populateIdentity(userName);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    // within this instruction the profile is created implicitly and it does have a default avatar
    String identityId = identity.getId();
    assertNotNull(identityId);
    InputStream stream = identityStorage.getAvatarInputStreamById(identity);
    assertNotNull(stream);

    FileItem avatarFile = identityStorage.getAvatarFile(identity);
    assertNotNull(avatarFile);

    assertNotNull(avatarFile.getFileInfo().getId());
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.updateIdentity(identity);
    identityStorage.saveProfile(profile);
    profile = identityStorage.loadProfile(profile);
    // we load the profile to check if the avatar is well attached to it, as well as @Profile.avatarLastUpdated value
    Long avatarLastUpdated = profile.getAvatarLastUpdated();
    assertNotNull(avatarLastUpdated);
    assertFalse(profile.isDefaultAvatar());

    restartTransaction();
    //Wait a bit before updating the avatar to make sure the avatarLastUpdated is changed
    sleep(10);

    // we re-attach the the avatar to the profile to be sure that @Profile.avatarLastUpdated value is updated
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.updateProfile(profile);
    restartTransaction();
    sleep(10);

    profile = identityStorage.loadProfile(profile);
    Long avatarLastUpdated1 = profile.getAvatarLastUpdated();
    assertNotNull(avatarLastUpdated1);
    assertTrue(avatarLastUpdated1 > avatarLastUpdated);

    stream = identityStorage.getAvatarInputStreamById(identity);
    assertNotNull(stream);
    
    /*
      test on identity with @SpaceIdentityProvider.NAME as providerId.
     */
    Space space = this.getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    String remoteId = space.getPrettyName();
    assertNotNull(remoteId);
    identity = new Identity(SpaceIdentityProvider.NAME, remoteId);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    assertNotNull(identity.getId());
    assertNotNull(identity.getRemoteId());
    stream = identityStorage.getAvatarInputStreamById(identity);
    // the space does not have an avatar
    assertNotNull(stream);
    // we set the avatar to the space
    space.setAvatarAttachment(avatarAttachment);
    spaceStorage.saveSpace(space, false);
    space = spaceStorage.getSpaceByPrettyName(remoteId);
    
    identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    profile = new Profile(identity);
    // we set the avatar to the corresponding space profile
    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.saveIdentity(identity);
    identityStorage.saveProfile(profile);
    restartTransaction();

    profile = identityStorage.loadProfile(profile);
    avatarLastUpdated = profile.getAvatarLastUpdated();
    assertNotNull(avatarLastUpdated);

    restartTransaction();

    //Wait a bit before updating the avatar to make sure the avatarLastUpdated is changed
    sleep(10);

    profile.setProperty(Profile.AVATAR, avatarAttachment);
    identityStorage.updateProfile(profile);
    profile = identityStorage.loadProfile(profile);
    avatarLastUpdated1 = profile.getAvatarLastUpdated();
    assertNotNull(avatarLastUpdated1);
    assertNotSame(avatarLastUpdated1, avatarLastUpdated);
    // we check that the  @Profile.avatarLastUpdated is updated with greater value
    assertTrue(avatarLastUpdated1 > avatarLastUpdated);
    
    tearDownIdentityList.add(identity);
    stream = identityStorage.getAvatarInputStreamById(identity);
    assertNotNull(stream);
  }

  public void testIdentityDefaultAvatar() throws Exception {
    String userName = "userIdentity2";
    Identity identity = populateIdentity(userName);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    String identityId = identity.getId();
    assertNotNull(identityId);

    FileItem avatarFile = identityStorage.getAvatarFile(identity);

    Profile profile = new Profile(identity);
    profile = identityStorage.loadProfile(profile);
    assertNotNull(avatarFile);
    assertNotNull(avatarFile.getFileInfo().getId());
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());
  }

  public void testGroupIdentityDefaultAvatar() throws Exception {
    // We check that any identities other than user or space identity shouldn't have
    // a generated default avatar
    Identity groupIdentity = new Identity("group", "testGroup");
    identityStorage.saveIdentity(groupIdentity);
    Profile groupProfile = new Profile(groupIdentity);
    identityStorage.saveProfile(groupProfile);
    groupIdentity.setProfile(groupProfile);
    tearDownIdentityList.add(groupIdentity);
    String groupIdentityId = groupIdentity.getId();
    assertNotNull(groupIdentityId);

    FileItem groupAvatarFile = identityStorage.getAvatarFile(groupIdentity);
    assertNull(groupAvatarFile);
  }

  public void testIdentityDefaultAvatarWhenRenameUserFirstName() throws Exception {
    String userName = "userIdentity2";
    Identity identity = populateIdentity(userName);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    String identityId = identity.getId();
    assertNotNull(identityId);

    FileItem avatarFile = identityStorage.getAvatarFile(identity);
    Profile profile = new Profile(identity);
    profile = identityStorage.loadProfile(profile);
    assertEquals(EntityConverterUtils.DEFAULT_AVATAR, avatarFile.getFileInfo().getName());
    assertTrue(profile.isDefaultAvatar());

    Long avatarFileId = avatarFile.getFileInfo().getId();
    assertNotNull(avatarFileId);

    profile.setProperty(Profile.FIRST_NAME, "alice");
    identityStorage.saveProfile(profile);
    restartTransaction();

    avatarFile = identityStorage.getAvatarFile(identity);
    assertNotNull(avatarFile);

    Long updatedAvatarFileId = avatarFile.getFileInfo().getId();
    assertNotNull(updatedAvatarFileId);

    assertNotEquals(avatarFileId, updatedAvatarFileId);
  }

  public void testGetBannerInputStreamById() throws Exception {
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    BannerAttachment bannerAttachment = new BannerAttachment(null, "banner", "png", inputStream, System.currentTimeMillis());

    /*
      test on identity with @OrganizationIdentityProvider.NAME as providerId.
     */
    String userName = "userIdentity3";
    Identity identity = populateIdentity(userName);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    // within this instruction the profile is created implicitly and it does not have an banner
    String identityId = identity.getId();
    assertNotNull(identityId);
    InputStream stream = identityStorage.getBannerInputStreamById(identity);
    assertNull(stream);

    //Wait a bit before updating the avatar to make sure the bannerLastUpdated is changed
    sleep(10);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityStorage.updateIdentity(identity);
    identityStorage.saveProfile(profile);
    profile = identityStorage.loadProfile(profile);
    // we load the profile to check if the banner is well attached to it, as well as @Profile.bannerLastUpdated value
    Long bannerLastUpdated = profile.getBannerLastUpdated();
    assertNotNull(bannerLastUpdated);

    restartTransaction();

    // we re-attach the the banner to the profile to be sure that @Profile.bannerLastUpdated value is updated
    profile.setProperty(Profile.BANNER, bannerAttachment);
    // Wait 10 ms before committing to make sure that the bannerLastUpdated is changed
    sleep(10);
    identityStorage.updateProfile(profile);
    profile = identityStorage.loadProfile(profile);
    Long bannerLastUpdated1 = profile.getBannerLastUpdated();
    assertNotNull(bannerLastUpdated1);
    assertNotSame(bannerLastUpdated1, bannerLastUpdated);
    assertTrue(bannerLastUpdated1 > bannerLastUpdated);

    stream = identityStorage.getBannerInputStreamById(identity);
    assertNotNull(stream);

    /*
      test on identity with @SpaceIdentityProvider.NAME as providerId.
     */
    Space space = this.getSpaceInstance(1);
    spaceStorage.saveSpace(space, true);
    String remoteId = space.getPrettyName();
    assertNotNull(remoteId);
    identity = new Identity(SpaceIdentityProvider.NAME, remoteId);
    identityStorage.saveIdentity(identity);
    assertNotNull(identity.getId());
    assertNotNull(identity.getRemoteId());
    stream = identityStorage.getBannerInputStreamById(identity);
    // the space does not have an banner
    assertNull(stream);
    // we set the banner to the space
    space.setBannerAttachment(bannerAttachment);
    spaceStorage.saveSpace(space, false);
    space = spaceStorage.getSpaceByPrettyName(remoteId);

    identity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    profile = new Profile(identity);
    // we set the banner to the corresponding space profile
    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityStorage.saveIdentity(identity);
    identityStorage.saveProfile(profile);
    profile = identityStorage.loadProfile(profile);
    bannerLastUpdated = profile.getBannerLastUpdated();
    assertNotNull(bannerLastUpdated);

    restartTransaction();

    //Wait a bit before updating the avatar to make sure the bannerLastUpdated is changed
    sleep(10);

    profile.setProperty(Profile.BANNER, bannerAttachment);
    identityStorage.updateProfile(profile);
    profile = identityStorage.loadProfile(profile);
    bannerLastUpdated1 = profile.getBannerLastUpdated();
    assertNotNull(bannerLastUpdated1);
    assertNotSame(bannerLastUpdated1, bannerLastUpdated);
    // we check that the  @Profile.bannerLastUpdated is updated with greater value
    assertTrue(bannerLastUpdated1 > bannerLastUpdated);

    tearDownIdentityList.add(identity);
    stream = identityStorage.getBannerInputStreamById(identity);
    assertNotNull(stream);
  }

  public void testUpdateProfile() throws Exception {
    String userName = "userIdentity4";
    Identity identity = populateIdentity(userName);
    identityStorage.saveIdentity(identity);
    tearDownIdentityList.add(identity);

    Profile profile = identity.getProfile();
    profile.setProperty(Profile.GENDER, "male");
    profile.setProperty(Profile.POSITION, "developer");
    identityStorage.updateProfile(profile);

    identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);
    assertNotNull(identity);
    assertNotNull(identity.getProfile());
    assertEquals("male", identity.getProfile().getGender());
    assertEquals("developer", identity.getProfile().getPosition());

    profile.removeProperty(Profile.POSITION);
    identityStorage.updateProfile(profile);

    identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);
    assertNotNull(identity);
    assertNotNull(identity.getProfile());
    assertEquals("male", identity.getProfile().getGender());
    assertNull(identity.getProfile().getPosition());

    profile.setProperty("multi-field", Collections.singletonList("new field"));
    identityStorage.updateProfile(profile);

    identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);
    assertNotNull(identity);
    assertNotNull(identity.getProfile());
    assertNotNull(identity.getProfile().getProperty("multi-field"));
    assertTrue(identity.getProfile().getProperty("multi-field") instanceof ArrayList<?>);

    profile.removeProperty("multi-field");
    identityStorage.updateProfile(profile);

    identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);
    assertNotNull(identity);
    assertNotNull(identity.getProfile());
    assertNull(identity.getProfile().getProperty("multi-field"));
  }

  /**
   * Populate one identity with remoteId.
   * 
   * @param remoteId
   * @return
   */
  private Identity populateIdentity(String remoteId) {
    return populateIdentity(remoteId, false);
  }

  /**
   * Populates one identity with remoteId.
   *
   * @param remoteId
   * @param addedToTearDown
   * @return
   */
  private Identity populateIdentity(String remoteId, boolean addedToTearDown) {
    String providerId = "organization";
    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, remoteId);
    profile.setProperty(Profile.LAST_NAME, "gtn");
    profile.setProperty(Profile.FULL_NAME, remoteId + " " +  "gtn");
    profile.setProperty(Profile.POSITION, "developer");
    profile.setProperty(Profile.GENDER, "male");
    identityStorage.saveProfile(profile);

    identity.setProfile(profile);
    if (addedToTearDown) {
      tearDownIdentityList.add(identity);
    }
    return identity;
  }
  
  private void populateData() {
    populateData(5);
  }
  
  private void populateData(int number) {
    String providerId = "organization";
    for (int i = 0; i < number; i++) {
      String remoteId = "username" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);
      tearDownIdentityList.add(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "FirstName" + i);
      profile.setProperty(Profile.LAST_NAME, "LastName" + i);
      profile.setProperty(Profile.FULL_NAME, "FirstName" + i + " " +  "LastName" + i);
      profile.setProperty("position", "developer");
      profile.setProperty("gender", "male");
      identity.setProfile(profile);
      identityStorage.saveProfile(profile);
    }
  }

  private void populateSpaceData() {
    String providerId = "organization";
    String[] spaceMembers = new String[] {"ABC", "ACB", "BAC", "BCA", "CAB", "CBA"};
    for (String member: spaceMembers) {
      String remoteId = member.toLowerCase();
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);
      StringBuilder sb = new StringBuilder(member).reverse();
      String lastName = sb.toString();

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, member);
      profile.setProperty(Profile.LAST_NAME, lastName);
      profile.setProperty(Profile.FULL_NAME, member + " " + lastName);
      profile.setProperty("position", "developer");
      profile.setProperty("gender", "male");
      identity.setProfile(profile);
      tearDownIdentityList.add(identity);
      identityStorage.saveProfile(profile);
    }
  }
  
  private User populateUser(String name) {
    OrganizationService os = SpaceUtils.getOrganizationService();
    User user = os.getUserHandler().createUserInstance(name);
    
    try {
      os.getUserHandler().createUser(user, false);
      identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, name);
    } catch (Exception e) {
      return null;
    }
    return user;
  }
  
  /**
   * Gets an instance of Space.
   *
   * @param number
   * @return an instance of space
   */
  private Space getSpaceInstance(int number) {
    Space space = new Space();
    space.setApp("app1,app2");
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] {"demo", "tom"};
    String[] members = new String[] {"raul", "ghost", "dragon"};
    String[] invitedUsers = new String[] {"register1", "mary"};
    String[] pendingUsers = new String[] {"jame", "paul", "hacker"};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    return space;
  }
  
  private static void addUserToGroupWithMembership(String remoteId, String groupId, String membership) {
    OrganizationService organizationService = SpaceUtils.getOrganizationService();
    try {
      // TODO: checks whether user is already manager?
      MembershipHandler membershipHandler = organizationService.getMembershipHandler();
      Membership found = membershipHandler.findMembershipByUserGroupAndType(remoteId, groupId, membership);
      if (found != null) {
        return;
      }
      User user = organizationService.getUserHandler().findUserByName(remoteId);
      MembershipType membershipType = organizationService.getMembershipTypeHandler().findMembershipType(membership);
      GroupHandler groupHandler = organizationService.getGroupHandler();
      Group existingGroup = groupHandler.findGroupById(groupId);
      membershipHandler.linkMembership(user, existingGroup, membershipType, true);
      persist();
    } catch (Exception e) {
      return;
    }
  }

  private void assertSorted(String remoteIdPrefix, String fieldName, List<Identity> result) {
    List<String> identitiesList = result.stream().map(identity -> identity.getProfile().getProperty(fieldName).toString()).collect(Collectors.toList());
    Iterator<String> iterator = identitiesList.iterator();
    while (iterator.hasNext()) {
      String username = (String) iterator.next();
      if (!username.startsWith(remoteIdPrefix)) {
        iterator.remove();
      }
    }
    List<String> identitiesListBackup = new ArrayList<>(identitiesList);
    Collections.sort(identitiesList);
    assertEquals("List '" + identitiesList + "' is not sorted", identitiesList, identitiesListBackup);
  }

}
