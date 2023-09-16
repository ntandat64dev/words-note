package com.example.wordnotes.data.repositories

import androidx.annotation.VisibleForTesting
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWordsRepository : WordsRepository {
    private var shouldThrowError = false

    private val _savedWords: MutableStateFlow<LinkedHashMap<String, Word>> = MutableStateFlow(LinkedHashMap())
    private val savedWords: StateFlow<LinkedHashMap<String, Word>> = _savedWords.asStateFlow()

    private val observableWords: Flow<Result<List<Word>>> = savedWords.map {
        if (shouldThrowError) Result.Error(Exception("Test exception"))
        else Result.Success(it.values.toList())
    }

    fun setShouldThrowError(value: Boolean) {
        shouldThrowError = value
    }

    override fun getWordsStream(): Flow<Result<List<Word>>> = observableWords

    override fun getWordStream(wordId: String): Flow<Result<Word>> {
        return observableWords.map { result ->
            when (result) {
                is Result.Loading -> Result.Loading
                is Result.Error -> Result.Error(result.exception)
                is Result.Success -> {
                    val word = result.data.firstOrNull { it.id == wordId } ?: return@map Result.Error(Exception("Word not found"))
                    Result.Success(word)
                }
            }
        }
    }

    override suspend fun refreshWords() {
        delay(1000)
    }

    override suspend fun getWords(): Result<List<Word>> {
        return if (shouldThrowError) Result.Error(Exception("Test exception")) else observableWords.first()
    }

    override suspend fun getWord(wordId: String): Result<Word> {
        if (shouldThrowError) return Result.Error(Exception("Test exception"))
        return savedWords.value[wordId]?.let { Result.Success(it) } ?: Result.Error(Exception("Word not found"))
    }

    override suspend fun getRemindWords(): Result<List<Word>> {
        val remindWords = savedWords.value.values.filter { it.isRemind }
        return if (remindWords.isNotEmpty()) remindWords.let { Result.Success(it) } else Result.Error(Exception("Words not found"))
    }

    override suspend fun saveWord(word: Word) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it[word.id] = word
            }
        }
    }

    override suspend fun updateWord(word: Word) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it[word.id] = word
            }
        }
    }

    override suspend fun remindWords(ids: List<String>) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it.putAll(ids.map { id -> id to it[id]!!.copy(isRemind = true) })
            }
        }
    }

    override suspend fun deleteWords(ids: List<String>) {
        _savedWords.update { words ->
            LinkedHashMap<String, Word>(words).also {
                it.keys.removeAll(ids.toSet())
            }
        }
    }

    @VisibleForTesting
    fun addWords(vararg words: Word) {
        _savedWords.update { oldWords ->
            val newWords = LinkedHashMap<String, Word>(oldWords)
            words.forEach { newWords[it.id] = it }
            newWords
        }
    }
}