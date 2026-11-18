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
package org.exoplatform.social.core.profileproperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertyOption;
import org.exoplatform.social.core.profileproperty.model.ProfilePropertySetting;
import org.exoplatform.social.core.profileproperty.storage.ProfileSettingStorage;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.translation.model.TranslationField;
import io.meeds.social.translation.service.TranslationService;

public class ProfilePropertyServiceImpl implements ProfilePropertyService, Startable {

  /**
   * Broadcast, with the updated setting as data, when an edit changes the
   * string the profile index holds for the property's values: an option's
   * value or labels, the dropdown switch, or the property hidden or shown
   * again by the administrator, whose values the documents leave out while it
   * is not visible.
   */
  public static final String                         PROPERTY_OPTIONS_UPDATED_EVENT         = "property_options_updated";

  /** Number of property holders read, then queued for reindex, at once. */
  public static final int                            PROPERTY_HOLDERS_PAGE_SIZE             = 1000;

  private static final String                        PROPERTY_REINDEX_THREAD_NAME           = "profile-option-reindex";

  private static final long                          PROPERTY_REINDEX_STOP_TIMEOUT_SECONDS  = 5;

  private static final Log                           LOG                                    =
                                                         ExoLogger.getLogger(ProfilePropertyServiceImpl.class);

  private final ProfileSettingStorage                profileSettingStorage;

  private final SettingService                       settingService;

  private final IndexingService                      indexingService;

  private final ListenerService                      listenerService;

  private final TranslationService                   translationService;

  private final IdentityDAO                          identityDAO;

  /**
   * Set when the container stops: no page of a property holders walk starts
   * once it is set. The interrupt flag is not the signal, since a page that
   * turns the interrupt into an exception clears it.
   */
  private final AtomicBoolean                        stopping                               = new AtomicBoolean();

  /** One property holders walk at a time; further edits are queued. */
  private final ExecutorService                      propertyReindexExecutor                =
                                                                             Executors.newSingleThreadExecutor(runnable -> {
                                                                               Thread thread = new Thread(runnable,
                                                                                                          PROPERTY_REINDEX_THREAD_NAME);
                                                                               thread.setDaemon(true);
                                                                               return thread;
                                                                             });

  int                                                propertyHoldersPageSize                = PROPERTY_HOLDERS_PAGE_SIZE;

  private static final String                        SYNCHRONIZED_DISABLED_PROPERTIES       = "synchronizationDisabledProperties";

  private static final String                        UNHIDDENABLE_PROPERTIES_PARAM          = "unHiddenableProperties";

  private static final String                        EXCLUDED_QUICK_SEARCH_PROPERTIES_PARAM = "excludedQuickSearchProperties";

  private static final String                        EXCLUDED_ANALYTICS_INDEX_PROPERTIES    = "excludedAnalyticsIndexProperties";

  private static final Scope                         HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE =
                                                                                            Scope.APPLICATION.id("ProfilePropertySettings");

  private static final String                        HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY   = "HiddenProfilePropertySettings";

  private static final String                        PROFILE_PROPERTY_FIELD_NAME            = "optionValue";

  private static final String                        PROFILE_PROPERTY_OBJECT_TYPE           = "propertySettingOption";

  protected List<ProfilePropertyDatabaseInitializer> profielPropertyPlugins                 = new ArrayList<>();

  private List<String>                               synchronizedGroupDisabledProperties    = new ArrayList<>();

  private List<String>                               nonHiddenableProps                     = new ArrayList<>();

  private List<String>                               excludedQuickSearchProps               = new ArrayList<>();

  private List<String>                               excludedAnalyticsIndexProps            = new ArrayList<>();

  private static final String                        USER_CARD_SETTINGS                     = "UserCardSettings";
  
  private static final String                        USER_TYPE                              = "user";

  public ProfilePropertyServiceImpl(InitParams params,
                                    ProfileSettingStorage profileSettingStorage,
                                    SettingService settingService,
                                    IndexingService indexingService,
                                    ListenerService listenerService,
                                    TranslationService translationService,
                                    IdentityDAO identityDAO) {
    this.profileSettingStorage = profileSettingStorage;
    this.identityDAO = identityDAO;
    this.settingService = settingService;
    this.indexingService = indexingService;
    this.listenerService = listenerService;
    this.translationService = translationService;
    if (params != null) {
      try {
        synchronizedGroupDisabledProperties = Arrays.asList(params.getValueParam(SYNCHRONIZED_DISABLED_PROPERTIES)
                                                                  .getValue()
                                                                  .split(","));
        nonHiddenableProps = Arrays.asList(params.getValueParam(UNHIDDENABLE_PROPERTIES_PARAM).getValue().split(","));
        excludedQuickSearchProps = Arrays.asList(params.getValueParam(EXCLUDED_QUICK_SEARCH_PROPERTIES_PARAM)
                                                       .getValue()
                                                       .split(","));
        excludedAnalyticsIndexProps = Arrays.asList(params.getValueParam(EXCLUDED_ANALYTICS_INDEX_PROPERTIES)
                                                          .getValue()
                                                          .split(","));
      } catch (Exception e) {
        LOG.warn("List of disabled properties for synchronization not provided, all properties can be synchronized! ");
      }
    }
  }

  @Override
  public List<ProfilePropertySetting> getPropertySettings() {
    return profileSettingStorage.getPropertySettings();
  }

  @Override
  public List<ProfilePropertySetting> getSynchronizedPropertySettings() {
    return profileSettingStorage.getSynchronizedPropertySettings();
  }

  @Override
  public ProfilePropertySetting getProfileSettingByName(String name) {
    return profileSettingStorage.findProfileSettingByName(name);
  }

  @Override
  public List<String> getExcludedAnalyticsIndexProperties() {
    return excludedAnalyticsIndexProps;
  }

  @Override
  public ProfilePropertySetting getProfileSettingById(Long id) {
    return profileSettingStorage.getProfileSettingById(id);
  }

  @Override
  public List<String> getUnhiddenableProfileProperties() {
    return nonHiddenableProps;
  }

  @Override
  public List<String> getExcludedQuickSearchProperties() {
    return excludedQuickSearchProps;
  }

  @Override
  public boolean isPropertySettingHiddenable(Long id) {
    return isPropertySettingHiddenable(getProfileSettingById(id));
  }

  @Override
  public ProfilePropertySetting createPropertySetting(ProfilePropertySetting profilePropertySetting) throws ObjectAlreadyExistsException {

    validatePropertySetting(profilePropertySetting);

    ProfilePropertySetting storedProfilePropertySetting =
                                                        profileSettingStorage.findProfileSettingByName(profilePropertySetting.getPropertyName());
    if (storedProfilePropertySetting != null) {
      throw new ObjectAlreadyExistsException(storedProfilePropertySetting,
                                             "A profile property with the provided name already exists.");
    }

    if (!isGroupSynchronizedEnabledProperty(profilePropertySetting)) {
      profilePropertySetting.setGroupSynchronized(false);
    }

    profilePropertySetting.setUpdated(System.currentTimeMillis());
    storedProfilePropertySetting = profileSettingStorage.saveProfilePropertySetting(profilePropertySetting,
                                                                                    true);
    savePropertyOptionsTranslations(storedProfilePropertySetting, profilePropertySetting.getPropertyOptions());
    if (storedProfilePropertySetting.getOrder() == null) {
      storedProfilePropertySetting.setOrder(storedProfilePropertySetting.getId());
      storedProfilePropertySetting = profileSettingStorage.saveProfilePropertySetting(storedProfilePropertySetting, false);
    }

    try {
      listenerService.broadcast("profile-property-setting-created", this, storedProfilePropertySetting);
    } catch (Exception e) {
      LOG.error("An error occurred while broadcasting the creation event for the property setting '{}'.",
                storedProfilePropertySetting.getPropertyName(),
                e);
    }
    return storedProfilePropertySetting;
  }

  @Override
  public void updatePropertySetting(ProfilePropertySetting profilePropertySetting) {

    validatePropertySetting(profilePropertySetting);

    if (profilePropertySetting.isHiddenbale()
        && getUnhiddenableProfileProperties().contains(profilePropertySetting.getPropertyName())) {
      throw new IllegalArgumentException(String.format("%s cannot be hidden", profilePropertySetting.getPropertyName()));
    }
    if (profilePropertySetting.isMultiValued() && isUserCardFieldSettings(profilePropertySetting.getPropertyName())) {
      throw new IllegalArgumentException(String.format("%s cannot be multivalued", profilePropertySetting.getPropertyName()));
    }
    if (!isGroupSynchronizedEnabledProperty(profilePropertySetting)) {
      profilePropertySetting.setGroupSynchronized(false);
    }
    ProfilePropertySetting createdProfilePropertySetting =
                                                         profileSettingStorage.getProfileSettingById(profilePropertySetting.getId());
    // read before the save: the stored options are compared after it
    boolean wasDropdownList = createdProfilePropertySetting != null && createdProfilePropertySetting.isDropdownList();
    boolean wasMultiValued = createdProfilePropertySetting != null && createdProfilePropertySetting.isMultiValued();
    boolean wasVisible = createdProfilePropertySetting != null && createdProfilePropertySetting.isVisible();
    Map<Long, String> previousOptionValues = getOptionValues(createdProfilePropertySetting == null ? null
                                                                                                   : createdProfilePropertySetting.getPropertyOptions());
    
    if (createdProfilePropertySetting != null && isDefaultProperties(profilePropertySetting)) {
      profilePropertySetting.setMultiValued(createdProfilePropertySetting.isMultiValued());
    }

    validatePropertySettingType(createdProfilePropertySetting, profilePropertySetting);

    profilePropertySetting.setUpdated(System.currentTimeMillis());
    ProfilePropertySetting updatedPropertySetting =
                                                  profileSettingStorage.saveProfilePropertySetting(profilePropertySetting, false);
    boolean labelsChanged = savePropertyOptionsTranslations(updatedPropertySetting, profilePropertySetting.getPropertyOptions());
    if (labelsChanged
        || (createdProfilePropertySetting != null && wasVisible != updatedPropertySetting.isVisible())
        // the indexed string of a value depends on the flag (toIndexedValue)
        || (createdProfilePropertySetting != null && wasMultiValued != updatedPropertySetting.isMultiValued())
        || isOptionValueChanged(updatedPropertySetting, wasDropdownList, previousOptionValues)) {
      try {
        listenerService.broadcast(PROPERTY_OPTIONS_UPDATED_EVENT, this, updatedPropertySetting);
      } catch (Exception e) {
        LOG.error("An error occurred when broadcasting the options update of the property setting {}",
                  updatedPropertySetting.getPropertyName(),
                  e);
      }
    }
    try {
      listenerService.broadcast("profile-property-setting-updated", this, updatedPropertySetting);
    } catch (Exception e) {
      LOG.error("An error occurred when broadcasting the update event of the property setting {}",
                updatedPropertySetting.getPropertyName(),
                e);
    }
  }

  @Override
  public void deleteProfilePropertySetting(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException("Profile Property Setting Technical Identifier is mandatory");
    }
    profileSettingStorage.deleteProfilePropertySetting(id);
  }

  @Override
  public boolean isGroupSynchronizedEnabledProperty(Long id) {
    return isGroupSynchronizedEnabledProperty(getProfileSettingById(id));
  }

  @Override
  public List<String> getPropertySettingNames() {
    return getPropertySettings().stream().map(ProfilePropertySetting::getPropertyName).toList();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void hidePropertySetting(long userIdentityId, long profilePropertyId) {
    List<Long> hiddenProperties = getHiddenProfilePropertyIds(userIdentityId);
    hiddenProperties.remove(profilePropertyId);
    hiddenProperties.add(profilePropertyId);
    settingService.set(Context.USER.id(String.valueOf(userIdentityId)),
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE,
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY,
                       SettingValue.create(hiddenProperties.toString()));
    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(userIdentityId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void showPropertySetting(long userIdentityId, long profilePropertyId) {
    List<Long> hiddenProperties = getHiddenProfilePropertyIds(userIdentityId);
    hiddenProperties.remove(profilePropertyId);
    settingService.set(Context.USER.id(String.valueOf(userIdentityId)),
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE,
                       HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY,
                       SettingValue.create(hiddenProperties.toString()));
    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(userIdentityId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Long> getHiddenProfilePropertyIds(long userIdentityId) {
    List<Long> hiddenProfileProperties = new ArrayList<>();
    SettingValue<?> settingValue = settingService.get(Context.USER.id(String.valueOf(userIdentityId)),
                                                      HIDDEN_PROFILE_PROPERTY_SETTINGS_SCOPE,
                                                      HIDDEN_PROFILE_PROPERTY_SETTINGS_KEY);
    if (settingValue == null) {
      return hiddenProfileProperties;
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      hiddenProfileProperties = mapper.readValue(settingValue.getValue().toString(), new TypeReference<ArrayList<Long>>() {
      });
    } catch (Exception e) {
      LOG.error("Error while parsing hidden properties setting of user: {}", userIdentityId, e);
    }
    return hiddenProfileProperties;
  }

  @Override
  public void start() {

    for (ProfilePropertyDatabaseInitializer plugin : profielPropertyPlugins) {
      try {
        plugin.init(this);
      } catch (Exception ex) {
        LOG.error("Failed start Profile properties Service , probably because of configuration error. Error occurs when initializing properties for {}",
                  plugin.getClass().getName(),
                  ex);
      }
    }

  }

  @Override
  public void stop() {
    stopping.set(true);
    propertyReindexExecutor.shutdownNow();
    try {
      if (!propertyReindexExecutor.awaitTermination(PROPERTY_REINDEX_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        LOG.warn("The reindex of profile property holders is still running {} seconds after the stop",
                 PROPERTY_REINDEX_STOP_TIMEOUT_SECONDS);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void reindexPropertyHolders(String propertyName) {
    if (StringUtils.isBlank(propertyName) || stopping.get()) {
      return;
    }
    try {
      propertyReindexExecutor.execute(() -> walkPropertyHolders(propertyName));
    } catch (RejectedExecutionException e) {
      LOG.warn("The reindex of the holders of profile property {} is not queued: the service is stopping", propertyName);
    }
  }

  /**
   * Reindexes the holders of a property page by page, through a keyset on the
   * identity id. A page whose reindex fails is logged and the walk resumes
   * after its last id; a page whose read fails ends the walk, since there is
   * no id to resume after. No page starts once the service is stopping.
   *
   * @param propertyName the name of the profile property
   */
  void walkPropertyHolders(String propertyName) {
    long lastId = 0;
    while (!stopping.get()) {
      List<Long> identityIds;
      try {
        identityIds = readPropertyHolders(propertyName, lastId);
      } catch (Exception e) {
        LOG.warn("The reindex of the holders of profile property {} ends: the page after identity id {} could not be read",
                 propertyName,
                 lastId,
                 e);
        return;
      }
      if (identityIds.isEmpty()) {
        return;
      }
      try {
        reindexIdentities(identityIds);
      } catch (Exception e) {
        LOG.warn("The holders of profile property {} after identity id {} could not be queued for reindex, the walk resumes after id {}",
                 propertyName,
                 lastId,
                 identityIds.getLast(),
                 e);
      }
      lastId = identityIds.getLast();
      if (identityIds.size() < propertyHoldersPageSize) {
        return;
      }
    }
  }

  @ContainerTransactional(atomic = true)
  List<Long> readPropertyHolders(String propertyName, long lastId) {
    return doReadPropertyHolders(propertyName, lastId);
  }

  @ContainerTransactional(atomic = true)
  void reindexIdentities(List<Long> identityIds) {
    doReindexIdentities(identityIds);
  }

  /** The un-advised body of {@link #readPropertyHolders}, which tests call. */
  List<Long> doReadPropertyHolders(String propertyName, long lastId) {
    return identityDAO.getIdsHoldingPropertyAfterId(OrganizationIdentityProvider.NAME,
                                                    propertyName,
                                                    lastId,
                                                    propertyHoldersPageSize);
  }

  /** The un-advised body of {@link #reindexIdentities}, which tests call. */
  void doReindexIdentities(List<Long> identityIds) {
    for (Long identityId : identityIds) {
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(identityId));
    }
  }

  @Override
  public void addProfilePropertyPlugin(ComponentPlugin profilePropertyInitPlugin) {
    this.profielPropertyPlugins.add((ProfilePropertyDatabaseInitializer) profilePropertyInitPlugin);
  }

  @Override
  public boolean isDefaultProperties(ProfilePropertySetting propertySetting) {
    for (ProfilePropertyDatabaseInitializer plugin : profielPropertyPlugins) {
      if (plugin.getConfig().getProfileProperties() != null && !plugin.getConfig().getProfileProperties().isEmpty()
          && plugin.getConfig()
                   .getProfileProperties()
                   .stream()
                   .anyMatch(profileProperty -> profileProperty.getPropertyName().equals(propertySetting.getPropertyName()))) {
        return true;
      }
    }
    return false;
  }

  private void validatePropertySetting(ProfilePropertySetting profilePropertySetting) {
    if (profilePropertySetting == null) {
      throw new IllegalArgumentException("Profile property setting object is mandatory.");
    }

    if (StringUtils.isBlank(profilePropertySetting.getPropertyName())) {
      throw new IllegalArgumentException("Profile property name is mandatory.");
    }

    if (profilePropertySetting.isDropdownList() && !"text".equals(profilePropertySetting.getPropertyType())) {
      throw new IllegalArgumentException("Only text properties can be dropdown lists.");
    }
  }

  private void validatePropertySettingType(ProfilePropertySetting existingProperty, ProfilePropertySetting updatedProperty) {
    if (existingProperty == null) {
      return;
    }
    String existingType = existingProperty.getPropertyType();
    String updatedType = updatedProperty.getPropertyType();

    if (!Objects.equals(existingType, updatedType) && (USER_TYPE.equals(existingType) || USER_TYPE.equals(updatedType))) {
      throw new IllegalArgumentException("Changing profile property type to or from USER type is not allowed.");
    }
  }

  /**
   * Saves the labels of the posted options on the stored ones, paired by
   * position.
   *
   * @return true when the labels of a stored option changed
   */
  private boolean savePropertyOptionsTranslations(ProfilePropertySetting savedProfilePropertySetting,
                                                  List<ProfilePropertyOption> newOptions) {
    if (!savedProfilePropertySetting.isDropdownList() || newOptions == null) {
      return false;
    }
    boolean labelsChanged = false;
    List<ProfilePropertyOption> savedOptions =
                                             profileSettingStorage.getProfilePropertyOptions(savedProfilePropertySetting.getId(),
                                                                                             0,
                                                                                             0);
    for (int i = 0; i < savedOptions.size(); i++) {
      ProfilePropertyOption savedOption = savedOptions.get(i);
      ProfilePropertyOption newOption = i < newOptions.size() ? newOptions.get(i) : null;
      if (savedOption == null || newOption == null) {
        continue;
      }
      try {
        TranslationField translationField = translationService.getTranslationField(PROFILE_PROPERTY_OBJECT_TYPE,
                                                                                   savedOption.getId(),
                                                                                   PROFILE_PROPERTY_FIELD_NAME);
        Map<Locale, String> previousTranslations = translationField == null ? Collections.emptyMap()
                                                                            : translationField.getLabels();
        Map<Locale, String> translations = newOption.getTranslations() == null ? Collections.emptyMap()
                                                                               : newOption.getTranslations()
                                                                                          .entrySet()
                                                                                          .stream()
                                                                                          .filter(entry -> entry.getKey() != null
                                                                                                           && entry.getValue() != null)
                                                                                          .collect(Collectors.toMap(entry -> Locale.forLanguageTag(entry.getKey()),
                                                                                                                    Map.Entry::getValue));
        translationService.saveTranslationLabels(PROFILE_PROPERTY_OBJECT_TYPE,
                                                 savedOption.getId(),
                                                 PROFILE_PROPERTY_FIELD_NAME,
                                                 translations);
        labelsChanged = labelsChanged || !previousTranslations.equals(translations);
      } catch (Exception e) {
        LOG.error("Error while saving translation labels for profile property option {}", savedOption.getId(), e);
      }
    }
    return labelsChanged;
  }

  /**
   * Whether an update changed the string the profile index holds for the
   * property's values, other than through the option labels: the dropdown
   * switch turned off or on while the property has options (the index then
   * holds the raw option id, or the option again), an option removed, or an
   * option's value changed.
   */
  private boolean isOptionValueChanged(ProfilePropertySetting updatedPropertySetting,
                                       boolean wasDropdownList,
                                       Map<Long, String> previousOptionValues) {
    Map<Long, String> optionValues =
                                   getOptionValues(profileSettingStorage.getProfilePropertyOptions(updatedPropertySetting.getId(),
                                                                                                   0,
                                                                                                   0));
    if (wasDropdownList != updatedPropertySetting.isDropdownList()) {
      return !previousOptionValues.isEmpty() || !optionValues.isEmpty();
    } else if (!updatedPropertySetting.isDropdownList()) {
      return false;
    }
    return previousOptionValues.entrySet()
                               .stream()
                               .anyMatch(previous -> !optionValues.containsKey(previous.getKey())
                                                     || !Objects.equals(previous.getValue(), optionValues.get(previous.getKey())));
  }

  private Map<Long, String> getOptionValues(List<ProfilePropertyOption> options) {
    Map<Long, String> optionValues = new HashMap<>();
    if (options != null) {
      options.stream()
             .filter(option -> option != null && option.getId() != null)
             .forEach(option -> optionValues.put(option.getId(), option.getValue()));
    }
    return optionValues;
  }

  private boolean isPropertySettingHiddenable(ProfilePropertySetting propertySetting) {
    return propertySetting != null
           && !nonHiddenableProps.contains(propertySetting.getPropertyName())
           && !propertySetting.isHasChildProperties()
           && propertySetting.isHiddenbale();
  }

  private boolean isGroupSynchronizedEnabledProperty(ProfilePropertySetting profilePropertySetting) {
    if (profilePropertySetting == null
        || synchronizedGroupDisabledProperties.contains(profilePropertySetting.getPropertyName())) {
      return false;
    }
    if (profilePropertySetting.getParentId() != null && profilePropertySetting.getParentId() > 0) {
      ProfilePropertySetting parent = profileSettingStorage.getProfileSettingById(profilePropertySetting.getParentId());
      return parent == null || !synchronizedGroupDisabledProperties.contains(parent.getPropertyName());
    }
    return true;
  }

  public boolean isUserCardFieldSettings(String propertyName) {
    SettingValue<?> userCardFirstFieldSetting = settingService.get(Context.GLOBAL,
                                                                   new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                   "UserCardFirstFieldSetting");
    SettingValue<?> userCardSecondFieldSetting = settingService.get(Context.GLOBAL,
                                                                   new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                   "UserCardSecondFieldSetting");
    SettingValue<?> userCardThirdFieldSetting = settingService.get(Context.GLOBAL,
                                                                   new Scope(Scope.GLOBAL.getName(), USER_CARD_SETTINGS),
                                                                   "UserCardThirdFieldSetting");
    return Stream.of(userCardFirstFieldSetting, userCardSecondFieldSetting, userCardThirdFieldSetting)
                 .filter(Objects::nonNull)
                 .anyMatch(setting -> propertyName.equals(setting.getValue()));                                            
  }

}
