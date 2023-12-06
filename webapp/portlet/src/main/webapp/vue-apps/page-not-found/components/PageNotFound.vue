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
  <v-app class="singlePageApplication">
    <v-card
      class="pageNotFoundInfo py-12 text-center"
      flat>
      <v-card-text>
        <v-icon color="primary" class="fa-7x">
          fa-question-circle
        </v-icon>
      </v-card-text>
      <v-card-text class="text-h5 text-color">
        {{ $t('siteAccess.pageNotFoundTitle') }}
      </v-card-text>
      <v-card-text class="text-h5 text-sub-title">
        {{ $t('siteAccess.pageNotFoundSubTitle') }}
      </v-card-text>
      <v-card-actions class="justify-center py-5">
        <v-btn
          :href="defaultLink"
          color="primary"
          class="btn pageNotFoundLink">
          <span class="text-none">{{ $t('siteAccess.pageNotFoundButton') }}</span>
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-app>
</template>
<script>
export default {
  computed: {
    defaultLink() {
      return `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}`;
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>