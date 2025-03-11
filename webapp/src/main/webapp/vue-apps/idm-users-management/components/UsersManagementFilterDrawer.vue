<template>
  <exo-drawer
    id="usersFilterDrawer"
    ref="usersFilterDrawer" 
    right
    @closed="drawer = false">
    <template #title>
      {{ $t('UsersManagement.filter') }}
    </template>
    <template #content>
      <v-list>
        <v-card
          class="pa-2"
          flat>
          <v-radio-group
            v-model="selectedFiler"
            class="mt-0">
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.lastConnection.connected')"
              value="connected"
              @click="resetOption(selectedFiler)" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.lastConnection.neverConnected')"
              value="neverConnected"
              @click="resetOption(selectedFiler)" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.enrollment.enrolled')"
              value="enrolled"
              @click="resetOption(selectedFiler)" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.enrollment.notEnrolled')"
              value="notEnrolled"
              @click="resetOption(selectedFiler)" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.enrollment.noEnrollmentPossible')"
              value="noEnrollmentPossible"
              @click="resetOption(selectedFiler)" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.type.internal')"
              value="internal"
              @click="resetOption(selectedFiler)" />
            <v-radio
              class="pa-2"
              :label="$t('UsersManagement.type.external')"
              value="external"
              @click="resetOption(selectedFiler)" />
          </v-radio-group>
        </v-card>
      </v-list>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-btn
          class="ps-0"
          color="primary"
          text
          @click="resetFilter">
          <v-icon class="pe-1">
            mdi-reload
          </v-icon>
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
      drawer () {
        if (this.drawer) {
          this.$refs.usersFilterDrawer.open();
        } else {
          this.$refs.usersFilterDrawer.close();
        }
      },
    },
    created () {
      this.$root.$on('advancedFilter', this.advancedFilter);
    },
    methods: {
      advancedFilter (selectedFiler) {
        this.selectedFiler = selectedFiler;
        this.drawer = true;
      },
      cancel () {
        this.drawer = false;
      },
      save () {
        this.$root.$emit('applyAdvancedFilter',this.selectedFiler);
        this.$refs.usersFilterDrawer.close();
      },
      resetFilter () {
        this.selectedFiler = null;
        this.previouslySelected = null;
      },
      resetOption (selectedFiler) {
        if (selectedFiler === this.previouslySelected) {
          this.selectedFiler = null;
        }
        this.previouslySelected = this.selectedFiler;
      },
    },
  };
</script>
