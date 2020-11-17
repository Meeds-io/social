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
      :required="required && !value"
      :items="items"
      :search-input.sync="searchTerm"
      :height="height"
      :filter="filterIgnoredItems"
      :hide-no-data="hideNoData"
      :class="(required && !value && 'required-field invalid') || (required && 'required-field')"
      append-icon=""
      menu-props="closeOnClick, maxHeight = 100"
      class="identitySuggester"
      content-class="identitySuggesterContent"
      width="100%"
      max-width="100%"
      item-text="profile.fullName"
      item-value="id"
      return-object
      persistent-hint
      hide-selected
      chips
      cache-items
      dense
      flat
      @update:search-input="searchTerm = $event">
      <template slot="no-data">
        <v-list-item class="pa-0">
          <v-list-item-title v-if="displaySearchPlaceHolder" class="px-2">
            {{ labels.searchPlaceholder }}
          </v-list-item-title>
          <v-list-item-title v-else-if="labels.noDataLabel" class="px-2">
            {{ labels.noDataLabel }}
          </v-list-item-title>
        </v-list-item>
      </template>

      <template slot="selection" slot-scope="{item, selected}">
        <v-chip
          v-if="item.profile"
          :input-value="selected"
          :close="!disabled"
          class="identitySuggesterItem"
          @click:close="remove(item)">
          <v-avatar left>
            <v-img :src="item.profile.avatarUrl"></v-img>
          </v-avatar>
          <span class="text-truncate">
            {{ item.profile.fullName }}
          </span>
        </v-chip>
      </template>

      <template slot="item" slot-scope="data">
        <v-list-item-avatar
          v-if="data.item && data.item.profile && data.item.profile.avatarUrl"
          size="20">
          <v-img :src="data.item.profile.avatarUrl"></v-img>
        </v-list-item-avatar>
        <v-list-item-title class="text-truncate identitySuggestionMenuItemText" v-text="data.item.profile.fullName" />
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
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
    ignoreItems: {
      type: Array,
      default: function() {
        return [];
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
    typeOfRelations: {
      type: String,
      default: function() {
        return 'mention_activity_stream';
      },
    },
    searchOptions: {
      type: Object,
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
      searchStarted: null,
      loadingSuggestions: 0,
    };
  },
  computed: {
    displaySearchPlaceHolder() {
      return this.labels.searchPlaceholder && (!this.searchStarted || !this.value);
    },
    hideNoData() {
      return !this.labels.noDataLabel && !this.labels.searchPlaceholder;
    },
  },
  watch: {
    loadingSuggestions() {
      if (this.loadingSuggestions > 0 && !this.searchStarted) {
        this.searchStarted = true;
      }
    },
    searchTerm(value) {
      if (value && value.length) {
        window.setTimeout(() => {
          if (this.previousSearchTerm === this.searchTerm) {
            this.loadingSuggestions = 0;
            this.items = [];

            this.$suggesterService.searchSpacesOrUsers(value,
              this.items,
              this.typeOfRelations,
              this.searchOptions,
              this.includeUsers,
              this.includeSpaces,
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
      this.init();
    },
  },

  mounted() {
    $(`#${this.id} input`).on('blur', () => {
      // A hack to close on select
      // See https://www.reddit.com/r/vuetifyjs/comments/819h8u/how_to_close_a_multiple_autocomplete_vselect/
      this.$refs.selectAutoComplete.isFocused = false;
    });
    this.init();
  },
  methods: {
    init() {
      this.items = this.value && (this.value.length && this.value || [this.value]) || [];
    },
    emitSelectedValue(value) {
      this.$emit('input', value);
      this.searchTerm = null;
    },
    canAddItem(item) {
      return !item || !item.id || this.ignoreItems.indexOf(item.id) < 0;
    },
    filterIgnoredItems(item, queryText, itemText) {
      if (queryText && itemText.toLowerCase().indexOf(queryText.toLowerCase()) < 0) {
        return false;
      }
      if (this.ignoreItems && this.ignoreItems.length) {
        return this.canAddItem(item);
      }
      return true;
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
          const index = this.value.findIndex(val => val.id === item.id);
          if (index >= 0){
            this.value.splice(index, 1);
          }
        } else {
          this.value = null;
        }
      }
      this.$emit('removeValue');
    },
  },
};
</script>