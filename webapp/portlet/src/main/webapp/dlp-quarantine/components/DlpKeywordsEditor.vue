<template>
  <v-app>
    <v-card-text>
      <div>
        <v-chip-group
          active-class="primary--text"
          column
        >
          <v-row no-gutters>
            <div class="my-auto col-1">
              <h4 class="font-weight-bold ma-0">{{ $t('items.dlp.editKeyword.keywords') }}</h4>
            </div>
            <v-chip
              v-for="keyword in keywords"
              :key="keyword"
              outlined
              class="my-1"
            >
              {{ keyword }}
            </v-chip>
            <div class="my-auto">
              <v-btn
                icon
                outlined
                small
                @click="editKeywords">
                <i v-if="keywords.length" class="uiIconEdit uiIconLightBlue pb-2" />
                <i v-else class="uiIconSocSimplePlus uiIconLightBlue" />
              </v-btn>
            </div>
          </v-row>
        </v-chip-group>
      </div>
    </v-card-text>
    <exo-drawer
      ref="keywordsDrawer"
      class="keywordsDrawer"
      right
      @closed="CleanInput">
      <template slot="title">
        {{ $t('items.dlp.editKeyword.title') }}
      </template>
      <template slot="content">
        <v-card flat>
          <v-card-text class="pb-0">
            <input
              ref="InputKeyword"
              :placeholder="$t('items.dlp.editKeyword.placeholder')"
              class="ignore-vuetify-classes inputKeyword"
              maxlength="50"
              @keyup.enter="AddKeyword"
            />
          </v-card-text>
          <v-card-text>
            <v-chip-group
              active-class="primary--text"
              column
            >
              <v-chip
                v-for="keyword in modifyingKeywords"
                :key="keyword"
                close
                outlined
                class="my-1"
                @click:close="removeKeyword(keyword)"
              >
                {{ keyword }}
              </v-chip>
            </v-chip-group>
          </v-card-text>
          <v-card-text v-if="error">
            <v-alert type="error">
              {{ error }} 
            </v-alert>
          </v-card-text>
        </v-card>
      </template>
      <template slot="footer">
        <div class="d-flex">
          <v-spacer />
          <v-btn
            class="btn me-2"
            @click="cancel">
            {{ $t('items.dlp.button.cancel') }}
          </v-btn>
          <v-btn
            class="btn btn-primary"
            @click="saveDlpKeywords">
            {{ $t('items.dlp.button.save') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
import {getDlpKeywords, setDlpKeywords} from '../dlpAdministrationServices';
export default {
        
  data () {
    return {
      keywordsEdit: false,
      keywords: [],
      modifyingKeywords : [],
      keywordTextLength : 100,
      showEditBtn : false, 
      error : null,
    };
  },
  watch: {
    modifyingKeywords() {
      this.error = null;
    },
  },
  created() {
    this.getDlpKeywords();
  },
  methods : {
    getDlpKeywords() {
      getDlpKeywords().then(keywords => {
        if (keywords) {
          this.keywords = keywords.split(',');
          this.modifyingKeywords = keywords.split(',');
        }
      });
    },
    AddKeyword() {
      const input = this.$refs.InputKeyword.value;
      if (input !== '') {
        const index = this.modifyingKeywords.indexOf(input);
        const words = input.trim().split(/\s+/);
        const num_words = words.length;
        if(num_words>3){
          this.error = this.$t('items.dlp.editKeyword.maxWordsAllowed');
          window.setTimeout(() => {
            this.error = null;
          }, 5000);
        } else if (index > -1) {
          this.error = this.$t('items.dlp.editKeyword.alreadyAdded');
          window.setTimeout(() => {
            this.error = null;
          }, 5000);
        } else {
          this.modifyingKeywords.push(this.$refs.InputKeyword.value.trim());
          this.$refs.InputKeyword.value = '';
        }
      }
    },
    removeKeyword(keyword) {
      const index = this.modifyingKeywords.indexOf(keyword);
      if (index > -1) {
        this.modifyingKeywords.splice(index, 1);
      }
    },
    editKeywords() {
      this.$refs.keywordsDrawer.open();
      this.getDlpKeywords();
    },
    saveDlpKeywords() {
      setDlpKeywords(this.modifyingKeywords.join());
      this.$refs.keywordsDrawer.close();
      this.keywords = this.modifyingKeywords;
    },
    cancel() {
      this.$refs.keywordsDrawer.close();
    },
    CleanInput() {
      this.$refs.InputKeyword.value = '';
    }
  }
};
</script>
