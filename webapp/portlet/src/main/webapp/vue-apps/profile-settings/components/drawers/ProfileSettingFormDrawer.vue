/*
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
 */


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
        class="form-horizontal pt-0 pb-4"
        flat>
        <v-card-text class="d-flex settingNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileSettings.label.name') }}<template v-if="newSetting">*</template>
        </v-card-text>
        <v-card-text class="d-flex settingNameField py-0">
          <input
            ref="settingNameInput"
            v-model="setting.propertyName"
            :disabled="saving || !newSetting"
            :autofocus="drawer"
            :placeholder="$t('profileSettings.placeholder.name')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex settingLabelsLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileSettings.label.labels') }}
        </v-card-text>
        <v-card-text class="d-flex settingNameField py-0">
          <profile-property-labels
            :propertylabels="labels"
            :languages="languages"
            :labels-object-type="labelsObjectType"
            :id="setting.id" />
        </v-card-text>

        <v-card-text class="d-flex parentLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileSettings.label.parent') }}
        </v-card-text>
        <v-card-text class="d-flex settingParentField py-0 pl-0 pr-7">
          <v-autocomplete
            ref="settingParentField"
            id="settingParentField" 
            v-model="setting.parentId"
            :items="parents"
            :placeholder="$t('profileSettings.placeholder.parent')"
            class="d-flex pa-4 ignore-vuetify-classes flex-grow-1"
            outlined
            dense
            width="100%"
            max-width="100%"
            item-text="resolvedLabel"
            item-value="id"
            @blur="blurAutocomplete()" />  
        </v-card-text>
        <v-list-item class="pt-4">
          <v-list-item-content transition="fade-transition" class="d-flex visibleLabel py-0">
            <v-list-item-title class="d-flex visibleLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
          <v-list-item-content transition="fade-transition" class="d-flex editableLabel py-0">
            <v-list-item-title class="d-flex editableLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
            <v-list-item-title class="d-flex requiredLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
        <v-list-item>
          <v-list-item-content transition="fade-transition" class="d-flex multiValuedField py-0">
            <v-list-item-title class="d-flex multiValuedLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
              :disabled="saving || setting.default"
              :ripple="false"
              color="primary"
              class="requiredSwitcher my-auto" />
          </v-list-item-action>
        </v-list-item>

        <v-list-item>
          <v-list-item-content transition="fade-transition" class="d-flex groupSynchronizedField py-0">
            <v-list-item-title class="d-flex groupSynchronizedLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
          <v-list-item-content transition="fade-transition" class="d-flex activeLabel py-0">
            <v-list-item-title class="d-flex activedLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
          <v-list-item-content
            transition="fade-transition"
            class="d-flex activeLabel py-0">
            <v-list-item-title
              class="d-flex activedLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
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
          :disabled="isSaveButtonDisabled || saving"
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
    languages: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    fieldError: false,
    drawer: false,
    newSetting: false,
    saving: false,
    confirmNewPassword: null,
    setting: {},
    parents: [],
    labels: [],
    changes: false,
    labelsObjectType: 'profileProperty',
    initialSetting: {},
    initialLabels: [],
    areLabelsChanged: false,
  }),
  computed: {
    unHiddenableSetting() {
      return this.unHiddenableProperties.includes(this.setting?.propertyName) || this.setting?.children?.length;
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
        return !this.areLabelsChanged && this.areSettingsEqual(this.initialSetting, this.setting);
      }
      return false;
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
        if (!areEqualsLabels) {
          this.areLabelsChanged = true;
        } else {
          this.areLabelsChanged = false;
        }
      },
    },
  },
  created() {
    this.$root.$on('open-settings-create-drawer', this.addNewSetting);
    this.$root.$on('open-settings-edit-drawer', this.editSetting);
    this.$root.$on('close-settings-form-drawer', this.cancel);
  },
  methods: {
    blurAutocomplete() {
      this.$refs.settingParentField.isFocused = false;
      this.$refs.settingParentField.isMenuActive = false;
    },
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileSettings.property.name.${item.propertyName}`)!==`profileSettings.property.name.${item.propertyName}`?this.$t(`profileSettings.property.name.${item.propertyName}`):item.propertyName;
    },
    resetCustomValidity() {
      this.$refs.settingNameInput.setCustomValidity('');
    },
    addNewSetting() {
      this.setting = {visible: true, editable: true, groupSynchronized: false, active: true, groupSynchronizationEnabled: true};
      this.labels = [{language: 'en', label: '', objectType: this.labelsObjectType}];
      this.parents = Object.assign([], this.settings);
      this.parents = this.parents.filter(setting => (setting.id !== this.setting.id && !setting.parentId) && (setting.children?.length || setting.multiValued));
      this.parents.forEach(setting => setting.resolvedLabel = this.getResolvedName(setting));
      this.newSetting = true;
      this.changes= false;
      this.drawer = true;
    },
    editSetting(setting) {
      this.initialSetting = {...setting};
      this.initialLabels = JSON.parse(JSON.stringify(setting.labels));
      this.setting = { ...setting};
      this.parents = Object.assign([], this.settings);
      this.parents = !(Array.isArray(this.setting?.children) && this.setting?.children.length) && this.parents.filter(setting => (setting.id !== this.setting.id && !setting.parentId) &&  (setting.children?.length || setting.multiValued)) || [];
      this.parents.forEach(setting => setting.resolvedLabel = this.getResolvedName(setting));
      this.parents.unshift({resolvedLabel: ''});
      this.newSetting = false;
      this.labels = JSON.parse(JSON.stringify(this.setting.labels));
      this.changes= false;
      this.drawer = true;     
    },
    saveSetting(event) {
      this.changes=true;
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.fieldError = false;
      this.resetCustomValidity();

      if (!this.$refs.settingForm.validate() // Vuetify rules
          || !this.$refs.settingForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.saving = true;
      if (this.newSetting){
        this.setting.labels= this.labels;
        this.$root.$emit('create-setting', this.setting);
      } else {
        this.mergeLabels();
        this.$root.$emit('update-setting', this.setting,true);
      }
      this.saving = false;
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
    },
    areSettingsEqual(initialSetting, setting) {
      const fields = ['id', 'parentId', 'active', 'groupSynchronized', 'multiValued', 'visible', 'required', 'editable', 'hiddenable'
      ];
      for (const field of fields) {
        if (field === 'parentId' && setting[field] === '') {
          setting[field] = null;
        }
        if (initialSetting[field] !== setting[field]) {
          return false;
        }
      }
      return true;
    }
  },
};
</script>
