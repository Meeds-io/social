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
  <v-card
    width="350px"
    max-width="100%"
    class="mx-auto"
    flat>
    <portal-forgot-password-expired
      v-if="action === 'expired'"
      :params="params" />
    <portal-forgot-password-reset-form
      v-else-if="action === 'resetPassword'"
      :params="params" />
    <portal-forgot-password-email-form
      v-else
      :params="params" />
  </v-card>
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
    action() {
      return this.params?.action;
    },
  },
};
</script>