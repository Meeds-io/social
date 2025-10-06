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
      <portal-external-onboarding-already-authenticated
        v-if="authenticated" />
      <portal-external-onboarding-expired
        v-else-if="action === 'expired'" />
      <portal-external-onboarding-create-user-form
        v-else-if="action === 'createUser'"
        :identifier='this.email'
        :token-param='this.token' />
    </v-card>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    email: '',
    action: '',
    token: ''
  }),
  created() {
    const searchParams = new URLSearchParams(window.location.search);
    this.token = searchParams.get('token');
    this.action = searchParams.get('action');
    if (this.token) {
      if (this.action === 'validateEmail') {
        this.$loginService.finishRegistration(this.token, 'email-validation').then((resp) => {
          if (!resp || !resp.ok) {
            this.action = 'expired';
          } else if (resp.redirected) {
            window.location.href = resp.url;
          } else {
            resp.json().then((data) => {
              this.email = data.username || '';
              this.action = 'createUser';
            });
          }
        });
      } else {
        this.$loginService.verifyToken(this.token, 'external-registration').then((resp) => {
          if (!resp || !resp.ok) {
            this.action = 'expired';
          } else if (resp.redirected) {
            window.location.href = resp.url;
          } else {
            resp.json().then((data) => {
              this.email = data.username || '';
              this.action = 'createUser';
            });
          }
        });
      }
    }
  },
  computed: {
    authenticated() {
      return eXo.env.portal.userName && eXo.env.portal.userName !== '';
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>
