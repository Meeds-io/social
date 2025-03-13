<template>
  <div
    v-if="property.value || property.isNew"
    class="d-flex flex-no-wrap pb-2 multiField">
    <select
      v-if="properties?.length && !multiValued"
      v-model="property.propertyName"
      class="ignore-vuetify-classes align-start flex-grow-0 half-width text-capitalize"
      :disabled="!property.editable"
      @change="$emit('propertyUpdated')">
      <option
        v-for="item in filtredProperties"
        :key="item.propertyName"
        class="text-capitalize"
        :value="item.propertyName">
        {{ getResolvedName(item) }}
      </option>
    </select>
    <input
      ref="multiInput"
      v-model="property.value"
      class="ignore-vuetify-classes align-end flex-grow-1"
      :disabled="parentProperty?.children && !parentProperty?.editable || !property.editable"
      maxlength="2000"
      :title="property.value"
      type="text"
      @change="$emit('propertyUpdated')"
      @input="$emit('propertyUpdated')">
    <v-icon
      class="removeMultiFieldValue  error--text"
      :class="hasParent && 'hasParent' ||'noParent'"
      small
      @click="$emit('remove')">
      fa-minus
    </v-icon>
    <div v-if="!multiValued">
      <profile-hide-property-button
        :property="property" />
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      property: {
        type: Object,
        default: () => null,
      },
      parentProperty: {
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
      },
    },
    computed: {
      hasParent () {
        return this.properties?.length && !this.multiValued;
      },
      filtredProperties (){
        return this.properties.filter((obj, index, self) =>
          obj.isNew && !obj.value || (index === self.findIndex(t => (
            t.propertyName === obj.propertyName
          )) && obj.visible && obj.active)
        );
      },
    },
  
    created () {
      this.$root.$on('non-valid-url-input', this.showError);
      this.$root.$on('reset-custom-validity', this.resetCustomValidity);
    },
    methods: {
      getResolvedName (item){
        const lang = eXo && eXo.env.portal.language || 'en';
        const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
        if (resolvedLabel){
          return resolvedLabel.label;
        }
        return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
      },
      showError (value){
        if (this.$refs.multiInput && this.$refs.multiInput.value===value){
          this.$refs.multiInput.setCustomValidity(this.$t('profileContactInformation.invalidUrlFormat'));
        }
      },
      resetCustomValidity () {
        if (this.$refs.multiInput) { this.$refs.multiInput.setCustomValidity('');}
      },
    },
  };
</script>
