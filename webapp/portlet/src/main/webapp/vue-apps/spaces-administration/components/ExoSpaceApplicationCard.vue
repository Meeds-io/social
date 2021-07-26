<template>
  <v-card :id="applicationId" flat>
    <div class="d-flex flex-no-wrap">
      <v-avatar
        class="SpaceApplicationCardImage mx-1 my-auto"
        size="45"
        tile>
        <i :class="applicationIcon"></i>
      </v-avatar>
      <div class="flex-grow-1 SpaceApplicationCardBody text-truncate">
        <div
          :title="applicationName"
          class="text-truncate subtitle-1 px-1 pt-4 text-color SpaceApplicationCardTitle">
          {{ applicationName }}
        </div>
        <v-card-subtitle
          :title="applicationDescription"
          class="text-truncate subtitle-2 px-1 pt-0 text-sub-title SpaceApplicationCardDescription">
          {{ applicationDescription || applicationName }}
        </v-card-subtitle>
      </div>
      <div class="SpaceApplicationCardAction">
        <v-btn
          v-if="useMenu"
          :disabled="skeleton"
          :class="skeleton && 'skeleton-background skeleton-text'"
          icon
          text
          class="primary--text"
          @click="displayActionMenu = true">
          <v-icon size="16">mdi-dots-vertical</v-icon>
        </v-btn>
        <v-btn
          v-else
          text
          height="100%"
          width="100%px"
          class="primary--text"
          @click="$emit('add')">
          <v-icon size="36">mdi-plus</v-icon>
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
            <v-list-item @click="$emit('remove')">
              <v-list-item-title>
                {{ $t('social.spaces.administration.manageSpaces.applications.remove') }}
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
    useMenu: {
      type: Boolean,
      default: false,
    },
    application: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    displayActionMenu: false,
  }),
  computed: {
    applicationId() {
      return this.application && this.application.contentId && this.application.contentId.split('/')[1];
    },
    applicationName() {
      return this.application && (this.application.displayName || this.application.applicationName);
    },
    applicationDescription() {
      return this.application && this.application.description;
    },
    applicationIcon() {
      if (!this.applicationId) {
        return '';
      }
      const iconSuffix = `${this.applicationId.charAt(0).toUpperCase()}${this.applicationId.substring(1)}`;
      return `uiIconApp${iconSuffix} uiIconDefaultApp`;
    },
  },
  mounted() {
    if (this.useMenu) {
      // Force to close DatePickers when clicking outside
      $(document).on('click', (e) => {
        if (e.target && !$(e.target).parents(`#${this.applicationId}`).length) {
          this.displayActionMenu = false;
        }
      });
    }
  },
};
</script>

