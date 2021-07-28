<template>
  <exo-drawer
    ref="spaceApplicationInstallerDrawer"
    right
    class="spaceApplicationInstallerDrawer">
    <template slot="title">
      {{ $t('SpaceSettings.title.addApplication') }}
    </template>
    <template slot="content">
      <div
        v-for="app in filteredApplications"
        :key="app.id"
        class="d-flex flex-no-wrap justify-space-between border-radius border-color ma-3">
        <space-setting-application-card
          :application="app"
          class="flex-grow-1"
          @add="addApplication(app)" />
      </div>
    </template>
  </exo-drawer>
</template>
<script>
const IDLE_TIME = 200;

export default {
  props: {
    spaceId: {
      type: String,
      default: null,
    },
    installedApplications: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    savingSpace: false,
    applications: [],
  }),
  computed: {
    filteredApplications() {
      if (!this.applications || !this.applications.length) {
        return [];
      }
      return this.applications.filter(app => !this.installedApplications.find(installedApp => installedApp.contentId === app.contentId));
    },
  },
  watch: {
    savingSpace() {
      if (this.savingSpace) {
        this.$refs.spaceApplicationInstallerDrawer.startLoading();
      } else {
        this.$refs.spaceApplicationInstallerDrawer.endLoading();
      }
    },
  },
  created() {
    this.$spaceService.getSpaceApplicationsChoices()
      .then(data =>
        this.applications = data
      );
  },
  methods: {
    open() {
      this.savingSpace = false;
      this.$refs.spaceApplicationInstallerDrawer.open();
    },
    cancel() {
      this.$refs.spaceApplicationInstallerDrawer.close();
    },
    addApplication(application) {
      if (this.savingSpace) {
        return;
      }
      this.savingSpace = true;
      this.$spaceService.addApplication(this.spaceId, application.contentId.split('/')[1])
        .then(() => {
          this.$emit('refresh');

          window.setTimeout(() => {
            this.$refs.spaceApplicationInstallerDrawer.close();
          }, IDLE_TIME);
        })
        .catch(e => {
          // eslint-disable-next-line no-console
          console.warn('Error adding application to space ', e);
        })
        .finally(() => this.savingSpace = false);
    }
  },
};
</script>