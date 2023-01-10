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
    <v-card-title class="primary--text px-0">
      {{ $t('forgotpassword.summary') }}
    </v-card-title>
    <v-card-text class="px-0">
      {{ $t('forgotpassword.description') }}
    </v-card-text>

    <form
      :action="formUrl"
      name="resetPasswordForm"
      method="post"
      autocomplete="off"
      class="d-flex ma-0 flex-column">
      <input
        type="hidden"
        name="action"
        value="send">
      <input
        v-if="initialUri"
        type="hidden"
        name="initialURI"
        :value="initialUri">

      <div class="pa-0">
        <v-row class="ma-0 pa-0">
          <v-text-field
            id="username"
            v-model="username"
            :title="$t('portal.login.Username')"
            :placeholder="$t('portal.login.Username')"
            name="username"
            prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
            class="login-username border-box-sizing pt-0"
            autofocus="autofocus"
            aria-required="true"
            type="text"
            tabindex="0"
            required="required"
            outlined
            dense />
        </v-row>
        <v-row class="mx-0 mt-8 pa-0">
          <v-btn
            :aria-label="$t('forgotpassword.send')"
            :disabled="!username"
            type="submit"
            width="222"
            max-width="100%"
            color="primary"
            class="mx-auto login-button btn-primary text-none"
            elevation="0">
            {{ $t('forgotpassword.send') }}
          </v-btn>
        </v-row>
        <v-row class="mx-0 mt-4 pa-0">
          <v-btn
            :aria-label="$t('forgotpassword.back')"
            href="/portal/login"
            width="222"
            max-width="100%"
            class="mx-auto login-button text-none"
            elevation="0"
            outlined>
            <span>
              <v-icon size="16" class="position-absolute mt-n2">fas fa-arrow-left</v-icon>
            </span>
            <span class="mx-auto">
              {{ $t('forgotpassword.backToLogin') }}
            </span>
          </v-btn>
        </v-row>
      </div>
    </form>
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
  data: () => ({
    username: '',
  }),
  computed: {
    initialUri() {
      return this.params?.initialUri;
    },
    formUrl() {
      return window.location.pathname;
    },
  },
  mounted() {
    this.username = this.params?.username;
  },
};
</script>