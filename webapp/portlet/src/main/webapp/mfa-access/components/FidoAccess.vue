<template>
  <v-app>
    <v-container class="pa-0">
      <div align="center"
           justify="center">
        <h class="font-weight-bold titleClass pb-3">{{ $t('mfa.otp.access.title') }}</h>
        <div class="font-weight-medium infoClass">{{ $t('mfa.fido.access.info') }}</div>
        <template v-if="screen === 'initial'">
          <div class="otpAccessBlock usbIcon">
            <i class="uiIconLock lockIconColor"></i>
          </div>
          <div class="font-italic messageClass">{{ $t('mfa.fido.access.confirm') }}</div>
        </template>
        <template v-if="screen === 'register'">
          <div class="progressCircularBlock">
            <v-progress-circular
              :size="100"
              :width="10"
              color="primary"
              indeterminate
            ></v-progress-circular>
          </div>
          <div class="font-italic messageClass">{{ $t('mfa.fido.access.save') }}</div>
        </template>
        <template v-if="screen === 'success'">
          <div class="otpAccessBlock usbIcon">
            <i class="uiIconSuccess successIconColor"></i>
          </div>
          <div class="font-italic messageClass">{{ $t('mfa.fido.access.save') }}</div>
        </template>
        <template v-if="screen === 'authenticate'">
          <div class="progressCircularBlock">
            <v-progress-circular
              :size="100"
              :width="10"
              color="primary"
              indeterminate
            ></v-progress-circular>
          </div>
          <div class="font-italic messageClass">{{ $t('mfa.fido.access.check') }}</div>
        </template>
        <template v-if="screen === 'error'">
          <div class="otpAccessBlock usbIcon">
            <i class="uiIconCloseCircled closeCircledIconColor"></i>
          </div>
          <div class="font-italic messageClass">{{ $t('mfa.fido.access.echec') }}</div>
        </template>
      </div>
    </v-container>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    screen: 'initial',
  }),
  created() {
    this.startRegistration();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
    strToBin(str) {
      return Uint8Array.from(str, c => c.charCodeAt(0));
    },
    binToStr(bin) {
      return new TextDecoder().decode(bin);
    },
    binToB64Str(bin) {
      return btoa(new Uint8Array(bin).reduce(
        (s, byte) => s + String.fromCharCode(byte), ''
      ));
    },
    changeScreen(screen) {
      this.screen = screen;
    },
    b64StrToBin(str) {
      return Uint8Array.from(atob(str), c => c.charCodeAt(0));
    },
    credentialListConversion(list) {
      return list.map(item => {
        const cred = {
          type: item.type,
          id: this.b64StrToBin(item.id),
        };
        return cred;
      });
    },
    startAuthentication() {
      this.changeScreen('authenticate');
      if (navigator.credentials && navigator.credentials.get) {
        fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/fido/startAuthentication`, {
          method: 'GET',
          credentials: 'include',
        }).then(resp => {
          if (resp && resp.ok) {
            resp.json().then(options => {
              console.log(options);

              const requestOptions = {};
              requestOptions.challenge = this.strToBin(options.challenge);

              if ('rpId' in options) {
                requestOptions.rpId = options.rpId;
              }
              if ('allowCredentials' in options) {
                requestOptions.allowCredentials = this.credentialListConversion(options.allowCredentials);
              }
              console.log('sending assertion request:');
              console.log(requestOptions);
              navigator.credentials.get({
                'publicKey': requestOptions
              }).then(assertion => {
                this.finishAuthentication(assertion);
              }).catch((err) => {
                this.changeScreen('error');
                console.error('Unable to get credentials', err);
              });
            });
          } else {
            this.changeScreen('error');
            console.error('Error when starting authentication');
          }
        });
      } else {
        this.changeScreen('error');
        console.error('WebAuthn is not available on this browser');
      }
    },
    finishAuthentication(assertion) {
      console.log('received assertion response:');
      console.log(assertion);

      const publicKeyCredential = {};
      if ('id' in assertion) {
        publicKeyCredential.id = assertion.id;
      }
      if ('type' in assertion) {
        publicKeyCredential.type = assertion.type;
      }
      if ('rawId' in assertion) {
        publicKeyCredential.rawId = this.binToB64Str(assertion.rawId);
      }
      if (!assertion.response) {
        console.error('Start Authentication response lacking response attribute');
        return;
      }

      publicKeyCredential.response = {
        clientDataJSON: this.binToB64Str(assertion.response.clientDataJSON),
        authenticatorData: this.binToB64Str(assertion.response.authenticatorData),
        signature: this.binToB64Str(assertion.response.signature)

      };
      if (assertion.response.userHandle) {
        publicKeyCredential.response.userHandle=this.binToStr(assertion.response.userHandle);
      }


      console.log(publicKeyCredential);
      let formData = new FormData();
      formData.append('data', JSON.stringify(publicKeyCredential));
      formData=new URLSearchParams(formData);
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/fido/finishAuthentication`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'pragma': 'no-cache',
          'cache-control': 'no-cache',
        },
        body: formData
      }).then(resp => {
        if (resp && resp.ok) {
          console.log('Authentication success');
          this.changeScreen('success');
          window.location.href=this.getQueryParam('initialUri');
        } else {
          this.changeScreen('error');
          console.error('Unable to finalize authentication');
        }
      });
    },
    startRegistration() {
      if (navigator.credentials && navigator.credentials.create) {
        fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/fido/startRegistration`, {
          method: 'GET',
          credentials: 'include',
        }).then(resp => {
          if (resp && resp.ok) {
            resp.json().then(options => {
              console.log(options);
              const makeCredentialOptions = {};
              makeCredentialOptions.rp = options.rp;
              makeCredentialOptions.user = options.user;
              makeCredentialOptions.user.id = this.strToBin(options.user.id);
              makeCredentialOptions.challenge = this.strToBin(options.challenge);
              makeCredentialOptions.pubKeyCredParams = options.pubKeyCredParams;

              if ('authenticatorSelection' in options) {
                makeCredentialOptions.authenticatorSelection = options.authenticatorSelection;
              }
              if ('attestation' in options) {
                makeCredentialOptions.attestation = options.attestation;
              }
              if ('excludeCredentials' in options) {
                makeCredentialOptions.excludeCredentials = this.credentialListConversion(options.excludeCredentials);
              }

              console.log('sending attestation request:');
              console.log(makeCredentialOptions);
              if (makeCredentialOptions.excludeCredentials.length>0) {
                //at least one authenticator is registred
                //so we should make authentication instead of register
                this.startAuthentication();
              } else {

                this.changeScreen('register');
                navigator.credentials.create({
                  'publicKey': makeCredentialOptions
                }).then(attestation => {
                  this.finishRegistration(attestation);
                }).catch((err) => {
                  this.changeScreen('error');
                  console.error('Unable to create credentials', err);
                });
              }
            });
          } else {
            console.error('Error when starting registration');
          }
        });
      } else {
        this.changeScreen('error');
        console.error('WebAuthn is not available on this browser');
      }
    },
    finishRegistration(attestation) {
      console.log('received attestation response');
      console.log(attestation);
      const publicKeyCredential = {};

      if ('id' in attestation) {
        publicKeyCredential.id = attestation.id;
      }
      if ('type' in attestation) {
        publicKeyCredential.type = attestation.type;
      }
      if (!attestation.response) {
        console.error('Make Credential response lacking response attribute');
        return;
      }

      const response = {};
      response.clientDataJSON = this.binToStr(attestation.response.clientDataJSON);
      response.attestationObject = this.binToB64Str(attestation.response.attestationObject);

      publicKeyCredential.response = response;

      console.log(publicKeyCredential);
      console.log(JSON.stringify(publicKeyCredential));
      let formData = new FormData();
      formData.append('data', JSON.stringify(publicKeyCredential));
      formData=new URLSearchParams(formData);
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/fido/finishRegistration`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'pragma': 'no-cache',
          'cache-control': 'no-cache',
        },
        body: formData
      }).then(resp => {
        if (resp && resp.ok) {
          console.log('Registration success');
          this.changeScreen('success');
          window.location.href=this.getQueryParam('initialUri');
        } else {
          this.changeScreen('error');
          console.error('Unable to finalize registration');
        }
      });
    }
  },

};
</script>
