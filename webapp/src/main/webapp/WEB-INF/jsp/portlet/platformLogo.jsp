<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>

<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light platformLogo"
    id="platformLogoApplication">
    <script type="text/javascript">
      require(['PORTLET/social/PlatformLogo'], app =>app.init());
    </script>
  </div>
</div>
