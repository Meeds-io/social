<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <v-app>
    <main class="application-body position-static">
      <group-management-toolbar
        ref="toolbar"
        class="application-layout-style" />
      <v-card
        class="d-flex flex-column flex-md-row mt-5 transparent"
        min-width="100%"
        min-height="90%"
        flat>
        <v-card
          class="application-layout-style mb-2 me-0 me-md-5 mb-5 mb-md-0 flex-shrink-0 flex-grow-0"
          :width="$root.isMobile ? '100%' : '300px !important'"
          :max-width="$root.isMobile ? '100%' : '300px !important'"
          flat>
          <group-management-tree-toolbar class="pb-2" />
          <group-management-tree />
        </v-card>
        <div
          ref="main"
          class="application-layout-style flex-shrink-1 flex-grow-1 overflow-hidden">
          <div v-if="!$root.selectedGroup" class="d-flex fill-height border-box-sizing">
            <group-management-placeholder />
          </div>
          <span v-else class="d-flex flex-column">
            <div class="d-flex align-center justify-space-between flex-grow-1 text-no-wrap pa-4">
              <span class="text-title">{{ selectedGroupLabel }}</span>
              <v-btn
                icon
                :title="$t('GroupsManagement.groupSettings.title')"
                @click="$root.$emit('open-group-settings-drawer', $root.selectedGroup)">
                <v-icon size="20">fa fa-cog</v-icon>
              </v-btn>
            </div>
            <nested-groups-management />
            <group-members-management />
          </span>
        </div>
      </v-card>
    </main>
    <group-management-form-drawer />
    <group-management-membership-form-drawer />
    <group-management-settings-drawer />
  </v-app>
</template>
<script>
export default {
  computed: {
    selectedGroupLabel() {
      return this.$root?.selectedGroup?.label || '';
    }
  }
};
</script>


