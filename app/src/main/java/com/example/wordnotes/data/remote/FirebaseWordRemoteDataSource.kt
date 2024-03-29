package com.example.wordnotes.data.remote

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.wordnotes.data.Result
import com.example.wordnotes.data.model.Word
import com.example.wordnotes.data.wrapWithResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await
import java.lang.reflect.Type
import javax.inject.Inject
import kotlin.reflect.KClass

private const val DATA_KEY = "data_key"
private const val WORK_KEY = "work_key"

private enum class WorkType { SAVE, UPDATE, DELETE }

class FirebaseWordRemoteDataSource @Inject constructor(private val workManager: WorkManager) : WordRemoteDataSource {

    override suspend fun loadWords(): Result<List<Word>> = wrapWithResult {
        val dataSnapshot = Firebase.database.reference.child("$WORDS_PATH/${FirebaseAuth.getInstance().uid}").get().await()
        val words = mutableListOf<Word>()
        for (word in dataSnapshot.children) word.getValue<Word>()?.let { words.add(it) }
        words.sortedBy { it.timestamp }
    }

    override suspend fun saveWords(words: List<Word>): Result<Unit> = wrapWithResult {
        val wordRequest = createOneTimeWorkRequest(NetworkSyncWorker::class, workType = WorkType.SAVE, workData = words)
        workManager.enqueue(wordRequest)
    }

    override suspend fun updateWords(words: List<Word>): Result<Unit> = wrapWithResult {
        val wordRequest = createOneTimeWorkRequest(NetworkSyncWorker::class, workType = WorkType.UPDATE, workData = words)
        workManager.enqueue(wordRequest)
    }

    override suspend fun deleteWords(ids: List<String>): Result<Unit> = wrapWithResult {
        val wordRequest = createOneTimeWorkRequest(NetworkSyncWorker::class, workType = WorkType.DELETE, workData = ids)
        workManager.enqueue(wordRequest)
    }

    private fun <T : ListenableWorker, K> createOneTimeWorkRequest(clazz: KClass<T>, workType: WorkType, workData: K): WorkRequest {
        val type: Type = object : TypeToken<K>() {}.type
        val json = Gson().toJson(workData, type)
        val data = Data.Builder()
            .putString(WORK_KEY, workType.name)
            .putString(DATA_KEY, json)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        return OneTimeWorkRequest.Builder(clazz.java)
            .setInputData(data)
            .setConstraints(constraints)
            .build()
    }
}

class NetworkSyncWorker(context: Context, private val workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        when (workerParams.inputData.getString(WORK_KEY)) {
            WorkType.SAVE.name -> saveWords()
            WorkType.UPDATE.name -> updateWords()
            WorkType.DELETE.name -> deleteWords()
        }
        return Result.success()
    }

    private fun saveWords() {
        val type: Type = object : TypeToken<List<Word>>() {}.type
        val words = Gson().fromJson<List<Word>>(workerParams.inputData.getString(DATA_KEY), type)
        Firebase.database.reference.child("$WORDS_PATH/${FirebaseAuth.getInstance().uid}")
            .updateChildren(words.associateBy { it.id })
    }

    private fun updateWords() {
        val type: Type = object : TypeToken<List<Word>>() {}.type
        val words = Gson().fromJson<List<Word>>(workerParams.inputData.getString(DATA_KEY), type)
        Firebase.database.reference.child("$WORDS_PATH/${FirebaseAuth.getInstance().uid}")
            .updateChildren(words.associateBy { it.id })
    }

    private fun deleteWords() {
        val type: Type = object : TypeToken<List<String>>() {}.type
        val ids = Gson().fromJson<List<String>>(workerParams.inputData.getString(DATA_KEY), type)
        Firebase.database.reference.child("$WORDS_PATH/${FirebaseAuth.getInstance().uid}")
            .updateChildren(ids.associateWith { null })
    }
}