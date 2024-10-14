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
  <v-menu
    v-if="$root.isExternalFeatureEnabled"
    content-class="application-menu z-index-modal"
    offset-y>
    <template #activator="{attrs, on}">
      <v-btn
        id="InviteUserToSpaceButton"
        :title="$t('SpaceSettings.users.inviteMembers')"
        color="primary"
        elevation="0"
        v-bind="attrs"
        v-on="on">
        <v-icon
          color="white"
          class="me-2"
          size="18">
          fa-plus
        </v-icon>
        {{ $t('SpaceSettings.users.invite') }}
      </v-btn>
    </template>
    <v-list max-width="300">
      <v-list-item
        id="InvitePlatformUserToSpaceButton"
        link
        @click="$root.$emit('space-settings-invite-member', true)">
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('SpaceSettings.users.button.inviteInternalMembers') }}</v-list-item-title>
          <v-list-item-subtitle class="text-truncate-3 text-wrap">{{ $t('SpaceSettings.users.button.inviteInternalMembers.description') }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
      <v-list-item
        id="InviteUserByEmailToSpaceButton"
        link
        @click="$root.$emit('space-settings-invite-email', true)">
        <v-list-item-content class="d-inline">
          <v-list-item-title>{{ $t('SpaceSettings.users.button.inviteByEmail') }}</v-list-item-title>
          <v-list-item-subtitle class="text-truncate-3 text-wrap">{{ $t('SpaceSettings.users.button.inviteByEmail.description') }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-menu>
  <v-btn
    v-else
    id="spaceSettingUsersListToolbar"
    :title="$t('SpaceSettings.users.invite')"
    color="primary"
    elevation="0"
    @click="$root.$emit('space-settings-invite-member', true)">
    <v-icon
      color="while"
      class="me-2"
      size="18">
      fa-plus
    </v-icon>
    {{ $t('SpaceSettings.users.invite') }}
  </v-btn>
</template>
<script></script>