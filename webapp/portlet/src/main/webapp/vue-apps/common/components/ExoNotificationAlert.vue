<template>
  <v-alert
    v-model="displayAlert"
    :type="alertType"
    :max-width="maxWidth"
    border="left"
    class="white"
    elevation="2"
    dismissible
    colored-border
    outlined>
    <span class="text-color">
      {{ alertMessage }}
    </span>
    <slot name="actions">
    </slot>
    <v-btn
      v-if="alert && alert.click"
      class="primary--text"
      text
      @click="alert.click">
      {{ alert.clickMessage }}
    </v-btn>
    <v-btn
      slot="close"
      slot-scope="{toggle}"
      icon
      small
      light
      @click="toggle">
      <v-icon>close</v-icon>
    </v-btn>
  </v-alert>
</template>

<script>
export default {
  props: {
    alert: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    displayAlert: true,
  }),
  computed: {
    alertMessage() {
      return this.alert && this.alert.message;
    },
    alertType() {
      return this.alert && this.alert.type;
    },
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
    maxWidth() {
      return this.isMobile && '100vw' || '50vw';
    },
  },
  watch: {
    displayAlert() {
      if (!this.displayAlert) {
        this.$emit('dismissed');
      }
    },
    alert() {
      if (this.alert) {
        this.showAlert();
      } else {
        this.$emit('dismissed');
      }
    },
  },
  created() {
    const time = 5000;
    window.setTimeout(() => this.displayAlert = false, time);
  },
  methods: {
    showAlert() {
      const time = 5000;
      this.displayAlert = true;
      window.setTimeout(() => this.$emit('dismissed'), time);
    },
  },
};
</script>