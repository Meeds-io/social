<template>
  <div>
    <v-card v-if="keywordsEdit" flat class="py-0">
      <v-card-text class="pb-0">
        <extended-textarea
          v-model="keywords"
          :max-length="keywordsTextLength"
          :placeholder="$t('documents.dlp.editKeyword.placeholder')"
          class="py-0"
          type="text"
        />
      </v-card-text>
      <v-card-actions class="px-4">
        <v-spacer />
        <v-btn
          class="btn mr-2"
          @click="keywordsEdit = false;getDlpKeywords()">
          {{ $t('documents.dlp.editKeyword.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="saveDlpKeywords">
          {{ $t('documents.dlp.editKeyword.save') }}
        </v-btn>
      </v-card-actions>
    </v-card>
    <v-card-text v-else>
      <div>
        <p @mouseover="showEditBtn = true"
           @mouseleave="showEditBtn = false">
          {{ keywords }} <v-btn v-if="showEditBtn" dark width="20" height="20" icon @click="keywordsEdit = true"><v-icon size="20" color="primary">mdi-pencil</v-icon></v-btn>
        </p>
      </div>
    </v-card-text>
  </div>
</template>

<script>
import {getDlpKeywords} from '../dlpAdministrationServices';
export default {
        
  data () {
    return {
      keywordsEdit: false,
      keywords :'',
      keywordsTextLength : 1000,
      showEditBtn : false, 
      saving : null
    };
  },
  created() {
    this.getDlpKeywords();
  },
  methods : {
    getDlpKeywords() {
      getDlpKeywords().then(keywords => {
        this.keywords = keywords;
        this.keywordsEdit = keywords === '';
      });
    },
    saveDlpKeywords() {
      if (this.keywords.trim()) {
        this.$settingService.setSettingValue('GLOBAL', 'DLP', 'GLOBAL', 'DlpKeywords', 'exo:dlpKeywords', this.keywords);
        this.keywordsEdit = false;
      } else {
        this.$settingService.setSettingValue('GLOBAL', 'DLP', 'GLOBAL', 'DlpKeywords', 'exo:dlpKeywords', '');
        this.keywords ='';
        this.keywordsEdit = true;
      }
    }
  }
};
</script>