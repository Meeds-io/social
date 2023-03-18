<template>
  <exo-drawer
    ref="spaceApplicationInstallerDrawer"
    class="spaceApplicationInstallerDrawer"
    right>
    <template slot="title">
      {{ $t('social.spaces.administration.manageSpaces.applications') }}
    </template>
    <template slot="content">
      <v-card
        width="420"
        max-width="100%"
        flat>
        <v-expansion-panels
          v-model="expanded"
          accordion
          flat
          focusable
          class="px-4">
          <exo-space-application-category-card
            v-for="(category, index) in applicationsByCategory"
            :key="category.id"
            :category="category"
            :expanded="expanded === index"
            :installed-applications="installedApplications"
            @addApplication="addApplication" />
        </v-expansion-panels>
      </v-card>
    </template>
  </exo-drawer>
</template>
<script>
const IDLE_TIME = 200;

export default {
  props: {
    applicationsByCategory: {
      type: Array,
      default: () => [],
    },
    installedApplications: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    expanded: 0,
    savingSpace: false,
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
      this.$spaceService.addSpacesApplication(application)
        .then(() => {
          this.$emit('refresh');

          window.setTimeout(() => {
            this.$refs.spaceApplicationInstallerDrawer.close();
          }, IDLE_TIME);
        })
        .catch(e => {
          // eslint-disable-next-line no-console
          console.warn('Error adding spaces application ', e);
        })
        .finally(() => this.savingSpace = false);
    }
  },
};
</script>