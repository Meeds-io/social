<template>
  <v-app id="dlpQuarantine">
    <v-card class="my-4 mx-2" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title mb-0">
              <v-row no-gutters class="col-4">
                <v-col class="col-3 pb-0 pt-5">
                  <h4 class="font-weight-bold ma-0">{{ $t('items.dlp.quarantine.label') }}</h4>
                </v-col>
                <v-col class="col-1">
                  <v-switch
                    v-if="dlpFeatureStatusLoaded"
                    v-model="dlpFeatureEnabled"
                    dense
                    @change="saveDlpFeatureStatus(dlpFeatureEnabled)" />
                </v-col>
              </v-row>
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title font-italic">
              {{ $t('items.dlp.quarantine.enableDisable') }}
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <dlp-keywords-editor />
        <v-divider class="mx-5" />
      </v-list>
      <exo-confirm-dialog
        ref="restoreConfirmDialog"
        :message="restoreConfirmMessage"
        :title="$t('items.dlp.title.confirmRestore')"
        :ok-label="$t('items.dlp.button.ok')"
        :cancel-label="$t('items.dlp.button.cancel')"
        @ok="restoreDlpPositiveItemConfirm()" />
      <exo-confirm-dialog
        ref="deleteConfirmDialog"
        :message="deleteConfirmMessage"
        :title="$t('items.dlp.title.confirmDelete')"
        :ok-label="$t('items.dlp.button.ok')"
        :cancel-label="$t('items.dlp.button.cancel')"
        @ok="deleteDlpPositiveItemConfirm()" />
      <v-data-table
        :headers="headers"
        :items="items"
        :loading="loading"
        :options.sync="options"
        :server-items-length="totalSize"
        :footer-props="{ itemsPerPageOptions }"
        class="px-5 data-table-light-border">
        <template slot="item.detectionDate" slot-scope="{ item }">
          <div class="d-flex justify-center">
            <date-format
              :value="item.detectionDate"
              :format="fullDateFormat"
              class="me-1" />
            <date-format
              :value="item.detectionDate"
              :format="dateTimeFormat"
              class="me-1" />
          </div>
        </template>
        <template slot="item.authorDisplayName" slot-scope="{ item }">
          <a
            :href="dlpItemOwnerLink(item.author)"
            class="text-decoration-underline"
            target="_blank">
            {{ item.authorDisplayName }}
          </a>
          <span v-if="item.isExternal" class="externalTagClass">{{ externalTag }}</span>
        </template>
        <template slot="item.actions" slot-scope="{ item }">
          <v-btn
            v-exo-tooltip.bottom.body="$t('items.dlp.quarantine.previewDownload')"
            :href="item.itemUrl"
            target="_blank"
            icon
            text>
            <i class="uiIconWatch"></i>
          </v-btn>
          <v-btn
            v-exo-tooltip.bottom.body="$t('items.dlp.quarantine.validate')"
            primary
            icon
            text
            @click.prevent="restoreDlpPositiveItem(item.id)">
            <i class="uiIconValidate"></i>
          </v-btn>
          <v-btn
            v-exo-tooltip.bottom.body="$t('items.dlp.quarantine.delete')"
            primary
            icon
            text
            @click="deleteDlpPositiveItem(item.id)">
            <i class="uiIconTrash"></i>
          </v-btn>
        </template>
      </v-data-table>
    </v-card>
  </v-app>
</template>

<script>
import * as dlpAdministrationServices from '../dlpAdministrationServices';
export default {
  data () {
    return {
      items: [],
      loading: true,
      featureName: 'dlp',
      restoreConfirmMessage: null,
      totalSize: 0,
      selectedDeleteItem: null,
      selectedRestoreItem: null,
      deleteConfirmMessage: null,
      itemsPerPageOptions: [20, 50, 100],
      options: {
        page: 1,
        itemsPerPage: 20,
      },
      dlpFeatureEnabled: null,
      dlpFeatureStatusLoaded: false,
      fullDateFormat: {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      },
      dateTimeFormat: {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }
    };
  },
  computed: {
    headers() {
      return [        {
        text: this.$t && this.$t('items.dlp.quarantine.content'),
        align: 'center',
        sortable: false,
        value: 'title',
      },
      { text: this.$t && this.$t('items.dlp.quarantine.keywordDetected'),
        align: 'center',
        sortable: false,
        value: 'keywords'
      },
      { text: this.$t && this.$t('items.dlp.quarantine.createdDate'),
        align: 'center',
        sortable: false,
        value: 'detectionDate'
      },
      { text: this.$t && this.$t('items.dlp.quarantine.author'),
        align: 'center',
        sortable: false,
        value: 'authorDisplayName'
      },
      { text: this.$t && this.$t('items.dlp.quarantine.actions'),
        align: 'center',
        sortable: false,
        value: 'actions'
      },];
    },
    externalTag() {
      return `(${this.$t('items.dlp.external.label')})`;
    },
  },
  watch: {
    options() {
      this.retrieveDlpPositiveItems();
    },
  },
  created() {
    this.getDlpFeatureStatus();
    this.retrieveDlpPositiveItems();
  },
  methods: {
    saveDlpFeatureStatus(status) {
      dlpAdministrationServices.changeFeatureActivation(status);
    },
    deleteDlpPositiveItemConfirm() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/item/${this.selectedDeleteItem}`, {
        method: 'DELETE',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp && resp.status === 400) {
            return resp.text().then(error => {
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('items.dlp.error.UnknownServerError'));
          }
        }
        return this.retrieveDlpPositiveItems();
      }).catch(error => {
        this.error = error.message || String(error);
        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }).finally(() => this.loading = false);
    },
    deleteDlpPositiveItem(itemId) {
      this.deleteConfirmMessage = this.$t('items.dlp.message.confirmDelete');
      this.$refs.deleteConfirmDialog.open();
      this.selectedDeleteItem = itemId;
    },
    getDlpFeatureStatus() {
      this.$featureService.isFeatureEnabled(this.featureName).then(status => {
        this.dlpFeatureEnabled = status;
        this.dlpFeatureStatusLoaded = true;
      });
    },
    restoreDlpPositiveItemConfirm() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/item/restore/${this.selectedRestoreItem}`, {
        method: 'PUT',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp && resp.status === 400) {
            return resp.text().then(error => {
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('items.dlp.error.UnknownServerError'));
          }
        }
        return this.retrieveDlpPositiveItems();
      }).catch(error => {
        this.error = error.message || String(error);
        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }).finally(() => this.loading = false);
    },
    restoreDlpPositiveItem(itemId) {
      this.restoreConfirmMessage = this.$t('items.dlp.message.confirmRestore');
      this.$refs.restoreConfirmDialog.open();
      this.selectedRestoreItem = itemId;
    },
    retrieveDlpPositiveItems()  {
      const page = this.options && this.options.page;
      let itemsPerPage = this.options && this.options.itemsPerPage;
      if (itemsPerPage <= 0) {
        itemsPerPage = this.totalSize || 20;
      }
      const offset = (page - 1) * itemsPerPage;
      this.loading = true;
      dlpAdministrationServices.getDlpPositiveItems(offset , itemsPerPage).then(data => {
        this.items = data.entities;
        this.totalSize = data.size;
      }).then(() =>{
        this.loading = false;
        this.$root.$emit('application-loaded');
      });
    },
    dlpItemOwnerLink(username) {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${username}`;
    }
  },
};
</script>
