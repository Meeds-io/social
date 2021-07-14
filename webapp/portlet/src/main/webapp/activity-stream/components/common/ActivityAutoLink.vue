<script>
export default {
  created() {
    this.updateDocumentLinks();
  },
  mounted() {
    this.$root.$on('activity-created', this.updateDocumentLinks);
    this.$root.$on('activity-updated', this.updateDocumentLinks);
    this.$root.$on('activity-refresh-ui', this.updateDocumentLinks);
    this.$root.$on('activity-refreshed', this.updateDocumentLinks);
    this.$root.$on('activity-comment-created', this.updateDocumentLinks);
    this.$root.$on('activity-comment-updated', this.updateDocumentLinks);
    this.$root.$on('activity-comment-refreshed', this.updateDocumentLinks);
    this.updateDocumentLinks();
  },
  beforeDestroy() {
    this.$root.$off('activity-created', this.updateDocumentLinks);
    this.$root.$off('activity-updated', this.updateDocumentLinks);
    this.$root.$off('activity-refresh-ui', this.updateDocumentLinks);
    this.$root.$off('activity-refreshed', this.updateDocumentLinks);
    this.$root.$off('activity-comment-created', this.updateDocumentLinks);
    this.$root.$off('activity-comment-updated', this.updateDocumentLinks);
    this.$root.$off('activity-comment-refreshed', this.updateDocumentLinks);
  },
  methods: {
    updateDocumentLinks() {
      window.setTimeout(() => {
        window.document.querySelectorAll('a[href^=http]').forEach(link => {
          if (link.href && link.href.indexOf('http') === 0 && !link.target && link.hostname !== window.location.hostname) {
            link.target = '_blank';
          }
        });
      }, 200);
    },
  },
};
</script>