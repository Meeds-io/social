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
  <v-list-item class="pa-0">
    <v-badge
      color="white d-flex align-center justify-center pa-0"
      class="externalBadge pa-0"
      bottom
      bordered
      offset-x="33"
      offset-y="26">
      <v-list-item-avatar class="ms-0">
        <v-img :src="defaultAvatar" class="border-color" />
      </v-list-item-avatar>
      <span slot="badge">
        <v-icon class="icon-default-color">fa-question</v-icon>
      </span>
    </v-badge>
    <v-list-item-content>
      <v-list-item-title
        :title="email"
        class="text-body text-truncate mb-1">
        {{ email }}
      </v-list-item-title>
      <v-list-item-subtitle class="text-truncate d-flex align-center">
        <span v-if="emailOnly && invitation.status === 'alreadySpaceMember'" class="error--text">
          {{ $t('SpaceSettings.invitation.alreadySpaceMember') }}
        </span>
        <span v-else-if="emailOnly && invitation.status === 'alreadyInvited'" class="error--text">
          {{ $t('SpaceSettings.invitation.alreadyInvited') }}
        </span>
        <span v-else-if="emailOnly && invitation.status === 'invalidEmail'" class="error--text">
          {{ $t('SpaceSettings.invitation.invalidEmail') }}
        </span>
        <span v-else-if="emailOnly && invitation.status === 'alreadyAddedInList'" class="error--text">
          {{ $t('SpaceSettings.invitation.alreadyAddedInList') }}
        </span>
        <template v-else-if="emailOnly && invitation.status === 'pending'">
          {{ $t('peopleList.label.pending') }}
        </template>
        <template v-else-if="!emailOnly && invitation.createdDate">
          {{ $t('peopleList.label.invitationSentOn') }}
          <date-format
            :value="invitation.createdDate"
            :format="format"
            class="ms-1" />
        </template>
        <template v-else-if="!emailOnly && !invitation.expired">
          {{ $t('peopleList.label.invitationSent') }}
        </template>
        <template v-if="!emailOnly && invitation.expired">
          <span class="mx-1">-</span>
          <span
            :title="$t('peopleList.label.invitationExpiredToolTip')"
            class="error--text">
            {{ $t('peopleList.label.invitationExpired') }}
          </span>
        </template>
      </v-list-item-subtitle>
    </v-list-item-content>
    <v-list-item-action class="ms-2">
      <v-btn
        :title="$t('SpaceSettings.roles.delete')"
        small
        icon
        @click="$emit('remove')">
        <v-icon size="18" color="error">
          fa-trash
        </v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    invitation: {
      type: Object,
      default: null,
    },
    emailOnly: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    format: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    },
  }),
  computed: {
    defaultAvatar() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`;
    },
    email() {
      return this.invitation.userEmail;
    },
  },
};
</script>