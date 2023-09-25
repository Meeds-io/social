<template>
  <exo-drawer
    ref="drawer"
    class="linkFormDrawer"
    go-back-button
    right
    @closed="reset">
    <template slot="title">
      {{ $t('links.label.addLinkDrawerTitle') }}
    </template>
    <template v-if="link" #content>
      <v-form ref="form" class="pa-4">
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
          :object-id="link.id"
          object-type="links"
          field-name="name"
          drawer-title="links.label.nameTranslation"
          class="width-auto flex-grow-1 mb-4"
          no-expand-icon
          back-icon
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
          :object-id="link.id"
          object-type="links"
          field-name="description"
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
    edit: false,
    index: -1,
    valid: false,
    maxNameLength: 50,
    maxDescriptionLength: 50,
  }),
  watch: {
    link: {
      deep: true,
      handler() {
        if (this.$refs.form) {
          this.valid = this.$refs.form.validate();
        }
      },
    },
  },
  computed: {
    disabled() {
      return !this.valid
        || this.link?.name[this.$root.defaultLanguage]?.length > this.maxNameLength
        || this.link?.description[this.$root.defaultLanguage]?.length > this.maxDescriptionLength;
    },
    rules() {
      return {
        name: [
          v => !!v?.length || this.$t('links.input.mandatory'),
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
          v => !!v?.length || this.$t('links.input.mandatory'),
          v => {
            try {
              return !!(v?.length && new URL(v)?.hostname?.length) || this.$t('links.input.invalidLink');
            } catch (e) {
              return this.$t('links.input.invalidLink');
            }
          },
        ],
      };
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
      if (!link.name || !link.name[this.$root.defaultLanguage]) {
        link.name = {};
        link.name[this.$root.defaultLanguage] = '';
      }
      if (!link.description || !link.description[this.$root.defaultLanguage]) {
        link.description = {};
        link.description[this.$root.defaultLanguage] = '';
      }
      this.link = JSON.parse(JSON.stringify(link));
      this.edit = edit;
      this.index = index;
      this.$refs.drawer.open();
    },
    reset() {
      this.link = null;
    },
    close() {
      this.$refs.drawer.close();
    },
    apply() {
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