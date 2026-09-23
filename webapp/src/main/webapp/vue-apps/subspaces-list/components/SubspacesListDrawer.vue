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
  <exo-drawer
    ref="drawer"
    :loading="loading"
    :right="!$vuetify.rtl"
    class="subspacesListDrawer">
    <!-- The feature's own name, as the design titles this panel — never the
         widget's header. The header is no signal of an administrator having
         named anything: the settings drawer seeds it with the default label on
         open and stores it on save, so an instance nobody renamed carries one
         all the same. -->
    <template #title>
      <div class="d-flex justify-space-between align-center">
        <span class="text-color">{{ $t('subspacesList.drawer.title') }}</span>
        <!-- The header suggests adding a subspace to whoever the
             envelope says may create one — the same rule, read from this
             drawer's own call, so the drawer never claims a right the widget
             was not given. Same shared button as the spaces toolbar. -->
        <space-creation-button
          v-if="creationAllowed"
          :parent-space-id="$root.spaceId"
          :elevation="0"
          color="primary"
          display-label
          require-form-drawer />
      </div>
    </template>
    <!-- no v-if on this template: the flag <exo-drawer> emits on close flips in
         the same tick as the closing, and Vue drops the slot with it, so the
         panel would slide out empty. Nothing renders before the first open
         anyway — <exo-drawer> gates its whole body on its own 'initialized' -->
    <template #content>
      <div class="d-flex flex-column pa-4">
        <subspaces-list-item
          v-for="subspace in subspaces"
          :key="subspace.id"
          :space="subspace"
          :avatar-listing-limit="subspaces.length" />
        <!-- the action is only offered when the widget saw an extra row, so
             this is the one case the spec calls impossible: every sub-space
             gone, or gone from view, between that call and this one -->
        <span v-if="loaded && !loading && !subspaces.length" class="text-center">
          {{ $t('subspacesList.label.noSubspaces') }}
        </span>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    /**
     * The widget's own server-computed flag, used to seed the header so the
     * button does not appear a call later than the title. The drawer's own
     * envelope then confirms or withdraws it: same Service rule, same viewer,
     * so seeding costs no ACL — it only removes a flicker.
     */
    canCreateSubspace: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    // the server's answer for this drawer: seeded from the widget's, then
    // re-read from each opening's envelope — who may create a subspace under
    // this parent is the Service's decision, never derived here
    creationAllowed: false,
    loading: false,
    // whether a call came back with a list. The empty state hangs on this and
    // not on the rows alone: the error path closes the drawer and clears the
    // loading flag in the same microtask, and the panel would otherwise carry
    // 'No subspace yet' — the very sentence that path exists to avoid — for
    // the length of its slide-out
    loaded: false,
    subspaces: [],
  }),
  methods: {
    /**
     * Opens the drawer on the whole list.
     *
     * The call carries no limit, so the portlet answers every sub-space the
     * viewer may see, up to its own cap — one call per opening, no pagination
     * (board US01.05). The rows are rendered in the order the server returns
     * them, which is the alphabetical order the widget already shows: the
     * ordering is the listing's (TITLE ASC), never re-sorted here.
     *
     * Whether hidden sub-spaces are part of that list is the portlet
     * preference's decision, exactly as for the widget — the call has no
     * parameter for it, so the two cannot diverge.
     *
     * The list is re-read on every opening rather than kept: the widget's own
     * rows are refreshed after a settings save, and a drawer holding the list
     * it read the first time would outlive that.
     *
     * The rows carry the number of rows received as the bound the portlet must
     * look through to serve a hidden sub-space's avatar: the portlet defaults
     * to the widget's own rows, and a row past those would otherwise show the
     * default image here while showing its real avatar in the widget. That
     * number is in the image URL, so adding or removing a sub-space changes
     * the browser cache key of every hidden row's avatar — the trade taken
     * knowingly, against a fixed bound that would make the portlet scan the
     * whole 500-row window for a list of twenty.
     *
     * The row's own index would be smaller again, and would move its cache key
     * only when the rows before it move. It is deliberately not used: avatars
     * load lazily, so the request can leave long after this listing, and a
     * single sub-space created alphabetically before the row would then push
     * it outside a window of index + 1 — a default image where the widget
     * shows the real one. The list length carries that slack for every row but
     * the last, where the two windows coincide; the extra rows scanned buy it.
     *
     * @returns {Promise<void>} resolved once the drawer shows the list
     */
    open() {
      this.subspaces = [];
      this.creationAllowed = this.canCreateSubspace;
      this.loaded = false;
      this.loading = true;
      this.$refs.drawer.open();
      return this.$subspacesListService.getSubspaces(this.$root.settings.resourceUrl)
        .then(envelope => {
          this.subspaces = envelope.subspaces || [];
          this.creationAllowed = !!envelope.canCreateSubspace;
          this.loaded = true;
        })
        .catch(() => {
          // no rows means an empty drawer, which would read as 'this parent
          // has no sub-space' — the one thing this drawer cannot be opened
          // on. Close it and say so instead.
          this.close();
          this.$root.$emit('alert-message', this.$t('subspacesList.drawer.error'), 'error');
        })
        .finally(() => this.loading = false);
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>
