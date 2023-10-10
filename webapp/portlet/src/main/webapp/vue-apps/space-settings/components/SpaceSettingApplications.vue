<template>
  <widget-wrapper v-if="displayed">
    <template v-if="!displayDetails" #title>
      {{ $t('SpaceSettings.applications') }}
    </template>
    <template v-if="!displayDetails" #action>
      <v-btn
        small
        icon
        @click="openDetail">
        <v-icon size="24" class="text-sub-title">
          {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
        </v-icon>
      </v-btn>
    </template>
    <space-setting-applications-window
      v-if="displayDetails"
      :space-id="spaceId"
      :applications="applications"
      @back="closeDetail"
      @refresh="refresh" />
  </widget-wrapper> 
</template>

<script>
export default {
  props: {
    spaceId: {
      type: Number,
      default: 0,
    },
  },
  data: () => ({
    id: `SpaceApplications${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    applications: null,
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
    refresh() {
      document.dispatchEvent(new CustomEvent('refreshSpaceNavigations'));

      this.$spaceService.getSpaceApplications(this.spaceId)
        .then(applications => {
          this.applications = applications;
        });
    },
    openDetail() {
      this.$spaceService.getSpaceApplications(this.spaceId)
        .then(applications => {
          this.applications = applications;

          document.dispatchEvent(new CustomEvent('hideSettingsApps', {detail: this.id}));
          this.displayDetails = true;
        });
    },
    closeDetail() {
      this.displayDetails = false;
      document.dispatchEvent(new CustomEvent('showSettingsApps'));
    },
  },
};
</script>

