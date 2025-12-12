<template>
  <div class="carousel-top-parent overflow-hidden position-relative">
    <v-expand-transition>
      <v-btn
        v-show="displayLeftArrow"
        :aria-label="$t('cardCarousel.leftArrowButtonTitle')"
        :left="!$vuetify.rtl"
        :right="$vuetify.rtl"
        color="while"
        width="23px"
        height="23px"
        class="absolute-vertical-center z-index-one"
        fab
        dark
        absolute
        x-small
        @click="moveLeft">
        <v-icon size="25">{{ leftArrowIcon }}</v-icon>
      </v-btn>
    </v-expand-transition>
    <v-card
      :class="!dense && 'px-0 pb-4 pt-2'"
      class="carousel-middle-parent scrollbar-width-none transparent d-flex overflow-x-scroll"
      flat
      @scroll="computeProperties"
      @resize="computeProperties">
      <div :class="parentClass" class="carousel-last-parent d-flex ma-auto">
        <slot></slot>
      </div>
    </v-card>
    <v-expand-transition>
      <v-btn
        v-show="displayRightArrow"
        :aria-label="$t('cardCarousel.rightArrowButtonTitle')"
        :left="$vuetify.rtl"
        :right="!$vuetify.rtl"
        color="while"
        width="23px"
        height="23px"
        class="absolute-vertical-center z-index-one"
        fab
        dark
        absolute
        x-small
        @click="moveRight">
        <v-icon size="25">{{ rightArrowIcon }}</v-icon>
      </v-btn>
    </v-expand-transition>
  </div>
</template>

<script>
export default {
  props: {
    parentClass: {
      type: Object,
      default: null,
    },
    hideArrows: {
      type: Boolean,
      default: false,
    },
    dense: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    scrollElement: null,
    displayLeftArrow: false,
    displayRightArrow: false,
    childScrollIndex: 0,
    visibleChildrenPerPage: 1,
    computing: false,
    initialized: false,
  }),
  computed: {
    leftArrowIcon() {
      return this.$vuetify.rtl && 'fa-arrow-circle-right' || 'fa-arrow-circle-left';
    },
    rightArrowIcon() {
      return this.$vuetify.rtl && 'fa-arrow-circle-left' || 'fa-arrow-circle-right';
    },
  },
  mounted() {
    if (!this.hideArrows) {
      this.scrollElement = this.$el && this.$el.children && this.$el.children.length > 1 && this.$el.children[1];
  
      window.setTimeout(() => {
        this.computeProperties();
      }, 500);
      window.onresize = this.computeProperties;
    }
  },
  updated() {
    this.computeProperties();
  },
  methods: {
    stopPropagation(event) {
      if (event) {
        event.stopPropagation();
      }
    },
    moveRight() {
      const children = this.scrollElement.firstChild.children;
      const newIndex = this.childScrollIndex + this.visibleChildrenPerPage;
      this.childScrollIndex = newIndex >= children.length ? (children.length - 1) : newIndex;
      this.scrollElement.scrollTo({
        left: children[this.childScrollIndex].offsetLeft - 8,
        behavior: 'smooth'
      });
    },
    moveLeft() {
      const children = this.scrollElement.firstChild.children;
      const newIndex = this.childScrollIndex - this.visibleChildrenPerPage;
      this.childScrollIndex = newIndex < 0 ? 0 : newIndex;
      this.scrollElement.scrollTo({
        left: children[this.childScrollIndex].offsetLeft - 8,
        behavior: 'smooth'
      });
    },
    computeProperties() {
      if (!this.computing && !this.hideArrows) {
        this.computing = true;
        window.setTimeout(() => {
          const parentWidth = this.scrollElement.offsetWidth;
          const contentWidth = this.scrollElement.firstChild.offsetWidth;
          const children = this.scrollElement.firstChild.children;
          const childrenCount = children.length;
          this.visibleChildrenPerPage = parseInt(parentWidth * childrenCount / contentWidth);
          this.displayLeftArrow = this.scrollElement && childrenCount && this.checkDisplayLeftArrow(children);
          this.displayRightArrow = this.scrollElement && childrenCount && this.checkDisplayRightArrow(children);
          if (!this.initialized && childrenCount) {
            this.childScrollIndex = this.visibleChildrenPerPage >= children.length ? (children.length - 1) : this.visibleChildrenPerPage;
            this.initialized = true;
          }
          this.computing = false;
        }, 200);
      }
    },
    checkDisplayLeftArrow(children) {
      return Math.abs(this.scrollElement.scrollLeft) - Math.abs(this.$vuetify.rtl ? 0 : children[0].offsetLeft) > 10;
    },
    checkDisplayRightArrow() {
      return parseInt(this.scrollElement.scrollWidth - this.scrollElement.offsetWidth - Math.abs(this.scrollElement.scrollLeft)) > 10;
    },
  },
};
</script>