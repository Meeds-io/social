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
  <exo-drawer
    id="userCardSettingsDrawer"
    ref="userCardSettingsDrawer"
    allow-expand
    right
    @closed="$emit('closed')">
    <template slot="title">
      <span class="text-color ma-auto">
        {{ $t('profileSettings.userCard.settings.label') }}
      </span>
    </template>
    <template slot="content">
      <div class="pa-5">
        <p>
          {{ $t('profileSettings.userCard.settings.customize.info') }}<br>
          <span>- {{ $t('profileSettings.peoplePage.label') }}</span><br>
          <span>- {{ $t('profileSettings.spaceMembers.label') }}</span><br>
          <span>- {{ $t('profileSettings.organizationalCharts.label') }}</span>
        </p>
        <people-user-card
          width="268"
          height="50"
          class="mb-2"
          :user="user"
          :user-navigation-extensions="userExtensions"
          :profile-action-extensions="profileActionExtensions"
          :preferences="{
            firstField: firstField,
            secondField: secondField,
            thirdField: thirdField
          }" />
        <label for="firstField">
          {{ $t('profileSettings.userCard.settings.firstField.label') }}
          <v-select
            ref="firstField"
            v-model="firstField"
            :items="textTypeSettings"
            item-text="label"
            item-value="value"
            name="firstField"
            class="pt-1"
            dense
            outlined
            @blur="$refs.firstField.blur();" />
        </label>
        <label for="secondField">
          {{ $t('profileSettings.userCard.settings.secondField.label') }}
          <v-select
            ref="secondField"
            v-model="secondField"
            :items="textTypeSettings"
            item-text="label"
            item-value="value"
            name="secondField"
            class="pt-1"
            dense
            outlined
            @blur="$refs.secondField.blur();" />
        </label>
        <label for="thirdField">
          {{ $t('profileSettings.userCard.settings.thirdField.label') }}
          <v-select
            ref="thirdField"
            v-model="thirdField"
            :items="textTypeSettings"
            item-text="label"
            item-value="value"
            name="thirdField"
            class="pt-1"
            dense
            outlined
            @blur="$refs.thirdField.blur();" />
        </label>
        <label
          v-if="phoneTypeSettings?.length"
          for="showPhoneOption"
          class="d-flex mt-7 align-center justify-space-between">
          <span class="font-weight-bold">
            {{ $t('profileSettings.userCard.settings.phoneOption.label') }}
          </span>
          <v-switch
            v-model="showPhoneOption"
            :disabled="isSavingSettings"
            :ripple="false"
            name="showPhoneOption"
            color="primary"
            class="ma-0 pt-0"
            hide-details />
        </label>
        <label
          v-if="showPhoneOption"
          for="phone"
          class="mt-2">
          {{ $t('profileSettings.userCard.settings.phone.label') }}
          <v-select
            ref="phone"
            v-model="selectedPhone"
            :items="phoneTypeSettings"
            :placeholder="$t('profileSettings.userCard.settings.attribute.select.label')"
            item-text="label"
            item-value="value"
            name="phone"
            class="pt-1"
            dense
            outlined
            @blur="$refs.thirdField.blur();" />
        </label>
      </div>
    </template>
    <template slot="footer">
      <div class="ma-auto d-flex width-full">
        <div class="ms-auto">
          <v-btn
            class="btn me-2"
            @click="close">
            {{ $t('profileSettings.button.cancel') }}
          </v-btn>
          <v-btn
            :loading="isSavingSettings"
            :disabled="!settingsUpdated"
            class="btn btn-primary"
            @click="saveSettings">
            {{ $t('profileSettings.button.save') }}
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      profileActionExtensions: [],
      userExtensions: [],
      preferences: {},
      firstField: 'position',
      secondField: 'team',
      thirdField: 'city',
      showPhoneOption: false,
      selectedPhone: null
    };
  },
  props: {
    user: {
      type: Object,
      default: null
    },
    settings: {
      type: Array,
      default: null
    },
    isSavingSettings: {
      type: Boolean,
      default: false
    },
    savedSettings: {
      type: Object,
      default: null
    },
  },
  computed: {
    settingsUpdated() {
      return this.firstField !== this.savedSettings?.firstField || this.secondField !==  this.savedSettings?.secondField
                                                                || this.thirdField !== this.savedSettings?.thirdField
                                                                || this.selectedPhone !== this.savedSettings?.displayedPhone
                                                                || this.showPhoneOption !== !!this.selectedPhone;
    },
    textTypeSettings() {
      return this.settings.filter(setting => setting.type === 'text');
    },
    phoneTypeSettings() {
      return this.settings.filter(setting => setting.type === 'call');
    }
  },
  watch: {
    savedSettings() {
      this.bindSavedSettings();
    }
  },
  created() {
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    document.addEventListener('extension-user-extension-navigation-updated', this.refreshUserExtensions);
    this.refreshExtensions();
    this.refreshUserExtensions();
  },
  methods: {
    bindSavedSettings() {
      this.firstField = this.savedSettings?.firstField || this.firstField;
      this.secondField = this.savedSettings?.secondField || this.secondField;
      this.thirdField = this.savedSettings?.thirdField || this.thirdField;
      this.selectedPhone = this.savedSettings?.displayedPhone || this.selectedPhone;
      this.showPhoneOption = !! this.selectedPhone;
    },
    refreshUserExtensions() {
      this.userExtensions = extensionRegistry.loadExtensions('user-extension', 'navigation') || [];
    },
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.profileActionExtensions.sort((elementOne, elementTwo) => (elementOne.order || 100) - (elementTwo.order || 100));
    },
    open() {
      this.bindSavedSettings();
      this.$refs.userCardSettingsDrawer.open();
    },
    close() {
      this.$refs.userCardSettingsDrawer.close();
    },
    saveSettings() {
      this.selectedPhone = this.showPhoneOption ? this.selectedPhone : null;
      this.$emit('save-settings', {
        firstField: this.firstField,
        secondField: this.secondField,
        thirdField: this.thirdField,
        displayedPhone: this.selectedPhone
      });
    }
  }
};
</script>
