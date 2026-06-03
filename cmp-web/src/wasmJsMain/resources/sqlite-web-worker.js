/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Web Worker implementing Room's SQLite message protocol.
 *
 * WASM loading strategy: sqlite3.js is loaded from CDN via importScripts, but
 * sqlite3.wasm is fetched separately as an ArrayBuffer and passed directly to
 * sqlite3InitModule({ wasmBinary }). This bypasses Emscripten's scriptDirectory
 * resolution (which would incorrectly look for sqlite3.wasm at the worker's
 * origin), so no local copy of sqlite3.wasm is needed.
 *
 * Storage strategy:
 *  - crossOriginIsolated = true  (localhost with COOP/COEP headers) →
 *    sqlite3.oo1.OpfsDb(path) — persistent OPFS-backed database
 *  - crossOriginIsolated = false (GitHub Pages, no headers) →
 *    this worker is never created; AppDatabaseFactory falls back to
 *    Room.inMemoryDatabaseBuilder directly.
 *
 * Protocol: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:sqlite/sqlite-web-worker-test/web-worker/worker.js
 */

const SQLITE_CDN = 'https://cdn.jsdelivr.net/npm/@sqlite.org/sqlite-wasm@3.49.1-build2/sqlite-wasm/jswasm/';

importScripts(SQLITE_CDN + 'sqlite3.js');

let sqlite3 = null;
const databases = new Map();
const statements = new Map();
let nextDatabaseId = 0;
let nextStatementId = 0;

function openRequest(id, requestData) {
    try {
        const newDatabaseId = nextDatabaseId++;
        let newDatabase;

        const dbPath = requestData.path;
        if (dbPath && sqlite3.oo1.OpfsDb) {
            try {
                newDatabase = new sqlite3.oo1.OpfsDb(dbPath);
            } catch (_) {
                newDatabase = new sqlite3.oo1.DB(':memory:');
            }
        } else {
            newDatabase = new sqlite3.oo1.DB(':memory:');
        }

        databases.set(newDatabaseId, newDatabase);
        postMessage({'id': id, data: {'databaseId': newDatabaseId}});
    } catch (error) {
        postMessage({'id': id, error: String(error.message || error)});
    }
}

function prepareRequest(id, requestData) {
    try {
        const database = databases.get(requestData.databaseId);
        if (!database) {
            postMessage({'id': id, error: 'Invalid database ID: ' + requestData.databaseId});
            return;
        }
        const newStatementId = nextStatementId++;
        const resultData = {
            'statementId': newStatementId,
            'parameterCount': 0,
            'columnNames': [],
        };
        const statement = database.prepare(requestData.sql);
        statements.set(newStatementId, statement);
        resultData.parameterCount = sqlite3.capi.sqlite3_bind_parameter_count(statement);
        for (let i = 0; i < statement.columnCount; i++) {
            resultData.columnNames.push(sqlite3.capi.sqlite3_column_name(statement, i));
        }
        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: String(error.message || error)});
    }
}

function stepRequest(id, requestData) {
    const statement = statements.get(requestData.statementId);
    if (!statement) {
        postMessage({'id': id, error: 'Invalid statement ID: ' + requestData.statementId});
        return;
    }
    try {
        const resultData = {
            'rows': [],
            'columnTypes': [],
        };
        statement.reset();
        statement.clearBindings();
        for (let i = 0; i < requestData.bindings.length; i++) {
            statement.bind(i + 1, requestData.bindings[i]);
        }
        while (statement.step()) {
            if (!resultData.columnTypes.length) {
                for (let i = 0; i < statement.columnCount; i++) {
                    resultData.columnTypes.push(sqlite3.capi.sqlite3_column_type(statement, i));
                }
            }
            resultData.rows.push(statement.get([]));
        }
        postMessage({'id': id, data: resultData});
    } catch (error) {
        postMessage({'id': id, error: String(error.message || error)});
    }
}

function closeRequest(id, requestData) {
    if (requestData.statementId != null) {
        const statement = statements.get(requestData.statementId);
        if (statement) {
            try { statement.finalize(); } catch (_) {}
            statements.delete(requestData.statementId);
        }
    }
    if (requestData.databaseId != null) {
        const database = databases.get(requestData.databaseId);
        if (database) {
            try { database.close(); } catch (_) {}
            databases.delete(requestData.databaseId);
        }
    }
}

const commandMap = {
    'open': openRequest,
    'prepare': prepareRequest,
    'step': stepRequest,
    'close': closeRequest,
};

function handleMessage(e) {
    const requestMsg = e.data;
    if (requestMsg == null || requestMsg.data == null) {
        postMessage({'id': requestMsg && requestMsg.id, 'error': "Invalid request, missing 'data'."});
        return;
    }
    if (requestMsg.data.cmd == null) {
        postMessage({'id': requestMsg.id, 'error': "Invalid request, missing 'cmd'."});
        return;
    }
    const handler = commandMap[requestMsg.data.cmd];
    if (handler) {
        handler(requestMsg.id, requestMsg.data);
    } else {
        postMessage({'id': requestMsg.id, 'error': "Unknown command: '" + requestMsg.data.cmd + "'."});
    }
}

const messageQueue = [];
onmessage = (e) => {
    if (!sqlite3) {
        messageQueue.push(e);
    } else {
        handleMessage(e);
    }
};

// Fetch sqlite3.wasm from CDN as an ArrayBuffer and pass it directly to
// sqlite3InitModule. This bypasses Emscripten's scriptDirectory-based WASM
// resolution, so the file does not need to be served from the worker's origin.
fetch(SQLITE_CDN + 'sqlite3.wasm')
    .then(r => r.arrayBuffer())
    .then(wasmBinary => sqlite3InitModule({wasmBinary}))
    .then(instance => {
        sqlite3 = instance;
        while (messageQueue.length > 0) {
            handleMessage(messageQueue.shift());
        }
    })
    .catch(err => {
        console.error('Room SQLite worker: Failed to initialize SQLite WASM:', err);
    });
