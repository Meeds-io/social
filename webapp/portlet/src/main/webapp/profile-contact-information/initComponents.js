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
import ProfileContactInformation from './components/ProfileContactInformation.vue';
import ProfileContactInformationDrawer from './components/ProfileContactInformationDrawer.vue';
import ProfileContactPhone from './components/ProfileContactPhone.vue';
import ProfileContactIms from './components/ProfileContactIms.vue';
import ProfileContactUrls from './components/ProfileContactUrls.vue';
import ProfileContactEditIms from './components/ProfileContactEditIms.vue';
import ProfileContactEditPhone from './components/ProfileContactEditPhone.vue';
import ProfileContactEditUrls from './components/ProfileContactEditUrls.vue';
import ProfileContactEditMultiField from './components/ProfileContactEditMultiField.vue';
import ProfileContactEditMultiFieldSelect from './components/ProfileContactEditMultiFieldSelect.vue';

const components = {
  'profile-contact-information': ProfileContactInformation,
  'profile-contact-phone': ProfileContactPhone,
  'profile-contact-ims': ProfileContactIms,
  'profile-contact-urls': ProfileContactUrls,
  'profile-contact-information-drawer': ProfileContactInformationDrawer,
  'profile-contact-edit-multi-field': ProfileContactEditMultiField,
  'profile-contact-edit-multi-field-select': ProfileContactEditMultiFieldSelect,
  'profile-contact-edit-ims': ProfileContactEditIms,
  'profile-contact-edit-phone': ProfileContactEditPhone,
  'profile-contact-edit-urls': ProfileContactEditUrls,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
