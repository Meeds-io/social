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
    id="spaceSettingPendingUsersDrawer"
    ref="drawer"
    v-model="drawer"
    allow-expand
    go-back-button
    :loading="loading"
    :right="!$vuetify.rtl">
    <template #title>
      {{ $t('SpaceSettings.roles.pending.drawer.title') }}
    </template>
    <template #titleIcons>
      <div class="full-height d-flex align-center">
        <v-btn
          v-if="role !== 'pending'"
          color="primary"
          elevation="0"
          small
          :title="$t('SpaceSettings.users.inviteMembers')"
          @click="inviteMembers">
          <v-icon
            class="me-2"
            color="while"
            size="18">
            fa-plus
          </v-icon>
          {{ $t('SpaceSettings.users.invite') }}
        </v-btn>
      </div>
    </template>
    <template
      v-if="drawer"
      #content>
      <div class="d-flex overflow-hidden full-width">
        <v-tabs
          v-model="role"
          class="flex-grow-1 flex-shrink-1"
          slider-size="4">
          <v-tab
            href="#invited"
            tab-value="invited">
            {{ $t('SpaceSettings.roles.pending.tab.invitations') }}
            <v-avatar
              v-show="$root.space.invitedUsersCount"
              class="ms-2 pa-1 aspect-ratio-1 white--text content-box-sizing"
              color="secondary"
              height="auto"
              min-height="16"
              min-width="16"
              width="auto">
              {{ $root.space.invitedUsersCount }}
            </v-avatar>
          </v-tab>
          <v-tab
            href="#pending"
            tab-value="pending">
            {{ $t('SpaceSettings.roles.pending.tab.requests') }}
            <v-avatar
              v-show="$root.space.pendingUsersCount"
              class="ms-2 pa-1 aspect-ratio-1 white--text content-box-sizing"
              color="secondary"
              height="auto"
              min-height="16"
              min-width="16"
              width="auto">
              {{ $root.space.pendingUsersCount }}
            </v-avatar>
          </v-tab>
          <v-tab
            v-if="$root.isExternalFeatureEnabled"
            href="#external"
            tab-value="external">
            {{ $t('SpaceSettings.roles.pending.tab.externalInvitations') }}
            <v-avatar
              v-show="$root.externalInvitations?.length"
              class="ms-2 pa-1 aspect-ratio-1 white--text content-box-sizing"
              color="secondary"
              height="auto"
              min-height="16"
              min-width="16"
              width="auto">
              {{ $root.externalInvitations?.length || 0 }}
            </v-avatar>
          </v-tab>
        </v-tabs>
      </div>
      <v-tabs-items
        v-model="role"
        class="px-4">
        <v-tab-item value="invited">
          <v-list v-if="users?.length">
            <space-setting-role-list
              display-date
              :role="role"
              :users="users"
              @remove="cancelInvitation" />
          </v-list>
          <div
            v-else-if="!loading"
            class="d-flex flex-column align-center justify-center py-8">
            <v-icon
              color="secondary"
              size="54">
              fa-history
            </v-icon>
            <span class="my-4">{{ $t('SpaceSettings.placeholder.emptyInvitations') }}</span>
            <v-btn
              color="primary"
              elevation="0"
              @click="role = 'pending'">
              {{ $t('SpaceSettings.placeholder.seeRequests') }}
            </v-btn>
          </div>
        </v-tab-item>
        <v-tab-item value="pending">
          <v-list v-if="users?.length">
            <space-setting-role-list
              approve-button
              display-date
              :role="role"
              :users="users"
              @approve="acceptUserRequest"
              @remove="denyUserRequest" />
          </v-list>
          <div
            v-else-if="!loading"
            class="d-flex flex-column align-center justify-center py-8">
            <v-icon
              color="secondary"
              size="54">
              fa-history
            </v-icon>
            <span class="my-4">{{ $t('SpaceSettings.placeholder.emptyRequests') }}</span>
            <v-btn
              color="primary"
              elevation="0"
              @click="role = 'invited'">
              {{ $t('SpaceSettings.placeholder.seeInvitations') }}
            </v-btn>
          </div>
        </v-tab-item>
        <v-tab-item
          v-if="$root.isExternalFeatureEnabled"
          value="external">
          <v-list v-if="externalInvitations?.length">
            <space-setting-role-list
              :external-invitations="externalInvitations"
              @remove="cancelInvitation" />
          </v-list>
          <div
            v-else-if="!loading"
            class="d-flex flex-column align-center justify-center py-8">
            <v-icon
              color="secondary"
              size="54">
              fa-history
            </v-icon>
            <span class="my-4">{{ $t('SpaceSettings.placeholder.emptyInvitations') }}</span>
            <v-btn
              color="primary"
              elevation="0"
              @click="role = 'pending'">
              {{ $t('SpaceSettings.placeholder.seeRequests') }}
            </v-btn>
          </div>
        </v-tab-item>
      </v-tabs-items>
    </template>
    <template
      v-if="hasMore"
      #footer>
      <div class="d-flex">
        <v-btn
          block
          class="btn me-2"
          :loading="loading"
          @click="loadMore">
          <template>
            {{ $t('SpaceSettings.button.loadMore') }}
          </template>
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
  export default {
    data: () => ({
      drawer: false,
      loading: false,
      role: 'invited',
      pageSize: 0,
      limit: 0,
      memberSpaces: null,
      visitedSpaces: null,
      activeSpaces: null,
      hasMore: false,
      users: null,
      page: 0,
      size: 0,
    }),
    computed: {
      externalInvitations () {
        return this.$root.externalInvitations;
      },
    },
    watch: {
      role () {
        this.reset();
        this.refreshMemberships();
      },
    },
    created () {
      this.$root.$on('space-settings-users-pending-list-open', this.open);
      this.$root.$on('space-settings-pending-updated', this.refreshMemberships);
      this.$root.$on('space-settings-members-updated', this.refreshMemberships);
    },
    beforeUnmount () {
      this.$root.$off('space-settings-users-pending-list-open', this.open);
      this.$root.$off('space-settings-pending-updated', this.refreshMemberships);
      this.$root.$off('space-settings-members-updated', this.refreshMemberships);
    },
    methods: {
      open (role) {
        this.role = role || 'invited';
        this.reset();
        this.getSpaceMemberships();
        this.$refs.drawer.open();
      },
      reset () {
        this.pageSize = parseInt((window.innerHeight - 180) / 56);
        this.page = 0;
        this.size = 0;
        this.users = [];
        this.loading = false;
        this.hasMore = false;
      },
      refreshMemberships () {
        if (this.drawer) {
          this.getSpaceMemberships(true);
        }
      },
      loadMore () {
        this.page++;
        this.getSpaceMemberships();
      },
      async getSpaceMemberships (reset) {
        if (this.role === 'external') {
          if (reset) {
            this.users = [];
          }
          return;
        }
        this.loading = true;
        try {
          const data = await eXo.$spaceService.getSpaceMemberships({
            space: this.$root.spaceId,
            offset: reset ? 0 : this.page * this.pageSize,
            limit: reset ? (this.page + 1) * this.pageSize + 1 : this.pageSize + 1,
            status: this.role,
            returnSize: false,
            expand: this.$root.space.canEdit && 'users,createdDate' || 'users',
          });
          const users = data?.spacesMemberships;
          if (users?.length) {
            if (reset) {
              this.users = users.slice(0, this.pageSize).map(m => m?.user && ({
                ...m.user,
                createdDate: m.createdDate,
              })).filter(u => u);
            } else {
              this.users.push(...users.slice(0, this.pageSize).map(m => m?.user && ({
                ...m.user,
                createdDate: m.createdDate,
              })).filter(u => u));
            }
            this.hasMore = users.length > this.pageSize;
          } else {
            if (reset) {
              this.users = [];
            }
            this.hasMore = false;
          }
        } finally {
          this.loading = false;
        }
      },
      close () {
        this.$refs.drawer.close();
      },
      async cancelInvitation (user) {
        this.loading = true;
        try {
          if (user.username) {
            await eXo.$spaceService.cancelInvitation(this.$root.space.id, user.username);
          } else if (user.invitationId) {
            await eXo.$spaceService.declineExternalInvitation(this.$root.space.id, user.invitationId);
          } else {
            throw new Error('Unkown invitation type');
          }
          this.$root.$emit('alert-message', this.$t('SpaceSettings.roles.invitationDeletedSuccessfully'), 'success');
          this.$root.$emit('space-settings-pending-updated');
        } catch (e) {
          this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
        } finally {
          this.loading = false;
        }
      },
      async acceptUserRequest (user) {
        this.loading = true;
        try {
          await eXo.$spaceService.acceptUserRequest(this.$root.space.id, user.username);
          this.$root.$emit('alert-message', this.$t('SpaceSettings.roles.userAddedAsSpaceMember'), 'success');
          this.$root.$emit('space-settings-members-updated');
        } catch (e) {
          this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
        } finally {
          this.loading = false;
        }
      },
      inviteMembers () {
        if (this.role === 'invited') {
          this.$root.$emit('space-settings-invite-member', true);
        } else {
          this.$root.$emit('space-settings-invite-email', true);
        }
      },
      async denyUserRequest (user) {
        this.loading = true;
        try {
          await eXo.$spaceService.refuseUserRequest(this.$root.space.id, user.username);
          this.$root.$emit('alert-message', this.$t('SpaceSettings.roles.userRequestDenied'), 'success');
          this.$root.$emit('space-settings-pending-updated');
        } catch (e) {
          this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingRoles'), 'error');
        } finally {
          this.loading = false;
        }
      },
    },
  };
</script>
