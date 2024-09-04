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
  <v-card
    :href="link"
    class="d-flex flex-column align-center mx-3 py-3"
    min-height="125"
    width="115"
    outlined
    hover
    v-on="!link && {
      click: () => $emit('apply'),
    }">
    <v-img
      class="mx-auto flex-grow-0" 
      :src="`/social-portlet/images/emptyCol${iconIndex}.webp`"
      :height="iconSize"
      :width="iconSize"
      contain
      eager />
    <v-card
      max-width="150"
      class="px-2 pt-3"
      flat>
      {{ infoMessage }}
    </v-card>
  </v-card>
</template>
<script>
export default {
  props: {
    iconSize: {
      type: Number,
      default: () => 60,
    },
    iconIndex: {
      type: Number,
      default: () => 0,
    },
    infoMessage: {
      type: String,
      default: null
    },
    link: {
      type: String,
      default: null
    },
  },
};
</script>