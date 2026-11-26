<!--
 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
    id="objectReactionsDrawer"
    ref="drawer"
    v-model="drawer"
    class="objectReactionsDrawer"
    right
    @closed="objectId = null">
    <template slot="title">
      <span class="text-title">{{ $t('UIActivity.label.reactions') }}</span>
    </template>
    <template slot="content">
      <div v-if="objectId" class="pa-4">
        <activity-reactions-list
          :activity-id="`${objectId}`"
          :object-type="objectType" />
      </div>
    </template>
  </exo-drawer>
</template>
<script>
/**
 * Lists the reactors of an object of any registered type, with the same list
 * (filter chips, reactors, load more) as the activities ones. A single instance
 * per page, opened by the 'object-reactions-drawer-open' event the
 * object-reactions component emits.
 */
export default {
  data: () => ({
    drawer: false,
    objectType: null,
    objectId: null,
  }),
  created() {
    this.$root.$on('object-reactions-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('object-reactions-drawer-open', this.open);
  },
  methods: {
    open({objectType, objectId}) {
      this.objectType = objectType;
      this.objectId = objectId;
      this.$nextTick(() => this.$refs.drawer.open());
    },
  },
};
</script>
