<template>
  <v-card
    :id="applicationId"
    class="border-radius border-color"
    flat>
    <div class="d-flex flex-no-wrap">
      <v-avatar
        class="SpaceApplicationCardImage mx-1 my-auto"
        size="45"
        tile>
        <i :class="applicationIcon"></i>
      </v-avatar>
      <div>
        <v-card-title
          :title="applicationName"
          class="text-truncate subtitle-1 px-1 text-color SpaceApplicationCardTitle">
          {{ applicationName }}
        </v-card-title>
        <v-card-subtitle
          :title="applicationDescription"
          class="text-truncate subtitle-2 px-1 text-sub-title SpaceApplicationCardDescription">
          {{ applicationDescription || applicationName }}
        </v-card-subtitle>
      </div>
      <div class="SpaceApplicationCardAction">
        <v-btn
          :disabled="skeleton"
          :class="skeleton && 'skeleton-background skeleton-text'"
          icon
          text
          class="primary--text d-none d-sm-block"
          @click="displayActionMenu = true">
          <v-icon size="12">mdi-dots-vertical</v-icon>
        </v-btn>
        <v-menu
          v-if="!skeleton"
          ref="actionMenu"
          v-model="displayActionMenu"
          :attach="`#${applicationId}`"
          transition="slide-x-reverse-transition"
          offset-y
          right
          close-on-click
          close-on-content-click>
          <v-list class="pa-0" dense>
            <v-list-item
              v-if="applicationRemovable"
              @click="$emit('remove')">
              <v-list-item-title>
                {{ $t('SpaceSettings.button.remove') }}
              </v-list-item-title>
            </v-list-item>
            <v-list-item
              v-if="!firstApplication"
              @click="$emit('moveBefore')">
              <v-list-item-title>
                {{ $t('SpaceSettings.button.moveBefore') }}
              </v-list-item-title>
            </v-list-item>
            <v-list-item
              v-if="!lastApplication"
              @click="$emit('moveAfter')">
              <v-list-item-title>
                {{ $t('SpaceSettings.button.moveAfter') }}
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </div>
  </v-card>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    application: {
      type: String,
      default: null,
    },
    index: {
      type: Number,
      default: 0,
    },
    length: {
      type: Number,
      default: 0,
    },
  },
  data: () => ({
    displayActionMenu: false,
  }),
  computed: {
    firstApplication() {
      return this.index === 0;
    },
    lastApplication() {
      return this.index === (this.length - 1);
    },
    applicationRemovable() {
      return this.application && this.application.id && this.application.removable && String(this.application.removable) === 'true';
    },
    applicationId() {
      return this.application && this.application.id;
    },
    applicationName() {
      return this.application && this.application.displayName;
    },
    applicationDescription() {
      return this.application && this.application.description;
    },
    applicationIcon() {
      if (!this.application || !this.application.id) {
        return '';
      }
      const iconSuffix = `${this.application.id.charAt(0).toUpperCase()}${this.application.id.substring(1)}`;
      return `uiIconApp${iconSuffix} uiIconDefaultApp`;
    },
  },
  mounted() {
    // Force to close DatePickers when clicking outside
    $(document).on('click', (e) => {
      if (e.target && !$(e.target).parents(`#${this.applicationId}`).length) {
        this.displayActionMenu = false;
      }
    });
  },
};
</script>

