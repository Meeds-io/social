<template>
  <v-card-text class="text-color pb-2">
    <div class="d-flex">
      <div class="align-start flex-grow-1 text-no-wrap text-left font-weight-bold d-flex align-center">
        <span>
          {{ getResolvedName(property) }}
        </span>
      </div>
      <div class="align-end flex-grow-1 text-no-wrap text-end">
        <profile-hide-property-button
          v-if="property.multiValued"
          :property="property" />
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
    <v-flex v-for="(childProperty, i) in property.children" :key="i">
      <profile-contact-edit-multi-field-select
        v-if="childProperty.isNew || (childProperty.visible && childProperty.active && childProperty.value) || (property.multiValued && property.active && property.visible && childProperty.value)"
        :property="childProperty"
        :parent-propery="property"
        :properties="property.children"
        :multi-valued="property.multiValued"
        @propertyUpdated="propertyUpdated"
        @remove="remove(i)" />
    </v-flex>
  </v-card-text>
</template>

<script>
export default {
  props: {
    property: {
      type: Object,
      default: () => null,
    }
  },
  methods: {
    remove(i) {
      if (this.property.children[i].isNew) {
        this.property.children.splice(i, 1);
      } else {
        this.property.children[i].value = null;
      }
      this.$emit('propertyUpdated',this.property);
    },
    addNewItem() {
      const item = {isNew: true, editable: true};
      this.property.children.push(item);
      this.$forceUpdate();
    },
    propertyUpdated(){
      this.$emit('propertyUpdated',this.property);
    },
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = !item.labels ? null : item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileContactInformation.${item.propertyName}`)!==`profileContactInformation.${item.propertyName}`?this.$t(`profileContactInformation.${item.propertyName}`):item.propertyName;
    }
  },
};
</script>
