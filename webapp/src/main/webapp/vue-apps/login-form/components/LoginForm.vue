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
  <v-app class="mx-auto" v-show="this.init" style="max-width: 375px;">
    <login-form-settings-drawer
          :translation-identifier="values.translationIdentifier"
          :register-enabled="values.registerEnabled"
          :signin-option="signinOption"
          :display-signin-email-button-icon="displaySigninEmailButtonIcon"
          :list-external-providers="listExternalProviders"
          :display-providers-icons="displayProvidersIcons"
          :display-welcome-message="displayWelcomeMessage" />
    <v-hover v-slot="{ hover }">
      <v-card
        class="rounded-0 transparent pa-5"
        flat>
        <div
          v-if="displayBackButton"
          class="position-absolute t-0"
          :class="{
            'r-0': $vuetify.rtl,
            'l-0': !$vuetify.rtl,
          }">
          <v-btn
            icon
            small
            class="mt-5"
            @click="clickDisplayForm">
            <v-icon size="18">
              {{ $vuetify.rtl && 'fa fa-arrow-right' || 'fa fa-arrow-left' }}
            </v-icon>
          </v-btn>
        </div>
        <div
          v-if="values.canEdit && hover"
          class="position-absolute t-0"
          :class="{
            'l-0': $vuetify.rtl,
            'r-0': !$vuetify.rtl,
          }">
          <v-fab-transition hide-on-leave>
            <v-btn
              :title="$t('loginForm.settings.editTooltip')"
              class="z-index-two me-2 mt-2"
              small
              icon
              @click="$root.$emit('login-form-settings')">
              <v-icon size="18">fa-cog</v-icon>
            </v-btn>
          </v-fab-transition>
        </div>
        <div v-if="registerEnabled && displayWelcomeMessage" class="widget-text-header align-center">
          {{ newHere }}
          <a
            :title="createAccount"
            href="/portal/register"
            class="text-decoration-underline">
            {{ createAccount }}
          </a>
        </div>
        <div v-else-if="displayWelcomeMessage" class="widget-text-header align-center">{{ welcomeBack }}</div>

        <portal-login-providers
          :params="values"
          :rememberme="rememberme"
          ref="loginProvidersComponent"
          v-show="!displayForm && listExternalProviders"
          :display-providers-icons="displayProvidersIcons" />

        <portal-login-separator class="mt-5" v-if="!displayForm && signinOption !== 'noform' && listExternalProviders"/>

        <form
          ref="form"
          name="form"
          action="/portal/login"
          method="post"
          autocomplete="off"
          class="d-flex ma-0 flex-column"
          @submit="validateForm()"
          v-if="displayForm || signinOption === 'loginform'">
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
                :autofocus="'autofocus'"
                prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
                class="login-username border-box-sizing"
                name="username"
                aria-required="true"
                type="text"
                tabindex="0"
                required="required"
                outlined
                dense
                background-color="white"/>
            </v-row>
            <v-row class="ma-0 pa-0">
              <v-text-field
                id="password"
                v-model="password"
                :title="$t('portal.login.Password')"
                :placeholder="$t('portal.login.Password')"
                :type="passwordType"
                :append-icon="showPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
                prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
                class="login-password border-box-sizing"
                name="password"
                aria-required="true"
                required="required"
                outlined
                dense
                @click:append="toggleShow"
                background-color="white" />
            </v-row>
            <v-row class="d-flex flex-column flex-sm-row ma-0 py-0 px-3 px-sm-0" flat>
              <v-checkbox
                v-model="rememberme"
                id="rememberme"
                :label="$t('portal.login.RememberOnComputer')"
                :value="rememberme"
                name="rememberme"
                on-icon="fas fa-check-square"
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
            <v-row class="mx-0 mt-8 pa-0 loginButton">
              <v-btn
                id="loginButton"
                :aria-label="$t('portal.login.Signin')"
                :type="'submit'"
                :loading="loading"
                width="222"
                max-width="100%"
                color="primary"
                class="mx-auto login-button text-none"
                elevation="0">
                {{ $t('portal.login.Signin') }}
              </v-btn>
            </v-row>
          </div>
        </form>
        <div v-else-if="!displayForm && signinOption !== 'noform'" class="mt-4 center text-body">
          <v-btn
            color="primary"
            class="elevation-0 loginFormSigninEmailButton"
            outlined
            style="background-color:white;"
            @click="clickDisplayForm()">
            <span class="text-body">
              <v-icon class="me-2" v-if="displaySigninEmailButtonIcon" color="primary">fas fa-envelope</v-icon>
              {{ signinEmailButton }}
            </span>
          </v-btn>
        </div>
      </v-card>
    </v-hover>
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
    loading: false,
    username: '',
    showPassword: false,
    displayForm: false,
    init: false,
    welcomeBack: '',
    newHere: '',
    createAccount: '',
    signinEmailButton: '',
    signinOption: 'loginform',
    displaySigninEmailButtonIcon: true,
    listExternalProviders: true,
    displayProvidersIcons: true,
    displayBackButton: false,
    displayWelcomeMessage: true,
  }),
  watch: {
    errorMessage: {
      immediate: true,
      handler: function() {
        if (this.errorMessage?.trim()?.length) {
          this.displayAlert(this.errorMessage, 'error');
        }
      },
    },
  },
  created() {
    this.values = JSON.parse(this.params);
    this.signinOption = this.values?.signinOption || 'loginform';
    this.welcomeBack = decodeURIComponent(this.values?.welcomeBack || this.$t('portal.login.WelcomeBack'));
    this.newHere = decodeURIComponent(this.values?.newHere || this.$t('UILoginForm.label.registerNewAccount'));
    this.createAccount = decodeURIComponent(this.values?.createAccount || this.$t('UILoginForm.button.registerNewAccount'));
    this.displaySigninEmailButtonIcon = this.values?.displaySigninEmailButtonIcon;
    this.displayWelcomeMessage = this.values?.displayWelcomeMessage;
    this.displayProvidersIcons = this.values?.displayProvidersIcons;
    this.listExternalProviders = this.values?.listExternalProviders;
    this.signinEmailButton = decodeURIComponent(this.values?.signinEmailButton || this.$t('portal.login.SigninUsingEmail'));
    this.$root.$on('login-form-settings-updated', (welcomeBackTranslations,
      newHereTranslations,
      createAccountTranslations,
      signinEmailButtonTranslations,
      signinOption,
      displaySigninEmailButtonIcon,
      listExternalProviders,
      displayProvidersIcons,
      displayWelcomeMessage) => {
      this.welcomeBack = welcomeBackTranslations?.[eXo.env.portal.language] || welcomeBackTranslations?.[eXo.env.portal.defaultLanguage] || this.$t('portal.login.WelcomeBack');
      this.newHere = newHereTranslations?.[eXo.env.portal.language] || newHereTranslations?.[eXo.env.portal.defaultLanguage] || this.$t('UILoginForm.label.registerNewAccount');
      this.createAccount = createAccountTranslations?.[eXo.env.portal.language] || createAccountTranslations?.[eXo.env.portal.defaultLanguage] || this.$t('UILoginForm.button.registerNewAccount');
      this.signinEmailButton = signinEmailButtonTranslations?.[eXo.env.portal.language] || signinEmailButtonTranslations?.[eXo.env.portal.defaultLanguage] || this.$t('portal.login.SigninUsingEmail');
      this.signinOption = signinOption || 'loginform';
      this.displaySigninEmailButtonIcon = displaySigninEmailButtonIcon;
      this.listExternalProviders = listExternalProviders;
      this.displayProvidersIcons = displayProvidersIcons;
      this.displayWelcomeMessage = displayWelcomeMessage;
    });
    this.$root.$on('login-providers-refreshed', (providers) => {
      this.displayForm = providers.length === 0;
      const t = this;
      setTimeout(() => {
        t.init = true;
      }, 10);
    });
  },
  computed: {
    registerEnabled() {
      return this.values?.registerEnabled;
    },
    forgotPasswordPath() {
      return this.values?.forgotPasswordPath;
    },
    passwordType(){
      return this.showPassword ? 'text' :'password';
    },
    initialUri() {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has('initialUri')) {
        return urlParams.get('initialUri');
      }
      return null;
    },
    errorCode() {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has('error')) {
        const error=urlParams.get('error');
        window.history.replaceState('', window.document.title, this.removeQueryParam('error'));
        return error;
      }
      return null;
    },
    errorMessage() {
      return this.errorCode && this.$te(`UILoginForm.label.${this.errorCode}`)
        && this.$t(`UILoginForm.label.${this.errorCode}`)
        || this.errorCode;
    },
  },
  mounted() {
    this.setupUserName();
  },
  methods: {
    clickDisplayForm() {
      this.displayForm = !this.displayForm;
      this.displayBackButton = !this.displayBackButton;
    },
    setupUserName(){
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has('username')) {
        this.username = urlParams.get('username');
      } else if (urlParams.has('email')) {
        this.username = urlParams.get('email');
      }
    },
    toggleShow() {
      this.showPassword = !this.showPassword;
    },
    validateForm() {
      this.loading = this.$refs.form.reportValidity();
      window.setTimeout(() => this.loading = false, 10000);
      return true;
    },
    displayAlert(message, type) {
      window.setTimeout(() => {
        this.$root.$emit('alert-message', message, type);
      }, 200);
    },
    removeQueryParam(paramName) {
      const url = new URL(window.location);
      url.searchParams.delete(paramName);
      return url.href;
    },
  },
};
</script>

<style lang="scss">
  .loginFormSigninEmailButton {
    min-height: 36px;
    height: 100% !important;
  }
  .loginFormSigninEmailButton > .v-btn__content {
    max-width: 100%;
    white-space: normal;
  }
</style>
