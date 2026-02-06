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
        <optional-attribute-selector
          v-model="selectedPhone"
          :enabled.sync="showPhoneOption"
          :items="phoneTypeSettings"
          :disabled="isSavingSettings"
          :is-selected-active="isSavedPhoneAttributeActive"
          :tooltip-label="$t('profileSettings.userCard.settings.phoneOption.disabled')"
          :toggle-label="$t('profileSettings.userCard.settings.phoneOption.label')"
          :select-label="$t('profileSettings.userCard.settings.phone.label')"
          :placeholder="$t('profileSettings.userCard.settings.attribute.select.label')"
          name="phone" />
        <optional-attribute-selector
          v-model="selectedEmail"
          :enabled.sync="showEmailOption"
          :items="emailTypeSettings"
          :disabled="isSavingSettings"
          :is-selected-active="isSavedEmailAttributeActive"
          :tooltip-label="$t('profileSettings.userCard.settings.emailOption.disabled')"
          :toggle-label="$t('profileSettings.userCard.settings.emailOption.label')"
          :select-label="$t('profileSettings.userCard.settings.email.label')"
          :placeholder="$t('profileSettings.userCard.settings.attribute.select.label')"
          name="email" />
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
      selectedPhone: null,
      showEmailOption: false,
      selectedEmail: null
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
                                                                || (this.phoneUpdated && this.isSavedPhoneAttributeActive)
                                                                || (this.emailUpdated && this.isSavedEmailAttributeActive);
    },
    phoneUpdated() {
      const savedPhone = this.savedSettings?.displayedPhone ?? null;
      return (this.selectedPhone !== savedPhone || !!savedPhone !== !!this.showPhoneOption);
    },
    emailUpdated() {
      const savedEmail = this.savedSettings?.displayedEmail ??  null;
      return (this.selectedEmail !== savedEmail || !!savedEmail !== !!this.showEmailOption);
    },
    isSavedEmailAttributeActive() {
      return this.isOptionActive(this.savedSettings?.displayedEmail, this.emailTypeSettings);
    },
    isSavedPhoneAttributeActive() {
      return this.isOptionActive(this.savedSettings?.displayedPhone, this.phoneTypeSettings);
    },
    textTypeSettings() {
      return this.settings.filter(setting => setting.type === 'text');
    },
    phoneTypeSettings() {
      return this.settings.filter(setting => setting.type === 'call');
    },
    emailTypeSettings() {
      return this.settings.filter(setting => setting.type === 'email');
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
      ['firstField', 'secondField', 'thirdField'].forEach(field => {
        this[field] = this.savedSettings?.[field] ?? this[field];
      });

      this.selectedPhone = this.savedSettings?.displayedPhone ?? this.selectedPhone;
      this.showPhoneOption = !!this.selectedPhone && this.phoneTypeSettings?.length > 0
          && this.isOptionActive(this.selectedPhone, this.phoneTypeSettings);

      this.selectedEmail = this.savedSettings?.displayedEmail ?? this.selectedEmail;
      this.showEmailOption = !!this.selectedEmail && this.emailTypeSettings?.length > 0
          && this.isOptionActive(this.selectedEmail, this.emailTypeSettings);
    },
    isOptionActive(selected, options) {
      return options.some(option => option.value === selected && option.active);
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
      this.selectedEmail = this.showEmailOption ? this.selectedEmail : null;
      this.$emit('save-settings', {
        firstField: this.firstField,
        secondField: this.secondField,
        thirdField: this.thirdField,
        displayedPhone: this.selectedPhone,
        displayedEmail: this.selectedEmail,
      });
    }
  }
};
</script>
