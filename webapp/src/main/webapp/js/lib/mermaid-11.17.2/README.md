# Mermaid 11.17.2 — split ES-module build (EXO-93576)

Shared by the platform as the `SHARED/mermaid` module (`js/mermaidLoader.js`), loaded on demand: the entry module and the chunks of the diagram kinds a page draws, from this webapp only, never from the internet.

- Source: the npm package `mermaid@11.17.2`, `dist/mermaid.esm.min.mjs` and `dist/chunks/mermaid.esm.min/*.mjs`, unchanged but for the removed `sourceMappingURL` comments (the source maps are not shipped).
- Package integrity: `sha512-V6K3C8EBdEsPFZXSKMJe6ppQOENxuHARr9GvHX4hh47lAbhMRD9qf4oEK7LoaRQxULMa80/qt5gHO73aCleBBg==`
- Licence: MIT, `LICENSE` (Mermaid). The build bundles parts of the packages Mermaid depends on, every one under a permissive licence; their licence texts are in `THIRD-PARTY-LICENSES.txt`. The tree `npm install mermaid@11.17.2` resolves holds 113 packages, Mermaid included:
  - MIT: 71, Mermaid and, among the others, @braintree/sanitize-url, @iconify/utils, @mermaid-js/parser, @upsetjs/venn.js, cytoscape and its cose-bilkent and fcose layouts, dagre-d3-es, dayjs, es-toolkit, fastdom, katex, khroma, lodash-es, marked, roughjs, stylis, ts-dedent and uuid;
  - ISC: 33, d3 and its modules, delaunator and internmap;
  - BSD-3-Clause: 6, d3-sankey with the d3-array 2, d3-path 1 and d3-shape 1 it pins, d3-ease and rw;
  - Apache-2.0: @chevrotain/types;
  - Unlicense: robust-predicates;
  - DOMPurify: MPL-2.0 or Apache-2.0, at the user's choice; the platform takes it under Apache-2.0.
- Bundled versions: the tree above is what npm resolves from Mermaid's ranges today; Mermaid's dist was built with its own versions. Those the dist names are DOMPurify 3.4.12 (the tree has 3.4.17), cytoscape 3.34.0 (3.34.4), KaTeX 0.16.47 and lodash-es 4.18.1 (the same). The dist's own banner for DOMPurify 3.4.12 states the same two licences; cytoscape differs by a patch release.

- Page integration: the build bundles UMD libraries (fastdom, dayjs plugins, cose-bilkent and others) that hand themselves to a global AMD `define` when one exists instead of exporting themselves, and every platform page has RequireJS's. `js/mermaidLoader.js` therefore loads and draws behind a guard that hides `define` from module code (a classic script still sees it; where `define` is not a configurable property of the window, it is unset for the call) and restores it after; a caller never imports the files directly.

To upgrade: replace this folder by the same files of the new version, rename it, rebuild `THIRD-PARTY-LICENSES.txt` and this list from the tree the new version resolves, and change `MERMAID_VERSION` in `js/mermaidLoader.js`.
