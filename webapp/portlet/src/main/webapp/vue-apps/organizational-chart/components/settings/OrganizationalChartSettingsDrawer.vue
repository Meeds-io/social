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
    ref="organizationalChartSettingsDrawer"
    allow-expand
    right
    @expand-updated="expanded = $event">
    <template slot="title">
      <span class="text-color ma-auto">
        {{ $t('organizationalChart.settings.title') }}
      </span>
    </template>
    <template slot="content">
      <div class="d-flex flex-wrap pa-4">
        <v-sheet
          class="mb-2"
          :class="expanded && 'me-6'"
          :width="expanded && '40%' || '100%'"
          :min-width="expanded && '500'">
          <label
            for="chartCenterUser">
            <span class="subtitle-2">
              {{ $t('organizationalChart.settings.centerUser.label') }}
            </span>
            <p class="caption grey--text">
              {{ $t('organizationalChart.settings.centerUser.info') }}
            </p>
            <v-radio-group
              name="chartCenterUser"
              v-model="chartCenterUser">
              <v-radio
                :label="$t('organizationalChart.settings.connectedUser.label')"
                :value="connectedUserOption" />
              <v-radio
                :label="$t('organizationalChart.settings.specificUser.label')"
                :value="specificUserOption" />
            </v-radio-group>
            <exo-identity-suggester
              v-if="specificUser && !user && !selectedUserData"
              ref="chartCenterUserSuggester"
              v-model="user"
              :labels="{
                searchPlaceholder: this.$t('organizationalChart.search.placeholder'),
                placeholder: this.$t(`organizationalChart.search.user.placeholder`),
                noDataLabel: this.$t('Search.noResults'),
              }"
              :ignore-items="listIgnoredItems"
              include-users
              :search-options="{}"
              @input="selectUser" />
            <v-chip
              v-if="showSelected"
              class="primary white--text"
              close
              @click:close="removeSelectedUser">
              <v-avatar left>
                <v-img :src="avatarUrl" />
              </v-avatar>
              {{ userFullName }}
            </v-chip>
          </label>
          <v-card
            class="grey pa-1 mt-2 lighten-2 overflow-hidden"
            height="306"
            outlined>
            <organizational-chart-app
              v-if="initialUserId"
              class="white position-relative chartPreview"
              :initial-user-id="initialUserId"
              :preview-count="previewCount"
              :preview="true" />
          </v-card>
        </v-sheet>
        <v-sheet
          class="fill-height"
          :width="expanded && '30%' || '100%'"
          :min-width="expanded && '500'">
          <div class="d-flex justify-space-between">
            <div class="my-auto">
              {{ $t('organizationalChart.settings.addHeader.label') }}
            </div>
            <div>
              <v-switch
                v-model="showHeaderInput"
                :aria-label="$t('organizationalChart.settings.addHeader.label')"
                color="primary"
                class="my-auto" />
            </div>
          </div>
          <div>
            <translation-text-field
              v-if="showHeaderInput"
              ref="headerTitleInput"
              id="headerTitleInput"
              v-model="headerTitle"
              :object-id="applicationId"
              :default-language="language"
              field-name="chartHeaderTitle"
              object-type="organizationalChart"
              :placeholder="$t('organizationalChart.headerTitle.placeholder')"
              drawer-title="organizationalChart.label.headerTitle.translation"
              maxlength="500"
              class="pt-3"
              no-expand-icon
              back-icon
              autofocus
              required />
          </div>
        </v-sheet>
      </div>
    </template>
    <template
      slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('organizationalChart.settings.cancel.label') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          :disabled="!settingsUpdated"
          @click="saveApplicationSettings">
          {{ $t('organizationalChart.settings.save.label') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      user: null,
      connectedUserOption: '@connected@',
      specificUserOption: '@specific@',
      chartCenterUser: this.connectedUserOption,
      listIgnoredItems: [],
      chartPreviewImage: null,
      selectedUserData: null,
      expanded: false,
      showHeaderInput: true,
      headerTitle: null,
    };
  },
  props: {
    selectedUser: {
      type: Object,
      default: null
    },
    savedHeaderTranslations: {
      type: Object,
      default: null
    },
    hasSettings: {
      type: Boolean,
      default: false
    },
    applicationId: {
      type: String,
      default: null
    },
    language: {
      type: String,
      default: 'en'
    },
    savedUserId: {
      type: String,
      default: null
    }
  },
  watch: {
    showHeaderInput() {
      if (this.showHeaderInput) {
        this.headerTitle = this.savedHeaderTranslations || {[this.language]: this.$t('organizationalChart.header.label')};
      } else {
        this.headerTitle = null;
      }
    },
    chartCenterUser() {
      if (this.connectedUser) {
        this.user = {identityId: eXo?.env?.portal.userIdentityId};
      } else {
        this.user = null;
        this.selectedUserData = !this.isConnectedUserSelected && this.selectedUser || null;
      }
    }
  },
  computed: {
    previewCount() {
      return this.expanded && 3 || 2;
    },
    firstSetupValid() {
      return this.connectedUser || this.user?.identityId;
    },
    settingsUpdated() {
      return !this.hasSettings && this.firstSetupValid
                               || (this.centerUserUpdated || this.listTranslationsUpdated);
    },
    listTranslationsUpdated() {
      return JSON.stringify(Object.values(this.savedHeaderTranslations || {}).sort()) !==
          JSON.stringify(Object.values(this.headerTitle || {}).sort());
    },
    centerUserUpdated() {
      return this.user && this.selectedUser?.id !== this.user?.identityId
                       || this.selectedUser?.id !== this.savedUserId;
    },
    showSelected() {
      return this.specificUser && (this.user || this.selectedUserData);
    },
    isConnectedUserSelected() {
      return this.selectedUser?.id === eXo?.env?.portal?.userIdentityId;
    },
    avatarUrl() {
      return this.user?.profile?.avatarUrl || this.selectedUserData?.avatar;
    },
    specificUser() {
      return this.chartCenterUser === this.specificUserOption;
    },
    connectedUser() {
      return this.chartCenterUser === this.connectedUserOption;
    },
    userFullName() {
      return this.user?.profile?.fullName || this.selectedUserData?.fullname;
    },
    initialUserId() {
      return this.user?.identityId || this.selectedUserData?.id;
    }
  },
  methods: {
    removeSelectedUser() {
      this.user = this.selectedUserData = null;
    },
    saveApplicationSettings() {
      const settings = {
        connectedUser: this.connectedUser,
        userId: this.connectedUser && this.chartCenterUser
                                   || this.user?.identityId
                                   || this.selectedUserData?.id,
        headerTranslations: this.headerTitle || null,
        title: this.headerTitle?.[this.language]
      };
      this.$emit('save-application-settings', settings);
    },
    selectUser(user) {
      this.user = user;
      this.listIgnoredItems = [user?.id];
    },
    initSettings() {
      this.selectedUserData = this.selectedUser;
      this.headerTitle = !this.selectedUser && {[this.language]: this.$t('organizationalChart.header.label')}
                                            || this.savedHeaderTranslations;
      this.showHeaderInput = !this.selectedUser || !!this.headerTitle;
      this.chartCenterUser = (!this.selectedUser || this.isConnectedUserSelected)
                                                && this.connectedUserOption
                                                || this.specificUserOption;
    },
    open() {
      this.initSettings();
      this.$refs.organizationalChartSettingsDrawer.open();
    },
    close() {
      this.$refs.organizationalChartSettingsDrawer.close();
    }
  }
};
</script>
