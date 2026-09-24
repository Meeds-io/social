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
    <v-hover v-if="parentSpace || !loaded" v-model="hover">
      <widget-wrapper
        :title="headerTitle"
        :loading="$root.loading"
        extra-class="application-body">
        <template v-if="hasHeaderActions" #action>
          <subspaces-list-header-actions
            :hover="hover"
            :can-manage-space="canManageSpace"
            :can-create-subspace="canCreateSubspace"
            :has-subspaces="hasSubspaces"
            :has-more="hasMore"
            @see-all="openSeeAllDrawer"
            @settings="openSettingsDrawer" />
        </template>
        <template #default>
          <div v-if="hasSubspaces" class="d-flex flex-column">
            <subspaces-list-item
              v-for="subspace in displayedSubspaces"
              :key="subspace.id"
              :space="subspace" />
          </div>
          <div v-else-if="loaded" class="d-flex flex-column align-center justify-center text-center">
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
      :refresh="refresh" />
    <subspaces-list-drawer
      v-if="seeAllDrawer"
      ref="seeAllDrawer"
      :can-create-subspace="canCreateSubspace" />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    // the render phase already answered true, or the widget is not booted:
    // this tracks the second answer only (a space that stopped being a parent
    // between render and fetch, or a listing the viewer may not read)
    parentSpace: null,
    // the empty state waits for the first answer, or it would read as 'no
    // subspace' before anything was asked
    loaded: false,
    canCreateSubspace: false,
    canManageSpace: false,
    subspaces: [],
    hover: false,
    // mounted on first use and kept: most viewers never see the cog, most
    // parents fit the limit, and a teardown on close would cut the slide-out
    settingsDrawer: false,
    seeAllDrawer: false,
  }),
  computed: {
    headerTitle() {
      return this.$root.headerTitle || this.$t('subspacesList.header.label');
    },
    hasSubspaces() {
      return this.subspaces.length > 0;
    },
    /**
     * Whether the header has an action to show at all; the group itself
     * decides which ones, from the same flags.
     *
     * @returns {boolean} whether the action slot is rendered
     */
    hasHeaderActions() {
      return this.canManageSpace || this.hasMore || (this.canCreateSubspace && this.hasSubspaces);
    },
    /**
     * Whether a row was left out of the widget body, which offers 'see all'.
     * Derived from the extra row the call asks for, never from a count: the
     * visible-spaces count filters membership differently from the listing
     * (MEMBER only, against MEMBER and INVITED) and would disagree with this
     * very list. A limit raised by another manager between the page render
     * and the call leaves the action hidden until the next page load.
     *
     * @returns {boolean} whether the parent has more sub-spaces than shown
     */
    hasMore() {
      return this.subspaces.length > this.$root.settings.subspacesLimit;
    },
    /**
     * @returns {Array} the first subspacesLimit rows; the extra one only
     *          feeds hasMore
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
     * Opens the whole list. The drawer makes its own call rather than being
     * handed these rows: the widget holds only subspacesLimit + 1 of them.
     *
     * @returns {Promise<void>} resolved once the drawer is open
     */
    async openSeeAllDrawer() {
      this.seeAllDrawer = true;
      await this.$nextTick();
      this.$refs.seeAllDrawer.open();
    },
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
    /**
     * Reloads the envelope and re-seats the widget on it, preferences
     * included: the echoed settings are the answer to "was the save applied",
     * which the action URL's own 200 cannot give.
     *
     * The caller passes the limit when it knows the widget is about to show a
     * different number of rows: after a save, {@code $root.settings} still
     * carries the previous limit until this very response replaces it, so
     * reading it here would ask for too few rows. Over-fetching when a save
     * was refused is harmless: the slice follows the echoed stored value.
     *
     * @param {number} limit the number of rows to show, defaulting to the
     *        currently stored one
     * @returns {Promise<void>} resolved once the widget reflects the server
     */
    refresh(limit = this.$root.settings.subspacesLimit) {
      this.$root.loading = true;
      return this.$subspacesListService.getSubspaces(this.$root.settings.resourceUrl, limit + 1)
        .then(envelope => {
          this.parentSpace = envelope.parentSpace;
          this.canManageSpace = envelope.canManageSpace;
          this.canCreateSubspace = envelope.canCreateSubspace;
          this.subspaces = envelope.subspaces || [];
          if (envelope.settings) {
            Object.keys(envelope.settings).forEach(name => this.$set(this.$root.settings, name, envelope.settings[name]));
          }
        })
        .catch(() => {
          // the page hosts no parent space, or the viewer may not list it:
          // either way the widget has nothing to show
          this.parentSpace = false;
        })
        .finally(() => {
          this.loaded = true;
          this.$root.loading = false;
          // a page that turned out to host no parent space, or a listing the
          // viewer may not read: the cell disappears in view mode and, in the
          // layout editor, stays an empty manageable cell (the hook's own
          // guard, common/initComponents.js)
          this.$updateApplicationVisibility(this.parentSpace);
        });
    },
  },
};
</script>
