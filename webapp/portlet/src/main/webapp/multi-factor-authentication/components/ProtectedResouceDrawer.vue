<template>
  <exo-drawer
    id="protectedResourceDrawer"
    ref="protectedResourceDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      {{ $t('authentication.multifactor.protected.resources.drawer') }}
    </template>
    <template slot="content">
      <v-checkbox
        v-model="isCheckAll"
        @click="selectOption"
        class="ml-3"
        :label="$t('authentication.multifactor.protected.resources.select')" />
      <hr class="mx-5">
      <div v-for="nav of navigations" :key="nav">
        <v-checkbox
          :key="nav"
          v-model="navigationsGroup"
          @change="updateCheckall"
          :value="nav"
          class="ml-3"
          :label="nav.label.startsWith('#{Space') ? nav.name : nav.label" />
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex ">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="cancel">
          {{ $t('authentication.multifactor.protected.resources.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="save">
          {{ $t('authentication.multifactor.protected.resources.button.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import {getNavigations, saveProtectedNavigations, getProtectedNavigations} from '../multiFactorServices';
export default {
  data: () => ({
    drawer: false,
    navigationsGroup: [],
    savedNavigationsGroup: [],
    isCheckAll: false,
    navigations: [],
  }),
  watch: {
    drawer() {
      if (this.drawer) {
        this.$refs.protectedResourceDrawer.open();
      } else {
        this.$refs.protectedResourceDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('protectedResource', this.protectedResource);
    this.getNavigations();
    this.getProtectedNavigations();
  },
  methods: {
    protectedResource() {
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
    },
    save() {
      this.savedNavigationsGroup = [] ;
      for (const nav of this.navigationsGroup) {
        this.savedNavigationsGroup.push(nav.name);
      }
      saveProtectedNavigations(this.savedNavigationsGroup.join(','));
      this.$root.$emit('protectedNavigationsList', this.savedNavigationsGroup);
      this.$refs.protectedResourceDrawer.close();
    },
    getProtectedNavigations() {
      getProtectedNavigations().then(data => {
        this.navigationsGroup = data;
      });
    },
    selectOption() {
      this.navigationsGroup = [];
      if (this.isCheckAll){ // Check all
        for (const key in this.navigations) {
          this.navigationsGroup.push(this.navigations[key]);
        }
      }
    },
    updateCheckall(){
      if (this.navigations.length === this.navigationsGroup.length){
        this.isCheckAll = true;
      } else {
        this.isCheckAll = false;
      }
    },
    getNavigations() {
      getNavigations().then(data => {
        for (const nav of data) {
          this.navigations.push(nav);
        }
      });
    },
  },
};
</script>
