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
  <v-hover v-model="hover">
    <v-card
      class="d-flex flex-column overflow-hidden"
      min-height="100%"
      height="fit-content"
      max-height="100%"
      flat>
      <v-list
        v-if="menuItems"
        class="d-flex flex-column overflow-hidden py-0 flex-grow-1 flex-shrink-1">
        <v-card
          :style="hover && scrollTop && {
            'box-shadow' : 'rgb(0 0 0 / 30%) 0 6px 4px -4px',
          }"
          class="d-flex flex-grow-0 flex-shrink-0 my-auto no-border-bottom-left-radius no-border-bottom-right-radius"
          min-height="57"
          flat>
          <v-list-item
            :href="$root.defaultUserPath"
            class="d-flex">
            <div class="my-auto me-2 logoContainer" tile>
              <img
                :src="companyLogo"
                :alt="companyName"
                height="auto"
                width="36px">
            </div>
            <v-list-item-content>
              <v-list-item-title class="logoTitle font-weight-bold menu-text-color text-truncate">
                {{ companyName }}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </v-card>
        <div
          ref="menuContent"
          class="flex-grow-1 flex-shrink-1 overflow-x-hidden overflow-y-auto specific-scrollbar">
          <v-list-item-group :value="activeMenu">
            <sidebar-menu-item
              v-for="(item, index) in menuItems"
              :key="`${item.name}_${item.icon}_${index}`"
              :item="item" />
          </v-list-item-group>
        </div>
        <div
          :style="hover && scrollBottom && {
            'box-shadow' : 'rgba(0, 0, 0, 0.3) 0px 6px 7px 6px',
          }"
          class="flex-grow-0 flex-shrink-0">
          <v-list-item class="d-flex" dense>
            <v-list-item-action class="me-auto font-weight-bold">
              <v-tooltip top>
                <template #activator="{ on, attrs }">
                  <a
                    :href="productLink"
                    :aria-label="$t('menu.productName.seeProduct')"
                    target="_blank"
                    class="text-body font-weight-bold my-auto"
                    role="link"
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
            <v-list-item-action class="ms-auto my-auto d-flex flex-row">
              <v-tooltip top>
                <template #activator="{ on, attrs }">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    :href="profileUri"
                    :aria-label="$t('menu.userProfilePageLink')"
                    class="accountTitleItem my-auto"
                    icon>
                    <v-avatar
                      :href="profileUri"
                      class="userAvatar"
                      size="20">
                      <img
                        :src="avatarUrl"
                        :alt="userName"
                        height="20"
                        width="20"
                        contain>
                    </v-avatar>
                  </v-btn>
                </template>
                <span>{{ $t('menu.userProfilePageLink') }}</span>
              </v-tooltip>
              <v-tooltip top>
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
              <v-tooltip top>
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
        </div>
      </v-list>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  data: () => ({
    settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/settings`,
    logoutUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/?portal:action=Logout&portal:componentId=UIPortal`,
    profileUri: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile`,
    productName: eXo.env.portal.productName,
    productLink: eXo.env.portal.productLink,
    companyName: eXo.env.portal.companyName,
    companyLogo: eXo.env.portal.companyLogo,
    userName: eXo.env.portal.userName,
    scrollTop: false,
    scrollBottom: false,
    hover: false,
  }),
  computed: {
    menuItems() {
      return this.$root.settings?.items;
    },
    avatarUrl() {
      return this.$root.avatarUrl;
    },
    activeMenu() {
      if (eXo.env.portal.spaceGroup) {
        const selectedSpaceMenu = this.menuItems.find(item => item?.properties?.groupId?.length && window.location.pathname.includes(item.properties.groupId.replaceAll('/', ':')));
        return selectedSpaceMenu?.url;
      } else {
        const selectedSiteMenu = this.menuItems.find(item => item.url && window.location.pathname.includes(item.url));
        return selectedSiteMenu?.url;
      }
    },
  },
  watch: {
    hover() {
      this.computeScollPosition();
    }
  },
  created() {
    this.$socialWebSocket.initCometd('/SpaceWebNotification');
  },
  mounted() {
    const interval = window.setInterval(() => {
      if (this.$refs?.menuContent) {
        this.$refs.menuContent.addEventListener('scroll', this.computeScollPosition, false);
        window.clearInterval(interval);
      }
    }, 500);
  },
  methods: {
    computeScollPosition() {
      if (this.$refs?.menuContent) {
        this.scrollTop = !!this.$refs.menuContent.scrollTop;
        this.scrollBottom = parseInt(this.$refs.menuContent.scrollHeight - this.$refs.menuContent.offsetHeight - this.$refs.menuContent.scrollTop) > 2;
      }
    },
  }
};
</script>