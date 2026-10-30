<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

-->
<template>
  <v-radio-group
    :value="value"
    :disabled="saving"
    :error-messages="errorMessages"
    class="px-4 mt-0 pt-0"
    hide-details="auto"
    @change="$emit('input', $event)">
    <v-radio
      v-for="role in roles"
      :id="role.id"
      :key="role.value"
      :value="role.value"
      :label="role.text"
      class="userRoleRadio mb-3" />
  </v-radio-group>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: 'INTERNAL',
    },
    errorMessages: {
      type: String,
      default: null,
    },
    saving: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    roles() {
      return [{
        value: 'INTERNAL',
        id: 'userRoleRadioInternal',
        text: this.$t('UsersManagement.creation.role.internal'),
      }, {
        value: 'EXTERNAL',
        id: 'userRoleRadioExternal',
        text: this.$t('UsersManagement.creation.role.external'),
      }, {
        value: 'ADMINISTRATOR',
        id: 'userRoleRadioAdministrator',
        text: this.$t('UsersManagement.creation.role.administrator'),
      }];
    },
  },
};
</script>
