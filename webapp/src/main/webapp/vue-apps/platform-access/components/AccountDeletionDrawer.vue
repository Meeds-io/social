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
    ref="drawer"
    id="accountDeletionDrawer"
    v-model="drawer"
    right
    disable-pull-to-refresh>
    <template #title>
      {{ $t('generalSettings.accountDeletion.drawerTitle') }}
    </template>
    <template #content>
      <v-card class="pa-4" flat>
        <div>
          {{ $t('generalSettings.accountDeletion.deactivationInformation') }}
        </div>
        <v-list-item
          class="px-0 my-0"
          dense
          @click="deactivationEnabled = !deactivationEnabled">
          <v-list-item-content>
            <v-list-item-title class="font-weight-bold">
              {{ $t('generalSettings.accountDeletion.deactivationLabel') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action class="my-0">
            <v-switch
              v-model="deactivationEnabled"
              @click.stop="0" />
          </v-list-item-action>
        </v-list-item>
        <div class="mt-2">
          <div>
            {{ $t('generalSettings.accountDeletion.deletionInformation') }}
          </div>
          <div>
            {{ $t('generalSettings.accountDeletion.deletionInformation.delay') }}
          </div>
          <div>
            {{ $t('generalSettings.accountDeletion.deletionInformation.irreversible') }}
          </div>
          <div>
            {{ $t('generalSettings.accountDeletion.deletionInformation.anonymized') }}
          </div>
        </div>
        <v-list-item
          class="px-0 my-0"
          dense
          v-on="deactivationEnabled && {
            click: () => deletionEnabled = !deletionEnabled,
          }">
          <v-list-item-content>
            <v-list-item-title class="font-weight-bold">
              {{ $t('generalSettings.accountDeletion.deletionLabel') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action class="my-0">
            <v-tooltip
              :disabled="deactivationEnabled"
              bottom>
              <template #activator="{ on, attrs }">
                <div
                  v-bind="attrs"
                  v-on="on">
                  <v-switch
                    v-model="deletionEnabled"
                    :disabled="!deactivationEnabled"
                    @click.stop="0" />
                </div>
              </template>
              <span>{{ $t('generalSettings.accountDeletion.deletionDisabledTooltip') }}</span>
            </v-tooltip>
          </v-list-item-action>
        </v-list-item>
        <template v-if="deletionEnabled">
          <v-list-item
            class="px-0 my-0"
            dense
            @click="anonymizationEnabled = !anonymizationEnabled">
            <v-list-item-content>
              <v-list-item-title>
                {{ $t('generalSettings.accountDeletion.anonymizationLabel') }}
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action class="my-0">
              <v-switch
                v-model="anonymizationEnabled"
                @click.stop="0" />
            </v-list-item-action>
          </v-list-item>
          <translation-text-field
            v-if="anonymizationEnabled"
            ref="deletedUserLabel"
            id="deletedUserLabel"
            v-model="deletedUserLabels"
            :placeholder="$t('generalSettings.accountDeletion.deletedUserLabelPlaceholder')"
            :maxlength="255"
            drawer-title="generalSettings.accountDeletion.labelDrawerTitle"
            class="mt-2" />
        </template>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          :aria-label="$t('generalSettings.cancel')"
          class="btn cancel-button me-4"
          elevation="0"
          @click="close">
          <span class="text-none">
            {{ $t('generalSettings.cancel') }}
          </span>
        </v-btn>
        <v-btn
          :aria-label="$t('generalSettings.apply')"
          :disabled="!changed"
          color="primary"
          class="btn btn-primary"
          elevation="0"
          @click="apply">
          <span class="text-none">
            {{ $t('generalSettings.apply') }}
          </span>
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    deactivationEnabled: false,
    deletionEnabled: false,
    anonymizationEnabled: false,
    deletedUserLabels: {},
  }),
  computed: {
    draft() {
      return {
        accountDeactivationEnabled: this.deactivationEnabled,
        accountDeletionEnabled: this.deletionEnabled,
        accountDeletionAnonymizationEnabled: this.anonymizationEnabled,
        deletedUserLabels: this.normalizeLabels(this.deletedUserLabels),
      };
    },
    changed() {
      const oldSettings = Object.assign({}, this.value, {
        deletedUserLabels: this.normalizeLabels(this.value?.deletedUserLabels),
      });
      return JSON.stringify(this.draft) !== JSON.stringify(oldSettings);
    },
  },
  watch: {
    deactivationEnabled() {
      if (!this.deactivationEnabled) {
        this.deletionEnabled = false;
      }
    },
    deletionEnabled() {
      if (!this.deletionEnabled) {
        this.anonymizationEnabled = false;
      }
    },
  },
  methods: {
    open() {
      this.reset();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    reset() {
      this.deactivationEnabled = this.value?.accountDeactivationEnabled || false;
      this.deletionEnabled = this.value?.accountDeletionEnabled || false;
      this.anonymizationEnabled = this.value?.accountDeletionAnonymizationEnabled || false;
      this.deletedUserLabels = Object.assign({}, this.value?.deletedUserLabels || {});
    },
    apply() {
      this.$emit('input', this.draft);
      this.close();
    },
    normalizeLabels(labels) {
      const normalizedLabels = {};
      Object.keys(labels || {})
        .filter(language => labels[language])
        .sort()
        .forEach(language => normalizedLabels[language] = labels[language]);
      return normalizedLabels;
    },
  }
};
</script>
