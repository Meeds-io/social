<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2026 Meeds Association
  contact@meeds.io
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
    class="d-flex verticalMenuBottomBar border-box-sizing"
    height="48"
    flat>
    <v-list-item class="my-auto" dense>
      <v-list-item-action class="me-auto font-weight-bold">
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
      <v-spacer />
      <v-list-item-action class="my-auto">
        <v-tooltip top>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :href="mainPortalLink"
              :title="$t('menu.goBackToMainPortal')"
              :aria-label="$t('menu.goBackToMainPortal')"
              class="goBackToMainPortalLink me-n2 my-auto"
              target="_blank"
              icon>
              <v-icon size="20">fa-home</v-icon>
            </v-btn>
          </template>
          <span>
            {{ $t('menu.goBackToMainPortal') }}
          </span>
        </v-tooltip>
      </v-list-item-action>
    </v-list-item>
  </v-card>
</template>
<script>
export default {
  data: () => ({
    productName: eXo.env.portal.productName,
    productLink: eXo.env.portal.productLink,
    mainPortalLink: eXo.env.portal.context,
  }),
};
</script>
