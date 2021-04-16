<template>
  <div class="d-flex flex-no-wrap pb-2 multiField">
    <select
      v-if="items && items.length"
      v-model="value[itemName]"
      class="ignore-vuetify-classes align-start flex-grow-0 half-width text-capitalize">
      <option
        v-for="item in items"
        :key="item"
        :value="item"
        class="text-capitalize">
        {{ getLabel(item) }}
      </option>
    </select>
    <input
      v-model="value[itemValue]"
      :title="value[itemValue]"
      type="text"
      class="ignore-vuetify-classes align-end flex-grow-1"
      maxlength="2000">
    <v-icon
      small
      class="removeMultiFieldValue error--text"
      @click="$emit('remove')">
      fa-minus
    </v-icon>
  </div>
</template>

<script>
export default {
  props: {
    items: {
      type: Array,
      default: () => [],
    },
    type: {
      type: String,
      default: () => null,
    },
    itemName: {
      type: String,
      default: () => null,
    },
    itemValue: {
      type: String,
      default: () => null,
    },
    value: {
      type: Object,
      default: () => ({}),
    },
  },
  methods: {
    getLabel(key) {
      const label = `profileContactInformation.${this.type}.${key}`;
      const translation = this.$t(label);
      return translation === label && key || translation;
    },
  }
};
</script>