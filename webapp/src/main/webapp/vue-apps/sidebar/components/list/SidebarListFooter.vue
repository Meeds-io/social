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
    flat
    height="48">
    <v-list-item
      class="my-auto"
      :class="!$root.expand && 'mx-0 px-0'"
      dense>
      <v-list-item-action
        v-if="$root.expand"
        class="me-auto font-weight-bold">
        <v-tooltip top>
          <template #activator="{ on, attrs }">
            <a
              :aria-label="$t('menu.productName.seeProduct')"
              class="text-body font-weight-bold my-auto"
              :href="productLink"
              target="_blank"
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
      <div
        v-else
        class="me-3"></div>
      <v-list-item-action
        class="my-auto d-flex flex-row"
        :class="$root.expand && 'mx-0' || 'ms-1 me-0'">
        <v-tooltip top>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              :aria-label="$t('menu.userProfilePageLink')"
              class="accountTitleItem my-auto"
              :class="!$root.expand && 'ms-n2px'"
              :href="profileUri"
              icon
              v-on="on">
              <v-avatar
                class="userAvatar"
                :href="profileUri"
                size="24">
                <img
                  :alt="userName"
                  contain
                  height="24"
                  :src="avatarUrl"
                  width="24">
              </v-avatar>
            </v-btn>
          </template>
          <span>{{ $t('menu.userProfilePageLink') }}</span>
        </v-tooltip>
        <v-tooltip
          v-if="$root.expand"
          top>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              :aria-label="$t('menu.settings')"
              class="userSettingsLink my-auto"
              :href="settingsUrl"
              icon
              v-on="on">
              <v-icon size="20">
                fa-sliders-h
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.settings') }}
          </span>
        </v-tooltip>
        <v-tooltip
          v-if="$root.expand"
          top>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              :aria-label="$t('menu.logout')"
              class="logoutLink me-n3 my-auto"
              :href="logoutUrl"
              icon
              v-on="on">
              <v-icon size="20">
                fa-power-off
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.logout') }}
          </span>
        </v-tooltip>
      </v-list-item-action>
    </v-list-item>
  </v-card>
</template>
<script>
  export default {
    data: () => ({
      settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/settings`,
      logoutUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/?portal:action=Logout&portal:componentId=UIPortal`,
      profileUri: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`,
      productName: eXo.env.portal.productName,
      productLink: eXo.env.portal.productLink,
      userName: eXo.env.portal.userName,
    }),
    computed: {
      avatarUrl () {
        return this.$root.avatarUrl;
      },
    },
  };
</script>