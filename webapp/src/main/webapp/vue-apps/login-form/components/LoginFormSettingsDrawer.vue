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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl"
    :loading="loading"
    eager
    @closed="reset">
    <template slot="title">
      {{ $t('loginForm.drawer.settings.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card
        class="ma-4"
        flat>
        <div class="text-header mb-4">
          {{ $t('loginForm.drawer.label.welcome.title') }}
        </div>
        <div v-if="!registerEnabled" class="mb-7">
          <v-card-text class="d-flex pa-0">
            <translation-text-field
              ref="welcomeBackTranslations"
              :object-id="translationIdentifier"
              :object-type="objectType"
              field-name="welcomeBack"
              :field-value="displayedValueWelcomeBack"
              class="width-auto flex-grow-1"
              drawer-title="loginForm.drawer.label.welcome.title"
              @input="translationUpdatedWelcomeBack" />
          </v-card-text>
        </div>
        <div v-else class="mb-7">
          <v-card-text class="d-flex pa-0 mb-3">
            <translation-text-field
              ref="newHereTranslations"
              :object-id="translationIdentifier"
              :object-type="objectType"
              field-name="newHere"
              :field-value="displayedValueNewHere"
              class="width-auto flex-grow-1"
              drawer-title="loginForm.drawer.label.newHere.title"
              @input="translationUpdatedNewHere" />
          </v-card-text>
          <v-card-text class="d-flex pa-0">
            <translation-text-field
              ref="createAccountTranslations"
              :object-id="translationIdentifier"
              :object-type="objectType"
              field-name="createAccount"
              :field-value="displayedValueCreateAccount"
              class="width-auto flex-grow-1"
              drawer-title="loginForm.drawer.label.createAccount.title"
              @input="translationUpdatedCreateAccount" />
          </v-card-text>
        </div>
        <div v-if="providersCount > 0" class="mb-7">
          <div class="text-header mb-4">
            {{ $t('loginForm.drawer.label.signinoptions.title') }}
          </div>
          <v-radio-group v-model="signinOption">
            <v-radio :label="$t('loginForm.drawer.label.signinoptions.loginForm')" value="loginform" />
            <v-radio :label="$t('loginForm.drawer.label.signinoptions.noForm')" value="noform" />
            <v-radio :label="$t('loginForm.drawer.label.signinoptions.button')" value="buttonform" />
          </v-radio-group>
          <div v-if="signinOption === 'buttonform'" class="mb-4">
            <v-card-text class="d-flex pa-0">
              <translation-text-field
                ref="signinEmailButtonTranslations"
                :object-id="translationIdentifier"
                :object-type="objectType"
                field-name="signinEmailButton"
                :field-value="displayedValueSigninEmailButton"
                class="width-auto flex-grow-1"
                drawer-title="loginForm.drawer.label.signinEmailButton.title"
                @input="translationUpdatedSigninEmailButton" />
            </v-card-text>
            <v-card-text class="d-flex pa-0 align-center">
              {{ $t('loginForm.drawer.label.signinoptions.displayIcon') }}
              <div class="spacer" />
              <v-switch
                v-model="displaySigninEmailButtonIcon"
                class="mt-0" />
            </v-card-text>

          </div>
        </div>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          :disabled="loading"
          :title="$t('loginForm.drawer.settings.cancel')"
          class="btn ms-auto me-2"
          @click="close()">
          {{ $t('loginForm.drawer.settings.cancel') }}
        </v-btn>
        <v-btn
          :loading="loading"
          :title="$t('loginForm.drawer.settings.save')"
          color="primary"
          elevation="0"
          @click="save()">
          {{ $t('loginForm.drawer.settings.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    translationIdentifier: {
      type: String,
      default: '',
    },
    registerEnabled: {
      type: Boolean,
      default: true,
    },
    signinOption: {
      type: String,
      default: 'loginform',
    },
    providersCount: {
      type: Number,
      default: 0,
    },
    displaySigninEmailButtonIcon: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    objectType: 'cmsPortlet',
    translationsWelcomeBack: [],
    translationsNewHere: [],
    translationsCreateAccount: [],
    translationsSigninEmailButton: [],
  }),
  created() {
    this.$root.$on('login-form-settings', this.open);
  },
  beforeDestroy() {
    this.$root.$off('login-form-settings', this.open);
  },
  computed: {
    displayedValueWelcomeBack() {
      return this.translationsWelcomeBack?.[this.userLocale] || this.defaultLangValueWelcomeBack;
    },
    defaultLangValueWelcomeBack() {
      return this.$t('portal.login.WelcomeBack');
    },
    displayedValueNewHere() {
      return this.translationsNewHere?.[this.userLocale] || this.defaultLangValueNewHere;
    },
    defaultLangValueNewHere() {
      return this.$t('UILoginForm.label.registerNewAccount');
    },
    displayedValueCreateAccount() {
      return this.translationsCreateAccount?.[this.userLocale] || this.defaultLangValueCreateAccount;
    },
    defaultLangValueCreateAccount() {
      return this.$t('UILoginForm.button.registerNewAccount');
    },
    displayedValueSigninEmailButton() {
      return this.translationsSigninEmailButton?.[this.userLocale] || this.defaultLangValueSigninEmailButton;
    },
    defaultLangValueSigninEmailButton() {
      return this.$t('portal.login.SigninUsingEmail');
    }
  },
  methods: {
    open() {
      this.reset();
      this.$refs.drawer.open();
    },
    reset() {
      this.loading = false;
    },
    close() {
      this.$refs.drawer.close();
    },
    save() {
      this.loading = true;
      const promise = [];


      if (this.registerEnabled) {
        promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'newHere', this.translationsNewHere));
        promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'createAccount', this.translationsCreateAccount));
      } else {
        promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'welcomeBack', this.translationsWelcomeBack));
      }

      if (this.signinOption === 'buttonform') {
        promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'signinEmailButton', this.translationsSigninEmailButton));
      }

      promise.push(this.saveSettings());

      Promise.all(promise).then(() => {
        this.$root.$emit('login-form-settings-updated',
          this.translationsWelcomeBack,
          this.translationsNewHere,
          this.translationsCreateAccount,
          this.translationsSigninEmailButton,
          this.signinOption,
          this.displaySigninEmailButtonIcon);
        this.loading = false;
        this.close();
      });
    },
    saveSettings() {
      const formData = new FormData();
      formData.append('pageRef', this.$root.pageRef);
      formData.append('applicationId', this.$root.portletStorageId);
      const params = new URLSearchParams(formData).toString();
      return fetch(`/layout/rest/pages/application/preferences?${params}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          preferences: [{
            name: 'signinOption',
            value: this.signinOption || 'loginForm',
          }, {
            name: 'displaySigninEmailButtonIcon',
            value: this.displaySigninEmailButtonIcon,
          }],
        }),
      });
    },
    translationUpdatedWelcomeBack(translations) {
      this.translationsWelcomeBack = translations;
    },
    translationUpdatedNewHere(translations) {
      this.translationsNewHere = translations;
    },
    translationUpdatedCreateAccount(translations) {
      this.translationsCreateAccount = translations;
    },
    translationUpdatedSigninEmailButton(translations) {
      this.translationsSigninEmailButton = translations;
    },
  },
};
</script>
