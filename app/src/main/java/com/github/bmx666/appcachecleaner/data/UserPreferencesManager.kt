package com.github.bmx666.appcachecleaner.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.github.bmx666.appcachecleaner.const.Constant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

data class UserPreferencesExtraSearchText(
    var clearCache: CharSequence? = null,
    var storage: CharSequence? = null,
)

data class UserPreferencesExtra(
    var showStartStopService: Boolean = false,
    var showCloseApp: Boolean = false,
    var afterClearingCacheStopService: Boolean = false,
    var afterClearingCacheCloseApp: Boolean = false,
)

data class UserPreferencesFilter(
    var minCacheSize: Long? = null,
    var hideDisabledApps: Boolean = false,
    var hideIgnoredApps: Boolean = false,
    var showDialogToIgnoreApp: Boolean = true,
    var listOfIgnoredApps: Set<String> = HashSet(),
)

data class UserPreferencesFirstBoot(
    var firstBoot: Boolean = true,
)

data class UserPreferencesBugWarning(
    var bug322519674: Boolean = true,
)

data class UserPreferencesUI(
    var nightMode: Boolean = false,
)

data class UserPreferencesSettings(
    var delayForNextAppTimeout: Int =
        Constant.Settings.CacheClean.DEFAULT_DELAY_FOR_NEXT_APP_MS / 1000,
    var maxWaitAppTimeout: Int =
        Constant.Settings.CacheClean.DEFAULT_WAIT_APP_PERFORM_CLICK_MS / 1000,
    var maxWaitClearCacheButtonTimeout: Int =
        Constant.Settings.CacheClean.DEFAULT_WAIT_CLEAR_CACHE_BUTTON_MS / 1000,
    var scenario: Constant.Scenario = Constant.Scenario.DEFAULT,
)

data class UserPreferences(
    var packageLists: HashMap<String, Set<String>> = HashMap(),
    val extraSearchText: HashMap<Locale, UserPreferencesExtraSearchText> = HashMap(),
    val extra: UserPreferencesExtra = UserPreferencesExtra(),
    val filter: UserPreferencesFilter = UserPreferencesFilter(),
    val firstBoot: UserPreferencesFirstBoot = UserPreferencesFirstBoot(),
    val bugWarning: UserPreferencesBugWarning = UserPreferencesBugWarning(),
    val ui: UserPreferencesUI = UserPreferencesUI(),
    val settings: UserPreferencesSettings = UserPreferencesSettings(),
)

class UserPreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {

        object PackageList {
            val KEY_NAMES =
                stringSetPreferencesKey("package_list_names")
        }

        object ExtraSearchText {
            val KEY_LOCALE =
                stringSetPreferencesKey("extra_search_text_locale")
            const val KEY_CLEAR_CACHE =
                "clear_cache"
            const val KEY_STORAGE =
                "text_storage"
        }

        object Extra {
            val KEY_SHOW_BUTTON_START_STOP_SERVICE =
                booleanPreferencesKey("extra_show_button_start_stop_service")
            val KEY_SHOW_BUTTON_CLOSE_APP =
                booleanPreferencesKey("extra_show_button_close_app")
            val KEY_ACTION_STOP_SERVICE =
                booleanPreferencesKey("extra_action_stop_service")
            val KEY_ACTION_CLOSE_APP =
                booleanPreferencesKey("extra_action_close_app")
        }

        object Filter {
            val KEY_MIN_CACHE_SIZE_BYTES =
                longPreferencesKey("filter_min_cache_size_bytes")
            val KEY_HIDE_DISABLED_APPS =
                booleanPreferencesKey("filter_hide_disabled_apps")
            val KEY_HIDE_IGNORED_APPS =
                booleanPreferencesKey("filter_hide_ignored_apps")
            val KEY_SHOW_DIALOG_TO_IGNORE_APP =
                booleanPreferencesKey("filter_show_dialog_to_ignore_app")
            val KEY_LIST_OF_IGNORED_APPS =
                stringSetPreferencesKey("filter_list_of_ignored_apps")
        }

        object FirstBoot {
            val KEY_FIRST_BOOT =
                booleanPreferencesKey("first_boot")
        }

        object BugWarning {
            val KEY_BUG_322519674 =
                booleanPreferencesKey("bug_322519674")
        }

        object UI {
            val KEY_NIGHT_MODE =
                booleanPreferencesKey("ui_night_mode")
        }

        object Settings {
            val KEY_DELAY_FOR_NEXT_APP_TIMEOUT =
                intPreferencesKey("settings_delay_for_next_app_timeout")
            val KEY_MAX_WAIT_APP_TIMEOUT =
                intPreferencesKey("settings_max_wait_app_timeout")
            val KEY_MAX_WAIT_CLEAR_CACHE_BTN_TIMEOUT =
                intPreferencesKey("settings_max_wait_clear_cache_btn_timeout")
            val KEY_SCENARIO =
                stringPreferencesKey("settings_scenario")
        }
    }

    suspend fun fetchInitialPreferences() =
        mapUserPreferences(dataStore.data.first().toPreferences())

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Timber.e("Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            mapUserPreferences(preferences)
        }

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {

        val packageLists = HashMap<String, Set<String>>().apply {
            preferences[PreferencesKeys.PackageList.KEY_NAMES]?.let { names ->
                names.forEach { name ->
                    val key = stringSetPreferencesKey("package_list,$name")
                    preferences[key]?.let {
                        put(name, it)
                    }
                }
            }
        }

        val extraSearchText = HashMap<Locale, UserPreferencesExtraSearchText>().apply {
            preferences[PreferencesKeys.ExtraSearchText.KEY_LOCALE]?.let { localeList ->
                localeList.forEach { locale ->
                    put(Locale.forLanguageTag(locale),
                        UserPreferencesExtraSearchText().apply {
                            val keyClearCache =
                                stringPreferencesKey("${PreferencesKeys.ExtraSearchText.KEY_CLEAR_CACHE},$locale")

                            preferences[keyClearCache]?.let {
                                clearCache = it
                            }

                            val keyStorage =
                                stringPreferencesKey("${PreferencesKeys.ExtraSearchText.KEY_STORAGE},$locale")
                            preferences[keyStorage]?.let {
                                storage = it
                            }
                        })
                }
            }
        }

        val extra = UserPreferencesExtra().apply {
            preferences[PreferencesKeys.Extra.KEY_SHOW_BUTTON_START_STOP_SERVICE]?.let {
                showStartStopService = it
            }
            preferences[PreferencesKeys.Extra.KEY_SHOW_BUTTON_CLOSE_APP]?.let {
                showCloseApp = it
            }
            preferences[PreferencesKeys.Extra.KEY_ACTION_STOP_SERVICE]?.let {
                afterClearingCacheStopService = it
            }
            preferences[PreferencesKeys.Extra.KEY_ACTION_CLOSE_APP]?.let {
                afterClearingCacheCloseApp = it
            }
        }

        val filter = UserPreferencesFilter().apply {
            preferences[PreferencesKeys.Filter.KEY_MIN_CACHE_SIZE_BYTES]?.let {
                minCacheSize = it
            }
            preferences[PreferencesKeys.Filter.KEY_HIDE_DISABLED_APPS]?.let {
                hideDisabledApps = it
            }
            preferences[PreferencesKeys.Filter.KEY_HIDE_IGNORED_APPS]?.let {
                hideIgnoredApps = it
            }
            preferences[PreferencesKeys.Filter.KEY_SHOW_DIALOG_TO_IGNORE_APP]?.let {
                showDialogToIgnoreApp = it
            }
            preferences[PreferencesKeys.Filter.KEY_LIST_OF_IGNORED_APPS]?.let {
                listOfIgnoredApps = it
            }
        }

        val firstBoot = UserPreferencesFirstBoot().apply {
            preferences[PreferencesKeys.FirstBoot.KEY_FIRST_BOOT]?.let {
                firstBoot = it
            }
        }

        val bugWarning = UserPreferencesBugWarning().apply {
            preferences[PreferencesKeys.BugWarning.KEY_BUG_322519674]?.let {
                bug322519674 = it
            }
        }

        val ui = UserPreferencesUI().apply {
            preferences[PreferencesKeys.UI.KEY_NIGHT_MODE]?.let {
                nightMode = it
            }
        }

        val settings = UserPreferencesSettings().apply {
            preferences[PreferencesKeys.Settings.KEY_DELAY_FOR_NEXT_APP_TIMEOUT]?.let {
                delayForNextAppTimeout = it
            }
            preferences[PreferencesKeys.Settings.KEY_MAX_WAIT_APP_TIMEOUT]?.let {
                maxWaitAppTimeout = it
            }
            preferences[PreferencesKeys.Settings.KEY_MAX_WAIT_CLEAR_CACHE_BTN_TIMEOUT]?.let {
                maxWaitClearCacheButtonTimeout = it
            }
            preferences[PreferencesKeys.Settings.KEY_SCENARIO]?.let {
                scenario = Constant.Scenario.valueOf(it)
            }
        }

        return UserPreferences(
            packageLists = packageLists,
            extraSearchText = extraSearchText,
            extra = extra,
            filter = filter,
            firstBoot = firstBoot,
            bugWarning = bugWarning,
            ui = ui,
            settings = settings,
        )
    }

    suspend fun toggleNightMode() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.UI.KEY_NIGHT_MODE] =
                !(preferences[PreferencesKeys.UI.KEY_NIGHT_MODE] ?: false)
        }
    }

    suspend fun toggleFilterHideDisabledApps() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.Filter.KEY_HIDE_DISABLED_APPS] =
                !(preferences[PreferencesKeys.Filter.KEY_HIDE_DISABLED_APPS] ?: false)
        }
    }

    suspend fun toggleFilterHideIgnoredApps() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.Filter.KEY_HIDE_IGNORED_APPS] =
                !(preferences[PreferencesKeys.Filter.KEY_HIDE_IGNORED_APPS] ?: false)
        }
    }
}