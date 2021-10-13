package com.nbow.texteditor

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


class SettingActivity : AppCompatActivity() {

    private val TAG = "SettingActivity"
    private lateinit var model : MyViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)

        model = ViewModelProvider(this,MyViewModelFactory(this.application)).get(MyViewModel::class.java)




        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        val toolbar:androidx.appcompat.widget.Toolbar=findViewById(R.id.toolbar)
        toolbar.setTitle("Settings")
//        toolbar.setNavigationIcon(R.drawable.ic_back_navigation)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);




        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)



    }
    var sharedPreferenceChangeListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key=="night_mode_preference")
            {
                initTheme()
            }


        }



    private fun initTheme() {

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        var keyNightMode:String="night_mode_preference"
        if(sharedPreferences.getBoolean(keyNightMode,true))
        {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        val editor = sharedPreferences.edit()
        val keyIsThemeChanged = "is_theme_changed_setting"
        editor.putBoolean(keyIsThemeChanged,true)
        editor.commit()
    }



    override fun onBackPressed() {
//        super.onBackPressed()
        this.finish()
    }

    override fun onResume() {
        //initTheme()
        super.onResume()
    }

}







class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preference, rootKey)
    }
    override fun  onPreferenceTreeClick(preference: Preference?): Boolean {
        val key = preference?.key
        return if (key == "feedback_preference") {

            feedback()
            true
        } else false
    }
    fun feedback()
    {
        try {
            val email = Intent(Intent.ACTION_SENDTO)
            email.data = Uri.parse("mailto:nbowdeveloper@gmail.com")
            email.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            email.putExtra(Intent.EXTRA_TEXT, "Write your Feedback Here!")
            startActivity(email)
        }
        catch(e:Exception)
        {

        }


    }


}