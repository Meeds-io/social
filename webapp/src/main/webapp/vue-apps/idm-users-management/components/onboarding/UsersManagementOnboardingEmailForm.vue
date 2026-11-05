<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <div class="pa-2 border-radius light-grey-background text-color">
    <div class="mb-2">{{ $t('UsersManagement.creation.email.hello') }}</div>
    <rich-editor
      :id="editorId"
      :value="initialMessage"
      :max-length="messageMaxLength"
      :disabled="saving"
      :tag-enabled="false"
      class="white border-radius"
      ck-editor-type="userInvitation"
      disable-suggester
      disable-image-attachment
      @input="$emit('update:message', $event)"
      @validity-updated="$emit('validity-updated', $event)" />
    <div v-if="errorMessages" class="error--text text-caption">{{ errorMessages }}</div>
    <div class="mt-2">{{ $t('UsersManagement.creation.email.confirmLink') }}</div>
    <div class="mt-3">{{ $t('UsersManagement.creation.email.tokenLink') }}</div>
    <div class="font-italic">
      {{ $t('UsersManagement.creation.email.expiry', {0: validityLabel}) }}
    </div>
    <div class="mt-3">{{ $t('UsersManagement.creation.email.bye') }}</div>
  </div>
</template>

<script>
export default {
  props: {
    editorId: {
      type: String,
      default: 'userInvitationMessageEditor',
    },
    message: {
      type: String,
      default: '',
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
  data: () => ({
    initialMessage: '',
    onboardingSettings: null,
  }),
  computed: {
    messageMaxLength() {
      return this.$userCreationService.MESSAGE_MAX_LENGTH;
    },
    // The validity as the email states it: the server says whether it is a
    // whole number of days, by the rule the email applies
    validityLabel() {
      if (!this.onboardingSettings) {
        return '…';
      } else if (this.onboardingSettings.linkValidityWholeDays) {
        return this.$t('UsersManagement.creation.email.expiry.days', {0: this.onboardingSettings.linkValidityDays});
      } else {
        return this.$t('UsersManagement.creation.email.expiry.hours', {0: this.onboardingSettings.linkValidityHours});
      }
    },
  },
  // The form is created at each open of its drawer, or when the onboarding
  // email is turned on: the message the design proposes stands in for a
  // blank one, held here before the editor is created since the editor
  // reads its value once, and handed to the drawer; the validity is read
  // again
  created() {
    this.initialMessage = this.message || this.$t('UsersManagement.creation.email.defaultMessage', {0: eXo?.env?.portal?.companyName || ''});
    if (this.initialMessage !== this.message) {
      this.$emit('update:message', this.initialMessage);
    }
    this.$userCreationService.getOnboardingSettings()
      .then(settings => this.onboardingSettings = settings)
      .catch(() => this.onboardingSettings = null);
  },
};
</script>
