package app.cascade.android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SqlitePersistence(
    context: Context,
    name: String? = "cascade.db",
    private val executor: Executor = Executors.newSingleThreadExecutor(),
) : OutlinePersistence {
    private val helper = object : SQLiteOpenHelper(context.applicationContext, name, null, 1) {
        override fun onConfigure(db: SQLiteDatabase) {
            db.enableWriteAheadLogging()
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE nodes (
                    id TEXT PRIMARY KEY NOT NULL,
                    parent_id TEXT,
                    ord TEXT NOT NULL,
                    text TEXT NOT NULL,
                    collapsed INTEGER NOT NULL DEFAULT 0,
                    task_done INTEGER,
                    due TEXT,
                    updated_at INTEGER NOT NULL
                )
                """.trimIndent(),
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }

    override fun load(): List<Node> {
        val nodes = ArrayList<Node>()
        helper.readableDatabase.rawQuery(
            "SELECT id, parent_id, ord, text, collapsed, task_done, due, updated_at FROM nodes",
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                nodes.add(
                    Node(
                        id = cursor.getString(0),
                        parentId = if (cursor.isNull(1)) null else cursor.getString(1),
                        order = cursor.getString(2),
                        text = cursor.getString(3),
                        collapsed = cursor.getInt(4) != 0,
                        task = if (cursor.isNull(5)) null else Task(cursor.getInt(5) != 0),
                        due = if (cursor.isNull(6)) null else cursor.getString(6),
                        updatedAt = cursor.getLong(7),
                    ),
                )
            }
        }
        return nodes
    }

    override fun write(put: List<Node>, delete: List<String>) {
        executor.execute {
            try {
                val db = helper.writableDatabase
                db.beginTransaction()
                try {
                    for (node in put) {
                        db.insertWithOnConflict("nodes", null, values(node), SQLiteDatabase.CONFLICT_REPLACE)
                    }
                    for (id in delete) {
                        db.delete("nodes", "id = ?", arrayOf(id))
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            } catch (error: Exception) {
                Log.e("SqlitePersistence", "write failed", error)
            }
        }
    }

    private fun values(node: Node) = ContentValues().apply {
        put("id", node.id)
        put("parent_id", node.parentId)
        put("ord", node.order)
        put("text", node.text)
        put("collapsed", if (node.collapsed) 1 else 0)
        if (node.task == null) putNull("task_done") else put("task_done", if (node.task.done) 1 else 0)
        put("due", node.due)
        put("updated_at", node.updatedAt)
    }
}
