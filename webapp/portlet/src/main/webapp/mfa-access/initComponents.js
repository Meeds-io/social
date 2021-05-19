import MfaAccess from './components/MfaAccess.vue';
import OtpAccess from './components/OtpAccess.vue';
import FidoAccess from './components/FidoAccess.vue';

const components = {
  'mfa-otp-access': OtpAccess,
  'mfa-access': MfaAccess,
  'mfa-fido-access': FidoAccess,
};

for (const key in components) {
  Vue.component(key, components[key]);
}


const fidoExtensionPlugin = {
  id: 'fido2',
  title: 'fido',
  enabled: () => true,
  component: {
    name: 'mfa-fido-access',
    model: {
      value: [],
      default: []
    }
  }
};

const otpExtensionPlugin = {
  id: 'otp',
  title: 'otp',
  enabled: () => true,
  component: {
    name: 'mfa-otp-access',
    model: {
      value: [],
      default: []
    }
  }
};


extensionRegistry.registerExtension('mfa-extension', 'mfa-extension', fidoExtensionPlugin);
extensionRegistry.registerExtension('mfa-extension', 'mfa-extension', otpExtensionPlugin);
