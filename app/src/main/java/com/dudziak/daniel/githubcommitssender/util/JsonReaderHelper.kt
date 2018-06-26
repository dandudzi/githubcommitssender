package com.dudziak.daniel.githubcommitssender.util

import android.util.JsonReader
import com.dudziak.daniel.githubcommitssender.model.Commit

class JsonReaderHelper(){
    companion object {

        fun readKey(jsonReader: JsonReader, _key: String): String {
            var tmp = ""
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val key = jsonReader.nextName()
                if (key == _key) {
                    tmp = jsonReader.nextString()
                } else {
                    jsonReader.skipValue()
                }
            }
            jsonReader.endObject()
            return tmp
        }


        fun readCommits(jsonReader: JsonReader, commits: MutableList<Commit>) {
            jsonReader.beginArray()
            while (jsonReader.hasNext()) {
                val commit = readCommit(jsonReader)
                commits.add(commit)
            }
            jsonReader.endArray()
        }

        private fun readCommit(jsonReader: JsonReader): Commit {
            var authorName = ""
            var shaValue = ""
            var message = ""
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val key = jsonReader.nextName()
                if (key == "sha") {
                    shaValue = jsonReader.nextString()
                } else if (key == "commit") {
                    val pair = readCommitBody(jsonReader)
                    authorName = pair.second
                    message = pair.first
                } else {
                    jsonReader.skipValue()
                }
            }
            jsonReader.endObject()
            return Commit(message, shaValue, authorName)
        }

        private fun readCommitBody(jsonReader: JsonReader): Pair<String, String> {
            var authorName = ""
            var message = ""
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val key = jsonReader.nextName()
                if (key == "author") {
                    authorName = readAuthorName(jsonReader)
                } else if (key == "message") {
                    message = jsonReader.nextString()
                } else {
                    jsonReader.skipValue()
                }
            }
            jsonReader.endObject()
            return Pair(message, authorName)
        }

        private fun readAuthorName(jsonReader: JsonReader): String {
            var authorName = ""
            jsonReader.beginObject()
            while (jsonReader.hasNext()) {
                val key = jsonReader.nextName()
                if (key == "name") {
                    authorName = jsonReader.nextString()
                } else {
                    jsonReader.skipValue()
                }
            }
            jsonReader.endObject()
            return authorName
        }
    }
}