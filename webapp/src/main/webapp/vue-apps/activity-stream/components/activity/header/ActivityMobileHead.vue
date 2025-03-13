<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
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
  <v-list-item
    v-if="displayUserAvatar"
    class="text-truncate">
    <exo-user-avatar
      avatar
      :identity="posterIdentity"
      :size="42" />
    <v-list-item-content class="py-0 accountTitleLabel ms-2">
      <v-list-item-title class="font-weight-bold d-flex mb-0">
        <exo-user-avatar
          bold-title
          extra-class="me-5 text-truncate"
          fullname
          :identity="posterIdentity"
          link-style
          username-class />
      </v-list-item-title>
      <v-list-item-subtitle>
        <activity-head-time
          :activity="activity"
          class="d-flex activity-head-time"
          :is-activity-shared="isActivityShared"
          is-mobile
          no-icon />
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
  <v-list-item
    v-else
    class="text-truncate">
    <exo-space-avatar
      avatar
      extra-class="text-truncate"
      :size="42"
      :space="space" />
    <exo-user-avatar
      avatar
      extra-class="ms-n4 mt-6"
      :identity="posterIdentity"
      :size="31" />
    <v-list-item-content class="py-0 accountTitleLabel text-truncate">
      <v-list-item-title class="font-weight-bold d-flex mb-0">
        <exo-space-avatar
          bold-title
          extra-class="text-truncate"
          fullname
          link-style
          :space="space"
          username-class />
      </v-list-item-title>
      <v-list-item-subtitle class="d-flex flex-row flex-nowrap">
        <exo-user-avatar
          extra-class="text-truncate ms-2 me-1"
          fullname
          :identity="posterIdentity"
          link-style
          small-font-size
          username-class />
        <activity-head-time
          :activity="activity"
          class="text-caption activity-head-time pt-0 ps-0"
          :is-activity-shared="isActivityShared"
          is-mobile
          no-icon />
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>

<script>
  export default {
    props: {
      activity: {
        type: Object,
        default: null,
      },
      isActivityShared: {
        type: Boolean,
        default: () => false,
      },
      posterIdentity: {
        type: Object,
        default: null,
      },
      space: {
        type: Object,
        default: null,
      },
    },
    computed: {
      displayUserAvatar () {
        return eXo.env.portal.spaceId !== '' || !this.space;
      },
    },

  };
</script>