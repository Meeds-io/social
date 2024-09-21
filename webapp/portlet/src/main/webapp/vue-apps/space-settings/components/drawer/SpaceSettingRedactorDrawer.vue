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
    ref="drawer"
    v-model="drawer"
    :loading="saving"
    class="spaceSettingsRedactorsDrawer"
    allow-expand
    right
    @closed="$emit('closed')">
    <template #title>
      {{ $t('SpaceSettings.roles.restrictContentCreation.drawer') }}
    </template>
    <template v-if="drawer" #content>
      <div class="pa-4">
        <div>
          {{ $t('SpaceSettings.roles.restrictContentCreation.placeholder1') }}
        </div>
        <div class="pt-2">
          {{ $t('SpaceSettings.roles.restrictContentCreation.placeholder2') }}
        </div>
        <div
          v-if="!originalRedactors.length && !publishers.length"
          class="pa-5 d-flex align-center justify-center">
          <v-icon
            color="secondary"
            class="me-2"
            size="18">
            fa-paper-plane
          </v-icon>
          <span>{{ $t('SpaceSettings.roles.noPublisher') }}</span>
        </div>
        <identity-suggester
          ref="suggester"
          id="userMemberSuggester"
          v-model="selectedUser"
          :labels="suggesterLabels"
          :search-options="searchOptions"
          :ignore-items="ignoreSuggesterItems"
          name="userMemberSuggester"
          class="user-suggester"
          width="220"
          include-users
          ignore-cache />
        <div v-if="redactors?.length">
          <space-setting-role-list
            :users="redactors"
            @add="addUserMembership"
            @remove="removeUserMembership" />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="saving"
          class="btn me-2"
          @click="close">
          <template>
            {{ $t('SpaceSettings.button.cancel') }}
          </template>
        </v-btn>
        <v-btn
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="saveRedactors">
          {{ $t('SpaceSettings.button.apply') }}
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
    selectedUser: null,
    originalRedactors: null,
    publishers: null,
    addedRedactors: null,
    removedRedactors: null,
  }),
  computed: {
    redactors() {
      const redactors = this.originalRedactors?.slice() || [];
      this.addedRedactors.forEach(u => redactors.push(u));
      this.removedRedactors.forEach(user => {
        const index = redactors.findIndex(u => u.id === user.id);
        if (index >= 0) {
          redactors.splice(index, 1);
        } else {
          // eslint-disable-next-line
          console.warn('User not found', user);
        }
      });
      return redactors;
    },
    ignoreSuggesterItems() {
      return this.redactors.map(u => `organization:${u.username}`);
    },
    suggesterLabels() {
      return {
        searchPlaceholder: this.$t('SpaceSettings.roles.redactor.searchPlaceholder'),
        placeholder: this.$t('SpaceSettings.roles.redactor.placeholder'),
        noDataLabel: this.$t('SpaceSettings.roles.redactor.noDataLabel'),
      };
    },
    searchOptions() {
      return {
        spacePrettyName: eXo.env.portal.spaceName,
        currentUser: false,
      };
    },
  },
  watch: {
    selectedUser() {
      if (this.selectedUser) {
        this.addUserMembership({
          id: this.selectedUser.identityId,
          fullname: this.selectedUser?.profile?.fullName,
          username: this.selectedUser?.remoteId,
          avatar: this.selectedUser?.profile?.avatarUrl,
          deleted: false,
          enabled: true,
        });
        this.$nextTick().then(() => this.selectedUser = null);
      }
    },
  },
  methods: {
    open(redactors, publishers) {
      this.publishers = publishers?.slice?.() || [];
      this.originalRedactors = redactors?.slice?.() || [];
      this.addedRedactors = !this.originalRedactors.length
        && this.publishers.slice()
        || [];
      this.removedRedactors = [];
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    removeUserMembership(user) {
      if (this.addedRedactors.find(u => u.id === user.id)) {
        const index = this.addedRedactors.findIndex(u => u.id === user.id);
        this.addedRedactors.splice(index, 1);
        this.addedRedactors = this.addedRedactors.slice();
      }
      if (!this.removedRedactors.find(u => u.id === user.id)
          && this.originalRedactors.find(u => u.id === user.id)) {
        this.removedRedactors.push(user);
      }
    },
    addUserMembership(user) {
      if (this.removedRedactors.find(u => u.id === user.id)) {
        const index = this.removedRedactors.findIndex(u => u.id === user.id);
        this.removedRedactors.splice(index, 1);
        this.removedRedactors = this.removedRedactors.slice();
      }
      if (!this.addedRedactors.find(u => u.id === user.id)
          && !this.originalRedactors.find(u => u.id === user.id)) {
        this.addedRedactors.push(user);
      }
    },
    async saveRedactors() {
      if (this.saving) {
        return;
      }
      this.error = null;
      this.saving = true;
      try {
        for (const i in this.addedRedactors) {
          // eslint-disable-next-line no-await-in-loop
          await this.$spaceService.promoteRedactor(this.$root.space.id, this.addedRedactors[i].username);
        }
        for (const i in this.removedRedactors) {
          // eslint-disable-next-line no-await-in-loop
          await this.$spaceService.removeRedactor(this.$root.space.id, this.removedRedactors[i].username);
        }
        this.$root.$emit('alert-message', this.$t('SpaceSettings.roles.redactorsUpdatedSuccessfully'), 'success');
        this.$refs.drawer.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
      } finally {
        this.$root.$emit('space-settings-redactors-updated', this.addedRedactors, this.removedRedactors);
        this.saving = false;
      }
    },
  },
};
</script>