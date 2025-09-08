<template>
  <v-app>
    <v-card class="application-body" flat>
      <users-management-toolbar
        :total-size="totalSize"
        :export-users-url="exportUsersUrl"
        :disabled-users="filter === 'DISABLED'" />
      <users-management-list
        @total-size-updated="totalSize = $event"
        @list-link-updated="exportUsersUrl = $event"
        @filter-updated="filter = $event" />
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
    totalSize: null,
    filter: 'ENABLED',
  }),
};
</script>