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
      :height="height"
      :filter="filterIgnoredItems"
      :hide-no-data="hideNoData"
      :class="autocompleteClass"
      :prepend-inner-icon="prependInnerIcon"
      append-icon=""
      menu-props="closeOnClick, closeOnContentClick, maxHeight = 100"
      class="identitySuggester"
      content-class="identitySuggesterContent"
      width="100%"
      max-width="100%"
      :item-text="itemText"
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
          <v-list-item-title
            v-if="displaySearchPlaceHolder"
            :style="menuItemStyle"
            class="px-2">
            {{ labels.searchPlaceholder }}
          </v-list-item-title>
          <v-list-item-title
            v-else-if="loadingSuggestions > 0"
            :style="menuItemStyle"
            class="px-2">
            {{ $t('Search.label.inProgress') }}
          </v-list-item-title>
          <v-list-item-title
            v-else-if="labels.noDataLabel"
            :style="menuItemStyle"
            class="px-2">
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
            <v-img :src="item.profile.avatarUrl" role="presentation" />
          </v-avatar>
          <span class="text-truncate">
            {{ item.profile.external
              ? (itemText !== 'profile.fullName' ? item[itemText] : item.profile.fullName).concat(' (').concat($t('userAvatar.external.label')).concat(')')
              : itemText !== 'profile.fullName' ? item[itemText] : item.profile.fullName
            }}          
          </span>
        </v-chip>
      </template>

      <template slot="item" slot-scope="data">
        <v-list-item-avatar
          v-if="data.item && data.item.profile && data.item.profile.avatarUrl"
          size="20">
          <v-img :src="data.item.profile.avatarUrl" />
        </v-list-item-avatar>
        <v-list-item-title
          :style="menuItemStyle"
          class="text-truncate identitySuggestionMenuItemText"
          v-text="itemText !== 'profile.fullName' ? data.item[itemText] : data.item.profile.fullName" />
      </template>
    </v-autocomplete>
  </v-flex>
</template>

<script>
export default {
  props: {
    allGroupsForAdmin: {
      type: Boolean,
      default: false,
    },
    value: {
      type: Object,
      default: null,
    },
    items: {
      type: Array,
      default: function() {
        return [];
      },
    },
    itemText: {
      type: String,
      default: function() {
        return 'profile.fullName';
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
    includeGroups: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    noRedactorSpace: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    onlyRedactor: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    onlyManager: {
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
    width: {
      type: String,
      default: function() {
        return null;
      },
    },
    groupMember: {
      type: String,
      default: function() {
        return null;
      },
    },
    groupType: {
      type: String,
      default: function() {
        return null;
      },
    },
    sugesterClass: {
      type: String,
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
    filterStyle: {
      type: Boolean,
      default: false
    },
    autofocus: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      id: `AutoComplete${parseInt(Math.random() * 10000)
        .toString()
        .toString()}`,
      previousSearchTerm: null,
      searchTerm: null,
      searchStarted: false,
      loadingSuggestions: 0,
      startSearchAfterInMilliseconds: 300,
      endTypingKeywordTimeout: 50,
      startTypingKeywordTimeout: 0,
      typing: false,
    };
  },
  computed: {
    prependInnerIcon() {
      return this.filterStyle && 'fa-filter' || '';
    },
    autocompleteClass() {
      const requiredClass = this.required && !this.value && 'required-field invalid' || this.required && 'required-field' || '';
      const sugesterStyleClass = this.filterStyle && 'identitySuggesterFilterStyle' || 'identitySuggesterInputStyle';
      return `${requiredClass} ${sugesterStyleClass} ${this.sugesterClass || ''}`;
    },
    displaySearchPlaceHolder() {
      return this.labels.searchPlaceholder && !this.searchStarted;
    },
    hideNoData() {
      return !this.searchStarted && this.items.length === 0;
    },
    menuItemStyle() {
      return this.width && `width:${this.width}px;max-width:${this.width}px;min-width:${this.width}px;` || '';
    },
  },
  watch: {
    loadingSuggestions() {
      if (this.loadingSuggestions > 0 && !this.searchStarted) {
        this.searchStarted = true;
      }
    },
    searchTerm() {
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
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
    if (this.autofocus) {
      window.setTimeout(() => {
        this.focus();
      }, 200);
    }
    this.init();
  },
  methods: {
    init() {
      if (this.value && this.value.length) {
        this.value.forEach(item => {
          if ( item.profile ) {
            this.items = this.value;
          }
        });
      } else if (this.value && this.value.profile){
        this.items = this.value;
      }
    },
    emitSelectedValue(value) {
      this.$emit('input', value);
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
      this.loadingSuggestions = 0;
    },
    remove(item) {
      if (this.value) {
        if (this.value.splice) {
          const index = this.value.findIndex(val => val.id === item.id);
          if (index >= 0){
            this.value.splice(index, 1);
          }
        } else {
          this.value = null;
        }
      }
      this.$emit('removeValue',item);
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.searchSpacesOrUsers();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    searchSpacesOrUsers() {
      if (this.searchTerm && this.searchTerm.length) {
        this.focus();
        if (!this.previousSearchTerm || this.previousSearchTerm !== this.searchTerm) {
          this.loadingSuggestions = 0;
          this.items = [];
          if (!this.includeGroups) {
            this.$suggesterService.searchSpacesOrUsers(this.searchTerm,
              this.items,
              this.typeOfRelations,
              this.searchOptions,
              this.includeUsers,
              this.includeSpaces,
              this.onlyRedactor,
              this.noRedactorSpace,
              this.onlyManager,
              () => this.loadingSuggestions++,
              () => {
                this.loadingSuggestions--;
              });
          } else {
            this.$suggesterService.search({
              term: this.searchTerm,
              items: this.items,
              typeOfRelations: this.typeOfRelations,
              searchOptions: this.searchOptions,
              includeUsers: this.includeUsers,
              includeSpaces: this.includeSpaces,
              includeGroups: this.includeGroups,
              onlyRedactor: this.onlyRedactor,
              groupMember: this.groupMember,
              allGroupsForAdmin: this.allGroupsForAdmin,
              groupType: this.groupType,
              noRedactorSpace: this.noRedactorSpace,
              onlyManager: this.onlyManager,
              loadingCallback: () => this.loadingSuggestions++,
              successCallback: () => {
                this.loadingSuggestions--;
              },
              errorCallback: () => {
                throw new Error('Response code indicates a server error');
              }
            });
          }

        }
        this.previousSearchTerm = this.searchTerm;
      } else {
        this.items = [];
        this.searchStarted = false;
      }
    }
  },
};
</script>
