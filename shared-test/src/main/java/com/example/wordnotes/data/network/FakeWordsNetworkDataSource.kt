package com.example.wordnotes.data.network

import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word

class FakeWordsNetworkDataSource(var words: MutableList<Word>? = mutableListOf()) : WordsNetworkDataSource {

    override suspend fun loadWords(onCompleted: (Result<List<Word>>) -> Unit) {
    }

    override suspend fun saveWords(words: List<Word>) {
        this.words = words.toMutableList()
    }

    override suspend fun updateWord(word: Word) {

    }

    override suspend fun deleteWords(ids: List<String>) {
    }

    override suspend fun remindWords(ids: List<String>) {
    }
}