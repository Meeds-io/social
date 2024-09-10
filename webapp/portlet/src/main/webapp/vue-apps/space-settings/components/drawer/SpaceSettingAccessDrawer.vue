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
    allow-expand
    right
    class="spaceFormDrawer">
    <template slot="title">
      {{ $t('SpaceSettings.accessAndVisibility.drawer') }}
    </template>
    <template v-if="drawer" slot="content">
      <form
        ref="form"
        :disabled="savingSpace"
        class="my-4 d-flex flex-column"
        @submit.prevent.stop="saveSpace">
        <div class="d-flex flex-column">
          <div class="text-header mx-4">
            {{ $t('SpaceSettings.label.access') }}
          </div>
          <v-radio-group
            v-model="subscription"
            class="mt-2 ms-3"
            mandatory
            inset>
            <v-radio
              value="open"
              class="mt-0 mb-8">
              <template #label>
                <div class="d-flex flex-column">
                  <div class="text-body">
                    {{ $t('SpaceSettings.label.open') }}
                  </div>
                  <div class="text-subtitle position-absolute line-height-normal mt-5">
                    {{ $t('SpaceSettings.description.open') }}
                  </div>
                </div>
              </template>
            </v-radio>
            <v-radio
              value="validation"
              class="mt-0 mb-8">
              <template #label>
                <div class="d-flex flex-column">
                  <div class="text-body">
                    {{ $t('SpaceSettings.label.validation') }}
                  </div>
                  <div class="text-subtitle position-absolute line-height-normal mt-5">
                    {{ $t('SpaceSettings.description.validation') }}
                  </div>
                </div>
              </template>
            </v-radio>
            <v-radio
              value="closed"
              class="mt-0 mb-6">
              <template #label>
                <div class="d-flex flex-column">
                  <div class="text-body">
                    {{ $t('SpaceSettings.label.closed') }}
                  </div>
                  <div class="text-subtitle position-absolute line-height-normal mt-5">
                    {{ $t('SpaceSettings.description.closed') }}
                  </div>
                </div>
              </template>
            </v-radio>
          </v-radio-group>
        </div>
        <div class="d-flex flex-column mt-4">
          <div class="text-header mx-4">
            {{ $t('SpaceSettings.label.visibility') }}
          </div>
          <v-radio-group
            v-model="visibility"
            class="mt-2 ms-3"
            mandatory
            inset>
            <v-radio
              value="private"
              class="mb-6">
              <template #label>
                <div class="d-flex flex-column">
                  <div class="text-body">
                    {{ $t('SpaceSettings.label.private') }}
                  </div>
                  <div class="text-subtitle position-absolute line-height-normal mt-5">
                    {{ $t(`SpaceSettings.description.private`) }}
                  </div>
                </div>
              </template>
            </v-radio>
            <v-radio
              value="hidden"
              class="mt-0 mb-8">
              <template #label>
                <div class="d-flex flex-column">
                  <div class="text-body">
                    {{ $t('SpaceSettings.label.hidden') }}
                  </div>
                  <div class="text-subtitle position-absolute line-height-normal mt-5">
                    {{ $t(`SpaceSettings.description.hidden`) }}
                  </div>
                </div>
              </template>
            </v-radio>
          </v-radio-group>
        </div>
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