package com.zimmy.splitmoney.appreference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.zimmy.splitmoney.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NoMainThreadWriteSharedPreferences private constructor(
    private val sysPrefs: SharedPreferences,
    val name: String
) : SharedPreferences {

    private val preferencesCache: MutableMap<String, Any?> = HashMap()

    companion object {
        private val executor: ExecutorService = Executors.newSingleThreadExecutor()
        private val INSTANCES: MutableMap<String, NoMainThreadWriteSharedPreferences> = HashMap()

        @JvmStatic
        fun getInstance(mContext: Context): SharedPreferences {

            fun getMasterKey(): MasterKey {
                return MasterKey.Builder(mContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            }

            fun providesSharedPreference() =
                EncryptedSharedPreferences.create(
                    mContext,
                    mContext.getString(R.string.app_name),
                    getMasterKey(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )

            return INSTANCES.getOrPut(mContext.getString(R.string.app_name)) {
                NoMainThreadWriteSharedPreferences(
                    providesSharedPreference(),
                    mContext.getString(R.string.app_name)
                )
            }
        }

        /**
         * Remove all instances for testing purpose.
         */
        @VisibleForTesting
        @JvmStatic
        fun reset() {
            INSTANCES.clear()
        }
    }

    init {
        preferencesCache.putAll(sysPrefs.all)
    }

    override fun contains(key: String?) = preferencesCache[key] != null

    override fun getAll() = HashMap(preferencesCache)

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferencesCache[key] as Boolean? ?: defValue
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferencesCache[key] as Int? ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferencesCache[key] as Long? ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferencesCache[key] as Float? ?: defValue
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return preferencesCache[key] as MutableSet<String>? ?: defValues
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferencesCache[key] as String? ?: defValue
    }

    override fun edit(): SharedPreferences.Editor {
        return Editor(sysPrefs.edit())
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sysPrefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sysPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    inner class Editor(private val sysEdit: SharedPreferences.Editor) : SharedPreferences.Editor {

        private val modifiedData: MutableMap<String, Any?> = HashMap()
        private var keysToRemove: MutableSet<String> = HashSet()
        private var clear = false

        override fun commit(): Boolean {
            submit()
            return true
        }

        override fun apply() {
            submit()
        }

        private fun submit() {
            synchronized(preferencesCache) {
                storeMemCache()
                queuePersistentStore()
            }
        }

        private fun storeMemCache() {
            if (clear) {
                preferencesCache.clear()
                clear = false
            } else {
                preferencesCache.keys.removeAll(keysToRemove)
            }
            keysToRemove.clear()
            preferencesCache.putAll(modifiedData)
            modifiedData.clear()
        }

        private fun queuePersistentStore() {
            try {
                executor.submit {
                    sysEdit.commit()
                }
            } catch (ex: Exception) {
                Log.e(
                    "NoMainThreadWritePrefs",
                    "NoMainThreadWriteSharedPreferences.queuePersistentStore(), submit failed for $name"
                )
            }
        }

        override fun remove(key: String): SharedPreferences.Editor {
            keysToRemove.add(key)
            modifiedData.remove(key)
            sysEdit.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            clear = true
            sysEdit.clear()
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            modifiedData[key] = value
            sysEdit.putLong(key, value)
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            modifiedData[key] = value
            sysEdit.putInt(key, value)
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            modifiedData[key] = value
            sysEdit.putBoolean(key, value)
            return this
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?
        ): SharedPreferences.Editor {
            modifiedData[key] = values
            sysEdit.putStringSet(key, values)
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            modifiedData[key] = value
            sysEdit.putFloat(key, value)
            return this
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            modifiedData[key] = value
            sysEdit.putString(key, value)
            return this
        }
    }
}