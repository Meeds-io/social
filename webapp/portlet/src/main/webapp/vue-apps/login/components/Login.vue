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
    <v-main>
      <v-container fluid>
        <div class="loginBGLight">
          <span></span>
        </div>
        <div class="uiLogin">
          <div class="loginContainer">
            <div class="loginHeader introBox">
              <div class="userLoginIcon">
                {{ $t('portal.login.Connectlabel') }}
              </div>
            </div>
            <div class="loginContent">
              <p class="brandingComanyClass mt-5 mb-0">{{ companyName }}</p>
              <div v-if="registerEnabled" class="center white--text my-3">
                {{ $t('UILoginForm.label.registerNewAccount') }}
                <a
                  :title="$t('UILoginForm.button.registerNewAccount')"
                  href="/portal/register"
                  class="text-decoration-underline white--text">
                  {{ $t('UILoginForm.button.registerNewAccount') }}
                </a>
              </div>
              <div class="titleLogin">
                <div
                  v-if="errorCode"
                  class="signinFail"
                  role="alert">
                  <i class="errorIcon uiIconError"></i>{{ errorMessage }}
                </div>
                <extension-registry-components
                  :params="extensionParams"
                  name="LoginHeader"
                  type="login-header"
                  parent-element="div"
                  element="div">
                  <template #separator>
                    <div class="d-flex my-5">
                      <v-divider class="my-auto white" />
                      <span class="mx-3 white--text text-uppercase">
                        {{ $t('UILoginForm.label.or') }}
                      </span>
                      <v-divider class="my-auto white" />
                    </div>
                  </template>
                  <template #footer>
                    <div class="d-flex mt-5">
                      <v-divider class="my-auto white" />
                      <span class="mx-3 white--text text-uppercase">
                        {{ $t('UILoginForm.label.or') }}
                      </span>
                      <v-divider class="my-auto white" />
                    </div>
                  </template>
                </extension-registry-components>
                <div class="centerLoginContent">
                  <form
                    name="loginForm"
                    action="/portal/login"
                    method="post"
                    style="margin: 0px;"
                    autocomplete="off">
                    <input
                      v-if="initialUri"
                      type="hidden"
                      name="initialURI"
                      :value="initialUri">
                    <div class="userCredentials">
                      <span class="iconUser"></span>
                      <input
                        id="username"
                        :placeholder="$t('portal.login.Username')"
                        name="username"
                        v-model="username"
                        tabindex="1"
                        type="text"
                        aria-required="true">
                    </div>
                    <div class="userCredentials">
                      <span class="iconPswrd"></span>
                      <input
                        id="password"
                        :placeholder="$t('portal.login.Password')"
                        tabindex="2"
                        :type="passwordType"
                        name="password"
                        aria-required="true">
                      <span class="PswdToggleShow" @click="toggleShow()">
                        <i :class="{'fas fa-eye-slash':showPassword ,'fas fa-eye':!showPassword}"></i>
                      </span>
                    </div>
                    <div class="rememberContent ms-1">
                      <input
                        v-model="rememberme"
                        id="rememberme"
                        name="rememberme"
                        type="checkbox"
                        class="checkbox mb-2">
                      <label class="uiCheckbox" for="rememberme">
                        {{ $t('portal.login.RememberOnComputer') }}
                      </label>
                    </div>
                    <div id="UIPortalLoginFormAction" class="loginButton">
                      <button
                        :aria-label="$t('portal.login.Signin')"
                        tabindex="4"
                        class="col-auto">
                        {{ $t('portal.login.Signin') }}
                      </button>
                    </div>
                    <div class="forgotPasswordClass">
                      <a
                        :href="forgotPasswordPath"
                        :title="$t('gatein.forgotPassword.loginLinkTitle')"
                        class="text-decoration-underline">
                        {{ $t('portal.login.forgotPassword') }} </a>
                    </div>
                  </form>
                </div>
                <extension-registry-components
                  :params="extensionParams"
                  name="LoginFooter"
                  type="login-footer"
                  parent-element="div"
                  element="div">
                  <template #separator>
                    <div class="d-flex my-5">
                      <v-divider class="my-auto white" />
                      <span class="mx-3 white--text text-uppercase">
                        {{ $t('UILoginForm.label.or') }}
                      </span>
                      <v-divider class="my-auto white" />
                    </div>
                  </template>
                  <template #header>
                    <div class="d-flex my-5">
                      <v-divider class="my-auto white" />
                      <span class="mx-3 white--text text-uppercase">
                        {{ $t('UILoginForm.label.or') }}
                      </span>
                      <v-divider class="my-auto white" />
                    </div>
                  </template>
                </extension-registry-components>
              </div>
            </div>
          </div>
        </div>
        <div class="brandingImageContent">
          <img
            :src="brandingLogo"
            class="brandingImage"
            role="presentation">
        </div>
      </v-container>
    </v-main>
  </v-app>
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
      return this.params && this.params.companyName;
    },
    initialUri() {
      return this.params && this.params.initialUri;
    },
    forgotPasswordPath() {
      return this.params && this.params.forgotPasswordPath;
    },
    brandingLogo() {
      return this.params && this.params.brandingLogo;
    },
    registerEnabled() {
      return this.params && this.params.registerEnabled;
    },
    errorCode() {
      return this.params && this.params.errorCode;
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
  created() {
    document.title = this.$t('UILoginForm.label.login');
  },
  mounted() {
    this.setupUserName();
    this.$root.$applicationLoaded();
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
    }
  }
};
</script>