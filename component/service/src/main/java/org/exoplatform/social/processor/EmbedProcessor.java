package org.exoplatform.social.processor;

import org.apache.commons.lang3.StringUtils;
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


  public EmbedProcessor(InitParams params) {
    super(params);
  }
   public void processActivity(ExoSocialActivity activity) {
     try {
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
           String urlCoder = defaultValue.substring(firstIndex + openingOembed.length(), lastIndex);
           url = URLDecoder.decode(urlCoder, StandardCharsets.UTF_8.toString());
           String test = "<a href=\""+url+"\" rel=\"nofollow\" target=\"_blank\">"+url+"</a>";
           firstMessage = firstMessage.replace(test,"");
       }
       if (StringUtils.isNotBlank(url)) {
           LinkShare linkShare = LinkShare.getInstance(url);
           if (linkShare.getMediaObject() != null) {
               String html = linkShare.getMediaObject().getHtml();
               StringBuilder newTitleActivity = new StringBuilder();
               newTitleActivity.append(firstMessage).append(html);
               activity.setTitle(newTitleActivity.toString());
           } else {
               Map<String, String> templateParams = new HashMap<>();
               templateParams.put(LINK, url);
               if (linkShare.getImages().size() != 0) {
                   templateParams.put(IMAGE, linkShare.getImages().get(0));
               }
               templateParams.put(DESCRIPTION, linkShare.getDescription());
               templateParams.put(TITLE, linkShare.getTitle());
               templateParams.put(COMMENT, firstMessage);
               templateParams.put(TEMPLATE_PARAM_TO_PROCESS, COMMENT);
               activity.setType(LINK_ACTIVITY);
               activity.setTitle(linkShare.getTitle());
               activity.setTemplateParams(templateParams);
           }
       }
     } catch (Exception e) {
         LOG.error("EmbedProcessor error : ", e);
     }
   }
}
