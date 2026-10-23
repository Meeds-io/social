/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
 * What the creation drawer does with the server's answer: the invitation
 * after a creation, a refused field, a creation the store did not complete
 */
export default {
  methods: {
    // The invitation is a second call once the account exists: its failure
    // leaves the account created, to be invited later
    invite(userName) {
      const invitation = this.sendOnboardingEmail
        ? userCreationService.sendInvitation(userName, this.message)
        : Promise.resolve(true);
      return invitation.then(sent => {
        this.$root.$emit('refreshUsers');
        this.$root.$emit('pending-users-changed');
        if (sent) {
          this.$root.$emit('alert-message', this.$t('UsersManagement.creation.success', {0: userName}), 'success');
        } else {
          this.$root.$emit('alert-message', this.$t('UsersManagement.creation.invitationNotSent'), 'warning');
        }
        this.close();
      });
    },
    // A refused field: its code is shown on the field, on the step holding it
    showRefusal(code) {
      const refusal = userCreationService.parseRefusal(code);
      if (!refusal) {
        throw this.translatedError(code);
      }
      this.setError(refusal.field, this.translateCode(code) || refusal.detail || code);
      if (refusal.prefix === 'USERNAME' && refusal.detail === 'GENERATION_NOT_ALLOWED') {
        this.customizeUsername = true;
      }
      this.stepper = refusal.field === 'role' ? 2 : 1;
    },
    // Nothing created: the drawer stays open on the error. An account created
    // without all its parts: the drawer closes, and the drawer completing
    // the missing part opens on the account
    showFailure(code) {
      const incomplete = userCreationService.parseIncomplete(code);
      if (!incomplete) {
        throw this.translatedError(code);
      }
      const missingLabels = incomplete.missing.map(part => this.$t(`UsersManagement.creation.part.${part}`)).join(', ');
      const key = incomplete.deactivated ? 'UsersManagement.creation.incomplete' : 'UsersManagement.creation.incompleteEnabled';
      this.$root.$emit('alert-message', this.$t(key, {0: incomplete.userName, 1: missingLabels}), incomplete.deactivated ? 'warning' : 'error');
      this.$root.$emit('refreshUsers');
      this.$root.$emit('pending-users-changed');
      this.close();
      // The drawer that completes the first missing part: the memberships,
      // else the language; the activation status is the row's own action
      if (incomplete.missing.includes('MEMBERSHIPS')) {
        this.$root.$emit('openUserMemberships', {
          userName: incomplete.userName,
          fullname: this.fullName || incomplete.userName,
        });
      } else if (incomplete.missing.includes('LANGUAGE')) {
        this.$root.$emit('editUser', {
          userName: incomplete.userName,
          fullname: this.fullName || incomplete.userName,
        });
      }
    },
    // An error whose message is already in the administrator's language
    translatedError(code) {
      const error = new Error(this.translateCode(code) || this.$t('IDMManagement.error.UnknownServerError'));
      error.translated = true;
      return error;
    },
    translateCode(code) {
      const key = `UsersManagement.creation.error.${(code || '').replace(/:/g, '.')}`;
      const label = this.$t(key);
      return label === key ? null : label;
    },
  },
};
