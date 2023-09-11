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
    :loading="loading"
    allow-expand
    right
    disable-pull-to-refresh>
    <template #title>
      {{ $t('generalSettings.access.defaultSpacesHeader') }}
    </template>
    <template #content>
      <v-card class="pa-4" flat>
        <div class="font-weight-bold dark-grey-color text-subtitle-1">
          {{ $t('generalSettings.access.defaultSpacesTitle') }}
        </div>
        <div class="caption text-light-color">
          {{ $t('generalSettings.access.defaultSpacesSubTitle') }}
        </div>

        <div class="font-weight-bold dark-grey-color text-subtitle-1 mt-4">
          {{ $t('generalSettings.access.selectDefaultSpaces') }}
        </div>
        <exo-identity-suggester
          ref="defaultSpacesInput"
          id="defaultSpacesInput"
          v-model="defaultSpaceIdentities"
          :labels="suggesterLabels"
          name="defaultSpaces"
          height="100"
          include-spaces
          multiple />
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          :aria-label="$t('generalSettings.cancel')"
          :disabled="loading"
          class="btn cancel-button me-4"
          elevation="0"
          @click="close">
          <span class="text-none">
            {{ $t('generalSettings.cancel') }}
          </span>
        </v-btn>
        <v-btn
          :aria-label="$t('generalSettings.apply')"
          :disabled="!changed"
          :loading="loading"
          color="primary"
          class="btn btn-primary"
          elevation="0"
          @click="apply">
          <span class="text-none">
            {{ $t('generalSettings.apply') }}
          </span>
        </v-btn>
      </div>
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
    defaultSpaceIdentities: [],
  }),
  computed: {
    changed() {
      return String(this.defaultSpaceGroupIds) !== String(this.value);
    },
    suggesterLabels() {
      return {
        searchPlaceholder: this.$t('generalSettings.access.defaultSpaces.searchPlaceholder'),
        placeholder: this.$t('generalSettings.access.defaultSpaces.placeholder'),
        noDataLabel: this.$t('generalSettings.access.defaultSpaces.noDataLabel'),
      };
    },
    defaultSpacesSubTitle() {
      return this.$t('generalSettings.access.defaultSpacesSubTitle', {
        0: `<a href="${this.mandatorySpacesLink}">`,
        1: '</a>',
      });
    },
    defaultSpaceGroupIds() {
      return this.defaultSpaceIdentities.map(identity => identity.groupId);
    },
  },
  methods: {
    open() {
      this.reset();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    reset() {
      this.defaultSpaceIdentities = [];
      const spaceGroupIds = this.value || [];
      this.loading = true;
      return Promise.all(
        spaceGroupIds.map(groupId => this.$spaceService.getSpaceByGroupId(groupId)
          .then(space => ({
            id: `space:${space.prettyName}`,
            remoteId: space.prettyName,
            spaceId: space.id,
            groupId: space.groupId,
            providerId: 'space',
            displayName: space.displayName,
            profile: {
              fullName: space.displayName,
              avatarUrl: space.avatarUrl,
            }
          })))
      ).then(spaceIdentities => this.defaultSpaceIdentities = spaceIdentities)
        .finally(() => this.loading = false);
    },
    apply() {
      this.$emit('input', this.defaultSpaceGroupIds);
      this.close();
    },
  }
};
</script>