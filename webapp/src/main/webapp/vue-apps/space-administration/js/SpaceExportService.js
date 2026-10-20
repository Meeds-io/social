/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/**
 * The Excel export of the Manage Spaces list, built in the browser.
 *
 * The rows are collected by the root's bulk loop, one space at a time, with
 * its processing alert, counter and Cancel link; the workbook is written in
 * the loop's finalisation step, while the page is still held in progress.
 * Everything runs on the root instance and this module: the bulk bar, and
 * the Export button with it, is destroyed as soon as the loop clears the
 * selection.
 *
 * A column is {key, title, type, value(space)}, type being 'string',
 * 'number', 'date' (epoch milliseconds, 0 for none) or 'link'
 * ({text, hyperlink}). A 'table-column' extension of the
 * 'spaces-administration' registry contributes a column when it declares
 * `export: {type, value(space, root), titleKey}`: a null value means the
 * column is unavailable to the viewer and drops it, a rejection aborts the
 * export.
 */

const I18N_PREFIX = 'social.spaces.administration.manageSpaces';

const PROCESSING_LABELS = {
  line1: `${I18N_PREFIX}.exportInProgress`,
  line2: `${I18N_PREFIX}.exportPleaseWait`,
  line3: `${I18N_PREFIX}.exportDoNotClosePage`,
};

const EXPORT_ALERT_COMPONENT = 'spaces-administration-export-alert';

const EXCEL_JS_MODULE = 'SHARED/exceljs';

const EXCEL_MIME_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

const EXCEL_DATE_FORMAT = 'dd mmm yyyy';

const EXCEL_LINK_FONT = {
  color: {
    argb: 'FF0563C1',
  },
  underline: true,
};

// Excel refuses a cell text longer than 32 767 characters
const EXCEL_MAX_TEXT_LENGTH = 32000;

// Characters, length and names Excel refuses for a sheet
const EXCEL_SHEET_NAME_FORBIDDEN_CHARS = /[[\]:*?/\\]/g;

const EXCEL_SHEET_NAME_MAX_LENGTH = 31;

const EXCEL_SHEET_NAME_RESERVED = 'history';

const DEFAULT_SHEET_NAME = 'Spaces';

// The headers and the short values are centered; the space names and the
// text values that can be long are aligned left
const LEFT_ALIGNED_COLUMNS = ['spaceName', 'template', 'admins', 'sync', 'access'];

const EXCEL_CENTER_ALIGNMENT = {
  horizontal: 'center',
};

const EXCEL_LEFT_ALIGNMENT = {
  horizontal: 'left',
};

// Room left around a header in its column
const COLUMN_HEADER_PADDING = 4;

const COLUMN_WIDTHS = {
  string: 20,
  number: 15,
  date: 15,
  link: 40,
};

// The page alerts open 500 ms after they are asked for: the export stays in
// progress long enough for its processing alert to be seen, a one-space
// export included
const MIN_PROGRESS_DURATION = 1500;

// The export whose alert is the latest one raised: a new export makes the
// alerts of a previous one inert
let currentExportRun = 0;

// The file of the last export, downloadable while its alert is displayed
let currentExport = null;

window.addEventListener('pagehide', event => {
  if (!event.persisted) {
    revokeCurrentExport();
  }
});

/**
 * Exports the selected spaces, or every space matching the current filter,
 * through the root's bulk loop, then displays the alert carrying the
 * Download link. A Cancel produces no file; a failure produces no file and
 * displays the error alert.
 *
 * @param {Object} root the Manage Spaces root Vue instance
 * @returns {Promise} resolved when the export is over, never rejected
 */
export async function exportSpaces(root) {
  revokeCurrentExport();
  const run = ++currentExportRun;
  const startTime = Date.now();
  const columns = getColumns(root);
  const rows = [];
  let failed = false;
  let blob = null;
  const result = await root.applyOperationInBulk(
    async (space, _params, {run: bulkRun, stop}) => {
      try {
        rows.push(await buildRow(space, columns));
      } catch (e) {
        // A row that cannot be built aborts the export: no partial file.
        // A failure that arrives after a Cancel is no failure of the export
        if (root.bulkRun === bulkRun && root.isBulkProcessing) {
          failed = true;
          stop();
        }
        throw e;
      }
    },
    null,
    () => {
      if (run === currentExportRun && blob) {
        currentExport = {
          url: window.URL.createObjectURL(blob),
          fileName: getFileName(),
        };
        displayDone(root);
      }
    },
    null,
    {
      labels: PROCESSING_LABELS,
      total: root.allSpacesSelected ? root.spacesSize : root.selectedSpaces.length,
      finalize: async () => {
        if (rows.length !== root.processedSpaces) {
          throw new Error(`${rows.length} rows collected for ${root.processedSpaces} spaces processed`);
        }
        const ExcelJS = await loadExcelJS();
        blob = await createWorkbookBlob(ExcelJS, columns, rows, root.$t(`${I18N_PREFIX}.exportSheetName`));
        const remainingTime = startTime + MIN_PROGRESS_DURATION - Date.now();
        if (remainingTime > 0) {
          await new Promise(resolve => window.setTimeout(resolve, remainingTime));
        }
      },
    });
  if (run === currentExportRun && (failed || result.status === 'failed')) {
    displayFailed();
  }
}

export function getFileName(date) {
  const now = date || new Date();
  const pad = n => String(n).padStart(2, '0');
  return `manage-spaces-${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}.xlsx`;
}

export function getColumns(root) {
  const t = key => root.$t(`${I18N_PREFIX}.${key}`);
  const contributedColumns = (root.tableColumnExtensions || [])
    .filter(extension => typeof extension.export?.value === 'function')
    .map(extension => ({
      key: extension.name,
      title: root.$t(extension.export.titleKey || extension.titleKey),
      type: extension.export.type || 'string',
      value: space => extension.export.value(space, root),
      contributed: true,
      dropped: false,
    }));
  return [{
    key: 'spaceName',
    title: t('exportColumn.spaceName'),
    type: 'link',
    value: space => ({
      text: space.displayName || '',
      hyperlink: getSpaceUrl(space),
    }),
  },
  ...contributedColumns,
  {
    key: 'template',
    title: t('template'),
    type: 'string',
    value: space => root.spaceTemplates?.find?.(template => template.id === Number(space.templateId))?.name || '-',
  }, {
    key: 'admins',
    title: t('admins'),
    type: 'string',
    value: space => getManagerNames(space, root.collator).join(', ') || '-',
  }, {
    key: 'numberOfUsers',
    title: t('exportColumn.numberOfUsers'),
    type: 'number',
    value: space => space.membersCount || 0,
  }, {
    key: 'sync',
    title: t('bindingStatus'),
    type: 'string',
    value: space => (space.hasBindings ? root.$t(`${I18N_PREFIX}.bindingStatus.tooltip`, {0: space.totalBoundUsers || 0}) : '-'),
  }, {
    key: 'access',
    title: t('access'),
    type: 'string',
    value: space => getLabel(root, 'registration', space.subscription),
  }, {
    key: 'visibility',
    title: t('visibility'),
    type: 'string',
    value: space => getLabel(root, 'visibility', space.visibility),
  }, {
    key: 'creationDate',
    title: t('exportColumn.creationDate'),
    type: 'date',
    value: space => Number(space.createdTime) || 0,
  }];
}

/**
 * @param {Object} space a space of the list
 * @param {Array} columns the export columns
 * @returns {Promise<Array>} the cell values, in columns order. A contributed
 *          column whose value is null is dropped from the file
 */
export async function buildRow(space, columns) {
  const values = await Promise.all(columns.map(column => column.value(space)));
  columns.forEach((column, index) => {
    if (column.contributed && values[index] === null) {
      column.dropped = true;
    }
  });
  return values;
}

export async function createWorkbookBlob(ExcelJS, columns, rows, sheetName) {
  const exportedIndexes = columns.map((column, index) => (column.dropped ? -1 : index)).filter(index => index >= 0);
  const exportedColumns = exportedIndexes.map(index => columns[index]);
  const workbook = new ExcelJS.Workbook();
  const sheet = workbook.addWorksheet(getSheetName(sheetName));
  sheet.columns = exportedColumns.map(column => ({
    header: column.title,
    key: column.key,
    // Wide enough for its header, in every language
    width: Math.max(COLUMN_WIDTHS[column.type] || COLUMN_WIDTHS.string, String(column.title || '').length + COLUMN_HEADER_PADDING),
  }));
  sheet.getRow(1).font = {
    bold: true,
  };
  sheet.getRow(1).alignment = EXCEL_CENTER_ALIGNMENT;
  rows.forEach(values => {
    const row = sheet.addRow(exportedIndexes.map(index => toCellValue(columns[index], values[index])));
    exportedColumns.forEach((column, index) => {
      const cell = row.getCell(index + 1);
      cell.alignment = isLeftAligned(column, cell.value) ? EXCEL_LEFT_ALIGNMENT : EXCEL_CENTER_ALIGNMENT;
      if (column.type === 'date') {
        cell.numFmt = EXCEL_DATE_FORMAT;
      } else if (column.type === 'link' && cell.value?.hyperlink) {
        cell.font = EXCEL_LINK_FONT;
      }
    });
  });
  const buffer = await workbook.xlsx.writeBuffer();
  return new Blob([buffer], {
    type: EXCEL_MIME_TYPE,
  });
}

export function getSheetName(name) {
  // Cut first: the cut can leave a trailing space or quote
  const sheetName = String(name || '')
    .replace(EXCEL_SHEET_NAME_FORBIDDEN_CHARS, '_')
    .substring(0, EXCEL_SHEET_NAME_MAX_LENGTH);
  return getValidSheetName(trimQuotesAndSpaces(sheetName));
}

function getValidSheetName(sheetName) {
  return (!sheetName.length || sheetName.toLowerCase() === EXCEL_SHEET_NAME_RESERVED) ? DEFAULT_SHEET_NAME : sheetName;
}

export function toCellValue(column, value) {
  switch (column.type) {
  case 'date':
    return toCalendarDate(value);
  case 'number':
    return Number(value) || 0;
  case 'link': {
    const hyperlink = toPlatformUrl(value?.hyperlink);
    return hyperlink ? {
      text: toText(value.text),
      hyperlink,
    } : toText(value?.text);
  }
  default:
    return toText(value);
  }
}

export function downloadCurrentExport() {
  if (currentExport) {
    const link = document.createElement('a');
    link.href = currentExport.url;
    link.setAttribute('download', currentExport.fileName);
    link.click();
  }
}

export function revokeCurrentExport() {
  if (currentExport) {
    window.URL.revokeObjectURL(currentExport.url);
    currentExport = null;
  }
}

function loadExcelJS() {
  // Without the errback, a library that fails to load never settles. A
  // module that failed stays registered as requested and never settles
  // again: it is undefined, so that the next export loads it again
  return new Promise((resolve, reject) => window.require([EXCEL_JS_MODULE], resolve, error => {
    window.require.undef?.(EXCEL_JS_MODULE);
    reject(error);
  }));
}

function displayFailed() {
  document.dispatchEvent(new CustomEvent('alert-message', {detail: {
    alertComponent: EXPORT_ALERT_COMPONENT,
    alertComponentParams: {
      icon: 'fa-times-circle',
      color: 'error',
      messageKey: `${I18N_PREFIX}.exportFailed`,
    },
    alertType: 'error',
    alertDismissible: true,
  }}));
}

function displayDone(root) {
  document.dispatchEvent(new CustomEvent('alert-message', {detail: {
    alertComponent: EXPORT_ALERT_COMPONENT,
    alertComponentParams: {
      icon: 'fa-check-circle',
      color: 'success',
      messageKey: `${I18N_PREFIX}.exportDone`,
    },
    alertType: 'success',
    alertDismissible: true,
    // The alert stays until it is closed or replaced by another one
    alertTimeout: -1,
    alertLinkText: root.$t(`${I18N_PREFIX}.exportDownload`),
    alertLinkCallback: downloadCurrentExport,
    alertDismissCallback: revokeCurrentExport,
  }}));
}

// A string is written as a shared string, never as a formula, whatever
// character it starts with
function toText(value) {
  return String(value ?? '').substring(0, EXCEL_MAX_TEXT_LENGTH);
}

// An .xlsx date has no time zone: the cell carries the calendar date the
// administrator's browser shows, as a UTC midnight
function toCalendarDate(timestamp) {
  if (!Number(timestamp)) {
    return null;
  }
  const date = new Date(Number(timestamp));
  return new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
}

// A link is written only when it points to the platform: an absolute URL
// of its origin, or a root-relative path, written as an absolute URL
function toPlatformUrl(url) {
  if (!url || typeof url !== 'string') {
    return null;
  } else if (url.startsWith('/') && !url.startsWith('//')) {
    return `${window.location.origin}${url}`;
  }
  try {
    return new URL(url).origin === window.location.origin ? url : null;
  } catch (e) {
    return null;
  }
}

// The link the screen renders (SpaceAvatar), built from the space id
function getSpaceUrl(space) {
  return space?.id && `${window.location.origin}${eXo.env.portal.context}/s/${space.id}` || null;
}

function getManagerNames(space, collator) {
  const names = (space.managers || [])
    .map(manager => manager?.fullname)
    .filter(Boolean);
  names.sort((a, b) => collator.compare(a.toLowerCase(), b.toLowerCase()));
  return names;
}

function getLabel(root, type, value) {
  const key = `${I18N_PREFIX}.${type}.${value}`;
  return (value && root.$te(key)) ? root.$t(key) : String(value ?? '');
}

// Removes the leading and trailing quotes and spaces, Excel refusing a sheet
// name that starts or ends with a quote
function trimQuotesAndSpaces(text) {
  const isTrimmed = char => char === '\'' || !char.trim();
  let start = 0;
  let end = text.length;
  while (start < end && isTrimmed(text[start])) {
    start++;
  }
  while (end > start && isTrimmed(text[end - 1])) {
    end--;
  }
  return text.substring(start, end);
}

// A Sync cell holding no synchronization ('-') is centered, as the short values
function isLeftAligned(column, value) {
  return LEFT_ALIGNED_COLUMNS.includes(column.key) && !(column.key === 'sync' && value === '-');
}
