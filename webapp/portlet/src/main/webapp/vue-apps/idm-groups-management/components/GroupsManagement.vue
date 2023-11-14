<template>
  <v-app>
    <v-card class="d-flex flex py-2 card-border-radius" flat>
      <v-flex class="sm12 md4" flat>
        <groups-management-tree-toolbar />
        <groups-management-tree />
      </v-flex>
      <v-divider vertical />
      <v-flex class="sm12 md8" flat>
        <groups-management-membership-toolbar />
        <groups-management-membership-list />
      </v-flex>
    </v-card>
    <groups-management-form-drawer />
    <groups-management-membership-form-drawer />
  </v-app>
</template>
