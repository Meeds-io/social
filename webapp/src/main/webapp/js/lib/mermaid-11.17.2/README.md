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

To upgrade: replace this folder by the same files of the new version, rename it, rebuild `THIRD-PARTY-LICENSES.txt` and this list from the tree the new version resolves, and change `MERMAID_VERSION` in `js/mermaidLoader.js`.
