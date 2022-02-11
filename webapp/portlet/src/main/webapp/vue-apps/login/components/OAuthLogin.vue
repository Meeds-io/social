<template>
  <a :id="id" :href="link">
    <i :class="cssClass"></i>
  </a>
</template>
<script>
export default {
  props: {
    provider: {
      type: Object,
      default: null,
    },
    rememberme: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    id() {
      return `login-${this.provider.key}`;
    },
    link() {
      return this.rememberme && `${this.provider.url}&_rememberme=true` || this.provider.url;
    },
    cssClass() {
      return `${this.provider.key.toLowerCase()} uiIconOauth white rounded-lg`;
    },
  },
};
</script>