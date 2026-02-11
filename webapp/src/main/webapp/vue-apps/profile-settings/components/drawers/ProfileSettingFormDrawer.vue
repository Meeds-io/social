<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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
 *
 -->
<template>
  <exo-drawer
    id="profileSettingFormDrawer"
    ref="profileSettingFormDrawer"
    right
    @closed="cancel">
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-form
        ref="settingForm"
        v-model="valid"
        lazy-validation
        class="form-horizontal pt-0 pb-4">
        <div class="py-4">
          <label
            for="settingNameInput"
            class="mx-4">
            {{ $t('profileSettings.label.name') }}
            <v-text-field
              ref="settingNameInput"
              v-model="setting.propertyName"
              :disabled="saving || !newSetting"
              :autofocus="drawer"
              name="settingNameInput"
              class="pt-3"
              :placeholder="$t('profileSettings.placeholder.name')"
              maxlength="2000"
              outlined
              dense
              :rules="[v => !!v || $t('profileSettings.message.field.required')]" />
          </label>
          <label
            for="labelsInput"
            class="mx-4 mt-2">
            {{ $t('profileSettings.label.labels') }}
            <profile-property-labels
              :propertylabels="labels"
              :languages="languages"
              :labels-object-type="labelsObjectType"
              :id="setting.id"
              name="labelsInput" />
          </label>
          <label
            for="propertyType"
            class="mx-4">
            {{ $t('profileSettings.label.propertyType') }}
            <v-tooltip
              bottom
              :disabled="(newSetting && !isDropdownList) || (!newSetting && !isUserType)">
              <template #activator="{ on, attrs }">
                <div
                  v-bind="attrs"
                  v-on="on">
                  <v-select
                    ref="propertyType"
                    v-model="setting.propertyType"
                    :items="propertyTypes"
                    :disabled="!newSetting && (isDropdownList || isUserType)"
                    :placeholder="!setting?.propertyType && $t('profileSettings.placeholder.propertyType')"
                    :rules="[v => !!v || $t('profileSettings.message.field.required')]"
                    item-disabled="disabled"
                    name="propertyType"
                    class="pt-3"
                    item-text="label"
                    item-value="value"
                    dense
                    outlined
                    @blur="$refs.propertyType.blur();" />
                </div>
              </template>
              <span v-if="isDropdownList && newSetting">
                {{ $t('profileSettings.dropdownList.disabled.propertyType.info') }}
              </span>
              <span v-else-if="!newSetting && isUserType">
                {{ $t('profileSettings.propertyType.disabled.label') }}
              </span>
            </v-tooltip>
          </label>
          <v-list-item class="mt-4">
            <v-list-item-content transition="fade-transition" class="d-flex activeLabel py-0">
              <v-list-item-title class="d-flex activedLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.active') }}
                </div>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <v-switch
                v-model="setting.active"
                :disabled="saving"
                :ripple="false"
                color="primary"
                class="activeSwitcher my-auto" />
            </v-list-item-action>
          </v-list-item>
          <v-list-item>
            <v-list-item-content transition="fade-transition" class="d-flex visibleLabel py-0">
              <v-list-item-title class="d-flex visibleLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.visible') }}
                </div>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <v-switch
                v-model="setting.visible"
                :disabled="saving"
                :ripple="false"
                color="primary"
                class="visibleSwitcher my-auto" />
            </v-list-item-action>
          </v-list-item>
          <v-list-item>
            <v-list-item-content
              transition="fade-transition"
              class="d-flex activeLabel py-0">
              <v-list-item-title
                class="d-flex activedLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.hiddenable') }}
                </div>
              </v-list-item-title>
              <v-list-item-subtitle
                class="mt-n3">
                <span
                  v-if="setting.hiddenable"
                  class="caption">
                  {{ $t('profileSettings.label.hiddenable.enabled') }}
                </span>
                <span
                  v-else
                  class="caption">
                  {{ $t('profileSettings.label.hiddenable.disabled') }}
                </span>
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-tooltip
                bottom
                :disabled="!unHiddenableSetting">
                <template #activator="{ on, attrs }">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    <v-switch
                      v-model="setting.hiddenable"
                      :disabled="saving || unHiddenableSetting"
                      :alt="setting.hiddenable && $t('profileSettings.show.property.alt')
                        || $t('profileSettings.hide.property.alt')"
                      :ripple="false"
                      color="primary"
                      :aria-labelledBy="$t('profileSettings.label.hiddenable')"
                      class="activeSwitcher my-auto" />
                  </div>
                </template>
                <span v-if="setting?.children?.length">
                  {{ $t('profileSettings.hiddenable.parentProperty.disabled') }}
                </span>
                <span v-else>
                  {{ $t('profileSettings.unHiddenable.property.tooltip') }}
                </span>
              </v-tooltip>
            </v-list-item-action>
          </v-list-item>
          <v-list-item>
            <v-list-item-content transition="fade-transition" class="d-flex editableLabel py-0">
              <v-list-item-title class="d-flex editableLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.editable') }}
                </div>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <v-switch
                v-model="setting.editable"
                :disabled="saving"
                :ripple="false"
                color="primary"
                class="editableSwitcher my-auto" />
            </v-list-item-action>
          </v-list-item>
          <v-list-item>
            <v-list-item-content transition="fade-transition" class="d-flex requiredField py-0">
              <v-list-item-title class="d-flex requiredLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.required') }}
                </div>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <v-switch
                v-model="setting.required"
                :disabled="saving"
                :ripple="false"
                color="primary"
                class="requiredSwitcher my-auto" />
            </v-list-item-action>
          </v-list-item>
          <v-tooltip
            bottom
            :disabled="!isUserType">
            <template #activator="{ on, attrs }">
              <div
                v-bind="attrs"
                v-on="on">
                <v-list-item>
                  <v-list-item-content
                    :class="{'text--disabled': isUserType}"
                    transition="fade-transition"
                    class="d-flex visibleLabel py-0">
                    <v-list-item-title class="d-flex visibleLabel flex-grow-1 text-no-wrap pb-2">
                      <div>
                        {{ $t('profileSettings.label.dropdownList') }}
                      </div>
                    </v-list-item-title>
                    <v-list-item-subtitle
                      class="mt-n3">
                      <span
                        :class="{'text--disabled': isUserType}"
                        class="caption">
                        {{ $t('profileSettings.dropdownList.info') }}
                      </span>
                    </v-list-item-subtitle>
                  </v-list-item-content>
                  <v-list-item-action :class="{'my-0': isDropdownList}">
                    <div
                      class="d-flex">
                      <v-btn
                        v-if="isDropdownList"
                        class="my-auto me-2 pa-0"
                        icon
                        @click="openDropdownListDrawer">
                        <v-icon
                          size="20"
                          class="icon-default-color">
                          fas fa-edit
                        </v-icon>
                      </v-btn>
                      <v-switch
                        v-model="setting.dropdownList"
                        :disabled="saving || isUserType"
                        :ripple="false"
                        color="primary"
                        class="align-center my-auto"
                        @change="openDropdownListDrawerOnSwitch" />
                    </div>
                  </v-list-item-action>
                </v-list-item>
              </div>
            </template>
            <span v-if="isUserType">
              {{ $t('profileSettings.dropdownList.available.info') }}
            </span>
          </v-tooltip>
          <v-list-item>
            <v-list-item-content transition="fade-transition" class="d-flex multiValuedField py-0">
              <v-list-item-title class="d-flex multiValuedLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.multiValued') }}
                </div>
              </v-list-item-title>
              <v-list-item-subtitle v-if="setting.default" class="mt-n3">
                <span class="caption"> {{ $t('profileSettings.label.attribute.canNotEdit') }} </span>
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-switch
                v-model="setting.multiValued"
                :disabled="saving || setting.default || setting.userCardFieldSettings"
                :ripple="false"
                color="primary"
                class="requiredSwitcher my-auto" />
            </v-list-item-action>
          </v-list-item>
          <v-list-item>
            <v-list-item-content transition="fade-transition" class="d-flex groupSynchronizedField py-0">
              <v-list-item-title class="d-flex groupSynchronizedLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.groupSynchronized') }}
                </div>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <v-switch
                v-model="setting.groupSynchronized"
                :disabled="saving || !setting.groupSynchronizationEnabled"
                :ripple="false"
                color="primary"
                class="groupSynchronizedSwitcher my-auto" />
            </v-list-item-action>
          </v-list-item>
          <v-list-item>
            <v-list-item-content
              transition="fade-transition"
              class="d-flex activeLabel py-0">
              <v-list-item-title
                class="d-flex activedLabel flex-grow-1 text-no-wrap pb-2">
                <div>
                  {{ $t('profileSettings.label.index.in.analytics') }}
                </div>
              </v-list-item-title>
              <v-list-item-subtitle
                class="mt-n3">
                <span
                  class="caption">
                  {{ $t('profileSettings.label.index.in.analytics.info') }}
                </span>
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-tooltip
                bottom
                :disabled="!excludedAnalyticsIndexSetting">
                <template #activator="{ on, attrs }">
                  <div
                    v-bind="attrs"
                    v-on="on">
                    <v-switch
                      v-model="setting.indexInAnalytics"
                      :disabled="saving || excludedAnalyticsIndexSetting"
                      :alt="$t('profileSettings.label.index.in.analytics')
                        || $t('profileSettings.hide.property.alt')"
                      :ripple="false"
                      color="primary"
                      :aria-labelledBy="$t('profileSettings.label.index.in.analytics')"
                      class="activeSwitcher my-auto" />
                  </div>
                </template>
                {{ $t('profileSettings.label.excluded.index.in.analytics.info') }}
              </v-tooltip>
            </v-list-item-action>
          </v-list-item>
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="cancel">
          {{ $t('profileSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="isSaveButtonDisabled || saving || !valid"
          :loading="saving"
          class="btn btn-primary"
          @click="saveSetting">
          {{ $t('profileSettings.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  props: {
    settings: {
      type: Object,
      default: null
    },
    unHiddenableProperties: {
      type: Array,
      default: () => []
    },
    excludedAnalyticsIndexProperties: {
      type: Array,
      default: () => []
    },
    languages: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    valid: false,
    fieldError: false,
    drawer: false,
    newSetting: false,
    saving: false,
    confirmNewPassword: null,
    setting: {},
    labels: [],
    changes: false,
    labelsObjectType: 'profileProperty',
    initialSetting: {},
    initialLabels: [],
    areLabelsChanged: false,
    translationsUpdated: false
  }),
  computed: {
    propertyTypes () {
      return !this.isDropdownList && [
        {label: this?.$t('profileSettings.label.text.propertyType'), value: 'text'},
        {
          label: this?.$t('profileSettings.label.user.propertyType'),
          value: 'user',
          disabled: !this.newSetting && !this.isUserType
        },
        {label: this?.$t('profileSettings.label.call.propertyType'), value: 'call'},
        {label: this?.$t('profileSettings.label.messaging.propertyType'), value: 'messaging'},
        {label: this?.$t('profileSettings.label.email.propertyType'), value: 'email'}
      ] || [
        {label: this?.$t('profileSettings.label.text.propertyType'), value: 'text'}];
    },
    unHiddenableSetting() {
      return this.unHiddenableProperties.includes(this.setting?.propertyName) || this.setting?.children?.length;
    },
    excludedAnalyticsIndexSetting() {
      return this.excludedAnalyticsIndexProperties.includes(this.setting?.propertyName) || this.setting?.children?.length;
    },
    title() {
      if (this.newSetting) {
        return this.$t('profileSettings.drawer.title.addSetting');
      } else {
        return this.$t('profileSettings.drawer.title.editSetting');
      }
    },
    isSaveButtonDisabled() {
      if (!this.newSetting) {
        return !this.areLabelsChanged && !this.translationsUpdated && this.areSettingsEqual(this.initialSetting, this.setting) ;
      }
      return false;
    },
    isDropdownList() {
      return this.setting?.dropdownList;
    },
    isUserType() {
      return this.setting.propertyType === 'user';
    }
  },
  watch: {
    saving() {
      if (this.saving) {
        this.$refs.profileSettingFormDrawer.startLoading();
      } else {
        this.$refs.profileSettingFormDrawer.endLoading();
      }
    },
    drawer() {
      if (this.drawer) {
        this.translationsUpdated = false;
        this.$refs.profileSettingFormDrawer.open();
      } else {
        this.$refs.profileSettingFormDrawer.close();
      }
    },
    labels: {
      immediate: true,
      deep: true,
      handler(newItems) {
        const areEqualsLabels = this.initialLabels.length === newItems.length && this.initialLabels.every((item, index) => {
          return item.id === newItems[index].id && item.label === newItems[index].label && item.language === newItems[index].language;
        });
        this.areLabelsChanged = !areEqualsLabels;
      },
    },
    'setting.dropdownList': function () {
      if (this.isDropdownList) {
        this.setting.propertyType = this.propertyTypes[0];
      }
    }
  },
  created() {
    this.$root.$on('open-settings-create-drawer', this.addNewSetting);
    this.$root.$on('open-settings-edit-drawer', this.editSetting);
    this.$root.$on('close-settings-form-drawer', this.cancel);
    this.$root.$on('setting-translation-updated', this.settingTranslationUpdated);
    this.$root.$on('setting-updated', this.handleSettingUpdated);
  },
  methods: {
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileSettings.property.name.${item.propertyName}`)!==`profileSettings.property.name.${item.propertyName}`?this.$t(`profileSettings.property.name.${item.propertyName}`):item.propertyName;
    },
    addNewSetting() {
      this.setting = {visible: true, editable: true, groupSynchronized: false, active: true, groupSynchronizationEnabled: true};
      this.labels = [{language: 'en', label: '', objectType: this.labelsObjectType}];
      this.newSetting = true;
      this.changes= false;
      this.drawer = true;
    },
    editSetting(setting) {
      this.initialSetting = structuredClone(setting);
      this.initialLabels = JSON.parse(JSON.stringify(setting.labels));
      this.setting = { ...setting};
      this.setting.propertyType = this.setting.propertyType || this.propertyTypes[0];
      this.newSetting = false;
      this.labels = JSON.parse(JSON.stringify(this.setting.labels));
      this.changes= false;
      this.drawer = true;     
    },
    handleSettingUpdated(setting) {
      this.initialSetting = structuredClone(setting);
      this.areLabelsChanged = false;
    },
    saveSetting(event) {
      this.changes=true;
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.fieldError = false;

      if (!this.$refs.settingForm.validate() // Vuetify rules
          || !this.$refs.settingForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }
      this.setting.propertyType = this.setting.propertyType?.value ?? this.setting.propertyType;
      this.saving = true;
      if (this.newSetting) {
        this.setting.labels = this.labels;
        this.$root.$emit('create-setting', this.setting);
      } else {
        this.mergeLabels();
        this.$root.$emit('update-setting', this.setting,true);
      }
      this.saving = false;
      this.translationsUpdated = false;
    },
    mergeLabels() {
      const labelstoCreate = [];
      const labelstoUpdate = [];
      const labelstoDelete = [];
      if (this.labels.length ===0 && !this.setting.labels.length ===0) {
        this.$root.$emit('delete-labels', this.setting.labels);
      } else if (!this.labels.length ===0 && this.setting.labels.length ===0) {
        this.$root.$emit('create-labels', this.labels);
      } else {
        this.setting.labels.forEach(label => {
          const foundProfileLabel = this.containsLabel(this.labels, label);
          if (foundProfileLabel){
            if (foundProfileLabel.label!==label.label || foundProfileLabel.language!==label.language) {
              labelstoUpdate.push(foundProfileLabel);
            }
          } else {
            labelstoDelete.push(label);
          }
        });
        this.labels.forEach(label => {
          if (!label.id) {
            labelstoCreate.push(label);
          }
        });
        if (labelstoCreate.length>0){
          this.$root.$emit('create-labels', labelstoCreate);
        }
        if (labelstoUpdate.length>0){
          this.$root.$emit('update-labels', labelstoUpdate);
        }
        if (labelstoDelete.length>0){
          this.$root.$emit('delete-labels', labelstoDelete);
        }
        this.setting.labels=this.labels;
      }   
    },
    containsLabel(labelsList, label){
      return labelsList.find((profileLabel) => profileLabel.id === label.id);  
    },
    cancel() {
      this.drawer = false;
      if (!this.changes){
        this.$root.$emit('cancel-edit-add');
        this.changes= false;
      }
      this.$refs.settingForm?.resetValidation();
    },
    arePropertyOptionsEqual(options1, options2) {
      if (!options1 || !options2) {
        return false;
      }
      if (options1?.length !== options2?.length) {
        return false;
      }
      for (let i = 0; i < options1.length; i++) {
        if (options1[i].value !== options2[i].value) {
          return false;
        }
      }
      return true;
    },
    areSettingsEqual(initialSetting, setting) {
      const fields = ['id', 'active', 'groupSynchronized', 'multiValued', 'propertyOptions',
        'dropdownList', 'visible', 'propertyType', 'required', 'editable', 'hiddenable', 'indexInAnalytics'
      ];
      for (const field of fields) {
        if (field === 'propertyOptions') {
          if (!this.arePropertyOptionsEqual(initialSetting?.[field], setting?.[field])) {
            return false;
          }
        } else if (initialSetting[field] !== setting[field]) {
          return false;
        }
      }
      return true;
    },
    openDropdownListDrawer() {
      this.$emit('open-dropdown-list', this.setting);
    },
    openDropdownListDrawerOnSwitch() {
      if (this.isDropdownList) {
        this.openDropdownListDrawer();
      }
    },
    settingTranslationUpdated(translationsUpdated) {
      this.translationsUpdated = translationsUpdated;
    }
  },
};
</script>
