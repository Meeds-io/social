<template>
  <v-app>
    <!--mfa-fido-access /-->
    <component
      v-if="extension"
      :ref="extension.id"
      v-model="extension.component.model.value"
      :is="extension.component.name" />
  </v-app>
</template>
<script>
import {getSettings} from '../mfaSettings.js';

export default {
  data() {
    return {
      extension: null,
    };
  },
  mounted() {
    getSettings().then(settings => {
      this.extension = extensionRegistry.loadExtensions('mfa-extension', 'mfa-extension')
        .find(plugin => plugin.id === settings.mfaSystem);
    });
  },
};

</script>
