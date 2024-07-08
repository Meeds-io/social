<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    id="defaultLanguageDrawer"
    allow-expand
    right>
    <template slot="title">
      {{ $t('generalSettings.manageDefaultLanguage.drawer.title') }}
    </template>
    <template v-if="drawer" slot="content">
      <v-radio-group v-model="language" class="px-4">
        <v-radio
          v-for="lang in languages"
          :key="lang.value"
          :label="lang.text"
          :value="lang.value"
          class="text-capitalize" />
      </v-radio-group>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('generalSettings.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="saveLanguage">
          {{ $t('generalSettings.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    branding: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    language: null,
  }),
  computed: {
    languages() {
      return this.branding?.supportedLanguages && Object.keys(this.branding?.supportedLanguages).map(l => ({
        value: l.replace('_', '-'),
        text: this.branding.supportedLanguages[l],
      })) || [];
    },
  },
  created() {
    this.$root.$on('default-language-edit', this.open);
  },
  methods: {
    open() {
      this.language = this.branding?.defaultLanguage;
      this.$refs.drawer.open();
    },
    saveLanguage() {
      const lang = this.language.replace('-', '_');
      this.loading = true;
      this.$languageSettingService.saveDefaultLanguage(lang)
        .then(() => {
          this.$emit('refresh');
          this.$root.$emit('alert-message', this.$t('generalSettings.defaultLanguageSettingSaved'), 'success');
          this.close();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('generalSettings.defaultLanguageSettingError'), 'error'))
        .finally(() => this.loading = false);
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>

