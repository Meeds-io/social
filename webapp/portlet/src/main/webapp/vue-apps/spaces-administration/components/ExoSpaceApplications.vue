<template>
  <v-card flat>
    <v-container class="border-box-sizing">
      <v-row dense>
        <v-col
          v-for="app in applications"
          :key="app.id"
          class="SpaceApplicationCard my-1 mx-auto mx-sm-1">
          <exo-space-application-card
            :application="app"
            use-menu
            @remove="remove(app)" />
        </v-col>
        <v-col class="SpaceApplicationCard dashed-border my-1 mx-auto mx-sm-1">
          <v-btn
            class="primary--text"
            width="100%"
            height="100%"
            text
            @click="openDrawer">
            <v-icon>mdi-plus</v-icon>
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <exo-space-add-application-drawer
      ref="spaceSettingAddApplicationDrawer"
      :installed-applications="applications"
      :applications-by-category="applicationsByCategory"
      @refresh="refresh" />
  </v-card>
</template>

<script>
export default {
  props: {
    applicationsByCategory: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    applications: [],
    saving: false,
  }),
  created() {
    this.refresh();
  },
  methods: {
    refresh() {
      return this.$spaceService.getSpaceApplicationsChoices().then(applications => this.applications = applications);
    },
    remove(application) {
      this.$spaceService.removeSpacesApplication(application.id)
        .then(this.refresh);
    },
    openDrawer() {
      this.$refs.spaceSettingAddApplicationDrawer.open();
    },
  },
};
</script>