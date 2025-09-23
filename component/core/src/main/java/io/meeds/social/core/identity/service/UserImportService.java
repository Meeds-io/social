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

import static io.meeds.social.core.identity.model.UserImportResult.getFieldName;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.rest.UserFieldValidator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.idm.UserImpl;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.Attachment;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
import org.exoplatform.social.core.profileproperty.ProfilePropertyService;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.login.recovery.PasswordRecoveryService;
import org.exoplatform.portal.config.UserACL;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.core.identity.model.UserImportResult;
import io.meeds.social.util.JsonUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Setter;
import lombok.SneakyThrows;

@Service
public class UserImportService {

  private static final String                   ONBOARD_USER_FIELD                = "onboardUser";

  private static final String                   EXTERNALS_GROUP                   = "/platform/externals";

  private static final String                   GUEST                             = "Guest";

  private static final List<String>             SYSTEM_PARENT_MULTIVALUED_FIELDS  = Arrays.asList("user",
                                                                                                  "phones",
                                                                                                  "ims",
                                                                                                  "urls",
                                                                                                  "manager");

  private static final String                   TYPE_FIELD                        = "type";

  private static final String                   ENABLED_FIELD                     = "enabled";

  private static final String                   TIME_ZONE_FIELD                   = "timeZone";

  private static final String                   ABOUT_ME_FIELD                    = "aboutMe";

  private static final String                   GROUPS_FIELD                      = "groups";

  private static final Log                      LOG                               = ExoLogger.getLogger(UserImportService.class);

  private static final String                   PASSWORD_FIELD                    = "password";

  private static final String                   FIRST_NAME_FIELD                  = "firstName";

  private static final String                   LAST_NAME_FIELD                   = "lastName";

  private static final String                   EMAIL_FIELD                       = "email";

  private static final String                   USER_NAME_FIELD                   = "userName";

  private static final List<String>             STANDARD_FIELDS                   = List.of(USER_NAME_FIELD,
                                                                                            PASSWORD_FIELD,
                                                                                            TYPE_FIELD,
                                                                                            GROUPS_FIELD,
                                                                                            ABOUT_ME_FIELD,
                                                                                            TIME_ZONE_FIELD,
                                                                                            ENABLED_FIELD);

  private static final List<String>             NOT_PROPERTY_FIELDS               = List.of(USER_NAME_FIELD,
                                                                                            FIRST_NAME_FIELD,
                                                                                            LAST_NAME_FIELD,
                                                                                            PASSWORD_FIELD,
                                                                                            EMAIL_FIELD,
                                                                                            GROUPS_FIELD,
                                                                                            ENABLED_FIELD,
                                                                                            TYPE_FIELD);

  private static final UserFieldValidator       USERNAME_VALIDATOR                =
                                                                   new UserFieldValidator(USER_NAME_FIELD, true, false);

  private static final UserFieldValidator       EMAIL_VALIDATOR                   =
                                                                new UserFieldValidator(EMAIL_FIELD, false, false);

  private static final UserFieldValidator       LASTNAME_VALIDATOR                =
                                                                   new UserFieldValidator(LAST_NAME_FIELD, false, true);

  private static final UserFieldValidator       FIRSTNAME_VALIDATOR               =
                                                                    new UserFieldValidator(FIRST_NAME_FIELD, false, true);

  private static final UserFieldValidator       PASSWORD_VALIDATOR                = new UserFieldValidator(PASSWORD_FIELD,
                                                                                                           false,
                                                                                                           false,
                                                                                                           8,
                                                                                                           255);

  private static final List<UserFieldValidator> USER_FIELD_VALIDATORS             = Arrays.asList(USERNAME_VALIDATOR,
                                                                                                  EMAIL_VALIDATOR,
                                                                                                  LASTNAME_VALIDATOR,
                                                                                                  FIRSTNAME_VALIDATOR,
                                                                                                  PASSWORD_VALIDATOR);

  private static final List<UserFieldValidator> USER_FIELD_NO_PASSWORD_VALIDATORS = Arrays.asList(USERNAME_VALIDATOR,
                                                                                                  EMAIL_VALIDATOR,
                                                                                                  LASTNAME_VALIDATOR,
                                                                                                  FIRSTNAME_VALIDATOR);

  protected Map<String, UserImportResult>       importUsersProcessing             = new ConcurrentHashMap<>();

  protected ExecutorService                     importExecutorService             = null;

  @Autowired
  @Setter
  private IdentityManager                       identityManager;

  @Autowired
  @Setter
  private OrganizationService                   organizationService;

  @Autowired
  @Setter
  private ProfilePropertyService                profilePropertyService;

  @Autowired
  @Setter
  private LocaleConfigService                   localeConfigService;

  @Autowired
  @Setter
  private PasswordRecoveryService               passwordRecoveryService;

  @Autowired
  @Setter
  private UploadService                         uploadService;

  @Autowired
  private UserACL                               userAcl;

  private Group                                 externalsGroup                    = null;

  private MembershipType                        memberMembershipType              = null;

  @PostConstruct
  public void init() {
    this.importExecutorService = Executors.newSingleThreadExecutor();
  }

  @PreDestroy
  public void stop() {
    this.importExecutorService.shutdownNow();
  }

  public UserImportResult getUsersImportResult(String uploadId) {
    return importUsersProcessing.get(uploadId);
  }

  public void cleanUsersImportResult(String uploadId) {
    importUsersProcessing.remove(uploadId);
    uploadService.removeUploadResource(uploadId);
  }

  public void importUsers(String uploadId,
                          String modifierUsername,
                          Locale locale,
                          String url,
                          boolean sync) {
    UploadResource uploadResource = uploadService.getUploadResource(uploadId);
    if (uploadResource == null) {
      throw new IllegalArgumentException("UPLOAD_ID:NOT_FOUND");
    }

    UserImportResult userImportResult = new UserImportResult();
    importUsersProcessing.put(uploadId, userImportResult);

    // count file lines
    try (BufferedReader reader = new BufferedReader(new FileReader(uploadResource.getStoreLocation()))) {
      userImportResult.setCount(reader.lines().count() - 1);
    } catch (FileNotFoundException e) {
      throw new IllegalStateException("UPLOAD_ID_FILE:NOT_FOUND");
    } catch (IOException e) {
      throw new IllegalStateException("ERROR_READING_FILE");
    }

    if (userImportResult.getCount() < 1) {
      throw new IllegalArgumentException("BAD_FORMAT:FILE_EMPTY");
    }

    if (sync) {
      importUsers(uploadResource.getStoreLocation(), userImportResult, modifierUsername, locale, url);
    } else {
      importUsersAsync(uploadResource.getStoreLocation(), userImportResult, modifierUsername, locale, url);
    }
  }

  protected void importUsersAsync(String fileLocation,
                                  UserImportResult userImportResult,
                                  String modifierUsername,
                                  Locale locale,
                                  String url) {
    importExecutorService.execute(() -> importUsersTransactional(fileLocation,
                                                                 userImportResult,
                                                                 modifierUsername,
                                                                 locale,
                                                                 url));
  }

  @ContainerTransactional
  protected void importUsersTransactional(String fileLocation,
                                          UserImportResult userImportResult,
                                          String modifierUsername,
                                          Locale locale,
                                          String url) {
    importUsers(fileLocation, userImportResult, modifierUsername, locale, url);
  }

  @SneakyThrows
  protected void importUsers(String fileLocation,
                             UserImportResult userImportResult,
                             String modifierUsername,
                             Locale locale,
                             String url) {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
      // Retrieve header line and import others
      String headerLine = null;
      headerLine = reader.readLine();
      if (StringUtils.isBlank(headerLine)) {
        return;
      }
      List<String> fields = new ArrayList<>(Arrays.stream(headerLine.split(",")).map(String::trim).toList());
      List<String> unauthorizedFields = computeFields(userImportResult, fields);

      String userCSVLine = reader.readLine();
      while (userCSVLine != null) {
        userImportResult.incrementProcessed();
        if (StringUtils.isNotBlank(userCSVLine)) {
          try { // NOSONAR
            importUser(userImportResult, modifierUsername, locale, url, fields, unauthorizedFields, userCSVLine);
          } catch (Throwable e) { // NOSONAR
            LOG.warn("Error importing user data at line '{}'. Continue processing other lines",
                     userImportResult.getProcessedCount() + 1,
                     e);
          } finally {
            RequestLifeCycle.restartTransaction();
          }
        }
        userCSVLine = reader.readLine();
      }
    }
  }

  protected String importUser(UserImportResult userImportResult, // NOSONAR
                              String modifierUsername,
                              Locale locale,
                              String url,
                              List<String> fields,
                              List<String> fieldsToRemove,
                              String userCSVLine) throws Exception {
    List<String> userProperties = Arrays.asList(userCSVLine.split(","));
    JSONObject userObject = new JSONObject();
    for (int i = 0; i < fields.size(); i++) {
      if (i < userProperties.size()) {
        userObject.put(fields.get(i), userProperties.get(i));
      }
    }
    UserImpl user = JsonUtils.fromJsonString(userObject.toString(), UserImpl.class);
    String userName = user.getUserName();
    if (StringUtils.isBlank(userName)) {
      userImportResult.addErrorMessage(userName, "BAD_LINE_FORMAT:MISSING_USERNAME");
      return userName;
    } else if (userProperties.size() < fields.size()) {
      userImportResult.addErrorMessage(userName, "BAD_LINE_FORMAT");
      return userName;
    }

    String errorMessage = null;
    try {
      errorMessage = validateUser(userObject, locale, fields);
    } catch (Exception e) {
      errorMessage = "USER_VALIDATION_ERROR:" + e.getMessage();
    }
    if (StringUtils.isNotBlank(errorMessage)) {
      userImportResult.addErrorMessage(userName, errorMessage);
      return userName;
    }
    boolean onboardUser = userObject.has(ONBOARD_USER_FIELD) && userObject.getString(ONBOARD_USER_FIELD).equals("true");
    boolean userStatus = userObject.has(ENABLED_FIELD) && ("true".equalsIgnoreCase(userObject.getString(ENABLED_FIELD))
                                                           || "false".equalsIgnoreCase(userObject.getString(ENABLED_FIELD)));

    UserHandler userHandler = organizationService.getUserHandler();
    User existingUser = userHandler.findUserByName(userName, UserStatus.ANY);

    if (existingUser != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Skipping password update for: {}", userName);
      }
      // skipping password overwrite from csvLine
      user.setPassword(null);
      try {
        user.setEnabled(true);
        userHandler.saveUser(user, true);
        if (userStatus) {
          boolean enabled = Boolean.parseBoolean(userObject.getString(ENABLED_FIELD));
          userHandler.setEnabled(userName, enabled, true);
          user.setEnabled(enabled);
        }
      } catch (Exception e) {
        LOG.warn("Error updating user {}", userName, e);
        userImportResult.addErrorMessage(userName, "UPDATE_USER_ERROR:" + e.getMessage());
        return userName;
      } finally {
        RequestLifeCycle.restartTransaction();
      }
      onboardUser = onboardUser
                    && existingUser.isEnabled()
                    && (existingUser.getLastLoginTime().getTime() == existingUser.getCreatedDate().getTime());
    } else if (isEmailAlreadyExists(user.getUserName(), user.getEmail())) {
      userImportResult.addErrorMessage(userName, "EMAIL:ALREADY_EXISTS");
      return userName;
    } else {
      try {
        userHandler.createUser(user, true);
        if (userStatus) {
          boolean enabled = Boolean.parseBoolean(userObject.getString(ENABLED_FIELD));
          userHandler.setEnabled(userName, enabled, true);
          user.setEnabled(enabled);
        }
      } catch (Exception e) {
        LOG.warn("Error importing user {}", userName, e);
        userImportResult.addErrorMessage(userName, "CREATE_USER_ERROR:" + e.getMessage());
        return userName;
      } finally {
        RequestLifeCycle.restartTransaction();
      }
    }

    updateUserProfileProperties(userImportResult, modifierUsername, fieldsToRemove, userObject, userName);

    addUserMemberships(user, userObject, userImportResult);

    // onboard user if the onboardUser csv field is true, the user is enabled
    // and not yet logged in
    if (onboardUser) {
      sendOnBoardingEmail(user, modifierUsername, url);
    }
    return userName;
  }

  protected void updateUserProfileProperties(UserImportResult userImportResult,
                                             String modifierUsername,
                                             List<String> fieldsToRemove,
                                             JSONObject userObject,
                                             String userName) {
    try {
      Map<String, Object> userProfileProperties = computeUserProfileProperties(fieldsToRemove, userObject);
      saveProfile(userName, userProfileProperties, modifierUsername);
    } catch (ObjectNotFoundException e) {
      LOG.info("User Identity profile {} wasn't found, ignore processing profile properties. This may happen when user is disabled",
               userName);
    } catch (IdentityStorageException e) {
      LOG.warn("Error saving user profile {}", userName, e);
      userImportResult.addWarnMessage(userName, e.getMessageKey());
    } catch (Exception e) {
      LOG.warn("Error saving user profile {}", userName, e);
      userImportResult.addWarnMessage(userName, "CREATE_USER_PROFILE_ERROR:" + e.getMessage());
    }
  }

  protected Map<String, Object> computeUserProfileProperties(List<String> fieldsToRemove, JSONObject userObject) {
    // Delete imported User object properties
    // And properties to ignore
    JSONObject userObjectCopy = new JSONObject();
    Iterator<String> keys = userObject.keys();
    while (keys.hasNext()) {
      String k = keys.next();
      if (!fieldsToRemove.contains(k) && !NOT_PROPERTY_FIELDS.contains(k)) {
        userObjectCopy.put(k, userObject.getString(k));
      }
    }

    Map<String, Object> userProfileProperties = new HashMap<>();
    Iterator<String> properties = userObjectCopy.keys();
    while (properties.hasNext()) {
      String propertyName = properties.next();
      String propertyValue = userObjectCopy.getString(propertyName);
      ProfilePropertySetting propertySetting;
      ProfilePropertySetting parentPropertySetting = null;
      propertySetting = profilePropertyService.getProfileSettingByName(propertyName);
      if (propertySetting == null && propertyName.contains(".")) {
        String[] propertyNames = propertyName.split("\\.");
        propertySetting = profilePropertyService.getProfileSettingByName(propertyNames[1]);
      }
      if (propertySetting != null && propertySetting.getParentId() != null) {
        parentPropertySetting = profilePropertyService.getProfileSettingById(propertySetting.getParentId());
      }
      Map<String, String> childPropertyMap = new HashMap<>();
      childPropertyMap.put("value", propertyValue);
      if (propertySetting != null && propertySetting.isMultiValued()) {
        userProfileProperties.computeIfAbsent(propertySetting.getPropertyName(), k -> new ArrayList<Map<String, String>>());
        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> values =
                                              (ArrayList<Map<String, String>>) userProfileProperties.get(propertySetting.getPropertyName());
        for (String val : propertyValue.split(";")) {
          Map<String, String> childProperty = new HashMap<>();
          childProperty.put("value", val);
          values.add(childProperty);
        }
        userProfileProperties.put(propertySetting.getPropertyName(), values);
      } else if (parentPropertySetting != null) {
        childPropertyMap.put("key", propertySetting.getPropertyName());
        userProfileProperties.computeIfAbsent(parentPropertySetting.getPropertyName(), k -> new ArrayList<Map<String, String>>());
        @SuppressWarnings("unchecked")
        ArrayList<Map<String, String>> values =
                                              (ArrayList<Map<String, String>>) userProfileProperties.get(parentPropertySetting.getPropertyName());
        values.add(childPropertyMap);
        userProfileProperties.put(parentPropertySetting.getPropertyName(), values);
      } else {
        userProfileProperties.put(propertyName, propertyValue);
      }
    }
    return userProfileProperties;
  }

  @SneakyThrows
  protected void addUserMemberships(UserImpl user, // NOSONAR
                                    JSONObject userObject,
                                    UserImportResult userImportResult) {
    try {
      if (userObject.has(TYPE_FIELD)
          && GUEST.equalsIgnoreCase(userObject.getString(TYPE_FIELD))) {
        RequestLifeCycle.restartTransaction();
        try {
          linkMembership(user,
                         getExternalsGroup(),
                         getMemberMembershipType(),
                         userImportResult);
          organizationService.getMembershipHandler()
                             .removeMembership(String.format("member:%s:/platform/users",
                                                             user.getUserName()),
                                               true);
        } finally {
          RequestLifeCycle.restartTransaction();
        }
      }
      String memberships = userObject.has(GROUPS_FIELD) ? userObject.getString(GROUPS_FIELD) : null;
      if (StringUtils.isNotBlank(memberships)) {
        String[] membershipsList = memberships.split(";");
        for (String membershipExpression : membershipsList) {
          String membershipType = membershipExpression.contains(":") ?
                                                                     StringUtils.trim(membershipExpression.split(":")[0]) :
                                                                     SpaceUtils.MEMBER;
          String groupId = membershipExpression.contains(":") ? StringUtils.trim(membershipExpression.split(":")[1]) :
                                                              membershipExpression;
          Group groupObject = organizationService.getGroupHandler().findGroupById(groupId);
          if (groupObject == null) {
            userImportResult.addWarnMessage(user.getUserName(), "GROUP_NOT_EXISTS:" + groupId);
          } else {
            MembershipType mType = organizationService.getMembershipTypeHandler()
                                                      .findMembershipType(membershipType);
            if (mType == null) {
              userImportResult.addWarnMessage(user.getUserName(), "MEMBERSHIP_TYPE_NOT_EXISTS:" + membershipType);
            } else {
              linkMembership(user, groupObject, mType, userImportResult);
            }
          }
        }
      }
    } catch (Exception e) {
      userImportResult.addWarnMessage(user.getUserName(), "IMPORT_MEMBERSHIP_ERROR:" + e.getMessage());
    }
  }

  protected void linkMembership(UserImpl user, Group groupObject, MembershipType mType, UserImportResult userImportResult) {
    try {
      organizationService.getMembershipHandler().linkMembership(user, groupObject, mType, true);
    } catch (Exception e) {
      userImportResult.addWarnMessage(user.getUserName(), "IMPORT_MEMBERSHIP_ERROR:" + e.getMessage());
    }
  }

  protected List<String> computeFields(UserImportResult userImportResult, List<String> fields) { // NOSONAR
    List<String> unauthorizedFields = new ArrayList<>();
    for (String field : fields) {
      if (!STANDARD_FIELDS.contains(field)) {
        if (!field.contains(".")) {
          ProfilePropertySetting propertySetting = profilePropertyService.getProfileSettingByName(field);
          if (propertySetting == null) {
            userImportResult.addWarnMessage("ALL", "PROFILE_PROPERTY_DOES_NOT_EXIST:" + field);
            unauthorizedFields.add(field);
          } else if (propertySetting.isHasChildProperties()) {
            userImportResult.addWarnMessage("ALL", "PARENT_PROPERTY_SHOULD_NOT_HAVE_VALUES:" + field);
            unauthorizedFields.add(field);
          } else if (propertySetting.isMultiValued() && !SYSTEM_PARENT_MULTIVALUED_FIELDS.contains(field)) {
            userImportResult.addWarnMessage("ALL", "CUSTOM_FIELD_MULTIVALUED:" + field);
            unauthorizedFields.add(field);
          }
        } else {
          String[] fieldNames = field.split("\\.");
          ProfilePropertySetting parentProperty = profilePropertyService.getProfileSettingByName(fieldNames[0]);
          if (fieldNames.length > 2) {
            userImportResult.addWarnMessage("ALL", "PROPERTY_HAS_MORE_THAN_ONE_PARENT:" + field);
            unauthorizedFields.add(field);
          } else if (parentProperty == null) {
            userImportResult.addWarnMessage("ALL", "PROPERTY_HAS_MISSING_PARENT_PROPERTY:" + field);
            unauthorizedFields.add(field);
          } else if (parentProperty.isMultiValued()
                     && !SYSTEM_PARENT_MULTIVALUED_FIELDS.contains(parentProperty.getPropertyName())) {
            userImportResult.addWarnMessage("ALL", "CUSTOM_PARENT_FIELD:" + field);
            unauthorizedFields.add(field);
          }
        }
      }
    }
    return unauthorizedFields;
  }

  protected void saveProfile(String username,
                             Map<String, Object> profileProperties,
                             String modifierUsername) throws ObjectNotFoundException, IllegalAccessException, IOException {
    Identity userIdentity = identityManager.getOrCreateUserIdentity(username);
    if (userIdentity == null) {
      throw new ObjectNotFoundException("User identity of " + username + " wasn't found. It can be due to a disabled user.");
    } else {
      Profile profile = userIdentity.getProfile();

      Set<Entry<String, Object>> profileEntries = profileProperties.entrySet();
      for (Entry<String, Object> entry : profileEntries) {
        String name = entry.getKey();
        Object value = entry.getValue();
        String fieldName = getFieldName(name);
        updateProfileField(profile, fieldName, value, false, modifierUsername);
      }
      identityManager.updateProfile(profile, modifierUsername, true);
    }
  }

  protected void updateProfileField(Profile profile,
                                    String name,
                                    Object value,
                                    boolean save,
                                    String modifierUsername) throws IllegalAccessException, IOException {
    ProfilePropertySetting propertySetting = profilePropertyService.getProfileSettingByName(name);
    if (propertySetting != null && !propertySetting.isEditable() && !StringUtils.equals(userAcl.getSuperUser(), modifierUsername)) {
      throw new IllegalAccessException(String.format("Not allowed to update non modifiable field '%s'", name));
    } else if (Profile.EXTERNAL.equals(name)) {
      throw new IllegalAccessException("Not allowed to update EXTERNAL field");
    } else if (Profile.USERNAME.equals(name)) {
      throw new IllegalAccessException("Not allowed to update USERNAME field");
    } else if (Profile.AVATAR.equals(name) || Profile.BANNER.equals(name)) {
      UploadResource uploadResource = uploadService.getUploadResource(value.toString());
      if (uploadResource == null) {
        throw new IllegalStateException("No uploaded resource found with uploadId = " + value);
      }
      String storeLocation = uploadResource.getStoreLocation();
      try (FileInputStream inputStream = new FileInputStream(storeLocation)) {
        Attachment attachment = null;
        if (Profile.AVATAR.equals(name)) {
          attachment = new AvatarAttachment(null,
                                            uploadResource.getFileName(),
                                            uploadResource.getMimeType(),
                                            inputStream,
                                            System.currentTimeMillis());
          profile.setListUpdateTypes(Arrays.asList(UpdateType.AVATAR));
        } else {
          attachment = new BannerAttachment(null,
                                            uploadResource.getFileName(),
                                            uploadResource.getMimeType(),
                                            inputStream,
                                            System.currentTimeMillis());
          profile.setListUpdateTypes(Arrays.asList(UpdateType.BANNER));
        }
        profile.setProperty(name, attachment);
        if (save) {
          identityManager.updateProfile(profile, modifierUsername, true);
        }
      } finally {
        uploadService.removeUploadResource(value.toString());
      }
    } else {
      profile.setProperty(name, value);
      if (save) {
        identityManager.updateProfile(profile, modifierUsername, true);
      }
    }
  }

  protected String validateUser(JSONObject userObject, Locale locale, List<String> fields) {
    String errorMessage = null;
    Iterator<UserFieldValidator> iterator = fields.contains(PASSWORD_FIELD) ? USER_FIELD_VALIDATORS.iterator() :
                                                                            USER_FIELD_NO_PASSWORD_VALIDATORS.iterator();
    while (iterator.hasNext() && errorMessage == null) {
      UserFieldValidator userFieldValidator = iterator.next();
      String fieldName = userFieldValidator.getField();
      String fieldValue = userObject.has(fieldName) ? userObject.getString(fieldName) : null;
      errorMessage = userFieldValidator.validate(locale, fieldValue);
    }
    return errorMessage;
  }

  protected void sendOnBoardingEmail(UserImpl user, String modifierUsername, String url) throws IllegalAccessException,
                                                                                         IOException {
    Locale locale = localeConfigService.getDefaultLocaleConfig().getLocale();
    boolean onBoardingEmailSent = passwordRecoveryService.sendOnboardingEmail(user, locale, new StringBuilder(url));
    if (onBoardingEmailSent) {
      Identity userIdentity = identityManager.getOrCreateUserIdentity(user.getUserName());
      Profile profile = userIdentity.getProfile();
      updateProfileField(profile,
                         Profile.ENROLLMENT_DATE,
                         String.valueOf(Calendar.getInstance().getTimeInMillis()),
                         true,
                         modifierUsername);
    }
  }

  protected boolean isEmailAlreadyExists(String username, String email) throws Exception {
    Query query = new Query();
    query.setEmail(email);
    ListAccess<User> users = organizationService.getUserHandler().findUsersByQuery(query, UserStatus.ANY);
    int usersLength = users.getSize();
    return usersLength > 1 || (usersLength == 1 && !StringUtils.equals(users.load(0, 1)[0].getUserName(), username));
  }

  private Group getExternalsGroup() throws Exception {
    if (externalsGroup == null) {
      externalsGroup = organizationService.getGroupHandler().findGroupById(EXTERNALS_GROUP);
    }
    return externalsGroup;
  }

  private MembershipType getMemberMembershipType() throws Exception {
    if (memberMembershipType == null) {
      memberMembershipType = organizationService.getMembershipTypeHandler().findMembershipType("member");
    }
    return memberMembershipType;
  }

}
