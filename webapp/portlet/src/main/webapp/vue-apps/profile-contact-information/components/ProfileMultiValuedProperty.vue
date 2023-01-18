<template>
  <div v-if="property && property.visible && hasValues">
    <v-flex class="d-flex">
      <div class="align-start text-no-wrap font-weight-bold me-3">
        {{ getResolvedName(property)}}
      </div>
      <div class="align-end flex-grow-1 text-truncate text-end">
        <div
          v-for="(childProperty, i) in property.children"
          :key="i"
          :title="childProperty.value"
          class="text-no-wrap text-truncate">
          <span v-if="childProperty.propertyName" class="pe-1 text-capitalize">
            {{ getResolvedName(childProperty)}}:
          </span>
          <span v-autolinker="childProperty.value"></span>
        </div>
      </div>
    </v-flex>
    <v-divider class="my-4" />
  </div>
</template>

<script>
export default {
  props: {
    property: {
      type: Object,
      default: () => null,
    },
  },
  computed: {
    hasValues(){
      if (this.property && this.property.children && this.property.children.length ){
        if (this.property.children.some(e => e.value)) {
          return true;
        } 
      }
      return false;
    }
  },
  methods: {
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    }
  },
};
</script>