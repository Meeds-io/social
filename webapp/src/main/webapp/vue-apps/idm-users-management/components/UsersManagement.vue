<template>
  <v-app>
    <v-card class="application-body" flat>
      <users-management-toolbar />
      <users-management-list />
    </v-card>
    <users-management-user-form-drawer />
    <users-management-user-membership-drawer />
    <users-management-filter-drawer />
  </v-app>
</template>