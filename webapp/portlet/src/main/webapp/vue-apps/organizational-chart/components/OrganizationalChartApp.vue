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
    <v-hover v-slot="{ hover }">
      <v-card
        outlined
        class="pa-5 application-body">
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
            :has-header-title="hasHeaderTitle"
            :hover="hover"
            :is-mobile="isMobile"
            :is-printing-pdf="isPrintingPdf"
            @download-chart="downloadChart"
            @open-chart-settings="openSettingsDrawer" />
          <organizational-chart
            v-if="hasSettings || preview"
            id="chart"
            :user="user"
            :managed-users="managedUsersList"
            :profile-action-extensions="profileActionExtensions"
            :user-navigation-extensions="userExtensions"
            :is-loading="isLoadingManagedUsers"
            :has-more="hasMore"
            :preview="preview"
            :preview-count="previewCount"
            @update-chart="updateChart"
            @load-more-managed-users="getManagedUsers" />
        </div>
      </v-card>
    </v-hover>
    <organizational-chart-settings-drawer
      v-if="!preview"
      ref="chartSettingsDrawer"
      :selected-user="user"
      :saved-header-translations="savedHeaderTranslations"
      :application-id="applicationId"
      :has-settings="hasSettings"
      :saved-user-id="savedUserId"
      :language="language"
      :default-title="configuredHeaderTitle"
      :can-update-center-user="canUpdateCenterUser"
      :has-header-title="hasHeaderTitle"
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
      userExtensions: [],
      userId: eXo?.env?.portal.userIdentityId,
      collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
      settingsContextKey: 'GLOBAL',
      settingScopeKey: 'APPLICATION',
      settingsCenterUserKey: 'organizationalChartCenterUser',
      settingsHasHeaderTitleKey: 'organizationalChartHasHeaderTitle',
      centerUserIdUrlParam: 'centerUserId',
      headerTitleFieldName: 'chartHeaderTitle',
      translationObjectType: 'organizationalChart',
      language: eXo?.env?.portal?.language,
      isPrintingPdf: false
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
    initialUserId: {
      type: String,
      default: null
    },
    isSpaceManager: {
      type: Boolean,
      default: false
    },
  },
  watch: {
    initialUserId() {
      if (this.initialUserId) {
        this.updateChart(this.initialUserId);
      }
    }
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    showApplication() {
      return this.hasSettings || (!this.hasSettings && this.isAdmin) || this.preview;
    },
    isAdmin() {
      return this.user?.isAdmin || this.isSpaceManager;
    },
    savedHeaderTranslations() {
      return this.$root.settings?.headerTranslations;
    },
    configuredHeaderTitle() {
      return this.$root?.settings?.title;
    },
    applicationId() {
      return this.$root?.applicationId;
    },
    savedUserId() {
      return this.$root.settings?.userId;
    },
    hasHeaderTitle() {
      return this.$root.settings?.hasHeaderTitle;
    },
    canUpdateCenterUser() {
      return this.$root.settings?.canUpdateCenterUser;
    },
    hasSettings() {
      return !!this.savedUserId || !this.canUpdateCenterUser;
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
    const centerUser = this.getRequestedCenterUserId() || this.initialUserId || this.userId;
    this.updateChart(centerUser);
    this.refreshExtensions();
    this.refreshUserExtensions();
    document.addEventListener('profile-extension-updated', this.refreshExtensions);
    document.addEventListener('user-extension-updated', this.refreshUserExtensions);
  },
  methods: {
    downloadChart() {
      this.isPrintingPdf = true;
      const content = document.getElementById('chart');
      html2canvas(content, { scale: 2 })
        .then(canvas => {
          const imgData = canvas.toDataURL('image/png');
          /* eslint-disable new-cap */
          const doc = new jspdf.jsPDF({orientation: 'landscape', unit: 'mm', format: [400, 600]});
          const img = new Image();
          img.src = imgData;
          img.onload = function() {
            const imgWidth = img.width;
            const imgHeight = img.height;
            const pageWidth = doc.internal.pageSize.getWidth();
            const pageHeight = doc.internal.pageSize.getHeight();
            const scaleFactor = Math.min(pageWidth / imgWidth, (pageHeight - 20) / imgHeight);
            const imageX = (pageWidth - (imgWidth * scaleFactor)) / 2;
            const imageY = (pageHeight - (imgHeight * scaleFactor)) / 2;
            doc.addImage(imgData, 'PNG', imageX, imageY, imgWidth * scaleFactor, imgHeight * scaleFactor);
            doc.save('chart.pdf');
          };
        })
        .catch(() => {
          this.$root.$emit('alert-message', this.$t('organizationalChart.download.pdf.error'), 'error');
        }).finally(() => this.isPrintingPdf = false);
    },
    updateUrl(identityId) {
      if (this.preview) {
        return;
      }
      const searchParams = new URLSearchParams(window.location.search);
      searchParams.set(this.centerUserIdUrlParam, identityId);
      const url = `${window.location.pathname}?${searchParams.toString()}`;
      window.history.pushState('OrganizationalChartUrl', '', url);
    },
    getRequestedCenterUserId() {
      if (this.preview) {
        return null;
      }
      const urlParams = new URLSearchParams(window.location.search);
      return urlParams?.get(this.centerUserIdUrlParam);
    },
    saveOrDeleteTranslations(settings) {
      if (settings?.headerTranslations) {
        return this.$translationService.saveTranslations(this.translationObjectType, this.applicationId,
          this.headerTitleFieldName, settings?.headerTranslations);
      } else if (this.savedHeaderTranslations) {
        return this.$translationService.deleteTranslations(this.translationObjectType, this.applicationId);
      }
      return Promise.resolve();
    },
    saveApplicationSettings(settings) {
      return this.saveSetting(this.settingsCenterUserKey, settings?.userId).then(() => {
        return this.saveSetting(this.settingsHasHeaderTitleKey, String(settings?.hasHeaderTitle)).then(() => {
          return this.saveOrDeleteTranslations(settings).then(() => {
            this.$refs.chartSettingsDrawer.close();
            this.$root.settings = settings;
            const userId = settings.connectedUser && eXo?.env?.portal?.userIdentityId
                                                || settings?.userId;
            this.$root.settings.userId = userId;
            this.updateChart(userId);
            this.$root.$emit('alert-message', this.$t('organizationalChart.settings.saved.success'), 'success');
          });
        });
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('organizationalChart.settings.saved.error'), 'error');
      });
    },
    saveSetting(settingKey, settingValue) {
      if (!settingValue) {
        return;
      }
      return this.$settingService.setSettingValue(this.settingsContextKey, '',
        this.settingScopeKey, `organizationalChart${this.applicationId}`, settingKey, settingValue);
    },
    openSettingsDrawer() {
      this.$refs.chartSettingsDrawer.open();
    },
    usersNaturalComparator(a, b) {
      return this.collator.compare(a.fullname, b.fullname);
    },
    updateChart(userId) {
      this.updateUrl(userId);
      this.isLoading = true;
      this.userId = userId;
      this.managedUsers = [];
      this.getUser();
    },
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.profileActionExtensions.sort((elementOne, elementTwo) => (elementOne.order || 100) - (elementTwo.order || 100));
    },
    refreshUserExtensions() {
      this.userExtensions = extensionRegistry.loadExtensions('user-extension', 'navigation') || [];
      this.userExtensions.sort((elementOne, elementTwo) => (elementOne.order || 100) - (elementTwo.order || 100));
    },
    getUser() {
      if (isNaN(this.userId)) {
        return this.$userService.getUser(this.userId, this.fieldsToRetrieve).then(user => {
          this.user = user;
          this.getManagedUsers();
        });
      } else {
        return this.$identityService.getIdentityById(this.userId, this.fieldsToRetrieve).then(user => {
          this.user = user?.profile;
          this.getManagedUsers();
        });
      }
    },
    getManagedUsers() {
      const profileSetting = {manager: this.user?.username};
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
