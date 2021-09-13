/**
 * Component displaying a popup with the space info and some actions.
 */
(function ($) {
    $.fn.spacePopup = function (options) {
        var defaults = {
            restURL: "",
            labels: "",
            getContentFunc: function() {},
            activation: "hover",
            keepAlive: false,
            maxWidth: "200px",
            edgeOffset: 3,
            defaultPosition: "bottom",
            delay: 400,
            fadeIn: 200,
            fadeOut: 200,
            attribute: "title",
            content: false,
            enter: function () {},
            exit: function () {}
        };
        var opts = $.extend(defaults, options);
        opts.labels = Object.assign(opts.labels || {}, Vue.prototype.$spacePopupLabels)
        if ($("#tiptip_holder").length <= 0) {
            var tiptip_holder = $('<div id="tiptip_holder" style="max-width:' + opts.maxWidth + ';"></div>');
            var tiptip_content = $('<div id="tiptip_content"></div>');
            var tiptip_arrow = $('<div id="tiptip_arrow"></div>');
            $("body").append(tiptip_holder.html(tiptip_content).prepend(tiptip_arrow.html('<div id="tiptip_arrow_inner"></div>')))
        } else {
            var tiptip_holder = $("#tiptip_holder");
            var tiptip_content = $("#tiptip_content");
            var tiptip_arrow = $("#tiptip_arrow")
        }
        return this.each(function () {
            var org_elem = $(this);

            if (opts.content) {
                var org_title = opts.content
            } else {
                var org_title = org_elem.attr(opts.attribute)
            }
            if (org_title != "") {
                if (!opts.content) {
                    org_elem.removeAttr(opts.attribute)
                }
                var timeout = false;
                if (opts.activation == "hover") {
                    org_elem.hover(function () {

                        //
                        loadData($(this));

                        clearTimeout($(this).data('timeoutId'));

                        active_tiptip()
                    }, function () {
                        if (!opts.keepAlive) {
                            deactive_tiptip()
                        }
                        //
                        var $this = $(this);
                        var timeoutId = setTimeout(function(){
                            if(!tiptip_holder.is(':hover')) {
                                deactive_tiptip();
                            }
                        }, 250);
                        $this.data('timeoutId', timeoutId);
                    });
                    if (opts.keepAlive) {
                        tiptip_holder.hover(function () {}, function () {
                            deactive_tiptip()
                        })
                    }
                } else if (opts.activation == "focus") {
                    org_elem.focus(function () {
                        active_tiptip()
                    }).blur(function () {
                        deactive_tiptip()
                    })
                } else if (opts.activation == "click") {
                    org_elem.click(function () {
                        active_tiptip();
                        return false
                    }).hover(function () {}, function () {
                        if (!opts.keepAlive) {
                            deactive_tiptip()
                        }
                    });
                    if (opts.keepAlive) {
                        tiptip_holder.hover(function () {}, function () {
                            deactive_tiptip()
                        })
                    }
                }
                function active_tiptip() {
                    opts.enter.call(this);
                    //tiptip_content.html(org_title);
                    tiptip_holder.hide().removeAttr("class").css("margin", "0");
                    tiptip_arrow.removeAttr("style");
                    var top = parseInt(org_elem.offset()['top']);
                    var left = parseInt(org_elem.offset()['left']);
                    var org_width = parseInt(org_elem.outerWidth(true));
                    var org_height = parseInt(org_elem.outerHeight(true));
                    var tip_w = tiptip_holder.outerWidth(true);
                    var tip_h = tiptip_holder.outerHeight(true);
                    var w_compare = Math.round((org_width - tip_w) / 2);
                    var h_compare = Math.round((org_height - tip_h) / 2);
                    var marg_left = Math.round(left + w_compare);
                    var marg_top = Math.round(top + org_height + opts.edgeOffset);
                    var t_class = "";
                    var arrow_top = "";
                    var arrow_left = Math.round(tip_w - 12) / 2;
                    if (opts.defaultPosition == "bottom") {
                        t_class = "_bottom"
                    } else if (opts.defaultPosition == "top") {
                        t_class = "_top"
                    } else if (opts.defaultPosition == "left") {
                        t_class = "_left"
                    } else if (opts.defaultPosition == "right") {
                        t_class = "_right"
                    }
                    var right_compare = (w_compare + left) < parseInt($(window).scrollLeft());
                    var left_compare = (tip_w + left) > parseInt($(window).width());
                    if ((right_compare && w_compare < 0) || (t_class == "_right" && !left_compare) || (t_class == "_left" && left < (tip_w + opts.edgeOffset + 5))) {
                        t_class = "_right";
                        arrow_top = Math.round(tip_h - 13) / 2;
                        arrow_left = -12;
                        marg_left = Math.round(left + org_width + opts.edgeOffset);
                        marg_top = Math.round(top + h_compare)
                    } else if ((left_compare && w_compare < 0) || (t_class == "_left" && !right_compare)) {
                        t_class = "_left";
                        arrow_top = Math.round(tip_h - 13) / 2;
                        arrow_left = Math.round(tip_w);
                        marg_left = Math.round(left - (tip_w + opts.edgeOffset + 5));
                        marg_top = Math.round(top + h_compare)
                    }
                    var top_compare = (top + org_height + opts.edgeOffset + tip_h + 8) > parseInt($(window).height() + $(window).scrollTop());
                    var bottom_compare = ((top + org_height) - (opts.edgeOffset + tip_h + 8)) < 0;
                    if (top_compare || (t_class == "_bottom" && top_compare) || (t_class == "_top" && !bottom_compare)) {
                        if (t_class == "_top" || t_class == "_bottom") {
                            t_class = "_top"
                        } else {
                            t_class = t_class + "_top"
                        }
                        arrow_top = tip_h;
                        marg_top = Math.round(top - (tip_h + 5 + opts.edgeOffset))
                    } else if (bottom_compare | (t_class == "_top" && bottom_compare) || (t_class == "_bottom" && !top_compare)) {
                        if (t_class == "_top" || t_class == "_bottom") {
                            t_class = "_bottom"
                        } else {
                            t_class = t_class + "_bottom"
                        }
                        arrow_top = -12;
                        marg_top = Math.round(top + org_height + opts.edgeOffset)
                    }
                    if (t_class == "_right_top" || t_class == "_left_top") {
                        marg_top = marg_top + 5
                    } else if (t_class == "_right_bottom" || t_class == "_left_bottom") {
                        marg_top = marg_top - 5
                    }
                    if (t_class == "_left_top" || t_class == "_left_bottom") {
                        marg_left = marg_left + 5
                    }
                    tiptip_arrow.css({
                        "margin-left": arrow_left + "px",
                        "margin-top": arrow_top + "px"
                    });
                    tiptip_holder.css({
                        "margin-left": marg_left + "px",
                        "margin-top": marg_top + "px"
                    }).attr("class", "tip" + t_class);
                    if (timeout) {
                        clearTimeout(timeout)
                    }
                    timeout = setTimeout(function () {
                        tiptip_holder.stop(true, true).fadeIn(opts.fadeIn)
                    }, opts.delay)
                }
                function deactive_tiptip() {
                    opts.exit.call(this);
                    if (timeout) {
                        clearTimeout(timeout)
                    }
                    tiptip_holder.fadeOut(opts.fadeOut)
                }

                function loadData(el) {
                    var spaceUrl = $(el).attr('href');
                    var spaceId= opts.spaceID;
                    var spaceRestUrl = opts.restURL.replace('{0}', window.encodeURI(spaceId));
                    var membersRestUrl = opts.membersRestURL.replace('{0}', window.encodeURI(spaceId));
                    var managerRestUrl = opts.managerRestUrl.replace('{0}', window.encodeURI(spaceId));

                    //
                    initPopup();

                    //
                    var cachingData = getCache(spaceId);

                    if ( cachingData ) {
                        buildPopup(cachingData, spaceUrl);
                    } else {
                        if (window.profileXHR && window.profileXHR.abort) {
                            window.profileXHR.abort();
                        }
                        window.profileXHR = $.ajax({
                            type: "GET",
                            url: spaceRestUrl,
                            complete: function(jqXHR) {

                              if (jqXHR.readyState === 4) {
                                var spaceData = $.parseJSON(jqXHR.responseText);
                                var membersData = null;
                                var managerData = null;
                                var membership = null;
                                if (!spaceData) {
                                  return;
                                }
                                window.profileXHR = $.ajax({
                                  type: "GET",
                                  url: membersRestUrl,
                                  complete: function(jqXHR) {

                                    if (jqXHR.readyState === 4) {
                                      membersData = $.parseJSON(jqXHR.responseText);
                                    }
                                    window.profileXHR = $.ajax({
                                      type: "GET",
                                      url: managerRestUrl,
                                      complete: function(jqXHR) {
                                        if (jqXHR.readyState === 4) {
                                          managerData = $.parseJSON(jqXHR.responseText);
                                        }
                                        var rolesArray = {
                                          roles: []
                                        };
                                        var currentUser = membersData.users.find(user => {
                                          return user.username === eXo.env.portal.userName
                                        })
                                        if (currentUser.isManager){
                                          rolesArray.roles.push("manager");
                                        }
                                        if (currentUser.isMember){
                                          rolesArray.roles.push("member");
                                        }

                                        if(!spaceData.avatarUrl){
                                          spaceData.avatarUrl= opts.defaultAvatarUrl;
                                        }

                                        spaceData.member = membersData.size;
                                        spaceData.manager = managerData.size;
                                        spaceData.membership = rolesArray;

                                        buildPopup(spaceData, spaceUrl);
                                        putToCache(spaceId, spaceData);

                                      }
                                    });
                                  }
                                });
                              }
                            }
                        });
                    }
                }

                function initPopup() {
                    var profile_popup = $('<div/>', {
                        "id": "profile-popup",
                        "class": "profile-popup",
                        "height": "100px"
                    });

                    var loadingIndicator = $('<div/>', {
                        "id": "loading-indicator"
                    });
                    var loadingText = $('<div/>', {
                        "id": "loading-text",
                        "text": "" + opts.labels.StatusTitle
                    });

                    $('#tiptip_content').find('div.loading-indicator').remove();
                    for (var i=1; i < 9; i++) {
                        loadingIndicator.append($('<div id="rotateG_0' + i + '" class="blockG"></div>'));
                    }

                    profile_popup.append(loadingIndicator);
                    profile_popup.append(loadingText);

                    tiptip_content.html(profile_popup);
                }

                function buildPopup(json, spaceUrl) {
                    var action = null;
                    var isManager = false;
                    var isMember = false;
                    var labels = opts.labels;
                    var spaceName = json.prettyName;
                    if(json.membership.roles.indexOf('manager')>=0) {
                        isManager = true;
                    }

                    if(json.membership.roles.indexOf('member')>=0) {
                        isMember = true;
                    }

                    tiptip_content.empty();

                    if ((isManager && json.manager >= 1)|| (!isManager && isMember)) {
                        action = $('<div/>', {
                            "class": "btn-link leave-space",
                            "text": "" + labels.leave,
                            "data-action": spaceName+":" + opts.userName+":member",
                            "onclick": "executeAction(this)"
                        });
                    }

                    if (!isMember) {
                        action = $('<div/>', {
                            "class": "btn-link join-space",
                            "text": "" + labels.join,
                            "data-action": spaceName+":" + opts.userName+":member",
                            "onclick": "executeAction(this)"
                        });
                    }

                    var popupContentContainer = $("<div/>");
                    var popupContent = $("<table/>", {
                        "id":"tipName"
                    });
                    var tbody = $("<tbody/>");
                    var tr = $("<tr/>");
                    var tdAvatar = $("<td/>", {
                        "width":"50px",
                        "valign" : "top"
                    });
                    var img = $("<img/>", {
                        "class": "tiptip-space-avatar",
                        "src":json.avatarUrl
                    });

                    var aAvatar = $("<a/>", {
                        "target":"_self",
                        "href":spaceUrl
                    });

                    tdAvatar.append(aAvatar.append(img));

                    var tdProfile = $("<td/>",{
                        "id": "profileName"
                    });
                    var aProfile = $("<a/>", {
                        "target":"_self",
                        "href":spaceUrl,
                        "text":json.displayName
                    });

                    tdProfile.append(aProfile);
                    if(json.member > 0){
                        var divMembersCount = $("<div/>", {
                            "class" : "space-members",
                        });
                        divMembersIcon = $("<i/>", {
                            "class" : "space-members-icon uiIconMembers",
                        });
                        divMembersSpan = $("<span/>", {
                            "class" : "space-members-count",
                            "font-weight":"normal",
                            "text":json.member + " " + opts.labels.members
                        });
                        divMembersCount.append(divMembersIcon);
                        divMembersCount.append(divMembersSpan);
                    } else {
                        divMembersCount = $("<div/>");
                    }
                    tdProfile.append(divMembersCount);

                    if (json.description) {
                        var divDescription = $('<div/>', {
                            "id": "description",
                            "text": json.description.replace(/<[^>]+>/g, '')
                        });
                    }
                    if(divDescription){
                        tdProfile.append(divDescription);
                    }

                    tr.append(tdAvatar).append(tdProfile);

                    tbody.append(tr);


                    popupContent.append(tbody);

                    popupContentContainer.append(popupContent);
                    if (action) {
                        var divUIAction = $("<div/>", {
                            "class": "uiAction connectAction isSpace"
                        }).append(action);

                        if(eXo.social && eXo.social.tiptip && eXo.social.tiptip.extraActions) {
                            for (var index = 0; index < eXo.social.tiptip.extraActions.length; index++) {
                                var extraAction = eXo.social.tiptip.extraActions[index];
                                if(extraAction.appendContentTo && (!extraAction.test || extraAction.test(popupContentContainer))) {
                                    extraAction.appendContentTo(divUIAction, spaceName, 'space-name');
                                }
                            }
                        }
                        popupContentContainer.append(divUIAction);
                    }

                    tiptip_content.html(popupContentContainer.html());
                }

                function executeAction(el) {
                    var thisTip = $(el).parents('div#tiptip_content:first');
                    var tipName = thisTip.find('table#tipName:first');
                    var spaceUrl = tipName.find('a:first').attr('href');


                    var dataAction = $(el).attr('data-action');
                    var spaceName = dataAction.split(":")[0];
                    var userName = dataAction.split(":")[1];
                    var role =  dataAction.split(":")[2];

                    if (window.profileActionXHR && window.profileActionXHR.abort) {
                        window.profileActionXHR.abort();
                    }
                    window.profileActionXHR = $.ajax({
                        type: "DELETE",
                        url: opts.deleteMembershipRestUrl.replace('{0}', spaceName).replace('{1}', userName).replace('{2}', role),
                        success: function (jqXHR) {
                            var popup = $(el).closest('#tiptip_holder');
                            popup.fadeOut('fast', function () {
                            });
                            if($(org_elem).data('link')) {
                                var actionLink = $(org_elem).data('link').replace('javascript:', '');
                                $.globalEval(actionLink);
                            }
                            // clear cache
                            clearCache();
                            document.dispatchEvent(new CustomEvent('space_membership_updated', {'detail': spaceName}));
                        }
                    });
                }

                function putToCache(key, data) {
                    var ojCache = $('div#socialSpaceData');
                    if (ojCache.length == 0) {
                        ojCache = $('<div id="socialSpaceData"></div>').appendTo($(document.body));
                        ojCache.hide();
                    }
                    key = 'result' + ((key === ' ') ? '_20' : key);
                    var datas = ojCache.data("CacheSearch");
                    if (String(datas) === "undefined") datas = {};
                    datas[key] = data;
                    ojCache.data("CacheSearch", datas);
                }

                function getCache(key) {
                    key = 'result' + ((key === ' ') ? '_20' : key);
                    var datas = $('div#socialSpaceData').data("CacheSearch");
                    return (String(datas) === "undefined") ? null : datas[key];
                }

                function clearCache() {
                    $('div#socialSpaceData').stop().animate({
                        'cursor':'none'
                    }, 1000, function () {
                        $(this).data("CacheSearch", {});
                    });
                }
                window.executeAction = executeAction;
            }
        })
    }
})(jQuery);
