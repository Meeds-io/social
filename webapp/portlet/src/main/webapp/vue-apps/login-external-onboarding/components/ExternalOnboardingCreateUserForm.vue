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
    class="mx-auto px-4"
    flat>
    <div class="d-flex flex-column align-center justify-center pt-10 pb-5">
      <v-icon size="80" color="primary">
        fa-envelope
      </v-icon>
      <span class="subtitle-1 mt-8 text-color">
        {{ $t('UILoginForm.label.confirmationEmailSentPart1') }}
      </span>
      <span class="subtitle-1 mt-3 text-color">
        {{ $t('UILoginForm.label.confirmationEmailSentPart2') }}
      </span>
    </div>
  </v-card>
  <v-card
    v-else
    flat>
    <v-card-title class="primary--text text-break title px-0">
      {{ $t('onboarding.summary1') }}
    </v-card-title>
    <v-card-title class="primary--text text-break title pa-0">
      {{ $t('onboarding.summary2') }}
    </v-card-title>

    <form
      name="form"
      ref="form"
      method="post"
      autocomplete="off"
      class="d-flex ma-0 flex-column"
      @submit="validateForm()">
      <input
        type="hidden"
        name="action"
        value="saveExternal">
      <input
        type="hidden"
        :value="username"
        name="username">
      <input
        type="hidden"
        :value="initialURI"
        name="initialURI">
      <div>
        <v-card-title class="px-0 mt-4 text-break title font-weight-bold">
          {{ $t('onboarding.yourProfileTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card width="350" flat>
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
          </v-card>
        </v-row>
        <v-card-title class="px-0 text-break title font-weight-bold">
          {{ $t('onboarding.yourPasswordTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card width="350" flat>
            <v-text-field
              id="password"
              ref="password"
              v-model="password"
              :title="$t('onboarding.NewPassword')"
              :placeholder="$t('onboarding.NewPassword')"
              :type="passwordType"
              :append-icon="showPassword ? 'fas fa-eye-slash subtitle-1 mt-0' : 'fas fa-eye subtitle-1 mt-0'"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              class="login-password border-box-sizing pt-0"
              name="password"
              autocomplete="new-password"
              required="required"
              outlined
              dense
              @click:append="toggleShow" />
          </v-card>
          <span class="caption">{{ $t('onboarding.passwordCondition') }}</span>
          <v-card width="350" flat>
            <v-text-field
              id="password2"
              ref="password2"
              v-model="confirmPassword"
              :title="$t('onboarding.ConfirmNewPassword')"
              :placeholder="$t('onboarding.ConfirmNewPassword')"
              :type="passwordConfirmType"
              :append-icon="showConfirmPassword ? 'fas fa-eye-slash subtitle-1 mt-0' : 'fas fa-eye subtitle-1 mt-0'"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              class="login-password border-box-sizing pt-2"
              name="password2"
              autocomplete="new-password"
              required="required"
              outlined
              dense
              @click:append="toggleConfirmShow" />
          </v-card>
          <span class="mt-4">{{ $t('onboarding.captchaCondition') }}</span>
          <v-card
            class="d-flex mt-4"
            width="350"
            flat>
            <v-img
              src="/portal/external-registration?serveCaptcha=true"
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
            type="submit"
            width="222"
            max-width="100%"
            color="primary"
            class="login-button btn-primary text-none mx-auto"
            elevation="0">
            {{ $t('onboarding.save') }}
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
  }),
  computed: {
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
      return !!this.params?.success?.length;
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
  },
  mounted() {
    this.username = this.params?.username;
    this.email = this.params?.email;
    this.firstName = this.params?.firstName;
    this.lastName = this.params?.lastName;
    this.password = this.params?.password;
    this.confirmPassword = this.params?.password2;
    this.initialURI = this.params?.initialURI;
    this.errorField = this.params?.errorField;
    this.error = this.params?.error;
  },
  methods: {
    toggleShow() {
      this.showPassword = !this.showPassword;
    },
    toggleConfirmShow() {
      this.showConfirmPassword = !this.showConfirmPassword;
    },
    validateForm() {
      this.loading = this.$refs.form.reportValidity();
      window.setTimeout(() => this.loading = false, 10000);
      return !this.disabled;
    },
  },
};
</script>