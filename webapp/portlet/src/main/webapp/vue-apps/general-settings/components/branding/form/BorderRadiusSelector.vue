<template>
  <div class="d-flex mt-8">
    <v-btn 
      text
      small
      @click="bordeRadius = 0">
      {{ $t('generalSettings.widgetAndAppStyle.square') }}
    </v-btn>
    <v-slider
      v-model="bordeRadius"
      :thumb-size="24"
      :max="maxBorderRadius"
      tick-size="2"
      thumb-label="always"
      ticks
      min="0"
      step="4"
      color="primary" />
    <v-btn
      text
      small
      @click="bordeRadius = maxBorderRadius"> 
      {{ $t('generalSettings.widgetAndAppStyle.rounded') }} 
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    bordeRadius: 8,
    maxBorderRadius: 20
  }),
  watch: {
    value() {
      this.bordeRadius = this.value;
    },
    bordeRadius() {
      this.$emit('input', this.bordeRadius);
    },
  },
  created() {
    this.bordeRadius = this.value;
  }
};
</script>
