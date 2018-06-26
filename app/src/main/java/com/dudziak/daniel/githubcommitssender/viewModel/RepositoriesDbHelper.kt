package com.dudziak.daniel.githubcommitssender.viewModel

import android.content.ContentValues
import com.dudziak.daniel.githubcommitssender.model.Commit
import java.util.*
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dudziak.daniel.githubcommitssender.model.Repository
import com.dudziak.daniel.githubcommitssender.util.DateHelper

private const val SQL_CREATE_ENTRIES =
    "CREATE TABLE ${Repository.Entry.TABLE_NAME} (" +
            "${Repository.Entry.COLUMN_NAME_ID} INTEGER PRIMARY KEY," +
            "${Repository.Entry.COLUMN_NAME_DATE} TEXT)"

private const val SQL_CREATE_ENTRIES_COMMITS =
    "CREATE TABLE ${Repository.Entry.TABLE_NAME_COMMITS} (" +
            "${Repository.Entry.COLUMN_NAME_ID_FOREIGN_KEY} INTEGER," +
            "${Repository.Entry.COLUMN_NAME_SHA} TEXT," +
            "${Repository.Entry.COLUMN_NAME_AUTHOR} TEXT," +
            "${Repository.Entry.COLUMN_NAME_MESSAGE} TEXT," +
            "FOREIGN KEY(${Repository.Entry.COLUMN_NAME_ID_FOREIGN_KEY}) REFERENCES ${Repository.Entry.TABLE_NAME}(${Repository.Entry.COLUMN_NAME_ID}))"

private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Repository.Entry.TABLE_NAME}"

private const val SQL_DELETE_ENTRIES_COMMITS =
    "DROP TABLE IF EXISTS ${Repository.Entry.TABLE_NAME_COMMITS}"

class RepositoriesDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_ENTRIES_COMMITS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES_COMMITS)
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Repositories.db"

        fun insertRepositoryInfo(_db: RepositoriesDbHelper, id: String, date: String) {
            val db = _db.writableDatabase
            if (isRepositoryExist(_db, id)) {
                updateRepositoryDate(_db, id, date)
            } else {
                val values = ContentValues().apply {
                    put(Repository.Entry.COLUMN_NAME_ID, id.toInt())
                    put(Repository.Entry.COLUMN_NAME_DATE, date)
                }
                val newRowId = db?.insert(Repository.Entry.TABLE_NAME, null, values)
            }
        }

        private fun isRepositoryExist(_db: RepositoriesDbHelper, repositoryName: String): Boolean {
            val cursor = getCursorToRepositories(_db, repositoryName)
            return cursor!!.count != 0
        }

        private fun getCursorToRepositories(
            _db: RepositoriesDbHelper,
            repositoryName: String
        ): Cursor? {
            val db = _db.readableDatabase
            val projection =
                arrayOf(Repository.Entry.COLUMN_NAME_ID, Repository.Entry.COLUMN_NAME_DATE)

            val selection = "${Repository.Entry.COLUMN_NAME_ID} = ?"
            val selectionArgs = arrayOf(repositoryName)

            return db.query(
                Repository.Entry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
            )
        }

        private fun updateRepositoryDate(_db: RepositoriesDbHelper, id: String, date: String) {
            val db = _db.writableDatabase

            val values = ContentValues().apply {
                put(Repository.Entry.COLUMN_NAME_DATE, date)
            }
            val selection = "${Repository.Entry.COLUMN_NAME_ID} LIKE ?"
            val selectionArgs = arrayOf(id)
            val count = db.update(
                Repository.Entry.TABLE_NAME,
                values,
                selection,
                selectionArgs
            )
        }

        fun isUpToDateRepositoryInDB(
            db: RepositoriesDbHelper,
            repositoryID: String,
            date: Date
        ): Boolean {
            return if (isRepositoryExist(db, repositoryID))
                isRepositoryUpToDate(db, repositoryID, date)
            else
                false
        }

        private fun isRepositoryUpToDate(
            db: RepositoriesDbHelper,
            repositoryID: String,
            date: Date
        ): Boolean {
            val cursor = getCursorToRepositories(db, repositoryID)
            var item = ""
            with(cursor!!) {
                while (moveToNext()) {
                    item = getString(getColumnIndexOrThrow(Repository.Entry.COLUMN_NAME_DATE))
                }
            }
            val otherDate = DateHelper.stringToDate(item)
            return !date.after(otherDate)
        }

        fun getCommitsFromDB(
            _db: RepositoriesDbHelper,
            repositoryID: String,
            commits: MutableList<Commit>
        ) {
            val db = _db.readableDatabase
            val projection =
                arrayOf(
                    Repository.Entry.COLUMN_NAME_ID_FOREIGN_KEY,
                    Repository.Entry.COLUMN_NAME_SHA,
                    Repository.Entry.COLUMN_NAME_AUTHOR,
                    Repository.Entry.COLUMN_NAME_MESSAGE
                )

            val selection = "${Repository.Entry.COLUMN_NAME_ID_FOREIGN_KEY} = ?"
            val selectionArgs = arrayOf(repositoryID)

            val cursor = db.query(
                Repository.Entry.TABLE_NAME_COMMITS,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
            )
            with(cursor!!) {
                while (moveToNext()) {
                    val sha = getString(getColumnIndexOrThrow(Repository.Entry.COLUMN_NAME_SHA))
                    val author =
                        getString(getColumnIndexOrThrow(Repository.Entry.COLUMN_NAME_AUTHOR))
                    val message =
                        getString(getColumnIndexOrThrow(Repository.Entry.COLUMN_NAME_MESSAGE))
                    commits.add(Commit(message, sha, author))
                }
            }
        }

        fun insertCommits(
            _db: RepositoriesDbHelper,
            repositoryID: String,
            commits: MutableList<Commit>
        ) {
            val db = _db.writableDatabase
            val selection = "${Repository.Entry.COLUMN_NAME_ID} LIKE ?"
            val selectionArgs = arrayOf(repositoryID)
            val deletedRows =
                db.delete(Repository.Entry.TABLE_NAME_COMMITS, selection, selectionArgs)
            for (commit in commits) {
                val values = ContentValues().apply {
                    put(Repository.Entry.COLUMN_NAME_ID_FOREIGN_KEY, repositoryID.toInt())
                    put(Repository.Entry.COLUMN_NAME_SHA, commit.shaValue)
                    put(Repository.Entry.COLUMN_NAME_AUTHOR, commit.authorName)
                    put(Repository.Entry.COLUMN_NAME_MESSAGE, commit.message)
                }
                val newRowId = db?.insert(Repository.Entry.TABLE_NAME_COMMITS, null, values)
            }
        }

        fun clearDataBase(_db: RepositoriesDbHelper) {
            val db = _db.writableDatabase
            db.delete(Repository.Entry.TABLE_NAME_COMMITS, null, null)
            db.delete(Repository.Entry.TABLE_NAME, null, null)
        }
    }
}