<template>
  <exo-drawer
    id="protectedResourceDrawer"
    ref="protectedResourceDrawer"
    right
    @closed="cancel">
    <template slot="title">
      {{ $t('authentication.multifactor.protected.resources.drawer') }}
    </template>
    <template slot="content">
      <v-checkbox
        v-model="isCheckAll"
        @click="selectOption"
        class="ml-3"
        :label="$t('authentication.multifactor.protected.resources.select')" />
      <hr class="mx-5 my-0">
      <div v-for="nav of filteredNavigations" :key="nav">
        <v-checkbox
          :key="nav"
          v-model="navigationsGroup"
          @change="updateCheckall"
          :value="nav.id"
          class="ml-3"
          :label="nav.label" />
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
        this.$refs.protectedResourceDrawer.startLoading();
      } else {
        this.$refs.protectedResourceDrawer.endLoading();
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
      this.getProtectedNavigations();
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
      this.navigationsGroup = [];
    },
    save() {
      this.saving = true;
      this.savedNavigationsGroup = [] ;
      for (const navId of this.navigationsGroup) {
        this.savedNavigationsGroup.push(navId);
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
        .then(() =>  this.$refs.protectedResourceDrawer.close());
    },
    getProtectedNavigations() {
      getProtectedNavigations().then(data => {
        const navs = data;
        const selectedNavs = [];
        navs.forEach(nav => {
          selectedNavs.push(nav.id);
        });
        this.navigationsGroup = selectedNavs;
        this.updateCheckall();
      });
    },
    selectOption() {
      this.navigationsGroup = [];
      if (this.isCheckAll){ // Check all
        for (const key in this.navigations) {
          this.navigationsGroup.push(this.navigations[key].id);
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
        const navs = data;
        navs.forEach(nav => {
          nav.name = nav.key.name ;
          nav.label = nav.label ;
          if (nav.key.type === 'PORTAL') {
            nav.id=`/portal/${nav.key.name}`;
          } else if (nav.key.type === 'GROUP') {
            const modifiedName = nav.key.name.replaceAll('/',':');
            nav.id=`/portal/g/${modifiedName}`;
          }
        });
        this.navigations = navs;
      });
    },
  },
};
</script>
