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
  <v-app v-if="organizationalUnits.length" class="d-flex align-center">
    <v-card class="d-flex flex-column pa-5" flat>
      <div class="d-flex align-center">
        <template v-if="selectedOrganizationalUnit">
          <v-btn
            :title="$t('organizationalUnitMembers.back')"
            icon
            @click="goBack">
            <v-icon size="18" class="icon-default-color">
              {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
            </v-icon>
          </v-btn>
          <span class="text-color font-weight-bold text-truncate ms-2 mb-0">
            {{ $t('organizationalUnitMembers.title', {0: selectedOrganizationalUnit.label}) }}
          </span>
        </template>
        <span v-else class="text-header mb-2 text-truncate">
          {{ $t('myOrganizationalUnits.label') }}
        </span>
      </div>
      <v-expand-transition>
        <v-list
          v-if="!selectedOrganizationalUnit"
          class="pa-0"
          dense>
          <v-list-item
            v-for="organizationalUnit in organizationalUnits"
            :key="organizationalUnit.groupId"
            class="px-0">
            <v-list-item-content>
              <v-list-item-title
                :title="organizationalUnit.label"
                class="text-start text-truncate">
                {{ organizationalUnit.label }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action class="my-1">
              <v-btn
                :title="$t('organizationalUnitMembers.title', {0: organizationalUnit.label})"
                icon
                @click="openMembers(organizationalUnit)">
                <v-icon size="18" class="icon-default-color">
                  {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
                </v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
        <div v-else>
          <application-toolbar
            id="organizationalUnitMembersToolbar"
            :right-text-filter="{
              minCharacters: 3,
              placeholder: $t('organizationalUnitMembers.filterBy'),
              tooltip: $t('organizationalUnitMembers.filterBy')
            }"
            compact
            no-text-truncate
            @filter-text-input-end-typing="keyword = $event || ''" />
          <organizational-unit-members-list
            :group-id="selectedOrganizationalUnit.groupId"
            :keyword="keyword" />
          <organizational-unit-membership-drawer
            :group-id="selectedOrganizationalUnit.groupId" />
        </div>
      </v-expand-transition>
    </v-card>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    organizationalUnits: [],
    selectedOrganizationalUnit: null,
    keyword: '',
  }),
  created() {
    this.$myOrganizationalUnitsService.getMyOrganizationalUnits()
      .then(organizationalUnits => {
        this.organizationalUnits = organizationalUnits || [];
      });
  },
  methods: {
    openMembers(organizationalUnit) {
      this.selectedOrganizationalUnit = organizationalUnit;
      document.dispatchEvent(new CustomEvent('hideMyTeamApps'));
    },
    goBack() {
      this.selectedOrganizationalUnit = null;
      this.keyword = '';
      document.dispatchEvent(new CustomEvent('showMyTeamApps'));
    },
  },
};
</script>
