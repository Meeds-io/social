<template>
  <v-card class="ma-4 border-radius" flat>
    <v-list two-line>
      <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title text-color">
            {{ $t('UserSettings.language') }}
          </v-list-item-title>
          <v-list-item-subtitle class="text-sub-title">
            {{ languageLabel }}
          </v-list-item-subtitle>
        </v-list-item-content>
        <v-list-item-action>
          <v-btn icon>
            <i class="uiIconEdit uiIconLightBlue pb-2"></i>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  computed: {
    languageLabel() {
      const langLabelInEnglish = new Intl.DisplayNames(['en'], {type: 'language'}).of(this.value);
      const langLabelInSameLang = new Intl.DisplayNames([eXo.env.portal.language], {type: 'language'}).of(this.value);
      return `${langLabelInEnglish} - ${langLabelInSameLang}`;
    },
  },
  watch: {
    value() {
      this.$emit('input', this.value);
    },
  },
};
</script>

