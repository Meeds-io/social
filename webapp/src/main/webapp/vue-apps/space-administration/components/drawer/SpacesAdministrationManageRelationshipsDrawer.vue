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
    id="ManageRelationshipsDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading || saving"
    allow-expand
    right>
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.manageRelationships') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <div class="text-header">
          {{ $t('social.spaces.administration.manageSpaces.parentSpace') }}
        </div>
        <div v-if="!isSubspace" class="d-flex flex-row flex-grow-1 activitySpaceSuggester">
          <exo-identity-suggester
            id="selectDestinationSpaceParent"
            ref="activitySpaceSuggester"
            v-model="spaces"
            :labels="spaceSuggesterLabels"
            :include-users="false"
            :ignore-items="ignoreItems"
            :search-options="{filterType: 'all'}"
            :width="220"
            name="activitySpaceAutocomplete"
            class="space-suggester activitySpaceAutocomplete"
            include-spaces
            only-parent-spaces
            ignore-cache
            @input="selectParentSpace" />
        </div>
        <v-list-item
          v-else
          class="px-0"
          dense>
          <v-list-item-content>
            <space-avatar
              :space="spaceParent"
              class="text-truncate"
              list-style />
          </v-list-item-content>
          <v-list-item-action class="my-auto me-0 ms-n2">
            <v-btn
              small
              icon
              @click="unlinkParentSpace()">
              <v-icon size="18">
                fas fa-unlink
              </v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <div class="text-header my-2">
          {{ $t('social.spaces.administration.manageSpaces.subspaces') }}
        </div>
        <div v-if="displayedSubspaces">
          <v-list-item
            v-for="subspace in subspaces"
            :key="subspace.id"
            class="px-0"
            dense>
            <v-list-item-content>
              <space-avatar
                :space="subspace"
                class="text-truncate"
                list-style />
            </v-list-item-content>
            <v-list-item-action class="my-auto me-0 ms-n2">
              <v-btn
                small
                icon
                @click="linkSubspace(subspace, 0)">
                <v-icon size="18">
                  fas fa-unlink
                </v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </div>
        <div v-else class="d-flex flex-column flex-grow-1 full-height">
          <div class="d-flex flex-column align-center justify-center mt-6">
            <v-icon class="tertiary--text" size="60">fas fa-layer-group</v-icon>
            <span class="mt-5">{{ $t('social.spaces.administration.manageSpaces.subspaces.instructionsOne') }}</span>
            <span>{{ $t('social.spaces.administration.manageSpaces.subspaces.instructionsTwo') }}</span>
          </div>
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
    saving: false,
    space: null,
    spaceParent: null,
    spaces: [],
    subspaces: [],
  }),
  computed: {
    spaceId() {
      return this.space?.id;
    },
    prettyName() {
      return this.space?.prettyName;
    },
    displayedSubspaces() {
      return !!this.subspaces?.length;
    },
    parentSpaceId() {
      return this.space?.parentSpaceId;
    },
    isSubspace() {
      return !!this.parentSpaceId;
    },
    spaceSuggesterLabels() {
      return {
        placeholder: this.$t('social.spaces.administration.spaceSuggesterPlaceholder'),
        noDataLabel: this.$t('social.spaces.administration.spaceSuggesterNoData'),
      };
    },
    ignoreItems() {
      return [`space:${this.prettyName}`];
    },
  },
  created() {
    this.$root.$on('space-administration-manage-relationships-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-manage-relationships-drawer-open', this.open);
  },
  methods: {
    async retrieveSubspaces() {
      this.loading = true;
      try {
        const data = await this.$spaceService.getSpacesByFilter({
          filter: 'all',
          expand: 'managers,groupBinding',
          parentSpaceId: this.spaceId,
          offset: 0,
          limit: 25,
          sortBy: 'title',
          sortDirection: 'desc',
        });
        this.subspaces = data?.spaces || [];
      } finally {
        this.loading = false;
      }
    },
    async retrieveSpaceParent() {
      if (this.isSubspace) {
        this.loading = true;
        try {
          this.spaceParent = await this.$spaceService.getSpaceById(this.parentSpaceId);
        } finally {
          this.loading = false;
        }
      }
    },

    open(space) {
      this.spaces = [];
      this.space = space;
      this.retrieveSubspaces();
      this.retrieveSpaceParent();
      this.$refs.drawer.open();
    },
    async linkSubspace(space, parentSpaceId) {
      const updatedSpace = await this.$spaceService.updateSpace({
        id: space.id,
        parentSpaceId: parentSpaceId,
      });
      if (this.spaceId === space.id) {
        this.space = updatedSpace;
      }
      await this.retrieveSpaceParent();
      await this.retrieveSubspaces();
      if (this.spaceId === space.id && !parentSpaceId) {
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.spaceNoMoreSubspace'), 'success');
      } else if (!parentSpaceId) {
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.subspaceSuccessfullyRemoved'), 'success');
      }
      this.$root.$emit('spaces-administration-list-refresh');
    },
    selectParentSpace(space) {
      this.linkSubspace(this.space, space?.spaceId);
    },
    unlinkParentSpace() {
      this.linkSubspace(this.space, 0);
      this.spaces = [];
    }
  },
};
</script>