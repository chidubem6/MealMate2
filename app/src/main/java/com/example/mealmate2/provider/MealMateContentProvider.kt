package com.example.mealmate2.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.mealmate2.data.MealMateDatabase

class MealMateContentProvider : ContentProvider() {
    private lateinit var database: MealMateDatabase

    override fun onCreate(): Boolean {
        database = MealMateDatabase.getInstance(requireNotNull(context))
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val table = tableFor(uri)
        val columns = projection?.takeIf { it.isNotEmpty() }?.also { validateColumns(table, it) }
            ?.joinToString(", ")
            ?: allowedColumns.getValue(table).joinToString(", ")
        val order = sortOrder?.takeIf { it.isNotBlank() }?.also { validateSortOrder(table, it) }
            ?.let { " ORDER BY $it" }
            ?: ""
        val idClause = idFromUri(uri)?.let { "id = ?" }
        val combinedSelection = listOfNotNull(idClause, selection?.takeIf { it.isNotBlank() })
            .joinToString(" AND ")
            .takeIf { it.isNotBlank() }
        val args = buildList {
            idFromUri(uri)?.let { add(it.toString()) }
            selectionArgs?.let(::addAll)
        }.toTypedArray()
        val where = combinedSelection?.let { " WHERE $it" } ?: ""
        val cursor = database.openHelper.readableDatabase.query(
            SimpleSQLiteQuery("SELECT $columns FROM $table$where$order", args)
        )
        cursor.setNotificationUri(requireNotNull(context).contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val table = tableFor(uri)
        require(idFromUri(uri) == null) { "Cannot insert into item URI: $uri" }
        val safeValues = ContentValues(values ?: ContentValues())
        if (table == TABLE_MEALS) {
            val now = System.currentTimeMillis()
            if (!safeValues.containsKey("createdAt")) safeValues.put("createdAt", now)
            if (!safeValues.containsKey("updatedAt")) safeValues.put("updatedAt", now)
            if (!safeValues.containsKey("servings")) safeValues.put("servings", 1)
            if (!safeValues.containsKey("instructions")) safeValues.put("instructions", "")
        }
        val id = database.openHelper.writableDatabase.insert(table, 0, safeValues)
        require(id > 0) { "Insert failed for $uri" }
        notifyChange(uri)
        return ContentUris.withAppendedId(baseUriFor(table), id)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val table = tableFor(uri)
        val safeValues = ContentValues(values ?: ContentValues())
        val count = database.openHelper.writableDatabase.update(
            table,
            0,
            safeValues,
            selectionFor(uri, selection),
            argsFor(uri, selectionArgs)
        )
        if (count > 0) notifyChange(uri)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val table = tableFor(uri)
        val count = database.openHelper.writableDatabase.delete(
            table,
            selectionFor(uri, selection),
            argsFor(uri, selectionArgs)
        )
        if (count > 0) notifyChange(uri)
        return count
    }

    override fun getType(uri: Uri): String = when (matcher.match(uri)) {
        MEALS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.meal"
        MEAL_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.meal"
        INGREDIENTS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.ingredient"
        SHOPPING -> "vnd.android.cursor.dir/vnd.$AUTHORITY.shopping"
        SHOPPING_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.shopping"
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    private fun tableFor(uri: Uri): String = when (matcher.match(uri)) {
        MEALS, MEAL_ID -> TABLE_MEALS
        INGREDIENTS -> TABLE_INGREDIENTS
        SHOPPING, SHOPPING_ID -> TABLE_SHOPPING
        else -> throw IllegalArgumentException("Unknown URI: $uri")
    }

    private fun idFromUri(uri: Uri): Long? = when (matcher.match(uri)) {
        MEAL_ID, SHOPPING_ID -> ContentUris.parseId(uri)
        else -> null
    }

    private fun selectionFor(uri: Uri, selection: String?): String? {
        val idSelection = idFromUri(uri)?.let { "id = ?" }
        return listOfNotNull(idSelection, selection?.takeIf { it.isNotBlank() })
            .joinToString(" AND ")
            .takeIf { it.isNotBlank() }
    }

    private fun argsFor(uri: Uri, selectionArgs: Array<out String>?): Array<String> = buildList {
        idFromUri(uri)?.let { add(it.toString()) }
        selectionArgs?.let(::addAll)
    }.toTypedArray()

    private fun validateColumns(table: String, columns: Array<out String>) {
        val allowed = allowedColumns.getValue(table)
        require(columns.all { it in allowed }) { "Invalid projection for $table" }
    }

    private fun validateSortOrder(table: String, sortOrder: String) {
        val allowed = allowedColumns.getValue(table)
        require(sortOrder in allowed) { "Invalid sort order for $table" }
    }

    private fun baseUriFor(table: String): Uri = when (table) {
        TABLE_MEALS -> MEALS_URI
        TABLE_INGREDIENTS -> INGREDIENTS_URI
        TABLE_SHOPPING -> SHOPPING_URI
        else -> throw IllegalArgumentException("Unknown table: $table")
    }

    private fun notifyChange(uri: Uri) {
        context?.contentResolver?.notifyChange(uri, null)
    }

    companion object {
        const val AUTHORITY = "com.example.mealmate2.provider"
        val MEALS_URI: Uri = Uri.parse("content://$AUTHORITY/meals")
        val INGREDIENTS_URI: Uri = Uri.parse("content://$AUTHORITY/ingredients")
        val SHOPPING_URI: Uri = Uri.parse("content://$AUTHORITY/shopping")

        private const val TABLE_MEALS = "meals"
        private const val TABLE_INGREDIENTS = "ingredients"
        private const val TABLE_SHOPPING = "shopping_items"
        private const val MEALS = 1
        private const val MEAL_ID = 2
        private const val INGREDIENTS = 3
        private const val SHOPPING = 4
        private const val SHOPPING_ID = 5

        private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, "meals", MEALS)
            addURI(AUTHORITY, "meals/#", MEAL_ID)
            addURI(AUTHORITY, "ingredients", INGREDIENTS)
            addURI(AUTHORITY, "shopping", SHOPPING)
            addURI(AUTHORITY, "shopping/#", SHOPPING_ID)
        }

        private val allowedColumns = mapOf(
            TABLE_MEALS to setOf("id", "name", "instructions", "servings", "createdAt", "updatedAt"),
            TABLE_INGREDIENTS to setOf("id", "mealId", "name", "quantity", "unit"),
            TABLE_SHOPPING to setOf("id", "name", "quantity", "unit", "isChecked", "isManual")
        )
    }
}
