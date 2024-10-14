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
  <div>
    <div
      class="author-date-wrapper d-flex justify-space-between">
      <div class="version-author">
        <v-chip
          class="ma-0 px-2 font-weight-bold"
          color="primary"
          x-small
          label>
          V{{ versionObject.versionNumber }}
        </v-chip>
        <span class="font-weight-bold text-truncate">{{ versionObject.authorFullName }}</span>
      </div>
      <div class="version-update-date">
        <date-format
          class="text-light-color text-truncate caption"
          :value="versionDate"
          :format="dateTimeFormat" />
      </div>
    </div>
    <div class="mb-1 description-restore-wrapper d-flex justify-space-between pt-2">
      <div
        v-if="enableEditDescription"
        class="flex item-version-description">
        <a
          v-if="!versionObject.summary && descriptionInputHidden"
          class="descriptionContent position-relative dark-grey-color"
          @click.stop.prevent="showInput">
          {{ $t('versionHistory.description.add') }}
        </a>
        <v-progress-circular
          v-if="isUpdatingDescription"
          :size="20"
          color="primary"
          indeterminate />
        <v-tooltip
          v-else-if="descriptionInputHidden"
          bottom>
          <template #activator="{ on, attrs }">
            <p
              v-bind="attrs"
              v-on="on"
              class="descriptionContent pa-0 text-truncate position-relative dark-grey-color"
              @click.stop.prevent="showInput">
              {{ versionObject.summary }}
            </p>
          </template>
          <div
            class="caption tooltip-version text-break">
            {{ versionObject.summary }}
          </div>
        </v-tooltip>
        <v-text-field
          v-if="canManage"
          ref="NewDescriptionInput"
          v-show="!descriptionInputHidden && !isUpdatingDescription"
          v-model="newDescription"
          :placeholder="$t('versionHistory.description.placeholder')"
          class="description pa-0 dark-grey-color"
          outlined
          dense
          autofocus
          @click.stop.prevent
          @keyup.enter="updateDescription">
          <div slot="append" class="d-flex mt-1">
            <v-btn
              class="mt-n1 me-n2 pb-1"
              icon
              small
              :disabled="descriptionMaxLengthReached"
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
              class="mt-n1 me-n2 pb-1"
              small
              icon>
              <v-icon
                class="clickable px-0 ma-0"
                color="red"
                small
                @click.stop.prevent="resetInput">
                fa-times
              </v-icon>
            </v-btn>
          </div>
        </v-text-field>
      </div>
      <div
        v-if="!versionObject.current && canManage && !disableRestoreVersion"
        v-show="descriptionInputHidden"
        class="item-version-restore">
        <v-tooltip
          bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              icon
              v-bind="attrs"
              v-on="on"
              :loading="isRestoringVersion"
              small
              color="primary"
              @click.stop.prevent="restoreVersion">
              <v-icon
                size="22"
                class="primary--text clickable pa-0 mt-1">
                mdi-restart
              </v-icon>
            </v-btn>
          </template>
          <span class="caption">{{ $t('versionHistory.label.restore') }}</span>
        </v-tooltip>
      </div>
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
    }
  },
  data: () => ({
    dateTimeFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    },
    MAX_DESCRIPTION_LENGTH: 500,
    descriptionInputHidden: true,
    newDescription: '',
    versionObject: {},
    isUpdatingDescription: false,
    isRestoringVersion: false
  }),
  created() {
    this.$root.$on('version-restored', this.handleVersionRestore);
    this.$root.$on('version-restore-error', this.handleVersionRestoreError);
    this.$root.$on('version-description-updated', this.handleDescriptionUpdated);
    this.$root.$on('version-description-update-error', this.handleDescriptionUpdateError);
    this.versionObject = Object.assign({}, this.version);
  },
  beforeDestroy() {
    this.$root.$off('version-restored', this.handleVersionRestore);
    this.$root.$off('version-restore-error', this.handleVersionRestoreError);
    this.$root.$off('version-description-updated', this.handleDescriptionUpdated);
    this.$root.$off('version-description-update-error', this.handleDescriptionUpdateError);
  },
  computed: {
    versionDate() {
      return this.versionObject?.updatedDate?.time || this.versionObject?.createdDate?.time;
    },
    descriptionMaxLengthReached() {
      return this.newDescription && this.newDescription.length > this.MAX_DESCRIPTION_LENGTH;
    },
  },
  methods: {
    handleVersionRestore(restoredVersion) {
      this.versionObject.current = this.versionObject.id === restoredVersion.id;
      this.isRestoringVersion = false;
    },
    handleVersionRestoreError() {
      this.isRestoringVersion = false;
    },
    handleDescriptionUpdated(version) {
      if (version.id === this.version.id) {
        this.versionObject.summary = version.summary;
        this.isUpdatingDescription = false;
        this.descriptionInputHidden = true;
      }
    },
    handleDescriptionUpdateError(version) {
      if (version.id === this.version.id) {
        this.isUpdatingDescription = false;
        this.descriptionInputHidden = false;
      }
    },
    resetInput() {
      this.descriptionInputHidden =  true;
    },
    updateDescription() {
      if (this.descriptionMaxLengthReached) {
        return;
      }
      this.isUpdatingDescription = true;
      this.$emit('version-update-description', this.versionObject, this.newDescription);
    },
    showInput() {
      this.newDescription = this.versionObject.summary;
      this.descriptionInputHidden = !this.descriptionInputHidden;
    },
    restoreVersion() {
      this.isRestoringVersion = true;
      this.$emit('restore-version', this.versionObject);
    }
  }
};
</script>