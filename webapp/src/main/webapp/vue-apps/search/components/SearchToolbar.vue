<template>
  <v-list-item class="px-0 my-2">
    <v-list-item-content class="align-start">
      <v-text-field
        id="searchInput"
        ref="searchInput"
        v-model="term"
        :placeholder="$t('Search.label.inputPlaceHolder')"
        type="text"
        autocomplete="off"
        class="fill-width my-auto pt-0 px-4 searchInputParent" />
    </v-list-item-content>
    <v-list-item-action class="align-end d-flex flex-row ms-0 me-4">
      <v-btn
        v-if="term"
        text
        color="error"
        @click="clearSearchTerm">
        {{ $t('search.connector.label.clear') }}
      </v-btn>
      <v-btn
        v-if="!standalone"
        :aria-label="$t('Search.button.close.label')"
        icon
        class="searchCloseIcon transparent"
        @click="$emit('close-search')">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
  props: {
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
    typing: false,
  }),
  watch: {
    term() {
      if (!this.term) {
        this.$emit('search');
        return;
      }
      this.startTypingKeywordTimeout = Date.now() + this.startSearchAfterInMilliseconds;
      if (!this.typing) {
        this.typing = true;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    this.$root.$on('search-opened', () => {
      window.setTimeout(() => {
        this.$refs.searchInput.$el.querySelector('input').focus();
      }, 200);
    });
    if (this.standalone) {
      const search = window.location.search && window.location.search.substring(1);
      if (search) {
        const parameters = JSON.parse(
          `{"${decodeURI(search)
            .replace(/"/g, '\\"')
            .replace(/&/g, '","')
            .replace(/=/g, '":"')}"}`
        );
        this.term = window.decodeURIComponent(parameters['q']);
        this.dialog = true;
      }
    }
  },
  mounted() {
    window.setTimeout(() => {
      this.$refs.searchInput.$el.querySelector('input').focus();
    }, 200);
  },
  methods: {
    clearSearchTerm() {
      this.term = '';
      window.setTimeout(() => {
        this.$refs.searchInput.$el.querySelector('input').focus();
      }, 200);
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() > this.startTypingKeywordTimeout) {
          this.typing = false;
          this.$emit('search', this.term);
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  },
};
</script>
