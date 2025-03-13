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
  <form
    autocomplete="off"
    class="d-flex ma-0 flex-column"
    method="post"
    name="registerForm">
    <div class="d-flex flex-column">
      <div class="mb-5 mx-auto text-header primary--text">
        {{ $t('onboarding.emailSummary') }}
      </div>
      <v-row class="ma-0 pa-0">
        <v-text-field
          id="email"
          v-model="email"
          aria-required="true"
          autofocus="autofocus"
          class="login-username border-box-sizing pt-0"
          dense
          name="email"
          outlined
          :placeholder="$t('onboarding.emailPlaceholder')"
          prepend-inner-icon="fas fa-user ms-n2 grey--text text--lighten-1"
          required="required"
          tabindex="0"
          :title="$t('onboarding.emailPlaceholder')"
          type="email" />
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
            src="/portal/register?serveCaptcha=true"
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
      <v-row class="mx-0 mt-4 pa-0">
        <v-btn
          :aria-label="$t('forgotpassword.send')"
          class="mx-auto login-button btn-primary text-none"
          color="primary"
          :disabled="disabled"
          elevation="0"
          max-width="100%"
          type="submit"
          width="222">
          {{ $t('forgotpassword.send') }}
        </v-btn>
      </v-row>
      <v-row class="mx-0 mt-4 pa-0">
        <v-btn
          :aria-label="$t('forgotpassword.back')"
          class="mx-auto login-button text-none"
          elevation="0"
          href="/portal/login"
          max-width="100%"
          outlined
          width="222">
          <span>
            <v-icon
              class="position-absolute mt-n2"
              size="16">fas fa-arrow-left</v-icon>
          </span>
          <span class="mx-auto">
            {{ $t('forgotpassword.backToLogin') }}
          </span>
        </v-btn>
      </v-row>
    </div>
  </form>
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
      email: null,
      captcha: null,
    }),
    computed: {
      disabled () {
        return !this.email?.length
          || !this.captcha?.length;
      },
    },
    mounted () {
      this.email = this.params?.email;
    }, 
  };
</script>