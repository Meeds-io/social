<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
 
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
  <div class="brandingLogo">
    <v-img
      :src="brandingLogo"
      width="9em"
      max-width="150px"
      max-height="6em"
      role="presentation"
      contain
      eager />
  </div>
</template>
<script>
export default {
  props: {
    params: {
      type: Object,
      default: null,
    },
  },
  computed: {
    brandingLogo() {
      return this.params?.brandingLogo;
    },
  },
};
</script>