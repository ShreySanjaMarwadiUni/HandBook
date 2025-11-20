package com.example.handbook

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TemplateDbHelper(context: Context) :
    SQLiteOpenHelper(context, "templates.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                image_path TEXT,
                cover_start_path TEXT,
                cover_end_path TEXT
            )"""
        )
    }

    fun getTemplateByName(name: String): TemplateItem? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM templates WHERE name = ?",
            arrayOf(name)
        )
        val template = if (cursor.moveToFirst()) {
            TemplateItem(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                coverStartPath = cursor.getString(cursor.getColumnIndexOrThrow("cover_start_path")),
                coverEndPath = cursor.getString(cursor.getColumnIndexOrThrow("cover_end_path"))
            )
        } else null
        cursor.close()
        return template
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS templates")
        onCreate(db)
    }

    fun insertTemplate(name: String, imagePath: String?, coverStart: String?, coverEnd: String?) {
        val values = ContentValues().apply {
            put("name", name)
            put("image_path", imagePath)
            put("cover_start_path", coverStart)
            put("cover_end_path", coverEnd)
        }
        writableDatabase.insert("templates", null, values)
    }

    fun templateExists(name: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM templates WHERE name = ?",
            arrayOf(name)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllTemplates(): List<TemplateItem> {
        val templates = mutableListOf<TemplateItem>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM templates", null)
        while (cursor.moveToNext()) {
            templates.add(
                TemplateItem(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                    coverStartPath = cursor.getString(cursor.getColumnIndexOrThrow("cover_start_path")),
                    coverEndPath = cursor.getString(cursor.getColumnIndexOrThrow("cover_end_path"))
                )
            )
        }
        cursor.close()
        return templates
    }

    /**
     * Ensures default (built-in) templates exist in the database.
     */
    fun ensureDefaultTemplates(context: Context) {
        val builtInTemplates = listOf(
            TemplateItem(
                id = 0,
                name = "tmplt1",
                imagePath = "tmplt1_cover_preview", // stored as drawable name
                coverStartPath = "tmplt1_cover_start",
                coverEndPath = "tmplt1_cover_end"
            ),
            TemplateItem(
                id = 1,
                name = "tmplt2",
                imagePath = "tmplt2_cover_preview",
                coverStartPath = "tmplt2_cover_start",
                coverEndPath = "tmplt2_cover_end"
            )
        )

        for (template in builtInTemplates) {
            if (!templateExists(template.name)) {
                insertTemplate(
                    name = template.name,
                    imagePath = template.imagePath,
                    coverStart = template.coverStartPath,
                    coverEnd = template.coverEndPath
                )
            }
        }
    }
}
