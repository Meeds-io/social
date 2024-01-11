/**
 *    - This is a wrapper of selectize.js (http://selectize.github.io/selectize.js/) and jquery.mention (https://github.com/ivirabyan/jquery-mentions)
 *    - The purpose is providing a simple jquery ui widget that help to suggest user input, autocompletion with customer data provider and custom menu 
 * and tag rendering 
 * 
 *    - This plugin is aim to provide a simple, easy to use, and consistent in eXo PLF. 
 *    - It's created by jquery UI widget factory (https://jqueryui.com/widget/). So the api follow jquery UI widget api design pattern, 
 * anyone that know jquery UI widget should be familiar with this plugin
 *    
 *    - Quickstart: this sample will create an input that suggest "username" from what user type
 *                    $(input).suggester({
 *                          type: 'tag',
 *                          source: [{uid: 1, value: 'root'},
 *                                      {uid: 2, value: 'demo}]
 *                     );
 * 
 *    - Available options:
 *      
 *      type:        choose to create a mix or tag component
 *      
 *                      'tag'   - create selectize component
 *                      'mix'  -  create jquery.mention component, this is the default
 *      
 *      source:     data source of the autocomplete
 *      
 *                      array        -  array of json objects {uid: '1', value: 'test', image: '/path/to/img.png'}  s
 *   
 *      sourceProviders  - Another option besize "source". Provider can be "shared" between suggester widget instance
 *                               - this is an array of provider names ['exo:chat', 'exo:social', 'exo:task']
 *                              
 *                     $(input1).suggester({
 *                        sourceProviders: ['exo:social']
 *                     });
 *                     
 *                     //add provider
 *                     //this need 2 parameters: provider name, and the loader function --> same as the loader function of "source"
 *                     $(input1).suggester('addProvider', 'exo:social', function(term, callback) {
 *                          //query for data 
 *                          var data = findInSocial(term);                                            
 *                          
 *                          //now response
 *                          //data = [{uid: 1, value: 'root', image: '/path/to/img'}, {uid: 2, value: 'demo', image: 'path/to/avatar.png'}]
 *                          callback(data);
 *                     });
 *                     
 *                     //now reuse in other input. Dont need to add provider again
 *                     $(input2).suggester({
 *                        sourceProviders: ['exo:social']
 *                     });
 *                      
 *      renderMenuItem  - provide custom render the autocomplete menu item
 *                                 - this function receive param: item --> the current data json {uid: 1, value: 'root', image: 'path/to/img.png'}
 *                                 - this function must return the html of the menu item
 *      
 *                     //let say we want to display the avatar in difference position
 *                     $(input).suggester({
 *                        source: [{uid: 1, value: 'root', image: 'path/to/img.png'}],
 *                        
 *                         renderMenuItem: function(item) {
 *                            return '<li>' + item.value + '<img src="' + item.image + '"></img></li>'
 *                         }
 *                     });
 *                      
 *      renderItem         - provide custom render for selected item
 *                                - work the same as renderMenuItem function, receive json data item, and return html
 *                                
 *   - Available function:
 *   
 *      getValue              - return current value of the input
 *                      
 *                      $(input).suggester({
 *                          //initialize the suggester        
 *                      });                      
 *                      //get the value after user fill the input
 *                      var val = $(input).suggester('getValue');
 *      
 *      setValue                - set value of the input programatically
 *                      
 *                      $(input).suggester({
 *                          //initialize the suggester        
 *                      });                      
 *                      //set the value after user fill the input
 *                      $(input).suggester('setValue', '@root');
 *                      
 *      getSuggests          - return selected items, this is for mix suggester that when the getValue method return both selected items mixed with other text
 *      
 *      addProvider           - register provider, need 2 parameters: name, and the loader function. Lets take a look at the sample of sourceProviders
 */
(function($) {  
  var $input, $editable;
  
  var type = {
      TAG : "tag",
      MIX : "mix"
  };
  
  var providers = {};
  function loadFromProvider(term, response) {
    var p = [];    
    var _this = this; 
    
    var sourceProviders = this.settings && this.settings.sourceProviders;
    sourceProviders = sourceProviders || (this.options && this.options.sourceProviders); 
    $.each(providers, function(name, provider) {
      if ($.inArray(name, sourceProviders) != -1) {
        if (!p[name]) {
          p[p.length] = provider;
        }
      }
    });
        
    var items = [];
    var count = 0;
    var finish = function(results) {
      if (results && results.length) {
        $.each(results, function(idx, elm) {
          items[items.length] = elm;
        });
      }

      if (++count == p.length) {
        response.call(this, items);
      }
    };

    //
    for (var i = 0; i < p.length; i++) {
      var provider = p[i];
      if ($.isFunction(provider)) {
        provider.call(_this, term,  function(results) {
          finish(results);
        });
      }
    }

  }

  var App = (function() {
    function App(input) {
      this.$input = input;
    }
    return App;
  })();

  var API = {
    addProvider: function(name, provider) {
      providers[name] = provider;
    },
    getValue : function() {
      if (this.settings.type === type.TAG) {
        return this.$input[0].selectize.getValue();
      } else {
        var content = this.$editable ? this.$editable.html() : this.$input.html();
        return API.replaceMentions.call(this, content);
      }
    },
    setValue : function(val) {
      if (this.settings.type === type.TAG) {
        return this.$input[0].selectize.setValue(val);
      } else {
        $.error("Method setValue is not supported with type=mix");
      }
    },
    clearValue: function() {
      this.$input.html('');
      if (this.$editable) {
        this.$editable.html('');
      }
    },
    getSuggests: function() {
      if (this.settings.type === type.TAG) {
        return this.$input[0].selectize.getValue();
      } else {
        var content = this.$input.html();
        var at = this.settings.at;
        var mentions = [];

        var $content = $('<div>' + content + '</div>');
        $content.find('[data-atwho-at-value]').each(function() {
          var value = $(this).attr('data-atwho-at-value');
          value = value.trim();
          if (value && value.charAt(0) != at) {
            value = at + value;
          }
          mentions.push(value);
        });
        return mentions;
      }
    },
    replaceMentions: function(content) {
      if ( !this.settings || this.settings.type === type.TAG ) {
        return content;
      } else {
        var at = this.settings.at;
        var $content = $('<div>' + content + '</div>');
        $content.find('[data-atwho-at-value]').each(function() {
          var value = $(this).attr('data-atwho-at-value');
          value = value.trim();
          if (value && value.charAt(0) != at) {
            value = at + value;
          }
          // This is work-around due to sometime the mention process can not process if has the close tag right after mention like this <p>@root</p>
          $(this).replaceWith(value + ' ');
        });
        content = $content.html();
        content = content.replace(/\u00a0/ig, '');
        content = content.replace(/\u200d/ig, '');
        return content;
      }
    }
  };

  var atWhoCallback = $.fn.atwho["default"].callbacks;
  var defaultAtConfig = {
    at: "@",
    searchKey: 'value',
    acceptSpaceBar: true,
    minLen: 1,
    callbacks: {
      matcher: function(flag, subtext, should_startWithSpace, acceptSpaceBar) {
        subtext = subtext.trim();
        var _a, _y, match, regexp, space;
        flag = flag.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        if (should_startWithSpace) {
          flag = '(?:^|\\s)' + flag;
        }
        _a = decodeURI("%C3%80");
        _y = decodeURI("%C3%BF");
        space = acceptSpaceBar ? "\ " : "";
        regexp = new RegExp(flag + "([A-Za-z" + _a + "-" + _y + "0-9_" + space + "\'\.\+\-]*)$|" + flag + "([^\\x00-\\xff]*)$", 'gi');
        match = regexp.exec(subtext);
        if (match) {
          var a = match[2] || match[1];
          return a;
        } else {
          return null;
        }
      },
      sorter: function(query, items, searchKey) {
        return items;
      },
      highlighter: function(li, query) {
        li = $('<div></div>').append(li).html();
        return atWhoCallback.highlighter.call(this, li, query);
      }
    },
    functionOverrides: {
      // This method just copy from AtWho library to add some more customization
      insert: function(content, $li) {
        var suffix;
        suffix = (suffix = this.getOpt('suffix')) === "" ? suffix : suffix || "\u200D\u00A0";
        var idField = this.getOpt('idField') || 'uid';
        var data = $li.data('item-data');
        var uid = data[idField] || data.id;
        this.query.el
            .removeClass('atwho-query')
            .addClass('atwho-inserted')
            .html(content)
            .attr('data-atwho-at-query', "" + data['atwho-at'] + this.query.text)
            .attr('data-atwho-at-value', "" + uid + '')
            .attr('contenteditable', "false")
            .on('click', '.remove', function(e) {
              $(this).closest('[data-atwho-at-query]').remove();
            });
        if (range = this._getRange()) {
          if (this.query.el.length) {
            range.setEndAfter(this.query.el[0]);
          }
          range.collapse(false);
          var suffixNode;
          range.insertNode(suffixNode = this.app.document.createTextNode(suffix));
          //range.insertNode(suffixNode = this.app.document.createTextNode(" "));
          this._setRange('after', suffixNode, range);
        }
        if (!this.$inputor.is(':focus')) {
          this.$inputor.focus();
        }
        return this.$inputor.change();
      }
    }
  };

  $.fn.suggester = function(settings) {
    var _args = arguments;
    var $this = $(this);
    var app = $this.data("suggester");
    const resetExistingEditor = app && (typeof settings === 'object');
    if (resetExistingEditor && settings.avoidReset) {
      // The editor is already initialized,
      // thus we ignore new initialization
      return;
    }

    if (!app) {
      $this.data('suggester', (app = new App(this)));
    }
    var $input = $this, $editable;

    if (!(typeof settings === 'object' || !settings)) {
      if (API[settings]) {
        var result = API[settings].apply(app, Array.prototype.slice.call(_args, 1));
        return result;
      } else {
        return $.error("Method " + method + " does not exist on eXo suggester");
      }
    }

    if (!settings) settings = {};
    if (settings.providers) {
      $.each(settings.providers, function(name, provider) {
        providers[name] = provider;
      });
    }

    if (settings.type !== type.TAG) {   // Using At.js
      if (settings.callbacks == undefined) {
        settings.callbacks = {};
      }

      $editable = $input;

      settings = $.extend(true, {}, defaultAtConfig, settings);
      if (settings.iframe) {
        $editable.atwho('setIframe', settings.iframe);
      }     
      
      var source = settings.source;
      if (!(source && source.length) && settings.sourceProviders && settings.sourceProviders.length) {
        settings.source = function(query, callback) {
          var width = $(window).width();	
          if ((width < 768 && width > screen.height)
        		  && (app.$input[0].className.indexOf('cke_editable') !== -1)) {
            return;
          }         
          loadFromProvider.call(app, query, callback);
        };
      } else if ($.isArray(settings.source)){
        settings.data = source;
        settings.source = null;
      }
      settings.callbacks.remoteFilter = settings.source;

      var listItemRender = false, insertRender = false;
      if (settings.renderMenuItem) {
        var render = settings.renderMenuItem;
        if (typeof render === 'string') {
          settings.displayTpl = render;
        } else if ($.isFunction(render)){
          listItemRender = render;
        }
      };

      if (settings.renderItem) {
        var render = settings.renderItem;
        if (typeof render === 'string') {
          settings.insertTpl = render;
        } else if ($.isFunction(render)){
          insertRender = render;
        }
      } else {
        settings.insertTpl = '<span>${value}<a href="#" class="remove"><i class="uiIconClose uiIconLightGray">x</i></a></span>';
      }

      if (listItemRender || insertRender) {
        settings.callbacks.tplEval = function(tpl, item, phase) {
          var args = arguments;
          if (phase === "onDisplay" && listItemRender) {
            var $li = $('<li class="option">');
            $li.html(listItemRender.call(app, item, $li));
            return $li;
          } else if (phase == "onInsert" && insertRender){
            return insertRender.call(app, item);
          }
          return atWhoCallback.tplEval(tpl, item);
        };
      }

      app.atWho = $editable.atwho(settings);
    } else {    // Using Selectize.js
      if (!settings.valueField) {
        settings.valueField = 'uid';
      }
      if (!settings.labelField) {
        settings.labelField = 'value';
      }
      if (!settings.searchField) {
        settings.searchField = [settings.labelField];
      }
      if (!settings.optionIconField) {
        settings.optionIconField = 'icon';
      }

      if (settings.selectedItems) {
        settings.items = settings.selectedItems;
      }

      var _this = this;
      if (!(settings.source && settings.source.length) && settings.sourceProviders && settings.sourceProviders.length) {
        settings.source = loadFromProvider;
      }

      if (settings.preload) {
        settings.load = loadFromProvider;
      }

      if (settings.source) {
        var source = settings.source;
        if ($.isArray(source)) {
          settings.options = settings.source;
        } else {
          settings.options = [];
          settings.load = source;
        }
      }

      var _onItemRemove = settings.onItemRemove;
      settings.onItemRemove = function(value) {
        if (this.options[value] && this.options[value].invalid) {
          this.removeOption(value);
        }
        if (_onItemRemove) {
          _onItemRemove.call(this, value);
        }
      }

      if (settings.renderMenuItem) {
        settings.render = {
          option: function(item, escape) {
            return '<div class="option">' + settings.renderMenuItem.call(app, item, escape) + '</div>';
          }
        };
      } else {
        settings.render = {
          option: function(item, escape) {
            var thumb = item[settings.optionIconField];

            if (!thumb) {
              if (settings.defaultOptionIcon) {
                if ($.isFunction(settings.defaultOptionIcon)) {
                  thumb = settings.defaultOptionIcon.call(this, item, escape);
                } else {
                  thumb = settings.defaultOptionIcon;
                }
              }
            }

            return '<div class="option">' +
              (thumb ? '<img class="thumb" src="' + thumb + '"> ' : '') +
              escape(item[settings.labelField]) + '</div>';
          }
        };
      }

      if (settings.renderItem) {
        if (!settings.render) {
          settings.render = {};
        }
        settings.render.item = settings.renderItem;
      }
      $input.selectize(settings);
    }

    app.settings = settings;
  };

  function escapeRegExp(str) {
    var specials;
    specials = /[.*+?|()\[\]{}\\$^]/g;
    return str.replace(specials, "\\$&");
  };
  
  return $;
})($);
