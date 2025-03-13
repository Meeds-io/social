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
  <widget-wrapper
    v-if="$root.space"
    no-margin>
    <template #title>
      <v-list-item
        class="px-0"
        dense>
        <v-list-item-action class="my-auto me-3 ms-n2">
          <v-btn
            icon
            size="24"
            :title="$t('generalSettings.access.backToMain')"
            @click="$root.showMain">
            <v-icon
              class="icon-default-color"
              size="18">
              {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
            </v-icon>
          </v-btn>
        </v-list-item-action>
        <v-list-item-content>
          <v-list-item-title>
            <v-card
              class="flex-grow-0 text-title text-start py-1"
              flat
              :title="$t('SpaceSettings.backToMain')"
              @click="$root.showMain">
              {{ $t('SpaceSettings.roles') }}
            </v-card>
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
    <template #default>
      <v-switch
        id="SpaceSettingRestrictContent"
        v-model="isContentCreationRestricted"
        class="ma-0"
        :loading="saving"
        @click="switchContentRestriction">
        <template #label>
          <div class="text-body">
            {{ $t('SpaceSettings.roles.restrictContentCreation') }}
          </div>
        </template>
      </v-switch>
      <space-setting-roles-table
        class="mb-5"
        @redactors-loaded="redactorsChoosing = false"
        @restriction-loaded="isContentCreationRestricted = $event" />
      <space-setting-redactor-drawer
        ref="redactorsDrawer"
        @closed="$root.$emit('space-settings-refresh-redactors')" />
      <space-setting-users-list-drawer
        ref="usersListDrawer" />
      <space-setting-users-selection-drawer
        ref="usersSelectionDrawer" />
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
    created () {
      this.init();
    },
    methods: {
      init () {
        this.space = this.$root.space;
      },
      async switchContentRestriction () {
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
               
                await eXo.$spaceService.removeRedactor(this.$root.space.id, redactors[i].username);
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
      getAllRedactors () {
        return this.getUsers('redactor');
      },
      getAllPublishers () {
        return this.getUsers('publisher');
      },
      async getUsers (role, limit, noRecursive) {
        const data = await eXo.$spaceService.getSpaceMemberships({
          offset: 0,
          limit: limit || 100,
          status: role,
          expand: 'users',
          space: this.space.id,
          returnSize: false,
        });
        const users = data?.spacesMemberships?.map?.(m => m.user) || [];
        const size = data?.size || 0;
        if (!noRecursive && size > users.length) {
          return this.getUsers(role, size, true);
        } else {
          return users;
        }
      },
    },
  };
</script>