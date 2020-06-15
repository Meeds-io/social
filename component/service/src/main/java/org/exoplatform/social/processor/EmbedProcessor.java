package org.exoplatform.social.processor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.embedder.ExoMedia;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.service.rest.LinkShare;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A processor if it found a tag oembed it transform this content to iframe to show the media
 */

public class EmbedProcessor extends BaseActivityProcessorPlugin {
  private static final Log LOG = ExoLogger.getLogger(EmbedProcessor.class);
  private static final String openingOembed = "<oembed>";
  private static final String closingOembed = "</oembed>";
  private static final String IMAGE = "image";
  private static final String LINK = "link";
  private static final String DESCRIPTION = "description";
  private static final String TITLE = "title";
  private static final String COMMENT = "comment";
  private static final String LINK_ACTIVITY = "LINK_ACTIVITY";
  private static final String FILE_ACTIVITY = "files:spaces";
  private static final String HTML_PARAM = "html";
  private static final String DEFAULT_TITLE = "default_title";


  public EmbedProcessor(InitParams params) {
    super(params);
  }
   public void processActivity(ExoSocialActivity activity) {
     try {
       Map<String, String> templateParams = new HashMap();
       int firstIndex = 0;
       int lastIndex = 0;
       String defaultValue = activity.getTitle();
       String firstMessage = "";
       String url = "";
       if (StringUtils.isNotBlank(defaultValue)) {
           firstIndex = defaultValue.indexOf(openingOembed, firstIndex);
           if (firstIndex >= 0) {
             lastIndex = defaultValue.indexOf(closingOembed, firstIndex);
           }
       }
       if (firstIndex >= 0 && lastIndex > firstIndex) {
           firstMessage = defaultValue.substring(0, firstIndex);
           firstMessage = StringEscapeUtils.unescapeHtml4(firstMessage);
           String urlCoder = defaultValue.substring(firstIndex + openingOembed.length(), lastIndex);
           url = URLDecoder.decode(urlCoder, StandardCharsets.UTF_8.toString());
           String linkSource = "<a href=\""+url+"\" rel=\"nofollow\" target=\"_blank\">"+url+"</a>";
           firstMessage = firstMessage.replace(linkSource,"");
       }
       if (StringUtils.isNotBlank(url)) {
           LinkShare linkShare = LinkShare.getInstance(url);
           if (linkShare.getMediaObject() != null) {
               ExoMedia exoMedia = linkShare.getMediaObject();
               templateParams.put(IMAGE, "");
               String html = exoMedia.getHtml();
               templateParams.put(HTML_PARAM, html);
               templateParams.put(DESCRIPTION, exoMedia.getDescription());
               templateParams.put(TITLE, exoMedia.getTitle());
               templateParams.put(COMMENT, firstMessage);
               activity.setTitle(firstMessage);
           } else {
               if (linkShare.getImages().size() != 0) {
                   templateParams.put(IMAGE, linkShare.getImages().get(0));
               }

               templateParams.put(DESCRIPTION, linkShare.getDescription());
               templateParams.put(HTML_PARAM, null);
               templateParams.put(TITLE, linkShare.getTitle());
               templateParams.put(COMMENT, firstMessage);
               activity.setTitle(linkShare.getTitle());
           }

           templateParams.put(TEMPLATE_PARAM_TO_PROCESS, COMMENT);
           templateParams.put(LINK, url);
           //if activity contains files params then add link template params to the existing ones
           if (activity.getType().equals(FILE_ACTIVITY)) {
             templateParams.putAll(activity.getTemplateParams());
           } else {
             activity.setType(LINK_ACTIVITY);
           }
           templateParams.put(DEFAULT_TITLE, defaultValue);
           activity.setTemplateParams(templateParams);
       }
     } catch (Exception e) {
         LOG.error("EmbedProcessor error : ", e);
     }
   }
}
