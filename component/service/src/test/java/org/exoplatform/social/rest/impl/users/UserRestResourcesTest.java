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
package org.exoplatform.social.rest.impl.users;

import io.meeds.portal.plugin.AclPlugin;
import io.meeds.social.core.identity.model.UserImportResult;
import io.meeds.social.organizationalunit.plugin.GroupAclPlugin;
import io.meeds.social.core.identity.service.UserExportService;
import io.meeds.social.core.identity.service.UserImportService;
import io.meeds.web.security.service.OtpService;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.rest.UserAccountConfirmation;
import org.exoplatform.portal.rest.UserAccountConfirmationSettings;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.thumbnail.ImageThumbnailService;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileSearchConnector;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.mock.MockUploadService;
import org.exoplatform.social.core.model.ProfileLabel;
import org.exoplatform.social.core.profilelabel.ProfileLabelService;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.api.ErrorResource;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.rest.entity.ProfilePropertySettingEntity;
import org.exoplatform.social.rest.impl.user.UserRest;
import org.exoplatform.social.service.test.AbstractResourceTest;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.UserProfile;
import java.util.Locale;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.idm.UserImpl;
import java.util.concurrent.atomic.AtomicBoolean;
import org.exoplatform.services.organization.idm.cache.CacheableUserHandlerImpl;
import org.mockito.ArgumentCaptor;
import org.exoplatform.social.rest.entity.OnboardingEmailEntity;
import org.exoplatform.portal.Constants;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MultivaluedMap;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class UserRestResourcesTest extends AbstractResourceTest {

  private ProfilePropertyService       profilePropertyService;

  private ProfileLabelService          profileLabelService;

  private UserACL                      userACL;

  private OtpService                   otpService;

  private RelationshipManager          relationshipManager;

  private SpaceService                 spaceService;

  private OrganizationService          organizationService;

  private ContinuationService          continuationService;

  private EXoContinuationBayeux        eXoContinuationBayeux;

  private UserStateService             userStateService;

  private MockUploadService            uploadService;

  private UserExportService            userExportService;

  private UserImportService            userImportService;

  private ImageThumbnailService        imageThumbnailService;

  private PasswordRecoveryService      passwordRecoveryService;

  private LocaleConfigService          localeConfigService;

  private SettingService               settingService;

  private Identity                     rootIdentity;

  private Identity                     johnIdentity;

  private Identity                     maryIdentity;

  private Identity                     demoIdentity;
  
  private SettingService               settingsService;

  private List<ProfilePropertySetting> tearDownProfilePropertyList = new ArrayList<>();

  private UserAccountConfirmationSettings confirmationSettings;

  private ProfilePropertyService       profilePropertyServiceSpy;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    profilePropertyService = getContainer().getComponentInstanceOfType(ProfilePropertyService.class);
    profileLabelService = getContainer().getComponentInstanceOfType(ProfileLabelService.class);
    userACL = getContainer().getComponentInstanceOfType(UserACL.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    settingsService = getContainer().getComponentInstanceOfType(SettingService.class);
    continuationService = mock(ContinuationService.class);
    userStateService = new UserStateService(continuationService,
                                            getContainer().getComponentInstanceOfType(CacheService.class),
                                            settingsService);
    uploadService = (MockUploadService) getContainer().getComponentInstanceOfType(UploadService.class);
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    userExportService = mock(UserExportService.class);
    userImportService = new UserImportService();
    userImportService.setIdentityManager(identityManager);
    userImportService.setOrganizationService(organizationService);
    userImportService.setProfilePropertyService(profilePropertyService);
    userImportService.setLocaleConfigService(localeConfigService);
    userImportService.setPasswordRecoveryService(passwordRecoveryService);
    userImportService.setUploadService(uploadService);

    imageThumbnailService = getContainer().getComponentInstanceOfType(ImageThumbnailService.class);
    passwordRecoveryService = getContainer().getComponentInstanceOfType(PasswordRecoveryService.class);
    localeConfigService = getContainer().getComponentInstanceOfType(LocaleConfigService.class);
    settingService = getContainer().getComponentInstanceOfType(SettingService.class);
    otpService = mock(OtpService.class);
    profilePropertyServiceSpy = spy(profilePropertyService);
    // The confirmation guard is switched off for the tests of the endpoints'
    // own behaviour; the guard tests switch it back on
    InitParams confirmationParams = new InitParams();
    ValueParam enabledParam = new ValueParam();
    enabledParam.setName(UserAccountConfirmationSettings.ENABLED_PARAM);
    enabledParam.setValue("false");
    confirmationParams.addParameter(enabledParam);
    confirmationSettings = new UserAccountConfirmationSettings(confirmationParams, organizationService, userACL);
    getContainer().registerComponentInstance(UserAccountConfirmationSettings.class, confirmationSettings);
    confirmedSessionUser = null;
    rootIdentity = createIdentity("root");
    johnIdentity = createIdentity("john");
    maryIdentity = createIdentity("mary");
    demoIdentity = createIdentity("demo");

    assertNotNull(rootIdentity);
    assertNotNull(johnIdentity);
    assertNotNull(maryIdentity);
    assertNotNull(demoIdentity);
    UserRest userRestResourcesV1 = new UserRest(userACL,
                                                organizationService,
                                                identityManager,
                                                relationshipManager,
                                                userStateService,
                                                spaceService,
                                                uploadService,
                                                userExportService,
                                                userImportService,
                                                imageThumbnailService,
                                                profilePropertyServiceSpy,
                                                passwordRecoveryService,
                                                localeConfigService,
                                                settingService,
                                                otpService);
    registry(userRestResourcesV1);
  }

  public void tearDown() throws Exception {
    getContainer().unregisterComponent(UserAccountConfirmationSettings.class);
    confirmedSessionUser = null;
    super.tearDown();
    for (ProfilePropertySetting propertySetting : tearDownProfilePropertyList) {
      profilePropertyService.deleteProfilePropertySetting(propertySetting.getId());
    }
    removeResource(UserRest.class);
  }

  public void testGetAllUsers() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());
  }

  public void testSearchUsers() throws Exception {

    startSessionAs("root");
    // when
    ContainerResponse response = service("GET", getURLResource("users?q=mar&isDisabled=true&limit=5&offset=0"), "", null, null);
    // then
    assertEquals(400, response.getStatus());
    Map<String, String> error = (Map<String, String>) response.getEntity();
    assertEquals("Unsupported operation: Can't search for disabled users!", error.get("error"));

    // when
    organizationService.getUserHandler().setEnabled("mary", false, false);
    response = service("GET", getURLResource("users?q=mar&isDisabled=true&limit=5&offset=0"), "", null, null);

    // then
    assertEquals(400, response.getStatus());
    error = (Map<String, String>) response.getEntity();
    assertEquals("Unsupported operation: Can't search for disabled users!", error.get("error"));

    // then
    organizationService.getUserHandler().setEnabled("mary", true, false);
    response = service("GET", getURLResource("users?q=mar&isDisabled=true&limit=5&offset=0"), "", null, null);

    // then
    assertEquals(400, response.getStatus());
    error = (Map<String, String>) response.getEntity();
    assertEquals("Unsupported operation: Can't search for disabled users!", error.get("error"));

    // test when isDisabled false
    removeResource(UserRest.class);
    identityManager = mock(IdentityManager.class);

    ListAccess<Identity> identityListAccess = new ListAccess<Identity>() {
      public Identity[] load(int index, int length) {
        List<Identity> identities = new ArrayList();
        identities.add(maryIdentity);
        Identity[] result = new Identity[identities.size()];
        return identities.toArray(result);
      }

      public int getSize() {
        return 1;
      }
    };

    when(identityManager.getIdentitiesByProfileFilter(anyString(), any(), anyBoolean())).thenReturn(identityListAccess);

    UserRest userRestResources = new UserRest(userACL,
                                              organizationService,
                                              identityManager,
                                              relationshipManager,
                                              userStateService,
                                              spaceService,
                                              uploadService,
                                              userExportService,
                                              userImportService,
                                              imageThumbnailService,
                                              profilePropertyService,
                                              passwordRecoveryService,
                                              localeConfigService,
                                              settingService,
                                              otpService);
    registry(userRestResources);

    // when
    response = service("GET", getURLResource("users?q=mar&limit=5&offset=0"), "", null, null);

    // then
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // when
    response = service("GET", getURLResource("users?q=mar&isDisabled=false&limit=5&offset=0"), "", null, null);

    // then
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testGetDisabledUsers() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users?limit=5&offset=0&isDisabled=true"), "", null, null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertNotNull(collections);
    int initialSize = collections.getEntities().size();

    // when
    maryIdentity.setEnable(false);
    johnIdentity.setEnable(false);
    identityManager.updateIdentity(maryIdentity);
    identityManager.updateIdentity(johnIdentity);
    response = service("GET", getURLResource("users?limit=5&offset=0&isDisabled=true"), "", null, null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(initialSize + 2, collections.getEntities().size());

    // when
    maryIdentity.setEnable(true);
    johnIdentity.setEnable(true);
    identityManager.updateIdentity(maryIdentity);
    identityManager.updateIdentity(johnIdentity);
    response = service("GET", getURLResource("users?limit=5&offset=0&isDisabled=true"), "", null, null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(initialSize, collections.getEntities().size());
  }

  public void testGetUsersByUserType() throws Exception {
    startSessionAs("root");
    Profile profile = maryIdentity.getProfile();
    profile.setProperty(Profile.EXTERNAL, "true");
    identityManager.updateProfile(profile);

    // when
    ContainerResponse response = service("GET", getURLResource("users?limit=5&offset=0"), "", null, null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    // when
    response = service("GET", getURLResource("users?limit=5&offset=0&userType=internal"), "", null, null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(3, collections.getEntities().size());

    // when
    response = service("GET", getURLResource("users?limit=5&offset=0&userType=external"), "", null, null);
    // then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
  }

  public void testGetAllUsersWithExtraFields() throws Exception {
    Space spaceTest = getSpaceInstance(700, "root");
    spaceTest.getId();
    spaceTest.setMembers(new String[] { "mary", "john", "demo" });
    spaceService.updateSpace(spaceTest);

    relationshipManager.inviteToConnect(rootIdentity, maryIdentity);

    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);

    relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, rootIdentity);

    relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, maryIdentity);

    relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, demoIdentity);

    startSessionAs("root");
    ContainerResponse response =
                               service("GET",
                                       getURLResource("users?limit=5&offset=0&expand=all,connectionsCount,spacesCount,connectionsInCommonCount,relationshipStatus"),
                                       "",
                                       null,
                                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(4, collections.getEntities().size());

    List<? extends DataEntity> entities = collections.getEntities();
    for (DataEntity dataEntity : entities) {
      String username = dataEntity.get("username").toString();
      if (StringUtils.equals(username, "root")) {
        continue;
      }
      String connectionsCount = dataEntity.get("connectionsCount").toString();
      String spacesCount = dataEntity.get("spacesCount").toString();
      String connectionsInCommonCount = dataEntity.get("connectionsInCommonCount").toString();
      String relationshipStatus = dataEntity.get("relationshipStatus").toString();
      if (StringUtils.equals(username, "john")) {
        assertEquals("3", connectionsCount);
      } else {
        assertEquals("1", connectionsCount);
      }
      assertEquals("1", spacesCount);
      if (StringUtils.equals(username, "john")) {
        assertEquals("CONFIRMED", relationshipStatus);
      } else if (StringUtils.equals(username, "mary")) {
        assertEquals("OUTGOING", relationshipStatus);
      } else if (StringUtils.equals(username, "demo")) {
        assertEquals("INCOMING", relationshipStatus);
      }
      if (StringUtils.equals(username, "john")) {
        assertEquals("0", connectionsInCommonCount);
      } else {
        assertEquals("1", connectionsInCommonCount);
      }
    }
  }

  public void testGetOnlineUsers() throws Exception {
    startSessionAs("root");
    when(continuationService.getConnectedUserIds()).thenReturn(new LinkedHashSet<>(Arrays.asList("john", "mary")));

    ContainerResponse response = service("GET", getURLResource("users?status=online&limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size()); // john and mary
  }

  public void testGetOnlineUsersOfSpace() throws Exception {
    startSessionAs("root");

    Space spaceTest = getSpaceInstance(0, "root");
    String spaceId = spaceTest.getId();
    spaceTest.setMembers(new String[] { "john" });
    spaceService.updateSpace(spaceTest);

    when(continuationService.isPresent("john")).thenReturn(true);
    when(continuationService.isPresent("mary")).thenReturn(true);

    ContainerResponse response = service("GET", getURLResource("users?status=online&spaceId=" + spaceId), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size()); // only john
    // non existing space
    response = service("GET", getURLResource("users?status=online&spaceId=600"), "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());
    ErrorResource errorResource = (ErrorResource) response.getEntity();
    assertEquals("space not found", errorResource.getDeveloperMessage());
    assertEquals("space 600 does not exist", errorResource.getMessage());
  }

  public void testGetUserById() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/john?expand=settings"), "", null, null);
    String etag = response.getHttpHeaders().get("etag").toString();
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    ProfileEntity userEntity = getBaseEntity(response.getEntity(), ProfileEntity.class);
    assertEquals("john", userEntity.getUsername());

    ContainerResponse response1 = service("GET", getURLResource("users/john?expand=settings"), "", null, null);
    String etag1 = response1.getHttpHeaders().get("etag").toString();
    assertEquals(etag, etag1);

    ProfileLabel label = new ProfileLabel();
    label.setLabel("labelTest");
    label.setLanguage("en");
    label.setObjectType("profileProperty");
    label.setObjectId(profilePropertyService.getProfileSettingByName(Profile.LOCATION).getId().toString());
    profileLabelService.createLabel(label);
    ContainerResponse response2 = service("GET", getURLResource("users/john?expand=settings"), "", null, null);
    String etag2 = response2.getHttpHeaders().get("etag").toString();
    assertNotEquals(etag1, etag2);

    ContainerResponse response3 = service("GET", getURLResource("users/john?expand=settings"), "", null, null);
    String etag3 = response3.getHttpHeaders().get("etag").toString();
    assertEquals(etag2, etag3);
  }

  public void testGetUserProfilePropertiesById() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/john?expand=settings"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity dataEntity = (DataEntity) response.getEntity();
    ArrayList collections = (ArrayList) dataEntity.get("properties");
    assertFalse(collections.isEmpty());
  }

  public void testGetUserAvatarForAnonymous() throws Exception {
    String user = "john";

    uploadUserAvatar(user);

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    String avatarUrl = identity.getProfile().getAvatarUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("users/" + user + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    response = service("GET", getURLResource("users/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetUserAvatarForAuthentiticatedUser() throws Exception {
    String user = "john";

    uploadUserAvatar(user);

    startSessionAs("mary");

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    String avatarUrl = identity.getProfile().getAvatarUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("users/" + user + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("users/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/avatar"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetUserAvatarWithDefaultSize() throws Exception {
    String user = "john";

    uploadUserAvatar(user, "user.png");

    startSessionAs("mary");

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    String avatarUrl = identity.getProfile().getAvatarUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream((byte[]) response.getEntity()));
    assertEquals(100, receivedImage.getWidth());
    assertEquals(100, receivedImage.getHeight());

  }

  public void testGetUserAvatarWithOriginalSize() throws Exception {
    String user = "john";

    uploadUserAvatar(user, "user.png");

    startSessionAs("mary");

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    String avatarUrl = identity.getProfile().getAvatarUrl().replace("/portal/rest", "");
    avatarUrl += "&size=0x0";
    ContainerResponse response = service("GET", avatarUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    BufferedImage receivedImage = ImageIO.read(new ByteArrayInputStream((byte[]) response.getEntity()));
    assertEquals(512, receivedImage.getWidth());
    assertEquals(512, receivedImage.getHeight());

  }

  public void testGetUserBannerForAnonymous() throws Exception {
    String user = "john";

    uploadUserBanner(user);

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    String bannerUrl = identity.getProfile().getBannerUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("users/" + user + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(404, response.getStatus());

    response = service("GET", getURLResource("users/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetUserBannerForAuthentiticatedUser() throws Exception {
    String user = "john";

    uploadUserBanner(user);

    startSessionAs("mary");

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user);
    String bannerUrl = identity.getProfile().getBannerUrl().replace("/portal/rest", "");

    ContainerResponse response = service("GET", bannerUrl, "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("users/" + user + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    response = service("GET", getURLResource("users/" + LinkProvider.DEFAULT_IMAGE_REMOTE_ID + "/banner"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testGetConnectionsOfUser() throws Exception {
    startSessionAs("root");
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    ContainerResponse response = service("GET", getURLResource("users/root/connections?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    ProfileEntity userEntity = getBaseEntity(collections.getEntities().get(0), ProfileEntity.class);
    assertEquals("demo", userEntity.getUsername());
  }

  public void testGetInvitationsOfUser() throws Exception {
    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/connections/invitations?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    ProfileEntity userEntity = getBaseEntity(collections.getEntities().get(0), ProfileEntity.class);
    assertEquals("demo", userEntity.getUsername());
  }

  public void testGetPendingOfUser() throws Exception {
    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/connections/pending?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    ProfileEntity userEntity = getBaseEntity(collections.getEntities().get(0), ProfileEntity.class);
    assertEquals("demo", userEntity.getUsername());
  }

  public void testGetSpacesOfUser() throws Exception {
    getSpaceInstance(0, "root");
    getSpaceInstance(1, "john");
    getSpaceInstance(2, "demo");
    getSpaceInstance(3, "demo");
    getSpaceInstance(4, "mary");

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/root/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    startSessionAs("john");
    relationshipManager.inviteToConnect(johnIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, johnIdentity);
    response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET", getURLResource("users/demo/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    startSessionAs("john");
    response = service("GET", getURLResource("users/demo/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    startSessionAs("john");
    response = service("GET", getURLResource("users/mary/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(403, response.getStatus());

    startSessionAs("root");
    response = service("GET", getURLResource("users/demo/spaces?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
  }

  public void testGetSpacesOfUserHidesTheHiddenSpacesAConnectionIsNotIn() throws Exception {
    // john has a public space and a hidden one. mary and demo are both connected
    // to john; only demo is a member of the hidden space.
    getSpaceInstance(10, "john");
    Space hiddenSpace = getSpaceInstance(11, "john");
    hiddenSpace.setVisibility(Space.HIDDEN);
    hiddenSpace.setMembers(new String[] { "john", "demo" });
    spaceService.updateSpace(hiddenSpace);

    relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, maryIdentity);
    relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, demoIdentity);

    // A connection who is not a member of the hidden space does not receive it
    // (eXIP note 50524, Security, point O1): the legacy listing applies the same
    // visibility rules as GET /social/rest/users/{username}/spaces
    startSessionAs("mary");
    ContainerResponse response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0&returnSize=true"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    assertEquals(1, collections.getSize());

    // A connection who is a member of the hidden space still receives it
    startSessionAs("demo");
    response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0&returnSize=true"), "", null, null);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
    assertEquals(2, collections.getSize());

    // The owner and the super user keep the whole listing
    startSessionAs("john");
    response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0"), "", null, null);
    assertEquals(200, response.getStatus());
    assertEquals(2, ((CollectionEntity) response.getEntity()).getEntities().size());

    startSessionAs("root");
    response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0"), "", null, null);
    assertEquals(200, response.getStatus());
    assertEquals(2, ((CollectionEntity) response.getEntity()).getEntities().size());
  }

  public void testGetSpacesOfUserNarrowsAnExternalConnectionToCommonSpaces() throws Exception {
    // john has a public space shared with demo and a public space of his own.
    // mary and demo are both confirmed connections of john; demo is external.
    Space commonSpace = getSpaceInstance(12, "john");
    commonSpace.setMembers(new String[] { "john", "demo" });
    spaceService.updateSpace(commonSpace);
    getSpaceInstance(13, "john");

    relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, maryIdentity);
    relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(johnIdentity, demoIdentity);
    markExternal(demoIdentity, true);
    try {
      // The viewer-axis matrix applies to this endpoint too (eXIP note 50524,
      // Security): an external connection receives the spaces it has in common
      // with the owner only, whatever the relationship grants an internal one
      startSessionAs("demo");
      ContainerResponse response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0&returnSize=true"), "", null, null);
      assertEquals(200, response.getStatus());
      CollectionEntity collections = (CollectionEntity) response.getEntity();
      assertEquals(1, collections.getEntities().size());
      assertEquals(1, collections.getSize());

      // The same relationship, held by an internal connection, gets the whole
      // visible listing — the external flag is the only discriminator here
      startSessionAs("mary");
      response = service("GET", getURLResource("users/john/spaces?limit=5&offset=0&returnSize=true"), "", null, null);
      assertEquals(200, response.getStatus());
      collections = (CollectionEntity) response.getEntity();
      assertEquals(2, collections.getEntities().size());
      assertEquals(2, collections.getSize());
    } finally {
      // The profile property outlives this test in the shared test database
      markExternal(demoIdentity, false);
    }
  }

  private void markExternal(Identity identity, boolean external) {
    Profile profile = identityManager.getOrCreateUserIdentity(identity.getRemoteId()).getProfile();
    profile.setProperty(Profile.EXTERNAL, String.valueOf(external));
    identityManager.updateProfile(profile);
  }

  public void testGetCommonSpaces() throws Exception {
    Space spaceTest = getSpaceInstance(0, "root");
    Space spaceTest1 = getSpaceInstance(1, "demo");

    startSessionAs("root");
    ContainerResponse response = service("GET", getURLResource("users/root/spaces/john?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(0, collections.getEntities().size());

    spaceTest.setMembers(new String[] { "john" });
    spaceService.updateSpace(spaceTest);
    response = service("GET", getURLResource("users/root/spaces/john?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    startSessionAs("john");
    response = service("GET", getURLResource("users/john/spaces/root?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    spaceTest1.setMembers(new String[] { "john", "root" });
    spaceService.updateSpace(spaceTest1);

    response = service("GET", getURLResource("users/john/spaces/root?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET", getURLResource("users/john/spaces/root?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(403, response.getStatus());
  }

  public void testUpdateProfileAttribute() throws Exception {
    startSessionAs("root");
    String propertyName = "firstName";
    String propertyValue = "Root";
    byte[] formData = ("name=" + propertyName + "&value=" + propertyValue).getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/root"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    Identity identity = identityManager.getOrCreateUserIdentity("root");
    assertNotNull(identity);
    assertEquals(propertyValue, identity.getProfile().getProperty(propertyName));
    response = service("PATCH", getURLResource("users/john"), "", headers, formData);
    assertNotNull(response);
    assertEquals("User root shouldn't be able to modify john attributes", 401, response.getStatus());
  }

  public void testUpdateEmailNoOTPCode() throws Exception {
    startSessionAs("root");
    String email = "root@test.com";
    byte[] formData = ("name=email&value=" + email).getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 400, response.getStatus());
  }

  public void testUpdateEmailWithWrongOTPCode() throws Exception {
    startSessionAs("root");
    doThrow(IllegalAccessException.class).when(otpService)
                                         .validateOtp(any(), any(), any());
    String email = "root@test.com";
    byte[] formData = ("name=email&value=" + email + "&otpMethod=test&otpCode=123").getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 400, response.getStatus());
  }

  public void testUpdateEmailWithCorrectOTPCode() throws Exception {
    startSessionAs("root");
    doNothing().when(otpService).validateOtp("root", "test", "1234");
    String email = "root@test.com";
    byte[] formData = ("name=email&value=" + email + "&otpMethod=test&otpCode=1234").getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
  }

  public void testUpdateProfileAvatar() throws Exception {
    startSessionAs("root");

    String uploadId = "testtest";
    byte[] formData = ("name=avatar&value=" + uploadId).getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 500, response.getStatus());

    URL resource = getClass().getClassLoader().getResource("blank.gif");
    uploadService.createUploadResource(uploadId, resource.getFile(), "avatar.png", "image/png");
    response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    assertNotNull(identity);
    assertNotNull(identity.getProfile().getAvatarLastUpdated());

    InputStream avatarInputStream = identityManager.getAvatarInputStream(identity);
    String storedContent = IOUtil.getStreamContentAsString(avatarInputStream);
    String content = IOUtil.getStreamContentAsString(getClass().getClassLoader().getResourceAsStream("blank.gif"));
    assertEquals(content, storedContent);
  }

  public void testUpdateProfileBanner() throws Exception {
    startSessionAs("root");

    String uploadId = "testtest";
    byte[] formData = ("name=banner&value=" + uploadId).getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 500, response.getStatus());

    URL resource = getClass().getClassLoader().getResource("blank.gif");
    uploadService.createUploadResource(uploadId, resource.getFile(), "banner.png", "image/png");
    response = service("PATCH", getURLResource("users/root/"), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    assertNotNull(identity);
    assertNotNull(identity.getProfile().getBannerLastUpdated());

    InputStream bannerInputStream = identityManager.getBannerInputStream(identity);
    String storedContent = IOUtil.getStreamContentAsString(bannerInputStream);
    String content = IOUtil.getStreamContentAsString(getClass().getClassLoader().getResourceAsStream("blank.gif"));
    assertEquals(content, storedContent);

    // Test remove banner
    byte[] formDataDefaultBanner = ("name=banner&value=DEFAULT_BANNER").getBytes();
    ContainerResponse responseDefaultBanner = service("PATCH", getURLResource("users/root/"), "", headers, formDataDefaultBanner);
    identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    // assertEquals("DEFAULT_BANNER", identity.getProfile().getBannerUrl());
    assertNull(identity.getProfile().getProperty("banner"));
    assertNotNull(responseDefaultBanner);
    assertEquals(String.valueOf(responseDefaultBanner.getEntity()), 204, responseDefaultBanner.getStatus());
  }

  public void testImportUsersWithCustomProperties() throws Exception {
    startSessionAs("root");

    //
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setPropertyName("custom-multi-property");
    profilePropertySetting.setMultiValued(true);
    profilePropertySetting = profilePropertyService.createPropertySetting(profilePropertySetting);
    tearDownProfilePropertyList.add(profilePropertySetting);

    ProfilePropertySetting profilePropertySetting1 = new ProfilePropertySetting();
    profilePropertySetting1.setPropertyName("first-property");
    profilePropertySetting1.setMultiValued(false);
    profilePropertySetting1.setEditable(true);
    profilePropertySetting1.setParentId(profilePropertySetting.getId());
    profilePropertySetting1 = profilePropertyService.createPropertySetting(profilePropertySetting1);
    tearDownProfilePropertyList.add(profilePropertySetting1);

    ProfilePropertySetting customSinglePropertySetting = new ProfilePropertySetting();
    customSinglePropertySetting.setPropertyName("custom-single-property");
    customSinglePropertySetting.setMultiValued(false);
    customSinglePropertySetting.setEditable(true);
    customSinglePropertySetting = profilePropertyService.createPropertySetting(customSinglePropertySetting);
    tearDownProfilePropertyList.add(customSinglePropertySetting);

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");

    String uploadId = "users-with-profile-settings.csv";
    URL resource = getClass().getClassLoader().getResource("users-with-profile-settings.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users-with-profile-settings.csv", "text/csv");
    ContainerResponse response = service("POST",
                                         getURLResource("users/csv"),
                                         "",
                                         headers,
                                         ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertNull(response.getEntity());
    assertEquals(204, response.getStatus());
    Identity importedUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jack.nipper");
    Profile importedUserProfile = importedUser.getProfile();

    List<Map<String, String>> urls = (List<Map<String, String>>) importedUserProfile.getProperty(Profile.CONTACT_URLS);
    assertEquals(1, urls.size());
    assertEquals(" https://www.exoplatform.com", urls.get(0).get("value"));

    assertEquals("test", importedUserProfile.getProperty("custom-single-property"));

    // should not import multivalued properties
    List<Map<String, String>> customMultiValuedProperty =
                                                        (ArrayList<Map<String, String>>) importedUserProfile.getProperty("custom-multi-property");
    assertNull(customMultiValuedProperty);

  }

  public void testAddUserStampsApiCreationSource() throws Exception {
    startSessionAs("root");

    byte[] jsonData = ("{\"username\":\"api.created\",\"password\":\"Passw0rd!\",\"email\":\"api.created@exoplatform.com\","
        + "\"firstname\":\"Api\",\"lastname\":\"Created\"}").getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + jsonData.length);

    ContainerResponse response = service("POST", getURLResource("users"), "", headers, jsonData);

    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
    try {
      User createdUser = organizationService.getUserHandler().findUserByName("api.created");
      assertNotNull(createdUser);
      // accounts provisioned through the API are excluded from the self
      // service account deactivation, which keys on this source
      assertEquals(UserRest.CREATION_SOURCE_API, createdUser.getCreationSource());
    } finally {
      organizationService.getUserHandler().removeUser("api.created", false);
    }
  }

  public void testImportUsers() throws Exception {
    startSessionAs("root");

    String uploadId = "users-empty.csv";
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("POST",
                                         getURLResource("users/csv"),
                                         "",
                                         headers,
                                         ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);

    assertEquals(400, response.getStatus());

    URL resource = getClass().getClassLoader().getResource("users-empty.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users-empty.csv", "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertEquals(400, response.getStatus());

    uploadId = "users.csv";
    resource = getClass().getClassLoader().getResource("users.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users.csv", "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertNull(response.getEntity());
    assertEquals(204, response.getStatus());

    uploadId = "users-update.csv";
    resource = getClass().getClassLoader().getResource("users-update.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users-update.csv", "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertNull(response.getEntity());
    assertEquals(204, response.getStatus());

    uploadId = "users-enabled.csv";
    resource = getClass().getClassLoader().getResource(uploadId);
    uploadService.createUploadResource(uploadId, resource.getFile(), uploadId, "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertNull(response.getEntity());
    assertEquals(204, response.getStatus());

    uploadId = "users.csv";
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&progress=true").getBytes());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertEquals(200, response.getStatus());
    assertEquals(response.getEntity().getClass(), UserImportResult.class);
    UserImportResult importResultEntity = (UserImportResult) response.getEntity();
    assertEquals(4, importResultEntity.getCount());
    assertEquals(importResultEntity.getCount(), importResultEntity.getProcessedCount());
    UploadResource uploadResource = uploadService.getUploadResource(uploadId);

    BufferedReader reader = new BufferedReader(new FileReader(uploadResource.getStoreLocation()));
    reader.readLine();
    String User = reader.readLine();
    List<String> userNames = new ArrayList<>();
    List<String> passWords = new ArrayList<>();

    while (User != null) {
      List<String> userProperties = Arrays.asList(User.split(","));
      userNames.add(userProperties.get(0));
      passWords.add(userProperties.get(3));
      User = reader.readLine();
    }

    assertNull(importResultEntity.getErrorMessages());
    assertNull(importResultEntity.getWarnMessages());

    uploadId = "users-update.csv";
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&progress=true").getBytes());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertEquals(200, response.getStatus());
    assertEquals(response.getEntity().getClass(), UserImportResult.class);
    importResultEntity = (UserImportResult) response.getEntity();
    assertEquals(4, importResultEntity.getCount());
    assertEquals(importResultEntity.getCount(), importResultEntity.getProcessedCount());

    assertNull(importResultEntity.getErrorMessages());
    assertNull(importResultEntity.getWarnMessages());

    for (int i = 0; i < (userNames.size()); i++) {
      String userEnabled = organizationService.getUserHandler().findUserByName(userNames.get(i), UserStatus.ANY).getUserName();
      boolean result = organizationService.getUserHandler().authenticate(userEnabled, passWords.get(i));
      assertTrue(result);
    }

    // Test import enabled or disabled users
    uploadId = "users-enabled.csv";
    resource = getClass().getClassLoader().getResource(uploadId);
    uploadService.createUploadResource(uploadId, resource.getFile(), uploadId, "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&progress=true").getBytes());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertEquals(200, response.getStatus());
    assertEquals(response.getEntity().getClass(), UserImportResult.class);
    importResultEntity = (UserImportResult) response.getEntity();
    assertEquals(2, importResultEntity.getCount());
    assertEquals(importResultEntity.getCount(), importResultEntity.getProcessedCount());

    assertNull(importResultEntity.getErrorMessages());
    assertNull(importResultEntity.getWarnMessages());
    String userEnabled = organizationService.getUserHandler().findUserByName("usera", UserStatus.ANY).getUserName();
    boolean result = organizationService.getUserHandler().authenticate(userEnabled, "successuser11");
    assertTrue(result);
    String userDisabled = organizationService.getUserHandler().findUserByName("userb", UserStatus.DISABLED).getUserName();
    try {
      organizationService.getUserHandler().authenticate(userDisabled, "successuser22");
      fail("disabled user");
    } catch (Exception e1) {
      // Expected
    }

    uploadId = "users-update-status.csv";
    resource = getClass().getClassLoader().getResource("users-update-status.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users-update-status.csv", "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertNull(response.getEntity());
    assertEquals(204, response.getStatus());

    // Test update status
    uploadId = "users-update-status.csv";
    resource = getClass().getClassLoader().getResource("users-update-status.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users-update-status.csv", "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&progress=true").getBytes());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertEquals(200, response.getStatus());
    assertEquals(response.getEntity().getClass(), UserImportResult.class);
    importResultEntity = (UserImportResult) response.getEntity();
    assertEquals(2, importResultEntity.getCount());
    assertEquals(importResultEntity.getCount(), importResultEntity.getProcessedCount());

    assertNull(importResultEntity.getErrorMessages());
    assertNull(importResultEntity.getWarnMessages());
    // Status after update
    boolean userStatusUpdated1 = organizationService.getUserHandler().findUserByName("usera", UserStatus.DISABLED).isEnabled();
    boolean userStatusUpdated2 = organizationService.getUserHandler().findUserByName("userb", UserStatus.ENABLED).isEnabled();
    assertFalse(userStatusUpdated1);
    assertTrue(userStatusUpdated2);
    organizationService.getUserHandler().setEnabled("usera", true, true);

    uploadId = "users.csv";
    uploadResource = uploadService.getUploadResource(uploadId);
    assertNotNull(uploadResource);
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&clean=true").getBytes());
    assertNotNull(response);
    assertEquals(204, response.getStatus());
    uploadResource = uploadService.getUploadResource(uploadId);
    assertNull(uploadResource);

    uploadId = "users-errors.csv";
    resource = getClass().getClassLoader().getResource("users-errors.csv");
    uploadService.createUploadResource(uploadId, resource.getFile(), "users-errors.csv", "text/csv");
    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&sync=true").getBytes());
    assertNotNull(response);
    assertNull(response.getEntity());
    assertEquals(204, response.getStatus());

    response = service("POST", getURLResource("users/csv"), "", headers, ("uploadId=" + uploadId + "&progress=true").getBytes());
    assertNotNull(response);
    assertNotNull(response.getEntity());
    assertEquals(200, response.getStatus());
    assertEquals(response.getEntity().getClass(), UserImportResult.class);
    importResultEntity = (UserImportResult) response.getEntity();
    assertEquals(6, importResultEntity.getCount());
    assertEquals(importResultEntity.getCount(), importResultEntity.getProcessedCount());
    assertNotNull(importResultEntity.getErrorMessages());
    assertEquals(2, importResultEntity.getErrorMessages().size());
    assertNotNull(importResultEntity.getWarnMessages());
    assertEquals(1, importResultEntity.getWarnMessages().size());
  }

  public void testUpdateProfileAttributes() throws Exception {
    String firstName = "Johnny";
    String lastName = "Bravo";
    String fullName = "Johnny Bravo";
    String aboutMe = "AboutMe";
    String imType = "Skype";
    String imId = "johnnyB";
    String phoneType = "work";
    String phoneNumber = "123456";
    String url = "fakeURL";

    StringBuilder input = new StringBuilder("{");
    input.append("\"");
    input.append(ProfileEntity.FIRSTNAME);
    input.append("\":\"");
    input.append(firstName);
    input.append("\",");

    input.append("\"");
    input.append(ProfileEntity.LASTNAME);
    input.append("\":\"");
    input.append(lastName);
    input.append("\",");

    input.append("\"");
    input.append(ProfileEntity.EMAIL);
    input.append("\":\"");
    input.append(johnIdentity.getProfile().getEmail());
    input.append("\",");

    input.append("\"");
    input.append(ProfileEntity.ABOUT_ME);
    input.append("\":\"");
    input.append(aboutMe);
    input.append("\",");

    input.append("\"");
    input.append(ProfileEntity.URLS);
    input.append("\":[{\"url\":\"");
    input.append(url);
    input.append("\"}]");
    input.append("}");

    startSessionAs("john");
    ContainerResponse response = getResponse("PATCH", getURLResource("users/john/profile"), input.toString());
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    assertNotNull(identity);
    assertEquals(firstName, identity.getProfile().getProperty(Profile.FIRST_NAME));
    assertEquals(lastName, identity.getProfile().getProperty(Profile.LAST_NAME));
    assertEquals(fullName, identity.getProfile().getProperty(Profile.FULL_NAME));

    List<Map<String, String>> urls = (List<Map<String, String>>) identity.getProfile().getProperty(Profile.CONTACT_URLS);
    assertNotNull(urls);
    assertEquals(1, urls.size());
    assertEquals(url, urls.get(0).get(url));
  }

  public void testUpdateUserFieldsWithValidators() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");

    String user = "demo";
    startSessionAs(user);
    ContainerResponse response = service("PATCH",
                                         getURLResource("users/john"),
                                         "",
                                         headers,
                                         ("name=" + ProfileEntity.FIRSTNAME + "&value=t").getBytes());
    assertNotNull(response);
    assertEquals("demo shouldn't be allowed to update john fields. Response content: " + response.getEntity(),
                 401,
                 response.getStatus());

    response = service("PATCH",
                       getURLResource("users/" + user),
                       "",
                       headers,
                       ("name=" + ProfileEntity.FIRSTNAME + "&value=D").getBytes());
    assertNotNull(response);
    assertEquals("FIRST name format validation should return HTTP 400 code. Response content: " + response.getEntity(),
                 400,
                 response.getStatus());

    response = service("PATCH",
                       getURLResource("users/" + user),
                       "",
                       headers,
                       ("name=" + ProfileEntity.LASTNAME + "&value=T").getBytes());
    assertNotNull(response);
    assertEquals("LAST name format validation should return HTTP 400 code. Response content: " + response.getEntity(),
                 400,
                 response.getStatus());
  }

  public void testUpdateUserFieldWithValidators() throws Exception {
    String johnEmail = "john@platform.com";
    startSessionAs("john");
    JSONObject johnData = new JSONObject();
    johnData.put(ProfileEntity.EMAIL, johnEmail);
    ContainerResponse response = getResponse("PATCH",
                                             "/v1/social/users/john/profile?otpMethod=test&otpCode=123",
                                             johnData.toString());
    assertNotNull(response);
    assertEquals(204, response.getStatus());

    JSONObject data = new JSONObject();
    data.put(ProfileEntity.USERNAME, "demo");
    data.put(ProfileEntity.FIRSTNAME, "Demo");
    data.put(ProfileEntity.LASTNAME, "Test");
    data.put(ProfileEntity.EMAIL, "demo@test.com");

    String user = "demo";
    startSessionAs(user);
    response = getResponse("PATCH", "/v1/social/users/john/profile", data.toString());
    assertNotNull(response);
    assertEquals("demo shouldn't be allowed to update john fields. Response content: " + response.getEntity(),
                 401,
                 response.getStatus());
    data.put(ProfileEntity.USERNAME, user);

    data.put(ProfileEntity.EMAIL, "WRONG_FORMAT");
    response = getResponse("PATCH", "/v1/social/users/" + user + "/profile?otpMethod=test&otpCode=123", data.toString());
    assertNotNull(response);
    assertEquals("Email format validation should return HTTP 400 code. Response content: " + response.getEntity(),
                 400,
                 response.getStatus());

    startSessionAs(user);
    data.put(ProfileEntity.EMAIL, johnEmail);
    response = getResponse("PATCH", "/v1/social/users/" + user + "/profile?otpMethod=test&otpCode=123", data.toString());
    assertNotNull(response);
    assertEquals("Email already exists return HTTP 401 code. Response content: " + response.getEntity(),
                 401,
                 response.getStatus());

    data.remove(ProfileEntity.EMAIL);
    data.put(ProfileEntity.FIRSTNAME, "d");
    response = getResponse("PATCH", "/v1/social/users/" + user + "/profile", data.toString());
    assertNotNull(response);
    assertEquals("FIRST name format validation should return HTTP 400 code. Response content: " + response.getEntity(),
                 400,
                 response.getStatus());
    data.put(ProfileEntity.FIRSTNAME, "Demo");

    data.put(ProfileEntity.LASTNAME, "t");
    response = getResponse("PATCH", "/v1/social/users/" + user + "/profile", data.toString());
    assertNotNull(response);
    assertEquals("LAST name format validation should return HTTP 400 code. Response content: " + response.getEntity(),
                 400,
                 response.getStatus());
  }

  public void testUpdateUserNewMultiProperties() throws Exception {
    startSessionAs("root");
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setPropertyName("new ArrayField");
    profilePropertySettingEntity.setMultiValued(false);
    ProfilePropertySettingEntity profilePropertySettingEntityChild = new ProfilePropertySettingEntity();
    profilePropertySettingEntityChild.setPropertyName("subField");
    profilePropertySettingEntityChild.setValue("subfield value");
    profilePropertySettingEntity.setChildren(Collections.singletonList(profilePropertySettingEntityChild));
    ArrayList<ProfilePropertySettingEntity> profilePropertySettingEntityList = new ArrayList<>();
    profilePropertySettingEntityList.add(profilePropertySettingEntity);
    ContainerResponse response = getResponse("PATCH",
                                             "/v1/social/users/root/profile/properties",
                                             new JSONArray(profilePropertySettingEntityList).toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }

  public void testUpdateUserPropertiesWithEmailAsAdmin() throws Exception {
    startSessionAs("root", true);
    ProfilePropertySetting emailSetting = profilePropertyService.getProfileSettingByName("email");
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setPropertyName("email");
    profilePropertySettingEntity.setValue("johnsmith@acme.com");
    profilePropertySettingEntity.setId(emailSetting.getId());
    ArrayList<ProfilePropertySettingEntity> profilePropertySettingEntityList = new ArrayList<>();
    profilePropertySettingEntityList.add(profilePropertySettingEntity);
    ContainerResponse response = getResponse("PATCH",
                                             "/v1/social/users/john/profile/properties",
                                             new JSONArray(profilePropertySettingEntityList).toString());
    assertNotNull(response);
    assertEquals(400, response.getStatus());
  }

  public void testUpdateUserPropertiesAsAdmin() throws Exception {
    startSessionAs("root", true);
    ArrayList<ProfilePropertySettingEntity> profilePropertySettingEntityList = new ArrayList<>();

    ProfilePropertySetting firstNameSetting = profilePropertyService.getProfileSettingByName("firstName");
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setPropertyName("firstName");
    profilePropertySettingEntity.setValue("JOHN");
    profilePropertySettingEntity.setId(firstNameSetting.getId());
    profilePropertySettingEntityList.add(profilePropertySettingEntity);

    ProfilePropertySetting emailSetting = profilePropertyService.getProfileSettingByName("email");
    ProfilePropertySettingEntity emailPropertySettingEntity = new ProfilePropertySettingEntity();
    emailPropertySettingEntity.setPropertyName("email");
    emailPropertySettingEntity.setValue(johnIdentity.getProfile().getEmail());
    emailPropertySettingEntity.setToHide(true);
    emailPropertySettingEntity.setId(emailSetting.getId());
    profilePropertySettingEntityList.add(emailPropertySettingEntity);

    ContainerResponse response = getResponse("PATCH",
                                             "/v1/social/users/john/profile/properties",
                                             new JSONArray(profilePropertySettingEntityList).toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    Identity identity = identityManager.getOrCreateUserIdentity("john");
    assertEquals(identity.getProfile().getProperty("firstName"), "JOHN");
    List<Long> hiddenProfilePropertyIds =
                                        profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(johnIdentity.getId()));
    assertNotNull(hiddenProfilePropertyIds);
    assertTrue(hiddenProfilePropertyIds.contains(emailSetting.getId()));
  }

  public void testHideUserPropertiesAsAdmin() throws Exception {
    startSessionAs("root", true);
    String propertyName = "firstName";
    String propertyValue = "John";
    ProfilePropertySetting setting = profilePropertyService.getProfileSettingByName(propertyName);
    ProfilePropertySettingEntity profilePropertySettingEntity = new ProfilePropertySettingEntity();
    profilePropertySettingEntity.setPropertyName(propertyName);
    profilePropertySettingEntity.setValue(propertyValue);
    profilePropertySettingEntity.setId(setting.getId());
    profilePropertySettingEntity.setToHide(true);
    ArrayList<ProfilePropertySettingEntity> profilePropertySettingEntityList = new ArrayList<>();
    profilePropertySettingEntityList.add(profilePropertySettingEntity);
    ContainerResponse response = getResponse("PATCH",
                                             "/v1/social/users/john/profile/properties",
                                             new JSONArray(profilePropertySettingEntityList).toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    List<Long> hiddenPropertiesId = profilePropertyService.getHiddenProfilePropertyIds(Long.parseLong(identity.getId()));
    assertTrue(hiddenPropertiesId.contains(setting.getId()));
  }

  private Space getSpaceInstance(int number, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setGroupId("/spaces/space" + number);
    String[] managers = new String[] { creator };
    String[] members = new String[] { creator };
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());
    this.spaceService.createSpace(space, creator);
    return space;
  }

  private void uploadUserBanner(String user) throws Exception {
    startSessionAs(user);
    String uploadId = "testtest";
    byte[] formData = ("name=banner&value=" + uploadId).getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    URL resource = getClass().getClassLoader().getResource("blank.gif");
    uploadService.createUploadResource(uploadId, resource.getFile(), "banner.png", "image/png");
    ContainerResponse response = service("PATCH", getURLResource("users/" + user), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    endSession();
  }

  private void uploadUserAvatar(String user) throws Exception {
    uploadUserAvatar(user, "blank.gif");
  }

  private void uploadUserAvatar(String user, String resourceName) throws Exception {
    startSessionAs(user);
    String uploadId = resourceName;
    byte[] formData = ("name=avatar&value=" + uploadId).getBytes();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");
    URL resource = getClass().getClassLoader().getResource(resourceName);
    uploadService.createUploadResource(uploadId, resource.getFile(), resourceName, "image/png");
    ContainerResponse response = service("PATCH", getURLResource("users/" + user), "", headers, formData);
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    endSession();
  }

  public void testAccountWritesRequireConfirmedSession() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("root", true);

    byte[] userData = ("{\"username\":\"guarded.user\",\"password\":\"Passw0rd!\",\"email\":\"guarded.user@exoplatform.com\","
        + "\"firstname\":\"Guarded\",\"lastname\":\"User\"}").getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> jsonHeaders = new MultivaluedMapImpl();
    jsonHeaders.putSingle("content-type", "application/json");
    jsonHeaders.putSingle("content-length", "" + userData.length);
    ContainerResponse response = service("POST", getURLResource("users"), "", jsonHeaders, userData, "root");
    assertOtpRequired(response);
    assertNull(organizationService.getUserHandler().findUserByName("guarded.user", UserStatus.ANY));

    response = service("DELETE", getURLResource("users/john"), "", null, null, "root");
    assertOtpRequired(response);
    assertNotNull(organizationService.getUserHandler().findUserByName("john", UserStatus.ANY));

    byte[] users = "[\"john\"]".getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> listHeaders = new MultivaluedMapImpl();
    listHeaders.putSingle("content-type", "application/json");
    response = service("PATCH", getURLResource("users/bulk/disable"), "", listHeaders, users, "root");
    assertOtpRequired(response);
    assertTrue(organizationService.getUserHandler().findUserByName("john", UserStatus.ANY).isEnabled());
    response = service("PATCH", getURLResource("users/bulk/enable"), "", listHeaders, users, "root");
    assertOtpRequired(response);
    // Sending the onboarding email alters no account
    response = service("PATCH", getURLResource("users/bulk/onboard"), "", listHeaders, users, "root");
    assertNotOtpRequired(response);

    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    Object firstName = identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.FIRST_NAME);
    response = service("PATCH", getURLResource("users/john"), "", formHeaders, "name=firstName&value=Johnny".getBytes(), "root");
    assertOtpRequired(response);
    assertEquals(firstName, identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.FIRST_NAME));

    response = getResponseAs("PATCH", "/v1/social/users/john/profile", "{\"firstname\":\"Johnny\"}", "root");
    assertOtpRequired(response);
    response = getResponseAs("PATCH",
                             "/v1/social/users/john/profile/properties",
                             "[{\"propertyName\":\"firstName\",\"value\":\"Johnny\",\"children\":[]}]",
                             "root");
    assertOtpRequired(response);
  }

  public void testAccountWritesPassFromConfirmedSession() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("root", true);
    confirmedSessionUser = "root";

    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH",
                                         getURLResource("users/john"),
                                         "",
                                         formHeaders,
                                         "name=firstName&value=Johnny".getBytes(),
                                         "root");
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());

    byte[] users = "[\"john\"]".getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> listHeaders = new MultivaluedMapImpl();
    listHeaders.putSingle("content-type", "application/json");
    try {
      response = service("PATCH", getURLResource("users/bulk/disable"), "", listHeaders, users, "root");
      assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
      assertEquals(List.of("john"), response.getEntity());
      assertFalse(organizationService.getUserHandler().findUserByName("john", UserStatus.ANY).isEnabled());
      response = service("PATCH", getURLResource("users/bulk/enable"), "", listHeaders, users, "root");
      assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
      assertEquals(List.of("john"), response.getEntity());
      assertTrue(organizationService.getUserHandler().findUserByName("john", UserStatus.ANY).isEnabled());
    } finally {
      organizationService.getUserHandler().setEnabled("john", true, true);
    }

    // A confirmation of another user does not count
    confirmedSessionUser = "mary";
    response = service("PATCH", getURLResource("users/john"), "", formHeaders, "name=firstName&value=John".getBytes(), "root");
    assertOtpRequired(response);
  }

  public void testOwnProfileIsNeverGuarded() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("john");
    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH",
                                         getURLResource("users/john"),
                                         "",
                                         formHeaders,
                                         "name=firstName&value=Johnny".getBytes(),
                                         "john");
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    response = getResponseAs("PATCH", "/v1/social/users/john/profile", "{\"firstname\":\"John\"}", "john");
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
  }

  public void testProfileGuardRunsAfterCallerCheck() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("mary");
    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH",
                                         getURLResource("users/john"),
                                         "",
                                         formHeaders,
                                         "name=firstName&value=Johnny".getBytes(),
                                         "mary");
    assertEquals(401, response.getStatus());
  }

  public void testImportStartRequiresConfirmedSessionButNotProgressNorClean() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("root", true);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("Content-Type", "application/x-www-form-urlencoded");

    ContainerResponse response = service("POST", getURLResource("users/csv"), "", headers, "sync=true".getBytes(), "root");
    assertEquals(400, response.getStatus());
    assertEquals("UPLOAD_ID:MANDATORY", response.getEntity());

    response = service("POST", getURLResource("users/csv"), "", headers, "uploadId=guarded&sync=true".getBytes(), "root");
    assertOtpRequired(response);

    response = service("POST", getURLResource("users/csv"), "", headers, "uploadId=guarded&progress=true".getBytes(), "root");
    assertEquals(404, response.getStatus());

    response = service("POST", getURLResource("users/csv"), "", headers, "uploadId=guarded&clean=true".getBytes(), "root");
    assertEquals(204, response.getStatus());
  }

  public void testSwitchedOffConfirmationLetsAccountWritesThrough() throws Exception {
    startSessionAs("root", true);
    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH",
                                         getURLResource("users/john"),
                                         "",
                                         formHeaders,
                                         "name=firstName&value=John".getBytes(),
                                         "root");
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
  }

  /**
   * The email change validates the code of the email method whatever the
   * client names, and consumes it once the change is written: a code that
   * confirmed one change cannot confirm another, and a refused request burns
   * nothing
   */
  public void testEmailChangeIsBoundToEmailMethodAndConsumedAfterTheWrite() throws Exception {
    startSessionAs("john");
    JSONObject data = new JSONObject();
    data.put(ProfileEntity.EMAIL, "WRONG_FORMAT");
    ContainerResponse response = getResponse("PATCH", "/v1/social/users/john/profile?otpMethod=sms&otpCode=123", data.toString());
    assertEquals(400, response.getStatus());
    verify(otpService, never()).consumeOtp(anyString(), anyString(), anyString());

    data.put(ProfileEntity.EMAIL, "john.new@platform.com");
    response = getResponse("PATCH", "/v1/social/users/john/profile?otpMethod=sms&otpCode=123", data.toString());
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    verify(otpService, never()).validateOtp(eq("john"), eq("sms"), anyString());
    verify(otpService, times(2)).validateOtp("john", "email", "123");
    verify(otpService).consumeOtp("john", "email", "123");

    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    response = service("PATCH",
                       getURLResource("users/john"),
                       "",
                       formHeaders,
                       "name=email&value=john.other@platform.com&otpMethod=sms&otpCode=456".getBytes());
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    verify(otpService).validateOtp("john", "email", "456");
    verify(otpService).consumeOtp("john", "email", "456");
  }

  public void testEmailChangeRefusedByTheCodeConsumesNothing() throws Exception {
    startSessionAs("john");
    doThrow(new IllegalAccessException()).when(otpService).validateOtp("john", "email", "000");
    JSONObject data = new JSONObject();
    data.put(ProfileEntity.EMAIL, "john.new@platform.com");
    ContainerResponse response = getResponse("PATCH", "/v1/social/users/john/profile?otpCode=000", data.toString());
    assertEquals(400, response.getStatus());
    assertEquals("EMAIL:OTP_CODE_WRONG", response.getEntity());
    verify(otpService, never()).consumeOtp(anyString(), anyString(), anyString());
  }

  /**
   * A request-supplied property name is looked up among the stored settings,
   * never through the by-name lookup that caches every miss; names that are
   * no setting keep being stored
   */
  public void testUpdateProfileFieldNeverLooksSettingsUpByName() throws Exception {
    startSessionAs("john");
    MultivaluedMap<String, String> formHeaders = new MultivaluedMapImpl();
    formHeaders.putSingle("Content-Type", "application/x-www-form-urlencoded");
    ContainerResponse response = service("PATCH", getURLResource("users/john"), "", formHeaders, "name=aboutMe&value=Hello".getBytes());
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());
    response = service("PATCH", getURLResource("users/john"), "", formHeaders, "name=enrollmentDate&value=123".getBytes());
    assertEquals(String.valueOf(response.getEntity()), 204, response.getStatus());

    verify(profilePropertyServiceSpy, never()).getProfileSettingByName(anyString());
    Profile profile = identityManager.getOrCreateUserIdentity("john").getProfile();
    assertEquals("Hello", profile.getProperty(Profile.ABOUT_ME));
    assertEquals("123", profile.getProperty(Profile.ENROLLMENT_DATE));
  }

  /**
   * The onboarding email carries the administrator's message and is written
   * in the user's preferred language; the enrollment date is stamped once
   * it was sent
   */
  public void testSendOnboardingEmailWithMessageInTheUserLanguage() throws Exception {
    PasswordRecoveryService recoveryService = mock(PasswordRecoveryService.class);
    when(recoveryService.sendOnboardingEmail(any(), any(), any(), any())).thenReturn(true);
    registerUserRestWith(recoveryService);
    UserProfileHandler profileHandler = organizationService.getUserProfileHandler();
    UserProfile profile = profileHandler.findUserProfileByName("john");
    if (profile == null) {
      profile = profileHandler.createUserProfileInstance("john");
    }
    profile.setAttribute(Constants.USER_LANGUAGE, "fr");
    profileHandler.saveUserProfile(profile, true);
    startSessionAs("root", true);

    ContainerResponse response = getResponseAs("PATCH", "/v1/social/users/onboard/john", "{\"message\":\"Welcome aboard\"}", "root");
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());

    ArgumentCaptor<Locale> locale = ArgumentCaptor.forClass(Locale.class);
    verify(recoveryService).sendOnboardingEmail(any(), locale.capture(), any(), eq("Welcome aboard"));
    assertEquals(Locale.FRENCH, locale.getValue());
    assertNotNull(identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.ENROLLMENT_DATE));

    // No body, as the row action and the bulk action send: no message, and
    // the platform's default language for an account with none stored
    profile.setAttribute(Constants.USER_LANGUAGE, null);
    profileHandler.saveUserProfile(profile, true);
    response = getResponseAs("PATCH", "/v1/social/users/onboard/mary", "", "root");
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
    verify(recoveryService).sendOnboardingEmail(any(),
                                                eq(localeConfigService.getDefaultLocaleConfig().getLocale()),
                                                any(),
                                                isNull());
  }

  public void testSendOnboardingEmailRefusesATooLongMessage() throws Exception {
    PasswordRecoveryService recoveryService = mock(PasswordRecoveryService.class);
    registerUserRestWith(recoveryService);
    startSessionAs("root", true);
    // The profile outlives the tests: the stamp is compared with its value
    // before the call, not with null
    Object enrollmentBefore = identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.ENROLLMENT_DATE);

    String message = "x".repeat(OnboardingEmailEntity.MESSAGE_MAX_LENGTH + 1);
    ContainerResponse response = getResponseAs("PATCH", "/v1/social/users/onboard/john", "{\"message\":\"" + message + "\"}", "root");
    assertEquals(400, response.getStatus());
    assertEquals(UserRest.MESSAGE_TOO_LONG, response.getEntity());
    verify(recoveryService, never()).sendOnboardingEmail(any(), any(), any(), any());
    assertEquals(enrollmentBefore, identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.ENROLLMENT_DATE));

    // The text is counted, not the markup
    message = "<p>" + "x".repeat(OnboardingEmailEntity.MESSAGE_MAX_LENGTH) + "</p>";
    when(recoveryService.sendOnboardingEmail(any(), any(), any(), any())).thenReturn(true);
    response = getResponseAs("PATCH", "/v1/social/users/onboard/john", "{\"message\":\"" + message + "\"}", "root");
    assertEquals(200, response.getStatus());
  }

  /**
   * The message is the editor's HTML: scripts and event attributes are
   * removed, the editor's formatting kept, and a message with no text is no
   * message
   */
  public void testSendOnboardingEmailSanitizesTheMessage() throws Exception {
    PasswordRecoveryService recoveryService = mock(PasswordRecoveryService.class);
    when(recoveryService.sendOnboardingEmail(any(), any(), any(), any())).thenReturn(true);
    registerUserRestWith(recoveryService);
    startSessionAs("root", true);

    String message = "<p onclick=\\\"x()\\\">Hi <b>Jane</b><script>alert(1)</script></p>";
    ContainerResponse response = getResponseAs("PATCH", "/v1/social/users/onboard/john", "{\"message\":\"" + message + "\"}", "root");
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
    ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
    verify(recoveryService).sendOnboardingEmail(any(), any(), any(), sent.capture());
    assertTrue(sent.getValue(), sent.getValue().contains("<b>Jane</b>"));
    assertFalse(sent.getValue(), sent.getValue().contains("script"));
    assertFalse(sent.getValue(), sent.getValue().contains("onclick"));

    response = getResponseAs("PATCH", "/v1/social/users/onboard/mary", "{\"message\":\"<p>&nbsp;</p>\"}", "root");
    assertEquals(200, response.getStatus());
    verify(recoveryService).sendOnboardingEmail(any(), any(), any(), isNull());
  }

  /**
   * A failed send is reported, and stamps nothing: the account stays listed
   * among the ones to invite
   */
  public void testSendOnboardingEmailFailureIsReported() throws Exception {
    PasswordRecoveryService recoveryService = mock(PasswordRecoveryService.class);
    when(recoveryService.sendOnboardingEmail(any(), any(), any(), any())).thenReturn(false);
    registerUserRestWith(recoveryService);
    startSessionAs("root", true);
    Object enrollmentBefore = identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.ENROLLMENT_DATE);

    ContainerResponse response = getResponseAs("PATCH", "/v1/social/users/onboard/john", "{\"message\":\"Hi\"}", "root");
    assertEquals(500, response.getStatus());
    assertEquals(UserRest.ONBOARDING_SEND_FAILED, response.getEntity());
    assertEquals(enrollmentBefore, identityManager.getOrCreateUserIdentity("john").getProfile().getProperty(Profile.ENROLLMENT_DATE));

    // The bulk action lists only the users whose email was sent
    byte[] users = "[\"john\",\"mary\"]".getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> listHeaders = new MultivaluedMapImpl();
    listHeaders.putSingle("content-type", "application/json");
    when(recoveryService.sendOnboardingEmail(any(), any(), any(), any())).thenAnswer(invocation -> "mary".equals(((User) invocation.getArgument(0)).getUserName()));
    response = service("PATCH", getURLResource("users/bulk/onboard"), "", listHeaders, users, "root");
    assertEquals(200, response.getStatus());
    assertEquals(List.of("mary"), response.getEntity());
  }

  /**
   * A row says whether its user never signed in by the social profile's last
   * login time, the source of the list's connection filter; the bulk onboard
   * sends to every such account, an external one included, and skips an
   * account that signed in once
   */
  public void testNeverConnectedAccountsAreFlaggedAndOnboardedWhateverTheirType() throws Exception {
    PasswordRecoveryService recoveryService = mock(PasswordRecoveryService.class);
    when(recoveryService.sendOnboardingEmail(any(), any(), any(), any())).thenReturn(true);
    registerUserRestWith(recoveryService);
    Profile johnProfile = identityManager.getOrCreateUserIdentity("john").getProfile();
    johnProfile.setProperty(Profile.LAST_LOGIN_TIME, System.currentTimeMillis());
    identityManager.updateProfile(johnProfile);
    Profile maryProfile = identityManager.getOrCreateUserIdentity("mary").getProfile();
    maryProfile.setProperty(Profile.EXTERNAL, "true");
    identityManager.updateProfile(maryProfile);
    try {
      assertNull(identityManager.getOrCreateUserIdentity("mary").getProfile().getProperty(Profile.LAST_LOGIN_TIME));
      startSessionAs("root", true);

      ContainerResponse response = service("GET", getURLResource("users?limit=10&offset=0&userType=ALL"), "", null, null);
      assertEquals(200, response.getStatus());
      Map<String, ProfileEntity> rows = new HashMap<>();
      for (Object entity : ((CollectionEntity) response.getEntity()).getEntities()) {
        ProfileEntity row = getBaseEntity(entity, ProfileEntity.class);
        rows.put(row.getUsername(), row);
      }
      assertEquals(Boolean.FALSE, rows.get("john").getNeverConnected());
      assertEquals(Boolean.TRUE, rows.get("mary").getNeverConnected());

      byte[] users = "[\"john\",\"mary\"]".getBytes(StandardCharsets.UTF_8);
      MultivaluedMap<String, String> listHeaders = new MultivaluedMapImpl();
      listHeaders.putSingle("content-type", "application/json");
      response = service("PATCH", getURLResource("users/bulk/onboard"), "", listHeaders, users, "root");
      assertEquals(200, response.getStatus());
      assertEquals(List.of("mary"), response.getEntity());
      ArgumentCaptor<User> sent = ArgumentCaptor.forClass(User.class);
      verify(recoveryService).sendOnboardingEmail(sent.capture(), any(), any(), any());
      assertEquals("mary", sent.getValue().getUserName());
    } finally {
      // The fixture's identities outlive the test: the other tests count
      // internal, never connected accounts
      maryProfile.removeProperty(Profile.EXTERNAL);
      identityManager.updateProfile(maryProfile);
      johnProfile.removeProperty(Profile.LAST_LOGIN_TIME);
      identityManager.updateProfile(johnProfile);
    }
  }

  /**
   * A bulk deactivation the store swallows, as with the deactivation option
   * of the identity store off, leaves the user out of the updated ones
   */
  public void testBulkStatusChangeListsOnlyTheUsersWhoseStatusHolds() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("root", true);
    confirmedSessionUser = "root";
    UserHandler realHandler = organizationService.getUserHandler();
    // The test container's handler is an in-memory one: no cache to evict
    UserHandler swallowingHandler = spy(realHandler);
    doReturn(null).when(swallowingHandler).setEnabled(anyString(), anyBoolean(), anyBoolean());
    OrganizationService swallowingService = mock(OrganizationService.class);
    when(swallowingService.getUserHandler()).thenReturn(swallowingHandler);
    registerUserRestWith(passwordRecoveryService, swallowingService);

    byte[] users = "[\"john\"]".getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> listHeaders = new MultivaluedMapImpl();
    listHeaders.putSingle("content-type", "application/json");
    ContainerResponse response = service("PATCH", getURLResource("users/bulk/disable"), "", listHeaders, users, "root");
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
    assertEquals(List.of(), response.getEntity());
    assertTrue(realHandler.findUserByName("john", UserStatus.ANY).isEnabled());
  }

  /**
   * The bulk read-back evicts the user's cache entry after the restart: a
   * cache still holding the attempted status would list a user the store
   * did not update
   */
  public void testBulkStatusReadBackEvictsTheCachedUser() throws Exception {
    confirmationSettings.setEnabled(true);
    startSessionAs("root", true);
    confirmedSessionUser = "root";
    User stored = organizationService.getUserHandler().findUserByName("john", UserStatus.ANY);
    UserImpl attempted = new UserImpl("john");
    attempted.setEnabled(false);
    AtomicBoolean evicted = new AtomicBoolean(false);
    CacheableUserHandlerImpl cachedHandler = mock(CacheableUserHandlerImpl.class);
    when(cachedHandler.findUserByName("john", UserStatus.ENABLED)).thenReturn(stored);
    when(cachedHandler.findUserByName("john", UserStatus.ANY)).thenAnswer(invocation -> evicted.get() ? stored : attempted);
    doAnswer(invocation -> {
      evicted.set(true);
      return null;
    }).when(cachedHandler).clearCache("john");
    OrganizationService cachedService = mock(OrganizationService.class);
    when(cachedService.getUserHandler()).thenReturn(cachedHandler);
    registerUserRestWith(passwordRecoveryService, cachedService);

    byte[] users = "[\"john\"]".getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> listHeaders = new MultivaluedMapImpl();
    listHeaders.putSingle("content-type", "application/json");
    ContainerResponse response = service("PATCH", getURLResource("users/bulk/disable"), "", listHeaders, users, "root");
    assertEquals(String.valueOf(response.getEntity()), 200, response.getStatus());
    assertEquals(List.of(), response.getEntity());
    assertTrue(evicted.get());
  }

  private void registerUserRestWith(PasswordRecoveryService recoveryService) throws Exception {
    registerUserRestWith(recoveryService, organizationService);
  }

  private void registerUserRestWith(PasswordRecoveryService recoveryService, OrganizationService orgService) throws Exception {
    removeResource(UserRest.class);
    registry(new UserRest(userACL,
                          orgService,
                          identityManager,
                          relationshipManager,
                          userStateService,
                          spaceService,
                          uploadService,
                          userExportService,
                          userImportService,
                          imageThumbnailService,
                          profilePropertyServiceSpy,
                          recoveryService,
                          localeConfigService,
                          settingService,
                          otpService));
  }

  private ContainerResponse getResponseAs(String method, String path, String input, String remoteUser) throws Exception {
    byte[] jsonData = input.getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + jsonData.length);
    return service(method, path, "", headers, jsonData, remoteUser);
  }

  private void assertOtpRequired(ContainerResponse response) {
    assertNotNull(response);
    assertEquals(String.valueOf(response.getEntity()), 403, response.getStatus());
    assertEquals(UserAccountConfirmation.OTP_REQUIRED, response.getEntity());
  }

  private void assertNotOtpRequired(ContainerResponse response) {
    assertNotNull(response);
    assertFalse(UserAccountConfirmation.OTP_REQUIRED.equals(response.getEntity()));
  }

  public void testIncludeCurrentUserInAdvancedSearchResult() throws Exception {
    startSessionAs("root");
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");

    // mock ProfileSearchConnector
    ProfileSearchConnector profileSearchConnector = mock(ProfileSearchConnector.class);
    RDBMSIdentityStorageImpl rdbmsIdentityStorage = getContainer().getComponentInstanceOfType(RDBMSIdentityStorageImpl.class);
    when(profileSearchConnector.search(any(),
                                       any(),
                                       any(),
                                       anyLong(),
                                       anyLong())).thenReturn(Arrays.asList(rootIdentity.getId(),
                                                                            johnIdentity.getId(),
                                                                            maryIdentity.getId()));
    rdbmsIdentityStorage.setProfileSearchConnector(profileSearchConnector);

    Profile profile = rootIdentity.getProfile();
    profile.setProperty("profession", "Developer");
    identityManager.updateProfile(profile, true);
    byte[] jsonData = "{\"profession\":\"Developer\"}".getBytes(StandardCharsets.UTF_8);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");
    headers.putSingle("content-length", "" + jsonData.length);
    ContainerResponse response = service("POST",
                                         getURLResource("users/advancedfilter?offset=0&limit=10&expand=all,spacesCount,relationshipStatus,connectionsCount,binding&filterType=all&returnSize=true"),
                                         "",
                                         headers,
                                         jsonData);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    Object collections = response.getEntity();
    List<? extends DataEntity> entities = ((CollectionEntity) collections).getEntities();
    assertEquals(3, entities.size());
    assertEquals("root", entities.get(0).get("username"));
    endSession();
  }

  public void testGetListManagersAndManagedUsersCount() throws Exception {
    startSessionAs("root");
    Identity rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");

    // mock ProfileSearchConnector
    ProfileSearchConnector profileSearchConnector = mock(ProfileSearchConnector.class);
    RDBMSIdentityStorageImpl rdbmsIdentityStorage = getContainer().getComponentInstanceOfType(RDBMSIdentityStorageImpl.class);
    when(profileSearchConnector.search(any(),
                                       any(),
                                       any(),
                                       anyLong(),
                                       anyLong())).thenReturn(List.of(maryIdentity.getId()));
    rdbmsIdentityStorage.setProfileSearchConnector(profileSearchConnector);

    Profile rootProfile = rootIdentity.getProfile();
    Map<String, String> values = new HashMap<>();
    List<Map<String, String>> manager = new ArrayList<>();
    values.put("value", "john");
    manager.add(values);
    rootProfile.setProperty("manager", manager);
    identityManager.updateProfile(rootProfile, true);
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.putSingle("content-type", "application/json");
    ContainerResponse response = service("GET",
                                         getURLResource("users/root?expand=settings,manager,managedUsersCount"),
                                         "",
                                         headers,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity dataEntity = (DataEntity) response.getEntity();
    assertEquals(1, ((List<DataEntity>) dataEntity.get("managers")).size());
    assertEquals(0, (int) dataEntity.get("managedUsersCount"));
    endSession();
  }

  public void testGetUsersByGroupId() throws Exception {
    AclPlugin previousAclPlugin = userACL.hasAclPlugin(GroupAclPlugin.OBJECT_TYPE) ?
                                                                                   userACL.getAclPlugin(GroupAclPlugin.OBJECT_TYPE) :
                                                                                   null;
    // john is manager of /platform/users only
    userACL.addAclPlugin(new AclPlugin() {
      @Override
      public String getObjectType() {
        return GroupAclPlugin.OBJECT_TYPE;
      }

      @Override
      public boolean hasPermission(String objectId,
                                   String permissionType,
                                   org.exoplatform.services.security.Identity identity) {
        return identity != null
               && StringUtils.equals("john", identity.getUserId())
               && StringUtils.equals("/platform/users", objectId);
      }
    });
    try {
      // A simple user can't list the members of a group he doesn't manage
      startSessionAs("demo");
      ContainerResponse response = service("GET",
                                           getURLResource("users?groupId=/platform/administrators&limit=5&offset=0"),
                                           "",
                                           null,
                                           null,
                                           "demo");
      assertNotNull(response);
      assertEquals(403, response.getStatus());

      // Nor export them
      response = service("GET",
                         getURLResource("users?groupId=/platform/administrators&export=true"),
                         "",
                         null,
                         null,
                         "demo");
      assertNotNull(response);
      assertEquals(403, response.getStatus());

      // An organizational unit manager can't list the members of another group
      startSessionAs("john");
      response = service("GET",
                         getURLResource("users?groupId=/platform/administrators&limit=5&offset=0"),
                         "",
                         null,
                         null,
                         "john");
      assertNotNull(response);
      assertEquals(403, response.getStatus());

      // ... but can list the members of a group he manages
      response = service("GET",
                         getURLResource("users?groupId=/platform/users&limit=5&offset=0"),
                         "",
                         null,
                         null,
                         "john");
      assertNotNull(response);
      assertEquals(200, response.getStatus());

      // An administrator can list the members of any group
      startSessionAs("root", true);
      response = service("GET",
                         getURLResource("users?groupId=/platform/administrators&limit=5&offset=0"),
                         "",
                         null,
                         null,
                         "root");
      assertNotNull(response);
      assertEquals(200, response.getStatus());
    } finally {
      if (previousAclPlugin != null) {
        userACL.addAclPlugin(previousAclPlugin);
      }
      endSession();
    }
  }
}
