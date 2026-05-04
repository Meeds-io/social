<template>
  <v-app>
    <main class="application-body position-static">
      <groups-management-toolbar
        ref="toolbar"
        class="application-layout-style" />
      <v-card
        class="d-flex flex-column flex-md-row mt-5 transparent"
        min-width="100%"
        flat>
        <v-card
          class="application-layout-style me-0 me-md-5 mb-5 mb-md-0 flex-shrink-0 flex-grow-0"
          :width="$root.isMobile ? '100%' : '300px !important'"
          :max-width="$root.isMobile ? '100%' : '300px !important'"
          flat>
          <groups-management-tree-toolbar />
          <groups-management-tree />
        </v-card>
        <div
          ref="main"
          class="application-layout-style flex-shrink-1 flex-grow-1 overflow-hidden">
          <groups-management-membership-toolbar />
          <groups-management-membership-list />
        </div>
      </v-card>
    </main>
    <groups-management-form-drawer />
    <groups-management-membership-form-drawer />
  </v-app>
</template>

