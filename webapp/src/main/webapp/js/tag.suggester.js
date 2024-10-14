(function($) {
  const atWhoCallback = $.fn.atwho["default"].callbacks;
  const providers = {};

  let tagSearchCached = {};
  let lastNoResultQuery;

  class TagSuggesterApp {
    constructor(input) {
      this.$input = input;
    }
  }

  function loadFromProvider(query, callback) {
    if (lastNoResultQuery && query && query.includes(lastNoResultQuery)) {
      return callback.call(this, [{
        name: query,
      }]);
    }
    if (tagSearchCached[query]) {
      return callback.call(this, tagSearchCached[query]);
    } else {
      tagSearchCached[query] = [{
        name: query,
      }];
      callback.call(this, tagSearchCached[query]);
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/tags?q=${query || ''}&limit=5`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error('Response code indicates a server error', resp);
        } else {
          return resp.json();
        }
      }).then(data => {
        let result = tagSearchCached[query];
        for (let d of data) {
          if (query !== d.name) {
            result.push({
              name: d.name,
            });
          }
        }
        if (data.length == 0) {
          lastNoResultQuery = query;
        } else {
          lastNoResultQuery = false;
        }
        return callback.call(this, result);
      }).catch(() => {
        const result = [{
          name: query,
        }];
        lastNoResultQuery = query;
        tagSearchCached[query] = result;
        callback.call(this, result);
      });
    }
  }

  let defaultAtConfig = {
    at: "#",
    suffix: '\u00A0',
    searchKey: 'name',
    acceptSpaceBar: true,
    maxLen: 25,
    minLen: 0,
    callbacks: {
      matcher: function(flag, subtext) {
        let _a, _y, match, regexp;
        flag = flag.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        _a = decodeURI("%C3%80");
        _y = decodeURI("%C3%BF");
        regexp = new RegExp(flag + "([A-Za-z" + _a + "-" + _y + "0-9_\'\.\+\-]*)$|" + flag + "([^\\x00-\\xff]*)$", 'gi');
        match = regexp.exec(subtext);
        if (match) {
          return match[2] || match[1];
        } else {
          return null;
        }
      },
      highlighter: function(li, query) {
        li = $('<div></div>').append(li).html();
        return atWhoCallback.highlighter.call(this, li, query);
      }
    },
    functionOverrides: {
      insert: function(content) {
        let element = this.query.el;
        element
          .removeClass('atwho-query')
          .html(content)
          .attr('contenteditable', "false");
        const parentElement = element.parent();
        if (parentElement.hasClass('metadata-tag')) {
          parentElement.html(element.html());
          element = parentElement;
        }
        let range = this._getRange();
        if (range) {
          if (element.length) {
            range.setEndAfter(element[0]);
          }
          range.collapse(false);
          if (element !== parentElement) {
            let suffix;
            suffix = (suffix = this.getOpt('suffix')) === "" ? suffix : suffix || "\u200D\u00A0";
            const suffixNode = this.app.document.createTextNode(suffix);
            range.insertNode(suffixNode);
            this._setRange('after', suffixNode, range);
          }
        }
        if (!this.$inputor.is(':focus')) {
          this.$inputor.focus();
        }
        return this.$inputor.change();
      },
    }
  };

  $.fn.tagSuggester = function(settings) {
    let $this = $(this);
    let app = $this.data("tagSuggester");
    const resetExistingEditor = app && (typeof settings === 'object');
    if (resetExistingEditor && settings.avoidReset) {
      // The editor is already initialized,
      // thus we ignore new initialization
      return;
    }
    if (!app) {
      app = new TagSuggesterApp(this);
      $this.data('tagSuggester', app);

      // Clear cache for new CKEDitor instances
      tagSearchCached = {};
      lastNoResultQuery = null;
    }

    if (!settings) settings = {};
    if (settings.providers) {
      $.each(settings.providers, function(name, provider) {
        providers[name] = provider;
      });
    }
    if (!settings.callbacks) {
      settings.callbacks = {};
    }

    settings = $.extend(true, {}, defaultAtConfig, settings);
    settings.callbacks.remoteFilter = (query, callback) => loadFromProvider.call(app, query, callback);
    settings.callbacks.tplEval = function(tpl, item, phase) {
      if (phase === "onDisplay") {
        return $(`<li class="option"><span>#${item.name}<span></li>`);
      } else if (phase == "onInsert") {
        return $(`<a class="metadata-tag">#${item.name}</a>`);
      } else {
        return atWhoCallback.tplEval(tpl, item);
      }
    };
    app.settings = settings;
    window.setTimeout(() => {
      app.atWho = $this.atwho(app.settings);
    }, 200);
  };

  return $;
})($);
