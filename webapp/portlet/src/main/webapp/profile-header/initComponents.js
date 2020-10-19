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
import ProfileHeader from './components/ProfileHeader.vue';
import ProfileHeaderText from './components/ProfileHeaderText.vue';
import ProfileHeaderActions from './components/ProfileHeaderActions.vue';
import ProfileHeaderAvatar from './components/ProfileHeaderAvatar.vue';
import ProfileHeaderBannerButton from './components/ProfileHeaderBannerButton.vue';

const components = {
  'profile-header': ProfileHeader,
  'profile-header-avatar': ProfileHeaderAvatar,
  'profile-header-banner-button': ProfileHeaderBannerButton,
  'profile-header-actions': ProfileHeaderActions,
  'profile-header-text': ProfileHeaderText,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
