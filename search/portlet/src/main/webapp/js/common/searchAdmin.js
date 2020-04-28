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
(function($){
function initSearchAdmin() {

    const i18NData = {};
    const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
    const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.searchadministration.searchadministration-${lang}.json`;
    fetch(url, {
      method: 'get',
      credentials: 'include'
    })
      .then(resp => resp && resp.ok && resp.json())
      .then(data => data && Object.assign(i18NData, data))
      .then(() => {
        $.getJSON("/portal/rest/search/registry", function(registry){
          var row_template =
            "<tr>" +
              "<td>%{displayName}</td>" +
              "<td>%{description}</td>" +
              "<td class='center'>" +
                "<input type='button' class='btn btn-mini contentType' id='%{id}' name='Enable' value='%{key}'>" +
              "</td>" +
            "</tr>";

          var connectors = registry[0];
          var searchTypes = registry[1];
          $.each(connectors, function(searchType, connector){
            $("#searchAdmin table").append(row_template.replace(/%{id}/g, connector.searchType).replace(/%{key}/g,i18NData['SearchAdmin.action.Enable'])
                                                      .replace(/%{displayName}/g, i18NData["SearchAdmin.type." + connector.displayName ])
                                                      .replace(/%{description}/g,  i18NData["SearchAdmin.type." + connector.displayName + ".description"]));
          });

          $.each(searchTypes, function(i, type){
            $(".contentType#"+type).val(i18NData["SearchAdmin.action.Disable"]);
            $(".contentType#"+type).attr("name","Disable");

            //$(".ContentType#"+type).next().attr("disabled", false);
          });
        });
      });

    $('body').on('click', '.contentType', function() {
      if("Enable"==$(this).attr("name")) {
        $(this).attr("name","Disable");
        $(this).val(i18NData["SearchAdmin.action.Disable"]);
        //$(this).next().attr("disabled", false);
      } else {
        $(this).attr("name","Enable");
        $(this).val(i18NData["SearchAdmin.action.Enable"]);
        //$(this).next().attr("disabled", true);
      }

      var enabledTypes = [];
      $.each($(".contentType"), function(){
        if("Disable"== this.name) enabledTypes.push(this.id);
      });

      $.ajax({
        url: '/portal/rest/search/enabled-searchtypes/' + enabledTypes,
        method: 'POST',
        data: {
          searchTypes: enabledTypes.join(",")
        },
        complete: function (data) {
          if ("ok" == data.responseText) {
            console.log("Search setting has been saved succesfully.");
          } else {
            alert("Problem occurred when saving your setting: " + data.responseText);
          }
        }
      });
    });
	}


  initSearchAdmin();

})($);
