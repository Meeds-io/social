package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.settings.ProfilePropertySettingsService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class GroupSynchronizationSocialProfileListenerTest extends AbstractCoreTest {

  private OrganizationService            organizationService;

  private IdentityManager                identityManager;

  private ProfilePropertySettingsService profilePropertySettingsService;

  private Identity                       paul;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    profilePropertySettingsService = getContainer().getComponentInstanceOfType(ProfilePropertySettingsService.class);

    paul = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
  }

  @Test
  public void testProfilePropertiesGroupSynchronization() throws Exception {
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setActive(true);
    profilePropertySetting.setEditable(true);
    profilePropertySetting.setVisible(true);
    profilePropertySetting.setPropertyName("postalCode");
    profilePropertySetting.setGroupSynchronized(true);
    profilePropertySetting.setMultiValued(false);
    profilePropertySetting.setParentId(0L);
    profilePropertySetting.setOrder(0L);
    profilePropertySettingsService.createPropertySetting(profilePropertySetting);
    profilePropertySetting.setPropertyName("street");
    profilePropertySettingsService.createPropertySetting(profilePropertySetting);

    String paulRemoteId = "paul";
    Profile profile = paul.getProfile();
    profile.setProperty("postalCode", "2100");
    identityManager.updateProfile(profile, true);

    Group group = organizationService.getGroupHandler().findGroupById("/profile/postalCode/2100");
    assertNotNull(group);
    Collection<Group> groups = organizationService.getGroupHandler().findGroupByMembership(paulRemoteId, "member");
    assertTrue(groups.contains(group));
    Group group1 = organizationService.getGroupHandler().findGroupById("/profile/street");
    assertNull(group1);
  }
}
