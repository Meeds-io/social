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
  <v-card class="transparent" flat>
    <confirm-dialog
      ref="dialog"
      :message="confirmDialogMessage"
      :title="$t('GroupsManagement.title.confirmDeleteMember')"
      :ok-label="$t('GroupsManagement.button.ok')"
      :cancel-label="$t('GroupsManagement.button.cancel')"
      @ok="deleteConfirm" />
    <v-list-item @click.prevent="openConfirmDialog" dense>
      <v-list-item-icon class="mx-1 justify-center">
        <v-icon
          class="error-color"
          small>
          fa-trash
        </v-icon>
      </v-list-item-icon>
      <v-list-item-title>
        <span class="error-color">{{ $t('label.remove') }}</span>
      </v-list-item-title>
    </v-list-item>
  </v-card>
</template>

<script>
export default {
  props: {
    item: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      memberships: [],
      loading: false,
      dialog: false
    };
  },
  computed: {
    selectedGroupId() {
      return this.$root.selectedGroup?.id;
    },
    userName() {
      return this.item?.userName;
    },
    fullName() {
      return this.item?.fullname;
    },
    confirmDialogMessage() {
      return this.$t('GroupsManagement.message.confirmDeleteMember', {0: this.fullName, 1: this.$root.selectedGroup?.label});
    }
  },
  async created() {
    await this.getUserMemberships();
  },
  methods: {
    async openConfirmDialog() {
      this.dialog = true;
      await this.$nextTick();
      window.setTimeout(() => this.$refs.dialog.open(), 200);
    },
    close() {
      window.setTimeout(() => this.dialog = false, 200);
    },
    async getUserMemberships() {
      try {
        const response = await fetch(
          `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${this.userName}/memberships?groupId=${this.selectedGroupId}`,
          {
            method: 'GET',
            credentials: 'include'
          }
        );
        if (!response?.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        }
        const data = await response.json();
        this.memberships = data?.entities || [];
      } catch (error) {
        console.error('Error while fetching memberships:', error);
      }
    },
    async deleteConfirm() {
      if (!this.memberships.length || this.loading) {
        return;
      }
      this.loading = true;
      try {
        await Promise.all(
          this.memberships.map(membership =>
            fetch(
              `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?membershipId=${membership.id}`,
              {
                method: 'DELETE',
                credentials: 'include'
              }
            ).then(response => {
              if (!response?.ok) {
                throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
              }
            })
          )
        );
        this.$root.$emit('refresh-group-members');
      } catch (error) {
        console.error('Error while deleting memberships:', error);
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
