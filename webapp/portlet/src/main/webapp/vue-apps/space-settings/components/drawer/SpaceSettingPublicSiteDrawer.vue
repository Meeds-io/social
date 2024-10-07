<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    id="SpaceSettingsPublicSiteDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    right>
    <template #title>
      {{ $t('SpaceSettings.publicSite.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div class="d-flex flex-column ma-4">
        <div class="mb-4">
          {{ $t('SpaceSettings.publicSite.drawer.summary') }}
        </div>
        <template v-if="publicSiteId">
          <div class="text-header">
            {{ $t('SpaceSettings.publicSite.makeItVisible') }}
          </div>
          <v-radio-group
            v-model="publicSiteVisibility"
            class="mt-2 ms-n1"
            mandatory
            inset
            @change="saveSpacePublicSite">
            <v-radio
              value="manager"
              class="mt-0 mb-2">
              <template #label>
                <div class="text-body">
                  {{ $t('SpaceSettings.publicSite.drawer.accessChoiceAdmins') }}
                </div>
              </template>
            </v-radio>
            <v-radio
              value="member"
              class="mt-0 mb-2">
              <template #label>
                <div class="text-body">
                  {{ $t('SpaceSettings.publicSite.drawer.accessChoiceMembers') }}
                </div>
              </template>
            </v-radio>
            <v-tooltip :disabled="!isHiddenSpace" bottom>
              <template #activator="{on, attrs}">
                <div v-on="on" v-bind="attrs">
                  <v-radio
                    :disabled="isHiddenSpace"
                    value="internal"
                    class="mt-0 mb-2">
                    <template #label>
                      <div :class="isHiddenSpace && 'text--disabled' || 'text-body'">
                        {{ $t('SpaceSettings.publicSite.drawer.accessChoiceUsersNoGuests') }}
                      </div>
                    </template>
                  </v-radio>
                  <v-radio
                    :disabled="isHiddenSpace"
                    value="authenticated"
                    class="mt-0 mb-2">
                    <template #label>
                      <div :class="isHiddenSpace && 'text--disabled' || 'text-body'">
                        {{ $t('SpaceSettings.publicSite.drawer.accessChoiceUsersWithGuests') }}
                      </div>
                    </template>
                  </v-radio>
                  <v-radio
                    :disabled="isHiddenSpace"
                    value="everyone"
                    class="mt-0 mb-2">
                    <template #label>
                      <div :class="isHiddenSpace && 'text--disabled' || 'text-body'">
                        {{ $t('SpaceSettings.publicSite.drawer.accessChoiceAnonymous') }}
                      </div>
                    </template>
                  </v-radio>
                </div>
              </template>
              <span>{{ $t('SpaceSettings.publicSite.visibilityNotAllowDueToHiddenSpace') }}</span>
            </v-tooltip>
          </v-radio-group>
          <div class="text-header my-1">
            {{ $t('SpaceSettings.publicSite.editPublicSite') }}
          </div>
          <v-list-item class="px-0" dense>
            <v-list-item-content>
              <v-list-item-title class="text-body">
                {{ $t('SpaceSettings.publicSite.drawer.editContent') }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action class="my-auto ms-2">
              <v-btn
                :href="publicSiteLink"
                target="_blank"
                icon>
                <v-icon size="18">fa-edit</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
          <v-list-item class="px-0" dense>
            <v-list-item-content>
              <v-list-item-title class="text-body">
                {{ $t('SpaceSettings.publicSite.drawer.copyLink') }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action class="my-auto ms-2">
              <v-tooltip :disabled="$root.isMobile" bottom>
                <template #activator="{on, bind}">
                  <v-btn
                    v-on="on"
                    v-bind="bind"
                    icon
                    @click="copyPublicSiteLink">
                    <v-icon size="18">fa-clone</v-icon>
                  </v-btn>
                </template>
                <span>{{ $t('SpaceSettings.publicSite.drawer.copyLink.tooltip') }}</span>
              </v-tooltip>
            </v-list-item-action>
          </v-list-item>
        </template>
        <div v-else class="my-8 d-flex align-center justify-center">
          <v-btn
            :loading="loading"
            color="primary"
            elevation="0"
            @click="saveSpacePublicSite">
            {{ $t('SpaceSettings.publicSite.create') }}
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    publicSiteVisibility: null,
    publicSiteId: null,
    publicSite: null,
  }),
  computed: {
    publicSiteLink() {
      return this.publicSite && `${eXo.env.portal.context}/${this.publicSite.name}`;
    },
    isHiddenSpace() {
      return this.$root.space?.visibility?.toLowerCase?.() === 'hidden';
    },
  },
  watch: {
    publicSiteId() {
      this.retrievePublicSite();
    },
  },
  methods: {
    open() {
      this.publicSiteVisibility = this.$root.space?.publicSiteVisibility || 'manager';
      this.publicSiteId = this.$root.space?.publicSiteId || null;
      this.$nextTick().then(() => this.$refs.drawer.open());
    },
    close() {
      this.$refs.drawer.close();
    },
    async retrievePublicSite() {
      if (this.publicSiteId) {
        this.publicSite = await this.$siteService.getSiteById(this.publicSiteId);
      }
    },
    async saveSpacePublicSite() {
      if (!this.drawer) {
        return;
      }
      this.loading = true;
      try {
        const space = await this.$spaceService.updateSpace({
          id: this.$root.spaceId,
          publicSiteVisibility: this.publicSiteVisibility,
        });
        this.publicSiteId = space?.publicSiteId;
        this.$root.$emit('space-settings-updated', space);
        this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.creation.success'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.creation.error'), 'error');
      } finally {
        this.loading = false;
      }
    },
    copyPublicSiteLink() {
      try {
        navigator.clipboard.writeText(`${window.location.origin}${this.publicSiteLink}`);
        this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.drawer.copyLink.success'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.drawer.copyLink.error'), 'warning');
      }
    },
  },
};
</script>