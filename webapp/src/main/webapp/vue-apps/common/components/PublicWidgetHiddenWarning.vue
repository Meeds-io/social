<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-tooltip v-if="isPublicSite && !hubAccessOpen" top>
    <template #activator="{attrs, on}">
      <v-icon
        class="me-2"
        color="warning"
        size="18"
        v-on="on"
        v-bind="attrs">
        fa-exclamation-triangle
      </v-icon>
    </template>
    <span>
      {{ $t('publicWidgetHidden.tooltipPart1') }}
      <br>
      {{ $t('publicWidgetHidden.tooltipPart2') }}
    </span>
  </v-tooltip>
</template>

<script>
export default {
  data: () => ({
    isPublicSite: eXo.env.portal.portalName === 'public',
    registrationSettings: null,
  }),
  computed: {
    hubAccessOpen() {
      return !this.registrationSettings || this.registrationSettings?.type === 'OPEN';
    },
  },
  created() {
    if (this.isPublicSite) {
      this.initRegistration();
    }
  },
  methods: {
    initRegistration() {
      return this.$registrationService.getRegistrationSettings()
        .then(registrationSettings => {
          this.registrationSettings = registrationSettings;
        });
    },
  },
};
</script>