<template>
  <v-app
    :class="owner && 'profileContactInformation' || 'profileContactInformationOther'"
    class="white">
    <v-toolbar
      color="white"
      flat
      class="border-box-sizing">
      <div
        class="text-header-title text-sub-title">
        {{ $t('profileContactInformation.contactInformation') }}
      </div>
      <v-spacer />
      <v-btn
        v-if="owner"
        icon
        outlined
        small
        @click="editContactInformation">
        <i class="uiIconEdit uiIconLightBlue pb-2"></i>
      </v-btn>
    </v-toolbar>
    <div class="px-4 pb-6 white">
      <div  v-for="property in properties" :key="property.id" >
        <profile-multi-valued-property v-if="property.children && property.children.length" :property="property" />
        <template v-else-if="property && property.visible && property.value">
          <v-flex class="d-flex">
            <div class="align-start text-no-wrap font-weight-bold me-3">
              {{ getResolvedName(property)}}
            </div>
            <div :title="property.value" class="align-end flex-grow-1 text-truncate text-end">
              {{ property.value }}
            </div>
          </v-flex>
          <v-divider class="my-4" />
        </template>
      </div>
    </div>
    <profile-contact-information-drawer
      v-if="owner"
      ref="contactInformationEdit"
      :properties="properties"
      :upload-limit="uploadLimit"
      @refresh="refresh" />
  </v-app>
</template>

<script>
export default {
  props: {
    uploadLimit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    properties: [],
  }),
  created() {
    this.refreshProperties(); 
  },
  mounted() {
    document.addEventListener('userModified', () => {
      this.refreshProperties();
    });

    if (this.properties) {
      this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    }
  },
  methods: {
    refreshProperties() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'settings')
        .then(properties => {
          this.properties = properties.filter(item => item.active).sort((s1, s2) => ((s1.order > s2.order) ? 1 : (s1.order < s2.order) ? -1 : 0));
          this.$nextTick().then(() => this.$root.$emit('application-loaded'));
        })
        .finally(() => this.$root.$applicationLoaded());
    },
    editContactInformation() {
      this.$refs.contactInformationEdit.open();
    },
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