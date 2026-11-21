<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <div class="d-flex align-center justify-center">
    <!-- The 'add a subspace' option, before 'see all', for a viewer the
         server says may create one. Always rendered and shown by the widget's
         hover-reveal-parent (hover or keyboard focus), whether or not the viewer
         may manage the widget: creating needs parent membership, not
         management. Listed state only: with no subspace yet the widget body already
         carries the one create button. -->
    <space-creation-button
      v-if="canCreateSubspace && hasSubspaces"
      :parent-space-id="$root.spaceId"
      :display-label="false"
      :icon-size="18"
      class="hover-reveal"
      color="primary"
      icon
      small
      require-form-drawer />
    <v-btn
      v-if="hasMore"
      :aria-label="$t('Widget.label.seeAll')"
      color="primary"
      small
      text
      link
      @click="$emit('see-all')">
      <span class="text-font-size text-none">{{ $t('Widget.label.seeAll') }}</span>
    </v-btn>
    <v-btn
      v-if="canManageSpace"
      :title="$t('subspacesList.settings.drawer.title')"
      :aria-label="$t('subspacesList.settings.drawer.title')"
      class="hover-reveal"
      small
      icon
      @click="$emit('settings')">
      <v-icon size="18">fa-cog</v-icon>
    </v-btn>
  </div>
</template>
<script>
/**
 * The widget header's action group: add a subspace, see all, settings. It
 * holds no state of its own and decides nothing: every flag is the server's
 * answer, handed down by the widget, and both actions are emitted back to it.
 * The add and settings buttons are shown by the widget's hover-reveal-parent.
 */
export default {
  props: {
    canManageSpace: {
      type: Boolean,
      default: false,
    },
    canCreateSubspace: {
      type: Boolean,
      default: false,
    },
    hasSubspaces: {
      type: Boolean,
      default: false,
    },
    hasMore: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
