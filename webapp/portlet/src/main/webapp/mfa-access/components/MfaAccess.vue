<template>
  <v-app>
    <div class="mfaAccessBlock lockIcon">
      <h3>{{ $t('mfa.access.title') }}</h3>
      <div class="mfaAccessInfo">{{ $t('mfa.access.info') }}</div>
      <form action="/portal/mfa" method="post">
        <input
          id="tokenInput"
          :placeholder="$t('mfa.access.token.placeholder')"
          class="ignore-vuetify-classes"
          required
          type="text"
          name="token">
        <input
          id="initialUriInput"
          v-model="initialUri"
          type="hidden"
          name="initialUri">
        <button class="btn btn-primary" type="submit">
          {{ $t('mfa.access.button.confirm') }}
        </button>
      </form>
    </div>
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      initialUri: '',
    };
  },
  created() {
    this.initialUri=this.getQueryParam('initialUri');
  },
  methods: {
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
  },
};
</script>
