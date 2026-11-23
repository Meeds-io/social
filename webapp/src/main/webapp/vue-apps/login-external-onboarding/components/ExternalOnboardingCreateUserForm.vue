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
    v-if="confirmed"
    width="600px"
    max-width="100%"
    class="mx-auto px-4 transparent"
    flat>
    <div class="d-flex flex-column align-center justify-center pt-10 pb-5">
      <v-icon size="80" color="secondary">
        fa-envelope
      </v-icon>
      <span class="mt-8">
        {{ $t('UILoginForm.label.confirmationEmailSentPart1') }}
      </span>
      <span class="mt-3">
        {{ $t('UILoginForm.label.confirmationEmailSentPart2') }}
      </span>
    </div>
  </v-card>
  <v-card
    v-else
    flat
    class="transparent">
    <v-card-title class="text-break text-body px-0">
      {{ $t('onboarding.summary1') }}
    </v-card-title>
    <v-card-title class="text-break text-body pa-0">
      {{ $t('onboarding.summary2') }}
    </v-card-title>

    <form
      name="form"
      ref="form"
      method="post"
      autocomplete="off"
      class="d-flex ma-0 flex-column"
      @submit.prevent="validateForm()">
      <div>
        <v-card-title class="px-0 mt-4 text-break text-header">
          {{ $t('onboarding.yourProfileTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card
            width="350"
            flat
            class="transparent">
            <v-text-field
              id="email"
              ref="email"
              v-model="email"
              :title="$t('onboarding.emailPlaceholder')"
              :readonly="isEmailReadOnly"
              :tabindex="!isEmailReadOnly && '0'"
              :autofocus="!isEmailReadOnly && 'autofocus'"
              :placeholder="$t('onboarding.emailPlaceholder')"
              name="email"
              prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
              class="login-username border-box-sizing pt-0"
              aria-required="true"
              type="email"
              required="required"
              outlined
              dense />
            <v-text-field
              id="firstName"
              ref="firstName"
              v-model="firstName"
              :title="$t('onboarding.firstName')"
              :placeholder="$t('onboarding.firstName')"
              :tabindex="isEmailReadOnly && '0'"
              :autofocus="isEmailReadOnly && 'autofocus'"
              name="firstName"
              class="login-username border-box-sizing"
              aria-required="true"
              type="text"
              required="required"
              minlength="1"
              maxlength="255"
              outlined
              dense />
            <v-text-field
              id="lastName"
              ref="lastName"
              v-model="lastName"
              :title="$t('onboarding.lastName')"
              :placeholder="$t('onboarding.lastName')"
              name="lastName"
              class="login-username border-box-sizing"
              aria-required="true"
              type="text"
              required="required"
              minlength="1"
              maxlength="255"
              outlined
              dense />
            <div id="externalOnboardingAdditionalFields">
              <component
                :is="extension.vueComponent"
                v-for="extension in extensions"
                :key="extension.id"
                :context="extensionContexts[extension.id]"
                @update:data="setExtensionData(extension.id, $event)" />
            </div>
          </v-card>
        </v-row>
        <v-card-title class="px-0 text-break text-header">
          {{ $t('onboarding.yourPasswordTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card
            width="350"
            flat
            class="transparent">
            <v-text-field
              id="password"
              ref="password"
              v-model="password"
              :title="$t('onboarding.NewPassword')"
              :placeholder="$t('onboarding.NewPassword')"
              :type="passwordType"
              :append-icon="showPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              class="login-password border-box-sizing pt-0"
              name="password"
              autocomplete="new-password"
              required="required"
              outlined
              dense
              @click:append="toggleShow" />
          </v-card>
          <span class="text-subtitle">{{ $t('onboarding.passwordCondition') }}</span>
          <v-card
            width="350"
            flat
            class="transparent">
            <v-text-field
              id="password2"
              ref="password2"
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
              @click:append="toggleConfirmShow" />
          </v-card>
          <span class="mt-4 text-body">{{ $t('onboarding.captchaCondition') }}</span>
          <v-card
            class="d-flex mt-4 transparent"
            width="350"
            flat>
            <v-img
              src="/social/rest/login/captcha?name=external-registration"
              width="150"
              heigh="40"
              class="primary me-2 rounded-lg"
              eager
              contain />
            <v-text-field
              id="captcha"
              ref="captcha"
              v-model="captcha"
              :title="$t('onboarding.captchaPlaceholder')"
              :placeholder="$t('onboarding.captchaPlaceholder')"
              name="captcha"
              class="login-username border-box-sizing pa-0 mt-1"
              aria-required="true"
              type="text"
              required="required"
              outlined
              dense />
          </v-card>
        </v-row>
        <v-row class="mx-0 my-8 pa-0">
          <v-btn
            :aria-label="$t('onboarding.save')"
            :disabled="disabled"
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
const EXTENSION_APP = 'ExternalOnboarding';
const EXTENSION_TYPE = 'register-form';

export default {
  props: {
    identifier: {
      type: String,
      default: null,
    },
    tokenParam: {
      type: String,
      default: null,
    },
    initialUri: {
      type: String,
      default: null
    }
  },
  data: () => ({
    username: null,
    email: null,
    firstName: null,
    lastName: null,
    password: null,
    confirmPassword: null,
    captcha: null,
    initialURI: null,
    showPassword: false,
    showConfirmPassword: false,
    loading: false,
    error: null,
    errorField: null,
    success: false,
    extensions: [],
    // The context each extension's component receives, by extension id
    extensionContexts: {},
  }),
  computed: {
    // A token naming an e-mail registers the account; one naming a username
    // goes through the e-mail verification, which sends no additional field
    branch() {
      return this.identifier && !this.identifier.includes('@') ? 'verifyEmail' : 'register';
    },
    passwordType() {
      return this.showPassword ? 'text' :'password';
    },
    passwordConfirmType() {
      return this.showConfirmPassword ? 'text' :'password';
    },
    isEmailReadOnly() {
      return !this.username;
    },
    disabled() {
      return !this.email?.length
        || !this.firstName?.length
        || !this.lastName?.length
        || !this.password?.length
        || !this.confirmPassword?.length
        || !this.captcha?.length;
    },
    confirmed() {
      return this.success;
    },
  },
  watch: {
    errorField() {
      if (this.error && this.errorField) {
        const element = this.$refs[this.errorField].setCustomValidity && this.$refs[this.errorField] || this.$refs[this.errorField].$el.querySelector('input');
        element.setCustomValidity(this.error);
        window.setTimeout(() => this.$refs.form.reportValidity(), 200);
        element.onkeydown = () => {
          element.setCustomValidity('');
        };
      }
    },
    // The extensions' fields are disabled while the registration is sent
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
  mounted() {
    if (this.identifier && this.identifier.includes('@')) {
      this.email = this.identifier;
    } else {
      this.username = this.identifier;
    }
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
          mode: 'register',
          token: this.tokenParam,
          branch: this.branch,
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
    // The values each extension sends with the registration, under its own
    // request parameter
    getExtensionParameters() {
      const parameters = {};
      this.extensions
        .filter(extension => extension.requestParameter)
        .forEach(extension => parameters[extension.requestParameter] = JSON.stringify(this.extensionContexts[extension.id]?.data || []));
      return parameters;
    },
    async register() {
      try {
        const resp = await this.$loginService.registerWithFields({
          email: this.email,
          firstName: this.firstName,
          lastName: this.lastName,
          password: this.password,
          confirmPassword: this.confirmPassword,
          token: this.tokenParam,
          captcha: this.captcha,
          additionalParameters: this.getExtensionParameters(),
        });
        if (resp?.ok) {
          if (resp.redirected) {
            sessionStorage.setItem('email',this.email);

            const redirectUrl = new URL(resp.url, window.location.origin);
            if (this.initialUri) {
              redirectUrl.searchParams.set('initialURI', this.initialUri);
            }
            window.location.href = redirectUrl.toString();
          } else {
            this.success = true;
          }
          this.loading=false;
          return;
        }
        // A used or expired token answers with no body
        const data = await resp.json()
          .catch(() => ({error: resp.status === 404 ? 'onboarding.linkExpired' : 'UILoginForm.label.unknownError'}));
        this.error = data.error;
        this.errorField = data.errorField;
        if (!this.errorField) {
          this.$root.$emit('alert-message', this.$t(this.error), 'error');
        }
        this.loading = false;
      } catch {
        // No answer at all: the registration may be sent again
        this.$root.$emit('alert-message', this.$t('UILoginForm.label.unknownError'), 'error');
        this.loading = false;
      }
    },
    validateForm() {
      this.loading = this.$refs.form.reportValidity();
      if (!this.disabled) {
        if (!this.username) {
          // The form's own constraints, then every extension's, before the
          // one request that checks them all and creates the account
          if (!this.loading) {
            return false;
          }
          this.validateExtensions().then(async valid => {
            if (valid) {
              await this.register();
            } else {
              this.loading = false;
            }
          }).catch(() => this.loading = false);
        } else {
          this.$loginService.sendEmailVerificationEmail(this.email, this.firstName, this.lastName, this.password, this.confirmPassword, this.tokenParam, this.captcha, this.username).then((resp) => {
            if (!resp || !resp.ok) {
              resp.json().then((data) => {
                this.error = data.error;
                this.errorField = data.errorField;
                if (!this.errorField) {
                  this.$root.$emit('alert-message', this.error, 'error');
                }
                this.loading = false;
              });
            } else {
              if (resp.redirected) {
                window.location.href = resp.url;
              } else {
                this.success = true;
              }
              this.loading=false;
            }
          });
        }
      } else {
        this.loading=false;
        return false;
      }
    },
  }
};
</script>
