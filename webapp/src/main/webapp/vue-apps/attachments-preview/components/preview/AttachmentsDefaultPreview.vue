<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-card flat class="transparent absolute-all-center align-center">
    <v-icon size="60" :color="fileIconColor">
      {{ fileIconClass }}
    </v-icon>
    <div class="my-3 text-body">
      {{ attachment.filename }}
    </div>
    <div class="my-3 text-body font-weight-bold"> {{ $t('attachment.fileNotPreviewed') }} </div>
    <v-btn
      color="primary"
      text
      :href="downloadURL"
      :download="attachment.filename">
      {{ $t('attachment.downloadFile') }}
    </v-btn>
  </v-card>
</template>
<script>
export default {

  props: {
    attachment: {
      type: Object,
      default: null,
    }
  },
  computed: {
    fileIconClass() {
      return this.attachment.icon?.class || 'fas fa-file';
    },
    fileIconColor() {
      return this.attachment.icon?.color || 'secondary';
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    downloadURL() {
      return `${eXo.env.portal.context}${ this.attachment.downloadUrl}?size=0x0&download=true`;
    },
  },
};
</script>

