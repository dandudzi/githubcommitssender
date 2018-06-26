package com.dudziak.daniel.githubcommitssender.model

import android.provider.BaseColumns

object Repository {
    object Entry : BaseColumns {
        const val TABLE_NAME = "Repositories"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_DATE = "date"

        const val COLUMN_NAME_ID_FOREIGN_KEY = "id"
        const val TABLE_NAME_COMMITS = "Commits"
        const val COLUMN_NAME_SHA = "sha"
        const val COLUMN_NAME_AUTHOR = "author"
        const val COLUMN_NAME_MESSAGE = "message"
    }
}