<template>
  <v-card class="ma-4 border-radius" flat>
    <v-list two-line>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title text-color">
            {{ $t('UserSettings.language') }}
          </v-list-item-title>
          <v-list-item-subtitle class="text-sub-title text-capitalize">
            {{ languageLabel }}
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn icon @click="openDrawer">
            <i class="uiIconEdit uiIconLightBlue pb-2"></i>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-list>
    <user-language-drawer
      ref="languagesDrawer"
      v-model="language"
      :languages="languages" />
  </v-card>
</template>

<script>
export default {
  props: {
    languages: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    language: eXo.env.portal.language,
  }),
  computed: {
    languageLabel() {
      const language = this.languages.find(lang => lang.value === this.language);
      return language && language.text;
    },
  },
  watch: {
    value() {
      this.$emit('input', this.value);
    },
  },
  methods: {
    openDrawer() {
      this.$refs.languagesDrawer.open();
    },
  },
};
</script>

