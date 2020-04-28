/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.search.service.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class PeopleSearchConnector extends SearchServiceConnector {

  public PeopleSearchConnector(InitParams initParams) {
    super(initParams);
    // TODO Auto-generated constructor stub
  }

  @Override
  public Collection<SearchResult> search(SearchContext context,
                                         String query,
                                         Collection<String> sites,
                                         int offset,
                                         int limit,
                                         String sort,
                                         String order) {
    List<SearchResult> results = new ArrayList<SearchResult>(); 
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
    
    Date createDate = null;
    try {
      createDate = sdf.parse("2013/03/29");
    } catch (ParseException e) {
      // TODO Auto-generated catch block
    }
    
    //
    SearchResult result1 = new SearchResult(
        "/profile/root",
        "root",
        "admin",
        "root@localhost",
        "eXoSkin/skin/images/system/UserAvtDefault.png",
        createDate.getTime(),
        1000);
    results.add(result1);
    
    Date johnCreateDate = null;
    try {
      johnCreateDate = sdf.parse("2013/04/01");
    } catch (ParseException e) {
      // TODO Auto-generated catch block      
    }
    SearchResult result2 = new SearchResult(
       "/profile/john",
       "john",
       "manager",
       "john@localhost",
       "eXoSkin/skin/images/system/UserAvtDefault.png",
       johnCreateDate.getTime(),
       900);
   results.add(result2);
                                       
    
    return results;
  }


}