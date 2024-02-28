/*

 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.image;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.model.AvatarAttachment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * @author tuan_nguyenxuan Oct 29, 2010
 */
public class ImageUtils {
  public static final String KEY_SEPARATOR           = "_";

  public static final String KEY_DIMENSION_SEPARATOR = "x";

  public static final String GIF_EXTENDSION          = "gif";

  private static final int   DEFAULT_AVATAR_WIDTH    = 350;

  private static final int   DEFAULT_AVATAR_HEIGHT   = 350;

  private static final Log   LOG                     = ExoLogger.getLogger(ImageUtils.class);

  /**
   * @param str Make string params not null
   */
  private static void makeNotNull(String... str)
  {
    for (String string : str) {
      if (string == null)
        string = "";
    }
  }

  /**
   * @param oldFileName
   * @param subfix
   * @param postfix
   * @return name of resized image name like from "avatar.jpg" to
   *         "RESIZED_avatar_100x100.jpg"
   */
  public static String buildFileName(String oldFileName, String subfix, String postfix) {
    makeNotNull(oldFileName,subfix,postfix);
    if(oldFileName.equals(""))
      return subfix + postfix;
    int dotIndex = oldFileName.lastIndexOf('.');
    if(dotIndex < 0)
      return subfix + oldFileName + postfix;
    String nameOnly = oldFileName.substring(0, dotIndex);
    String extendtionAndDot = oldFileName.substring(dotIndex);
    return subfix + nameOnly + postfix + extendtionAndDot;
  }

  /**
   * @param width
   * @param height
   * @return postfix for image name like avatar_100x100
   */

  public static String buildImagePostfix(int width, int height) {
    return KEY_SEPARATOR + (width < 0 ? 0 : width) + KEY_DIMENSION_SEPARATOR
        + (height < 0 ? 0 : height);
  }

  /**
   * @param imageStream
   * @param maxWidth
   * @param maxHeight
   * @param avatarId
   * @param avatarFileName
   * @param avatarMimeType
   * @param avatarWorkspace
   * @return new AvatarAtachment that contain parameter values and resized
   *         avatar
   */
  public static AvatarAttachment createResizedAvatarAttachment(InputStream imageStream,
                                                               int maxWidth,
                                                               int maxHeight,
                                                               String avatarId,
                                                               String avatarFileName,
                                                               String avatarMimeType,
                                                               String avatarWorkspace) {
    if (maxHeight <= 0 || maxWidth <= 0) {
      LOG.warn("Fail to resize image to avatar attachment with dimension <= 0");
      return null;
    }

    try {
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      String extension = mimeTypeResolver.getExtension(avatarMimeType);
      // TODO: Resize gif image. Now we skip gif because we can't resize it now
      if (extension.equalsIgnoreCase(GIF_EXTENDSION)) {
        return null;
      }

      BufferedImage image = ImageIO.read(imageStream);

      int targetHeight = image.getHeight();
      int targetWidth = image.getWidth();

      double maxDimensionsRatio =  (double) maxHeight / (double) maxWidth;
      double imageRatio =  (double) image.getHeight() / (double) image.getWidth();

      if(imageRatio > maxDimensionsRatio && image.getHeight() > maxHeight) {
        targetHeight = maxHeight;
        targetWidth = (maxHeight * image.getWidth()) / image.getHeight();
      } else if(imageRatio < maxDimensionsRatio && image.getWidth() > maxWidth) {
        targetHeight = (maxWidth * image.getHeight()) / image.getWidth();
        targetWidth = maxWidth;
      }

      // Create temp file to store resized image to put to avatar attachment
      File tmp = File.createTempFile("RESIZED", null);
      image = resizeImage(image, targetWidth, targetHeight);

      ImageIO.write(image, extension, tmp);
      
      // Create new avatar attachment
      AvatarAttachment newAvatarAttachment = new AvatarAttachment(avatarId,
                                                                  avatarFileName,
                                                                  avatarMimeType,
                                                                  new FileInputStream(tmp),
                                                                  System.currentTimeMillis());

      // Delete temp file
      tmp.delete();
      return newAvatarAttachment;
    } catch (Exception e) {
      LOG.error("Fail to resize image to avatar attachment: " + e);
      return null;
    }
  }

  public static AvatarAttachment createDefaultAvatar(String identityId, String fullNameAbbreviation) {
    AvatarAttachment newAvatarAttachment = null;

    List<Color> colorList = List.of(new Color(239, 83, 80),
                                    new Color(25, 118, 210),
                                    new Color(171, 71, 188),
                                    new Color(0, 137, 123),
                                    new Color(158, 157, 36),
                                    new Color(251, 192, 45),
                                    new Color(0, 191, 165),
                                    new Color(117, 117, 117),
                                    new Color(244, 67, 54),
                                    new Color(33, 150, 243),
                                    new Color(124, 179, 66),
                                    new Color(48, 63, 159),
                                    new Color(69, 39, 160),
                                    new Color(141, 110, 99),
                                    new Color(255, 111, 0));
    BufferedImage image = new BufferedImage(DEFAULT_AVATAR_WIDTH, DEFAULT_AVATAR_HEIGHT, BufferedImage.TYPE_INT_RGB);

    Graphics2D graphics = image.createGraphics();
    graphics.setColor(colorList.get(Integer.parseInt(identityId) % colorList.size()));
    graphics.fillRect(0, 0, DEFAULT_AVATAR_WIDTH, DEFAULT_AVATAR_HEIGHT);
    graphics.setColor(Color.WHITE);

    graphics.setFont(new Font("Arial", Font.BOLD, 150));
    FontMetrics fm = graphics.getFontMetrics();

    int x = (DEFAULT_AVATAR_WIDTH - fm.stringWidth(fullNameAbbreviation)) / 2;
    int y = (fm.getAscent() + (DEFAULT_AVATAR_HEIGHT - (fm.getAscent() + fm.getDescent())) / 2);

    graphics.drawString(fullNameAbbreviation, x, y);
    graphics.drawImage(image, 0, 0, DEFAULT_AVATAR_WIDTH, DEFAULT_AVATAR_HEIGHT, null);
    graphics.dispose();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", outputStream);
      newAvatarAttachment = new AvatarAttachment(null,
                                                 "DEFAULT_AVATAR",
                                                 "image/png",
                                                 new ByteArrayInputStream(outputStream.toByteArray()),
                                                 System.currentTimeMillis());
      return newAvatarAttachment;
    } catch (IOException e) {
      LOG.warn("Fail to create default avatar for identity {}. Use default static avatar instead.", identityId, e);
      return null;
    }
  }

  private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics2D = bufferedImage.createGraphics();
    graphics2D.setComposite(AlphaComposite.Src);
    //below three lines are for RenderingHints for better image quality
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    graphics2D.drawImage(image, 0, 0, width, height, null);
    graphics2D.dispose();
    return bufferedImage;
  }
}
