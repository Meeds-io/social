/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import { createLocalVue, shallowMount } from '@vue/test-utils';
import {spacesConstants} from '../../main/webapp/js/spacesConstants';
import ExoWhoIsOnline from '../../main/webapp/who-is-online-app/components/ExoWhoIsOnline';

const localVue = createLocalVue();

describe('ExoWhoIsOnline.test.js', () => {
  let cmp;
  const data = {
    users: [
      {
        'id': '1',
        'href': 'toto1',
        'avatar': 'titi1'
      },
      {
        'id': '2',
        'href': 'toto2',
        'avatar': 'titi2'
      }
    ],
  };

  beforeEach(() => {
    cmp = shallowMount(ExoWhoIsOnline, {
      localVue,
      stubs: {
      },
      mocks: {
        $t: () => {},
        $constants : spacesConstants
      }
    });
  });

  it('should display 2 users in list when 2 users in data', () => {
    cmp.vm.users = data.users;
    cmp.vm.$nextTick(() => {
      const usersList = cmp.findAll('li');
      expect(usersList).toHaveLength(2); // 2 rows
    });
  });
});