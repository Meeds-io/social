<template>
  <v-app>
    <div class="otpAccessBlock lockIcon">
      <h3>{{ $t('mfa.otp.access.title') }}</h3>
      <div class="otpAccessInfo">{{ $t('mfa.otp.access.info') }}</div>
      <form id="otpForm">
        <input
          id="tokenInput"
          v-model="token"
          :placeholder="$t('mfa.otp.access.token.placeholder')"
          class="ignore-vuetify-classes"
          required
          type="text"
          name="token">
        <button class="btn btn-primary" @click="onSubmit()">
          {{ $t('mfa.otp.access.button.confirm') }}
        </button>
      </form>
    </div>
  </v-app>
</template>
<script>
export default {
  data () {
    return {
      token: '',
    };
  },
  methods: {
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
    onSubmit() {
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/otp/verify/?token=${this.token}`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => resp && resp.ok && resp.json())
        .then(data => {
          if (data.result) {
            window.location.href=this.getQueryParam('initialUri');
          }
        });
    }
  },
};
</script>
