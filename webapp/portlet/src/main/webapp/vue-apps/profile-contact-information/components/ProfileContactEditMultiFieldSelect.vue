<template>
  <div v-if="property.value || property.isNew" class="d-flex flex-no-wrap pb-2 multiField">
    <select
      v-if="properties && properties.length && !multiValued"
      v-model="property.propertyName"
      class="ignore-vuetify-classes align-start flex-grow-0 half-width text-capitalize"
      @change="$emit('propertyUpdated')">
      <option
        v-for="item in filtredProperties"
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
      ref="multiInput"
      @change="$emit('propertyUpdated')"
      @input="$emit('propertyUpdated')">
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
  
  created() {
    this.$root.$on('non-valid-url-input', this.showError);
    this.$root.$on('reset-custom-validity', this.resetCustomValidity);
  },
  computed: {
    filtredProperties(){
      return this.properties.filter((obj, index, self) =>
        index === self.findIndex((t) => (
          t.propertyName === obj.propertyName
        ))
      );
    },
  },
  methods: {
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    },
    showError(value){
      if (this.$refs.multiInput && this.$refs.multiInput.value===value){
        this.$refs.multiInput.setCustomValidity(this.$t('profileContactInformation.invalidUrlFormat'));
      }
    },
    resetCustomValidity() {
      if (this.$refs.multiInput) { this.$refs.multiInput.setCustomValidity('');}
    },
  }
};
</script>