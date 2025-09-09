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
  <v-menu
    v-model="menu"
    offset-y>
    <template #activator="{on, attrs}">
      <v-btn
        v-bind="attrs"
        v-on="on"
        ref="menuButton"
        :id="`menuButton${item.id}`"
        :title="$t('UsersManagement.userActions')"
        :aria-label="$t('UsersManagement.userActions')"
        icon>
        <v-icon size="20">fas fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-list class="position-relative" dense>
      <v-tooltip :disabled="item.isInternal" bottom>
        <template #activator="{on, attrs}">
          <v-list-item
            v-if="!$root.isDelegatedAdministrator"
            v-on="on"
            v-bind="attrs"
            :disabled="!item.isInternal"
            class="px-2"
            dense
            @click="$root.$emit('editUser', item)">
            <v-list-item-icon class="mx-1 justify-center">
              <v-icon size="14">fa-edit</v-icon>
            </v-list-item-icon>
            <v-list-item-title class="ps-0">{{ $t('UsersManagement.edit') }}</v-list-item-title>
          </v-list-item>
        </template>
        <span>{{ $t('UsersManagement.tooltip.editSynchronzedUser') }}</span>
      </v-tooltip>
      <v-tooltip bottom>
        <template #activator="{on, attrs}">
          <div v-on="on" v-bind="attrs">
            <v-list-item
              :disabled="item.enrollmentStatus !== 'reInviteToJoin' && item.enrollmentStatus !== 'inviteToJoin'"
              class="px-2"
              dense
              @click="$emit('onboard')">
              <v-list-item-icon class="mx-1 justify-center">
                <v-icon
                  size="14"
                  :class="{
                    'text--disabled': item.enrollmentStatus !== 'reInviteToJoin' && item.enrollmentStatus !== 'inviteToJoin'
                  }">
                  fa-user-plus
                </v-icon>
              </v-list-item-icon>
              <v-list-item-title class="ps-0">{{ $t('UsersManagement.selection.onboard') }}</v-list-item-title>
            </v-list-item>
          </div>
        </template>
        <span>{{ item.enrollmentDetails }}</span>
      </v-tooltip>
      <v-list-item
        class="px-2"
        dense
        @click="$root.$emit('openUserMemberships', item)">
        <v-list-item-icon class="mx-1 justify-center">
          <v-icon size="14">fa-users</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="ps-0">{{ $t('UsersManagement.userMemberships') }}</v-list-item-title>
      </v-list-item>
      <v-list-item
        v-if="item.enabled"
        class="px-2"
        dense
        @click="$emit('disable')">
        <v-list-item-icon class="mx-1 justify-center">
          <v-icon size="14">fa-user-slash</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="ps-0">{{ $t('UsersManagement.selection.disable') }}</v-list-item-title>
      </v-list-item>
      <v-list-item
        v-else
        class="px-2"
        dense
        @click="$emit('enable')">
        <v-list-item-icon class="mx-1 justify-center">
          <v-icon size="14">fa-user</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="ps-0">{{ $t('UsersManagement.selection.enable') }}</v-list-item-title>
      </v-list-item>
      <v-list-item
        v-if="$root.isSuperUser"
        :disabled="!item.isInternal"
        class="px-2"
        dense
        @click="$emit('delete')">
        <v-list-item-icon class="mx-1 justify-center">
          <v-icon size="14" color="error">fa-trash</v-icon>
        </v-list-item-icon>
        <v-list-item-title class="ps-0">
          <div class="error--text">{{ $t('UsersManagement.button.deleteUser') }}</div>
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
<script>
export default {
  props: {
    item: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
  }),
  watch: {
    menu: {
      immediate: true,
      handler() {
        if (this.menu) {
          document.addEventListener('mouseup', this.closeMenu);
        } else {
          document.removeEventListener('mouseup', this.closeMenu);
        }
      },
    },
  },
  methods: {
    closeMenu() {
      if (this.menu) {
        this.$refs.menuButton?.$el?.click?.();
      }
    },
  },
};
</script>
