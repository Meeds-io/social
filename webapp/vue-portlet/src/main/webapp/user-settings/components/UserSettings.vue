<template>
  <v-app>
    <user-setting-language v-model="language" />
    <user-setting-timezone v-model="timezone" />
    <user-setting-notifications v-model="notificationSettings" />
    <user-setting-security />
    <user-setting-social-network />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    skeleton: true,
    language: eXo.env.portal.language,
    timezone: eXo.env.portal.timezone,
    notificationSettings: null,
  }),
  created() {
    this.refresh();
  },
  methods: {
    refresh() {
      // TODO
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    },
  },
};
</script>
