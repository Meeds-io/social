/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import * as registry from './js/ProfileFormFieldsRegistry.js';

// The additional fields of the account form: rendered at the insertion point
// of the users administration's Add user and Edit user forms, checked before
// the account is written, written once it is
export function registerExtensions() {
  extensionRegistry.registerExtension('UsersManagement', 'account-form', {
    id: 'profile-fields',
    rank: 10,
    vueComponent: Vue.options.components['profile-form-fields'],
    // The fields' own rules, then the server's dry run; true when the list
    // holds no attribute or no form is rendered. When the list could not be
    // read, the server's dry run decides on creation, and nothing is sent on
    // an account's edition
    validate(context) {
      const component = registry.get(context);
      return component ? component.validate() : Promise.resolve(true);
    },
    // The values the form held when it was submitted, written once the
    // account exists. A form that listed attributes, or could not read them,
    // writes even with nothing entered: the server's write refuses an
    // account missing a Required attribute
    execute(context, account) {
      if (context?.mode === 'edit') {
        return executeEdition(context, account);
      }
      const loadState = registry.getLoadState(context);
      if (!context?.data?.length && loadState !== 'listed' && loadState !== 'failed') {
        return Promise.resolve();
      }
      return Vue.prototype.$profileFormFieldService.saveAccountFields(account.userName, context.data || [])
        .then(outcome => {
          if (!outcome.ok) {
            throw new Error(outcome.refusal?.code || `Additional fields not saved: ${outcome.status}`);
          }
        });
    },
  });

  // The additional fields of the registration form: rendered in its profile
  // section, checked before the registration is sent, and sent with it under
  // the request parameter, the registration validating them before it
  // creates the account
  extensionRegistry.registerExtension('ExternalOnboarding', 'register-form', {
    id: 'profile-fields',
    rank: 10,
    vueComponent: Vue.options.components['profile-form-fields'],
    requestParameter: 'profileProperties',
    // The fields' own rules once the list has answered; false when it could
    // not be read, the required attributes being unknown
    validate(context) {
      const component = registry.get(context);
      return component ? component.validate() : Promise.resolve(true);
    },
  });

  // The additional fields of the onboarding page: rendered after its e-mail,
  // holding the values the account has, checked before the password is sent,
  // and sent with it under the request parameter, the attributes the user
  // changed only, the request validating them before it sets the password
  extensionRegistry.registerExtension('InternalOnboarding', 'onboarding-form', {
    id: 'profile-fields',
    rank: 10,
    vueComponent: Vue.options.components['profile-form-fields'],
    requestParameter: 'profileProperties',
    // The fields' own rules once the list has answered; false when it could
    // not be read, the required attributes being unknown
    validate(context) {
      const component = registry.get(context);
      return component ? component.validate() : Promise.resolve(true);
    },
  });
}

// On an account's edition, the fields' own rules once more, since a Save
// after a failed write sends that write alone, then the attributes the
// administrator changed, when there are any. A refused value shows on its
// field, the form being still open on that account
function executeEdition(context, account) {
  const component = registry.get(context);
  if (component && !component.checkRules()) {
    return Promise.reject(new Error('Additional fields refused by their rules'));
  } else if (!context?.data?.length) {
    return Promise.resolve();
  }
  return Vue.prototype.$profileFormFieldService.saveUserFields(account.userName, context.data)
    .then(outcome => {
      if (!outcome.ok) {
        if (outcome.status === 400) {
          registry.get(context)?.showRefusal(outcome.refusal);
        }
        throw new Error(outcome.refusal?.code || `Additional fields not saved: ${outcome.status}`);
      }
    });
}
