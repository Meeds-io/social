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
        <v-card-text class="d-flex pa-0">
          <div class="text-header mb-4">
            {{ $t('loginForm.drawer.label.welcome.title') }}
          </div>
          <div class="spacer" />
          <v-switch
            v-model="displayWelcomeMessage"
            class="mt-0" />
        </v-card-text>
        <div v-if="!registerEnabled && displayWelcomeMessage" class="mb-7">
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
        <div v-else-if="displayWelcomeMessage" class="mb-7">
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
        <div v-if="providers.length > 0" class="mb-7">
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
        <div v-if="providers.length > 0" class="mb-7">
          <div class="text-header mb-4">
            {{ $t('loginForm.drawer.label.externalsigninoptions.title') }}
          </div>
          <v-card-text class="d-flex pa-0 align-center">
            {{ $t('loginForm.drawer.label.externalsigninoptions.listProviders') }}
            <div class="spacer" />
            <v-switch
              v-model="listExternalProviders"
              class="mt-0" />
          </v-card-text>
          <div v-if="listExternalProviders">
            <v-card-text class="d-flex pa-0 mb-3"
              v-for="provider in providers"
              :key="provider.key">
              <translation-text-field
                :ref="`${provider.key}ProviderTranslations`"
                :object-id="translationIdentifier"
                :object-type="objectType"
                :field-name="`${provider.key}`"
                :field-value="providerDisplayedValue(provider)"
                class="width-auto flex-grow-1"
                :drawer-title="providerKeyCapitalize(provider.key)"
                @input="translations => translationUpdatedForProvider(provider, translations)" />
            </v-card-text>
            <div v-if="providers.length==1">
              <v-card-text class="d-flex pa-0 align-center">
                {{ $t('loginForm.drawer.label.signinoptions.displayIcon') }}
                <div class="spacer" />
                <v-switch
                  v-model="displayProvidersIcons"
                  class="mt-0" />
              </v-card-text>
            </div>
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
    listExternalProviders: {
      type: Boolean,
      default: true,
    },
    displayWelcomeMessage: {
      type: Boolean,
      default: true,
    },
    displayProvidersIcons: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    objectType: 'cmsPortlet',
    translationsWelcomeBack: {},
    translationsNewHere: {},
    translationsCreateAccount: {},
    translationsSigninEmailButton: {},
    providers: [],
  }),
  created() {
    this.$root.$on('login-form-settings', this.open);
    this.$root.$on('login-providers-refreshed', (providers) => {
      this.providers = providers;
      providers.forEach((provider) => {
        provider.translations = {};
      });
    });
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
      let firstTranslation = true;

      //if we have saveTranslations request, we need to ensure that the first one is executed before the others
      //because it create the metadata for the translations
      //if all saveTranslations are executed at the same time, we will have a "unique constraint violation" error because metadata will be created more than once

      if (this.registerEnabled) {
        if (firstTranslation) {
          this.saveTranslationSynchronously(this.objectType, this.translationIdentifier, 'newHere', this.translationsNewHere);
          firstTranslation = false;
        } else {
          promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'newHere', this.translationsNewHere));
        }
        promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'createAccount', this.translationsCreateAccount));
      } else {
        if (firstTranslation) {
          this.saveTranslationSynchronously(this.objectType, this.translationIdentifier, 'welcomeBack', this.translationsWelcomeBack);
          firstTranslation = false;
        } else {
          promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'welcomeBack', this.translationsWelcomeBack));
        }
      }

      if (this.signinOption === 'buttonform') {
        if (firstTranslation) {
          this.saveTranslationSynchronously(this.objectType, this.translationIdentifier, 'signinEmailButton', this.translationsSigninEmailButton);
          firstTranslation = false;
        } else {
          promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, 'signinEmailButton', this.translationsSigninEmailButton));
        }
      }

      this.providers.forEach((provider) => {
        if (provider.translations && Object.keys(provider.translations).length > 0) {
          if (firstTranslation) {
            this.saveTranslationSynchronously(this.objectType, this.translationIdentifier, provider.key, provider.translations);
            firstTranslation = false;
          } else {
            promise.push(this.$translationService.saveTranslations(this.objectType, this.translationIdentifier, provider.key, provider.translations));
          }
        }
      });

      promise.push(this.saveSettings());
      Promise.all(promise).then(() => {
        this.$root.$emit('login-form-settings-updated',
          this.translationsWelcomeBack,
          this.translationsNewHere,
          this.translationsCreateAccount,
          this.translationsSigninEmailButton,
          this.signinOption,
          this.displaySigninEmailButtonIcon,
          this.listExternalProviders,
          this.displayProvidersIcons,
          this.displayWelcomeMessage);
        this.loading = false;
        this.close();
      });
    },
    async saveTranslationSynchronously(objectType, objectId, fieldName, translations) {
      await this.$translationService.saveTranslations(objectType, objectId, fieldName, translations);
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
          }, {
            name: 'listExternalProviders',
            value: this.listExternalProviders,
          }, {
            name: 'displayProvidersIcons',
            value: this.displayProvidersIcons,
          }, {
            name: 'displayWelcomeMessage',
            value: this.displayWelcomeMessage,
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
    translationUpdatedForProvider(provider, translations) {
      provider.translations = translations;
    },
    providerDisplayedValue(provider) {
      return provider.translations?.[this.userLocale] || this.defaultLangValueProvider(provider);
    },
    defaultLangValueProvider(provider) {
      return this.providerButtonLabel(provider.key);
    },
    providerButtonLabel(providerName) {
      const translatedProviderName = this.$te(`UILoginForm.label.provider.${providerName.toLowerCase()}`)
        ? this.$t(`UILoginForm.label.provider.${providerName.toLowerCase()}`)
        : this.providerKeyCapitalize(providerName);
      return this.$t('UILoginForm.label.singInWith', {0: translatedProviderName});
    },
    providerKeyCapitalize(providerName) {
      return `${providerName.toLowerCase().charAt(0).toUpperCase()}${providerName.toLowerCase().substring(1)}`;
    },
  },
};
</script>
