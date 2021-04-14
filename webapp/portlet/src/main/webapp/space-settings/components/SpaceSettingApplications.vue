<template>
  <div v-if="displayed">
    <space-setting-applications-window
      v-if="displayDetails"
      :space-id="spaceId"
      :applications="applications"
      @back="closeDetail"
      @refresh="refresh" />
    <v-card
      v-else
      class="border-radius"
      flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('SpaceSettings.applications') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="openDetail">
              <v-icon size="24" class="text-sub-title">
                {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
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
          this.applications = applications.map(app => ({
            applicationName: app.applicationName,
            contentId: app.contentId,
            description: this.$t(`SpaceSettings.application.${/\s/.test(app.displayName) ? app.displayName.replace(/ /g,'.').toLowerCase() : app.displayName.toLowerCase()}.description`),
            displayName: this.$t(`SpaceSettings.application.${/\s/.test(app.displayName) ? app.displayName.replace(/ /g,'.').toLowerCase() : app.displayName.toLowerCase()}.title`),
            id: app.id(),
            removable: app.removable,
          }));
        });
    },
    openDetail() {
      this.$spaceService.getSpaceApplications(this.spaceId)
        .then(applications => {
          this.applications = applications.map(app => ({
            applicationName: app.applicationName,
            contentId: app.contentId,
            description: this.$t(`SpaceSettings.application.${/\s/.test(app.displayName) ? app.displayName.replace(/ /g,'.').toLowerCase() : app.displayName.toLowerCase()}.description`),
            displayName: this.$t(`SpaceSettings.application.${/\s/.test(app.displayName) ? app.displayName.replace(/ /g,'.').toLowerCase() : app.displayName.toLowerCase()}.title`),
            id: app.id,
            removable: app.removable,
          }));

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

