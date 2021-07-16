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
      <div v-for="nav of filteredNavigations" :key="nav">
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
          :disabled="saving"
          @click="cancel">
          {{ $t('authentication.multifactor.protected.resources.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          :disabled="saving"
          @click="save">
          {{ $t('authentication.multifactor.protected.resources.button.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
import {getNavigations, getProtectedNavigations} from '../multiFactorServices';
export default {
  data: () => ({
    drawer: false,
    saving: false,
    navigationsGroup: [],
    savedNavigationsGroup: [],
    filteredNavigationsGroup: [],
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
    saving() {
      if (this.saving) {
        this.$refs.userFormDrawer.startLoading();
      } else {
        this.$refs.userFormDrawer.endLoading();
      }
    },
  },
  created() {
    this.$root.$on('protectedResource', this.protectedResource);
    this.getNavigations();
    this.getProtectedNavigations();
  },
  computed: {
    filteredNavigations() {
      return [...new Set(this.filteredNavigationsGroup)];
    }
  },
  methods: {
    protectedResource() {
      this.checkNavigations();
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
      this.navigationsGroup = [];
    },
    save() {
      this.saving = true;
      this.savedNavigationsGroup = [] ;
      for (const nav of this.navigationsGroup) {
        this.savedNavigationsGroup.push(nav.name);
      }
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/saveProtectedNavigations `, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: this.savedNavigationsGroup.join(','),
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp.status === 400) {
            return resp.text().then(error => {
              this.fieldError = error;
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('authentication.multifactor.protected.error.UnknownServerError'));
          }
        }
      }).then(() => this.$root.$emit('protectedNavigationsList'))
        .then(() =>   this.saving = false)
        .then(() =>   this.navigationsGroup = [])
        .then(() =>  this.$refs.protectedResourceDrawer.close());
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
    checkNavigations() {
      this.navigations.filter(nav => {
        if (this.navigationsGroup.some(e=> e.name !== nav.name))
        {this.filteredNavigationsGroup.push(nav);}}
      );
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
        const navs = data;
        navs.forEach(nav => {
          nav.name = nav.name ;
          nav.id = nav.id ;
          nav.label = nav.label ;
        });
        this.navigations = navs;
      });
    },
  },
};
</script>
