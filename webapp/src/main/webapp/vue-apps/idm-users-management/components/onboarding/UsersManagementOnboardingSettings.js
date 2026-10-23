/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
import * as userCreationService from '../../js/UserCreationService.js';

/**
 * The onboarding email's message and link validity, shared by the drawers
 * that send it: the creation drawer and the Onboard User drawer
 */
export default {
  data: () => ({
    message: '',
    messageValid: true,
    onboardingSettings: null,
    messageMaxLength: userCreationService.MESSAGE_MAX_LENGTH,
    companyName: eXo?.env?.portal?.companyName || '',
  }),
  computed: {
    // The validity as the email states it: the server says whether it is a
    // whole number of days, by the rule the email applies
    linkValidityLabel() {
      if (!this.onboardingSettings) {
        return '…';
      } else if (this.onboardingSettings.linkValidityWholeDays) {
        return this.$t('UsersManagement.creation.email.expiry.days', {0: this.onboardingSettings.linkValidityDays});
      } else {
        return this.$t('UsersManagement.creation.email.expiry.hours', {0: this.onboardingSettings.linkValidityHours});
      }
    },
  },
  methods: {
    // The message the design proposes, editable, counted, and the validity
    // read again at each open
    resetOnboardingEmail() {
      this.message = this.$t('UsersManagement.creation.email.defaultMessage', {0: this.companyName});
      this.messageValid = true;
      this.onboardingSettings = null;
      userCreationService.getOnboardingSettings()
        .then(settings => this.onboardingSettings = settings)
        .catch(() => this.onboardingSettings = null);
    },
  },
};
