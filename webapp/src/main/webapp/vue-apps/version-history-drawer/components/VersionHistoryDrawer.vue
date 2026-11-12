<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
    ref="versionHistoryDrawer"
    v-model="drawer"
    class="versionHistoryDrawer"
    right
    @closed="closed">
    <template slot="title">
      {{ $t('versionHistory.label.title') }}
    </template>
    <template v-if="sourcesExtension" slot="content">
      <version-history-timeline
        v-if="drawer"
        :versions="versions"
        :is-loading="isLoading"
        :can-manage="canManage"
        :enable-edit-description="enableEditDescription"
        :disable-restore-version="disableRestoreVersion"
        :sources-extension="sourcesExtension"
        :source-params="sourceParams"
        @open-version="openVersion"
        @version-update-description="updateVersionDescription"
        @restore-version="restoreVersion" />
    </template>
    <template v-else slot="content">
      <div
        v-if="!isLoading && !hasVersions"
        class="versionHistoryEmpty text-center text-light-color mt-5">
        {{ $t('versionHistory.label.noVersion') }}
      </div>
      <v-list
        v-else
        class="versionHistoryList py-0">
        <v-slide-y-transition group>
          <v-list-item
            v-for="version in versions"
            :key="version.id"
            :class="[version.current? 'comment-background-color' : '']"
            class="history-line versionHistoryLine py-3 px-4 border-bottom-color d-block text-start"
            @click="openVersion(version)">
            <version-card
              :version="version"
              :can-manage="canManage"
              :disable-restore-version="disableRestoreVersion"
              :enable-edit-description="enableEditDescription"
              @version-update-description="updateVersionDescription"
              @restore-version="restoreVersion" />
          </v-list-item>
        </v-slide-y-transition>
      </v-list>
    </template>
    <template v-if="showLoadMore && !sourcesExtension" slot="footer">
      <v-btn
        :loading="isLoading"
        :disabled="isLoading"
        class="versionHistoryLoadMore btn"
        block
        @click="loadMore">
        {{ $t('versionHistory.button.loadMore') }}
      </v-btn>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  props: {
    versions: {
      type: Array,
      default: () => {
        return null;
      }
    },
    canManage: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    showLoadMore: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    isLoading: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    enableEditDescription: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    disableRestoreVersion: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    /**
     * The extensionRegistry app under which add-ons register line sources
     * for this host, of type version-history-source: set, the drawer lists
     * the host's versions and those sources' lines in one timeline, with
     * chips and scrolling; null keeps the plain list of versions.
     */
    sourcesExtension: {
      type: String,
      default: null,
    },
    /**
     * What each source's isEnabled and load receive about the object, such
     * as {noteId}.
     */
    sourceParams: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    drawer: false,
  }),
  computed: {
    /**
     * Tells whether there is at least one version to list.
     * @returns {boolean} true when the list has versions
     */
    hasVersions() {
      return !!this.versions && this.versions.length > 0;
    },
  },
  watch: {
    /**
     * Shows the drawer loading while the host loads versions; the timeline
     * shows its own when sources are listed.
     * @returns {void}
     */
    isLoading() {
      if (this.sourcesExtension) {
        return;
      }
      if (this.isLoading) {
        this.$refs.versionHistoryDrawer.startLoading();
      } else {
        this.$refs.versionHistoryDrawer.endLoading();
      }
    },
  },
  methods: {
    /**
     * Opens the drawer.
     * @returns {void}
     */
    open() {
      this.$refs.versionHistoryDrawer.open();
    },
    /**
     * Tells the host the drawer closed.
     * @returns {void}
     */
    closed() {
      this.$emit('drawer-closed');
    },
    /**
     * Asks the host for the next page of versions.
     * @returns {void}
     */
    loadMore() {
      this.$emit('load-more');
    },
    /**
     * Asks the host to open a version.
     * @param {Object} version the version to open
     * @returns {void}
     */
    openVersion(version) {
      this.$emit('open-version', version);
    },
    /**
     * Asks the host to save a version's summary.
     * @param {Object} version the version
     * @param {string} newDescription its new summary
     * @returns {void}
     */
    updateVersionDescription(version, newDescription) {
      this.$emit('version-update-description', version, newDescription);
    },
    /**
     * Asks the host to restore a version the card has confirmed.
     * @param {Object} version the version to restore
     * @returns {void}
     */
    restoreVersion(version) {
      this.$emit('restore-version', version);
    },
  }
};
</script>