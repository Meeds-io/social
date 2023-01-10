<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <v-card flat>
    <v-card-title class="primary--text subtitle-1 px-0">
      <span class="mx-auto text-center">{{ $t('onboarding.linkExpired') }}</span>
    </v-card-title>
    <div class="d-flex ma-0 flex-column">
      <div class="pa-0">
        <v-row class="mx-0 mt-8 pa-0">
          <v-btn
            :aria-label="$t('forgotpassword.backToLogin')"
            href="/portal/login"
            color="primary"
            width="222"
            max-width="100%"
            tabindex="0"
            class="mx-auto login-button text-none"
            elevation="0">
            {{ $t('forgotpassword.backToLogin') }}
          </v-btn>
        </v-row>
      </div>
    </div>
  </v-card>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
  },
  computed: {
    formUrl() {
      return window.location.pathname;
    },
  },
};
</script>