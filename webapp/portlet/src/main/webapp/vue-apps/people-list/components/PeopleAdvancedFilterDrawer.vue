<template>
  <exo-drawer
      ref="peopleAdvancedFilterDrawer"
      id="peopleAdvancedFilterDrawer"
      right
      @closed="close">
    <template slot="title">
      <span class="popupTitle">{{ $t('pepole.advanced.filter.title') }}</span>
    </template>
    <template slot="content">
      <v-form class="pa-2 ms-2 mt-4">
      <div class="d-flex flex-column flex-grow-1">
      <div class="d-flex flex-row" v-for="item in settings" :key="item">
        <input
            v-model="item.valueToSearch"
            :placeholder="getResolvedName(item)"
            type="text"
            class="input-block-level ignore-vuetify-classes my-3">
      </div>
      </div>
      </v-form>
    </template>
    <template slot="footer">
      <div class="VuetifyApp flex d-flex">
        <v-btn
            class="peopleAdvancedFilterResetButton">
          <v-icon x-small class="pr-1">fas fa-redo</v-icon>
          <template>
            {{ $t('pepole.advanced.filter.button.reset') }}
          </template>
        </v-btn>
        <v-spacer />
        <div class="d-btn">
          <v-btn
              class="btn me-2"
              @click="cancel">
            <template>
              {{ $t('pepole.advanced.filter.button.cancel') }}
            </template>
          </v-btn>
          <v-btn
              :disabled="disabled"
              class="btn btn-primary"
              @click="confirm">
            <template>
              {{ $t('pepole.advanced.filter.button.confirm') }}
            </template>
          </v-btn>
        </div>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  name: 'peopleAdvancedFilterDrawer',
  data() {
    return {
      settings: [],
    };
  },
  created() {
    this.$root.$on('open-people-advanced-filter-drawer', this.openDrawer);
    this.getSettings();
  },
  methods: {
    openDrawer() {
      this.$refs.peopleAdvancedFilterDrawer.open();
    },
    close() {
      this.$refs.peopleAdvancedFilterDrawer.close();
    },
    cancel() {
      this.settings.forEach((element) => {
        if (element && element.valueToSearch) {
          element.valueToSearch = '';
        }
      });
      this.$refs.peopleAdvancedFilterDrawer.close();
    },
    getSettings() {
      return this.$profileSettingsService.getSettings()
        .then(settings => {
          this.settings =  settings.filter((e) => e.active ).map(obj => ({ ...obj, valueToSearch: '' })) || [];
        });
    },
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileSettings.property.name.${item.propertyName}`) !== `profileSettings.property.name.${item.propertyName}` ?this.$t(`profileSettings.property.name.${item.propertyName}`) : item.propertyName;
    }
  }
};

</script>
