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
                v-for="(version,index) in versions"
                :key="index"
                :class="[version.current? 'current_version' : '']"
                class="history-line pa-2 mb-2 border-color border-radius d-block"
                @click="$emit('open-version', version)">
              <div class="author-date-wrapper d-flex justify-space-between ">
                <div class="version-author">
                  <span class="node-version border-radius primary px-2 font-weight-bold me-2 clickable">V{{ version.versionNumber }}</span>
                  <span class="font-weight-bold text-truncate">{{ version.authorFullName }}</span>
                </div>
                <div class="version-update-date">
                  <date-format
                      class="text-light-color text-truncate caption"
                      :value="versionDate(version)"
                      :format="dateTimeFormat" />
                </div>
              </div>
              <div class="description-restore-wrapper d-flex justify-space-between pt-2">
                <div class="node-version-description"></div>
                <div v-if="index > 0 && canManage" class="node-version-restore">
                  <v-tooltip bottom>
                    <template #activator="{ on, attrs }">
                      <v-icon
                          v-bind="attrs"
                          v-on="on"
                          size="22"
                          class="primary--text clickable pa-0"
                          @click="$emit('restore-version', version)">
                        mdi-restart
                      </v-icon>
                    </template>
                    <span class="caption">{{ $t('versionHistory.label.restore') }}</span>
                  </v-tooltip>
                </div>
              </div>
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
  data: () => ({
    dateTimeFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    },
    pageSize: 0,
  }),
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
    }
  },
  methods: {
    open() {
      this.$refs.versionHistoryDrawer.open();
    },
    closed() {
      this.$emit('drawer-closed');
    },
    versionDate(version) {
      return version && version.updatedDate && version.updatedDate.time || version.createdDate.time;
    },
    loadMore() {
      this.$emit('load-more');
    }
  }
};
</script>