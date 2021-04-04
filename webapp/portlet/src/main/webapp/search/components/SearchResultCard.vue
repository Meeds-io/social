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
      const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);
      const vueI18n = new VueI18n({
        locale: this.$i18n.locale,
        messages: this.$i18n.messages,
      });

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
        },
        template: `
          <${this.result.connector.uiComponent} :id="id" :result="result" :term="term" @refresh="refresh" />
        `,
      });

      new SearchResultItem({
        i18n: vueI18n,
        vuetify,
      }).$mount(`#${this.id}`);
      this.$forceUpdate();
    }
  },
};
</script>
