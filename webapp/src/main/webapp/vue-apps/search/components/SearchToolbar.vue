<template>
  <div class="d-flex align-center mx-4 mb-2">
    <v-text-field
      ref="searchInput"
      id="searchInput"
      v-model="term"
      :placeholder="searchInputPlaceholder"
      :aria-label="searchInputPlaceholder"
      rounded
      clearable
      clear-icon="fas fa-times fa-1x"
      dense
      outlined
      prepend-inner-icon="fas fa-search"
      type="text"
      autofocus
      class="border-box-sizing"
      @keypress.enter="searchByEnter">
      <template #append>
        <extension-registry-components
          :params="{
            standalone,
            term,
            searchInput: $refs.searchInput,
          }"
          name="SearchToolbarInputAppend"
          type="search-toolbar-input"
          parent-element="div"
          element="div"
          class="d-flex mt-n2 pt-2px" />
      </template>
    </v-text-field>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    standalone: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    term: null,
    changed: false,
    typing: false,
  }),
  computed: {
    searchInputPlaceholder() {
      return this.$t('Search.label.inputPlaceHolder', {0: eXo.env?.portal?.companyName});
    }
  },
  watch: {
    value() {
      if (this.term !== this.value) {
        this.term = this.value;
      }
    },
    term() {
      if (!this.term) {
        this.$emit('input', this.term);
        return;
      } else if (this.$root.disableAutoSearch) {
        this.changed = true;
      } else {
        this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
        if (!this.typing) {
          this.typing = true;
          this.waitForEndTyping();
        }
      }
    },
  },
  created() {
    if (this.value && !this.term) {
      this.term = this.value;
    }
    if (this.standalone) {
      const search = window.location.search && window.location.search.substring(1);
      if (search) {
        const parameters = JSON.parse(
          `{"${decodeURI(search)
            .replace(/"/g, '\\"')
            .replace(/&/g, '","')
            .replace(/=/g, '":"')}"}`
        );
        this.term = window.decodeURIComponent(parameters['q']) || this.value || '';
      }
    }
  },
  methods: {
    searchByEnter() {
      if (this.$root.disableAutoSearch && this.changed) {
        this.$emit('input', this.term);
      }
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.$emit('input', this.term);
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  },
};
</script>
