<template>
  <exo-drawer
    ref="drawer"
    :confirm-close="modified"
    :confirm-close-labels="confirmCloseLabels"
    class="linkAdvancedSettingDrawer"
    go-back-button
    right
    @closed="reset">
    <template slot="title">
      {{ $t('links.label.displaySettings') }}
    </template>
    <template v-if="settings" #content>
      <v-form
        ref="form"
        autocomplete="off"
        class="pa-4">
        <div class="d-flex flex-column">
          <div class="d-flex align-center mb-2 flex-grow-1 flex-shrink-1 text-truncate text-color">
            {{ $t('links.label.preview') }}
          </div>
  
          <links-display-preview
            :settings="settings"
            :links="links"
            class="mb-4" />
  
          <div class="mb-2">
            <div class="d-flex mb-2">
              <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                {{ $t('links.label.addHeader') }}
              </div>
              <v-switch
                v-model="showHeader"
                class="my-0 me-n2"
                dense
                hide-details />
            </div>
            <div v-if="showHeader && settings?.header" class="d-flex mb-2">
              <translation-text-field
                ref="linksHeader"
                id="linksHeader"
                v-model="settings.header"
                :default-language="$root.defaultLanguage"
                :rules="rules.header"
                :placeholder="$t('links.label.headerPlaceHolder')"
                :maxlength="maxHeaderLength"
                drawer-title="links.label.headerTranslation"
                class="width-auto flex-grow-1"
                no-expand-icon
                autofocus
                back-icon
                required />
            </div>
          </div>
          
          <div class="d-flex mb-4">
            <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
              {{ $t('links.label.largeIcons') }}
            </div>
            <v-switch
              v-model="settings.largeIcon"
              class="my-0 me-n2"
              dense
              hide-details />
          </div>

          <div class="d-flex mb-4">
            <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
              {{ $t('links.label.showName') }}
            </div>
            <v-switch
              v-model="settings.showName"
              class="my-0 me-n2"
              dense
              hide-details />
          </div>

          <div class="mb-2">
            <div class="d-flex mb-2">
              <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
                {{ $t('links.label.addSeeMore') }}
              </div>
              <v-switch
                v-model="seeMore"
                class="my-0 me-n2"
                dense
                hide-details />
            </div>
            <div v-if="seeMore" class="mb-2">
              <v-text-field
                id="seeMoreInput"
                name="seeMoreInput"
                ref="seeMoreInput"
                v-model="settings.seeMore"
                :placeholder="$t('links.label.enterUrl')"
                :rules="rules.seeMore"
                class="border-box-sizing width-auto pt-0"
                type="text"
                outlined
                dense />
            </div>
          </div>
        </div>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex align-center justify-end">
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('links.label.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabled"
          class="btn primary"
          @click="apply">
          {{ $t('links.label.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    links: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    settings: null,
    originalSettings: null,
    showHeader: false,
    seeMore: false,
    valid: false,
    maxHeaderLength: 25,
    settingsHeader: null,
  }),
  computed: {
    disabled() {
      return !this.valid || !this.modified || (this.showHeader && !this.settings?.header?.[this.$root.defaultLanguage]?.length);
    },
    modified() {
      return this.originalSettings && JSON.stringify(this.originalSettings) !== JSON.stringify(this.settings || {});
    },
    confirmCloseLabels() {
      return {
        title: this.$t('links.title.confirmCloseModification'),
        message: this.$t('links.message.confirmCloseModification'),
        ok: this.$t('confirm.yes'),
        cancel: this.$t('confirm.no'),
      };
    },
    displayTypes() {
      return [{
        value: 'ROW',
        label: this.$t('links.label.configureDisplay.row'),
      }, {
        value: 'COLUMN',
        label: this.$t('links.label.configureDisplay.column'),
      }, {
        value: 'CARD',
        label: this.$t('links.label.configureDisplay.cards'),
      }];
    },
    rules() {
      return {
        header: [
          v => !!v?.length || ' ',
          v => (v && v.length < this.maxHeaderLength) || this.$t('links.input.exceedsMaxLength', {
            0: this.maxHeaderLength,
          }),
        ],
        seeMore: [
          v => !!v?.length || ' ',
          v => {
            try {
              if (v.indexOf('/') === 0) {
                v = `${window.location.origin}${v}`;
              }
              return !!Autolinker.parse(v).length || this.$t('links.input.invalidLink');
            } catch (e) {
              return this.$t('links.input.invalidLink');
            }
          },
        ],
      };
    },
  },
  watch: {
    showHeader() {
      if (this.showHeader) {
        if (!this.settings.header?.[this.$root.defaultLanguage]) {
          this.settings.header = this.settingsHeader || {};
          if (!this.settingsHeader) {
            this.settings.header[this.$root.defaultLanguage] = '';
          }
          this.settingsHeader = null;
        }
      } else {
        this.settingsHeader = this.settings.header;
        this.settings.header = null;
      }
      this.refreshValidation();
    },
    seeMore() {
      if (!this.seeMore) {
        this.settings.seeMore = null;
      }
      this.refreshValidation();
    },
    settings: {
      deep: true,
      handler() {
        this.refreshValidation();
      },
    },
  },
  created() {
    this.$root.$on('links-advanced-settings-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('links-advanced-settings-drawer', this.open);
  },
  methods: {
    open() {
      this.settings = this.value && JSON.parse(JSON.stringify(this.value)) || {};
      this.showHeader = !!this.settings?.header?.en?.length;
      this.seeMore = !!this.settings?.seeMore?.length;
      if (!this.showHeader) {
        this.settings.header = null;
      }
      this.valid = false;
      this.$nextTick().then(() => {
        this.originalSettings = JSON.parse(JSON.stringify(this.settings));
        this.$refs.drawer.open();
      });
    },
    reset() {
      this.settings = null;
      this.originalSettings = null;
      this.settingsHeader = null;
    },
    close() {
      this.originalSettings = null;
      return this.$nextTick().then(() => this.$refs.drawer.close());
    },
    apply() {
      this.$emit('input', this.settings);
      this.close();
    },
    refreshValidation() {
      this.$nextTick().then(() => {
        if (this.$refs.form) {
          this.valid = this.$refs.form.validate();
          this.$refs.form.resetValidation();
        }
      });
    },
  },
};
</script>