<template>
  <v-app id="dlpQuarantine">
    <v-card class="my-4 mx-2" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title mb-0">
              <v-row no-gutters class="col-4">
                <v-col class="col-4 pb-0 pt-5">
                  <h4 class="font-weight-bold ma-0">{{ $t('documents.dlp.quarantine.label') }}</h4>
                </v-col>
                <v-col class="col-4">
                  <v-switch
                    v-if="dlpFeatureStatusLoaded"
                    v-model="dlpFeatureEnabled"
                    dense
                    @change="saveDlpFeatureStatus(dlpFeatureEnabled)"/>
                </v-col>
              </v-row>
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title font-italic">
              {{ $t('documents.dlp.quarantine.enableDisable') }}
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-divider class="mx-5"/>
      </v-list>
      <v-data-table
        :headers="headers"
        :items="documents"
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
              class="mr-1" />
            <date-format
              :value="item.detectionDate"
              :format="dateTimeFormat"
              class="mr-1" />
          </div>

        </template>
        <template slot="item.authorFullName" slot-scope="{ item }">
          <a
            :href="dlpItemOwnerLink(item.author)"
            class="text-decoration-underline"
            target="_blank">
            {{ item.authorFullName }}
          </a>
        </template>
        <template slot="item.actions" slot-scope="{ item }">
          <v-btn
            v-exo-tooltip.bottom.body="$t('documents.dlp.quarantine.previewDownload')"
            icon
            text>
            <i class="uiIconWatch"></i>
          </v-btn>
          <v-btn
            v-exo-tooltip.bottom.body="$t('documents.dlp.quarantine.validateDoc')"
            primary
            icon
            text>
            <i class="uiIconValidate"></i>
          </v-btn>
          <v-btn
            v-exo-tooltip.bottom.body="$t('documents.dlp.quarantine.deleteDoc')"
            primary
            icon
            text>
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
      documents: [],
      loading: true,
      totalSize: 0,
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
        text: this.$t && this.$t('documents.dlp.quarantine.content'),
        align: 'center',
        sortable: false,
        value: 'title',
      },
      { text: this.$t && this.$t('documents.dlp.quarantine.keywordDetected'),
        align: 'center',
        sortable: false,
        value: 'keywords'
      },
      { text: this.$t && this.$t('documents.dlp.quarantine.createdDate'),
        align: 'center',
        sortable: false,
        value: 'detectionDate'
      },
      { text: this.$t && this.$t('documents.dlp.quarantine.author'),
        align: 'center',
        sortable: false,
        value: 'authorFullName'
      },
      { text: this.$t && this.$t('documents.dlp.quarantine.actions'),
        align: 'center',
        sortable: false,
        value: 'actions'
      },];
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
      dlpAdministrationServices.saveDlpFeatureStatus(status);
    },
    getDlpFeatureStatus() {
      dlpAdministrationServices.isDlpFeatureActive().then(status => {
        this.dlpFeatureEnabled = status.value;
        this.dlpFeatureStatusLoaded = true;
      });
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
        this.documents = data.entities;
        this.totalSize = data.size;
      }).then(() =>{
        this.loading = false;
        this.$root.$emit('application-loaded');
      });
    },
    dlpItemOwnerLink(username) {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${username}`;
    },
  },
};
</script>
