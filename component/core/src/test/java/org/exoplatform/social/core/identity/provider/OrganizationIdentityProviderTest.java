package org.exoplatform.social.core.identity.provider;

import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.organization.*;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.organization.impl.UserProfileImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.lenient;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationIdentityProviderTest {

    @Mock(extraInterfaces = ComponentRequestLifecycle.class)
    private OrganizationService organizationService;

    @Mock
    private UserHandler userHandler;

    @Mock
    private UserProfileHandler userProfileHandler;

    @Test
    public void shouldUpdateProfileWhenResetingGender() throws Exception {
        // Given
        Mockito.when(userHandler.findUserByName(Mockito.eq("john"))).thenReturn(new UserImpl("john"));
        Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
        UserProfileImpl existingProfile = new UserProfileImpl("john");
        existingProfile.setAttribute(Profile.GENDER, "Male");
        existingProfile.setAttribute(Profile.POSITION, "Product Manager");
        Mockito.when(userProfileHandler.findUserProfileByName("john")).thenReturn(existingProfile);
        Mockito.when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);

        OrganizationIdentityProvider organizationIdentityProvider = new OrganizationIdentityProvider(organizationService);

        Profile updatedProfile = new Profile(new Identity("john"));
        updatedProfile.setProperty(Profile.USERNAME, "john");
        updatedProfile.setProperty(Profile.GENDER, "");
        updatedProfile.setProperty(Profile.POSITION, "Product Manager");
        updatedProfile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));

        // When
        organizationIdentityProvider.onUpdateProfile(updatedProfile);

        // Then
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        Mockito.verify(userProfileHandler, Mockito.times(1)).saveUserProfile(userProfileCaptor.capture(), Mockito.eq(false));
        UserProfile savedProfile = userProfileCaptor.getValue();
        assertEquals("", savedProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]));
        assertEquals("Product Manager", savedProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]));
    }

    @Test
    public void shouldUpdateProfileWhenResetingPosition() throws Exception {
        // Given
        Mockito.when(userHandler.findUserByName(Mockito.eq("john"))).thenReturn(new UserImpl("john"));
        Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
        UserProfileImpl existingProfile = new UserProfileImpl("john");
        existingProfile.setAttribute(Profile.POSITION, "Product Manager");
        Mockito.when(userProfileHandler.findUserProfileByName(Mockito.eq("john"))).thenReturn(existingProfile);
        Mockito.when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);

        OrganizationIdentityProvider organizationIdentityProvider = new OrganizationIdentityProvider(organizationService);

        Profile updatedProfile = new Profile(new Identity("john"));
        updatedProfile.setProperty(Profile.USERNAME, "john");
        updatedProfile.setProperty(Profile.POSITION, "");
        updatedProfile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));

        // When
        organizationIdentityProvider.onUpdateProfile(updatedProfile);

        // Then
        ArgumentCaptor<UserProfile> userProfileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        Mockito.verify(userProfileHandler, Mockito.times(1)).saveUserProfile(userProfileCaptor.capture(), Mockito.eq(false));
        UserProfile savedProfile = userProfileCaptor.getValue();
        assertEquals("", savedProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]));
    }

    @Test
    public void testFindByRemoteId() throws Exception {
        String JOHN_USERNAME = "john";
        UserImpl JOHN = new UserImpl(JOHN_USERNAME);
        Mockito.when(organizationService.getUserHandler()).thenReturn(userHandler);
        OrganizationIdentityProvider organizationIdentityProvider = new OrganizationIdentityProvider(organizationService);

        // User john enabled
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.ANY)).thenReturn(JOHN);
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.ENABLED)).thenReturn(JOHN);
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.DISABLED)).thenReturn(null);

        assertNotNull(organizationIdentityProvider.findByRemoteId(JOHN_USERNAME));

        // User john disabled
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.ANY)).thenReturn(JOHN);
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.ENABLED)).thenReturn(null);
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.DISABLED)).thenReturn(JOHN);

        assertNotNull(organizationIdentityProvider.findByRemoteId(JOHN_USERNAME));

        // User john not found
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.ANY)).thenReturn(null);
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.ENABLED)).thenReturn(null);
        lenient().when(userHandler.findUserByName(JOHN_USERNAME, UserStatus.DISABLED)).thenReturn(null);

        assertNull(organizationIdentityProvider.findByRemoteId(JOHN_USERNAME));
    }
}
