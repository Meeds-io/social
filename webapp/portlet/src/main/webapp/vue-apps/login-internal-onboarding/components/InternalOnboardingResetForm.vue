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
    <v-card-title class="primary--text text-break title px-0">
      {{ $t('onboarding.summary1') }}
    </v-card-title>
    <v-card-title class="primary--text text-break title pa-0">
      {{ $t('onboarding.summary2') }}
    </v-card-title>

    <form
      name="resetPasswordForm"
      method="post"
      autocomplete="off"
      class="d-flex ma-0 flex-column">
      <input
        type="hidden"
        name="action"
        value="resetPassword">
      <div>
        <v-card-title class="px-0 mt-4 text-break title font-weight-bold">
          {{ $t('onboarding.yourPasswordTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card width="350" flat>
            <v-text-field
              id="username"
              v-model="username"
              :title="$t('portal.login.Username')"
              :placeholder="$t('portal.login.Username')"
              name="username"
              prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
              class="login-username border-box-sizing pt-0"
              aria-required="true"
              type="text"
              required="required"
              readonly
              outlined
              dense />
          </v-card>
          <v-card width="350" flat>
            <v-text-field
              id="password"
              v-model="password"
              :title="$t('onboarding.NewPassword')"
              :placeholder="$t('onboarding.NewPassword')"
              :type="passwordType"
              :append-icon="showPassword ? 'fas fa-eye-slash subtitle-1 mt-0' : 'fas fa-eye subtitle-1 mt-0'"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              class="login-password border-box-sizing"
              name="password"
              autocomplete="new-password"
              autofocus="autofocus"
              tabindex="0"
              required="required"
              outlined
              dense
              @click:append="toggleShow" />
          </v-card>
          <span class="caption">{{ $t('onboarding.passwordCondition') }}</span>
          <v-card width="350" flat>
            <v-text-field
              id="password2"
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
              src="/portal/on-boarding?serveCaptcha=true"
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
              dense />
          </v-card>
        </v-row>
        <v-row class="mx-0 my-8 pa-0">
          <v-btn
            :aria-label="$t('onboarding.save')"
            :disabled="!username"
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
    username: '',
    password: '',
    confirmPassword: '',
    captcha: '',
    showPassword: false,
    showConfirmPassword: false,
  }),
  computed: {
    passwordType() {
      return this.showPassword ? 'text' :'password';
    },
    passwordConfirmType() {
      return this.showConfirmPassword ? 'text' :'password';
    },
  },
  mounted() {
    this.username = this.params?.username;
    this.password = this.params?.password;
    this.confirmPassword = this.params?.password2;
  },
  methods: {
    toggleShow() {
      this.showPassword = !this.showPassword;
    },
    toggleConfirmShow() {
      this.showConfirmPassword = !this.showConfirmPassword;
    },
  },
};
</script>