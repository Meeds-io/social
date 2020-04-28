<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-app
    class="spacesAdministration"
    flat>
    <div class="uiTabAdvanced uiTabInPage">
      <ul class="nav nav-tabs">
        <li :class="{active: activeTab === 1}" @click="activeTab=1">
          <a href="#manage" data-toggle="tab">{{ $t('social.spaces.administration.manageSpaces') }}</a>
        </li>
        <li v-show="canChangePermissions" :class="{active: activeTab === 2}" @click="activeTab=2" >
          <a href="#permissions" data-toggle="tab">{{ $t('social.spaces.administration.permissions') }}</a>
        </li>
        <li :class="{active: activeTab === 3}" @click="activeTab=3">
          <a href="#spaceApplications" data-toggle="tab">{{ $t('social.spaces.applications') }}</a>
        </li>
        <li :class="{active: activeTab === 4}" @click="activeTab=4">
          <a href="#spaceTemplates" data-toggle="tab">{{ $t('social.spaces.templates') }}</a>
        </li>
        <li v-show="canChangePermissions" :class="{active: activeTab === 5}" @click="activeTab=5" >
          <a href="#bindingReports" data-toggle="tab">{{ $t('social.spaces.administration.bindingReports') }}</a>
        </li>
      </ul>
      <div class="tab-content">
        <div v-if="showManageSpaces" id="manage" class="tab-pane fade in active">
          <exo-spaces-administration-manage-spaces :can-bind-groups-and-spaces="canChangePermissions" @bindingReports="activeTab = 5"></exo-spaces-administration-manage-spaces>
        </div>
        <div v-else-if="showPermissions" id="permissions" class="tab-pane fade in active">
          <exo-spaces-administration-manage-permissions></exo-spaces-administration-manage-permissions>
        </div>
        <div v-else-if="showSpaceApplications" id="spaceApplications" class="tab-pane fade in active">
          <exo-space-applications :applications-by-category="applicationsByCategory" />
        </div>
        <div v-else-if="showSpaceTemplates" id="spaceTemplates" class="tab-pane fade in active">
          <exo-space-templates-spaces></exo-space-templates-spaces>
        </div>
        <div v-else-if="showBindingReports" id="bindingReports" class="tab-pane fade in active">
          <exo-spaces-administration-binding-reports></exo-spaces-administration-binding-reports>
        </div>
      </div>
    </div>
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
    spacesAdministrationServices.getUserPermissions(eXo.env.portal.userName).then(data => {
      if(data && data.platformAdministrator) {
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

