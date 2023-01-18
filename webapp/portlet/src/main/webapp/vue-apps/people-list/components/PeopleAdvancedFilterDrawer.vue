<template>
  <exo-drawer
      ref="PeopleAdvancedFilterDrawer"
      id="PeopleAdvancedFilterDrawer"
      right
      @closed="close">
    <template slot="title">
      <span class="PopupTitle">{{ $t('pepole.advanced.filter.title') }}</span>
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
            class="resetButtonFilterPeopl">
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
  name: 'PeopleAdvancedFilterDrawer',
  props: {
    keyword: {
      type: String,
      default: null,
    },
  },
  data() {
    return {
      settings: [],
    };
  },
  created() {
    this.$root.$on('open-people-advanced-filter-drawer', this.openDrawer);
    this.getSettings();
  },
  watch: {
    keyword() {
      this.settings = this.settings.map((element) => {
        const keywrd = this.keyword.split(' ');
        if (element && element.propertyName && element.propertyName === 'firstName' || element.propertyName === 'lastName' ) {
          if (element.propertyName === 'firstName' ){
            element.valueToSearch = keywrd.length? keywrd[0] : this.keyword;
          } else {
            element.valueToSearch = keywrd.length? keywrd[1] : '' ;
          }
        }
        return element ;
      });
    },
  },
  methods: {
    openDrawer() {
      this.$refs.PeopleAdvancedFilterDrawer.open();
    },
    close() {
      this.$refs.PeopleAdvancedFilterDrawer.close();
    },
    getSettings() {
      return this.$profileSettingsService.getSettings()
        .then(settings => {
          const notAlowedPrp = ['ims.skype','ims.jitsi','ims.msn','ims.facebook','ims','phones.other','phones.work','phones.home','ims.other'];
          this.settings =  settings.filter((e) => e.active && !notAlowedPrp.includes(e.propertyName) ).map(obj => ({ ...obj, valueToSearch: '' })) || [];
        });
    },
    getResolvedName(item){
      const lang = eXo && eXo.env.portal.language || 'en';
      const resolvedLabel = item.labels.find(v => v.language === lang);
      if (resolvedLabel){
        return resolvedLabel.label;
      }
      return this.$t && this.$t(`profileSettings.property.name.${item.propertyName}`)!==`profileSettings.property.name.${item.propertyName}`?this.$t(`profileSettings.property.name.${item.propertyName}`):item.propertyName;
    }
  }
};

</script>
