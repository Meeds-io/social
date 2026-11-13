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
// of the users administration's Add user form, checked before the account is
// created, written once it exists
export function registerExtensions() {
  extensionRegistry.registerExtension('UsersManagement', 'account-form', {
    id: 'profile-fields',
    rank: 10,
    vueComponent: Vue.options.components['profile-form-fields'],
    // The fields' own rules, then the server's dry run; true when the list
    // holds no attribute or no form is rendered. When the list could not be
    // read, the server's dry run decides
    validate(context) {
      const component = registry.get(context);
      return component ? component.validate() : Promise.resolve(true);
    },
    // The values the form held when it was submitted, written once the
    // account exists. A form that listed attributes, or could not read them,
    // writes even with nothing entered: the server's write refuses an
    // account missing a Required attribute
    execute(context, account) {
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
}
