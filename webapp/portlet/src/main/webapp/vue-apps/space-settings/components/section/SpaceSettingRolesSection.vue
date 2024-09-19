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
  <widget-wrapper v-if="$root.space" no-margin>
    <template #title>
      <v-list-item class="px-0" dense>
        <v-list-item-action class="my-auto me-3 ms-n2">
          <v-btn
            :title="$t('generalSettings.access.backToMain')"
            size="24"
            icon
            @click="$root.showMain">
            <v-icon size="18" class="icon-default-color">
              {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
            </v-icon>
          </v-btn>
        </v-list-item-action>
        <v-list-item-content>
          <v-list-item-title>
            <v-card
              :title="$t('SpaceSettings.backToMain')"
              class="flex-grow-0 text-title text-start py-1"
              flat
              @click="$root.showMain">
              {{ $t('SpaceSettings.roles') }}
            </v-card>
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
    <template #default>
      <div>
        <v-switch
          v-model="isContentCreationRestricted"
          :loading="saving"
          class="ma-0"
          @click="switchContentRestriction">
          <template #label>
            <div class="text-body">{{ $t('SpaceSettings.roles.restrictContentCreation') }}</div>
          </template>
        </v-switch>
      </div>
      <space-setting-roles-table
        class="mb-5"
        @restriction-loaded="isContentCreationRestricted = $event"
        @redactors-loaded="redactorsChoosing = false" />
      <space-setting-redactor-drawer
        ref="redactorsDrawer"
        @closed="$root.$emit('space-settings-refresh-redactors')" />
      <space-setting-users-list-drawer
        ref="usersListDrawer" />
      <space-setting-users-selection-drawer
        ref="usersSelectionDrawer" />
      <space-setting-users-invitation-drawer
        ref="usersInvitationDrawer" />
    </template>
  </widget-wrapper>
</template>
<script>
export default {
  data: () => ({
    space: null,
    saving: false,
    isContentCreationRestricted: false,
    redactorsChoosing: true,
  }),
  created() {
    this.init();
  },
  methods: {
    init() {
      this.space = this.$root.space;
    },
    async switchContentRestriction() {
      await this.$nextTick();
      if (this.isContentCreationRestricted) {
        this.redactorsChoosing = true;
        const redactors = await this.getAllRedactors();
        const publishers = await this.getAllPublishers();
        this.$refs.redactorsDrawer.open(redactors, publishers);
      } else {
        this.saving = true;
        try {
          const redactors = await this.getAllRedactors();
          if (redactors?.length) {
            for (const i in redactors) {
              // eslint-disable-next-line no-await-in-loop
              await this.$spaceService.removeRedactor(this.$root.space.id, redactors[i].username);
            }
          }
          this.$root.$emit('alert-message', this.$t('SpaceSettings.roles.redactorsRemovedSuccessfully'), 'success');
        } catch (e) {
          this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
        } finally {
          this.$root.$emit('space-settings-redactors-updated', this.redactors);
          this.saving = false;
        }
      }
    },
    getAllRedactors(expand) {
      return this.getAllUsers('redactor', expand);
    },
    getAllPublishers(expand) {
      return this.getAllUsers('publisher', expand);
    },
    async getAllUsers(filter, expand) {
      let data = await this.$spaceService.getSpaceMembers(null, 0, 100, expand || '', filter, this.space.id);
      let users = data?.users;
      const size = data?.size;
      if (users?.length && size > users.length) {
        data = await this.$spaceService.getSpaceMembers(null, 0, size, expand || '', filter, this.space.id);
        users = data?.users;
      }
      return users;
    },
  },
};
</script>