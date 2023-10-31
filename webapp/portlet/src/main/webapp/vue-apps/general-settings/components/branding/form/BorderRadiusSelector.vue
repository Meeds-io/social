<template>
  <div class="d-flex mt-8">
    <v-btn 
      text
      small
      @click="setBorderRadius(0)">
      {{ $t('generalSettings.widgetAndAppStyle.square') }}
    </v-btn>
    <v-slider
      v-model="defaultBorderRadius"
      :thumb-size="24"
      tick-size="2"
      thumb-label="always"
      ticks
      min="0"
      max="20"
      step="4"
      color="primary"
      @change="$emit('input', defaultBorderRadius)" />
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
    borderRadius: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    defaultBorderRadius: 8
  }),
  watch: {
    borderRadius() {
      this.defaultBorderRadius = this.borderRadius && Number(this.borderRadius.split('px')[0]);
    }
  },
  created() {
    this.defaultBorderRadius = this.borderRadius && Number(this.borderRadius.split('px')[0]);
  },
  methods: {
    setBorderRadius(value) {
      this.defaultBorderRadius = value;
      console.warn(this.defaultBorderRadius);
      this.$emit('input', this.defaultBorderRadius);
    }
  }
};
</script>
