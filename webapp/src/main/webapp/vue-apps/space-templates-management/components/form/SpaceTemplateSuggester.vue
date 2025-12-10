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
  <v-flex :id="id">
    <v-autocomplete
      ref="selectAutoComplete"
      v-model="value"
      :label="labels.label"
      :placeholder="labels.placeholder"
      :items="templateItems"
      :loading="!!loadingSuggestions"
      :multiple="multiple"
      :hide-no-data="hideNoData"
      append-icon=""
      menu-props="closeOnClick, closeOnContentClick, maxHeight = 100"
      class="identitySuggester identitySuggesterInputStyle"
      content-class="identitySuggesterContent"
      width="100%"
      max-width="100%"
      item-text="name"
      item-value="id"
      cache-items
      return-object
      persistent-hint
      hide-selected
      chips
      dense
      flat
      required
      @update:search-input="searchTerm = $event">
      <template #no-data>
        <v-list-item class="pa-0">
          <v-list-item-title
            v-if="displaySearchPlaceHolder"
            class="px-2">
            {{ labels.searchPlaceholder }}
          </v-list-item-title>
        </v-list-item>
      </template>
      <template #selection="{ item }">
        <space-template-chip :template="item" @remove="remove(item)" />
      </template>
      <template #item="{ item }">
        <v-list-item-avatar class="text-truncate">
          <v-icon>{{ item.icon }}</v-icon>
        </v-list-item-avatar>
        <v-list-item-title class="text-truncate">
          {{ item.name }}
        </v-list-item-title>
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: null
    },
    multiple: {
      type: Boolean,
      default: false
    },
    labels: {
      type: Object,
      default: () => ({
        label: '',
        placeholder: '',
        searchPlaceholder: '',
        noDataLabel: '',
      }),
    },
  },
  data() {
    return {
      id: `SpaceTemplateSuggester${parseInt(Math.random() * 10000)}`,
      templates: [],
      searchTerm: null,
      loadingSuggestions: false,
      searchStarted: false,
    };
  },
  computed: {
    displaySearchPlaceHolder() {
      return this.labels.searchPlaceholder && !this.searchStarted;
    },
    hideNoData() {
      return !this.searchStarted && this.templates.length === 0;
    },
    templateItems() {
      return  this.searchTerm
        ? this.templates.filter(t =>
          t.name?.toLowerCase().includes(this.searchTerm.toLowerCase())
        )
        : this.templates;
    },
  },
  watch: {
    value() {
      this.$emit('input', this.value);
      this.init();
    },
  },
  created() {
    if (this.multiple) {
      this.templates = this.value?.length && this.value || [];
    } else {
      this.templates = this.value && [this.value] || [];
    }
    this.retrieveTemplates();
  },
  mounted() {
    $(`#${this.id} input`).on('blur', () => {
      this.$refs.selectAutoComplete.isFocused = false;
    });
  },
  methods: {
    init() {
      if (this.value && this.value.length) {
        this.value.forEach(item => {
          if (item.id) {
            this.templates.push(item);
          }
        });
      } else if (this.value && this.value.id){
        this.templates = [this.value];
      }
    },
    remove(item) {
      if (Array.isArray(this.value)) {
        const index = this.value.findIndex(val => val.id === item.id);
        if (index >= 0) {this.value.splice(index, 1);}
      } else {
        this.value = null;
      }
      this.$emit('removeTemplate', item);
    },
    retrieveTemplates() {
      this.loadingSuggestions = true;
      this.$spaceTemplateService
        .getSpaceTemplates()
        .then(data => {
          this.templates = data;
        })
        .finally(() => (this.loadingSuggestions = false));
    },
  },
};
</script>
