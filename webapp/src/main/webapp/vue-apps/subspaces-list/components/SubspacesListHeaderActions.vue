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
  <div
    class="d-flex align-center justify-center"
    @focusin="onFocusIn"
    @focusout="onFocusOut">
    <!-- The 'add a subspace' option, before 'see all', for a viewer the
         server says may create one. Offered on plain hover or keyboard focus,
         not on hoverEdit: creating needs parent membership, not management.
         Mounted lazily, the first time the header is revealed, and kept
         mounted after (addMounted latches): the shared button makes REST calls
         in its own init (spaces, space templates, subspace template ids), which
         must not run on every page load. It sits before 'see all', so once
         revealed, Shift+Tab from 'see all' reaches it; when it would be the
         header's only control (no 'see all', no cog), nothing else can take the
         focus first, so it is mounted at once. Listed state only: with
         no subspace yet the widget body already carries the one create button. -->
    <v-fab-transition hide-on-leave>
      <space-creation-button
        v-if="canCreateSubspace && hasSubspaces && (addMounted || aloneInHeader)"
        :class="{ 'd-sr-only-focusable': !revealed }"
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
      :aria-label="$t('Widget.label.seeAll')"
      color="primary"
      small
      link
      @click="$emit('see-all')">
      <v-icon v-if="hoverEdit" size="18">fa-external-link-alt</v-icon>
      <span v-else class="text-font-size text-none">{{ $t('Widget.label.seeAll') }}</span>
    </v-btn>
    <v-btn
      v-if="canManageSpace"
      :class="{ 'd-sr-only-focusable': !hoverEdit }"
      :title="$t('subspacesList.settings.drawer.title')"
      :aria-label="$t('subspacesList.settings.drawer.title')"
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
  data: () => ({
    focused: false,
    addMounted: false,
  }),
  computed: {
    /**
     * Hovered, or holding the keyboard focus.
     *
     * @returns {boolean} whether the header actions show
     */
    revealed() {
      return this.hover || this.focused;
    },
    /**
     * Hovered, or focused, by someone who may manage the widget: gates both
     * the cog and the switch of 'see all' from a label to an icon, so the two
     * never half-appear.
     *
     * @returns {boolean} whether the editing affordance shows
     */
    hoverEdit() {
      return this.revealed && this.canManageSpace;
    },
    // the add button would be the header's only control: the keyboard reaches it only if it is there
    aloneInHeader() {
      return !this.hasMore && !this.canManageSpace;
    },
  },
  watch: {
    revealed: {
      immediate: true,
      handler() {
        if (this.revealed) {
          this.addMounted = true;
        }
      },
    },
  },
  methods: {
    onFocusIn(event) {
      this.focused = event.target.matches(':focus-visible');
    },
    onFocusOut(event) {
      this.focused = !!(event.relatedTarget && event.currentTarget.contains(event.relatedTarget));
    },
  },
};
</script>
