<template>
  <div>{{ displayedDate }}</div>
</template>
<script>
export default {
  props: {
    value: {
      type: Number,
      default: () => 0,
    },
    lang: {
      type: String,
      default: function() {
        return eXo.env.portal.language;
      },
    },
    format: {
      type: Object,
      default: function() {
        return {
          year: 'numeric',
          month: 'long',
          day: 'numeric',
        };
      },
    },
  },
  computed: {
    displayedDate() {
      return this.value && this.$dateUtil.formatDateObjectToDisplay(new Date(this.value), this.format, this.lang) || '';
    },
  },
};
</script>