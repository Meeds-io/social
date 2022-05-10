/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.service;

/**
 * Service to parse an HTML content, extract temporary uploaded files, store them in a permanent location
 * and replace URLs in the HTML content with the permanent URLs
 */

public interface HTMLUploadImageProcessor {
  /**
   * Process the given HTML content, extract temporary uploaded files
   * and replace URLs in the HTML content with the permanent URLs
   * @param content The HTML content
   * @param locationId The location to store the images.
   * @param imagesSubLocationPath The subpath of the folder under parentNode to store the images. If the nodes of this
   *                            path do not exist, they are automatically created, only if there are images to store.
   * @return The updated HTML content with the permanent images URLs
   */
  String processImages(String content, String locationId, String imagesSubLocationPath);
  /**
   * Process the given HTML content, extract temporary uploaded files
   * and replace URLs in the HTML content with the permanent URLs
   * @param content The HTML content
   * @param spaceGroupId The space group ID to store the image under it's content.
   * @param imagesSubLocationPath The subpath of the folder under parentNode to store the images. If the nodes of this
   *                            path do not exist, they are automatically created, only if there are images to store.
   * @return The updated HTML content with the permanent images URLs
   */
  String processSpaceImages(String content, String spaceGroupId, String imagesSubLocationPath);
  /**
   * Process the given HTML content, extract temporary uploaded files
   * and replace URLs in the HTML content with the permanent URLs
   * @param content The HTML content
   * @param userId The user ID to store the image under he's content.
   * @param imagesSubLocationPath The subpath of the folder under parentNode to store the images. If the nodes of this
   *                            path do not exist, they are automatically created, only if there are images to store.
   * @return The updated HTML content with the permanent images URLs
   */
  String processUserImages(String content, String userId, String imagesSubLocationPath);

  /**
   * Process the given HTML content, export Files
   * and replace URLs in the HTML content with files name
   * @param content The HTML content
 * @return The updated HTML content with the images name
   */
  String processImagesForExport(String content);

  void uploadSpaceFile(String filePath, String spaceGroupId, String fileName, String imagesSubLocationPath) ;

  void uploadUserFile(String filePath, String userId, String fileName, String imagesSubLocationPath) ;



}
