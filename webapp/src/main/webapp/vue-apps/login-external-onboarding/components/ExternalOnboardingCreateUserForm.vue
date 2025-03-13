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
    class="mx-auto px-4"
    flat
    max-width="100%"
    width="600px">
    <div class="d-flex flex-column align-center justify-center pt-10 pb-5">
      <v-icon
        color="tertiary"
        size="80">
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
    flat>
    <v-card-title class="primary--text text-break text-header px-0">
      {{ $t('onboarding.summary1') }}
    </v-card-title>
    <v-card-title class="primary--text text-break text-header pa-0">
      {{ $t('onboarding.summary2') }}
    </v-card-title>

    <form
      ref="form"
      autocomplete="off"
      class="d-flex ma-0 flex-column"
      method="post"
      name="form"
      @submit="validateForm()">
      <input
        name="action"
        type="hidden"
        value="saveExternal">
      <input
        name="username"
        type="hidden"
        :value="username">
      <input
        name="initialURI"
        type="hidden"
        :value="initialURI">
      <div>
        <v-card-title class="px-0 mt-4 text-break text-header">
          {{ $t('onboarding.yourProfileTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card
            flat
            width="350">
            <v-text-field
              id="email"
              ref="email"
              v-model="email"
              aria-required="true"
              :autofocus="!isEmailReadOnly && 'autofocus'"
              class="login-username border-box-sizing pt-0"
              dense
              name="email"
              outlined
              :placeholder="$t('onboarding.emailPlaceholder')"
              prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
              :readonly="isEmailReadOnly"
              required="required"
              :tabindex="!isEmailReadOnly && '0'"
              :title="$t('onboarding.emailPlaceholder')"
              type="email" />
            <v-text-field
              id="firstName"
              ref="firstName"
              v-model="firstName"
              aria-required="true"
              :autofocus="isEmailReadOnly && 'autofocus'"
              class="login-username border-box-sizing"
              dense
              maxlength="255"
              minlength="1"
              name="firstName"
              outlined
              :placeholder="$t('onboarding.firstName')"
              required="required"
              :tabindex="isEmailReadOnly && '0'"
              :title="$t('onboarding.firstName')"
              type="text" />
            <v-text-field
              id="lastName"
              ref="lastName"
              v-model="lastName"
              aria-required="true"
              class="login-username border-box-sizing"
              dense
              maxlength="255"
              minlength="1"
              name="lastName"
              outlined
              :placeholder="$t('onboarding.lastName')"
              required="required"
              :title="$t('onboarding.lastName')"
              type="text" />
          </v-card>
        </v-row>
        <v-card-title class="px-0 text-break text-header">
          {{ $t('onboarding.yourPasswordTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card
            flat
            width="350">
            <v-text-field
              id="password"
              ref="password"
              v-model="password"
              :append-icon="showPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
              autocomplete="new-password"
              class="login-password border-box-sizing pt-0"
              dense
              name="password"
              outlined
              :placeholder="$t('onboarding.NewPassword')"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              required="required"
              :title="$t('onboarding.NewPassword')"
              :type="passwordType"
              @click:append="toggleShow" />
          </v-card>
          <span class="caption">{{ $t('onboarding.passwordCondition') }}</span>
          <v-card
            flat
            width="350">
            <v-text-field
              id="password2"
              ref="password2"
              v-model="confirmPassword"
              :append-icon="showConfirmPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
              autocomplete="new-password"
              class="login-password border-box-sizing pt-2"
              dense
              name="password2"
              outlined
              :placeholder="$t('onboarding.ConfirmNewPassword')"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              required="required"
              :title="$t('onboarding.ConfirmNewPassword')"
              :type="passwordConfirmType"
              @click:append="toggleConfirmShow" />
          </v-card>
          <span class="mt-4">{{ $t('onboarding.captchaCondition') }}</span>
          <v-card
            class="d-flex mt-4"
            flat
            width="350">
            <v-img
              class="primary me-2 rounded-lg"
              contain
              eager
              heigh="40"
              src="/portal/external-registration?serveCaptcha=true"
              width="150" />
            <v-text-field
              id="captcha"
              ref="captcha"
              v-model="captcha"
              aria-required="true"
              class="login-username border-box-sizing pa-0 mt-1"
              dense
              name="captcha"
              outlined
              :placeholder="$t('onboarding.captchaPlaceholder')"
              required="required"
              :title="$t('onboarding.captchaPlaceholder')"
              type="text" />
          </v-card>
        </v-row>
        <v-row class="mx-0 my-8 pa-0">
          <v-btn
            :aria-label="$t('onboarding.save')"
            class="login-button btn-primary text-none mx-auto"
            color="primary"
            :disabled="disabled"
            elevation="0"
            :loading="loading"
            max-width="100%"
            type="submit"
            width="222">
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
      passwordType () {
        return this.showPassword ? 'text' :'password';
      },
      passwordConfirmType () {
        return this.showConfirmPassword ? 'text' :'password';
      },
      isEmailReadOnly () {
        return !this.username;
      },
      disabled () {
        return !this.email?.length
          || !this.firstName?.length
          || !this.lastName?.length
          || !this.password?.length
          || !this.confirmPassword?.length
          || !this.captcha?.length;
      },
      confirmed () {
        return !!this.params?.success?.length;
      },
    },
    watch: {
      errorField () {
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
    mounted () {
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
      toggleShow () {
        this.showPassword = !this.showPassword;
      },
      toggleConfirmShow () {
        this.showConfirmPassword = !this.showConfirmPassword;
      },
      validateForm () {
        this.loading = this.$refs.form.reportValidity();
        window.setTimeout(() => this.loading = false, 10000);
        return !this.disabled;
      },
    },
  };
</script>