<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
    id="SpaceManagersDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    no-x-scroll
    right>
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.syncMembers') }}
    </template>
    <template v-if="space" #titleIcons>
      <v-btn
        icon
        @click="$root.$emit('space-administration-sync-reports-drawer-open', space)">
        <v-icon>fa-external-link-alt</v-icon>
      </v-btn>
    </template>
    <template v-if="drawer && space" #content>
      <div class="pa-4">
        <div class="mb-4">
          {{ $t('social.spaces.administration.manageSpaces.syncMembersDescription') }}
        </div>
        <div class="text-header mb-1">
          {{ $t('social.spaces.administration.manageSpaces.space') }}
        </div>
        <space-avatar
          :space="space"
          class="mb-4" />
        <div class="text-header mb-1">
          {{ $t('social.spaces.administration.manageSpaces.groupSyncedToYourSpace') }}
        </div>
        <spaces-administration-permissions
          v-if="groups"
          v-model="groups"
          admins
          users
          externals />
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('social.spaces.administration.manageSpaces.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="!modified"
          class="btn-primary"
          elevation="0"
          @click="apply">
          {{ $t('social.spaces.administration.manageSpaces.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    saving: false,
    space: null,
    groups: null,
    originalGroups: null,
    bindings: null,
  }),
  computed: {
    modified() {
      return JSON.stringify(this.groups) !== JSON.stringify(this.originalGroups);
    },
  },
  created() {
    this.$root.$on('space-administration-sync-members-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-sync-members-drawer-open', this.open);
  },
  methods: {
    async open(space) {
      this.space = space;
      this.$refs.drawer.open();
      if (this.space.hasBindings) {
        this.groups = null;
        this.bindings = null;
        this.originalGroups = null;
        const groups = await this.$spaceBindingService.getGroupSpaceBindings(this.space.id);
        this.bindings = groups?.groupSpaceBindings || [];
        this.originalGroups = this.bindings.map(b => b.group) || [];
        this.groups = this.originalGroups.slice();
      } else {
        this.groups = [];
        this.bindings = [];
        this.originalGroups = [];
      }
    },
    async apply() {
      this.saving = true;
      try {
        await this.$spaceBindingService.saveGroupsSpaceBindings(this.space.id, this.groups);
        this.$root.$emit('spaces-administration-list-refresh');
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceBindingUpdateSuccess'), 'success');
        const bindingsToDelete = this.bindings.filter(b => !this.groups.find(g => g === b.group));
        if (bindingsToDelete.length) {
          await Promise.all(bindingsToDelete.map(b => this.$spaceBindingService.removeBinding(b.id)));
        }
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceBindingUpdateError', {0: this.space.displayName}), 'error');
      } finally {
        this.saving = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>