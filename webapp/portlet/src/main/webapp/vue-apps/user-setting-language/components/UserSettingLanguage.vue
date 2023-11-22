<template>
  <v-app v-if="displayed">
    <div class="card-border-radius overflow-hidden">
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('UserSettings.language') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title text-capitalize font-italic">
              {{ languageLabel }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="openDrawer">
              <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <user-language-drawer
        ref="languagesDrawer"
        v-model="language"
        :languages="languages" />
    </div>
  </v-app>
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
    id: `Settings${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    language: eXo.env.portal.language,
    displayed: true,
  }),
  computed: {
    languageLabel() {
      const language = this.languages.find(lang => lang.value === this.language);
      return language && language.text;
    },
  },
  created() {
    this.languages = this.languages.sort((a, b) => a.text.localeCompare(b.text));
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$applicationLoaded());
  },
  methods: {
    openDrawer() {
      this.$refs.languagesDrawer.open();
    },
  },
};
</script>
