<template>
  <div v-if="property.value || property.isNew" class="d-flex flex-no-wrap pb-2 multiField">
    <select
      v-if="properties && properties.length && !multiValued"
      v-model="property.propertyName"
      class="ignore-vuetify-classes align-start flex-grow-0 half-width text-capitalize"
      @change="$emit('propertyUpdated')">
      <option
        v-for="item in properties"
        :key="item.propertyName"
        :value="item.propertyName"
        class="text-capitalize">
        {{ getResolvedName(item) }}
      </option>
    </select>
    <input
      v-model="property.value"
      :title="property.value"
      type="text"
      class="ignore-vuetify-classes align-end flex-grow-1"
      maxlength="2000"
      @change="$emit('propertyUpdated')">
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
    property: {
      type: Object,
      default: () => null,
    },
    properties: {
      type: Array,
      default: () => null,
    },
    multiValued: {
      type: Boolean,
      default: () => null,
    }
  },
  methods: {
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = item.labels && item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    },
  }
};
</script>