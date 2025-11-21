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
package io.meeds.social.core.identity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.impl.LocaleConfigImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;

import io.meeds.social.core.identity.model.UserImportResult;

import lombok.SneakyThrows;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.Silent.class)
public class UserImportServiceTest {

  private static final String     UPLOAD_ID     = "id";

  private static final String     NOT_FOUND_ID  = "x";

  private static final String     COMPANY_VALUE = "ACME";

  private static final String     COMPANY_PROP  = "company";

  private static final String     TEST_USER_2   = "mod";

  private static final String     TEST_USER_1   = "ghost";

  private static final String     HEADER_LINE_1 = "userName,password,enabled,email,firstName,lastName";

  private static final String     HEADER_LINE_2 = "userName,password,enabled,email,firstName,lastName,onboardUser";

  private static final String     HEADER_LINE_3 = "userName,password,enabled,email,firstName,lastName,aboutMe";

  private static final String     HEADER_LINE_4 = "userName,password,email,firstName,lastName";

  private static final String     HEADER_LINE_5 = "userName,email,firstName,lastName";

  private static final String     URL           = "http://localhost";

  @Rule
  public final MockitoRule        rule          = MockitoJUnit.rule();

  @Mock
  private IdentityManager         identityManager;

  @Mock
  private OrganizationService     organizationService;

  @Mock
  private ProfilePropertyService  profilePropertyService;

  @Mock
  private LocaleConfigService     localeConfigService;

  @Mock
  private PasswordRecoveryService passwordRecoveryService;

  @Mock
  private UploadService           uploadService;

  @Mock
  private UserHandler             userHandler;

  @Mock
  private GroupHandler            groupHandler;

  @Mock
  private MembershipTypeHandler   membershipTypeHandler;

  @Mock
  private MembershipHandler       membershipHandler;

  @Mock
  private Identity                userIdentity;

  @Mock
  private Profile                 profile;

  @Mock
  private UploadResource          uploadResource;

  @Mock
  private ExecutorService         executorService;

  @Mock
  private UserACL                 userAcl;

  @InjectMocks
  private UserImportService       service;

  private File                    tempDir;

  @Before
  @SneakyThrows
  public void setUp() {
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    when(organizationService.getMembershipTypeHandler()).thenReturn(membershipTypeHandler);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);

    when(userIdentity.getProfile()).thenReturn(profile);
    LocaleConfigImpl localeConfig = new LocaleConfigImpl();
    localeConfig.setLocale(Locale.ENGLISH);
    when(localeConfigService.getDefaultLocaleConfig()).thenReturn(localeConfig);
    when(identityManager.getOrCreateUserIdentity(anyString())).thenReturn(userIdentity);

    tempDir = Files.createTempDirectory("uimport-test").toFile();
    service.init();
    service.importExecutorService = executorService;
    doAnswer(invocation -> {
      invocation.getArgument(0, Runnable.class).run();
      return null;
    }).when(executorService).execute(any());
  }

  @After
  public void tearDown() {
    service.stop();
    deleteRecursively(tempDir);
  }

  @Test
  public void testGetAndCleanUsersImportResult() {
    UserImportResult r = new UserImportResult();
    service.importUsersProcessing = new HashMap<>();
    assertNull(service.getUsersImportResult(NOT_FOUND_ID));

    service.importUsersProcessing.put(UPLOAD_ID, r);
    assertEquals(r, service.getUsersImportResult(UPLOAD_ID));

    service.cleanUsersImportResult(UPLOAD_ID);
    assertNull(service.getUsersImportResult(UPLOAD_ID));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testImportUsersUploadIdNotFound() {
    service.importUsers(NOT_FOUND_ID, TEST_USER_2, Locale.ENGLISH, URL, true);
  }

  @Test(expected = IllegalStateException.class)
  public void testImportUsersFileMissingOnDisk() {
    File nonExisting = new File(tempDir, "nope.csv");
    when(uploadResource.getStoreLocation()).thenReturn(nonExisting.getAbsolutePath());
    when(uploadService.getUploadResource("u1")).thenReturn(uploadResource);
    service.importUsers("u1", TEST_USER_2, Locale.ENGLISH, URL, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testImportUsersEmptyFile() {
    File f = csv("empty.csv", "userName,password,enabled");
    when(uploadResource.getStoreLocation()).thenReturn(f.getAbsolutePath());
    when(uploadService.getUploadResource("u2")).thenReturn(uploadResource);
    service.importUsers("u2", TEST_USER_2, Locale.ENGLISH, URL, true);
  }

  @Test
  public void testImportUsersSyncPathSuccess() throws Exception {
    File f = csv("one.csv",
                 HEADER_LINE_1,
                 "john,Secret123,true,john@ex.com,John,Doe");
    when(uploadResource.getStoreLocation()).thenReturn(f.getAbsolutePath());
    when(uploadService.getUploadResource("u3")).thenReturn(uploadResource);

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(null);
    doAnswer(i -> null).when(userHandler).createUser(any(User.class), eq(true));

    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);

    assertDoesNotThrow(() -> service.importUsers("u3", TEST_USER_2, Locale.ENGLISH, URL, true));
  }

  @Test
  public void testImportUsersHeaderBlankReturnsEarly() {
    File f = csv("blankHeader.csv", "");
    UserImportResult r = new UserImportResult();
    service.importUsers(f.getAbsolutePath(),
                        r,
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);
    assertEquals(0, r.getProcessedCount());
  }

  @Test
  public void testImportUsersBadLineMissingUserName() throws Exception {
    File f = csv("badLine.csv",
                 HEADER_LINE_1,
                 ",Secret123,true,jane@ex.com,Jane,Doe");
    UserImportResult r = new UserImportResult();
    when(userHandler.findUserByName(anyString(), any())).thenReturn(null);
    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), any())).thenReturn(none);

    service.importUsers(f.getAbsolutePath(),
                        r,
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);
    assertEquals(1, r.getProcessedCount());
  }

  @Test
  public void testImportUsersExistingUserUpdateAndOnboarding() throws Exception {
    File f = csv("exists.csv",
                 HEADER_LINE_2,
                 "john,Secret123,true,john@ex.com,John,Doe,true");

    User existing = mock(User.class);
    Date now = Calendar.getInstance().getTime();
    when(existing.isEnabled()).thenReturn(true);
    when(existing.getCreatedDate()).thenReturn(now);
    when(existing.getLastLoginTime()).thenReturn(now);

    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(existing);

    when(passwordRecoveryService.sendOnboardingEmail(any(UserImpl.class),
                                                     eq(Locale.ENGLISH),
                                                     any(StringBuilder.class))).thenReturn(true);

    when(profilePropertyService.getProfileSettingByName(anyString())).thenReturn(null);

    UserImportResult r = new UserImportResult();
    service.importUsers(f.getAbsolutePath(),
                        r,
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);

    verify(identityManager, atLeastOnce()).updateProfile(any(Profile.class), eq(TEST_USER_2), eq(true));
  }

  @Test
  public void testImportUsersEmailAlreadyExistsCreateRejected() throws Exception {
    File f = csv("dupemail.csv",
                 HEADER_LINE_1,
                 "alice,Secret123,true,dup@ex.com,Alice,Smith");

    when(userHandler.findUserByName("alice", UserStatus.ANY)).thenReturn(null);

    ListAccess<User> one = mock(ListAccess.class);
    when(one.getSize()).thenReturn(1);
    User other = mock(User.class);
    when(other.getUserName()).thenReturn("bob");
    when(one.load(0, 1)).thenReturn(new User[] { other });
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(one);

    UserImportResult r = new UserImportResult();
    service.importUsers(f.getAbsolutePath(),
                        r,
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);
    assertEquals(1, r.getProcessedCount());
  }

  @Test
  public void testImportUsersNewUserCreatedEnabledFlag() throws Exception {
    File f = csv("create.csv",
                 HEADER_LINE_1,
                 "neo,Secret123,false,neo@ex.com,Neo,Anderson");

    when(userHandler.findUserByName("neo", UserStatus.ANY)).thenReturn(null);
    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);

    UserImportResult r = new UserImportResult();
    service.importUsers(f.getAbsolutePath(),
                        r,
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);

    verify(userHandler).createUser(any(User.class), eq(true));
    verify(userHandler).setEnabled("neo", false, true);
  }

  @Test
  public void testUpdateUserProfilePropertiesObjectNotFoundWarn() throws Exception {
    when(identityManager.getOrCreateUserIdentity(TEST_USER_1)).thenReturn(null);

    File f = csv("profile_notfound.csv",
                 HEADER_LINE_3,
                 "ghost,Secret123,true,g@e.com,Gh,Ost,hello");

    when(userHandler.findUserByName(TEST_USER_1, UserStatus.ANY)).thenReturn(null);
    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);

    UserImportResult r = new UserImportResult();
    assertDoesNotThrow(() -> service.importUsers(f.getAbsolutePath(),
                                                 r,
                                                 TEST_USER_2,
                                                 Locale.ENGLISH,
                                                 URL));
  }

  @Test
  public void testComputeFieldsWarnings() throws Exception {
    ProfilePropertySetting parent = new ProfilePropertySetting();
    parent.setPropertyName("phones");
    parent.setMultiValued(true);
    parent.setHasChildProperties(true);

    when(profilePropertyService.getProfileSettingByName("unknown")).thenReturn(null);
    when(profilePropertyService.getProfileSettingByName("phones")).thenReturn(parent);
    when(profilePropertyService.getProfileSettingByName("child")).thenReturn(null);
    ProfilePropertySetting profilePropertySetting = new ProfilePropertySetting();
    profilePropertySetting.setPropertyName("customMulti");
    profilePropertySetting.setMultiValued(true);
    when(profilePropertyService.getProfileSettingByName("customMulti")).thenReturn(profilePropertySetting);

    File f = csv("headers.csv",
                 "userName,unknown,phones,phones.child,customMulti,emails.too.many.parents,email",
                 "john,,,,,,john@ex.com");

    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);
    when(userHandler.findUserByName("john", UserStatus.ANY)).thenReturn(null);

    UserImportResult r = new UserImportResult();
    service.importUsers(f.getAbsolutePath(),
                        r,
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);
    assertEquals(1, r.getProcessedCount());
  }

  @Test(expected = IllegalAccessException.class)
  public void testUpdateProfileFieldNotEditable() throws IllegalAccessException, IOException {
    ProfilePropertySetting s = new ProfilePropertySetting();
    s.setPropertyName(COMPANY_PROP);
    s.setEditable(false);
    when(profilePropertyService.getProfileSettingByName(COMPANY_PROP)).thenReturn(s);
    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(TEST_USER_2)).thenReturn(identity);
    when(identity.isMemberOf(userAcl.getAdminGroups())).thenReturn(false); 
    
    service.updateProfileField(profile,
                               COMPANY_PROP,
                               COMPANY_VALUE,
                               false,
                               TEST_USER_2);
  }

  @Test(expected = IllegalAccessException.class)
  public void testUpdateProfileFieldReservedExternal() throws IllegalAccessException, IOException {
    when(profilePropertyService.getProfileSettingByName(Profile.EXTERNAL)).thenReturn(null);
    service.updateProfileField(profile,
                               Profile.EXTERNAL,
                               "true",
                               false,
                               TEST_USER_2);
  }

  @Test(expected = IllegalAccessException.class)
  public void testUpdateProfileFieldReservedUsername() throws IllegalAccessException, IOException {
    service.updateProfileField(profile,
                               Profile.USERNAME,
                               "john",
                               false,
                               TEST_USER_2);
  }

  @Test(expected = IllegalStateException.class)
  public void testUpdateProfileFieldAvatarUploadMissing() throws IllegalAccessException, IOException {
    when(uploadService.getUploadResource("nope")).thenReturn(null);
    service.updateProfileField(profile,
                               Profile.AVATAR,
                               "nope",
                               false,
                               TEST_USER_2);
  }

  @Test
  public void testUpdateProfileFieldAvatarSuccess() throws IllegalAccessException, IOException {
    File f = csv("avatar.bin", "content");
    UploadResource ur = mock(UploadResource.class);
    when(ur.getStoreLocation()).thenReturn(f.getAbsolutePath());
    when(ur.getFileName()).thenReturn("pic.png");
    when(ur.getMimeType()).thenReturn("image/png");
    when(uploadService.getUploadResource("up1")).thenReturn(ur);

    service.updateProfileField(profile,
                               Profile.AVATAR,
                               "up1",
                               true,
                               TEST_USER_2);

    verify(identityManager, atLeastOnce()).updateProfile(any(Profile.class), eq(TEST_USER_2), eq(true));
    verify(uploadService).removeUploadResource("up1");
  }

  @Test
  public void testUpdateProfileFieldBannerSuccess() throws IllegalAccessException, IOException {
    File f = csv("banner.bin", "content");
    UploadResource ur = mock(UploadResource.class);
    when(ur.getStoreLocation()).thenReturn(f.getAbsolutePath());
    when(ur.getFileName()).thenReturn("ban.png");
    when(ur.getMimeType()).thenReturn("image/png");
    when(uploadService.getUploadResource("up2")).thenReturn(ur);

    service.updateProfileField(profile,
                               Profile.BANNER,
                               "up2",
                               true,
                               TEST_USER_2);

    verify(identityManager, atLeastOnce()).updateProfile(any(Profile.class), eq(TEST_USER_2), eq(true));
    verify(uploadService).removeUploadResource("up2");
  }

  @Test
  public void testUpdateProfileFieldSimpleProperty() throws IllegalAccessException, IOException {
    when(profilePropertyService.getProfileSettingByName("department")).thenReturn(null);
    service.updateProfileField(profile,
                               "department",
                               "R&D",
                               true,
                               TEST_USER_2);
    verify(identityManager, atLeastOnce()).updateProfile(any(Profile.class), eq(TEST_USER_2), eq(true));
  }

  @Test
  public void testSendOnBoardingEmailFalseNoUpdate() throws Exception {
    when(passwordRecoveryService.sendOnboardingEmail(any(UserImpl.class), any(Locale.class), any(StringBuilder.class)))
                                                                                                                       .thenReturn(false);

    File f = csv("onboard2.csv",
                 HEADER_LINE_2,
                 "sam,Secret123,true,sam@ex.com,Sam,S, true");

    when(userHandler.findUserByName("sam", UserStatus.ANY)).thenReturn(null);
    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);

    UserImportResult r = new UserImportResult();
    assertDoesNotThrow(() -> service.importUsers(f.getAbsolutePath(),
                                                 r,
                                                 TEST_USER_2,
                                                 Locale.ENGLISH,
                                                 URL));
  }

  @Test
  public void testAddUserMembershipsWarningsForMissingGroupAndMembershipType() throws Exception {
    File f = csv("memberships_warn.csv",
                 "userName,password,enabled,email,firstName,lastName,groups",
                 "mia,Secret123,true,mia@ex.com,Mia,M, manager:/unknown;ghost:/g");

    when(userHandler.findUserByName("mia", UserStatus.ANY)).thenReturn(null);
    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);

    when(groupHandler.findGroupById("/unknown")).thenReturn(null);
    Group g = mock(Group.class);
    when(groupHandler.findGroupById("/g")).thenReturn(g);
    when(membershipTypeHandler.findMembershipType(TEST_USER_1)).thenReturn(null);

    assertDoesNotThrow(() -> service.importUsers(f.getAbsolutePath(),
                                                 new UserImportResult(),
                                                 TEST_USER_2,
                                                 Locale.ENGLISH,
                                                 URL));
  }

  @Test
  public void testValidateUserPasswordMissingUsesNoPasswordValidators() throws Exception {
    File f = csv("validators.csv",
                 HEADER_LINE_5,
                 "val,valid@ex.com,V,Alid");
    when(userHandler.findUserByName("val", UserStatus.ANY)).thenReturn(null);
    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);
    assertDoesNotThrow(() -> service.importUsers(f.getAbsolutePath(),
                                                 new UserImportResult(),
                                                 TEST_USER_2,
                                                 Locale.ENGLISH,
                                                 URL));
  }

  @Test
  public void testSaveProfileSuccess() throws ObjectNotFoundException, IllegalAccessException, IOException {
    Map<String, Object> props = new HashMap<>();
    props.put("aboutMe", "Hello");
    service.saveProfile("john",
                        props,
                        TEST_USER_2);
    verify(identityManager).updateProfile(any(Profile.class), eq(TEST_USER_2), eq(true));
  }

  @Test
  public void testEmailAlreadyExistsZeroOrSameUserFalse() throws Exception {
    File f = csv("email_none.csv",
                 HEADER_LINE_4,
                 "tom,Secret123,tom@ex.com,Tom,M");
    when(userHandler.findUserByName("tom", UserStatus.ANY)).thenReturn(null);

    ListAccess<User> none = mock(ListAccess.class);
    when(none.getSize()).thenReturn(0);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(none);

    service.importUsers(f.getAbsolutePath(),
                        new UserImportResult(),
                        TEST_USER_2,
                        Locale.ENGLISH,
                        URL);

    ListAccess<User> oneSame = mock(ListAccess.class);
    when(oneSame.getSize()).thenReturn(1);
    User same = mock(User.class);
    when(same.getUserName()).thenReturn("tom");
    when(oneSame.load(0, 1)).thenReturn(new User[] { same });
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(oneSame);

    assertDoesNotThrow(() -> service.importUsers(f.getAbsolutePath(),
                                                 new UserImportResult(),
                                                 TEST_USER_2,
                                                 Locale.ENGLISH,
                                                 URL));
  }

  @Test
  public void testEmailAlreadyExistsMultipleTrue() throws Exception {
    File f = csv("email_many.csv",
                 HEADER_LINE_4,
                 "tina,Secret123,tina@ex.com,Tina,N");
    when(userHandler.findUserByName("tina", UserStatus.ANY)).thenReturn(null);

    ListAccess<User> listAccess = mock(ListAccess.class);
    when(listAccess.getSize()).thenReturn(2);
    when(organizationService.getUserHandler().findUsersByQuery(any(Query.class), eq(UserStatus.ANY))).thenReturn(listAccess);

    assertDoesNotThrow(() -> service.importUsers(f.getAbsolutePath(),
                                                 new UserImportResult(),
                                                 TEST_USER_2,
                                                 Locale.ENGLISH,
                                                 URL));
  }

  private static void deleteRecursively(File f) {
    if (f == null || !f.exists()) {
      return;
    }
    if (f.isDirectory()) {
      for (File c : Objects.requireNonNull(f.listFiles())) {
        deleteRecursively(c);
      }
    }
    try {
      Files.deleteIfExists(Path.of(f.getAbsolutePath()));
    } catch (IOException e) {
      f.deleteOnExit();
    }
  }

  @SneakyThrows
  private File csv(String name, String... lines) {
    File file = new File(tempDir, name);
    Files.write(file.toPath(), String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    return file;
  }
}
