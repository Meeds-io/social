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
      :title="$t('SpaceSettings.sovereignty')"
      :summary="summary">
      <template #action>
        <v-switch
          id="SpaceSettingRestrictContent"
          v-model="$root.space.sovereign"
          :loading="saving"
          class="ma-0"
          @click="switchSpaceSovereignty" />
      </template>
    </space-setting-row>
  </div>
</template>
<script>
export default {
  data: () => ({
    saving: false,
    savedSovereign: null,
  }),
  computed: {
    /**
     * @returns {String} whether the space is stored as sovereign, and what it means for its data
     */
    summary() {
      const sovereign = this.savedSovereign === null ? this.$root.space.sovereign : this.savedSovereign;
      return sovereign
        ? this.$t('SpaceSettings.sovereignty.summary.on')
        : this.$t('SpaceSettings.sovereignty.summary.off');
    },
  },
  methods: {
    /**
     * Saves the sovereignty the switch now shows; a failed save puts the
     * switch back and says so.
     *
     * @returns {Promise} resolved once the space is saved
     */
    async switchSpaceSovereignty() {
      const sovereign = this.$root.space.sovereign;
      this.saving = true;
      try {
        await this.$spaceService.updateSpace({
          id: this.$root.spaceId,
          sovereign,
        });
        this.savedSovereign = sovereign;
      } catch (e) {
        this.$root.space.sovereign = !sovereign;
        this.savedSovereign = !sovereign;
        this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingSpace'), 'error');
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>
