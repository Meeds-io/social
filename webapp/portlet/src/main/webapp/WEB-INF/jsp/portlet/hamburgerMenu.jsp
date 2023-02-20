<%@page import="org.exoplatform.commons.utils.PropertyManager"%>
<%@page import="org.exoplatform.commons.api.settings.SettingValue"%>
<%@page import="org.exoplatform.commons.api.settings.data.Scope"%>
<%@page import="org.exoplatform.commons.api.settings.data.Context"%>
<%@page import="org.exoplatform.commons.api.settings.SettingService"%>
<%@page import="org.exoplatform.social.core.space.SpacesAdministrationService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%
  boolean canCreateSpace = ExoContainerContext.getService(SpacesAdministrationService.class).canCreateSpace(request.getRemoteUser());
%>
<div class="VuetifyApp">
  <div id="HamburgerNavigationMenu" data-app="true" class="v-application HamburgerNavigationMenu v-application--is-ltr theme--light" id="app" color="transaprent" flat="">
    <div class="v-application--wrap">
      <a class="HamburgerNavigationMenuLink">
        <div class="px-5 py-3">
          <i aria-hidden="true"
            class="v-icon notranslate fa fa-bars theme--light"
            style="font-size: 24px;"></i>
        </div>
      </a>
      <script type="text/javascript">
        require(['SHARED/HamburgerMenu'], app => app.init(<%=canCreateSpace%>));
      </script>
    </div>
  </div>
</div>