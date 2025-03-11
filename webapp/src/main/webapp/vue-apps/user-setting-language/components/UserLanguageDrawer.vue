<template>
  <exo-drawer
    ref="userLanguageDrawer"
    allow-expand
    class="userLanguageDrawer"
    right>
    <template #title>
      {{ $t('UserSettings.language') }}
    </template>
    <template
      v-if="languages && languages.length"
      #content>
      <v-radio-group
        v-model="langValue"
        class="px-4">
        <v-radio
          v-for="lang in languages"
          :key="lang.value"
          class="text-capitalize"
          :label="lang.text"
          :value="lang.value" />
      </v-radio-group>
    </template>
    <template #footer>
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
    computed: {
      langValue: {
        set(value) {
          this.$emit('input', value);
        },
        get() {
          return this.value;
        },
      },
    },
    methods: {
      open () {
        this.$refs.userLanguageDrawer.open();
      },
      saveLanguage () {
        const lang = this.value.replace('_', '-');
        window.location.replace(`${eXo.env.portal.context}/${lang}/${eXo.env.portal.metaPortalName}/settings`);
      },
      cancel () {
        this.$refs.userLanguageDrawer.close();
      },
    },
  };
</script>

