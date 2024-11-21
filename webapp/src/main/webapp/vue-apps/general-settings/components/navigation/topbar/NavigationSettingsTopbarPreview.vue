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
    class="d-flex align-center px-4"
    height="56">
    <div v-if="displayCompanyName || !displaySiteName" class="d-flex">
      <v-card
        min-width="36"
        class="pe-3 flex-shrink-0"
        flat>
        <img
          src="/portal/rest/v1/platform/branding/logo"
          height="36px"
          width="auto"
          alt="">
      </v-card>
      <div v-if="displayCompanyName" class="ps-2 align-self-center d-none d-sm-flex">
        <div class="logoTitle text-body font-weight-bold menu-text-color text-truncate">
          {{ $root.branding?.companyName }}
        </div>
      </div>
    </div>
    <div v-if="displaySiteName && displayCompanyName" class="mx-4">
      <v-icon>
        {{ $vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right' }}
      </v-icon>
    </div>
    <div v-if="displaySiteName" class="d-flex">
      <div class="pe-3">
        <v-icon size="33">{{ firstSidebarSiteIcon }}</v-icon>
      </div>
      <div class="ps-2 align-self-center d-none d-sm-flex">
        <div class="logoTitle text-body font-weight-bold menu-text-color text-truncate">
          {{ firstSidebarSiteName }}
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
        <v-icon size="22">{{ app.icon }}</v-icon>
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
  },
  computed: {
    firstSidebarSiteIcon() {
      return this.settings?.sidebar?.items?.[0]?.icon;
    },
    firstSidebarSiteName() {
      return this.settings?.sidebar?.items?.[0]?.name;
    },
    enabledApplications() {
      return this.settings?.topbar?.applications?.filter?.(a => a.enabled);
    },
    displayCompanyName() {
      return this.settings?.topbar?.displayCompanyName;
    },
    displaySiteName() {
      return this.settings?.topbar?.displaySiteName;
    },
  },
};
</script>