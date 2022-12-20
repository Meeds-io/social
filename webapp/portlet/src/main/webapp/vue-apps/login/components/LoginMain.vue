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
  <v-card
    width="350px"
    max-width="100%"
    class="mx-auto"
    flat>
    <v-card-title class="display-1 primary--text px-0 center d-none d-sm-block">
      {{ companyName }}
    </v-card-title>
    <div v-if="registerEnabled" class="center my-3">
      {{ $t('UILoginForm.label.registerNewAccount') }}
      <a
        :title="$t('UILoginForm.button.registerNewAccount')"
        href="/portal/register"
        class="text-decoration-underline">
        {{ $t('UILoginForm.button.registerNewAccount') }}
      </a>
    </div>

    <v-alert
      v-if="errorCode"
      type="error"
      class="position-static elevation-0"
      dismissible>
      {{ errorMessage }}
    </v-alert>

    <portal-login-main-top-extensions
      :params="params"
      :rememberme="rememberme" />

    <portal-login-providers
      :params="params"
      :rememberme="rememberme" />

    <form
      name="loginForm"
      action="/portal/login"
      method="post"
      autocomplete="off"
      class="d-flex ma-0 flex-column"
      onsubmit="return isValidForm()">
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
            :placeholder="$t('portal.login.Username')"
            :readonly="disabled"
            :autofocus="!disabled && 'autofocus'"
            prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
            class="login-username border-box-sizing"
            name="username"
            aria-required="true"
            type="text"
            tabindex="1"
            required="required"
            outlined
            dense />
        </v-row>
        <v-row class="ma-0 pa-0">
          <v-text-field
            id="password"
            v-model="password"
            :placeholder="$t('portal.login.Password')"
            :type="passwordType"
            :append-icon="showPassword ? 'fas fa-eye-slash subtitle-1 mt-0' : 'fas fa-eye subtitle-1 mt-0'"
            :readonly="disabled"
            prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
            class="login-password border-box-sizing"
            name="password"
            tabindex="2"
            required="required"
            outlined
            dense
            @click:append="toggleShow" />
        </v-row>
        <v-row class="d-flex flex-column flex-sm-row ma-0 py-0 px-3 px-sm-0" flat>
          <v-checkbox
            v-model="rememberme"
            id="rememberme"
            name="rememberme"
            on-icon="fas fa-check-square"
            :label="$t('portal.login.RememberOnComputer')"
            class="mx-0 my-3 my-sm-0"
            dense />
          <v-spacer />
          <a
            :href="forgotPasswordPath"
            :title="$t('portal.login.forgotPassword')"
            class="text-decoration-underline d-flex">
            <span class="v-label theme--light pb-2px my-auto">
              {{ $t('portal.login.forgotPassword') }}
            </span>
          </a>
        </v-row>
        <v-row class="mx-0 mt-8 pa-0">
          <v-btn
            :aria-label="$t('portal.login.Signin')"
            :type="disabled && 'button' || 'submit'"
            width="222"
            max-width="100%"
            tabindex="4"
            color="primary"
            class="mx-auto login-button"
            elevation="0">
            {{ $t('portal.login.Signin') }}
          </v-btn>
        </v-row>
      </div>
    </form>

    <portal-login-main-bottom-extensions
      :params="params"
      :rememberme="rememberme" />
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
    rememberme: true,
    username: '',
    showPassword: false
  }),
  computed: {
    passwordType(){
      return this.showPassword ? 'text' :'password';
    },
    companyName() {
      return this.params?.companyName;
    },
    disabled() {
      return this.params?.disabled;
    },
    initialUri() {
      return this.params?.initialUri;
    },
    forgotPasswordPath() {
      return this.params?.forgotPasswordPath;
    },
    registerEnabled() {
      return this.params?.registerEnabled;
    },
    errorCode() {
      return this.params?.errorCode;
    },
    errorMessage() {
      return this.errorCode && this.$t(`UILoginForm.label.${this.errorCode}`);
    },
    extensionParams() {
      return {
        params: this.params,
        rememberme: this.rememberme,
      };
    },
  },
  mounted() {
    if (!this.disabled) {
      this.setupUserName();
    }
  },
  methods: {
    setupUserName(){
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has('username')) {
        this.username = urlParams.get('username');
      }
    },
    toggleShow(){
      this.showPassword = !this.showPassword;
    },
    isValidForm(){
      return !this.disabled;
    },
  },
};
</script>