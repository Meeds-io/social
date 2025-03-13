<template>
  <v-alert
    v-model="displayAlert"
    border="left"
    class="white"
    colored-border
    dismissible
    elevation="2"
    :max-width="maxWidth"
    outlined
    :type="alertType">
    <span
      v-sanitized-html="alertMessage"
      class="text-color"></span>
    <slot name="actions"></slot>
    <v-btn
      v-if="alert && alert.link"
      class="primary--text"
      :class="alert.linkClass"
      :href="alert.link"
      rel="nofollow noreferrer noopener"
      target="_blank"
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
        default: null,
      },
      noTimeout: {
        type: Boolean,
        default: false,
      },
    },
    data: () => ({
      displayAlert: true,
    }),
    computed: {
      alertMessage () {
        return this.alert && this.alert.message;
      },
      alertType () {
        return this.alert && this.alert.type;
      },
      isMobile () {
        return eXo.vuetify && eXo.vuetify.display .value&& eXo.vuetify.display.name.value === 'xs';
      },
      maxWidth () {
        return this.isMobile && '100vw' || '50vw';
      },
    },
    watch: {
      displayAlert () {
        if (!this.displayAlert) {
          this.$emit('dismissed');
        }
      },
      alert () {
        if (this.alert) {
          this.showAlert();
        } else {
          this.$emit('dismissed');
        }
      },
    },
    created () {
      if (!this.noTimeout) {
        const time = 5000;
        window.setTimeout(() => this.displayAlert = false, time);
      }
    },
    methods: {
      showAlert () {
        if (!this.noTimeout) {
          const time = 5000;
          this.displayAlert = true;
          window.setTimeout(() => this.$emit('dismissed'), time);
        }
      },
    },
  };
</script>