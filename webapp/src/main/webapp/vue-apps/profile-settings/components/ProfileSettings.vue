<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <v-app>
    <v-main>
      <v-card
        class="px-4 mb-12 mb-sm-0 application-body"
        flat>
        <v-list
          v-if="mainPageSelected">
          <profile-settings-option
            :option-label="$t('profileSettings.label.userCard.settings')"
            :description="$t('profileSettings.label.userCard.settings.info')"
            :action-icon="'fas fa-edit'"
            :icon-size="22"
            @action="openUserCardSettings" />
          <profile-settings-option
            :option-label="$t('profileSettings.label.profile.attributes.settings')"
            :description="$t('profileSettings.label.profile.attributes.settings.info')"
            :action-icon="'fa-caret-right'"
            @action="selectedOption = '#attributesettings'" />
        </v-list>
        <profile-attribute-settings
          v-if="attributeSettingsSelected"
          :languages="languages"
          :settings="settings"
          :un-hiddenable-properties="unHiddenableProperties"
          @back-to-main-page="setMainPageSelected" />
      </v-card>
    </v-main>
    <confirm-dialog
      ref="dialog"
      :title="dialogTitle"
      :message="dialogMessage"
      :ok-label="dialogOkLabel"
      :cancel-label="dialogCancelLabel"
      @ok="onDialogOk"
      @closed="confirmDialogClosed" />
    <user-card-settings-drawer
      ref="userCardSettings"
      :user="user"
      :settings="userCardFilteredFieldSettings"
      :is-saving-settings="isSavingCardSettings"
      :saved-settings="savedCardSettings"
      @closed="setMainPageSelected"
      @save-settings="saveUserCardSettings" />
    <profile-setting-form-drawer
      ref="settingFormDrawer"
      :settings="settings"
      :languages="languagesData"
      :un-hiddenable-properties="unHiddenableProperties"
      :excluded-analytics-index-properties="excludedAnalyticsIndexProperties"
      @open-dropdown-list="openDropdownListDrawer" />
    <dropdown-list-values-drawer
      ref="dropdownListDrawer" />
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      user: null,
      selectedOption: window.location.hash,
      settings: [],
      unHiddenableProperties: [],
      excludedAnalyticsIndexProperties: [],
      fieldsToRetrieve: 'settings',
      excludedSettingsProp: [],
      userCardSettingsContextKey: 'GLOBAL',
      userCardSettingScopeKey: 'GLOBAL',
      userCardFirstFieldSettingKey: 'UserCardFirstFieldSetting',
      userCardSecondFieldSettingKey: 'UserCardSecondFieldSetting',
      userCardThirdFieldSettingKey: 'UserCardThirdFieldSetting',
      userPhonePropertyKey: 'UserDisplayedPhonePropertySetting',
      isSavingCardSettings: false,
      savedCardSettings: null,
      settingOptionObjectType: 'propertySettingOption',
      settingOptionFieldName: 'optionValue',
      dialogTitle: null,
      dialogMessage: null,
      dialogOkLabel: null,
      dialogCancelLabel: null,
      dialogCallback: null
    };
  },
  props: {
    languages: {
      type: Array,
      default: null
    }
  },
  watch: {
    selectedOption() {
      if (this.selectedOption) {
        window.location.hash = this.selectedOption;
        this.openUserCardSettingsDrawer();
      } else {
        this.setCleanUri();
      }
    }
  },
  created() {
    this.getCurrentUser();
    this.getSettings();
    this.getSavedUserCardSettings();
    this.$root.$on('update-setting', this.editSetting);
    this.$root.$on('create-setting', this.createSetting);
    this.$root.$on('update-labels', this.updateLabels);
    this.$root.$on('create-labels', this.createLabels);
    this.$root.$on('delete-labels', this.deleteLabels);
    this.$root.$on('move-up-setting', this.moveUpSetting);
    this.$root.$on('move-down-setting', this.moveDownSetting);
    this.$root.$on('cancel-edit-add', this.displayNoChangeWarning);
    this.$root.$on('open-confirm-dialog', this.openConfirmDialog);
    window.addEventListener('popstate', this.updateSelected);
    setTimeout(() => {
      this.openUserCardSettingsDrawer();
    }, 500);
  },
  computed: {
    languagesData() {
      return [...this.languages].sort((a, b) => a.value.localeCompare(b.value));
    },
    attributeSettingsSelected() {
      return this.selectedOption === '#attributesettings';
    },
    userCardSettingsSelected() {
      return this.selectedOption === '#cardsettings';
    },
    mainPageSelected() {
      return !this.selectedOption || this.userCardSettingsSelected;
    },
    userCardFilteredFieldSettings() {
      return this.settings.filter(setting => !setting.multiValued && setting.propertyType !== 'user'
                                                                  && !setting?.children?.length
                                                                  && !this.unHiddenableProperties?.includes(setting.propertyName))
        .map(setting => {
          return {label: this.getResolvedName(setting), value: setting.propertyName, type: setting.propertyType};
        });
    }
  },
  methods: {
    getCurrentUser() {
      return this.$identityService.getIdentityById(eXo?.env?.portal?.userIdentityId, this.fieldsToRetrieve)
        .then(user => {
          const profile = user?.profile || {};
          const dataEntity = profile?.dataEntity || {};

          this.user = {
            ...profile,
            enabled: dataEntity.enabled,
            external: dataEntity.external,
            deleted: dataEntity.deleted,
          };
        });

    },
    setMainPageSelected() {
      this.selectedOption = null;
    },
    updateSelected() {
      this.selectedOption =  window.location.hash;
    },
    openUserCardSettings() {
      this.selectedOption = '#cardsettings';
      this.openUserCardSettingsDrawer();
    },
    openUserCardSettingsDrawer() {
      if (this.userCardSettingsSelected) {
        this.$refs.userCardSettings.open();
      }
    },
    getSettings() {
      return this.$profileSettingsService.getSettings()
        .then(settings => {
          this.settings = settings?.settings || [];
          this.unHiddenableProperties = settings?.unHiddenableProperties;
          this.excludedSettingsProp = settings?.excludedQuickSearchProperties;
          this.excludedAnalyticsIndexProperties = settings?.excludedAnalyticsIndexProperties;
        });
    },
    editSetting(setting, refresh) {
      this.$profileSettingsService.updateSetting(setting).then(() => {
        if (refresh) {
          this.getSettings();
        }
        this.$root.$emit('setting-updated', setting);
        this.$root.$emit('alert-message', this.$t('profileSettings.update.success.message'), 'success');
      }).catch(e => {
        console.error(e);
        this.$root.$emit('alert-message', this.$t(e.message), 'error');
      });
    },
    createSetting(setting) {
      this.$profileSettingsService.addSetting(setting).then(storedSetting => {
        const promises = [];
        if (setting?.labels?.length) {
          setting.labels.forEach(label => {
            label.objectId = storedSetting.id;
          });
          promises.push(this.$profileLabelService.addLabels(setting.labels));
        }
        return Promise.all(promises);
      })
        .then(() => {
          this.$root.$emit('close-settings-form-drawer');
          this.getSettings();
          this.$root.$emit(
            'alert-message',
            this.$t('profileSettings.create.success.message'),
            'success'
          );
        })
        .catch(error => {
          console.error(error);
          this.$root.$emit(
            'alert-message',
            this.$t(error.message || 'profileSettings.create.error.message'),
            'error'
          );
        });
    },
    updateLabels(labels) {
      this.$profileLabelService.updateLabels(labels);
    },
    createLabels(labels) {
      this.$profileLabelService.addLabels(labels);
    },
    deleteLabels(labels) {
      this.$profileLabelService.deleteLabels(labels);
    },
    moveUpSetting(setting) {
      this.moveSetting(setting, 'up');
    },
    moveDownSetting(setting) {
      this.moveSetting(setting, 'down');
    },
    displayNoChangeWarning() {
      this.$root.$emit('alert-message', this.$t('profileSettings.nochange.warning.message'), 'warning');
    },
    moveSetting(setting, direction) {
      this.settings.sort((s1, s2) => ((s1.order > s2.order) ? 1 : (s1.order < s2.order) ? -1 : 0));
      const index = this.settings.findIndex(object => {
        return object.id === setting.id;
      });
      if (index > 0 && direction === 'up'){
        const order = this.settings[index].order;
        this.settings[index].order=this.settings[index-1].order;
        this.settings[index-1].order = order;
        this.editSetting(this.settings[index],false);
        this.editSetting(this.settings[index-1],true);
      }
      if (index < this.settings.length && direction === 'down'){
        const order = this.settings[index].order;
        this.settings[index].order=this.settings[index+1].order;
        this.settings[index+1].order = order;
        this.editSetting(this.settings[index],false);
        this.editSetting(this.settings[index+1],true);
      }
    },
    setCleanUri() {
      const uri = window.location.toString();
      const cleanUri = uri.substring(0, uri.indexOf('#'));
      window.history.replaceState({}, document.title, cleanUri);
    },
    getResolvedName(property) {
      const lang = eXo?.env.portal.language || 'en';
      const resolvedLabel = !property.labels ? null : property.labels.find(v => v.language === lang);
      if (resolvedLabel) {
        return resolvedLabel.label;
      }
      return this.$t && this.$te(`profileSettings.property.name.${property.propertyName}`)
          && this.$t(`profileSettings.property.name.${property.propertyName}`)
          || property.propertyName;
    },
    saveCardSetting(settingKey, settingValue) {
      return this.$settingService.setSettingValue(this.userCardSettingsContextKey, '',
        this.userCardSettingScopeKey, 'UserCardSettings', settingKey, settingValue);
    },
    getSavedUserCardSettings() {
      return this.$userService.getUserCardSettings().then(userCardSettings => this.savedCardSettings = userCardSettings);
    },
    saveUserCardSettings(settings) {
      this.isSavingCardSettings = true;
      return this.saveCardSetting(this.userCardFirstFieldSettingKey, settings.firstField)
        .then(() => this.saveCardSetting(this.userCardSecondFieldSettingKey, settings.secondField))
        .then(() => this.saveCardSetting(this.userCardThirdFieldSettingKey, settings.thirdField))
        .then(() => this.saveCardSetting(this.userPhonePropertyKey, settings.displayedPhone))
        .then(() => {
          this.savedCardSettings = {
            firstField: settings.firstField,
            secondField: settings.secondField,
            thirdField: settings.thirdField,
            displayedPhone: settings.displayedPhone
          };
          this.$root.$emit(
            'alert-message',
            this.$t('profileSettings.userCard.settings.saved.success'),
            'success'
          );
          this.getSettings();
          this.$refs.userCardSettings.close();
        })
        .catch(() => {
          this.$root.$emit(
            'alert-message',
            this.$t('profileSettings.userCard.settings.saved.error'),
            'error'
          );
        })
        .finally(() => {
          this.isSavingCardSettings = false;
        });
    },
    openDropdownListDrawer(setting) {
      this.$refs.dropdownListDrawer.open(setting);
    },
    confirmDialogClosed() {
      this.dialogCallback = null;
    },
    openConfirmDialog(setting) {
      this.dialogTitle = setting.title;
      this.dialogMessage = setting.message;
      this.dialogOkLabel = setting.okLabel;
      this.dialogCancelLabel = setting.cancelLabel;
      this.dialogCallback = setting.callback;
      this.$refs.dialog.open();
    },
    onDialogOk() {
      this.dialogCallback?.();
    }
  }
};
</script>
