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
  <div class="d-flex flex-column align-center justify-center">
    <v-icon size="36">fa-people-arrows</v-icon>
    <div class="my-6">
      {{ $t('menu.spaces.noSpacesFound') }}
    </div>
    <template v-if="!keyword">
      <div v-if="$root.canAddSpaces">
        {{ $t('menu.spaces.joinOrCreateSpace') }}
      </div>
      <div v-else>
        {{ $t('menu.spaces.joinSpace') }}
      </div>
    </template>
    <v-btn
      :href="spacesLink"
      :title="$t('menu.spaces.exploreSpaces')"
      :class="keyword && 'mb-6' || 'my-6'"
      class="btn primary">
      <span class="text-none">
        {{ $t('menu.spaces.exploreSpaces') }}
      </span>
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    keyword: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    spacesLink: `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/spaces`,
  }),
};
</script>