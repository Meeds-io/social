<template>
  <v-flex :id="id">
    <v-autocomplete
      ref="selectAutoComplete"
      v-model="value"
      :label="labels.label"
      :placeholder="labels.placeholder"
      :disabled="disabled"
      :loading="loadingSuggestions > 0"
      :rules="rules"
      :multiple="multiple"
      :required="required"
      :items="items"
      :search-input.sync="searchTerm"
      :height="height"
      append-icon=""
      menu-props="closeOnClick, closeOnContentClick, maxHeight = 100"
      class="identitySuggester"
      content-class="identitySuggesterContent"
      width="100%"
      max-width="100%"
      item-text="profile.fullName"
      item-value="id"
      return-object
      hide-details
      hide-selected
      chips
      cache-items
      dense
      flat
      @update:search-input="searchTerm = $event">
      <template slot="no-data">
        <v-list-item v-if="labels.noDataLabel || labels.searchPlaceholder" class="pa-0">
          <v-list-item-title v-if="labels.noDataLabel">
            {{ labels.noDataLabel }}
          </v-list-item-title>
          <v-list-item-title v-else>
            {{ labels.searchPlaceholder }}
          </v-list-item-title>
        </v-list-item>
      </template>

      <template slot="selection" slot-scope="{item, selected}">
        <v-chip
          v-if="item.profile"
          :input-value="selected"
          close
          class="identitySuggesterItem"
          @click:close="remove(item)">
          <v-avatar left>
            <v-img :src="item.profile.avatarUrl"></v-img>
          </v-avatar>
          {{ item.profile.fullName }}
        </v-chip>
      </template>

      <template slot="item" slot-scope="data">
        <v-list-item-avatar
          v-if="data.item && data.item.profile && data.item.profile.avatarUrl"
          size="20">
          <v-img :src="data.item.profile.avatarUrl"></v-img>
        </v-list-item-avatar>
        <v-list-item-title v-text="data.item.profile.fullName" />
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
import * as suggesterService from '../js/SuggesterService.js';

export default {
  props: {
    value: {
      type: Object,
      default: function() {
        return false;
      },
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
    includeUsers: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    includeSpaces: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    disabled: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    multiple: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    required: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    rules: {
      type: Array,
      default: function() {
        return [];
      },
    },
    height: {
      type: Number,
      default: function() {
        return null;
      },
    },
  },
  data() {
    return {
      items: [],
      id: `AutoComplete${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
      previousSearchTerm: null,
      searchTerm: null,
      loadingSuggestions: 0,
    };
  },
  watch: {
    searchTerm(value) {
      if (value && value.length) {
        window.setTimeout(() => {
          if (this.previousSearchTerm === this.searchTerm) {
            this.loadingSuggestions = 0;
            this.items = [];

            suggesterService.searchSpacesOrUsers(value, this.items, this.includeUsers, this.includeSpaces,
              () => this.loadingSuggestions++,
              () => {
                this.loadingSuggestions--;
              });
          }
          this.previousSearchTerm = this.searchTerm;
        }, 400);
      } else {
        this.items = [];
      }
    },
    value() {
      this.emitSelectedValue(this.value);
    },
  },
  methods: {
    emitSelectedValue(value) {
      this.$emit('input', value);
      this.searchTerm = null;
    },
    focus() {
      this.$refs.selectAutoComplete.focus();
    },
    clear() {
      this.items = [];
      this.value = null;
      this.searchTerm = null;
      this.loadingSuggestions = 0;
    },
    remove(item) {
      if(this.value) {
        if(this.value.splice) {
          const index = this.value.indexOf(item);
          if (index >= 0){
            this.value.splice(index, 1);
          }
        } else {
          this.value = null;
        }
      }
    },
  },
};
</script>