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
        <v-row class="mx-0 px-0">
          <div class="pa-0 d-inline-flex"><v-card-text class="pr-0 text-sub-title">{{ $t('UsersManagement.lastConnexion') }}</v-card-text></div>
          <v-col class="align-self-center mx-2"><v-divider></v-divider></v-col>
        </v-row>
        <v-card flat class="px-4">
          <v-radio-group
            v-model="connectionStatus"
            class="mt-0"
          >
            <v-radio
              :label="$t('UsersManagement.lastConnexion.connected')"
              value="neverConnected"
            ></v-radio>
            <v-radio
              :label="$t('UsersManagement.lastConnexion.neverConnected')"
              value="connected"
            ></v-radio>
          </v-radio-group>
        </v-card>
        <v-row class="col-12 mx-0 px-0">
          <div class="pa-0 d-inline-flex"><v-card-text class="pr-0 text-sub-title">{{ $t('UsersManagement.enrollment') }}</v-card-text></div>
          <v-col class="align-self-center mx-2"><v-divider></v-divider></v-col>
        </v-row>
        <v-card flat class="px-4">
          <v-checkbox
            v-model="enrollmentStatus"
            :label="$t('UsersManagement.enrollment.enrolled')"
            class="mt-0"
            value="enrolled"
            hide-details
          ></v-checkbox>
          <v-checkbox
            v-model="enrollmentStatus"
            :label="$t('UsersManagement.enrollment.notEnrolled')"
            class="mt-0"
            value="notEnrolled"
            hide-details
          ></v-checkbox>
          <v-checkbox
            v-model="enrollmentStatus"
            :label="$t('UsersManagement.enrollment.noEnrollmentPossible')"
            class="mt-0"
            value="noEnrollmentPossible"
            hide-details
          ></v-checkbox>
        </v-card>
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
    connectionStatus: null,
    userType: null,
    enrollmentStatus: []
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
    advancedFilter(connectionStatus, userType, enrollmentStatus) {   
      this.connectionStatus = connectionStatus;
      this.userType = userType;
      this.enrollmentStatus = enrollmentStatus;
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
    },
    save() {
      this.$root.$emit('applyAdvancedFilter', this.connectionStatus, this.userType, this.enrollmentStatus);
      this.$refs.usersFilterDrawer.close();
    },
    resetFilter() {
      this.connectionStatus = null;
      this.userType = null;
      this.enrollmentStatus = [];
    }
  },
};
</script>