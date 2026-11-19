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
  <v-card flat class="transparent">
    <v-card-title class="text-break text-body px-0">
      {{ $t('onboarding.summary1') }}
    </v-card-title>
    <v-card-title class="text-break text-body pa-0">
      {{ $t('onboarding.summary2') }}
    </v-card-title>

    <form
      name="resetPasswordForm"
      method="post"
      autocomplete="off"
      class="d-flex ma-0 flex-column"
      @submit.prevent="submitForm()">
      <div>
        <v-row class="ma-0 pa-0">
          <v-card
            width="350"
            flat
            class="transparent mt-4">
            <v-text-field
              id="username"
              :value="accountIdentifier"
              :title="accountIdentifierLabel"
              :placeholder="accountIdentifierLabel"
              :prepend-inner-icon="`${accountIdentifierIcon} ms-n2 grey--text text--lighten-1`"
              name="username"
              class="login-username border-box-sizing pt-0"
              aria-required="true"
              type="text"
              required="required"
              readonly
              outlined
              dense
              background-color="white" />
          </v-card>
          <v-card
            width="350"
            flat
            class="transparent">
            <div id="internalOnboardingAdditionalFields">
              <component
                :is="extension.vueComponent"
                v-for="extension in extensions"
                :key="extension.id"
                :context="extensionContexts[extension.id]"
                @update:data="setExtensionData(extension.id, $event)" />
            </div>
          </v-card>
          <v-card
            width="350"
            flat
            class="transparent">
            <v-text-field
              id="password"
              v-model="password"
              :title="$t('onboarding.NewPassword')"
              :placeholder="$t('onboarding.NewPassword')"
              :type="passwordType"
              :append-icon="showPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              class="login-password border-box-sizing"
              name="password"
              autocomplete="new-password"
              autofocus="autofocus"
              tabindex="0"
              required="required"
              outlined
              dense
              @click:append="toggleShow"
              background-color="white" />
          </v-card>
          <span class="text-subtitle">{{ $t('onboarding.passwordCondition') }}</span>
          <v-card
            width="350"
            flat
            class="transparent">
            <v-text-field
              id="password2"
              v-model="confirmPassword"
              :title="$t('onboarding.ConfirmNewPassword')"
              :placeholder="$t('onboarding.ConfirmNewPassword')"
              :type="passwordConfirmType"
              :append-icon="showConfirmPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              class="login-password border-box-sizing pt-2"
              name="password2"
              autocomplete="new-password"
              required="required"
              outlined
              dense
              @click:append="toggleConfirmShow"
              background-color="white" />
          </v-card>
          <span class="mt-4 text-body">{{ $t('onboarding.captchaCondition') }}</span>
          <v-card
            class="d-flex mt-4 transparent"
            width="350"
            flat>
            <v-img
              src="/social/rest/login/captcha?name=on-boarding"
              width="150"
              heigh="40"
              class="primary me-2 rounded-lg"
              eager
              contain />
            <v-text-field
              id="captcha"
              v-model="captcha"
              :title="$t('onboarding.captchaPlaceholder')"
              :placeholder="$t('onboarding.captchaPlaceholder')"
              name="captcha"
              class="login-username border-box-sizing pa-0 mt-1"
              aria-required="true"
              type="text"
              required="required"
              outlined
              dense
              background-color="white" />
          </v-card>
        </v-row>
        <v-row class="mx-0 my-8 pa-0">
          <v-btn
            :aria-label="$t('onboarding.save')"
            :disabled="!usernameParam || !password || !confirmPassword || loading"
            :loading="loading"
            width="222"
            max-width="100%"
            color="primary"
            class="login-button btn-primary text-none mx-auto"
            elevation="0"
            type="submit">
            {{ $t('onboarding.save') }}
          </v-btn>
        </v-row>
      </div>
    </form>
  </v-card>
</template>
<script>
const EXTENSION_APP = 'InternalOnboarding';
const EXTENSION_TYPE = 'onboarding-form';

export default {
  props: {
    usernameParam: {
      type: String,
      default: null,
    },
    emailParam: {
      type: String,
      default: null,
    },
    tokenParam: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    password: '',
    confirmPassword: '',
    captcha: '',
    showPassword: false,
    showConfirmPassword: false,
    loading: false,
    extensions: [],
    // The context each extension's component receives, by extension id
    extensionContexts: {},
  }),
  computed: {
    // The account is shown by its email, which signs in as well
    accountIdentifier() {
      return this.emailParam || this.usernameParam;
    },
    accountIdentifierLabel() {
      return this.emailParam ? this.$t('UILoginForm.label.email') : this.$t('portal.login.Username');
    },
    accountIdentifierIcon() {
      return this.emailParam ? 'fas fa-envelope' : 'fas fa-user';
    },
    passwordType() {
      return this.showPassword ? 'text' :'password';
    },
    passwordConfirmType() {
      return this.showConfirmPassword ? 'text' :'password';
    },
  },
  watch: {
    // The extensions' fields are disabled while the password is sent
    loading(loading) {
      Object.values(this.extensionContexts).forEach(context => context.saving = loading);
    },
  },
  created() {
    this.refreshExtensions();
    document.addEventListener(`extension-${EXTENSION_APP}-${EXTENSION_TYPE}-updated`, this.refreshExtensions);
  },
  beforeDestroy() {
    document.removeEventListener(`extension-${EXTENSION_APP}-${EXTENSION_TYPE}-updated`, this.refreshExtensions);
  },
  methods: {
    toggleShow() {
      this.showPassword = !this.showPassword;
    },
    toggleConfirmShow() {
      this.showConfirmPassword = !this.showConfirmPassword;
    },
    refreshExtensions() {
      this.extensions = (extensionRegistry.loadExtensions(EXTENSION_APP, EXTENSION_TYPE) || [])
        .filter(extension => extension?.id && extension.vueComponent);
      const contexts = {...this.extensionContexts};
      this.extensions
        .filter(extension => !contexts[extension.id])
        .forEach(extension => contexts[extension.id] = {
          mode: 'onboard',
          token: this.tokenParam,
          data: null,
          saving: this.loading,
        });
      this.extensionContexts = contexts;
    },
    setExtensionData(extensionId, data) {
      const context = this.extensionContexts[extensionId];
      if (context) {
        context.data = data;
      }
    },
    // Every extension's check; false when one refuses or fails
    validateExtensions() {
      return Promise.all(this.extensions.map(extension => {
        if (!extension.validate) {
          return Promise.resolve(true);
        }
        // Called inside the chain, so that a check throwing at once refuses
        return Promise.resolve()
          .then(() => extension.validate(this.extensionContexts[extension.id]))
          .then(result => result !== false, () => false);
      })).then(results => results.every(Boolean));
    },
    // The values each extension sends with the password, under its own
    // request parameter
    getExtensionParameters() {
      const parameters = {};
      this.extensions
        .filter(extension => extension.requestParameter)
        .forEach(extension => parameters[extension.requestParameter] = JSON.stringify(this.extensionContexts[extension.id]?.data || []));
      return parameters;
    },
    // The form's own constraints are the browser's, checked before submit;
    // then every extension's, before the one request that checks them all and
    // sets the password
    submitForm() {
      if (this.loading) {
        return;
      }
      this.loading = true;
      this.validateExtensions()
        .then(valid => {
          if (valid) {
            return this.setPassword();
          }
          this.loading = false;
        })
        .catch(() => this.loading = false);
    },
    setPassword() {
      return this.$loginService.setPassword(this.usernameParam, this.password, this.confirmPassword, this.tokenParam, this.captcha, this.getExtensionParameters())
        .then(resp => {
          if (!resp.ok) {
            // A used or expired link answers with no body
            return resp.json()
              .catch(() => ({error: resp.status === 404 ? 'onboarding.linkExpired' : 'UILoginForm.label.unknownError'}))
              .then(data => {
                this.$root.$emit('alert-message', this.$t(data?.error || 'UILoginForm.label.unknownError'), 'error');
                this.loading = false;
              });
          } else if (resp.redirected) {
            sessionStorage.setItem('email', this.accountIdentifier);
            window.location.href = resp.url;
          } else {
            this.loading = false;
          }
        })
        .catch(() => {
          // No answer at all: the password may be sent again
          this.$root.$emit('alert-message', this.$t('UILoginForm.label.unknownError'), 'error');
          this.loading = false;
        });
    },
  }
};
</script>
