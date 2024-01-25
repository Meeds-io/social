/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

<template>
  <v-app>
    <v-main>
      <v-card
        color="white"
        class="px-4 py-6 mb-12 mb-sm-0 card-border-radius"
        flat>
        <profile-settings-header :filter="filter" />
        <profile-settings-table :settings="filteredSettings" />
        <profile-setting-form-drawer
          :settings="settings"
          :languages="languages"
          :un-hiddenable-properties="unHiddenableProperties" />
      </v-card>
    </v-main>
  </v-app>
</template>

<script>
export default {
  props: {
    languages: {
      type: Array,
      default: null,
    },
  },

  data: () => ({
    settings: [],
    unHiddenableProperties: [],
    filter: 'Active'
  }),
  created() {
    this.$root.$on('update-setting', this.editSetting);
    this.$root.$on('create-setting', this.createSetting);
    this.$root.$on('update-labels', this.updateLabels);
    this.$root.$on('create-labels', this.createLabels);
    this.$root.$on('delete-labels', this.deleteLabels);
    this.$root.$on('move-up-setting', this.moveUpSetting);
    this.$root.$on('move-down-setting', this.moveDownSetting);
    this.$root.$on('settings-set-filter', this.setFilter);
    this.$root.$on('cancel-edit-add', this.displayNoChangeWarning);
    this.languages = this.languages.sort((a, b) => a.value.localeCompare(b.value));
    this.getSettings();
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'sm' || this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'md';
    },
    filteredSettings(){
      if (this.filter === 'Active') {
        return this.settings.filter(function (setting) {
          return setting.active;
        });
      } else if (this.filter === 'Inactive') {
        return this.settings.filter(function (setting) {
          return !setting.active;
        });
      } return this.settings;
    }
  },
  methods: {
    displayMessage(alert) {
      this.$root.$emit('alert-message', alert.message, alert.type);
    },
    getSettings() {
      return this.$profileSettingsService.getSettings()
        .then(settings => {
          this.settings = settings?.settings || [];
          this.unHiddenableProperties = settings?.unHiddenableProperties;
        });
    },
    editSetting(setting,refresh) {
      this.$profileSettingsService.updateSetting(setting).then(() => {
        this.$root.$emit('close-settings-form-drawer');      
        if (refresh){
          this.getSettings();
        }
        this.displayMessage({type: 'success', message: this.$t('profileSettings.update.success.message')});
      }).catch(e => {
        console.error(e);
        this.displayMessage({type: 'error', message: this.$t(e.message)});
      });
    },
    createSetting(setting) {
      this.$profileSettingsService.addSetting(setting).then(storedSetting => {
        if (setting.labels && setting.labels.length>0){
          setting.labels.forEach(element => {
            element.objectId=storedSetting.id;
          });
          this.$profileLabelService.addLabels(setting.labels).then(() => {
            this.$root.$emit('close-settings-form-drawer');  
            this.getSettings();
            this.displayMessage({type: 'success', message: this.$t('profileSettings.create.success.message')});
          });
        } else {
          this.$root.$emit('close-settings-form-drawer');  
          this.getSettings();
          this.displayMessage({type: 'success', message: this.$t('profileSettings.create.success.message')});
        }
      }).catch(e => {
        console.error(e);
        this.displayMessage({type: 'error', message: this.$t(e.message)});
      });
    },
    updateLabels(labels) {
      this.$profileLabelService.updateLabels(labels);
    },
    createLabels(labels) {
      this.$profileLabelService.addLabels(labels);
    },
    deleteLabels(labels) {
      this.$profileLabelService.deleteLabels(labels);
    },
    setFilter(filter) {
      this.filter = filter;
    },
    moveUpSetting(setting) {
      this.moveSetting(setting, 'up');
    },
    moveDownSetting(setting) {
      this.moveSetting(setting, 'down');
    },
    displayNoChangeWarning() {
      this.displayMessage({type: 'warning', message: this.$t('profileSettings.nochange.warning.message')});
    },
    moveSetting(setting, direction) {
      this.settings.sort((s1, s2) => ((s1.order > s2.order) ? 1 : (s1.order < s2.order) ? -1 : 0));
      const index = this.settings.findIndex(object => {
        return object.id === setting.id;
      });
      if (index > 0 && direction === 'up'){
        const order = this.settings[index].order;
        this.settings[index].order=this.settings[index-1].order;
        this.settings[index-1].order = order;
        this.editSetting(this.settings[index],false);
        this.editSetting(this.settings[index-1],true);
      }
      if (index < this.settings.length && direction === 'down'){
        const order = this.settings[index].order;
        this.settings[index].order=this.settings[index+1].order;
        this.settings[index+1].order = order;
        this.editSetting(this.settings[index],false);
        this.editSetting(this.settings[index+1],true);
      }
    },
  }
};
</script>
