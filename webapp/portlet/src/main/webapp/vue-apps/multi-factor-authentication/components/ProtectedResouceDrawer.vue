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
        v-model="checkbox"
        class="ml-3"
        :label="$t('authentication.multifactor.protected.resources.select')" />
      <hr class="mx-5">
      <v-checkbox
        v-model="checkbox"
        class="ml-3"
        :label="$t('authentication.multifactor.protected.resources.manage.users')" />
      <v-checkbox
        v-model="checkbox"
        class="ml-3"
        :label="$t('authentication.multifactor.protected.resources.manage.groups')" />
      <v-checkbox
        v-model="checkbox"
        class="ml-3"
        :label="$t('authentication.multifactor.protected.resources.Memberships')" />
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
export default {
  data: () => ({
    drawer: false,
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
  },
  methods: {
    protectedResource() {
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
    },
    save() {
      this.$refs.protectedResourceDrawer.close();
    },
  },
};
</script>
