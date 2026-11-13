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
package org.exoplatform.social.core.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;

@RunWith(MockitoJUnitRunner.class)
public class SynchronizedUserProfileListenerTest {

  private MockedStatic<CommonsUtils>                 commonsUtils;

  @Mock
  private IdentityManager                         identityManager;

  @Mock
  private ProfilePropertyService                  profilePropertyService;

  @Mock
  private IDMExternalStoreImportService           idmExternalStoreImportService;

  @Before
  public void openStaticMock() {
    commonsUtils = mockStatic(CommonsUtils.class);
  }

  @After
  public void closeStaticMock() {
    commonsUtils.close();
  }

  @Test
  public void onEventTest() throws Exception {
    Identity userIdentity = mock(Identity.class);
    String userName = "john";
    commonsUtils.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName)).thenReturn(userIdentity);
    Profile profile = new Profile();
    profile.setProperty("username", userName);
    Map<String, String> externalUserProfileProperties = new HashMap<>();
    externalUserProfileProperties.put("username", userName);
    externalUserProfileProperties.put("phones.home", "12345678");
    externalUserProfileProperties.put("phones.work", "12131415");
    externalUserProfileProperties.put("ims.facebook", "testImsProperty");
    when(userIdentity.getProfile()).thenReturn(profile);
    commonsUtils.when(() -> CommonsUtils.getService(ProfilePropertyService.class)).thenReturn(profilePropertyService);
    String[] propertySettingNames = new String[] { "firstName", "lastName", "email", "skills", "external", "phones",
        "phones.work", "phones.home", "urls", "ims", "ims.facebook", "ims.other" };
    when(profilePropertyService.getPropertySettingNames()).thenReturn(Arrays.asList(propertySettingNames));
    ProfilePropertySetting phonesPropertySetting = mock(ProfilePropertySetting.class);
    ProfilePropertySetting imsPropertySetting = mock(ProfilePropertySetting.class);
    when(phonesPropertySetting.getPropertyName()).thenReturn("phones");
    when(imsPropertySetting.getPropertyName()).thenReturn("ims");
    when(profilePropertyService.getProfileSettingByName(phonesPropertySetting.getPropertyName())).thenReturn(phonesPropertySetting);
    when(profilePropertyService.getProfileSettingByName(imsPropertySetting.getPropertyName())).thenReturn(imsPropertySetting);
    Event<IDMExternalStoreImportService, Map<String, String>> importUserProfile =
                                                                                new Event<>("exo.idm.externalStore.user.profile.new",
                                                                                            idmExternalStoreImportService,
                                                                                            externalUserProfileProperties);
    SynchronizedUserProfileListener synchronizedUserProfileListener = new SynchronizedUserProfileListener();
    synchronizedUserProfileListener.onEvent(importUserProfile);
    //
    assertEquals(3, profile.getProperties().size());

    ArrayList<Map<String, String>> expectedPhonesProperty = new ArrayList<>();
    Map<String, String> homePhonesMap = new HashMap<>();
    homePhonesMap.put("key", "phones.home");
    homePhonesMap.put("value", externalUserProfileProperties.get("phones.home"));
    //
    Map<String, String> workPhonesMap = new HashMap<>();
    workPhonesMap.put("key", "phones.work");
    workPhonesMap.put("value", externalUserProfileProperties.get("phones.work"));
    expectedPhonesProperty.add(homePhonesMap);
    expectedPhonesProperty.add(workPhonesMap);

    // assert the expected phones property settings
    assertEquals(profile.getProperty("phones"), expectedPhonesProperty);
    //
    ArrayList<Map<String, String>> expectedImsProperty = new ArrayList<>();
    Map<String, String> facebookImsMap = new HashMap<>();
    facebookImsMap.put("key", "ims.facebook");
    facebookImsMap.put("value", externalUserProfileProperties.get("ims.facebook"));
    expectedImsProperty.add(facebookImsMap);
    //
    assertEquals(profile.getProperty("ims"), expectedImsProperty);
    verify(identityManager, times(1)).updateProfile(profile, true);

  }


  private static byte[] pngBytes() throws Exception {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", out);
    return out.toByteArray();
  }

  private Profile prepare(Map<String, String> external) throws Exception {
    Identity userIdentity = mock(Identity.class);
    commonsUtils.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
    commonsUtils.when(() -> CommonsUtils.getService(ProfilePropertyService.class)).thenReturn(profilePropertyService);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john")).thenReturn(userIdentity);
    Profile profile = new Profile();
    profile.setProperty("username", "john");
    when(userIdentity.getProfile()).thenReturn(profile);
    when(profilePropertyService.getPropertySettingNames()).thenReturn(Arrays.asList("firstName"));
    new SynchronizedUserProfileListener().onEvent(new Event<>("exo.idm.externalStore.user.profile.new",
                                                              idmExternalStoreImportService,
                                                              external));
    return profile;
  }

  @Test
  public void profilePictureSetsAvatarOnlyWhenChanged() throws Exception {
    byte[] picture = pngBytes();
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", java.util.Base64.getEncoder().encodeToString(picture));
    Profile profile = prepare(external);
    assertNotNull(profile.getProperty(Profile.AVATAR));
    assertEquals("image/png", ((AvatarAttachment) profile.getProperty(Profile.AVATAR)).getMimeType());
    verify(identityManager, times(1)).updateProfile(profile, true);
  }

  @Test
  public void profilePictureMimeTypeFollowsItsFormat() throws Exception {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "jpg", out);
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", java.util.Base64.getEncoder().encodeToString(out.toByteArray()));
    Profile profile = prepare(external);
    assertEquals("image/jpeg", ((AvatarAttachment) profile.getProperty(Profile.AVATAR)).getMimeType());
  }

  @Test
  public void unchangedProfilePictureIsNotUploadedAgain() throws Exception {
    byte[] picture = pngBytes();
    when(identityManager.getAvatarInputStream(org.mockito.ArgumentMatchers.any())).thenReturn(new ByteArrayInputStream(picture));
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", java.util.Base64.getEncoder().encodeToString(picture));
    Profile profile = prepare(external);
    assertNull(profile.getProperty(Profile.AVATAR));
    verify(identityManager, never()).updateProfile(profile, true);
  }

  @Test
  public void profilePictureThatIsNotAnImageIsIgnored() throws Exception {
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", java.util.Base64.getEncoder().encodeToString("not an image".getBytes()));
    Profile profile = prepare(external);
    assertNull(profile.getProperty(Profile.AVATAR));
    verify(identityManager, never()).updateProfile(profile, true);
  }

  @Test
  public void profilePictureIsNeverCopiedAsAProfileProperty() throws Exception {
    byte[] picture = pngBytes();
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", java.util.Base64.getEncoder().encodeToString(picture));
    commonsUtils.when(() -> CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
    commonsUtils.when(() -> CommonsUtils.getService(ProfilePropertyService.class)).thenReturn(profilePropertyService);
    Identity userIdentity = mock(Identity.class);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john")).thenReturn(userIdentity);
    Profile profile = new Profile();
    profile.setProperty("username", "john");
    when(userIdentity.getProfile()).thenReturn(profile);
    // a profile property of that very name exists
    when(profilePropertyService.getPropertySettingNames()).thenReturn(Arrays.asList("profilePicture"));
    ProfilePropertySetting setting = mock(ProfilePropertySetting.class);
    // only read when the listener does not skip the key
    lenient().when(setting.getPropertyName()).thenReturn("profilePicture");
    lenient().when(profilePropertyService.getProfileSettingByName("profilePicture")).thenReturn(setting);

    new SynchronizedUserProfileListener().onEvent(new Event<>("exo.idm.externalStore.user.profile.new",
                                                              idmExternalStoreImportService,
                                                              external));

    assertNull(profile.getProperty("profilePicture"));
    assertNotNull(profile.getProperty(Profile.AVATAR));
  }

  private static byte[] imageBytes(int width, int height, String format) throws Exception {
    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
    assertTrue(javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(width,
                                                                            height,
                                                                            java.awt.image.BufferedImage.TYPE_INT_RGB),
                                           format,
                                           out));
    return out.toByteArray();
  }

  private Profile prepareWithPicture(byte[] picture) throws Exception {
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", java.util.Base64.getEncoder().encodeToString(picture));
    return prepare(external);
  }

  @Test
  public void gifProfilePictureIsAccepted() throws Exception {
    Profile profile = prepareWithPicture(imageBytes(2, 2, "gif"));
    assertEquals("image/gif", ((AvatarAttachment) profile.getProperty(Profile.AVATAR)).getMimeType());
  }

  @Test
  public void tooLargeProfilePictureIsIgnoredBeforeReadingTheStoredAvatar() throws Exception {
    Profile profile = prepareWithPicture(new byte[1024 * 1024 + 1]);
    assertNull(profile.getProperty(Profile.AVATAR));
    verify(identityManager, never()).getAvatarInputStream(org.mockito.ArgumentMatchers.any());
    verify(identityManager, never()).updateProfile(profile, true);
  }

  @Test
  public void profilePictureWithTooManyPixelsIsIgnored() throws Exception {
    // too wide, and too tall: each side has its own bound
    assertNull(prepareWithPicture(imageBytes(2049, 1, "png")).getProperty(Profile.AVATAR));
    assertNull(prepareWithPicture(imageBytes(1, 2049, "png")).getProperty(Profile.AVATAR));
    // the limit is inclusive on both sides
    assertNotNull(prepareWithPicture(imageBytes(2048, 1, "png")).getProperty(Profile.AVATAR));
    assertNotNull(prepareWithPicture(imageBytes(1, 2048, "png")).getProperty(Profile.AVATAR));
  }

  @Test
  public void profilePictureOfExactlyTheMaximumSizePassesTheSizeCheck() throws Exception {
    // not an image, so it is ignored, but only after the size check let it through
    // and the stored avatar was read: the size limit is inclusive
    Profile profile = prepareWithPicture(new byte[1024 * 1024]);
    assertNull(profile.getProperty(Profile.AVATAR));
    verify(identityManager).getAvatarInputStream(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void profilePictureInAFormatBrowsersDoNotRenderIsIgnored() throws Exception {
    Profile profile = prepareWithPicture(imageBytes(2, 2, "tiff"));
    assertNull(profile.getProperty(Profile.AVATAR));
  }

  @Test
  public void invalidProfilePictureIsIgnored() throws Exception {
    Map<String, String> external = new HashMap<>();
    external.put("username", "john");
    external.put("profilePicture", "[B@1a2b3c");
    Profile profile = prepare(external);
    assertNull(profile.getProperty(Profile.AVATAR));
  }
}
