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
    :allow-expand="expanded"
    :loading="drawerLoading"
    class="appCenterDrawer"
    @expand-updated="expanded = $event">
    <template #title>
      {{ $t("generalSettings.addTopBarItem.addApplicationDrawerTitle") }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        v-model="valid"
        class="ma-5 overflow-hidden">
        <v-label for="applicationDescription">
          {{ $t('generalSettings.addTopBarItem.selectApplication') }}
        </v-label>
        <v-autocomplete
          ref="selectAutoComplete"
          v-model="applicationId"
          :placeholder="$t('generalSettings.addTopBarItem.applicationPlaceholder')"
          :items="applications"
          :rules="rules.application"
          item-value="id"
          item-text="title"
          append-icon=""
          class="no-box-shadow no-border pa-0 mt-2"
          width="100%"
          max-width="100%"
          hide-selected
          hide-details
          outlined
          chips
          dense>
          <template #selection="{item}">
            <v-chip>
              <v-img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                max-height="18"
                max-width="18"
                class="me-2"
                contain />
              <v-icon
                v-else-if="item.icon"
                class="me-2"
                size="18">
                {{ item.icon }}
              </v-icon>
              {{ item.title }}
            </v-chip>
          </template>
        </v-autocomplete>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex justify-end align-center">
        <v-btn
          class="btn ms-2"
          @click="close">
          {{ $t('generalSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!valid"
          class="btn btn-primary ms-2"
          @click="save">
          {{ $t('generalSettings.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    extension: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    valid: false,
    applications: [],
    applicationId: null,
  }),
  computed: {
    rules() {
      return {
        application: [
          v => !!v || ' ',
        ],
      };
    },
    application() {
      return this.applications.find(app => app.id === this.applicationId);
    },
  },
  methods: {
    async open() {
      this.applicationId = 0;
      this.$refs.drawer.open();
      if (!this.applications?.length) {
        this.applications = await this.extension?.getApplications();
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    save() {
      this.$emit('add', {
        id: `${this.extension.id}-${this.application.id}`,
        name: this.application.title,
        description: this.application.description,
        icon: this.application.icon,
        imageUrl: this.application.imageUrl,
        type: 'EXTENSION',
        enabled: true,
        mobile: true,
        properties: {
          applicationId: this.application.id,
          jsModule: this.extension.getApplicationJsModule(this.application),
        },
      });
      this.close();
    },
  },
};
</script>
