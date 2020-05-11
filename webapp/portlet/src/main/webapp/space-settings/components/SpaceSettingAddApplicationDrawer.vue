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
        v-for="(app, index) in applications"
        :key="app.id"
        class="d-flex flex-no-wrap justify-space-between border-radius border-color ma-3">
        <space-setting-application-card
          :application="app"
          :index="index"
          :length="applications.length"
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
  },
  data: () => ({
    savingSpace: false,
    spaceSaved: false,
    error: null,
    applications: [],
  }),
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
      .then(data => this.applications = data);
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
      if (this.spaceSaved || this.savingSpace) {
        return;
      }
      this.error = null;
      this.savingSpace = true;
      this.$spaceService.addApplication(this.spaceId, application.id)
        .then(() => {
          this.spaceSaved = true;
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