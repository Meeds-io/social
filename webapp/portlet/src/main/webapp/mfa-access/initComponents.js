import MfaAccess from './components/MfaAccess.vue';
import OtpAccess from './components/OtpAccess.vue';
import FidoAccess from './components/FidoAccess.vue';

const components = {
  'mfa-otp-access': OtpAccess,
  'mfa-access': MfaAccess,
  'mfa-fido-access': FidoAccess,
};

for(const key in components) {
  Vue.component(key, components[key]);
}
