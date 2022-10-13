<template>
  <v-main>
    <div
      class="author-date-wrapper d-flex justify-space-between ">
      <div class="version-author">
        <span class="item-version border-radius primary px-2 font-weight-bold me-2 clickable">
          V{{ versionObject.versionNumber }}
        </span>
        <span class="font-weight-bold text-truncate">{{ versionObject.authorFullName }}</span>
      </div>
      <div class="version-update-date">
        <date-format
          class="text-light-color text-truncate caption"
          :value="versionDate"
          :format="dateTimeFormat" />
      </div>
    </div>
    <div class="description-restore-wrapper d-flex justify-space-between pt-2">
      <br>
      <div
        v-if="enableEditDescription"
        class="item-version-description">
        <a
          @click="showInput"
          v-if="!versionObject.summary && descriptionInputHidden"
          class="descriptionContent">
          {{ $t('versionHistory.description.add') }}
        </a>
        <v-progress-circular
          v-if="isUpdatingDescription"
          :size="20"
          color="primary"
          indeterminate />
        <v-tooltip
          v-if="descriptionInputHidden"
          bottom>
          <template #activator="{ on, attrs }">
            <p
              v-bind="attrs"
              v-on="on"
              class="descriptionContent pa-0 text-truncate"
              @click="showInput">
              {{ versionObject.summary }}
            </p>
          </template>
          <div
            class="caption tooltip-version">
            {{ versionObject.summary }}
          </div>
        </v-tooltip>
        <v-text-field
          ref="NewDescriptionInput"
          v-show="!descriptionInputHidden && !isUpdatingDescription"
          v-if="canManage"
          v-model="newDescription"
          :placeholder="$t('versionHistory.description.placeholder')"
          class="description pa-0"
          outlined
          dense
          autofocus
          @keyup.enter="updateDescription">
          <div slot="append" class="d-flex mt-1">
            <v-icon
              :class="descriptionMaxLengthReached && 'not-allowed' || 'clickable'"
              :color="descriptionMaxLengthReached && 'grey--text' || 'primary'"
              class="px-1"
              small
              @click="updateDescription">
              fa-check
            </v-icon>
            <v-icon
              class="clickable px-0"
              color="red"
              small
              @click="resetInput">
              fa-times
            </v-icon>
          </div>
        </v-text-field>
      </div>
      <div
        v-show="descriptionInputHidden"
        v-if="!versionObject.current && canManage && !disableRestoreVersion"
        class="item-version-restore">
        <v-progress-circular
          v-if="isRestoringVersion"
          :size="20"
          color="primary"
          indeterminate />
        <v-tooltip
          v-if="!isRestoringVersion"
          bottom>
          <template #activator="{ on, attrs }">
            <v-icon
              v-bind="attrs"
              v-on="on"
              size="22"
              class="primary--text clickable pa-0 mt-1"
              @click="restoreVersion">
              mdi-restart
            </v-icon>
          </template>
          <span class="caption">{{ $t('versionHistory.label.restore') }}</span>
        </v-tooltip>
      </div>
    </div>
  </v-main>
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
    this.$root.$on('version-restored', (restoredVersion) => {
      this.versionObject.current = this.versionObject.id === restoredVersion.id;
      this.isRestoringVersion = false;
    });
    this.$root.$on('version-restore-error', () => {
      this.isRestoringVersion = false;
    });
    this.$root.$on('version-description-updated', (version) => {
      if (version.id === this.version.id) {
        this.versionObject.summary = version.summary;
        this.isUpdatingDescription = false;
        this.descriptionInputHidden = true;
      }
    });
    this.$root.$on('version-description-update-error', (version) => {
      if (version.id === this.version.id) {
        this.isUpdatingDescription = false;
        this.descriptionInputHidden = false;
      }
    });
    this.versionObject = Object.assign({}, this.version);
  },
  computed: {
    versionDate() {
      return this.versionObject && this.versionObject.updatedDate
                                && this.versionObject.updatedDate.time
                                || this.versionObject.createdDate.time;
    },
    descriptionMaxLengthReached() {
      return this.newDescription && this.newDescription.length > this.MAX_DESCRIPTION_LENGTH;
    },
  },
  methods: {
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