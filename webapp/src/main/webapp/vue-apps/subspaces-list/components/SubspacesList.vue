<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <v-app>
    <v-hover v-if="parentSpace" v-model="hover">
      <widget-wrapper
        :title="headerTitle"
        :loading="$root.loading"
        extra-class="application-body">
        <template v-if="canManageSpace" #action>
          <div class="d-flex align-center justify-center">
            <v-btn
              v-show="hover"
              :title="$t('subspacesList.settings.drawer.title')"
              height="27"
              width="27"
              class="pa-0"
              min-width="auto"
              text
              icon
              @click="openSettingsDrawer">
              <v-icon size="18">
                fa-cog
              </v-icon>
            </v-btn>
          </div>
        </template>
        <template #default>
          <div v-if="hasSubspaces" class="d-flex flex-column">
            <subspaces-list-item
              v-for="subspace in displayedSubspaces"
              :key="subspace.id"
              :space="subspace" />
          </div>
          <div v-else class="d-flex flex-column align-center justify-center text-center">
            <span>{{ $t('subspacesList.label.noSubspaces') }}</span>
            <space-creation-button
              v-if="canCreateSubspace"
              :parent-space-id="$root.spaceId"
              :color="'primary'"
              class="mt-4"
              require-form-drawer
              :display-icon="false"
              display-label
              outlined />
          </div>
        </template>
      </widget-wrapper>
    </v-hover>
    <subspaces-list-settings-drawer
      v-if="settingsDrawer"
      ref="settingsDrawer"
      @saved="refresh" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    // until the resource call answers, the application stays as the page
    // rendered it: nothing is shown and nothing is hidden
    parentSpace: null,
    canCreateSubspace: false,
    canManageSpace: false,
    subspaces: [],
    hover: false,
    // the drawer is mounted on first use only, and kept from then on: most
    // viewers never manage the space and never see the action, and tearing
    // it down on close would cut the drawer's slide-out short
    settingsDrawer: false,
  }),
  computed: {
    headerTitle() {
      return this.$root.headerTitle || this.$t('subspacesList.header.label');
    },
    hasSubspaces() {
      return this.subspaces.length > 0;
    },
    /**
     * The call asks for one item more than the widget shows, so that the
     * 'see more' action can be derived from the extra row rather than from a
     * count query that would not agree with the list. Only the first
     * subspacesLimit items are rendered here.
     *
     * @returns {Array} the sub-spaces the widget body shows
     */
    displayedSubspaces() {
      return this.subspaces.slice(0, this.$root.settings.subspacesLimit);
    },
  },
  created() {
    this.refresh();
  },
  methods: {
    /**
     * Opens the preferences drawer. The action is offered from the resource
     * envelope's canManageSpace, and the portlet re-checks the same guard on
     * save: showing the button is a convenience, never the decision.
     *
     * @returns {Promise<void>} resolved once the drawer is open
     */
    async openSettingsDrawer() {
      this.settingsDrawer = true;
      await this.$nextTick();
      this.$refs.settingsDrawer.open();
    },
    refresh() {
      this.$root.loading = true;
      const limit = this.$root.settings.subspacesLimit + 1;
      return this.$subspacesListService.getSubspaces(this.$root.settings.resourceUrl, limit)
        .then(envelope => {
          this.parentSpace = envelope.parentSpace;
          this.canManageSpace = envelope.canManageSpace;
          this.canCreateSubspace = envelope.canCreateSubspace;
          this.subspaces = envelope.subspaces || [];
        })
        .catch(() => {
          // the page hosts no parent space, or the viewer may not list it:
          // either way the widget has nothing to show
          this.parentSpace = false;
        })
        .finally(() => {
          this.$root.loading = false;
          // outside a parent space the cell disappears in view mode and keeps
          // the layout editor's toolbar in edit mode
          this.$updateApplicationVisibility(this.parentSpace);
        });
    },
  },
};
</script>
