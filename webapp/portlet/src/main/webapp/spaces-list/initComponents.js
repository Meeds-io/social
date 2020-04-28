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
import ExoSpacesList from './components/ExoSpacesList.vue';
import ExoSpacesCardList from './components/ExoSpacesCardList.vue';
import ExoSpacesToolbar from './components/ExoSpacesToolbar.vue';
import ExoSpaceCard from './components/ExoSpaceCard.vue';
import ExoSpaceCardFront from './components/ExoSpaceCardFront.vue';
import ExoSpaceCardReverse from './components/ExoSpaceCardReverse.vue';
import ExoSpaceManagersDrawer from './components/ExoSpaceManagersDrawer.vue';
import ExoSpaceFormDrawer from './components/ExoSpaceFormDrawer.vue';

const components = {
  'exo-spaces-list': ExoSpacesList,
  'exo-spaces-card-list': ExoSpacesCardList,
  'exo-spaces-toolbar': ExoSpacesToolbar,
  'exo-space-card': ExoSpaceCard,
  'exo-space-card-front': ExoSpaceCardFront,
  'exo-space-card-reverse': ExoSpaceCardReverse,
  'exo-space-managers-drawer': ExoSpaceManagersDrawer,
  'exo-space-form-drawer': ExoSpaceFormDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
