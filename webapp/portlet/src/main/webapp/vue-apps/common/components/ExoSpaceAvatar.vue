<template>
  <v-menu
    v-if="popover && !isMobile"
    v-model="menu"
    rounded="rounded"
    open-on-hover
    :close-on-content-click="false"
    :left="popoverLeftPosition"
    content-class="profile-popover-menu white"
    max-width="270"
    min-width="270"
    offset-y>
    <template v-slot:activator="{ on, attrs }">
      <div 
        class="profile-popover space-avatar-wrapper"
        :class="extraClass">
        <a
          v-if="avatar"
          v-bind="attrs"
          v-on="on"
          :id="id"
          :href="url"
          class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
          <v-avatar
            :size="size"
            tile
            class="pull-left my-auto">
            <img
              :src="avatarUrl"
              :class="avatarClass"
              class="object-fit-cover ma-auto"
              loading="lazy"
              role="presentation">
          </v-avatar>
        </a>
        <a
          v-else-if="fullname"
          v-bind="attrs"
          v-on="on"
          :id="id"
          :href="url"
          class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
          <div
            v-if="displayName || $slots.subTitle"
            :class="!subtitleNewLine && 'd-flex'"
            class="pull-left text-truncate">
            <p
              v-if="displayName"
              :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
              class="text-truncate subtitle-2 my-auto">
              {{ displayName }}
            </p>
            <p v-if="$slots.subTitle" class="text-sub-title my-auto">
              <slot name="subTitle">
              </slot>
            </p>
          </div>
        </a>
        <a
          v-else
          v-bind="attrs"
          v-on="on"
          :id="id"
          :href="url"
          class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
          <v-avatar
            :size="size"
            tile
            class="pull-left my-auto">
            <img
              :src="avatarUrl"
              :class="avatarClass"
              class="object-fit-cover ma-auto"
              loading="lazy"
              role="presentation">
          </v-avatar>
          <div
            v-if="displayName || $slots.subTitle"
            :class="!subtitleNewLine && 'd-flex'"
            class="pull-left text-truncate ms-2">
            <p
              v-if="displayName"
              :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
              class="text-truncate subtitle-2 my-auto">
              {{ displayName }}
            </p>
            <p v-if="$slots.subTitle" class="text-sub-title my-auto">
              <slot name="subTitle">
              </slot>
            </p>
          </div>
        </a>
      </div>
    </template>
    <v-card elevation="0" class="pa-2">
      <v-list-item class="px-2">
        <v-list-item-content class="py-0">
          <v-list-item-title>
            <exo-space-avatar
              :space="space"
              :size="40"
              avatar-class="border-color"
              class="activity-share-space d-inline-block my-auto"
              bold-title
              link-style
              subtitle-new-line>
              <template slot="subTitle">
                <span v-if="spaceMembersCount" class="caption text-bold">
                  {{ spaceMembersCount }} {{ $t('UIActivity.label.Members') }}
                </span>
              </template>
            </exo-space-avatar>
          </v-list-item-title>
          <p v-if="spaceDescription" class="text-truncate-3 text-caption text--primary font-weight-medium">
            {{ spaceDescription }}
          </p>
        </v-list-item-content>
      </v-list-item>
      <div class="d-flex justify-end">
        <extension-registry-components
          :params="params"
          class="d-flex"
          name="SpacePopover"
          type="space-popover-action"
          parent-element="div"
          element="div"
          element-class="mx-auto ma-lg-0" />
        <div
          v-for="extension in enabledWebConferencingComponents"
          :key="extension.key"
          :class="`${extension.appClass} ${extension.typeClass}`"
          :ref="extension.key">
        </div>
      </div>
    </v-card>
  </v-menu>
  <div
    v-else
    class="profile-popover space-avatar-wrapper"
    :class="extraClass">
    <a
      v-if="avatar"
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <v-avatar
        :size="size"
        tile
        class="pull-left my-auto">
        <img
          :src="avatarUrl"
          :class="avatarClass"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
    </a>
    <a
      v-else-if="fullname"
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <div
        v-if="displayName || $slots.subTitle"
        :class="!subtitleNewLine && 'd-flex'"
        class="pull-left text-truncate ms-2">
        <p
          v-if="displayName"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 my-auto">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title my-auto">
          <slot name="subTitle">
          </slot>
        </p>
      </div>
    </a>
    <a
      v-else
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <v-avatar
        :size="size"
        tile
        class="pull-left my-auto">
        <img
          :src="avatarUrl"
          :class="avatarClass"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <div
        v-if="displayName || $slots.subTitle"
        :class="!subtitleNewLine && 'd-flex'"
        class="pull-left text-truncate ms-2">
        <p
          v-if="displayName"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 my-auto">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title my-auto">
          <slot name="subTitle">
          </slot>
        </p>
      </div>
    </a>
  </div>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    spacePrettyName: {
      type: String,
      default: () => null,
    },
    spaceId: {
      type: String,
      default: () => null,
    },
    avatar: {
      type: Boolean,
      default: () => false,
    },
    fullname: {
      type: Boolean,
      default: () => false,
    },
    boldTitle: {
      type: Boolean,
      default: () => false,
    },
    linkStyle: {
      type: Boolean,
      default: () => false,
    },
    smallFontSize: {
      type: Boolean,
      default: () => false,
    },
    subtitleNewLine: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    popover: {
      type: Boolean,
      default: () => false,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
    },
    extraClass: {
      type: String,
      default: () => '',
    },
  },
  data() {
    return {
      id: `spaceAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      spacePopupExtensions: [],
      menu: false,
    };
  },
  watch: {
    menu () {
      if ( this.menu ) {
        this.spacePopupExtensions.map((extension) => {
          if ( !this.$refs[extension.key] ) {
            this.$nextTick().then(() => {
              this.initWebConferencingActionComponent(extension);
            });
          }
        });
      }
    }
  },
  computed: {
    displayName() {
      return this.space && this.space.displayName;
    },
    prettyName() {
      return this.space && this.space.prettyName;
    },
    groupId() {
      return this.space && this.space.groupId;
    },
    avatarUrl() {
      return this.space && this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.prettyName}/avatar`;
    },
    url() {
      if (!this.groupId) {
        return '#';
      }
      const uri = this.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
    spaceMembersCount() {
      return this.space && this.space.membersCount;
    },
    spaceDescription() {
      return this.space && this.space.description;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${this.smallFontSize && 'caption ' || ''}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    enabledWebConferencingComponents() {
      return this.spacePopupExtensions.filter(extension => extension.enabled);
    },
    params() {
      return {
        identity: this.space
      };
    },
  },
  created() {
    if (!this.space) {
      if (this.spacePrettyName && !this.spaceId){
        this.$spaceService.getSpaceByPrettyName(this.spacePrettyName)
          .then(space => {
            this.space = space;
          });
      } else {
        if (this.spaceId) {
          this.$spaceService.getSpaceById(this.spaceId)
            .then(space => {
              this.space = space;
            });
        }
      }
    }
    if ( this.popover && !this.isMobile) {
      this.refreshExtensions();
    }

  },
  methods: {
    refreshExtensions() {
      this.spacePopupExtensions = extensionRegistry.loadExtensions('space-popup', 'space-popup-action') || [];
    },
    initWebConferencingActionComponent(extension) {
      if (extension.enabled) {
        let container = this.$refs[extension.key];
        if (container && container.length > 0) {
          container = container[0];
          extension.init(container, this.space.prettyName);
        } else {
          // eslint-disable-next-line no-console
          console.error(
            `Error initialization of the ${extension.key} action component: empty container`
          );
        }
      }
    },
    extensionAction(event, extension) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      if (extension && extension.click) {
        extension.click(this.space.id);
      }
    }
  }
};
</script>
