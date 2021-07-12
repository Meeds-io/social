import MfaAccess from './components/MfaAccess.vue';
import OtpAccess from './components/OtpAccess.vue';

const components = {
  'mfa-otp-access': OtpAccess,
  'mfa-access': MfaAccess,
};

for (const key in components) {
  Vue.component(key, components[key]);
}


const otpExtensionPlugin = {
  id: 'OTP',
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

extensionRegistry.registerExtension('mfa-extension', 'mfa-extension', otpExtensionPlugin);
