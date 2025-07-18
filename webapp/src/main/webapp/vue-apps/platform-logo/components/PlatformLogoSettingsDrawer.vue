<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
    :right="!$vuetify.rtl"
    :loading="loading"
    eager
    @closed="reset">
    <template slot="title">
      {{ $t('platformLogo.drawer.settings.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card
        max-width="100%"
        class="ma-4"
        flat>
          <div class="text-header mb-2">
            {{ $t('layout.alignApp') }}
          </div>
          <div class="d-flex">
            <div class="col-6 pa-0">
              <v-radio-group v-model="hAlign" class="ma-0">
                <v-radio
                  value="START"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignLeft') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="CENTER"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignCenter') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="END"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignRight') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
            </div>
            <div class="col-6 pa-0">
              <v-radio-group v-model="vAlign" class="ma-0">
                <v-radio
                  value="START"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignTop') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="CENTER"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignMiddle') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="END"
                  class="ma-0 pa-0">
                  <template #label>
                    <span class="text-body">{{ $t('layout.alignBottom') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
            </div>
          </div>
        </v-card>
      </template>
      <template #footer>
        <div class="d-flex align-center">
          <v-btn
            :disabled="loading"
            :title="$t('platformLogo.drawer.settings.cancel')"
            class="btn ms-auto me-2"
            @click="close()">
            {{ $t('platformLogo.drawer.settings.cancel') }}
          </v-btn>
          <v-btn
            :loading="loading"
            :title="$t('platformLogo.drawer.settings.save')"
            color="primary"
            elevation="0"
            @click="save()">
            {{ $t('platformLogo.drawer.settings.save') }}
          </v-btn>
        </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    hAlign: 'CENTER',
    vAlign: 'CENTER',
  }),
  created() {
    this.$root.$on('platform-logo-settings', this.open);
  },
  beforeDestroy() {
    this.$root.$off('platform-logo-settings', this.open);
  },
  methods: {
    open() {
      this.reset();
      this.$refs.drawer.open();
    },
    reset() {
      this.hAlign = this.$root.hAlign || 'CENTER';
      this.vAlign = this.$root.vAlign || 'CENTER';
      this.loading = false;
    },
    close() {
      this.$refs.drawer.close();
    },
    save() {
      this.loading = true;
      const formData = new FormData();
      formData.append('pageRef', this.$root.pageRef);
      formData.append('applicationId', this.$root.portletStorageId);
      const params = new URLSearchParams(formData).toString();
      return fetch(`/layout/rest/pages/application/preferences?${params}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          preferences: [{
            name: 'hAlign',
            value: this.hAlign || 'CENTER',
          },
          {
            name: 'vAlign',
            value: this.vAlign || 'CENTER',
          }],
        }),
      })
        .then(() => {
          this.$root.hAlign = this.hAlign || 'CENTER';
          this.$root.vAlign = this.vAlign || 'CENTER';
          this.close();
        })
        .finally(() => this.loading = false);
    }
  },
};
</script>
