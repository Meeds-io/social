<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <v-app v-if="displayed">
    <div class="application-body">
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="text-title">
              {{ $t('UserSettings.hiddenApplications.title') }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ countLabel }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              id="userSettingHiddenApplicationsEditButton"
              :title="$t('UserSettings.hiddenApplications.title')"
              :aria-label="$t('UserSettings.hiddenApplications.title')"
              small
              icon
              @click="$refs.drawer.open()">
              <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <user-setting-hidden-applications-drawer
        ref="drawer"
        :hidden-applications="hiddenApplications"
        @shown="remove" />
    </div>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    hiddenApplications: [],
    loadingError: false,
    displayed: true,
  }),
  computed: {
    countLabel() {
      if (this.loadingError) {
        return this.$t('UserSettings.hiddenApplications.loadError');
      } else if (!this.hiddenApplications.length) {
        return this.$t('UserSettings.hiddenApplications.none');
      } else if (this.hiddenApplications.length === 1) {
        return this.$t('UserSettings.hiddenApplications.one');
      } else {
        return this.$t('UserSettings.hiddenApplications.count', {0: this.hiddenApplications.length});
      }
    },
  },
  watch: {
    displayed() {
      this.$root.$updateApplicationVisibility(this.displayed);
    },
  },
  created() {
    // The settings page shows one setting app at a time while one of them
    // edits its setting, as the other user settings apps do
    document.addEventListener('hideSettingsApps', this.hideApplication);
    document.addEventListener('showSettingsApps', this.showApplication);
    this.$hiddenApplicationService.getHiddenApplications(eXo.env.portal.language)
      .then(hiddenApplications => this.hiddenApplications = hiddenApplications || [])
      .catch(() => {
        // Not the empty state: the user's hidden content may exist
        this.hiddenApplications = [];
        this.loadingError = true;
      })
      .finally(() => this.$root.$applicationLoaded());
  },
  beforeDestroy() {
    document.removeEventListener('hideSettingsApps', this.hideApplication);
    document.removeEventListener('showSettingsApps', this.showApplication);
  },
  methods: {
    remove(hiddenApplication) {
      this.hiddenApplications = this.hiddenApplications.filter(application => application !== hiddenApplication);
    },
    hideApplication(event) {
      if (event?.detail && event.detail !== this.$el?.id) {
        this.displayed = false;
      }
    },
    showApplication() {
      this.displayed = true;
    },
  },
};
</script>
