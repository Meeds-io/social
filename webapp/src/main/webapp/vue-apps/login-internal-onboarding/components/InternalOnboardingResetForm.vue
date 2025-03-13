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
    <v-card-title class="primary--text text-break text-header px-0">
      {{ $t('onboarding.summary1') }}
    </v-card-title>
    <v-card-title class="primary--text text-break text-header pa-0">
      {{ $t('onboarding.summary2') }}
    </v-card-title>

    <form
      autocomplete="off"
      class="d-flex ma-0 flex-column"
      method="post"
      name="resetPasswordForm">
      <input
        name="action"
        type="hidden"
        value="resetPassword">
      <div>
        <v-card-title class="px-0 mt-4 text-break text-header">
          {{ $t('onboarding.yourPasswordTitle') }}
        </v-card-title>
        <v-row class="ma-0 pa-0">
          <v-card
            flat
            width="350">
            <v-text-field
              id="username"
              v-model="username"
              aria-required="true"
              class="login-username border-box-sizing pt-0"
              dense
              name="username"
              outlined
              :placeholder="$t('portal.login.Username')"
              prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
              readonly
              required="required"
              :title="$t('portal.login.Username')"
              type="text" />
          </v-card>
          <v-card
            flat
            width="350">
            <v-text-field
              id="password"
              v-model="password"
              :append-icon="showPassword ? 'fas fa-eye-slash text-font-size mt-0' : 'fas fa-eye text-font-size mt-0'"
              autocomplete="new-password"
              autofocus="autofocus"
              class="login-password border-box-sizing"
              dense
              name="password"
              outlined
              :placeholder="$t('onboarding.NewPassword')"
              prepend-inner-icon="fas fa-lock ms-n2 grey--text text--lighten-1"
              required="required"
              tabindex="0"
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
              src="/portal/on-boarding?serveCaptcha=true"
              width="150" />
            <v-text-field
              id="captcha"
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
            :disabled="!username"
            elevation="0"
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
      username: '',
      password: '',
      confirmPassword: '',
      captcha: '',
      showPassword: false,
      showConfirmPassword: false,
    }),
    computed: {
      passwordType () {
        return this.showPassword ? 'text' :'password';
      },
      passwordConfirmType () {
        return this.showConfirmPassword ? 'text' :'password';
      },
    },
    mounted () {
      this.username = this.params?.username;
      this.password = this.params?.password;
      this.confirmPassword = this.params?.password2;
    },
    methods: {
      toggleShow () {
        this.showPassword = !this.showPassword;
      },
      toggleConfirmShow () {
        this.showConfirmPassword = !this.showConfirmPassword;
      },
    },
  };
</script>