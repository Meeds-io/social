<template>
  <v-app>
    <v-container class="pa-0">
      <div
        align="center"
        justify="center">
        <h class="font-weight-bold titleClass pb-3">{{ $t('mfa.otp.access.title') }}</h>
        <div class="otpAccessBlock lockIcon">
        </div>
        <div v-if="screen === 'registration'">
          <div class="messageClass">1. {{ $t('mfa.otp.registration.step1') }}</div>
          <div class="messageClass">2. {{ $t('mfa.otp.registration.step2') }}</div>
          <img :src="secretSrc">
          <div class="messageClass">
            {{ $t('mfa.otp.registration.alternativeStep2') }}<br />
            {{ secret }}
          </div>
          <div class="messageClass">3. {{ $t('mfa.otp.registration.step3') }}</div>
        </div>
        <div v-if="screen === 'askToken'">
          <div class="messageClass">{{ $t('mfa.otp.registration.step3') }}</div>
        </div>
        <div v-if="screen === 'error'">
          <div class="otpAccessBlock lockIcon">
            <i class="uiIconCloseCircled closeCircledIconColor"></i>
          </div>
          <div class="font-italic messageClass">{{ $t('mfa.otp.access.echec') }}</div>
        </div>
        <div v-if="screen === 'askToken' || 'registration'">
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
      </div>
    </v-container>
  </v-app>
</template>
<script>
export default {
  data() {
    return {
      token: '',
      screen: '',
      secret: '',
      secretSrc: ''
    };
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  created() {
    this.changeScreen('initial');
    window.setTimeout(() => {
      this.checkRegistration();
    }, 1000);
  },
  methods: {
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
    changeScreen(screen) {
      this.screen = screen;
    },
    checkRegistration() {
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/otp/checkRegistration`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => resp && resp.ok && resp.json())
        .then(data => {
          if (data.result && data.result === 'true') {
            this.changeScreen('askToken');
          } else {
            this.startRegistration();
          }
        });
    },
    startRegistration() {
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/otp/generateSecret`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => resp && resp.ok && resp.json())
        .then(data => {
          if (data.secret) {
            window.require(['SHARED/qrcode'], () => {
              this.secret=data.secret;
              this.secretSrc = window.QRCode.generatePNG(data.url, {});
              this.changeScreen('registration');
            });
          } else {
            this.changeScreen('error');
          }
        });
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
