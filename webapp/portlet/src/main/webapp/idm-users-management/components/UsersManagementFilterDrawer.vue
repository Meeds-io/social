<template>
  <exo-drawer
    id="usersFilterDrawer"
    ref="usersFilterDrawer" 
    right
    @closed="drawer = false">
    <template slot="title">
      {{ $t('UsersManagement.filter') }}
    </template>
    <template slot="content">
      <v-list>
        <v-row no-gutters class="col-12 mx-0 px-0">
          <div class="pa-0 d-inline-flex"><v-card-text class="pr-0 text-sub-title">{{ $t('UsersManagement.type') }}</v-card-text></div>
          <v-col class="align-self-center mx-2"><v-divider></v-divider></v-col>
        </v-row>
        <v-card flat class="px-4">
          <v-radio-group
            v-model="userType"
            class="mt-0"
          >
            <v-radio
              :label="$t('UsersManagement.type.internal')"
              value="internal"
            ></v-radio>
            <v-radio
              :label="$t('UsersManagement.type.external')"
              value="external"
            ></v-radio>
          </v-radio-group>
        </v-card>
      </v-list>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-btn
          class="pl-0"
          color="primary"
          text
          @click="resetFilter">
          <v-icon class="pr-1">mdi-reload</v-icon>
          {{ $t('UsersManagement.filter.reset') }}
        </v-btn>
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="cancel">
          {{ $t('UsersManagement.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="save">
          {{ $t('UsersManagement.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    userType: null,
  }),
  watch: {
    drawer() {
      if (this.drawer) {
        this.$refs.usersFilterDrawer.open();
      } else {
        this.$refs.usersFilterDrawer.close();
      }
    }
  },
  created() {
    this.$root.$on('advancedFilter', this.advancedFilter);
  },
  methods: {
    advancedFilter(userType) {   
      this.userType = userType;
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
    },
    save() {
      this.$root.$emit('applyAdvancedFilter', this.userType);
      this.$refs.usersFilterDrawer.close();
    },
    resetFilter() {
      this.userType = null;
    }
  },
};
</script>