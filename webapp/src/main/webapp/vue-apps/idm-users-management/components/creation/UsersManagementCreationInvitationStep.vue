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
  <div class="px-4">
    <v-checkbox
      :input-value="sendOnboardingEmail"
      :label="$t('UsersManagement.creation.sendOnboardingEmail')"
      :disabled="saving"
      class="mt-0 pt-0"
      hide-details
      dense
      @change="$emit('update:send-onboarding-email', !!$event)" />
    <v-expand-transition>
      <users-management-onboarding-email-form
        v-if="sendOnboardingEmail"
        :message="message"
        :message-max-length="messageMaxLength"
        :error-messages="errorMessages"
        :validity-label="validityLabel"
        :saving="saving"
        class="mt-3"
        @update:message="$emit('update:message', $event)"
        @validity-updated="$emit('validity-updated', $event)" />
    </v-expand-transition>
  </div>
</template>

<script>
export default {
  props: {
    sendOnboardingEmail: {
      type: Boolean,
      default: false,
    },
    message: {
      type: String,
      default: '',
    },
    messageMaxLength: {
      type: Number,
      default: 500,
    },
    errorMessages: {
      type: String,
      default: null,
    },
    validityLabel: {
      type: String,
      default: '',
    },
    saving: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
