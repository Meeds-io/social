<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <div v-if="$root.isAllSections">
    <space-setting-row
      :title="$t('SpaceSettings.accessAndVisibility')"
      :summary="summary">
      <template #action>
        <v-btn
          small
          icon
          @click="openDrawer">
          <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </template>
    </space-setting-row>
    <space-setting-access-drawer ref="accessSettingDrawer" />
  </div>
</template>
<script>
export default {
  computed: {
    /**
     * @returns {String} the visibility and the access rule of the space, with
     *          the labels its drawer offers them under
     */
    summary() {
      const visibilityKey = `SpaceSettings.label.${this.$root.space.visibility?.toLowerCase?.()}`;
      const subscriptionKey = `SpaceSettings.label.${this.$root.space.subscription?.toLowerCase?.()}`;
      // A value the drawer does not offer (the legacy « public » visibility) has no label to show
      if (!this.$te(visibilityKey) || !this.$te(subscriptionKey)) {
        return null;
      }
      return this.$t('SpaceSettings.accessAndVisibility.summary', {
        0: this.$t(visibilityKey),
        1: this.$t(subscriptionKey),
      });
    },
  },
  methods: {
    /**
     * Opens the drawer editing the visibility and the access rule.
     *
     * @returns {void}
     */
    openDrawer() {
      this.$refs.accessSettingDrawer.open();
    },
  },
};
</script>
