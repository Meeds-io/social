<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.portal.config.model.Page"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.portal.config.UserACL"%>
<%@page import="org.apache.commons.text.StringEscapeUtils"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />

<%

  String id = "sideBarLogin-" + renderRequest.getWindowID();

  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  String branding = EntityBuilder.toJsonString(brandingService.getBrandingInformation(false));

  String portletStorageId = ((String) request.getAttribute("portletStorageId"));
  String hAlign = request.getAttribute("hAlign") == null ? "CENTER" : ((String[]) request.getAttribute("hAlign"))[0];
  String vAlign = request.getAttribute("vAlign") == null ? "CENTER" : ((String[]) request.getAttribute("vAlign"))[0];


  Page currentPage = PortalRequestContext.getCurrentInstance().getPage();
  boolean canEdit = ExoContainerContext.getService(UserACL.class).hasEditPermission(currentPage, ConversationState.getCurrent().getIdentity());
  String pageRef = currentPage.getPageKey().format();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light sidebarLogin"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/SidebarLogin'], app =>app.init('<%=id%>',
        "<%=StringEscapeUtils.escapeJava(branding)%>",
        '<%=portletStorageId%>',
        '<%=hAlign%>',
        '<%=vAlign%>',
        '<%=pageRef%>',
        <%=canEdit%>
      ));
    </script>
  </div>
</div>
