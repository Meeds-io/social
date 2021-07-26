<template>
  <v-card-text class="text-color pb-2">
    <div class="d-flex">
      <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold d-flex align-center">
        <span>
          {{ title }}
        </span>
      </div>
      <div class="align-end flex-grow-1 text-no-wrap text-end">
        <v-btn
          color="primary"
          class="px-0"
          outlined
          link
          text
          @click="addNewItem">
          + {{ $t('profileContactInformation.addNew') }}
        </v-btn>
      </div>
    </div>
    <v-flex v-for="(value, i) in values" :key="i">
      <profile-contact-edit-multi-field-select
        :value="value"
        :items="items"
        :item-name="itemName"
        :item-value="itemValue"
        :type="type"
        @remove="remove(i)" />
    </v-flex>
  </v-card-text>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: () => null,
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
    items: {
      type: Array,
      default: () => [],
    },
    values: {
      type: Array,
      default: () => null,
    },
  },
  methods: {
    remove(i) {
      this.values.splice(i, 1);
    },
    addNewItem() {
      const item = {};
      if (this.itemName && this.items && this.items.length) {
        item[this.itemName] = this.items[0] || '';
      }
      if (this.itemValue) {
        item[this.itemValue] = '';
      }
      this.values.push(item);
      this.$forceUpdate();
    },
  },
};
</script>