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
        <v-card flat class="pa-2">
          <v-radio-group
            v-model="selectedFiler"
            class="mt-0">
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.lastConnection.connected')"
              @click="resetOption(selectedFiler)"
              value="connected" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.lastConnection.neverConnected')"
              @click="resetOption(selectedFiler)"
              value="neverConnected" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.enrollment.enrolled')"
              @click="resetOption(selectedFiler)"
              value="enrolled" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.enrollment.notEnrolled')"
              @click="resetOption(selectedFiler)"
              value="notEnrolled" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.enrollment.noEnrollmentPossible')"
              @click="resetOption(selectedFiler)"
              value="noEnrollmentPossible" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.type.internal')"
              @click="resetOption(selectedFiler)"
              value="internal" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.type.external')"
              @click="resetOption(selectedFiler)"
              value="external" />
          </v-radio-group>
        </v-card>
      </v-list>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-btn
          class="ps-0"
          color="primary"
          text
          @click="resetFilter">
          <v-icon class="pe-1">mdi-reload</v-icon>
          {{ $t('UsersManagement.filter.reset') }}
        </v-btn>
        <v-spacer />
        <v-btn
          class="btn me-2"
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
    selectedFiler: null,
    previouslySelected: null,
  }),
  watch: {
    drawer() {
      if (this.drawer) {
        this.$refs.usersFilterDrawer.open();
      } else {
        this.$refs.usersFilterDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('advancedFilter', this.advancedFilter);
  },
  methods: {
    advancedFilter(selectedFiler) {
      this.selectedFiler = selectedFiler;
      this.drawer = true;
    },
    cancel() {
      this.drawer = false;
    },
    save() {
      this.$root.$emit('applyAdvancedFilter',this.selectedFiler);
      this.$refs.usersFilterDrawer.close();
    },
    resetFilter() {
      this.selectedFiler = null;
      this.previouslySelected = null;
    },
    resetOption(selectedFiler) {
      if (selectedFiler === this.previouslySelected) {
        this.selectedFiler = null;
      }
      this.previouslySelected = this.selectedFiler;
    },
  },
};
</script>
