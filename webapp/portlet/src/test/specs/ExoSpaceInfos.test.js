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
import ExoSpaceInfos from '../../main/webapp/space-infos-app/components/ExoSpaceInfos';
import {spacesConstants} from '../../main/webapp/js/spacesConstants';

const localVue = createLocalVue();

describe('ExoSpaceInfos.test.js', () => {
  let cmp;
  const data = {
    description: 'This is a fake description',
    managers: [
      {
        'id':'1',
        'href':'http://localhost:8080/rest/v1/social/users/root',
        'identity':'http://localhost:8080/rest/v1/social/identities/1',
        'username':'root',
        'firstname':'Root',
        'lastname':'Root',
        'fullname':'Root Root',
        'email':'root@gatein.com',
        'avatar':'/rest/v1/social/users/root/avatar',
        'experiences':[],
        'ims':[],
        'url':[],
        'deleted':false
      },
      {
        'id':'2',
        'href':'http://localhost:8080/rest/v1/social/users/toto',
        'identity':'http://localhost:8080/rest/v1/social/identities/2',
        'username':'toto',
        'firstname':'Toto',
        'lastname':'Toto',
        'fullname':'Toto Toto',
        'email':'toto@gatein.com',
        'avatar':'/rest/v1/social/users/toto/avatar',
        'experiences':[],
        'ims':[],
        'url':[],
        'deleted':false
      },
    ],
  };
  beforeEach(() => {
    cmp = shallowMount(ExoSpaceInfos, {
      localVue,
      mocks: {
        $t: () => {},
        $constants : spacesConstants
      }
    });
  });

  it('should display 2 managers in list when 2 users in managers data', () => {
    cmp.vm.managers = data.managers;
    cmp.vm.$nextTick(() => {
      const managersList = cmp.findAll('.spaceManagerEntry');
      expect(managersList).toHaveLength(2); // 2 rows
    });
  });
  it('should display the description when description in data', () => {
    cmp.vm.description = data.description;
    cmp.vm.$nextTick(() => {
      const descr = cmp.findAll('p');
      expect(descr).toHaveLength(1);
      expect(descr.at(0).attributes().id).toBe('spaceDescription');
      expect(descr.at(0).text()).toBe('This is a fake description');
    });
  });
});