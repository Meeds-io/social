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
  <v-app v-if="showApplication">
    <v-card
      outlined
      class="white border-radius pa-5 card-border-radius">
      <div
        v-if="isLoading"
        class="width-full d-flex full-height">
        <v-progress-circular
          class="ma-auto"
          color="primary"
          indeterminate />
      </div>
      <div v-else>
        <organizational-chart-header
          v-if="!preview"
          :has-settings="hasSettings"
          :configured-title="configuredHeaderTitle"
          :is-admin="isAdmin"
          @open-chart-settings="openSettingsDrawer" />
        <organizational-chart
          v-if="hasSettings || preview"
          :user="user"
          :managed-users="managedUsersList"
          :profile-action-extensions="profileActionExtensions"
          :is-loading="isLoadingManagedUsers"
          :has-more="hasMore"
          :preview="preview"
          :preview-count="previewCount"
          @update-chart="updateChart"
          @load-more-managed-users="getManagedUsers" />
      </div>
    </v-card>
    <organizational-chart-settings-drawer
      v-if="!preview"
      ref="chartSettingsDrawer"
      :selected-user="user"
      :saved-header-text="configuredHeaderTitle"
      :has-settings="hasSettings"
      @save-application-settings="saveApplicationSettings" />
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      user: null,
      managedUsers: [],
      fieldsToRetrieve: 'settings,manager,managedUsersCount',
      pageSize: 12,
      limit: 0,
      offset: 0,
      hasMore: false,
      isLoadingManagedUsers: false,
      isLoading: true,
      profileActionExtensions: [],
      userName: eXo?.env?.portal.userName,
      collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
      settingsContextKey: 'GLOBAL',
      settingScopeKey: 'APPLICATION',
      settingsCenterUserKey: 'organizationalChartCenterUser',
      settingsHeaderTitleKey: 'organizationalChartHeaderTitle'
    };
  },
  props: {
    preview: {
      type: Boolean,
      default: false
    },
    previewCount: {
      type: Number,
      default: 2
    },
    initialUserName: {
      type: String,
      default: null
    },
    isSpaceManager: {
      type: Boolean,
      default: false
    },
  },
  watch: {
    initialUserName() {
      if (this.initialUserName) {
        this.updateChart(this.initialUserName);
      }
    }
  },
  computed: {
    showApplication() {
      return this.hasSettings || (!this.hasSettings && this.isAdmin) || this.preview;
    },
    isAdmin() {
      return this.user?.isAdmin || this.isSpaceManager;
    },
    configuredHeaderTitle() {
      return this.$root?.settings?.title;
    },
    applicationId() {
      return this.$root?.applicationId;
    },
    hasSettings() {
      return this.$root.settings?.user;
    },
    managedUsersList() {
      return this.preview && this.sortedManagedUsersList?.length
                          && this.sortedManagedUsersList.slice(0, this.previewCount) || this.sortedManagedUsersList;
    },
    sortedManagedUsersList() {
      return this.managedUsers?.filter(managedUser => managedUser.enabled)
        ?.sort((a, b) => this.usersNaturalComparator(a, b));
    }
  },
  created() {
    const centerUser = this.initialUserName || this.userName;
    this.updateChart(centerUser);
    this.refreshExtensions();
    document.addEventListener('profile-extension-updated', this.refreshExtensions);
  },
  methods: {
    saveApplicationSettings(settings) {
      return this.saveSetting(this.settingsCenterUserKey, settings?.user).then(() => {
        this.saveSetting(this.settingsHeaderTitleKey, settings?.title).then(() => {
          this.$refs.chartSettingsDrawer.close();
          this.$root.settings = settings;
          const user = settings.connectedUser && eXo?.env?.portal?.userName
                                              || settings?.user;
          this.$root.settings.user = user;
          this.updateChart(user);
          this.$root.$emit('alert-message', this.$t('organizationalChart.settings.saved.success'), 'success');
        });
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('organizationalChart.settings.saved.error'), 'error');
      });
    },
    saveSetting(settingKey, settingValue) {
      return this.$settingService.setSettingValue(this.settingsContextKey, '',
        this.settingScopeKey, this.applicationId, settingKey, settingValue);
    },
    openSettingsDrawer() {
      this.$refs.chartSettingsDrawer.open();
    },
    usersNaturalComparator(a, b) {
      return this.collator.compare(a.fullname, b.fullname);
    },
    updateChart(userName) {
      this.isLoading = true;
      this.userName = userName;
      this.managedUsers = [];
      this.getUser();
      this.getManagedUsers();
    },
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.profileActionExtensions.sort((elementOne, elementTwo) => (elementOne.order || 100) - (elementTwo.order || 100));
    },
    getUser() {
      return this.$userService.getUser(this.userName, this.fieldsToRetrieve).then(user => {
        this.user = user;
      });
    },
    getManagedUsers() {
      const profileSetting = {manager: this.userName};
      if (this.abortController) {
        this.abortController.abort();
      }
      this.abortController = new AbortController();
      this.offset = this.managedUsers.length || 0;
      this.limit = this.limit || this.pageSize;
      this.isLoadingManagedUsers = true;
      this.$userService.getUsersByAdvancedFilter(profileSetting, this.offset, this.limit + 1, 'settings,managedUsersCount', 'all', null, false, this.abortController.signal).then(data => {
        const users = data?.users?.slice(0, this.limit);
        this.managedUsers.push(...users);
        this.hasMore = data?.users?.length > this.limit;
      }).finally(() => {
        this.abortController = null;
        this.isLoadingManagedUsers = false;
        this.isLoading = false;
      });
    }
  }
};
</script>
