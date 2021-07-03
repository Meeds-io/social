package org.exoplatform.social.processor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.embedder.ExoMedia;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.service.rest.LinkShare;

/**
 * A processor if it found a tag oembed it transform this content to iframe to show the media
 */

public class EmbedProcessor extends BaseActivityProcessorPlugin {
  private static final Log LOG = ExoLogger.getLogger(EmbedProcessor.class);
  private static final String OPENING_OEMBED = "<oembed>";
  private static final String CLOSING_OEMBED = "</oembed>";
  private static final String IMAGE = "image";
  private static final String LINK = "link";
  private static final String DESCRIPTION = "description";
  private static final String TITLE = "title";
  private static final String COMMENT = "comment";
  private static final String LINK_ACTIVITY = "LINK_ACTIVITY";
  private static final String HTML_PARAM = "html";
  private static final String DEFAULT_TITLE = "default_title";


  public EmbedProcessor(InitParams params) {
    super(params);
  }
   public void processActivity(ExoSocialActivity activity) {
     boolean hasLinkProperties = false;
     try {
       Map<String, String> templateParams = new HashMap<>();
       int firstIndex = 0;
       int lastIndex = 0;
       String defaultValue = activity.getTitle();
       String firstMessage = "";
       String url = "";
       if (StringUtils.isNotBlank(defaultValue)) {
           firstIndex = defaultValue.indexOf(OPENING_OEMBED, firstIndex);
           if (firstIndex >= 0) {
             lastIndex = defaultValue.indexOf(CLOSING_OEMBED, firstIndex);
           }
       }
       if (firstIndex >= 0 && lastIndex > firstIndex) {
           firstMessage = defaultValue.substring(0, firstIndex);
           firstMessage = StringEscapeUtils.unescapeHtml4(firstMessage);
           String urlCoder = defaultValue.substring(firstIndex + OPENING_OEMBED.length(), lastIndex);
           url = URLDecoder.decode(urlCoder, StandardCharsets.UTF_8.toString());
           String linkSource1 = "<a href=\""+url+"\" rel=\"nofollow\" target=\"_blank\">"+url+"</a>";
           String linkSource2 = "<p><a href=\""+url+"\">"+url+"</a></p>";
           String linkSource3 = "<a href=\""+url+"\">"+url+"</a>";
           firstMessage = firstMessage.replace(linkSource1,"");
           firstMessage = firstMessage.replace(linkSource2,"");
           firstMessage = firstMessage.replace(linkSource3,"");
       }
       if (StringUtils.isNotBlank(url)) {
           LinkShare linkShare = LinkShare.getInstance(url);
           if (linkShare == null) {
             return;
           } else if (linkShare.getMediaObject() != null) {
               ExoMedia exoMedia = linkShare.getMediaObject();
               templateParams.put(IMAGE, "");
               String html = exoMedia.getHtml();
               templateParams.put(HTML_PARAM, html);
               templateParams.put(DESCRIPTION, linkShare.getDescription());
               templateParams.put(TITLE, linkShare.getTitle());
               templateParams.put(COMMENT, firstMessage);
               hasLinkProperties = true;
               activity.setTitle(linkShare.getTitle());
           } else {
               if (!CollectionUtils.isEmpty(linkShare.getImages())) {
                 templateParams.put(IMAGE, linkShare.getImages().get(0));
               }

               templateParams.put(DESCRIPTION, linkShare.getDescription());
               templateParams.put(HTML_PARAM, null);
               templateParams.put(TITLE, linkShare.getTitle());
               templateParams.put(COMMENT, firstMessage);
               hasLinkProperties = true;
               activity.setTitle(linkShare.getTitle());
           }

           templateParams.put(TEMPLATE_PARAM_TO_PROCESS, COMMENT);
           templateParams.put(LINK, url);
           templateParams.put(DEFAULT_TITLE, defaultValue);
           //if activity contains files params then add link template params to the existing ones
           if (StringUtils.isBlank(activity.getType())) {
             activity.setType(LINK_ACTIVITY);
           }
           activity.getTemplateParams().putAll(templateParams);
       }
     } catch (Exception e) {
         LOG.error("EmbedProcessor error : ", e);
     } finally {
       // Cleanup properties if no more of type link
       if (!hasLinkProperties) {
         activity.getTemplateParams().remove(IMAGE);
         activity.getTemplateParams().remove(DESCRIPTION);
         activity.getTemplateParams().remove(HTML_PARAM);
         activity.getTemplateParams().remove(TITLE);
         activity.getTemplateParams().remove(TEMPLATE_PARAM_TO_PROCESS);
         activity.getTemplateParams().remove(LINK);
         activity.getTemplateParams().remove(DEFAULT_TITLE);
         activity.getTemplateParams().remove(COMMENT);
         if (StringUtils.equals(LINK_ACTIVITY, activity.getType())) {
           activity.setType("");
         }
       }
     }
   }

   @Override
  public boolean isPreActivityProcessor() {
    return true;
  }
}
