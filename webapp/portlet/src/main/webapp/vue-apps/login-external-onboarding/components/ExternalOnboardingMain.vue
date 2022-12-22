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
    width="600px"
    max-width="100%"
    class="mx-auto"
    flat>
    <v-card-title class="display-1 primary--text px-0 center d-none d-sm-block">
      {{ companyName }}
    </v-card-title>

    <portal-external-onboarding-expired
      v-if="action === 'expired'"
      :params="params" />
    <portal-external-onboarding-create-user-form
      v-else
      :params="params" />

    <alert-notifications />
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
    successMessage() {
      return this.params?.success;
    },
    errorMessage() {
      return this.params?.error;
    },
    companyName() {
      return this.params?.companyName;
    },
  },
  watch: {
    successMessage: {
      immediate: true,
      handler: function() {
        if (this.successMessage?.trim()?.length) {
          this.displayAlert(this.successMessage, 'success');
        }
      },
    },
    errorMessage: {
      immediate: true,
      handler: function() {
        if (this.errorMessage?.trim()?.length) {
          this.displayAlert(this.errorMessage, 'error');
        }
      },
    },
  },
  methods: {
    displayAlert(message, type) {
      window.setTimeout(() => {
        this.$root.$emit('alert-message', message, type);
      }, 200);
    }
  }
};
</script>