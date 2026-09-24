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
         server says may create one. Offered on plain hover, not on hoverEdit:
         creating needs parent membership, not management. Mounted on first
         hover rather than v-show'n, because the shared button fetches the
         space templates in its own init; the results are cached on $root, so
         re-mounting costs nothing. Listed state only: with no subspace yet the
         widget body already carries the one create button. -->
    <v-fab-transition hide-on-leave>
      <space-creation-button
        v-if="canCreateSubspace && hasSubspaces && hover"
        :parent-space-id="$root.spaceId"
        :display-label="false"
        :icon-size="18"
        color="primary"
        icon
        small
        require-form-drawer />
    </v-fab-transition>
    <!-- The affordance every dashboard widget shares (analytics ->
         SpacesListWidget.vue): 'see all' reads as a label until the widget is
         hovered by someone who may manage it, then becomes an icon so the cog
         can sit beside it. -->
    <v-btn
      v-if="hasMore"
      :icon="hoverEdit"
      :text="!hoverEdit"
      :title="hoverEdit && $t('Widget.label.seeAll') || null"
      color="primary"
      small
      link
      @click="$emit('see-all')">
      <v-icon v-if="hoverEdit" size="18">fa-external-link-alt</v-icon>
      <span v-else class="text-font-size text-none">{{ $t('Widget.label.seeAll') }}</span>
    </v-btn>
    <v-fab-transition hide-on-leave>
      <v-btn
        v-if="canManageSpace"
        v-show="hoverEdit"
        :title="$t('subspacesList.settings.drawer.title')"
        small
        icon
        @click="$emit('settings')">
        <v-icon size="18">fa-cog</v-icon>
      </v-btn>
    </v-fab-transition>
  </div>
</template>
<script>
/**
 * The widget header's action group: add a subspace, see all, settings. It
 * holds no state of its own and decides nothing: every flag is the server's
 * answer, handed down by the widget, and both actions are emitted back to it.
 */
export default {
  props: {
    hover: {
      type: Boolean,
      default: false,
    },
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
  computed: {
    /**
     * Hovered by someone who may manage the widget: gates both the cog and
     * the switch of 'see all' from a label to an icon, so the two never
     * half-appear.
     *
     * @returns {boolean} whether the editing affordance shows
     */
    hoverEdit() {
      return this.hover && this.canManageSpace;
    },
  },
};
</script>
