<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <div
    id="UserHamburgerNavigation"
    class="white full-width">
    <div class="d-flex flex-row">
      <v-tooltip top>
        <template #activator="{ on, attrs }">
          <a
            :href="productLink"
            target="_blank"
            class="body-2 text-sub-title my-auto"
            v-bind="attrs"
            v-on="on">
            {{ productName }}
          </a>
        </template>
        <span>
          {{ $t('menu.productName.seeProduct') }}
        </span>
      </v-tooltip>
      <v-spacer />
      <div class="settingsTitle">
        <v-tooltip top>
          <template #activator="{ on, attrs }">
            <v-btn
              :href="settingsUrl"
              :aria-label="$t('menu.settings')"
              height="50px"
              min-width="40px"
              v-bind="attrs"
              v-on="on"
              icon>
              <v-icon size="22">
                fa-sliders-h
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.settings') }}
          </span>
        </v-tooltip>
      </div>
      <div class="logoutLinks me-n3">
        <v-tooltip top>
          <template #activator="{ on, attrs }">
            <v-btn
              :href="logoutUrl"
              :aria-label="$t('menu.logout')"
              height="50px"
              min-width="40px"
              v-bind="attrs"
              v-on="on"
              icon>
              <v-icon size="22">
                fa-power-off
              </v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.logout') }}
          </span>
        </v-tooltip>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  data: () => ({
    settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/settings`,
    logoutUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/?portal:action=Logout&portal:componentId=UIPortal`,
    productName: eXo.env.portal.productName,
    productLink: eXo.env.portal.productLink,
  }),
};
</script>

