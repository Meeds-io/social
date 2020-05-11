<template>
  <div v-if="displayed">
    <space-setting-applications-window
      v-if="displayDetails"
      :space="space"
      @back="closeDetail" />
    <v-card v-else class="border-radius" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
                {{ skeleton && '&nbsp;' || $t('SpaceSettings.applications') }}
              </div>
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :class="skeleton && 'skeleton-background'"
              small
              icon
              @click="openDetail">
              <v-icon v-if="!skeleton" size="24" class="text-sub-title">
                fa-caret-right
              </v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
  </div>
</template>

<script>
export default {
  props: {
    spaceId: {
      type: Number,
      default: 0,
    },
    skeleton: {
      type: Boolean,
      default: () => false,
    },
  },
  data: () => ({
    id: `SpaceApplications${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    space: null,
    displayed: true,
    displayDetails: false,
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
    openDetail() {
      this.$spaceService.getSpaceById(this.spaceId)
        .then(space => {
          this.space = space;

          document.dispatchEvent(new CustomEvent('hideSettingsApps', {detail: this.id}));
          this.displayDetails = true;
        });
    },
    closeDetail() {
      document.dispatchEvent(new CustomEvent('showSettingsApps'));
      this.displayDetails = false;
    },
  },
};
</script>

