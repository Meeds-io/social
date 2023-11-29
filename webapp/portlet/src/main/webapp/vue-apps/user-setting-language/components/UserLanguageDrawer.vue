<template>
  <exo-drawer
    ref="userLanguageDrawer"
    class="userLanguageDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('UserSettings.language') }}
    </template>
    <template v-if="languages && languages.length" slot="content">
      <v-radio-group v-model="value" class="px-4">
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
          @click="cancel">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="saveLanguage">
          {{ $t('UserSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    languages: {
      type: Array,
      default: null,
    },
  },
  methods: {
    open() {
      this.$refs.userLanguageDrawer.open();
    },
    saveLanguage() {
      const lang = this.value.replace('_', '-');
      window.location.replace(`${eXo.env.portal.context}/${lang}/${eXo.env.portal.metaPortalName}/settings`);
    },
    cancel() {
      this.$refs.userLanguageDrawer.close();
    },
  },
};
</script>

