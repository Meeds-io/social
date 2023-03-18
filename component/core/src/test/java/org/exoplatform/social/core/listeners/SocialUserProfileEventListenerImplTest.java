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
package org.exoplatform.social.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.storage.cache.SocialStorageCacheService;
import org.exoplatform.social.core.test.AbstractCoreTest;

public class SocialUserProfileEventListenerImplTest extends AbstractCoreTest {
  private final Log  LOG = ExoLogger.getLogger(SocialUserProfileEventListenerImplTest.class);

  private IdentityManager identityManager;
  

  private List<Identity>  tearDownIdentityList;

  private OrganizationService organizationService;
  private SocialStorageCacheService cacheService;
  private ProfilePropertyService profilePropertyService;
  private Identity paul;
  private Identity raul;

  public SocialUserProfileEventListenerImplTest() {
    setForceContainerReload(true);
  }

  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    organizationService = (OrganizationService) getContainer().getComponentInstanceOfType(OrganizationService.class);
    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    cacheService = getContainer().getComponentInstanceOfType(SocialStorageCacheService.class);
    cacheService.getIdentityCache().clearCache();
    cacheService.getIdentityIndexCache().clearCache();

    paul = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
    raul = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul", true);
    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(paul);
    tearDownIdentityList.add(raul);
    org.exoplatform.services.security.Identity identity = getService(IdentityRegistry.class).getIdentity("root");
    ConversationState.setCurrent(new ConversationState(identity));
  }

  public void tearDown() throws Exception {
    
    for (Identity identity : tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    super.tearDown();
  }

  /**
   * This testcase what will use for unit testing with scenario to 
   * synchronous profile from Social to Portal's Organization
   * @throws Exception
   */
  public void testSynchronizeFromSocialToPortal() throws Exception {
    String paulRemoteId = "paul";
    Identity paulIdentity = populateProfile(paul);
    assertNotNull(paulIdentity);
    //
    UserProfile pProfile = organizationService.getUserProfileHandler().findUserProfileByName(paulRemoteId);
    assertNotNull(pProfile);
    
    //
    assertEquals(paulIdentity.getProfile().getPosition(), pProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]));//user.jobtitle
    
    //update firstName
    {
      Identity identity1 = updateProfileKeyValue(paulIdentity, Profile.FIRST_NAME, "thanh");
      User pUser1 = organizationService.getUserHandler().findUserByName(paulRemoteId);
      //
      assertEquals(identity1.getProfile().getProperty(Profile.FIRST_NAME), pUser1.getFirstName());
    }
    
    //update lastName
    {
      Identity identity1 = updateProfileKeyValue(paulIdentity, Profile.LAST_NAME, "vu");
      User pUser1 = organizationService.getUserHandler().findUserByName(paulRemoteId);
      //
      assertEquals(identity1.getProfile().getProperty(Profile.LAST_NAME), pUser1.getLastName());
    }
    
    //update leader
    {
      Identity identity1 = updateProfilePosition(paulIdentity, "leader");
      UserProfile pProfile1 = organizationService.getUserProfileHandler().findUserProfileByName(paulRemoteId);
      //
      assertEquals(identity1.getProfile().getPosition(), pProfile1.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]));//user.jobtitle

    }
    
    //update gender
    {
      Identity identity2 = updateProfileGender(paulIdentity, "male");
      UserProfile pProfile2 = organizationService.getUserProfileHandler().findUserProfileByName(paulRemoteId);
      //user.gender
      assertEquals(identity2.getProfile().getProperty(Profile.GENDER), pProfile2.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]));

    }
  }
  
  /**
   * This TestCase what will use for unit testing with scenario to 
   * synchronous profile from Portal's Organization to Social
   * @throws Exception
   */
  public void testSynchronizeFromProtalToSocial() throws Exception {
    String raulRemoteId = "raul";
    Identity raulIdentity = populateProfile(raul);
    assertNotNull(raulIdentity);
    
    User pUser = organizationService.getUserHandler().findUserByName(raulRemoteId);
    assertNotNull(pUser);
    //
    Profile sProfile = identityManager.getProfile(raulIdentity);
    assertNotNull(sProfile);
    
    assertEquals(sProfile.getProperty(Profile.FIRST_NAME), pUser.getFirstName());
    assertEquals(sProfile.getProperty(Profile.LAST_NAME), pUser.getLastName());
    
  }
  /**
   * Populates the list of identities by specifying the number of items and to indicate if they are added to
   * the tear-down list.
   *
   * @param identity
   */
  private Identity populateProfile(Identity identity) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    Profile profile = identity.getProfile();
    assertNotNull(profile);
    
    //BASIC_INFO
    profile.setProperty(Profile.FIRST_NAME, "FirstName");
    profile.setProperty(Profile.LAST_NAME, "LastName");
    profile.setProperty(Profile.FULL_NAME, "FirstName" + " " +  "LastName");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    
    //CONTACT INFO
    profile.setProperty(Profile.GENDER, "fmale");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    

    //POSITION
    profile.setProperty(Profile.POSITION, "developer");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);

    RequestLifeCycle.end();
    identity.setProfile(profile);
    
    return identity;

  }
  
  private Identity updateProfilePosition(Identity identity, String position) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    Profile profile = identity.getProfile();
    assertNotNull(profile);
    //
    profile.setProperty(Profile.POSITION, position);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    
    //profile.setProperty(Profile.CONTACT_PHONES, new String[]{"098939179"});
    //profile.setProperty(Profile.CONTACT_URLS, new String[]{"http://exoplatform.com"});

    RequestLifeCycle.end();
    identity.setProfile(profile);
    return identity;

  }
  
  private Identity updateProfileGender(Identity identity, String gender) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    Profile profile = identity.getProfile();
    assertNotNull(profile);
        
    profile.setProperty(Profile.GENDER, gender);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    
    RequestLifeCycle.end();
    identity.setProfile(profile);
    return identity;

  }
  
  private Identity updateProfileKeyValue(Identity identity, String key, String value) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    Profile profile = identity.getProfile();
    assertNotNull(profile);
        
    profile.setProperty(key, value);
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);
    
    RequestLifeCycle.end();
    identity.setProfile(profile);
    return identity;

  }

  public void testSynchronizeGateinProfileToSocialProfile() throws Exception {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName("postalCode");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    profilePropertyService.createPropertySetting(profilePropertySetting);

    String raulRemoteId = "raul";
    UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(raulRemoteId);
    userProfile.setAttribute("postalCode", "2100");
    organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);

    Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, raulRemoteId).getProfile();
    assertNotNull(profile);
    assertEquals("2100", profile.getProperty("postalCode"));
  }

  public void testIgnoreExcludedProfileProperties() throws Exception {
    String raulRemoteId = "raul";
    UserProfile userProfile = organizationService.getUserProfileHandler().findUserProfileByName(raulRemoteId);
    userProfile.setAttribute("propertyToIgnore", "value1");
    userProfile.setAttribute("propertyNotToIgnore", "Yes !");
    organizationService.getUserProfileHandler().saveUserProfile(userProfile, true);

    Profile profile = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, raulRemoteId).getProfile();
    assertNotNull(profile);

    // propertyToIgnore should be ignored
    assertNull(profile.getProperty("propertyToIgnore"));

    // propertyNotToIgnore should not be ignored
    assertNotNull("Property should not be saved in social profile", profile.getProperty("propertyNotToIgnore"));
    assertEquals("Yes !", profile.getProperty("propertyNotToIgnore"));
    assertTrue(profilePropertyService.getPropertySettingNames().contains("propertyNotToIgnore"));
    ProfilePropertySetting profilePropertySetting = profilePropertyService.getProfileSettingByName("propertyNotToIgnore");
    assertNotNull(profilePropertySetting);
    assertTrue(profilePropertySetting.isActive());
    assertFalse(profilePropertySetting.isVisible());
  }
}
