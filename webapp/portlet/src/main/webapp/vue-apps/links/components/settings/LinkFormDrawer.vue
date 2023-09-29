<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :confirm-close="modified"
    :confirm-close-labels="confirmCloseLabels"
    class="linkFormDrawer"
    go-back-button
    right
    @closed="reset">
    <template slot="title">
      {{ $t('links.label.addLinkDrawerTitle') }}
    </template>
    <template v-if="link && drawer" #content>
      <v-form
        ref="form"
        autocomplete="off"
        class="pa-4">
        <div class="d-flex align-center mb-2 flex-grow-1 flex-shrink-1 text-truncate text-color">
          {{ $t('links.label.linkName') }}
        </div>
        <translation-text-field
          ref="linkNameInput"
          id="linkNameInput"
          v-model="link.name"
          :default-language="$root.defaultLanguage"
          :rules="rules.name"
          :placeholder="$t('links.label.linkNamePlaceholder')"
          :maxlength="maxNameLength"
          drawer-title="links.label.nameTranslation"
          class="width-auto flex-grow-1 mb-4"
          no-expand-icon
          back-icon
          autofocus
          required />

        <div class="d-flex align-center mb-2 flex-grow-1 flex-shrink-1 text-truncate text-color">
          {{ $t('links.label.description') }}
        </div>
        <translation-text-field
          ref="linkDescriptionInput"
          id="linkDescriptionInput"
          v-model="link.description"
          :default-language="$root.defaultLanguage"
          :rules="rules.description"
          :placeholder="$t('links.label.descriptionPlaceholder')"
          :maxlength="maxDescriptionLength"
          :required="false"
          drawer-title="links.label.descriptionTranslation"
          class="width-auto flex-grow-1 mb-4"
          no-expand-icon
          back-icon />

        <div class="d-flex align-center mb-2 flex-grow-1 flex-shrink-1 text-truncate text-color">
          {{ $t('links.label.url') }}
        </div>
        <v-text-field
          id="linkUrlInput"
          name="linkUrlInput"
          ref="linkUrlInput"
          v-model="link.url"
          :placeholder="$t('links.label.enterUrl')"
          :rules="rules.url"
          class="border-box-sizing width-auto pt-0 mb-4"
          type="text"
          outlined
          dense
          mandatory />

        <div class="d-flex mb-4">
          <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
            {{ $t('links.label.openInSameTab') }}
          </div>
          <v-switch
            v-model="link.sameTab"
            class="my-0 me-n2"
            dense
            hide-details />
        </div>

        <div class="d-flex flex-column mb-4">
          <div class="d-flex align-center flex-grow-1 flex-shrink-1 text-truncate text-color">
            {{ $t('links.label.updateIcon') }}
          </div>
          <links-icon-input
            v-model="link.iconUploadId"
            :link="link"
            class="mt-2"
            @reset="resetIcon"
            @src="link.iconSrc = $event" />
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
          {{ edit && $t('links.label.update') || $t('links.label.add') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    link: null,
    originalLink: null,
    drawer: false,
    edit: false,
    canValidate: false,
    index: -1,
    valid: true,
    maxNameLength: 50,
    maxDescriptionLength: 50,
  }),
  computed: {
    disabled() {
      return !this.valid
        || !this.modified
        || this.link?.name?.[this.$root.defaultLanguage]?.length > this.maxNameLength
        || this.link?.description?.[this.$root.defaultLanguage]?.length > this.maxDescriptionLength;
    },
    modified() {
      return this.originalLink && JSON.stringify(this.originalLink) !== JSON.stringify(this.link || {});
    },
    confirmCloseLabels() {
      return {
        title: this.$t('links.title.confirmCloseModification'),
        message: this.$t('links.message.confirmCloseModification'),
        ok: this.$t('confirm.yes'),
        cancel: this.$t('confirm.no'),
      };
    },
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length < this.maxNameLength || this.$t('links.input.exceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
        description: [
          v => !v?.length || v.length < this.maxDescriptionLength || this.$t('links.input.exceedsMaxLength', {
            0: this.maxDescriptionLength,
          }),
        ],
        url: [
          v => !!v?.length || ' ',
          v => {
            try {
              return !!this.$linkService.toLinkUrl(v)?.length || this.$t('links.input.invalidLink');
            } catch (e) {
              return this.$t('links.input.invalidLink');
            }
          },
        ],
      };
    },
  },
  watch: {
    link: {
      deep: true,
      handler() {
        if ((this.canValidate || this.edit) && this.$refs.form) {
          this.valid = this.$refs.form.validate();
          this.$refs.form.resetValidation();
        }
      },
    },
    drawer() {
      if (this.drawer) {
        this.valid = false;
        window.setTimeout(() => {
          if (this.edit) {
            this.valid = false;
          } else {
            this.canValidate = true;
          }
        }, 200);
      }
    },
  },
  created() {
    this.$root.$on('links-form-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('links-form-drawer', this.open);
  },
  methods: {
    open(link, edit, index) {
      if (!link) {
        link = {};
      }
      if (!link.name?.[this.$root.defaultLanguage]) {
        link.name = {};
        link.name[this.$root.defaultLanguage] = '';
      }
      if (!link.description?.[this.$root.defaultLanguage]) {
        link.description = {};
        link.description[this.$root.defaultLanguage] = '';
      }
      if (!link.iconSrc) {
        link.iconSrc = null;
      }
      this.link = JSON.parse(JSON.stringify(link));
      this.originalLink = JSON.parse(JSON.stringify(link));
      this.edit = edit;
      this.index = index;
      this.canValidate = false;
      this.valid = false;
      this.$refs.drawer.open();
    },
    reset() {
      this.link = null;
      this.originalLink = null;
    },
    close() {
      this.originalLink = null;
      return this.$nextTick().then(() => this.$refs.drawer.close());
    },
    resetIcon() {
      this.link.iconUrl = null;
      this.link.iconSrc = null;
      this.link.iconUploadId = null;
      this.link.iconFileId = 0;
    },
    apply() {
      this.canValidate = true;
      this.valid = this.$refs.form.validate();
      if (!this.valid) {
        return;
      }
      if (this.edit) {
        this.$emit('link-edit', this.link, this.index);
      } else {
        this.$emit('link-add', this.link);
      }
      this.close();
    },
  },
};
</script>