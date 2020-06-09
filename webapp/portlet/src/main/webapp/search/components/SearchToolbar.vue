<template>
  <v-list-item class="pr-0">
    <v-list-item-content class="drawerTitle align-start text-header-title text-truncate">
      <input
        ref="searchInput"
        v-model="term"
        :placeholder="$t('Search.label.inputPlaceHolder')"
        type="text"
        class="ignore-vuetify-classes fill-width my-auto" />
    </v-list-item-content>
    <v-list-item-action class="align-end d-flex flex-row mx-0">
      <v-btn icon class="searchCloseIcon transparent mx-4" @click="$emit('close-search')">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>

<script>
export default {
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
        this.$refs.searchInput.focus();
      }, 200);
    });
  },
  methods: {
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
