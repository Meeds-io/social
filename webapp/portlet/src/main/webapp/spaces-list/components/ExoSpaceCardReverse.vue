<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
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
  <v-card :id="spaceMenuParentId" class="spaceCardItem d-flex flex-column" flat>
    <v-btn
      absolute
      small
      icon
      class="spaceInfoIcon ma-2"
      @click="$emit('flip')">
      <v-icon size="12">fa-info</v-icon>
    </v-btn>
    <div class="d-flex pt-3 pb-2">
      <p class="font-weight-bold ma-auto">
        {{ $t('spacesList.label.description') }}
      </p>
    </div>
    <v-card-text class="align-center pa-0 flex-grow-1">
      <ellipsis
        v-show="space.description"
        :title="space.description"
        :data="space.description"
        :line-clamp="3"
        end-char="...">
      </ellipsis>
    </v-card-text>

    <template v-if="space && space.managers && space.managers.length">
      <div class="d-flex px-3 pt-3">
        <p class="font-weight-bold ma-auto">
          {{ $t('spacesList.title.managers') }}
        </p>
      </div>
      <v-card-text class="align-center flex-grow-0">
        <exo-user-avatars-list
          :users="space.managers"
          max="5"
          @open-detail="$root.$emit('displaySpaceManagers', space)" />
      </v-card-text>
    </template>
  </v-card>
</template>

<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
};
</script>

