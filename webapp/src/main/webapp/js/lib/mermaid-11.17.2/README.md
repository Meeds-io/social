# Mermaid 11.17.2 — split ES-module build (EXO-93576)

Shared by the platform as the `SHARED/mermaid` module (`js/mermaidLoader.js`), loaded on demand: the entry module and the chunks of the diagram kinds a page draws, from this webapp only, never from the internet.

- Source: the npm package `mermaid@11.17.2`, `dist/mermaid.esm.min.mjs` and `dist/chunks/mermaid.esm.min/*.mjs`, unchanged but for the removed `sourceMappingURL` comments (the source maps are not shipped).
- Package integrity: `sha512-V6K3C8EBdEsPFZXSKMJe6ppQOENxuHARr9GvHX4hh47lAbhMRD9qf4oEK7LoaRQxULMa80/qt5gHO73aCleBBg==`
- Licence: MIT, `LICENSE` (Mermaid). The build bundles Mermaid's dependencies (`package.json` `dependencies` of 11.17.2): @braintree/sanitize-url, @iconify/utils, @mermaid-js/parser, @upsetjs/venn.js, cytoscape, cytoscape-cose-bilkent, cytoscape-fcose, d3, d3-sankey, dagre-d3-es, dayjs, dompurify, es-toolkit, fastdom, katex, khroma, marked, roughjs, stylis, ts-dedent, uuid.

To upgrade: replace this folder by the same files of the new version, rename it, and change `MERMAID_VERSION` in `js/mermaidLoader.js`.
