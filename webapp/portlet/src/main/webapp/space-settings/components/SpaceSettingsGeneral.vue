<template>
  <v-app v-if="displayed">
    <v-card class="border-radius" flat>
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
                {{ skeleton && '&nbsp;' || $t('SpaceSettings.general') }}
              </div>
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :class="skeleton && 'skeleton-background'"
              small
              icon
              @click="openDrawer">
              <i v-if="!skeleton" class="uiIconEdit uiIconLightBlue pb-2"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>

      <space-settings-form-drawer
        ref="spaceFormDrawer"
        :max-upload-size="maxUploadSize" />
    </v-card>
  </v-app>
</template>

<script>
export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    },
    maxUploadSize: {
      type: Number,
      default: 2,
    },
    skeleton: {
      type: Boolean,
      default: true,
    },
  },
  data: () => ({
    id: `Settings${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    displayed: true,
  }),
  created() {
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  methods: {
    openDrawer() {
      this.$refs.spaceFormDrawer.open(this.spaceId);
    },
  },
};
</script>