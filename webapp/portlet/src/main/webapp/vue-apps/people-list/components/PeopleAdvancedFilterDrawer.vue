/*
 * This file is part of the Meeds project (https://meeds.io/).

 * Copyright (C) 2023 Meeds Association contact@meeds.io

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
<template>
  <exo-drawer
    ref="peopleAdvancedFilterDrawer"
    id="peopleAdvancedFilterDrawer"
    right
    @closed="close">
    <template slot="title">
      <span class="popupTitle">{{ $t('pepole.advanced.filter.title') }}</span>
    </template>
    <template slot="content">
      <v-form class="pa-2 ms-2 mt-4">
        <div class="d-flex flex-column flex-grow-1">
          <div
            v-for="item in settings"
            :key="item.id"
            class="d-flex flex-row">
            <people-advanced-filter-input-item
              :settings-item="item"
              @input-value-changed="setValueToSearch" />
          </div>
        </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="flex d-flex">
        <v-btn
          color="primary"
          elevation="0"
          text
          link
          @click="reset">
          <v-icon x-small class="pr-1">fas fa-redo</v-icon>
          {{ $t('pepole.advanced.filter.button.reset') }}
        </v-btn>
        <v-spacer />
        <div class="d-btn">
          <v-btn
            class="btn me-2"
            @click="cancel">
            <template>
              {{ $t('pepole.advanced.filter.button.cancel') }}
            </template>
          </v-btn>
          <v-btn
            :disabled="disabled"
            class="btn btn-primary"
            @click="confirm">
            <template>
              {{ $t('pepole.advanced.filter.button.confirm') }}
            </template>
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  name: 'PeopleAdvancedFilterDrawer',
  data() {
    return {
      settings: [],
    };
  },
  created() {
    this.$root.$on('open-people-advanced-filter-drawer', this.openDrawer);
    this.getSettings();
  },
  methods: {
    openDrawer() {
      this.$refs.peopleAdvancedFilterDrawer.open();
    },
    close() {
      this.$refs.peopleAdvancedFilterDrawer.close();
    },
    cancel() {
      this.settings.forEach((element) => {
        if (element && element.valueToSearch) {
          element.valueToSearch = '';
        }
      });
      this.$root.$emit('cancel-advanced-filter');
      this.$refs.peopleAdvancedFilterDrawer.close();
    },
    reset() {
      this.settings.forEach((element) => {
        if (element && element.valueToSearch) {
          element.valueToSearch = '';
        }
      });
      this.$root.$emit('reset-advanced-filter');
      this.$root.$emit('reset-advanced-filter-count');
    },
    getSettings() {
      return this.$profileSettingsService.getSettings()
        .then(settings => {
          this.settings = settings?.settings.filter((e) => e.active && e.visible && e.parentId === null).map(obj => ({
            ...obj,
            valueToSearch: ''
          })) || [];
        });
    },
    confirm() {
      const keyValue = {};
      this.settings.forEach((e)=>{
        if (e && e.valueToSearch){
          keyValue[e.propertyName] = e.valueToSearch;
        }
      });
      this.$root.$emit('advanced-filter', keyValue);
      const advancedFilterCount = Object.keys(keyValue).length || 0 ;
      this.$root.$emit('advanced-filter-count', advancedFilterCount);
      this.close();
    },
    setValueToSearch(item){
      this.settings.forEach((e)=>{
        if (e.id === item.itemId){
          e.valueToSearch = item.valueTosearch;
        }
      });
    }
  }
};

</script>
