<template>
  <div :id="id"></div>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: () => null,
    },
  },
  data: () => ({
    id: `SearchResult${String(parseInt(Math.random() * 10000))}`,
  }),
  mounted() {
    if (this.result && this.result.connector) {
      const self = this;
      const SearchResultItem = Vue.extend({
        data: () => ({
          result: this.result,
          term: this.term,
          id: this.id,
        }),
        methods: {
          refresh() {
            self.$root.$emit('refresh', self.result.connector);
          },
          refreshFavorites() {
            self.$root.$emit('refresh', self.result.connector, true);
          },
        },
        template: `
          <${this.result.connector.uiComponent} :id="id" :result="result" :term="term" @refresh="refresh" @refresh-favorite="refreshFavorites()" />
        `,
      });

      new SearchResultItem({
        vuetify: Vue.prototype.vuetifyOptions,
        i18n: exoi18n.i18n,
      }).$mount(`#${this.id}`);
      this.$forceUpdate();
    }
  },
};
</script>
