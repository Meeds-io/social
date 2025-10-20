<%@ page import="org.exoplatform.portal.branding.BrandingService"%>
<%@ page import="org.exoplatform.commons.utils.CommonsUtils"%>
<%@page import="org.exoplatform.social.rest.api.EntityBuilder"%>
<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@ page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@ page import="org.exoplatform.portal.config.model.Page"%>
<%@ page import="org.exoplatform.container.ExoContainerContext"%>
<%@ page import="org.exoplatform.portal.config.UserACL"%>
<%@page import="org.apache.commons.text.StringEscapeUtils"%>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="io.meeds.social.translation.service.TranslationService" %>
<%@ page import="org.exoplatform.commons.file.model.FileItem" %>
<%@ page import="org.exoplatform.commons.file.services.FileService" %>
<%@page import="org.exoplatform.web.PortalHttpServletResponseWrapper"%>
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.social.attachment.AttachmentService"%>
<%@page import="org.exoplatform.social.attachment.model.ObjectAttachmentList"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects />

<%

  String id = "sideBarLogin-" + renderRequest.getWindowID();
  String objectType = "cmsPortlet";
  BrandingService brandingService = CommonsUtils.getService(BrandingService.class);
  String branding = EntityBuilder.toJsonString(brandingService.getBrandingInformation(false));

  String portletStorageId = ((String) request.getAttribute("portletStorageId"));
  String hAlign = request.getAttribute("hAlign") == null ? "CENTER" : ((String[]) request.getAttribute("hAlign"))[0];
  String vAlign = request.getAttribute("vAlign") == null ? "CENTER" : ((String[]) request.getAttribute("vAlign"))[0];
  String settingName = ((String) request.getAttribute("settingName"));


  Page currentPage = PortalRequestContext.getCurrentInstance().getPage();
  boolean canEdit = ExoContainerContext.getService(UserACL.class).hasEditPermission(currentPage, ConversationState.getCurrent().getIdentity());
  String pageRef = currentPage.getPageKey().format();

  TranslationService translationService = CommonsUtils.getService(TranslationService.class);

  String title = translationService.getTranslationLabelOrDefault(objectType,
              settingName, "brandingTitle", request.getLocale());
  title = title == null ? null : URLEncoder.encode(title.replace(" ", "._.")).replace("._.", " ");

  String subtitle = translationService.getTranslationLabelOrDefault(objectType,
                settingName, "brandingSubtitle", request.getLocale());
  subtitle = subtitle == null ? null : URLEncoder.encode(subtitle.replace(" ", "._.")).replace("._.", " ");


  AttachmentService attachmentService = CommonsUtils.getService(AttachmentService.class);
  ObjectAttachmentList attachments = attachmentService.getAttachments(objectType, settingName);
  String backgroundFileId = "0";
  if (attachments.getAttachments().size() > 0) {
    backgroundFileId = attachments.getAttachments().get(0).getId();
    PortalRequestContext rcontext = (PortalRequestContext) PortalRequestContext.getCurrentInstance();
    PortalHttpServletResponseWrapper responseWrapper = (PortalHttpServletResponseWrapper) rcontext.getResponse();
    responseWrapper.addHeader("Link", "</portal/rest/v1/social/attachments/cmsPortlet/" + settingName + "/" + backgroundFileId + ">; rel=prefetch; as=image; crossorigin=use-credentials", false);
  }
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application white v-application--is-ltr theme--light sidebarLogin"
    id="<%=id%>">
    <script type="text/javascript">
      require(['PORTLET/social/SidebarLogin'], app =>app.init('<%=id%>',
        "<%=StringEscapeUtils.escapeJava(branding)%>",
        '<%=portletStorageId%>',
        '<%=settingName%>',
        <%=title == null ? null : String.format("'%s'",title)%>,
        <%=subtitle == null ? null : String.format("'%s'",subtitle)%>,
        '<%=hAlign%>',
        '<%=vAlign%>',
        '<%=pageRef%>',
        <%=canEdit%>,
        <%=backgroundFileId%>
      ));
    </script>
  </div>
</div>
