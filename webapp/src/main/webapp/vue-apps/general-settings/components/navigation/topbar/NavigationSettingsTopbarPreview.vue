<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <v-card
    :class="mobilePreview && 'mx-auto'"
    :width="mobilePreview && 420 || undefined"
    class="d-flex align-center px-4"
    height="56">
    <div class="d-flex">
      <v-avatar
        v-if="displayCompanyLogo"
        class="flex-shrink-0"
        size="36"
        width="auto"
        tile>
        <img
          src="/portal/rest/v1/platform/branding/logo"
          height="36px"
          width="auto"
          alt="">
      </v-avatar>
      <div
        v-if="displayCompanyName"
        :class="mobilePreview && 'd-none' || 'd-flex'"
        class="align-self-center ms-4">
        <div class="logoTitle text-body font-weight-bold menu-text-color text-truncate">
          {{ $root.branding?.companyName }}
        </div>
      </div>
    </div>
    <div v-if="displayCompanyLogo" class="mx-4">
      <v-icon>
        {{ $vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right' }}
      </v-icon>
    </div>
    <div class="d-flex">
      <div>
        <portal-general-settings-navigation-settings-icon
          :icon="firstSidebarSiteIcon"
          :icon-size="33" />
      </div>
      <div v-if="displaySiteName" class="ps-2 ms-3 align-self-center d-none d-sm-flex">
        <div class="logoTitle text-body font-weight-bold menu-text-color text-truncate">
          {{ $t(firstSidebarSiteName) }}
        </div>
      </div>
    </div>
    <v-spacer />
    <div v-if="enabledApplications.length" class="d-flex">
      <v-btn
        v-for="(app, index) in enabledApplications"
        :key="`${app.name}_${index}`"
        :title="app.name"
        :class="{
          'hidden-xs-only': !app.mobile,
        }"
        icon
        class="ms-2">
        <v-img
          v-if="app.imageUrl"
          :src="app.imageUrl"
          max-height="22"
          max-width="22"
          contain />
        <portal-general-settings-navigation-settings-icon
          v-else-if="app.icon"
          :icon="app.icon"
          :icon-size="22" />
      </v-btn>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    mobilePreview: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    firstSidebarItem() {
      return this.settings?.sidebar?.items?.[0];
    },
    firstSidebarSiteIcon() {
      return this.firstSidebarItem?.properties?.siteIcon || this.firstSidebarItem?.icon;
    },
    firstSidebarSiteName() {
      return this.firstSidebarItem?.properties?.siteDisplayName || this.firstSidebarItem?.name;
    },
    enabledApplications() {
      return this.settings?.topbar?.applications?.filter?.(a => a.enabled && (!this.mobilePreview || a.mobile));
    },
    displayCompanyName() {
      return this.settings?.topbar?.displayCompanyName;
    },
    displayCompanyLogo() {
      return !this.mobilePreview || this.settings?.topbar?.displayMobileCompanyLogo;
    },
    displaySiteName() {
      return !this.mobilePreview && this.settings?.topbar?.displaySiteName;
    },
  },
};
</script>