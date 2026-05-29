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
  <exo-drawer
    id="groupManagementSettingsDrawer"
    ref="drawer"
    v-model="drawer"
    right>
    <template #title>
      {{ $t('GroupsManagement.groupSettings') }}
    </template>
    <template v-if="drawer" #content>
      <div class="application-body d-flex flex-column pa-5">
        <span class="text-header py-0">{{ $t('GroupsManagement.groupInfo') }}</span>
        <div class="d-flex align-center justify-space-between flex-grow-1 text-no-wrap">
          <span class="text-body text-capitalize-first-letter pt-4">{{ groupLabel }}</span>
          <v-btn
            icon
            :title="$t('GroupsManagement.editInfo.title')"
            @click="$root.$emit('edit-group', group)">
            <v-icon size="20">fa fa-edit</v-icon>
          </v-btn>
        </div>
        <span class="text-header pt-5">{{ $t('GroupsManagement.groupCharacteristics') }}</span>
      </div>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          class="btn ms-auto me-2"
          @click="cancel">
          {{ $t('GroupsManagement.button.cancel') }}
        </v-btn>
        <v-btn
          @click="apply"
          class="btn primary">
          {{ $t('activity.filter.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    group: null,
  }),
  computed: {
    groupLabel() {
      return this.group?.label || '';
    },
  },
  created() {
    this.$root.$on('open-group-settings-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-group-settings-drawer', this.open);
  },
  methods: {
    open(group) {
      this.group = group;
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
    },
    apply() {
      this.cancel();
    },
  },
};
</script>
