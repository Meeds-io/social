<template>
  <div class="translation-text-field">
    <v-text-field
      v-model="defaultLanguageValue"
      :id="id"
      :name="id"
      :placeholder="placeholder"
      :required="required"
      :aria-required="required"
      append-icon="fas fa-language"
      class="border-box-sizing pt-0"
      type="text"
      outlined
      dense
      @click:append="openDrawer" />
    <translation-drawer
      ref="translationDrawer"
      v-model="valuesPerLanguage"
      :drawer-title="drawerTitle"
      :default-language="defaultLanguage"
      :supported-languages="supportedLanguages"
      @input="$emit('input', $event)" />
  </div>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: null,
    },
    value: {
      type: Object,
      default: null,
    },
    placeholder: {
      type: String,
      default: null,
    },
    drawerTitle: {
      type: String,
      default: null,
    },
    defaultLanguage: {
      type: String,
      default: null,
    },
    supportedLanguages: {
      type: Array,
      default: null,
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    defaultLanguageValue: null,
    valuesPerLanguage: {},
  }),
  watch: {
    value: {
      immediate: true,
      handler: function() {
        this.valuesPerLanguage = this.value && JSON.parse(JSON.stringify(this.value)) || {};
        this.defaultLanguageValue = this.valuesPerLanguage[this.defaultLanguage] || '';
      },
    },
    defaultLanguageValue() {
      if (this.defaultLanguageValue !== this.valuesPerLanguage[this.defaultLanguage]) {
        this.valuesPerLanguage[this.defaultLanguage] = this.defaultLanguageValue;
        this.$emit('input', this.valuesPerLanguage);
      }
    },
  },
  methods: {
    openDrawer() {
      this.$refs.translationDrawer.open();
    },
  },
};
</script>