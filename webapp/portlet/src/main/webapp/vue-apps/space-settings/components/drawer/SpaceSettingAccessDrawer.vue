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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="savingSpace"
    right
    class="spaceFormDrawer">
    <template slot="title">
      {{ $t('SpaceSettings.general') }}
    </template>
    <template v-if="drawer" slot="content">
      <form
        ref="form"
        :disabled="savingSpace"
        class="ma-4 d-flex flex-column"
        @submit.prevent.stop="saveSpace">
        <div class="d-flex flex-wrap pt-2">
          <div class="text-header">
            {{ $t('SpaceSettings.label.hidden') }}
          </div>
          <v-switch
            v-model="visibility"
            true-value="hidden"
            false-value="private"
            class="float-left my-0 ms-4" />
        </div>
        <div class="text-subtitle mb-2 mt-1">
          {{ $t(`SpaceSettings.description.${visibility || 'hidden'}`) }}
        </div>
        <div class="d-flex flex-wrap pt-2">
          <div class="text-header">
            {{ $t('SpaceSettings.label.registration') }}
          </div>
          <v-radio-group
            v-model="subscription"
            class="mt-2 ms-2"
            mandatory
            row
            inset>
            <v-radio
              :label="$t('SpaceSettings.label.open')"
              value="open"
              class="my-0" />
            <v-radio
              :label="$t('SpaceSettings.label.validation')"
              value="validation"
              class="my-0" />
            <v-radio
              :label="$t('SpaceSettings.label.closed')"
              value="closed"
              class="my-0" />
          </v-radio-group>
        </div>
        <div class="text-subtitle ps-1">{{ $t(`SpaceSettings.description.${subscription || 'open'}`) }}</div>
      </form>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="savingSpace"
          class="btn me-2"
          @click="close">
          <template>
            {{ $t('SpaceSettings.button.cancel') }}
          </template>
        </v-btn>
        <v-btn
          :loading="savingSpace"
          class="btn btn-primary"
          @click.prevent.stop="saveSpace">
          {{ $t('SpaceSettings.button.updateSpace') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    savingSpace: false,
    subscription: null,
    visibility: null,
  }),
  methods: {
    open() {
      this.subscription = this.$root.space?.subscription;
      this.visibility = this.$root.space?.visibility;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    async saveSpace() {
      if (this.savingSpace) {
        return;
      }
      this.error = null;
      this.savingSpace = true;
      try {
        const space = await this.$spaceService.updateSpace({
          id: this.$root.spaceId,
          subscription: this.subscription,
          visibility: this.visibility,
        });
        this.$root.$emit('space-settings-updated', space);
        this.$root.$emit('alert-message', this.$t('SpaceSettings.successfullySaved'), 'success');
        this.$refs.drawer.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingSpace'), 'error');
      } finally {
        this.savingSpace = false;
      }
    },
  },
};
</script>