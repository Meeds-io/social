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
    <span v-sanitized-html="alertMessage" class="text-color"></span>
    <slot name="actions">
    </slot>
    <v-btn
      v-if="alert && alert.link"
      :href="alert.link"
      :class="alert.linkClass"
      target="_blank"
      rel="nofollow noreferrer noopener"
      class="primary--text"
      text>
      {{ alert.linkMessage }}
    </v-btn>
    <v-btn
      v-if="alert && alert.click"
      class="primary--text"
      text
      @click="alert.click">
      {{ alert.clickMessage }}
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
    noTimeout: {
      type: Boolean,
      default: false
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
    if (!this.noTimeout) {
      const time = 5000;
      window.setTimeout(() => this.displayAlert = false, time);
    }
  },
  methods: {
    showAlert() {
      if (!this.noTimeout) {
        const time = 5000;
        this.displayAlert = true;
        window.setTimeout(() => this.$emit('dismissed'), time);
      }
    },
  },
};
</script>