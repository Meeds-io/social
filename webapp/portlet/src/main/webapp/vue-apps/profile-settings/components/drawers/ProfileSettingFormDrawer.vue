<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <exo-drawer
    id="profileSettingFormDrawer"
    ref="profileSettingFormDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      {{ title }}
    </template>
    <template slot="content">
      <v-form
        ref="settingForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="saveSetting">
        <v-card-text v-if="error" class="errorMessage">
          <v-alert type="error">
            {{ error }}
          </v-alert>
        </v-card-text>

        <v-card-text class="d-flex settingNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileSettings.label.name') }}<template v-if="newSetting">*</template>
        </v-card-text>
        <v-card-text class="d-flex settingNameField py-0">
          <input
            ref="settingNameInput"
            v-model="setting.propertyName"
            :disabled="saving || !newSetting"
            :autofocus="drawer"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>

        <v-card-text class="d-flex parentLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileSettings.label.parent') }}*
        </v-card-text>
        <v-card-text class="d-flex parentField py-0">
          <input
            ref="parentInput"
            v-model="setting.parentId"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            >
        </v-card-text>

        <v-list-item class="pt-4">
      <v-list-item-content transition="fade-transition" class="d-flex visibleLabel py-0">
        <v-list-item-title class="d-flex visibleLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          <div>
            {{ $t('profileSettings.label.visible') }}*
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
            {{ $t('profileSettings.label.editable') }}*
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

        <v-card-text class="d-flex ldapAttributeNameLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          {{ $t('profileSettings.label.ldapAttributeName') }}*
        </v-card-text>
        <v-card-text class="d-flex ldapAttributeNameField py-0">
          <input
            ref="ldapAttributeNameInput"
            v-model="setting.ldapAttribute"
            :disabled="saving"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000">
        </v-card-text>

        <v-list-item class="pt-4">
      <v-list-item-content transition="fade-transition" class="d-flex groupSynchronizedField py-0">
        <v-list-item-title class="d-flex groupSynchronizedLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          <div>
            {{ $t('profileSettings.label.groupSynchronized') }}*
          </div>
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
                v-model="setting.groupSynchronized"
                :disabled="saving"
                :ripple="false"
                color="primary"
                class="groupSynchronizedSwitcher my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content transition="fade-transition" class="d-flex activeLabel py-0">
        <v-list-item-title class="d-flex activedLabel flex-grow-1 text-no-wrap text-left font-weight-bold pb-2">
          <div>
            {{ $t('profileSettings.label.active') }}*
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
          :disabled="saving"
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
  data: () => ({
    error: null,
    fieldError: false,
    drawer: false,
    newSetting: false,
    saving: false,
    confirmNewPassword: null,
    setting: {},
  }),
  computed: {
    title() {
      if (this.newSetting) {
        return this.$t('profileSettings.drawer.title.addSetting');
      } else {
        return this.$t('profileSettings.drawer.title.editSetting');
      }
    },
  },
  watch: {
    confirmNewPassword() {
      this.resetCustomValidity();
    },
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
  },
  created() {
    this.$root.$on('open-settings-create-drawer', this.addNewSetting);
    this.$root.$on('open-settings-edit-drawer', this.editSetting);
    this.$root.$on('close-settings-form-drawer', this.cancel);
  },
  methods: {
    resetCustomValidity() {
      this.$refs.settingNameInput.setCustomValidity('');
    },
    addNewSetting() {
      this.setting = {};
      this.newSetting = true;
      this.drawer = true;
    },
    editSetting(setting) {
      this.setting = Object.assign({}, setting);
      this.newSetting = false;
      this.drawer = true;
    },
    saveSetting(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.error = null;
      this.fieldError = false;
      this.resetCustomValidity();

      if (!this.$refs.settingForm.validate() // Vuetify rules
          || !this.$refs.settingForm.$el.reportValidity()) { // Standard HTML rules
        return;
      }

      this.saving = true;
      if (this.newSetting){
        this.$root.$emit('create-setting', this.setting);
      } else {
        this.$root.$emit('update-setting', this.setting,true);
      }
      this.saving = false;
      //this.drawer = false;
    },
    cancel() {
      this.drawer = false;
    },
  },
};
</script>
