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
      :title="$t('SpaceSettings.publicSite')"
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
    <space-setting-public-site-drawer ref="publicSiteDrawer" />
  </div>
</template>
<script>
/**
 * The label of each public site visibility, as the public site drawer offers it.
 */
const VISIBILITY_LABELS = {
  manager: 'SpaceSettings.publicSite.drawer.accessChoiceAdmins',
  member: 'SpaceSettings.publicSite.drawer.accessChoiceMembers',
  internal: 'SpaceSettings.publicSite.drawer.accessChoiceUsersNoGuests',
  authenticated: 'SpaceSettings.publicSite.drawer.accessChoiceUsersWithGuests',
  everyone: 'SpaceSettings.publicSite.drawer.accessChoiceAnonymous',
};

export default {
  computed: {
    /**
     * @returns {String} whether the space has a public site, and who can see it
     */
    summary() {
      if (!this.$root.space.publicSiteId) {
        return this.$t('SpaceSettings.publicSite.summary.notPublished');
      }
      const visibilityLabel = VISIBILITY_LABELS[this.$root.space.publicSiteVisibility || 'manager'];
      return visibilityLabel
        ? this.$t('SpaceSettings.publicSite.summary.published', {0: this.$t(visibilityLabel)})
        : this.$t('SpaceSettings.publicSite.summary.publishedNoVisibility');
    },
  },
  methods: {
    /**
     * Opens the drawer editing the public site of the space.
     *
     * @returns {void}
     */
    openDrawer() {
      this.$refs.publicSiteDrawer.open();
    },
  },
};
</script>
