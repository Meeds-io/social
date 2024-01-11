<%
/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
%>
<%@page import="org.exoplatform.social.attachment.model.ObjectAttachmentDetail"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="java.util.List"%>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
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