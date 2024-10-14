<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
    ref="versionHistoryDrawer"
    class="versionHistoryDrawer"
    @closed="closed"
    show-overlay
    right>
    <template slot="title">
      {{ $t('versionHistory.label.title') }}
    </template>
    <template slot="content">
      <div
        v-if="!isLoading && versions.length === 0"
        class="text-center mt-5">
        <p class="grey--text darken-1">
          {{ $t('versionHistory.label.empty') }}
        </p>
      </div>
      <v-list
        class="ma-3">
        <v-list-item-group
          active-class="bg-active">
          <v-slide-y-transition group>
            <v-list-item
              v-for="version in versions"
              :key="version.id"
              :class="[version.current? 'light-grey-background-color' : '']"
              @click="openVersion(version)"
              class="history-line pa-2 mb-2 border-color border-radius d-block">
              <version-card
                :version="version"
                :can-manage="canManage"
                :disable-restore-version="disableRestoreVersion"
                :enable-edit-description="enableEditDescription"
                @version-update-description="updateVersionDescription"
                @restore-version="restoreVersion" />
            </v-list-item>
          </v-slide-y-transition>
        </v-list-item-group>
      </v-list>
    </template>
    <template v-if="showLoadMore" slot="footer">
      <div
        class="d-flex mx-4">
        <v-btn
          :loading="isLoading"
          @click="loadMore"
          class="primary--text mx-auto"
          text>
          {{ $t('versionHistory.button.loadMore') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  props: {
    versions: {
      type: Array,
      default: () => {
        return null;
      }
    },
    canManage: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    showLoadMore: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    isLoading: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    enableEditDescription: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    disableRestoreVersion: {
      type: Boolean,
      default: () => {
        return false;
      }
    }
  },
  watch: {
    isLoading() {
      if (this.isLoading) {
        this.$refs.versionHistoryDrawer.startLoading();
      } else {
        this.$refs.versionHistoryDrawer.endLoading();
      }
    },
  },
  methods: {
    open() {
      this.$refs.versionHistoryDrawer.open();
    },
    closed() {
      this.$emit('drawer-closed');
    },
    loadMore() {
      this.$emit('load-more');
    },
    openVersion(version) {
      this.$emit('open-version', version);
    },
    updateVersionDescription(version, newDescription) {
      this.$emit('version-update-description', version, newDescription);
    },
    restoreVersion(version) {
      this.$emit('restore-version', version);
    }
  }
};
</script>