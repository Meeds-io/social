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
  <exo-drawer
    id="userSettingHiddenApplicationsDrawer"
    ref="drawer"
    right>
    <template #title>
      {{ $t('UserSettings.hiddenApplications.title') }}
    </template>
    <template #content>
      <v-list v-if="hiddenApplications.length" class="pa-4">
        <v-list-item
          v-for="hiddenApplication in hiddenApplications"
          :key="`${hiddenApplication.type}:${hiddenApplication.name}`"
          class="hidden-application-item px-0">
          <v-list-item-content>
            <v-list-item-title>
              {{ hiddenApplication.title }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ pageLabel(hiddenApplication) }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :loading="showing === hiddenApplication"
              :disabled="!!showing && showing !== hiddenApplication"
              class="btn show-hidden-application-button"
              @click="show(hiddenApplication)">
              {{ $t('UserSettings.hiddenApplications.show') }}
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <div
        v-else
        id="userSettingHiddenApplicationsEmpty"
        class="d-flex flex-column align-center text-center pa-8">
        <v-icon
          size="48"
          class="icon-default-color mb-4"
          aria-hidden="true">
          fa-eye-slash
        </v-icon>
        <div class="text-title mb-2">
          {{ $t('UserSettings.hiddenApplications.none') }}
        </div>
        <div class="text-subtitle">
          {{ $t('UserSettings.hiddenApplications.emptyDescription') }}
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    hiddenApplications: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    showing: null,
  }),
  methods: {
    pageLabel(hiddenApplication) {
      return [hiddenApplication.siteLabel, hiddenApplication.pageTitle].filter(label => label).join(' - ');
    },
    open() {
      this.$refs.drawer.open();
    },
    show(hiddenApplication) {
      this.showing = hiddenApplication;
      return this.$hiddenApplicationService.show(hiddenApplication.type, hiddenApplication.name)
        .then(() => this.$emit('shown', hiddenApplication))
        .catch(() => this.$root.$emit('alert-message', this.$t('UserSettings.hiddenApplications.showError'), 'error'))
        .finally(() => this.showing = null);
    },
  },
};
</script>
