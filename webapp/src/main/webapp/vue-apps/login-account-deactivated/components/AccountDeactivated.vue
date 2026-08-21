<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <v-app>
    <v-card
      width="350px"
      max-width="100%"
      class="mx-auto transparent"
      flat>
      <div class="text-color font-weight-bold text-center mb-2">
        {{ $t('accountDeactivated.title') }}
      </div>
      <div class="text-color text-center">
        <div>{{ $t('accountDeactivated.support') }}</div>
        <div>{{ $t('accountDeactivated.seeYouSoon') }}</div>
      </div>
      <v-row class="mx-0 mt-8 pa-0">
        <v-btn
          :aria-label="$t('accountDeactivated.backToLogin')"
          :href="loginUrl"
          max-width="100%"
          tabindex="0"
          class="mx-auto login-button text-none px-4"
          elevation="0"
          outlined>
          <v-icon size="16" class="me-2">fas fa-arrow-left</v-icon>
          {{ $t('accountDeactivated.backToLogin') }}
        </v-btn>
      </v-row>
    </v-card>
  </v-app>
</template>
<script>
export default {
  computed: {
    loginUrl() {
      return `${eXo.env.portal.context}/login`;
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>
