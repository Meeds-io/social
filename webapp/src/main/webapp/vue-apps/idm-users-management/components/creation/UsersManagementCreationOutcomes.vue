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
<script>
/**
 * What follows the server's answer to a creation: the invitation after a
 * creation, a refused field, a creation the store did not complete. Renders
 * nothing: the drawer hands it the answer with what the request held (the
 * onboarding email and its message, the full name, the request's sequence),
 * read then rather than from the drawer, which may have been closed or
 * opened again meanwhile, and applies what it emits to its own state
 */
export default {
  render: h => h(),
  methods: {
    // The answer of the creation call: a created account to invite, a
    // refused field, a failure; a rejection carries a translated error
    handle(outcome, request) {
      if (outcome.userName) {
        return this.invite(outcome.userName, request);
      } else if (outcome.status === 400) {
        this.showRefusal(outcome.code, request);
      } else {
        this.showFailure(outcome.code, request);
      }
    },
    // The invitation is a second call once the account exists: its failure
    // leaves the account created, to be invited later
    invite(userName, request) {
      const invitation = request.sendOnboardingEmail
        ? this.$userCreationService.sendInvitation(userName, request.message)
        : Promise.resolve(true);
      return invitation.then(sent => {
        this.$root.$emit('refreshUsers');
        this.$root.$emit('pending-users-changed');
        if (request.extensionsNotSaved) {
          this.showIncomplete(userName, false, sent ? ['FIELDS'] : ['FIELDS', 'INVITATION'], request);
        } else if (sent) {
          this.$root.$emit('alert-message', this.$t('UsersManagement.creation.success', {0: userName}), 'success');
        } else {
          this.$root.$emit('alert-message', this.$t('UsersManagement.creation.invitationNotSent'), 'warning');
        }
        this.$emit('close', request);
      });
    },
    // A refused field: its code goes on the field, on the step holding it;
    // a username the server would not generate opens the username field
    showRefusal(code, request) {
      const refusal = this.$userCreationService.parseRefusal(code);
      if (!refusal) {
        throw this.$userCreationService.translatedError(this.translate, code);
      }
      this.$emit('refused', {
        field: refusal.field,
        message: this.$userCreationService.translateCode(this.translate, code) || refusal.detail || code,
        customizeUsername: refusal.prefix === 'USERNAME' && refusal.detail === 'GENERATION_NOT_ALLOWED',
        step: refusal.field === 'role' ? 2 : 1,
      }, request);
    },
    // Nothing created: the drawer stays open on the error. An account created
    // without all its parts: the drawer closes, and the drawer completing
    // the missing part opens on the account
    showFailure(code, request) {
      const incomplete = this.$userCreationService.parseIncomplete(code);
      if (!incomplete) {
        throw this.$userCreationService.translatedError(this.translate, code);
      }
      const missing = request.extensionsNotSaved ? incomplete.missing.concat('FIELDS') : incomplete.missing;
      this.showIncomplete(incomplete.userName, incomplete.deactivated, missing, request);
      this.$root.$emit('refreshUsers');
      this.$root.$emit('pending-users-changed');
      this.$emit('close', request);
      // The drawer that completes the first missing part: the memberships,
      // else the language; the activation status is the row's own action
      if (incomplete.missing.includes('MEMBERSHIPS')) {
        this.$root.$emit('openUserMemberships', {
          userName: incomplete.userName,
          fullname: request.fullName || incomplete.userName,
        });
      } else if (incomplete.missing.includes('LANGUAGE')) {
        this.$root.$emit('editUser', {
          userName: incomplete.userName,
          fullname: request.fullName || incomplete.userName,
        });
      }
    },
    // An account created without all its parts; the additional fields not
    // saved are completed from the Edit drawer, the alert's action
    showIncomplete(userName, deactivated, missing, request) {
      const missingLabels = missing.map(part => this.$t(`UsersManagement.creation.part.${part}`)).join(', ');
      const key = deactivated ? 'UsersManagement.creation.incomplete' : 'UsersManagement.creation.incompleteEnabled';
      const editAction = missing.includes('FIELDS')
        ? () => this.$root.$emit('editUser', {userName, fullname: request.fullName || userName})
        : null;
      this.$root.$emit('alert-message',
        this.$t(key, {0: userName, 1: missingLabels}),
        deactivated ? 'warning' : 'error',
        editAction,
        editAction && 'fa-edit',
        editAction && this.$t('UsersManagement.creation.editUser'));
    },
    translate(key) {
      return this.$t(key);
    },
  },
};
</script>
