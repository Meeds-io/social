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
    id="nodeIconPickerDrawer"
    :right="!$vuetify.rtl"
    go-back-button
    allow-expand
    eager
    @closed="$emit('closed')">
    <template slot="title">
      {{ $t('nodeIconPickerDrawer.title') }}
    </template>
    <template slot="content">
      <v-card-text class="d-flex pb-2">
        <v-text-field
          v-model="keyword"
          :placeholder="$t('nodeIconPickerDrawer.searchPlaceholder')"
          prepend-inner-icon="fa-filter">
          <template #append>
            <v-btn
              icon
              size="22"
              @click="closeFilter()">
              <v-icon
                size="18"
                color="primary">
                fa-times
              </v-icon>
            </v-btn>
          </template>
        </v-text-field>
      </v-card-text>
      <v-card-text class="d-flex pb-2">
        <v-container id="iconPicker">
          <v-row class="ma-0">
            <v-col 
              v-for="icon in filtredIcons"
              :key="icon.value"
              cols="3"
              class="pa-0 pa-1">
              <v-card
                :class="selectedIcon === icon.value && 'primary-border-color' || 'border-color transparent'"
                class="rounded-lg position-relative"
                flat
                @click="selectedIcon = icon.value">
                <div class="d-flex flex-grow-1">
                  <v-icon
                    v-if="selectedIcon === icon.value"
                    color="primary"
                    size="18"
                    class="pb-8 py-1 pl-1 position-absolute">
                    fa-check-square
                  </v-icon>
                  <v-icon
                    size="32"
                    class="flex-grow-1 align-center pt-6 pb-2">
                    {{ icon.value }}
                  </v-icon>
                </div>
                <v-tooltip bottom>
                  <template #activator="{ on, attrs }">
                    <p                 
                      v-on="on"
                      v-bind="attrs"
                      class="align-center text-truncate text-caption text--primary font-weight-medium pb-2 mx-1">
                      {{ icon.name }}
                    </p>
                  </template>
                  <span>{{ icon.name }}</span>
                </v-tooltip>
              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
      <v-card-text v-if="hasMore" class="align-center">
        <v-btn
          v-if="!search"
          elevation="0"
          @click="showMoreIcons">
          {{ $t('nodeIconPickerDrawer.showMore') }}
        </v-btn>
      </v-card-text>
    </template>
    <template slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          class="btn ms-2"
          @click="close">
          {{ $t('nodeIconPickerDrawer.cancel') }}
        </v-btn>
        <v-btn
          :loading="loading"
          :disabled="disabled"
          class="btn btn-primary ms-2"
          @click="save">
          {{ $t('nodeIconPickerDrawer.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    limit: 16,
    showMore: false,
    selectedIcon: null,
    keyword: null,
  }),
  computed: {
    allIcons() {
      const fontLibrary = this.$root.$fontLibrary.map(icon => ({
        name: icon.replace('fa-', ''),
        value: icon,
      }));
      fontLibrary.sort((icon1, icon2) => icon1.name.localeCompare(icon2.name));
      return fontLibrary;
    },
    filtredIconsNoLimit() {
      return this.allIcons
        .filter(item => !this.keyword?.length || item.name.includes(this.keyword.toLowerCase()));
    },
    filtredIcons() {
      return this.filtredIconsNoLimit.slice(0, this.limit);
    },
    hasMore() {
      return this.filtredIconsNoLimit.length > this.filtredIcons.length;
    },
    disabled() {
      return this.selectedIcon === this.value;
    },
  },
  methods: {
    open() {
      this.keyword = null;
      this.selectedIcon = this.value;
      this.limit = 16;
      this.$nextTick().then(() => this.$refs.drawer.open());
    },
    close() {
      this.$refs.drawer.close();
    },
    showMoreIcons() {
      this.limit += 16;
    },
    save() {
      this.$emit('input', this.selectedIcon);
      this.$nextTick().then(() => this.close());
    },
    filterIcons(event) {
      const search = event.target.value.trim();
      if (search.length > 0) {
        this.search = true;
      } else {
        this.search = false;
      }
    },
    closeFilter() {
      this.keyword = '';
      this.search = false;
    }
  },
};
</script>