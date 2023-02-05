package com.github.bmx666.appcachecleaner.ui.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.github.bmx666.appcachecleaner.R
import com.github.bmx666.appcachecleaner.config.SharedPreferencesManager
import com.github.bmx666.appcachecleaner.ui.activity.AppCacheCleanerActivity
import com.github.bmx666.appcachecleaner.ui.dialog.CustomListDialogBuilder
import com.github.bmx666.appcachecleaner.util.LocaleHelper
import java.util.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        val context = requireContext()
        val locale = LocaleHelper.getCurrentLocale(context)

        initializeExtraSearchText(
            preferenceManager.findPreference("clear_cache"),
            context, locale,
            context.getText(R.string.clear_cache_btn_text),
            { SharedPreferencesManager.ExtraSearchText.getClearCache(context, locale) },
            { value ->
                if (value.isEmpty() or value.trim().isEmpty())
                    SharedPreferencesManager.ExtraSearchText.removeClearCache(context, locale)
                else
                    SharedPreferencesManager.ExtraSearchText.saveClearCache(context, locale, value)
            }
        )

        initializeExtraSearchText(
            preferenceManager.findPreference("storage"),
            context, locale,
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                    context.getText(R.string.storage_settings_for_app)
                else -> context.getText(R.string.storage_label)
            },
            { SharedPreferencesManager.ExtraSearchText.getStorage(context, locale) },
            { value ->
                if (value.isEmpty() or value.trim().isEmpty())
                    SharedPreferencesManager.ExtraSearchText.removeStorage(context, locale)
                else
                    SharedPreferencesManager.ExtraSearchText.saveStorage(context, locale, value)
            }
        )

        initializeCustomList(
            context,
            preferenceManager.findPreference("custom_list_add"),
            preferenceManager.findPreference("custom_list_edit"),
            preferenceManager.findPreference("custom_list_remove"),
        )
    }

    private fun initializeExtraSearchText(pref: EditTextPreference?,
                                          context: Context, locale: Locale,
                                          extraText: CharSequence,
                                          getExtraText: () -> String?,
                                          onChangeExtraText: (String) -> Unit) {
        pref?.apply {
            dialogTitle = context.getString(
                R.string.dialog_extra_search_text_message,
                locale.displayLanguage, locale.displayCountry,
                extraText)
            summary = getExtraText()
            setSummaryProvider {
                getExtraText()
            }
            setOnBindEditTextListener { editText ->
                val value = getExtraText()
                if (value?.isNotEmpty() == true) {
                    editText.setText(value)
                    editText.hint = null
                } else {
                    editText.text = null
                    editText.hint = extraText
                }
            }
            setOnPreferenceChangeListener { _, newValue ->
                onChangeExtraText(newValue as String)
                true
            }
        }
    }

    private fun initializeCustomList(context: Context,
                                     addPref: Preference?,
                                     editPref: Preference?,
                                     removePref: Preference?) {
        SharedPreferencesManager.PackageList.getNames(context).apply {
            editPref?.isVisible = isNotEmpty()
            removePref?.isVisible = isNotEmpty()
        }

        addPref?.apply {
            setOnPreferenceClickListener {
                // show dialog from Settings Fragment for better UX
                CustomListDialogBuilder.buildAddDialog(context) { name ->
                    name ?: return@buildAddDialog

                    // check if entered name already exists
                    val names = SharedPreferencesManager.PackageList.getNames(context)
                    if (names.contains(name)) {
                        Toast.makeText(context,
                            R.string.toast_custom_list_add_already_exists,
                            Toast.LENGTH_SHORT).show()
                        return@buildAddDialog
                    }
                    (activity as AppCacheCleanerActivity?)?.showCustomListPackageFragment(name)
                }
                true
            }
        }

        editPref?.apply {
            setOnPreferenceClickListener {
                // show dialog from Settings Fragment for better UX
                CustomListDialogBuilder.buildEditDialog(context) { name ->
                    name ?: return@buildEditDialog

                    // check if entered name already exists
                    val names = SharedPreferencesManager.PackageList.getNames(context)
                    if (names.contains(name))
                        (activity as AppCacheCleanerActivity?)?.showCustomListPackageFragment(name)
                }
                true
            }
        }

        removePref?.apply {
            setOnPreferenceClickListener {
                // show dialog from Settings Fragment for better UX
                CustomListDialogBuilder.buildRemoveDialog(context) { name ->
                    name?.let {
                        SharedPreferencesManager.PackageList.remove(context, name)
                        SharedPreferencesManager.PackageList.getNames(context).apply {
                            editPref?.isVisible = isNotEmpty()
                            removePref?.isVisible = isNotEmpty()
                        }
                        Toast.makeText(context,
                            R.string.toast_custom_list_has_been_removed,
                            Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment()
    }
}