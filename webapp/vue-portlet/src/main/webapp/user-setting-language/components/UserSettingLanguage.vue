<template>
  <v-app v-if="displayed">
    <v-card class="ma-4 border-radius" flat>
      <v-list two-line>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width skeleton-text-height my-2'">
                {{ skeleton && '&nbsp;' || $t('UserSettings.language') }}
              </div>
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title text-capitalize">
              <div :class="skeleton && 'skeleton-background skeleton-border-radius skeleton-text-width-small skeleton-text-height-fine my-2'">
                {{ skeleton && '&nbsp;' || languageLabel }}
              </div>
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              :class="skeleton && 'skeleton-background'"
              icon
              @click="openDrawer">
              <i v-if="!skeleton" class="uiIconEdit uiIconLightBlue pb-2"></i>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
      <user-language-drawer
        ref="languagesDrawer"
        v-model="language"
        :languages="languages" />
    </v-card>
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
    skeleton: true,
  }),
  computed: {
    languageLabel() {
      const language = this.languages.find(lang => lang.value === this.language);
      return language && language.text;
    },
  },
  created() {
    this.languages = this.languages.sort((a, b) => a.text.localeCompare(b.text));
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.skeleton = false;
  },
  methods: {
    openDrawer() {
      this.$refs.languagesDrawer.open();
    },
  },
};
</script>