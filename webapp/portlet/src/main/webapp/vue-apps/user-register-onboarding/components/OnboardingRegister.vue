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
  <div v-if="onboardingRegisterEnabled">
    <v-alert
      v-if="onBoardingEmailSent"
      type="success"
      class="position-static my-2 white"
      dense
      outlined
      rounded>
      {{ $t('UILoginForm.label.onboardingEmailSent') }}
    </v-alert>
    <v-alert
      v-if="errorMessage"
      type="error"
      class="position-static my-2 white"
      dense
      outlined
      rounded>
      {{ errorMessage }}
    </v-alert>
    <form
      action="/portal/register"
      method="post"
      class="center">
      <input
        name="onboardingRegister"
        type="hidden"
        :value="true">
      <input
        name="onboardingRegisterToken"
        type="hidden"
        :value="onboardingRegisterToken">
      <input
        id="email"
        :placeholder="$t('UILoginForm.label.email')"
        :value="email"
        name="email"
        tabindex="2"
        type="email"
        class="ps-4 pe-8">
      <v-btn
        :aria-label="$t('portal.register')"
        type="submit"
        tabindex="3"
        class="col-4 secondary"
        elevation="0">
        <span class="text-capitalize">
          {{ $t('UILoginForm.label.Signup') }}
        </span>
      </v-btn>
    </form>
  </div>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
  },
  computed: {
    onboardingRegisterToken() {
      return this.params && this.params.onboardingRegisterToken;
    },
    onboardingRegisterEnabled() {
      return this.params && this.params.onboardingRegisterEnabled;
    },
    onBoardingEmailSent() {
      return this.params && this.params.onBoardingEmailSent;
    },
    errorCode() {
      return this.params && this.params.errorCode;
    },
    errorMessage() {
      if (this.errorCode === 'REGISTRATION_ERROR') {
        return this.$t('UILoginForm.label.unknownError');
      } else if (this.errorCode === 'EMAIL_ALREADY_EXISTS') {
        return this.$t('UILoginForm.label.emailAlreadyExists');
      } else if (this.errorCode === 'USER_ALREADY_EXISTS') {
        return this.$t('UILoginForm.label.usernameAlreadyExists');
      } else if (this.errorCode === 'EMAIL_MANDATORY') {
        return this.$t('UILoginForm.label.emailMandatory');
      }
      return this.errorCode && this.$t(`UILoginForm.label.${this.errorCode}`);
    },
  },
};
</script>