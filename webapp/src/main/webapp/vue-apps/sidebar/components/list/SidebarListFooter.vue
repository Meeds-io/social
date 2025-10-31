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
  <v-card
    class="d-flex"
    height="48"
    flat>
    <v-list-item
      :class="!$root.expand && 'mx-0 px-0'"
      class="my-auto"
      dense>
      <v-list-item-action v-if="$root.expand" class="me-auto font-weight-bold">
        <v-tooltip top>
          <template #activator="{ on, attrs }">
            <a
              :href="productLink"
              :aria-label="$t('menu.productName.seeProduct')"
              target="_blank"
              class="text-body font-weight-bold my-auto"
              v-bind="attrs"
              v-on="on">
              {{ productName }}
            </a>
          </template>
          <span>
            {{ $t('menu.productName.seeProduct') }}
          </span>
        </v-tooltip>
      </v-list-item-action>
      <v-spacer v-if="$root.expand" />
      <div v-else class="me-3"></div>
      <v-list-item-action
        :class="$root.expand && 'mx-0' || 'ms-2'"
        class="my-auto d-flex flex-row user-avatar-footer">
        <v-badge
          :color="statusColor"
          :class="{'me-2': $root.expand}"
          :value="true"
          class="my-auto pa-0"
          content=""
          offset-x="8"
          offset-y="8"
          bordered
          bottom
          overlap
          dot>
          <v-avatar
            :href="profileUri"
            class="userAvatar clickable"
            size="24"
            @click.stop="openMenu($event)">
            <img
              :src="avatarUrl"
              alt=""
              height="24"
              width="24"
              contain>
          </v-avatar>
        </v-badge>
        <v-tooltip v-if="$root.expand" top>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :href="settingsUrl"
              :aria-label="$t('menu.settings')"
              class="userSettingsLink my-auto"
              icon>
              <v-icon size="20">fa-sliders-h</v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.settings') }}
          </span>
        </v-tooltip>
        <v-tooltip v-if="$root.expand" top>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :href="logoutUrl"
              :aria-label="$t('menu.logout')"
              class="logoutLink me-n3 my-auto"
              icon>
              <v-icon size="20">fa-power-off</v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.logout') }}
          </span>
        </v-tooltip>
      </v-list-item-action>
    </v-list-item>
    <sidebar-user-popup
      ref="menu"
      v-if="$root.expand"
      attach-to=".user-avatar-footer"
      position-top="0"
      position-right="20"
      @user-status-updated="statusColor = $event" />
  </v-card>
</template>
<script>

export default {
  data: () => ({
    settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/settings`,
    logoutUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings/?portal:action=Logout&portal:componentId=UIPortal`,
    profileUri: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`,
    productName: eXo.env.portal.productName,
    productLink: eXo.env.portal.productLink,
    statusColor: '#707070'
  }),
  created() {
    document.addEventListener('user-status-updated', this.changeStatusColor);
  },
  beforeDestroy() {
    document.removeEventListener('user-status-updated', this.changeStatusColor);
  },
  computed: {
    avatarUrl() {
      return this.$root.avatarUrl;
    }
  },
  methods: {
    openMenu(event) {
      this.$refs?.menu?.open(event.clientX, event.clientY);
    },
    changeStatusColor(statusObject) {
      this.statusColor = statusObject.detail.color;
    }
  }
};
</script>
