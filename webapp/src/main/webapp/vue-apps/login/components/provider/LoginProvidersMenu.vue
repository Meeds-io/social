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
    id="login-providers-menu"
    flat>
    <v-menu offset-x offset-y>
      <template #activator="{on, attrs}">
        <v-btn
          :title="$t('UILoginForm.label.moreProviders')"
          :aria-label="$t('UILoginForm.label.moreProviders')"
          min-width="auto"
          color="primary"
          class="pa-4"
          large
          outlined
          v-bind="attrs"
          v-on="on">
          <v-icon size="20">fas fa-ellipsis-h</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <component
          v-for="provider in providers"
          :key="provider.key"
          :provider="provider"
          :rememberme="rememberme"
          :params="params"
          :is="provider.vueComponentName || 'portal-login-provider-menu-link'"
          class="mx-auto"
          display-text
          is-menu />
      </v-list>
    </v-menu>
  </v-card>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
    providers: {
      type: Array,
      default: null,
    },
    rememberme: {
      type: Boolean,
      default: false,
    },
  },
};
</script>