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
  <component
    :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
    ref="actionMenu"
    v-model="menu"
    :attach="$root.isMobile && '#vuetify-apps'"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    transition="slide-x-reverse-transition"
    content-class="position-absolute application-menu z-index-modal"
    offset-y
    eager>
    <template #activator="{ attrs, on }">
      <v-btn
        v-bind="attrs"
        v-on="on"
        class="pa-0"
        icon>
        <v-icon
          size="16"
          class="icon-default-color">
          fa-ellipsis-v
        </v-icon>
      </v-btn>
    </template>
    <v-list class="pa-0" dense>
      <v-list-item @click.prevent="openGroupMembershipDrawer" dense>
        <v-list-item-icon class="mx-1 justify-center">
          <v-icon small>fa-users</v-icon>
        </v-list-item-icon>
        <v-list-item-title>{{ $t('groupsManagement.members.editMembership') }}</v-list-item-title>
      </v-list-item>
      <group-members-delete-menu-item :item="member" />
    </v-list>
  </component>
</template>
<script>
export default {
  props: {
    member: {
      type: Object,
      default: null
    }
  },
  data: () => ({
    id: Math.random(), // NOSONAR
    menu: false
  }),
  watch: {
    menu() {
      // Workaround to fix closing menu when clicking outside
      if (this.menu) {
        document.addEventListener('mousedown', this.closeMenu);
      } else {
        document.removeEventListener('mousedown', this.closeMenu);
      }
    },
  },
  methods: {
    openGroupMembershipDrawer() {
      this.$root.$emit('open-group-membership-drawer', this.member);
    },
    closeMenu(event) {
      if (event !== this.id) {
        if (event?.target) {
          window.setTimeout(() => {
            this.menu = false;
          }, 200);
        } else {
          this.menu = false;
        }
      }
    },
  }
};
</script>
