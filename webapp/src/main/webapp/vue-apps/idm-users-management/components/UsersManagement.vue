<template>
  <v-app>
    <v-card class="application-body" flat>
      <users-management-toolbar
        :export-users-url="exportUsersUrl" />
      <users-management-list
        @list-link-updated="exportUsersUrl = $event" />
    </v-card>
    <users-management-user-form-drawer />
    <users-management-user-membership-drawer />
    <users-management-filter-drawer />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    exportUsersUrl: null,
  }),
};
</script>