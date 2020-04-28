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
  <v-card class="peopleCardItem d-flex flex-column" flat>
    <v-btn
      absolute
      small
      icon
      class="peopleInfoIcon ma-2"
      @click="$emit('flip')">
      <v-icon size="12">fa-info</v-icon>
    </v-btn>
    <div class="d-flex pt-3 pb-2">
      <p class="font-weight-bold ma-auto">
        {{ $t('peopleList.label.aboutMe') }}
      </p>
    </div>
    <div class="aboutMeText align-center py-0 px-1 flex-grow-1">
      <ellipsis
        :title="user.aboutMe"
        :data="user.aboutMe"
        :line-clamp="5"
        end-char="...">
      </ellipsis>
    </div>

    <div class="d-flex pa-2 align-end flex-row">
      <div class="d-flex flex-column flex-grow-0 ml-2">
        <p class="font-weight-bold ma-auto mb-1">
          {{ user.connectionsCount }}
        </p>
        <p class="ma-auto text-sub-title">
          {{ $t('peopleList.title.connectionsCount') }}
        </p>
      </div>
      <div class="flex-grow-1"></div>
      <div class="d-flex flex-column flex-grow-0 mr-2">
        <p class="font-weight-bold ma-auto mb-1">
          {{ user.spacesCount }}
        </p>
        <p class="ma-auto text-sub-title">
          {{ $t('peopleList.title.spacesCount') }}
        </p>
      </div>
    </div>
  </v-card>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: null,
    },
  },
};
</script>

