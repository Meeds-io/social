<template>
  <div class="translation-text-field">
    <v-text-field
      v-model="defaultLanguageValue"
      :id="id"
      :name="id"
      :placeholder="placeholder"
      :required="required"
      :aria-required="required"
      class="border-box-sizing pt-0"
      type="text"
      outlined
      dense>
      <template #append>
        <v-btn
          :title="iconTitle"
          class="mt-n2 pt-2px"
          icon
          @click="openDrawer">
          <v-icon :color="iconColor">fas fa-language</v-icon>
        </v-btn>
      </template>
    </v-text-field>
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
  computed: {
    translationsCount() {
      return Object.keys(this.valuesPerLanguage).length;
    },
    iconColor() {
      return this.translationsCount > 1 ? 'primary' : '';
    },
    iconTitle() {
      return this.translationsCount > 1 ? this.$t('translationDrawer.existingTranslationsTooltip', {0: this.translationsCount - 1}) : this.$t('translationDrawer.noTranslationsTooltip');
    },
  },
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