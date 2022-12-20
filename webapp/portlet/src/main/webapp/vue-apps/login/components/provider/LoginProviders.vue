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
    id="login-extensions"
    flat>
    <template v-if="providers.length">
      <v-card
        class="d-flex mx-auto mt-5"
        :width="mainProvidersWidth"
        flat>
        <component
          v-for="(provider, index) in mainProviders"
          :key="provider.key"
          :provider="provider"
          :rememberme="rememberme"
          :params="params"
          :is="provider.vueComponentName || 'portal-login-provider-link'"
          :display-text="providers.length === 1"
          :class="index === 0 ? 'flex-grow-0' : 'flex-grow-1 text-end'"
          class="mx-auto" />
        <portal-login-providers-menu
          v-if="displayMoreMenu"
          :providers="menuProviders"
          :params="params"
          :rememberme="rememberme"
          key="providerMenu"
          class="flex-grow-1 text-end mx-auto" />
      </v-card>
      <portal-login-separator class="mt-5" />
    </template>
  </v-card>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
    rememberme: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    providers: [],
    extensionName: 'LoginProvider',
    extensionType: 'login-provider',
  }),
  computed: {
    oAuthEnabled() {
      return this.params?.oAuthEnabled;
    },
    oAuthProviderTypes() {
      return this.params?.oAuthProviderTypes || [];
    },
    oAuthProviders() {
      return this.oAuthEnabled && this.params?.oAuthProviderTypes?.map(key => ({
        key,
        url: this.params[`oAuthInitURL-${key}`],
        rank: 0,
      })) || [];
    },
    mainProviders() {
      return this.providers.length > 4 ? this.providers.slice(0, 3) : this.providers;
    },
    menuProviders() {
      return this.providers.length > 4 ? this.providers.slice(3) : [];
    },
    displayMoreMenu() {
      return this.menuProviders.length > 0;
    },
    mainProvidersWidth() {
      return this.mainProviders.length === 2 && this.mainProviders.length * 82 || '';
    },
  },
  created() {
    this.refreshProviders();
    document.addEventListener(`extension-${this.extensionName}-${this.extensionType}-updated`, this.refreshProviders);
  },
  methods: {
    refreshProviders() {
      const providers = this.oAuthProviders.slice();
      const providerExtensions = extensionRegistry.loadExtensions(this.extensionName, this.extensionType);
      if (providerExtensions?.length) {
        providerExtensions.forEach(extension => {
          if (extension.isEnabled && extension.isEnabled(this.params)) {
            providers.push(extension);
          }
        });
      }
      this.providers = providers.sort((p1, p2) => (p2.rank || 0) - (p1.rank || 0));
    },
  },
};
</script>