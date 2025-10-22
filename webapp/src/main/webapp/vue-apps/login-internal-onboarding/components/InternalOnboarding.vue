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
  <v-app>
    <v-card
      width="600px"
      max-width="100%"
      class="mx-auto px-4 transparent"
      flat>
      <portal-internal-onboarding-expired
        v-if="action === 'expired'" />
      <portal-internal-onboarding-reset-form
        v-else-if="action === 'createUser'"
        :username-param="this.username"
        :token-param="this.token" />
    </v-card>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    username: '',
    action: '',
  }),
  created() {
    this.token = new URLSearchParams(window.location.search).get('token');
    if (this.token) {
      this.$loginService.verifyToken(this.token, 'onboard').then((resp) => {
        if (!resp || !resp.ok) {
          this.action = 'expired';
        } else if (resp.redirected) {
          window.location.href = resp.url;
        } else {
          resp.json().then((data) => {
            this.username = data.username || '';
            this.action = 'createUser';
          });
        }
      });
    }
  },

  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>
