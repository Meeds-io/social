<template>
  <div class="d-flex mt-8">
    <v-btn 
      text
      small
      @click="setBorderRadius(0)">
      {{ $t('generalSettings.widgetAndAppStyle.square') }}
    </v-btn>
    <v-slider
      v-model="bordeRadius"
      :thumb-size="24"
      tick-size="2"
      thumb-label="always"
      ticks
      min="0"
      max="20"
      step="4"
      color="primary" />
    <v-btn
      text
      small
      @click="setBorderRadius(20)"> 
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
    bordeRadius: 8
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
  },
  methods: {
    setBorderRadius(value) {
      this.bordeRadius = value;
      this.$emit('input', this.bordeRadius);
    }
  }
};
</script>
