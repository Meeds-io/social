<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
    id="defaultSpacesRegistrationDrawer"
    v-model="drawer"
    right
    disable-pull-to-refresh>
    <template #title>
      {{ $t('generalSettings.managePublicSite') }}
    </template>
    <template #content>
      <v-card class="pa-4" flat>
        <div class="text-color text-subtitle-1">
          {{ $t('generalSettings.enablePublicSiteDescription') }}
        </div>
        <v-list-item class="px-0" two-line>
          <v-list-item-content>
            <v-list-item-title>
              <h4 class="my-0 text-font-size text-color">{{ $t('generalSettings.makePublicSiteVisible') }}</h4>
            </v-list-item-title>
            <v-list-item-subtitle class="caption">
              {{ $t('generalSettings.subtitle.makePublicSiteVisible') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-tooltip :disabled="$root.isMobile" bottom>
              <template #activator="{on, bind}">
                <div
                  v-on="on"
                  v-bind="bind">
                  <v-switch
                    v-model="publicSiteVisible"
                    :loading="loading"
                    class="my-auto"
                    hide-details
                    @click="switchSiteMode" />
                </div>
              </template>
              <span>{{ publicSiteVisible && $t('generalSettings.hidePublicSite') || $t('generalSettings.makeSitePublicallyVisible') }}</span>
            </v-tooltip>
          </v-list-item-action>
        </v-list-item>
        <v-list-item class="px-0" two-line>
          <v-list-item-content>
            <v-list-item-title>
              <h4 class="my-0 text-font-size text-color">{{ $t('generalSettings.editPublicSite') }}</h4>
            </v-list-item-title>
            <v-list-item-subtitle class="caption">
              {{ $t('generalSettings.subtitle.editPublicSite') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-tooltip :disabled="$root.isMobile" bottom>
              <template #activator="{on, bind}">
                <v-btn
                  v-on="on"
                  v-bind="bind"
                  icon
                  @click="copyAddress">
                  <v-icon size="18" class="icon-default-color">fa-clone</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('generalSettings.copyPublicSiteUrl') }}</span>
            </v-tooltip>
          </v-list-item-action>
          <v-list-item-action class="ms-2">
            <v-tooltip :disabled="$root.isMobile" bottom>
              <template #activator="{on, bind}">
                <v-btn
                  v-on="on"
                  v-bind="bind"
                  :href="publicSiteLink"
                  target="_blank"
                  icon>
                  <v-icon size="18" class="icon-default-color">fa-external-link-alt</v-icon>
                </v-btn>
              </template>
              <span>{{ $t('generalSettings.openPublicSite') }}</span>
            </v-tooltip>
          </v-list-item-action>
        </v-list-item>
      </v-card>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    loading: false,
    publicSiteVisible: false,
    publicSiteLink: '/portal/public',
  }),
  created() {
    this.publicSiteVisible = this.$root.publicSiteVisible;
    this.$root.$on('public-site-edit', this.open);
  },
  methods: {
    open() {
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    switchSiteMode() {
      this.loading = true;
      const formData = new FormData();
      formData.append('name', 'accessPermissions');
      formData.append('value', this.publicSiteVisible && 'Everyone' || '*:/platform/administrators,publisher:/platform/web-contributors');
      const params = new URLSearchParams(formData).toString();
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/sites/${this.$root.publicSiteId}`, {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        method: 'PATCH',
        credentials: 'include',
        body: params,
      })
        .then(resp => {
          if (resp?.ok) {
            this.$root.$emit('alert-message', this.$t('generalSettings.publicSiteUpdatedSuccessfully'), 'success');
          } else {
            this.publicSiteVisible = !this.publicSiteVisible;
            this.$root.$emit('alert-message', this.$t('generalSettings.publicSiteUpdateError'), 'error');
          }
        })
        .finally(() => this.loading = false);
    },
    copyAddress() {
      try {
        navigator.clipboard.writeText(`${window.location.origin}${this.publicSiteLink}`);
        this.$root.$emit('alert-message', this.$t('generalSettings.publicSiteUrlCopiedSuccessfully'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('generalSettings.publicSiteUrlCopiedError'), 'warning');
      }
    }
  }
};
</script>