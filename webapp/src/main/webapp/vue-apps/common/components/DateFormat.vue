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
        default () {
          return eXo.env.portal.language;
        },
      },
      format: {
        type: Object,
        default () {
          return {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
          };
        },
      },
    },
    computed: {
      displayedDate () {
        const value = this.value && Number.isInteger(Number(this.value)) ? Number(this.value) : this.value;
        return value && eXo.$dateUtil.formatDateObjectToDisplay(new Date(value), this.format, this.lang) || '';
      },
    },
  };
</script>