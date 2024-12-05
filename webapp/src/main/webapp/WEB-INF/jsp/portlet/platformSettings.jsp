<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.portal.config.UserPortalConfigService" %>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext" %>
<%@ page import="org.exoplatform.web.application.RequestContext" %>
<%
String path = ExoContainerContext.getService(UserPortalConfigService.class)
    .getDefaultSitePath("administration", request.getRemoteUser());
  if (path != null) {
%>
  <div class="VuetifyApp">
    <div data-app="true"
      class="v-application v-application--is-ltr theme--light"
      id="platformSettings">
      <div class="v-application--wrap">
        <a href="/portal/administration/home/general/mainsettings" target="_blank" class="mx-2 d-none d-sm-block v-btn v-btn--flat v-btn--icon v-btn--round theme--light v-size--default">
          <span class="v-btn__content">
            <i aria-hidden="true" role="button" aria-haspopup="true" aria-expanded="false" class="v-icon notranslate icon-default-color fas fa-cog theme--light" style="font-size: 22px;"></i>
          </span>
        </a>
      </div>
      <script type="text/javascript">
        require(['PORTLET/social/PlatformSettings'], app => app.init());
      </script>
    </div>
  </div>
<% } %>
