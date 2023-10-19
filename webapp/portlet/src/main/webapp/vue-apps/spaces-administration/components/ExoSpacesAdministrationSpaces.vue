<template>
  <v-app
    class="spacesAdministration"
    flat>
    <main>
      <v-layout>
        <v-flex>
          <v-tabs
            v-model="selectedTab"
            class="card-border-radius overflow-hidden"
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
            <v-tab key="spaceApplications" href="#spaceApplications">
              {{ $t('social.spaces.applications') }}
            </v-tab>
            <v-tab key="spaceTemplates" href="#spaceTemplates">
              {{ $t('social.spaces.templates') }}
            </v-tab>
            <v-tab
              v-if="canChangePermissions"
              key="bindingReports"
              href="#bindingReports">
              {{ $t('social.spaces.administration.bindingReports') }}
            </v-tab>
          </v-tabs>

          <v-tabs-items v-model="selectedTab" class="mt-2 card-border-radius overflow-hidden">
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
              id="spaceApplications"
              value="spaceApplications"
              class="px-4 py-2">
              <exo-space-applications :applications-by-category="applicationsByCategory" />
            </v-tab-item>
            <v-tab-item
              id="spaceTemplates"
              value="spaceTemplates">
              <exo-space-templates-spaces />
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
import * as spacesAdministrationServices from '../spacesAdministrationServices';

export default {
  props: {
    applicationsByCategory: {
      type: Array,
      default: () => [],
    }
  },
  data() { 
    return {
      selectedTab: 'applications',
      activeTab: 1,
      canChangePermissions: false
    };
  },
  computed: {
    showManageSpaces() {
      return this.activeTab && this.activeTab === 1;
    },
    showPermissions() {
      const permissionTabNumber = 2;
      return this.canChangePermissions && this.activeTab && this.activeTab === permissionTabNumber;
    },
    showSpaceApplications() {
      return this.activeTab === 3;
    },
    showSpaceTemplates() {
      return this.activeTab === 4;
    },
    showBindingReports() {
      const bindingReportsTabNumber = 5;
      return this.canChangePermissions && this.activeTab && this.activeTab === bindingReportsTabNumber && this.canChangePermissions;
    }
  },
  created() {
    spacesAdministrationServices.getUserPermissions(eXo.env.portal.userName)
      .then(data => {
        if (data && data.platformAdministrator) {
          this.canChangePermissions = data.platformAdministrator;
        }
      });
  },
  mounted() {
    const windowLocationHash = window.location.hash;
    if (windowLocationHash === '#bindingReports') {
      this.activeTab = 5;
    } else if (windowLocationHash === '#permissions') {
      this.activeTab = 2;
    } else {
      this.activeTab = 1;
    }
  }
};
</script>

