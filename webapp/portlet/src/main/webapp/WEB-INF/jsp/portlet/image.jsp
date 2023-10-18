<%@page import="org.exoplatform.social.attachment.model.ObjectAttachmentDetail"%>
<%@page import="org.apache.commons.lang.StringUtils"%>
<%@page import="java.util.List"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />
<%
  List<ObjectAttachmentDetail> files = (List<ObjectAttachmentDetail>) request.getAttribute("files");
  String name = (String) request.getAttribute("settingName");
  boolean canEdit = (boolean) request.getAttribute("canEdit");
  String id = "Image-" + renderRequest.getWindowID();

  if (files.size() > 0) {
    PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
    PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
    responseWrapper.addHeader("Link", "</portal/rest/v1/social/attachments/imagePortlet/" + name + "/" + files.get(0).getId() + ">; rel=prefetch; as=image; crossorigin=use-credentials", false);
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application transparent v-application--is-ltr theme--light" flat=""
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/Image'], app => app.init('<%=id%>', '<%=name%>', <%=canEdit%>, [<%=StringUtils.join(files.stream().map(ObjectAttachmentDetail::toString).toList(), ",")%>]));
    </script>
  </div>
</div>