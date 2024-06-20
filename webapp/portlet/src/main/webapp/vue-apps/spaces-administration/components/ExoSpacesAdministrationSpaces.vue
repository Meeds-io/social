<template>
  <v-app
    class="spacesAdministration"
    flat>
    <main class="application-body">
      <v-layout>
        <v-flex>
          <v-tabs
            v-model="selectedTab"
            slider-size="4">
            <v-tab key="manage" href="#manage">
              {{ $t('social.spaces.administration.manageSpaces') }}
            </v-tab>
            <v-tab
              v-if="canChangePermissions"
              key="permissions"
              href="#permissions">
              {{ $t('social.spaces.administration.permissions') }}
            </v-tab>
            <v-tab
              v-if="canChangePermissions"
              key="bindingReports"
              href="#bindingReports">
              {{ $t('social.spaces.administration.bindingReports') }}
            </v-tab>
          </v-tabs>

          <v-tabs-items v-model="selectedTab" class="mt-2">
            <v-tab-item
              id="manage"
              value="manage"
              class="px-4 py-2">
              <exo-spaces-administration-manage-spaces :can-bind-groups-and-spaces="canChangePermissions" @bindingReports="activeTab = 5" />
            </v-tab-item>
            <v-tab-item
              v-if="canChangePermissions"
              id="permissions"
              value="permissions">
              <exo-spaces-administration-manage-permissions />
            </v-tab-item>
            <v-tab-item
              v-if="canChangePermissions"
              id="bindingReports"
              value="bindingReports"
              class="px-4 py-2">
              <exo-spaces-administration-binding-reports />
            </v-tab-item>
          </v-tabs-items>
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
export default {
  props: {
    applicationsByCategory: {
      type: Array,
      default: () => [],
    }
  },
  data() { 
    return {
      selectedTab: 'manage',
      activeTab: 1,
      canChangePermissions: false
    };
  },
  computed: {
    showManageSpaces() {
      return this.activeTab === 1;
    },
    showPermissions() {
      return this.activeTab === 2 && this.canChangePermissions;
    },
    showBindingReports() {
      return this.activeTab === 3 && this.canChangePermissions && this.canChangePermissions;
    }
  },
  created() {
    this.$spacesAdministrationServices.getUserPermissions(eXo.env.portal.userName)
      .then(data => {
        if (data && data.platformAdministrator) {
          this.canChangePermissions = data.platformAdministrator;
        }
      });
  },
  mounted() {
    const windowLocationHash = window.location.hash;
    if (windowLocationHash === '#bindingReports') {
      this.activeTab = 3;
    } else if (windowLocationHash === '#permissions') {
      this.activeTab = 2;
    } else {
      this.activeTab = 1;
    }
  }
};
</script>

