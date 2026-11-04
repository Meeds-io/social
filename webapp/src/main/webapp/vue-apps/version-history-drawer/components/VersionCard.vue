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
  <div class="versionCard text-start" @keydown.enter.stop>
    <div class="versionTitle d-flex align-center text-body-2 font-weight-bold">
      <span>{{ $t('versionHistory.label.version', {0: versionObject.versionNumber}) }}</span>
      <v-chip
        v-if="versionObject.current"
        class="versionCurrentChip ms-2"
        color="primary"
        x-small
        label
        outlined>
        {{ $t('versionHistory.label.current') }}
      </v-chip>
    </div>
    <div v-if="isUpdatingDescription" class="my-1">
      <v-progress-circular
        :size="20"
        color="primary"
        indeterminate />
    </div>
    <div
      v-else-if="!descriptionInputHidden"
      class="versionDescriptionEdit my-1"
      @click.stop>
      <v-text-field
        ref="newDescriptionInput"
        v-model="newDescription"
        :placeholder="$t('versionHistory.description.writeSummary')"
        :maxlength="maxDescriptionLength"
        class="versionDescriptionInput pa-0"
        :hint="$t('versionHistory.description.hint')"
        persistent-hint
        counter
        outlined
        dense
        autofocus
        @keydown.enter.prevent="updateDescription"
        @keydown.esc.stop.prevent="resetInput">
        <div slot="append" class="d-flex mt-1">
          <v-btn
            :title="$t('versionHistory.description.save')"
            :aria-label="$t('versionHistory.description.save')"
            :disabled="descriptionMaxLengthReached"
            class="versionDescriptionSave mt-n1 me-n2 pb-1"
            icon
            small
            @click.stop.prevent="updateDescription">
            <v-icon
              :class="descriptionMaxLengthReached && 'not-allowed' || 'clickable'"
              :color="descriptionMaxLengthReached && 'grey--text' || 'primary'"
              class="px-1 ma-0"
              small>
              fa-check
            </v-icon>
          </v-btn>
          <v-btn
            :title="$t('versionHistory.description.cancel')"
            :aria-label="$t('versionHistory.description.cancel')"
            class="versionDescriptionCancel mt-n1 me-n2 pb-1"
            icon
            small
            @click.stop.prevent="resetInput">
            <v-icon
              class="text-light-color clickable px-0 ma-0"
              small>
              fa-times
            </v-icon>
          </v-btn>
        </div>
      </v-text-field>
    </div>
    <div
      v-else-if="versionObject.summary"
      class="versionSummary d-flex align-start text-body-2 my-1">
      <span class="text-break">{{ versionObject.summary }}</span>
      <v-btn
        v-if="canEditDescription"
        :title="$t('versionHistory.description.edit')"
        :aria-label="$t('versionHistory.description.edit')"
        class="versionSummaryEdit ms-1"
        x-small
        icon
        @click.stop.prevent="showInput">
        <v-icon class="text-light-color" x-small>fa-pencil-alt</v-icon>
      </v-btn>
    </div>
    <a
      v-else-if="canEditDescription"
      href="#"
      class="versionSummaryAdd d-inline-block text-caption primary--text my-1"
      @click.stop.prevent="showInput">
      {{ $t('versionHistory.description.addSummary') }}
    </a>
    <div class="versionMeta d-flex align-center flex-wrap text-start text-caption text-light-color mt-1">
      <span
        v-if="versionObject.author"
        class="versionAuthor"
        @click.stop>
        <user-avatar
          :profile-id="versionObject.author"
          :size="20"
          extra-class="me-2"
          fullname
          small-font-size
          popover />
      </span>
      <span v-else-if="versionObject.authorFullName" class="me-2">{{ versionObject.authorFullName }}</span>
      <date-format
        :value="versionDate"
        :format="dateFormat"
        class="me-2" />
      <span class="me-1">·</span>
      <a
        :aria-label="$t('versionHistory.label.openVersion', {0: versionObject.versionNumber})"
        href="#"
        class="versionOpenLink primary--text"
        @click.stop.prevent="openVersion">
        {{ $t('versionHistory.label.open') }}
      </a>
      <template v-if="canRestore">
        <span class="mx-1">·</span>
        <v-progress-circular
          v-if="restoring"
          :size="14"
          :width="2"
          color="primary"
          indeterminate />
        <a
          v-else
          :aria-label="$t('versionHistory.confirm.restoreTitle', {0: versionObject.versionNumber})"
          href="#"
          class="versionRestoreLink primary--text"
          @click.stop.prevent="restoreVersion">
          {{ $t('versionHistory.button.restore') }}
        </a>
      </template>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    version: {
      type: Object,
      default: () => {
        return {};
      }
    },
    enableEditDescription: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    disableRestoreVersion: {
      type: Boolean,
      default: () => {
        return false;
      },
    },
    canManage: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    restoring: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    },
    maxDescriptionLength: 500,
    descriptionInputHidden: true,
    newDescription: '',
    versionObject: {},
    isUpdatingDescription: false,
  }),
  /**
   * Copies the version and listens to the host's answers about it.
   * @returns {void}
   */
  created() {
    this.$root.$on('version-restored', this.handleVersionRestore);
    this.$root.$on('version-description-updated', this.handleDescriptionUpdated);
    this.$root.$on('version-description-update-error', this.handleDescriptionUpdateError);
    this.versionObject = Object.assign({}, this.version);
  },
  /**
   * Stops listening to the host's answers.
   * @returns {void}
   */
  beforeDestroy() {
    this.$root.$off('version-restored', this.handleVersionRestore);
    this.$root.$off('version-description-updated', this.handleDescriptionUpdated);
    this.$root.$off('version-description-update-error', this.handleDescriptionUpdateError);
  },
  computed: {
    /**
     * Gives the date the version shows: its last update, else its creation.
     * @returns {number} the date, in milliseconds
     */
    versionDate() {
      return this.versionObject?.updatedDate?.time || this.versionObject?.createdDate?.time;
    },
    /**
     * Tells whether the viewer may write the version's summary: the host
     * enables summaries and the viewer may manage the versions.
     * @returns {boolean} true when the summary can be added or changed
     */
    canEditDescription() {
      return this.enableEditDescription && this.canManage;
    },
    /**
     * Tells whether the version offers « Restore »: never on the current
     * version, never while its summary is edited, and only for a viewer who
     * may manage the versions of a host that allows restoring.
     * @returns {boolean} true when « Restore » shows
     */
    canRestore() {
      return !this.versionObject.current && this.canManage && !this.disableRestoreVersion && this.descriptionInputHidden;
    },
    /**
     * Tells whether the summary being written is over the length limit. The
     * field stops typing and pasting at the limit, but the server sets none,
     * so a summary stored longer through another path opens over it until
     * it is shortened.
     * @returns {boolean} true when it is too long to save
     */
    descriptionMaxLengthReached() {
      return !!this.newDescription && this.newDescription.length > this.maxDescriptionLength;
    },
  },
  methods: {
    /**
     * Marks the version current when it is the one restored, and not
     * current otherwise.
     * @param {Object} restoredVersion the version the host restored
     * @returns {void}
     */
    handleVersionRestore(restoredVersion) {
      this.versionObject.current = this.versionObject.id === restoredVersion.id;
    },
    /**
     * Shows the saved summary once the host has stored it for this version.
     * @param {Object} version the version whose summary was saved
     * @returns {void}
     */
    handleDescriptionUpdated(version) {
      if (version.id === this.version.id) {
        this.versionObject.summary = version.summary;
        this.isUpdatingDescription = false;
        this.descriptionInputHidden = true;
      }
    },
    /**
     * Gives the summary field back when the host could not store it.
     * @param {Object} version the version whose summary failed to save
     * @returns {void}
     */
    handleDescriptionUpdateError(version) {
      if (version.id === this.version.id) {
        this.isUpdatingDescription = false;
        this.descriptionInputHidden = false;
      }
    },
    /**
     * Leaves the summary field without saving it.
     * @returns {void}
     */
    resetInput() {
      this.descriptionInputHidden = true;
    },
    /**
     * Asks the host to save the summary being written.
     * @returns {void}
     */
    updateDescription() {
      if (this.descriptionMaxLengthReached) {
        return;
      }
      this.isUpdatingDescription = true;
      this.$emit('version-update-description', this.versionObject, this.newDescription);
    },
    /**
     * Opens the summary field, filled with the current summary.
     * @returns {void}
     */
    showInput() {
      this.newDescription = this.versionObject.summary || '';
      this.descriptionInputHidden = false;
    },
    /**
     * Asks to open the version, as a click on its row does.
     * @returns {void}
     */
    openVersion() {
      this.$emit('open-version', this.versionObject);
    },
    /**
     * Asks to restore the version; the drawer confirms it first.
     * @returns {void}
     */
    restoreVersion() {
      this.$emit('restore-version', this.versionObject);
    }
  }
};
</script>
