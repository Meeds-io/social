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
    class="versionHistoryDrawer"
    right
    @closed="closed">
    <template slot="title">
      {{ $t('versionHistory.label.title') }}
    </template>
    <template slot="content">
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
              :restoring="restoringVersionId === version.id"
              @version-update-description="updateVersionDescription"
              @restore-version="requestRestoreVersion" />
          </v-list-item>
        </v-slide-y-transition>
      </v-list>
      <exo-confirm-dialog
        ref="restoreConfirmDialog"
        :title="$t('versionHistory.confirm.restoreTitle', {0: versionToRestoreNumber})"
        :message="$t('versionHistory.confirm.restoreMessage', {0: versionToRestoreNumber})"
        :ok-label="$t('versionHistory.button.restore')"
        :cancel-label="$t('versionHistory.button.cancel')"
        @ok="restoreConfirmedVersion" />
    </template>
    <template v-if="showLoadMore" slot="footer">
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
  },
  data: () => ({
    versionToRestore: null,
    restoringVersionId: null,
  }),
  computed: {
    /**
     * Tells whether there is at least one version to list.
     * @returns {boolean} true when the list has versions
     */
    hasVersions() {
      return !!this.versions && this.versions.length > 0;
    },
    /**
     * Gives the number of the version the confirmation is about.
     * @returns {number} the version number, or an empty string
     */
    versionToRestoreNumber() {
      return this.versionToRestore && this.versionToRestore.versionNumber || '';
    },
  },
  watch: {
    /**
     * Ends the restoring state when the host lists a new newest version, as
     * a host that reloads its versions after a restore does; a page loaded
     * by « Show more » keeps the newest one, and a host that adds the
     * restored version in place ends it through `version-restored`.
     * @param {Array} newVersions the versions now listed, newest first
     * @param {Array} oldVersions the versions listed before
     * @returns {void}
     */
    versions(newVersions, oldVersions) {
      const newestId = newVersions && newVersions.length && newVersions[0].id;
      const previousNewestId = oldVersions && oldVersions.length && oldVersions[0].id;
      if (newestId !== previousNewestId) {
        this.endRestore();
      }
    },
    /**
     * Shows the drawer loading while the host loads versions.
     * @returns {void}
     */
    isLoading() {
      if (this.isLoading) {
        this.$refs.versionHistoryDrawer.startLoading();
      } else {
        this.$refs.versionHistoryDrawer.endLoading();
      }
    },
  },
  /**
   * Listens to the host's end of a restore, success or failure.
   * @returns {void}
   */
  created() {
    this.$root.$on('version-restored', this.endRestore);
    this.$root.$on('version-restore-error', this.endRestore);
  },
  /**
   * Stops listening to the host's end of a restore.
   * @returns {void}
   */
  beforeDestroy() {
    this.$root.$off('version-restored', this.endRestore);
    this.$root.$off('version-restore-error', this.endRestore);
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
     * Asks to confirm the restore of a version before the host restores it.
     * @param {Object} version the version to restore
     * @returns {void}
     */
    requestRestoreVersion(version) {
      this.versionToRestore = version;
      this.$refs.restoreConfirmDialog.open();
    },
    /**
     * Asks the host to restore the version the confirmation was about, and
     * shows it restoring until the host says it is done or failed.
     * @returns {void}
     */
    restoreConfirmedVersion() {
      if (this.versionToRestore) {
        this.restoringVersionId = this.versionToRestore.id;
        this.$emit('restore-version', this.versionToRestore);
      }
    },
    /**
     * Ends the restoring state once the host has restored a version or
     * failed to.
     * @returns {void}
     */
    endRestore() {
      this.restoringVersionId = null;
    },
  }
};
</script>