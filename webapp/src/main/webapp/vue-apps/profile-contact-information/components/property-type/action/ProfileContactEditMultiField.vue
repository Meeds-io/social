<template>
  <v-card-text class="text-color pb-2">
    <div class="d-flex">
      <div class="align-start flex-grow-1 text-no-wrap d-flex align-center">
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
    <v-flex
      v-for="(childProperty, index) in property.children"
      :key="childProperty.id">
      <div v-if="showChild(childProperty, property)">
        <profile-dropdown-property
          v-if="property.dropdownList"
          :multi-valued="true"
          :parent-property="property"
          :property="childProperty"
          :property-label="getResolvedName(childProperty)"
          @remove="remove(index)"
          @property-updated="propertyUpdated" />
        <profile-contact-edit-multi-field-select
          v-else
          :property="childProperty"
          :parent-propery="property"
          :properties="property.children"
          :multi-valued="property.multiValued"
          @propertyUpdated="propertyUpdated"
          @remove="remove(index)" />
      </div>
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
    showChild(property, parent) {
      return property.isNew
          || (property.value && property.visible && property.active)
          || (parent.multiValued && property.value && parent.active && parent.visible);
    },
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
    propertyUpdated() {
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
