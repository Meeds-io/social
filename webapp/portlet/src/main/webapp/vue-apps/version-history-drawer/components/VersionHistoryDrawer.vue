<template>
  <exo-drawer
    @closed="closed"
    ref="versionHistoryDrawer"
    class="versionHistoryDrawer"
    show-overlay
    right>
    <template slot="title">
      {{ $t('versionHistory.label.title') }}
    </template>
    <template slot="content">
      <div
        v-if="isLoading"
        class="text-center mt-5">
        <v-progress-circular
          indeterminate
          color="primary" />
      </div>
      <div
        v-if="!isLoading && versions.length === 0"
        class="text-center mt-5">
        <p class="grey--text darken-1">
          {{ $t('versionHistory.label.empty') }}
        </p>
      </div>
      <v-list
        class="ma-3">
        <v-list-item-group
          active-class="bg-active">
          <v-slide-y-transition group>
            <v-list-item
              v-for="version in versions"
              :key="version.id"
              :class="[version.current? 'current_version' : '']"
              @click="openVersion($event, version)"
              class="history-line pa-2 mb-2 border-color border-radius d-block">
              <version-card
                @version-update-description="updateVersionDescription"
                @restore-version="restoreVersion"
                :version="version"
                :can-manage="canManage"
                :disable-restore-version="disableRestoreVersion"
                :enable-edit-description="enableEditDescription" />
            </v-list-item>
          </v-slide-y-transition>
        </v-list-item-group>
      </v-list>
    </template>
    <template slot="footer">
      <div
        v-if="showLoadMore"
        class="d-flex mx-4">
        <v-btn
          @click="loadMore"
          class="primary--text mx-auto"
          text>
          {{ $t('versionHistory.button.loadMore') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  props: {
    versions: {
      type: Array,
      default: () => {
        return [];
      }
    },
    canManage: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    showLoadMore: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    isLoading: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    enableEditDescription: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    disableRestoreVersion: {
      type: Boolean,
      default: () => {
        return false;
      }
    }
  },
  methods: {
    open() {
      this.$refs.versionHistoryDrawer.open();
    },
    closed() {
      this.$emit('drawer-closed');
    },
    loadMore() {
      this.$emit('load-more');
    },
    openVersion(event, version) {
      if (event.target.tagName !== 'INPUT' && event.target.tagName !== 'BUTTON'
          && !event.target.classList.contains('descriptionContent')) {
        this.$emit('open-version', version);
      }
    },
    updateVersionDescription(version, newDescription) {
      this.$emit('version-update-description', version, newDescription);
    },
    restoreVersion(version) {
      this.$emit('restore-version', version);
    }
  }
};
</script>