<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('social.admins.confirmDeleteAdminMembership')"
      :ok-label="$t('social.admins.button.ok')"
      :cancel-label="$t('social.admins.button.cancel')"
      @ok="ConfirmDeleteAdminMembership()" />
    <a :href="url">
      <v-list-item>
        <v-list-item-avatar>
          <v-img :src="avatarUrl" />
        </v-list-item-avatar>
        <v-list-item-content>
          <v-list-item-title>{{ fullName }}</v-list-item-title>
          <v-list-item-subtitle>{{ profession }}</v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action @click="deleteAdminMembership()">
          <v-btn
            :title="$t('social.admins.button.delete')"
            icon
            @click.stop.prevent="deleteAdminMembership()">
            <v-icon size="18" color="error">fa-trash</v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </a>
  </div>
</template>
<script>
export default {
  props: {
    membership: {
      type: Object,
      default: null
    }
  },
  data: () => ({
    user: null,
    deleteConfirmMessage: null
  }),
  computed: {
    avatarUrl() {
      return this.user?.avatar;
    },
    fullName() {
      return this.user?.fullname;
    },
    profession() {
      return this.user?.profession;
    },
    userName() {
      return this.membership?.userName || this.membership?.remoteId;
    },
    url() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.user?.username}` || '#';
    }
  },
  created() {
    this.$userService.getUser(this.userName)
      .then(user => this.user = user);
  },
  methods: {
    deleteAdminMembership() {
      this.deleteConfirmMessage = this.$t('social.admins.confirmDeleteAdminMembership.message', {0: this.fullName});
      this.$refs.deleteConfirmDialog.open();
    },
    ConfirmDeleteAdminMembership() {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?membershipId=${this.membership.id}`, {
        method: 'DELETE',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        }
        this.$root.$emit('platform-settings-admins-refresh');
      });
    },
  }
};
</script>