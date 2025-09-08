<template>
  <exo-drawer
    id="usersFilterDrawer"
    ref="drawer"
    v-model="drawer"
    :right="!$vuetify.rtl">
    <template #title>{{ $t('UsersManagement.filter') }}</template>
    <template #content>
      <div flat class="pa-5">
        <div class="text-header mb-2">{{ $t('UsersManagement.filter.profileStatus') }}</div>
        <v-radio-group
          v-model="status"
          class="mt-0">
          <v-radio
            :label="$t('UsersManagement.status.enabled')"
            value="ENABLED"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.status.disabled')"
            value="DISABLED"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
        </v-radio-group>
        <div class="text-header mt-4 mb-2">{{ $t('UsersManagement.filter.profileType') }}</div>
        <v-radio-group
          v-model="type"
          class="mt-0">
          <v-radio
            :label="$t('UsersManagement.filter.all')"
            value="ALL"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.type.internal')"
            value="internal"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.type.external')"
            value="external"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
        </v-radio-group>
        <div class="text-header mt-4 mb-2">{{ $t('UsersManagement.filter.connectionStatus') }}</div>
        <v-radio-group
          v-model="connectionStatus"
          class="mt-0">
          <v-radio
            :label="$t('UsersManagement.filter.all')"
            value="ALL"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.lastConnection.connected')"
            value="connected"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.lastConnection.neverConnected')"
            value="neverConnected"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
        </v-radio-group>
        <div class="text-header mt-4 mb-2">{{ $t('UsersManagement.filter.enrollmentStatus') }}</div>
        <v-radio-group
          v-model="enrollmentStatus"
          class="mt-0">
          <v-radio
            :label="$t('UsersManagement.filter.all')"
            value="ALL"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.enrollment.enrolled')"
            value="enrolled"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.enrollment.notEnrolled')"
            value="notEnrolled"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
          <v-radio
            :label="$t('UsersManagement.enrollment.noEnrollmentPossible')"
            value="noEnrollmentPossible"
            on-icon="fa-lg far fa-dot-circle"
            off-icon="fa-lg far fa-circle" />
        </v-radio-group>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('UsersManagement.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="apply">
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
    status: 'ENABLED',
    type: 'ALL',
    connectionStatus: 'ALL',
    enrollmentStatus: 'ALL',
  }),
  computed: {
    filter() {
      return {
        status: this.status, // Enabled / Disabled
        type: this.type === 'ALL' ? null : this.type, // Internals / Externals
        connectionStatus: this.connectionStatus === 'ALL' ? null : this.connectionStatus, // Connected / Never Connected
        enrollmentStatus: this.enrollmentStatus === 'ALL' ? null : this.enrollmentStatus, // Email sent / Not yet invited / No enrollment possible
      };
    },
  },
  created() {
    this.$root.$on('advancedFilter', this.open);
  },
  methods: {
    open(filter) {
      this.filter = filter;
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    apply() {
      this.$root.$emit('applyAdvancedFilter',this.filter);
      this.close();
    },
  },
};
</script>
