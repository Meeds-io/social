<template>
  <v-app>
    <user-setting-language :languages="languages" />
    <user-setting-timezone :timezones="timezones" />
    <user-setting-notifications v-model="notificationSettings" />
    <user-setting-security />
  </v-app>
</template>

<script>
export default {
  props: {
    languages: {
      type: Array,
      default: null,
    },
    timezones: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    skeleton: true,
    notificationSettings: null,
  }),
  created() {
    this.refresh();
    this.languages = this.languages.sort((a, b) => a.text.localeCompare(b.text));
  },
  methods: {
    refresh() {
      // TODO notification settings
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    },
  },
};
</script>
