package com.nbow.texteditorpro

import android.app.Activity
import android.content.*
import android.content.ClipboardManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.nbow.texteditorpro.databinding.ActivityMainBinding
import java.io.*
import java.net.URLConnection
import java.util.*
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.DocumentsContract
import android.provider.Settings
import android.text.*
import android.text.style.*
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import top.defaults.colorpicker.ColorPickerPopup

import android.view.LayoutInflater
import androidx.core.content.FileProvider
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import com.nbow.texteditorpro.data.Note


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val TAG = "MainActivity"
    private val THEME_PREFERENCE_KEY = "night_mode_preference"
    val mimeType = "text/* |application/java |application/sql |application/php |application/x-php |application/x-javascript |application/javascript |application/x-tcl |application/xml |application/octet-stream"
    val TEXT = "text/*"
    val JAVA = "application/java"
    val SQL = "application/sql"
    val PHP = "application/php"
    val X_PHP = "application/x-php"
    val X_JS = "application/x-javascript"
    val JS = "application/javascript"
    val X_TCL = "application/x-tcl"
    val XML = "application/xml"
    val OCTECT_STRM = "application/octet-stream"
    private var supportedMimeTypes = arrayOf(TEXT, JAVA, SQL, PHP, X_PHP, X_JS, JS, X_TCL, XML, OCTECT_STRM)
    private lateinit var toolbar: Toolbar

    //    private lateinit var pager2 : ViewPager2
//    private lateinit var tabLayout : TabLayout
//    private lateinit var fragmentManager: FragmentManager
    private var menu: Menu? = null

    //    private lateinit var bottomNavigationView : BottomNavigationView
    private var darkTheme: Boolean = true

    private lateinit var model: MyViewModel
    private lateinit var adapter: FragmentAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var helper: Utils

    private var actionMode: ActionMode? = null
    private lateinit var manager: ReviewManager
    private var index: Int = 0
    private var indexList: MutableList<Int> = arrayListOf()
    private var findText: String = ""
    private var replaceText: String = ""
    private var ignoreCase: Boolean = true
    private lateinit var alertDialogGlobal: AlertDialog
    lateinit var progressBar: ProgressBar
    lateinit var constraintLayout: ConstraintLayout
    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: callled")


        binding = ActivityMainBinding.inflate(layoutInflater)
       // analytics = FirebaseAnalytics.getInstance(this)
        setContentView(binding.root)
        init()
        if(savedInstanceState==null)
            askForRating()
        if (intent != null && savedInstanceState == null) {
            val uri: Uri? = intent.data
            if (uri !== null) {
                readFileUsingUri(uri,true)
            }
        }
        binding.textEditorBottam.apply {

                textSize.setOnClickListener({
                    if (isValidTab()) {
                        val cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                            seekbarDialog()
                    }
                })

            effect.setOnClickListener({
                if (isValidTab()) {
                    val cf = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                    if (cf.isSelected())
                        viewSpecialDialog(cf)
                    else
                        Toast.makeText(applicationContext, application.getString(R.string.select_text_to_apply_effects),Toast.LENGTH_SHORT).show()

                }
            })

                heading.setOnClickListener({
                    showHeadingSelectionPopUp()
                })

                close.setOnClickListener({
                    if (isValidTab()) {
                        val cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                        if (cf.hasUnsavedChanges.value ?: false) {
                            showUnsavedDialog(cf)
                        } else {
                            closeTab()
                        }

                    }
                })

                bold.setOnClickListener({
                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        cf.boldClicked()

                        binding.textEditorBottam.bold.apply {

                            if (cf.isBoldEnabled)
                                this.setBackgroundResource(R.drawable.round_btn)
                            else this.background = null
                        }
                    }

                })
                italic.setOnClickListener({

                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        cf.italicClicked()


                        binding.textEditorBottam.italic.apply {

                            if (cf.isItalicEnabled) this.setBackgroundResource(R.drawable.round_btn)
                            else this.background = null
                        }
                    }

//                binding.editText.apply {
//                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart,selectionEnd , flag)
//                }
                })
                underline.setOnClickListener({
                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        cf.underlineClicked()

                        binding.textEditorBottam.underline.apply {
                            if (cf.isUnderlineEnabled) this.setBackgroundResource(R.drawable.round_btn)
                            else this.background = null
                        }
                    }

                })
                strikethrough.setOnClickListener {
                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        cf.strikeThroughClicked()
//
                        binding.textEditorBottam.strikethrough.apply {
                            if (cf.isStrikethroughEnabled) this.setBackgroundResource(R.drawable.round_btn)
                            else this.background = null
                        }
                    }
//
                }

            bullets.setOnClickListener {
                if (isValidTab()) {
                    var cf =
                        adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
//                    cf.makeBullets(true)   // by jayesh

                    cf.setBullets()
                    binding.textEditorBottam.bullets.apply {
                        if (cf.isBulletsOn)
                            this.setBackgroundResource(R.drawable.round_btn)
                        else
                            this.background = null
                    }
                }
            }
            alignCenter.setOnClickListener({

                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                        cf.alignCenter()
                    }
//
                })

                alignLeft.setOnClickListener({
                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        cf.alignLeft()
                    }
//
                })
                alignRight.setOnClickListener({
                    if (isValidTab()) {
                        var cf =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        cf.alignRight()
                    }
//
                })

            hyperlink.setOnClickListener({

                if (isValidTab()) {
                    var cf = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                    if(cf.isSelected()) {
                        if (cf.removeIfUrlSpan()) {

                            Toast.makeText(applicationContext, application.getString(R.string.url_removed), Toast.LENGTH_SHORT).show()
                        } else {
                            askForUrl(cf)
                        }
                    }
                    else
                    {
                        Toast.makeText(applicationContext, application.getString(R.string.please_select_text), Toast.LENGTH_SHORT).show()
                    }
                }

            })

//            quote.setOnClickListener({
//
//                if (isValidTab()) {
//                    var cf =
//                        adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
//                    cf.quoteSpan()
//                }
//
//            })




            colorText.setOnClickListener({
                    pickColor()
                })
            colorBackground.setOnClickListener({
                pickColor(background=true)
            })
            textFont.setOnClickListener({
//
                showFontSelectionPopUp()
//
            })



        }
    }

    private fun viewSpecialDialog(cf:EditorFragment) {


        var view = binding.textEditorBottam.textSize as View
        val popup = android.widget.PopupMenu(applicationContext,view)
        popup.inflate(R.menu.special_layout_menu)


        popup.setOnMenuItemClickListener { item ->
            if(isValidTab()) {
                val currentFragment =
                    adapter.fragmentList[binding.tabLayout.selectedTabPosition] as EditorFragment


                when (item.itemId) {

                    R.id.smaller -> {
                        cf.small()
                    }
                    R.id.bigger -> {
                        cf.big()
                    }
                    R.id.sub_script -> {
                        cf.subScript()
                    }
                    R.id.super_script -> {
                        cf.superScript()
                    }
                    R.id.normal-> {
                        cf.normal()
                    }

                }
            }
            false
        }
        popup.show()



    }


    fun pickColor(background: Boolean=false) {

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        val editor = sharedPreferences.edit()

        val v:View = binding.textEditorBottam.root as View
        ColorPickerPopup.Builder(this)
            .initialColor(sharedPreferences.getInt("color",Color.RED)) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(true) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(false)
            .build()
            .show(v, object : ColorPickerPopup.ColorPickerObserver() {
                override fun onColorPicked(color: Int) {

                    editor.putInt("color",color)
                    editor.commit()

                    if (isValidTab()) {
                        var cf = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                            if (!cf.isSelected()) {
                                Toast.makeText(applicationContext, application.getString(R.string.select_text_to_apply_effects), Toast.LENGTH_SHORT).show()
                            }
                            else {
                                if(background==false)
                                    cf.setColor(color)
                                else
                                    cf.setBackgroundColor(color)
                            }
                        }
                    }

                fun onColor(color: Int, fromUser: Boolean) {}
            })



    }
    private fun init() {


        model =
            ViewModelProvider(this, MyViewModelFactory(this.application)).get(
                MyViewModel::class.java
            )
        helper  = Utils(this)
        if (!helper.isStoragePermissionGranted()) helper.takePermission(applicationContext)

        toolbar = findViewById(R.id.toolbar)
        adapter = FragmentAdapter(fragmentManager = supportFragmentManager, lifecycle = lifecycle)
        adapter.fragmentList = arrayListOf()
        binding.pager2.adapter = adapter
        binding.pager2.offscreenPageLimit=1
        binding.pager2.isUserInputEnabled = false

        val v:View  = binding.noTabLayout.cl1
        v.setOnClickListener({
            try {
                val dir = File(applicationContext.filesDir, "note")
                if (!dir.exists())
                    dir.mkdir()
                var count = 1
                var file = File(dir, "untitled" + count + ".html")
                while (file.exists()) {
                    count++
                    file = File(dir, "untitled" + count + ".html")
                }
                makeBlankFragment("untitled"+count+".html")
            }
            catch (e:Exception)
            {
                Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "newFileLauncher: ${e.toString()}.")
            }

        })
        val v2:View = binding.noTabLayout.cl2
        v2.setOnClickListener({
            if (!helper.isStoragePermissionGranted()) helper.takePermission(applicationContext)

            if (helper.isStoragePermissionGranted()) chooseFile()

        })
        manager = ReviewManagerFactory.create(applicationContext)


        setDefaultToolbarTitle()
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_navigation)
        }

        setSupportActionBar(toolbar)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                changeNoTabLayout()
                binding.pager2.setCurrentItem(tab!!.position, true)
                Log.e(TAG, "onTabSelected: position ${tab.position}")
                toolbar.apply {
                    if (isValidTab()) {
                        (adapter.fragmentList.get(tab.position) as EditorFragment).apply {
                            title = getFileName()
                            subtitle = ""
                        }
                    }
                }
                changeBottamBarColor()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {


            }
            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
//        TabLayoutMediator(binding.tabLayout, binding.pager2) { tab, position ->
//            (adapter.fragmentList[position]).apply{
//                var fileName = (this as EditorFragment).getFileName()
//                if(hasUnsavedChanges.value == true) fileName = "*$fileName"
//
//                tab.apply {
//                    if (customView == null) {
//                        setCustomView(R.layout.tab_layout)
//                    }
//                    customView!!.findViewById<TextView>(R.id.file_name).setText(fileName)
//                }
//            }
//        }.attach()




        binding.contextualBottomNavigation.setOnItemSelectedListener(object :
            NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var currentFragment: EditorFragment? = null

                if (isValidTab()) {
                    currentFragment =
                        adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                }

                if (currentFragment != null)
                    return when (item.itemId) {

                        R.id.up -> {
                            if (indexList.isNotEmpty()) {
                                index = currentFragment!!.highlight(
                                    findText,
                                    indexList.last(),
                                    true
                                )//TODO : remaining
                                indexList.remove(indexList.last())
                            }
//                            Toast.makeText(this@MainActivity, "up", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.down -> {
                            down(currentFragment!!)
//                            Toast.makeText(this@MainActivity, "down", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.replace -> {
                            index = currentFragment!!.findReplace(
                                findText,
                                replaceText,
                                index,
                                ignoreCase
                            )
                            down(currentFragment!!)
                            if (index == -1) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "search not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                                if (actionMode != null)
                                    actionMode!!.finish()
                            }

//                            Toast.makeText(this@MainActivity, "replace", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.replace_all -> {

                            currentFragment!!.replaceAll(findText, replaceText, ignoreCase)
                            if (actionMode != null)
                                actionMode!!.finish()
//                            Toast.makeText(this@MainActivity, "replace all", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }

                return false
            }

        })


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)





    }
    private fun changeBottamBarColor() {
        if(isValidTab())
        {
            var cf= adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

            binding.textEditorBottam.apply {
                if(cf.isBoldEnabled)
                    bold.setBackgroundResource(R.drawable.round_btn)
                else bold.background = null

                if(cf.isItalicEnabled)
                    italic.setBackgroundResource(R.drawable.round_btn)
                else italic.background = null

                if(cf.isUnderlineEnabled)
                    underline.setBackgroundResource(R.drawable.round_btn)
                else underline.background = null

                if(cf.isStrikethroughEnabled)
                    strikethrough.setBackgroundResource(R.drawable.round_btn)
                else strikethrough.background = null



            }

        }
    }
    private fun changeNoTabLayout() {
        binding.apply {


            if(tabLayout.tabCount>0) {
                (noTabLayout.root).visibility = View.GONE
                constraintLayoutMain.visibility = View.VISIBLE
            }
            else{
                (noTabLayout.root).visibility = View.VISIBLE
                constraintLayoutMain.visibility = View.GONE
            }

        }
    }
    fun makeBlankFragment(fileName: String)
    {

        Log.e(TAG, "makeBlankFragment: " )
        val dataFile = DataFile(
            fileName = fileName,
            filePath = "note",
            uri = null,
            data = Utils.htmlToSpannable(applicationContext," "),
            isNote = true

        )
        val fragment = EditorFragment(dataFile,applicationContext,hasUnsavedChanges = true,model)
        adapter.addFragment(fragment)



        binding.tabLayout.apply {
            this.addTab(newTab())
            setCustomTabLayout(tabCount - 1, fileName)
            adapter.notifyItemInserted(tabCount - 1)
            selectTab(getTabAt(tabCount - 1))
        }
        fragment.hasUnsavedChanges.observe(this@MainActivity) {
            if (it) {
                setCustomTabLayout(binding.tabLayout.tabCount-1, "*${fragment.getFileName()}")
            }else setCustomTabLayout(binding.tabLayout.tabCount-1, fragment.getFileName())
        }


    }


    private fun showUnsavedDialog(currentFragment: EditorFragment) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Unsaved File")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        val view = LayoutInflater.from(this).inflate(R.layout.unsaved_dialog, null, false)


        builder.setView(view)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            run {
                saveFile(currentFragment, currentFragment.getUri(), false, true)
                //closeTab()
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            run {
                closeTab()
                dialogInterface.dismiss()
            }
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()


    }
    private fun seekbarDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("TextSize")
        builder.setIcon(R.drawable.ic_textsize)

        val view = LayoutInflater.from(this).inflate(R.layout.seekbar_layout, null, false)
        val textView = view.findViewById<TextView>(R.id.textview_seekbar)
        val seekBar = view.findViewById<SeekBar>(R.id.seekbar)

        if(isValidTab())
        {
            val currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            val textsize = currentFragment.getTextSize(applicationContext)
            textView.text = textsize.toString()
            textView.textSize=textsize
            seekBar.progress = textsize.toInt()

        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                textView.setText(progress.toString())
                textView.textSize=progress*(1.0f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        builder.setView(view)

        builder.setPositiveButton("Apply") {  dialogInterface, which ->

            if(isValidTab())
            {
                val currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                currentFragment.changeTextSize(seekBar.progress*(1.0f))
            }

        }
        //performing cancel actio
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }



        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()


    }

    val noteActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                if(intent!=null) {
                    val fileName = intent.getStringExtra("file_name")
                    Log.e(TAG, "$fileName: ")
                    if(fileName!=null)
                    createFragmentFromNote(fileName)

                }
            }
        }
    fun createFragmentFromNote(fileName: String,isReload: Boolean = false){

        val dir = File(applicationContext.filesDir,"note")
        if(dir.exists())
        {
        val file= File(dir,fileName)
            if(file.exists()) {
                val bufferedReader = BufferedReader(FileReader(file))
                val content = java.lang.StringBuilder()
                bufferedReader.forEachLine {
                    content.append(it + '\n')
                }

                var note  = model.getNoteByName(fileName)
                if(note==null)
                    note= Note(fileName)

                Log.e(TAG, "createFragmentFromNote: $content")
                val dataFile = DataFile(
                    fileName = fileName,
                    filePath = "note",
                    uri = null,
                    data = Utils.htmlToSpannable(applicationContext, content.toString()),
                    isNote = true,
                    font = note.font,
                    textSize = note.textSize

                )
                val fragment = EditorFragment(dataFile, applicationContext, viewModel = model)
                if (isReload && isValidTab()) {
                    val position = binding.tabLayout.selectedTabPosition
                    adapter.fragmentList.removeAt(position)
                    adapter.fragmentList.add(position, fragment)
                    setCustomTabLayout(position, "$fileName")
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.addFragment(fragment)
                    binding.tabLayout.apply {
                        addTab(newTab())
                        setCustomTabLayout(tabCount - 1, fileName)
                        adapter.notifyItemInserted(tabCount - 1)
                        selectTab(getTabAt(tabCount - 1))
                    }
                }
            }
        }

    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        Log.e(TAG, "onNavigationItemSelected: outside ")
        when (item.itemId) {



            R.id.nav_storage_manager -> {
                if (!helper.isStoragePermissionGranted()) helper.takePermission(applicationContext)

                if (helper.isStoragePermissionGranted()) chooseFile()
            }
            R.id.nav_feedback -> {
                feedback()
            }
            R.id.note_list -> {
                Log.e(TAG, "onNavigationItemSelected: clicked")
                val intent = Intent(this@MainActivity, NoteActivity::class.java)
                noteActivityLauncher.launch(intent)

            }

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    private fun initFontPopUpMenu(popup: android.widget.PopupMenu, res:Int, title: String)
    {
        var menuItem  = popup.menu.findItem(res)
        val ss = SpannableStringBuilder(title)
        val typeface = Utils.getTypefaceFromName(applicationContext,title)
        ss.setSpan(CustomTypefaceSpan(typeface,title),0,ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        menuItem.title= SpannableString(ss)
    }

    private fun initHeading(popup: android.widget.PopupMenu, res:Int,size:Float,text:String)
    {
        var menuItem  = popup.menu.findItem(res)
        val ss = SpannableStringBuilder(text)
        ss.setSpan(RelativeSizeSpan(size),0,ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        menuItem.title= SpannableString(ss)
    }


    private fun showFontSelectionPopUp() {
        //val view = findViewById<View>(item.itemId)
        var view = binding.textEditorBottam.textFont as View
        val courier = "Courier New"
        val helvetica = "Helvetica"
        val georgia = "Georgia"
        val timesnew = "Times New Roman"
        val garamond = "Garamond"
        val arial = "Arial"
        val tahoma = "Tahoma"
        val verdana = "Verdana"
        val brushscript = "Brush Script MT"
        val trebuchet = "Trebuchet MS"
        val popup = android.widget.PopupMenu(applicationContext,view)
        popup.inflate(R.menu.font_selection_menu)
        initFontPopUpMenu(popup,R.id.courier,"Courier New")
        initFontPopUpMenu(popup,R.id.helvetica,"Helvetica")
        initFontPopUpMenu(popup,R.id.georgia,"Georgia")
        initFontPopUpMenu(popup,R.id.times_new_roman,"Times New Roman")
        initFontPopUpMenu(popup,R.id.garamond,"Garamond")
        initFontPopUpMenu(popup,R.id.arial,"Arial")
        initFontPopUpMenu(popup,R.id.tahoma,"Tahoma")
        initFontPopUpMenu(popup,R.id.verdana,"Verdana")
        initFontPopUpMenu(popup,R.id.brush_script_mt,"Brush Script MT")
        initFontPopUpMenu(popup,R.id.trebuchet_ms,"Trebuchet MS")
        initFontPopUpMenu(popup,R.id.festive,Utils.festive)
        initFontPopUpMenu(popup,R.id.great_vibes,Utils.greatvibes)
        initFontPopUpMenu(popup,R.id.love_light,Utils.lovelight)
        initFontPopUpMenu(popup,R.id.ole,Utils.ole)
        initFontPopUpMenu(popup,R.id.sacramento,Utils.sacramento)
        popup.setOnMenuItemClickListener { item ->
           if(isValidTab()) {
               val currentFragment =
                   adapter.fragmentList[binding.tabLayout.selectedTabPosition] as EditorFragment


               when (item.itemId) {

                   R.id.normal -> {

                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont("default")
                       else
                           currentFragment.applyFontEdittext("")

                   }

                   R.id.georgia -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.georgia)
                       else
                           currentFragment.applyFontEdittext("georgia")
                   }
                   R.id.arial -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.arial)
                       else
                           currentFragment.applyFontEdittext("arial")
                   }
                   R.id.courier -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.courier)
                       else
                           currentFragment.applyFontEdittext(courier)
                   }
                   R.id.helvetica -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.helvetica)
                       else
                           currentFragment.applyFontEdittext("helvetica")
                   }
                   R.id.times_new_roman -> {
                       if (!currentFragment.isSelected())
                           currentFragment.settingFont(Utils.timesnew)
                       else
                           currentFragment.applyFontEdittext(timesnew)

                   }
                   R.id.brush_script_mt -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.brushscript)
                       else
                           currentFragment.applyFontEdittext(brushscript)
                   }
                   R.id.verdana -> {
                       if (!currentFragment.isSelected())
                           currentFragment.settingFont(Utils.verdana)
                       else
                           currentFragment.applyFontEdittext("verdana")
                   }
                   R.id.garamond -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.garamond)
                       else
                           currentFragment.applyFontEdittext(garamond)
                   }
                   R.id.tahoma -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.tahoma)
                       else
                           currentFragment.applyFontEdittext(tahoma)
                   }
                   R.id.trebuchet_ms -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.trebuchet)
                       else
                           currentFragment.applyFontEdittext(trebuchet)
                   }
                   R.id.festive -> {
                       if ( !currentFragment.isSelected())
                           currentFragment.settingFont(Utils.festive)
                       else
                           currentFragment.applyFontEdittext(Utils.festive)
                   }
                   R.id.great_vibes -> {
                   if ( !currentFragment.isSelected())
                       currentFragment.settingFont(Utils.greatvibes)
                   else
                       currentFragment.applyFontEdittext(Utils.greatvibes)
               }
                   R.id.love_light -> {
                   if ( !currentFragment.isSelected())
                       currentFragment.settingFont(Utils.lovelight)
                   else
                       currentFragment.applyFontEdittext(Utils.lovelight)
               }
                   R.id.ole -> {
                   if ( !currentFragment.isSelected())
                       currentFragment.settingFont(Utils.ole)
                   else
                       currentFragment.applyFontEdittext(Utils.ole)
               }
                   R.id.sacramento -> {
                   if ( !currentFragment.isSelected())
                       currentFragment.settingFont(Utils.sacramento)
                   else
                       currentFragment.applyFontEdittext(Utils.sacramento)
               }


               }
           }
            false
        }
        popup.show()

    }

    private fun showHeadingSelectionPopUp() {
        //val view = findViewById<View>(item.itemId)

        var view = binding.textEditorBottam.heading as View
        val popup = android.widget.PopupMenu(applicationContext,view)
        popup.inflate(R.menu.heading_menu)
        initHeading(popup,R.id.h1,Utils.heading[0],"Heading 1")
        initHeading(popup,R.id.h2,Utils.heading[1],"Heading 2")
        initHeading(popup,R.id.h3,Utils.heading[2],"Heading 3")
        initHeading(popup,R.id.h4,Utils.heading[3],"Heading 4")
        initHeading(popup,R.id.h5,Utils.heading[4],"Heading 5")
        popup.setOnMenuItemClickListener { item ->
            if(isValidTab()) {
                val currentFragment =
                    adapter.fragmentList[binding.tabLayout.selectedTabPosition] as EditorFragment

                when (item.itemId) {

                    R.id.h1 -> {
                        if (isValidTab()) {
                            currentFragment.makeH1(Utils.heading[0])
                        }
                    }
                    R.id.h2-> {
                        if (isValidTab()) {
                            currentFragment.makeH1(Utils.heading[1])
                        }
                    }
                    R.id.h3 -> {
                        if (isValidTab()) {
                            currentFragment.makeH1(Utils.heading[2])
                        }
                    }
                    R.id.h4 -> {
                        if (isValidTab()) {
                            currentFragment.makeH1(Utils.heading[3])
                        }
                    }
                    R.id.h5 -> {
                        if (isValidTab()) {
                            currentFragment.makeH1(Utils.heading[4])
                        }
                    }
                    R.id.h6 -> {
                        if (isValidTab()) {
                            currentFragment.makeH1(Utils.heading[5])
                        }
                    }
                    R.id.normal ->
                    {
                        if(isValidTab())
                        {
                            currentFragment.removeHeadingSpan()
                        }
                    }

                }
            }
            false
        }
        popup.show()
    }

    private fun down(currentFragment: EditorFragment) {
        if (index != -1) {
            indexList.add(index)
            index = currentFragment.highlight(findText, index + 1, ignoreCase)
        }
        if (index == -1) {
            indexList.clear()
            index = 0
            index = currentFragment.highlight(findText, index, ignoreCase)
        }
    }
    private fun closeTab() {
        binding.tabLayout.apply {
            if (isValidTab()) {
                adapter.apply {
                    fragmentList.removeAt(selectedTabPosition)
                    notifyItemRemoved(selectedTabPosition)
                }
                removeTabAt(selectedTabPosition)
                if (tabCount == 0) {
                    setDefaultToolbarTitle()
                }
            }
        }

    }
    private fun setDefaultToolbarTitle() {
        toolbar.apply {
            setTitle(R.string.greet_line)
            changeNoTabLayout()
        }
    }
    private fun isValidTab(): Boolean {
        binding.tabLayout.apply {
            if (tabCount > 0 && selectedTabPosition >= 0 && selectedTabPosition < adapter.itemCount)
                return true
            return false
        }
    }
    override fun onResume() {
        super.onResume()

        //lifecycleScope.launch(Dispatchers.Main){


        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if(model.isWrap!=preferences.getBoolean("word_wrap",true)) {
            model.isWrap = !model.isWrap
            recreate()
        }
        if(model.isLineNumber!=preferences.getBoolean("line_number",false)) {
            model.isLineNumber = !model.isLineNumber
            recreate()
        }

            model.isHistoryLoaded.observe(this@MainActivity) {
                adapter.fragmentList = model.getFragmentList().value ?: arrayListOf()
                binding.tabLayout.apply {
                    if (tabCount > 0) {
                        Log.e(TAG, "onResume: selectedtabposition $selectedTabPosition")
                        model.currentTab = selectedTabPosition
                    }
                }
                if(model.currentTab>=0 && model.currentTab<adapter.fragmentList.size)
                    binding.pager2.currentItem = model.currentTab
//                if(adapter.fragmentList!=null)
                createTabsInTabLayout(adapter.fragmentList)


                for ((count, frag) in model.getFragmentList().value!!.withIndex()) {
                    val fragment = frag as EditorFragment
                    fragment.hasUnsavedChanges.observe(this@MainActivity) {
                        if (it) {
                            setCustomTabLayout(count, "*${fragment.getFileName()}")
                        }else setCustomTabLayout(count, fragment.getFileName())
                    }

                    fragment.hasLongPress.observe(this@MainActivity) {
                        if (it) {

                                startActionMode(actionModeCallbackCopyPaste)
                                fragment.hasLongPress.value = false
                        }
                    }
                    fragment.cursorChanged.observe(this@MainActivity) {
                        if (it) {
                            changeColorBottamText()
                            fragment.cursorChanged.value = false
                        }
                    }

                }

//                if (binding.tabLayout.tabCount == 0 && adapter.fragmentList.size==0) {
//
//                    val dir = File(applicationContext.filesDir, "note")
//                    if (!dir.exists())
//                        dir.mkdir()
//                    var count = 1
//                    var file = File(dir, "untitled" + count + ".html")
//                    while (file.exists()) {
//                        count++
//                        file = File(dir, "untitled" + count + ".html")
//                    }
//                    makeBlankFragment("untitled" + count + ".html")
//                }

                Log.e(TAG, "onResume: called")

                binding.tabLayout.apply {
                    if (model.currentTab >= 0 && model.currentTab < tabCount)
                        selectTab(getTabAt(model.currentTab))
                }


            }


    }
    private fun changeColorBottamText() {
        Log.e(TAG, "changeColorBottamText: called")
        if (isValidTab() ) {
            var cf =
                adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment


            binding.textEditorBottam.apply {
                this.bold.background=null
                this.italic.background=null
                this.strikethrough.background=null
                this.underline.background=null
                this.hyperlink.background=null
                this.bullets.background=null
            }

            cf.isBoldEnabled = false
            cf.isItalicEnabled = false
            cf.isUnderlineEnabled = false
            cf.isStrikethroughEnabled = false
            cf.isBulletsOn = false

            val spans = cf.getCurrentSpan()
            if (spans != null) {
                for (span in spans) {
                    if (span is StyleSpan) {
                        val spn = span as StyleSpan
                        if (spn.style == Typeface.BOLD) {
                            binding.textEditorBottam.bold.setBackgroundResource(R.drawable.round_btn)
                            cf.isBoldEnabled = true
                        } else if (spn.style == Typeface.ITALIC) {
                            binding.textEditorBottam.italic.setBackgroundResource(R.drawable.round_btn)
                            cf.isItalicEnabled = true
                        }

                    } else if (span is CustomUnderlineSpan) {
                        binding.textEditorBottam.underline.setBackgroundResource(R.drawable.round_btn)
                        cf.isUnderlineEnabled = true

                    } else if (span is StrikethroughSpan) {
                        binding.textEditorBottam.strikethrough.setBackgroundResource(R.drawable.round_btn)
                        cf.isStrikethroughEnabled = true
                    }
                    else if(span is URLSpan)
                    {
                        binding.textEditorBottam.hyperlink.setBackgroundResource(R.drawable.round_btn)
                    }
                }

            }

            val paragraphStyleSpans = cf.getCurrentParagraphStyleSpan()
            if(paragraphStyleSpans != null ){
                for (span in paragraphStyleSpans){
                    if(span is BulletSpan){
                        binding.textEditorBottam.bullets.setBackgroundResource(R.drawable.round_btn)
                        cf.isBulletsOn = true
                    }
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: called")

        if (isValidTab()) {
            model.currentTab = binding.tabLayout.selectedTabPosition
        }

        for (frag in adapter.fragmentList) {
            val fragment = frag as EditorFragment
            fragment.saveDataToDataFile()
        }
        model.addHistories(applicationContext)
    }
    override fun onDestroy() {

        // saving all files to databse
        model.setFragmentList(adapter.fragmentList)

        // saving current tab
        if (adapter.fragmentList.size > 0)
            model.currentTab = binding.pager2.currentItem


        super.onDestroy()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        this.menu = menu
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.open -> showPopupMenu(item, R.menu.open_file_menu)
            R.id.edit -> showPopupMenu(item, R.menu.edit_menu)
            R.id.overflow_menu -> showPopupMenu(item, R.menu.overflow_menu)
        }
        return super.onOptionsItemSelected(item)
    }
    private fun showPopupMenu(menuItem: MenuItem, menuResourceId: Int) {
        val view = findViewById<View>(menuItem.itemId)
        val contextThemeWrapper = ContextThemeWrapper(this,R.style.ToolbarPopUpTheme)
        val popup = PopupMenu(contextThemeWrapper, view)

        popup.inflate(menuResourceId)


        popup.setOnMenuItemClickListener { item -> //TODO : list all action for menu popup
//            Log.e(TAG, "onMenuItemClick: " + item.title)
            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList[binding.tabLayout.selectedTabPosition] as EditorFragment
            }

            when (item.itemId) {

                R.id.open -> {

                    if (!helper.isStoragePermissionGranted()) helper.takePermission(applicationContext)

                    if (helper.isStoragePermissionGranted()) chooseFile()
//                    Log.e(TAG, "showPopupMenu: open called")
                }

                R.id.save_as -> {
                    if(currentFragment!=null)
                        saveAsDialog(currentFragment.getFileExtension())
                }

                R.id.save_as_note -> {
                    if(currentFragment!=null)
                    {
                        try {

                           saveAsNoteFile(currentFragment.getFileName(),currentFragment)
                        }
                        catch (e:java.lang.Exception)
                        {
                            Log.e(TAG, "showPopupMenu: ${e.message}")
                        }

                    }

                }

                R.id.save -> {
                    if (currentFragment != null) {
                        if (currentFragment.hasUnsavedChanges.value != false) {
                                saveFile(
                                    currentFragment,
                                    currentFragment.getUri(),
                                    isHtml = (currentFragment.getFileExtension() == ".html")
                                )
                            }

                        }
                        else
                            Toast.makeText(this, application.getString(R.string.no_change_found), Toast.LENGTH_SHORT).show()
                    }

                R.id.close -> {
                    if (currentFragment != null) {
                        if (currentFragment.hasUnsavedChanges.value ?: false) {
                            showUnsavedDialog(currentFragment)
                        } else {
                            closeTab()
                        }
                    }
                }
                R.id.new_file -> {
                    //TODO : remaining ....

                    val dir = File(applicationContext.filesDir, "note")
                    if (!dir.exists())
                        dir.mkdir()
                    var count = 1
                    var file = File(dir, "untitled" + count + ".html")

                        while (file.exists()) {
                            count++
                            file = File(dir, "untitled" + count + ".html")
                        }
                        var i=0
                        while(i < adapter.fragmentList.size)
                        {
                            if("untitled" + count + ".html" == (adapter.fragmentList.get(i) as EditorFragment).getFileName() ){
                                count++;
                                i=0;
                            }
                            else {
                                i++
                            }
                        }

                    makeBlankFragment("untitled"+count+".html")
//                    try {
//
//
//                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                            addCategory(Intent.CATEGORY_OPENABLE)
//                            type = "*/*"
//                            putExtra(Intent.EXTRA_TITLE, "new.txt")
//                        }
//                        newFileLauncher.launch(intent)
//                    }
//                    catch (e:Exception)
//                    {
//                        Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
//                        Log.e(TAG, "newFileLauncher: ${e.toString()}.")
//                    }
                }
                R.id.paste -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val dataToPaste = clipboardManager.primaryClip?.getItemAt(0)?.text
                    if (currentFragment !== null) {
                        currentFragment.insertSpecialChar(dataToPaste.toString())
                    }

                }

                R.id.reload -> {

                    if (currentFragment != null)
                        reloadFile(currentFragment)

                }
                R.id.refresh ->
                {
                    if (currentFragment != null)
                        currentFragment.invalidateEditText()
                }
                R.id.rename -> {


                    if(currentFragment != null)
                    {
                        if(isValidTab())
                        openRenameDialog(currentFragment)
                    }

                }

                R.id.print->{
                    if (currentFragment != null) {
                        doWebViewPrint(currentFragment)
                        if(model.isWrap==true)
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.please_keep_word_wrap_on), Toast.LENGTH_SHORT).show()
                    }


                }

                R.id.wrap_content -> {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val editor = preferences.edit()
                    editor.putBoolean("word_wrap", !model.isWrap)
                    editor.apply()
                    onResume()
                }

                R.id.line_number -> {

                    val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    val editor = preferences.edit()
                    editor.putBoolean("line_number", !model.isLineNumber)
                    editor.apply()
                    onResume()

                }


                R.id.copy -> {


                    if (currentFragment !== null) {
                        val selectedData = currentFragment.getSelectedData()
                        if (selectedData != null) copy(selectedData)
                    }

                }
                R.id.select_all -> {
                    //TODO : remaining ...
                    if (currentFragment != null) {
                        currentFragment.selectAll()
                        actionMode=startActionMode(actionModeCallbackCopyPaste)
                    }
                }

                R.id.search -> {
                    if (currentFragment != null)
                        search(currentFragment, false)

                }
                R.id.search_replace -> {
                    if (currentFragment != null)
                        search(currentFragment, true)
                }

//                R.id.settings -> {
//                    Log.e(TAG, "onNavigationItemSelected: clicked")
//                    val intent: Intent = Intent(this@MainActivity, SettingActivity::class.java)
//                    startActivity(intent)
//                }
                R.id.share -> {

                    if (currentFragment != null ) {

                        val prefix= currentFragment.getFileName().substringBeforeLast('.')
                        val suffix=currentFragment.getFileExtension()
                        val file = File.createTempFile(prefix,suffix,applicationContext.cacheDir)

                        file.bufferedWriter().use {

                            if(currentFragment.getFileExtension()==".html") {
                                it.write(
                                    Utils.spannableToHtml(
                                        currentFragment.getEditable() ?: SpannableStringBuilder(
                                            ""
                                        )
                                    )
                                )
                            }
                            else
                            {
                                it.write(currentFragment.getEditable().toString())
                            }
                        }


                        ShareCompat.IntentBuilder(this)
                            .setStream(FileProvider.getUriForFile(applicationContext,BuildConfig.APPLICATION_ID+".provider",file))
                            .setType(URLConnection.guessContentTypeFromName(currentFragment.getFileName()))
                            .startChooser()

                    }


                }
//                R.id.undo_change -> {
//                    if(currentFragment!=null)
//                    {
//                        currentFragment.undoChanges()
//                        actionMode = startActionMode(actionModeCallbackUndoRedo)
//                    }
//                }
//                R.id.redo_change->{
//
//                    if(currentFragment!=null)
//                    {
//                        currentFragment.redoChanges()
//                        actionMode = startActionMode(actionModeCallbackUndoRedo)
//                    }
//
//                }


            }
            false
        }
        val menuHelper: Any
        val argTypes: Array<Class<*>?>
        try {
            val fMenuHelper = PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper.isAccessible = true
            menuHelper = fMenuHelper[popup]
            argTypes = arrayOf(Boolean::class.javaPrimitiveType)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes)
                .invoke(menuHelper, true)
        } catch (e: Exception) {
        }
        popup.show()
    }

    private fun openRenameDialog(currentFragment: EditorFragment) {



        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter File Name")
        builder.setIcon(R.drawable.ic_search)
        val view = LayoutInflater.from(this).inflate(R.layout.file_name_input_dialog, null, false)
        val editText = view.findViewById<EditText>(R.id.file_name)
        editText.setText("untitled"+currentFragment.getFileExtension())
        builder.setView(view)
        builder.setPositiveButton("OK") { dialogInterface, which ->
            run {
                try {
                    findText = editText.text.toString()
                    if(currentFragment.isNote()==false) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            currentFragment.getUri()?.let {
                                DocumentsContract.renameDocument(
                                    contentResolver,
                                    it, findText
                                )
                            }
                            Toast.makeText(
                                applicationContext,
                                "File Name Changed Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            setCustomTabLayout(binding.tabLayout.selectedTabPosition,findText)
                            currentFragment.changeFilename(findText)
                        }
                    }
                    else
                    {

                       model.renameNoteFile(applicationContext,findText,currentFragment)
                        Toast.makeText(
                            applicationContext,
                            "File Name Changed Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        setCustomTabLayout(binding.tabLayout.selectedTabPosition,findText)
                        currentFragment.changeFilename(findText)

                    }
                }
                catch (e:Exception)
                {
                    Toast.makeText(applicationContext, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                }
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(editText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !TextUtils.isEmpty(s)
            }
        })



    }


    private fun reloadFile(currentFragment: EditorFragment) {
            val uri = currentFragment.getUri()
            if(uri!=null)
                readFileUsingUri(uri,false,true)
            else if(currentFragment.isNote())
            {
                createFragmentFromNote(fileName = currentFragment.getFileName(),isReload=true)
            }

    }
    private fun doWebViewPrint(currentFragment: EditorFragment) {
        // Create a WebView object specifically for printing
        val webView = WebView(this)
        val webSettings = webView.settings


        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

            override fun onPageFinished(view: WebView, url: String) {
                createWebPrintJob(view,currentFragment)

                mWebView = null

                Log.e(TAG, "page finished loading $url")

            }
        }

        val textSize = currentFragment.getTextSizeForPrint(applicationContext)

        // Generate an HTML document on the fly:
        val str1 = "<html><head><style type=\"text/css\">" +
                "@font-face {font-family:courier new;src: url(\"file:///android_asset/cour.ttf\")}  " +
                "@font-face {font-family:arial;src: url(\"file:///android_asset/arial.ttf\")}"+
                "@font-face {font-family:${Utils.brushscript};src: url(\"file:///android_asset/brush_script_mt_kursiv.ttf\")}  " +
                "@font-face {font-family:garamond;src: url(\"file:///android_asset/garamond_regular.ttf\")}"+
                "@font-face {font-family:helvetica;src: url(\"file:///android_asset/helvetica.ttf\")}  " +
                "@font-face {font-family:tahoma;src: url(\"file:///android_asset/tahoma.ttf\")}"+
                "@font-face {font-family:${Utils.timesnew};src: url(\"file:///android_asset/times_new_roman.ttf\")}  " +
                "@font-face {font-family:${Utils.trebuchet};src: url(\"file:///android_asset/trebuc.ttf\")}"+
                "@font-face {font-family:verdana;src: url(\"file:///android_asset/verdana.ttf\")}  " +
                "@font-face {font-family:georgia;src: url(\"file:///android_asset/georgia.ttf\")}  " +

                "body {font-family:${currentFragment.fontFamily};font-size:${textSize}px}"+
                "</style></head><body>"
        Log.e(TAG, "doWebViewPrint: fontsize : ${currentFragment.getTextSize(this)} fontfamily : ${currentFragment.fontFamily}")

        val str2 = "</body></html>";

        val htmlDocument = Utils.spannableToHtml(currentFragment.getEditable()!!)
        val myHtmlString = str1 + htmlDocument + str2;


        webView.loadDataWithBaseURL(null, myHtmlString, "text/HTML", "UTF-8", null)

        // Keep a reference to WebView object until you pass the PrintDocumentAdapter
        // to the PrintManager
        mWebView = webView
    }
    private fun createWebPrintJob(webView: WebView,currentFragment: EditorFragment) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Get a PrintManager instance
            (this?.getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.let { printManager ->

                val jobName = currentFragment.getFileName().substringBeforeLast('.')

                // Get a print adapter instance
                val printAdapter = webView.createPrintDocumentAdapter(jobName)

                // Create a print job with name and adapter instance
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder().build()
                ).also { printJob ->

                    // Save the job object for later status checking
                    //printJobs += printJob
                }
            }
        }
        else{

        }
    }
    val resLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val clipData = result.data?.clipData
                Log.e(TAG, "clipdata: $clipData")
                if (clipData != null) {
                    Log.e(TAG, "${clipData.itemCount}: ")
                        var i = 0
                        while (i < clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            if (uri !== null) readFileUsingUri(uri)
                            i++
                        }
                }
                else{
                    val uri = result.data?.data
                    if(uri!=null)
                        readFileUsingUri(uri)
                }
            }
        }
    private fun readFileUsingUri(uri: Uri,isOuterFile : Boolean = false,isReload : Boolean = false) {
        Log.e(TAG, "readFileUsingUri: $uri" )
        try {
            val takeFlags: Int =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                applicationContext.contentResolver.takePersistableUriPermission(uri, takeFlags)

        } catch (e: Exception) {

            e.printStackTrace()
        }

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileSize: Int = inputStream!!.available()
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val data:StringBuilder = java.lang.StringBuilder()

            bufferedReader.forEachLine {
                data.append(it)
                data.append("\n")
            }


            val fileName: String = helper.queryName(contentResolver, uri)
            var content : Spanned = SpannableStringBuilder()
            val fileExtension = when(val index = fileName.lastIndexOf(".")){
                -1 -> String()
                else -> fileName.substring(index)
            }

            if(fileExtension == ".html" || fileExtension == ".htm"){
                content = Utils.htmlToSpannable(applicationContext,data.toString())
            }else{
                content = SpannableStringBuilder(data)
            }

            val dataFile = DataFile(
                fileName = fileName,
                filePath = uri.path!!,
                uri = uri,
                data = content,
                isNote = false
            )
            val fragment = EditorFragment(dataFile,applicationContext, viewModel = model)

            if (isReload && isValidTab()) {
                val position = binding.tabLayout.selectedTabPosition
                adapter.fragmentList.removeAt(position)
                adapter.fragmentList.add(position, fragment)
                setCustomTabLayout(position, "$fileName")
                adapter.notifyDataSetChanged()

            } else {
                adapter.addFragment(fragment)
                binding.tabLayout.apply {
                    addTab(newTab())
                    setCustomTabLayout(tabCount - 1, fileName)
                    adapter.notifyItemInserted(tabCount - 1)
                    selectTab(getTabAt(tabCount - 1))
                    if (isOuterFile) {
                        model.getFragmentList().value?.add(fragment)
                        model.currentTab = tabCount - 1
                    }
                }


            }
            fragment.hasUnsavedChanges.observe(this) {
                if (it)
                    setCustomTabLayout(binding.tabLayout.selectedTabPosition, "*$fileName")
                else setCustomTabLayout(binding.tabLayout.selectedTabPosition, "$fileName")
            }
            fragment.hasLongPress.observe(this@MainActivity){
                if (it) {
                    startActionMode(actionModeCallbackCopyPaste)
                    fragment.hasLongPress.value = false


                }
            }
            fragment.cursorChanged.observe(this@MainActivity) {
                if (it) {
                    changeColorBottamText()
                    fragment.cursorChanged.value=false
                }


            }

            Log.e(
                TAG,
                "readFileUsingUri : tab layout selected position : ${binding.tabLayout.selectedTabPosition}"
            )
        }
        catch (e:Exception)
        {
            Toast.makeText(applicationContext,"${e.message.toString()}",Toast.LENGTH_SHORT).show()
        }

    }
    private fun chooseFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/text")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            resLauncher.launch(intent)
        } else {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).setType(mimeType)
            resLauncher.launch(intent)
        }
    }
    private fun createTabsInTabLayout(list: MutableList<Fragment>) {
        binding.tabLayout.removeAllTabs()

        if (binding.tabLayout.tabCount == 0) {

            binding.tabLayout.apply {
                list.forEach {
                    val frag = it as EditorFragment
                    addTab(newTab())
                    setCustomTabLayout(tabCount-1, "*${frag.getFileName()}")
                }
            }

        }
        if (model.currentTab >= 0 && model.currentTab < adapter.fragmentList.size) {
            binding.pager2.setCurrentItem(model.currentTab)
        }
        adapter.notifyDataSetChanged()

//        TabLayoutMediator(binding.tabLayout,binding.pager2){
//            tab , position ->
//            tab.text = list[position].getFileName()
//        }.attach()
    }
    private fun setCustomTabLayout(position: Int, fileName: String) {
        binding.tabLayout.apply {
            if (position >= 0 && position < tabCount) {
                val tab = getTabAt(position)
                tab?.apply {
                    if (customView == null) {
                        setCustomView(R.layout.tab_layout)
                    }
                    customView!!.findViewById<TextView>(R.id.file_name).setText(fileName)
                }
            }
        }
    }
    val saveAsSystemPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) {
//                    Log.e(TAG, "save as sytem picker: uri -> $uri")
                    if (isValidTab()) {
                        val fragment =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        saveAsFile(fragment, uri,true)
                    }
                }
            }
        }
    val saveAsSystemPickerLauncherTxt =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) {
//                    Log.e(TAG, "save as sytem picker: uri -> $uri")
                    if (isValidTab()) {
                        val fragment =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        saveAsFile(fragment, uri,false)
                    }
                }
            }
        }
    val newFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) readFileUsingUri(uri)
            }
        }
    private fun saveFile(
        fragment: EditorFragment,
        uri: Uri?,
        isCloseFlag: Boolean = false,
        isHtml:Boolean = true
    ) {



        Log.e(TAG, "saveFile: isNote ${fragment.isNote()}")

        if (uri !== null) {

            Log.e(TAG, "saveFile: saving external file" )
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    applicationContext.contentResolver.takePersistableUriPermission(uri!!, takeFlags)
                contentResolver.openFileDescriptor(uri!!, "wt")?.use {
                    FileOutputStream(it.fileDescriptor).use {
                        if(isHtml)
                        {
                            it.write(
                                fragment.getEditable()?.let { it1 -> Utils.spannableToHtml(it1).toByteArray() }
                            )
                        }
                        else {
                            it.write(
                                fragment.getEditTextData().toString().toByteArray()
                            )
                            Toast.makeText(applicationContext, application.getString(R.string.please_save_as_to_keep_effect), Toast.LENGTH_SHORT).show()
                        }

                            showProgressBarDialog("Saved Successfully", isCloseFlag)
                        if(!isHtml)
                            Toast.makeText(applicationContext, application.getString(R.string.please_save_as_to_keep_effect), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: FileNotFoundException) {
                Toast.makeText(applicationContext, application.getString(R.string.file_doesnot_saved), Toast.LENGTH_SHORT).show()
                Log.e(TAG, " file not found saveFile: ${e.message}")
                e.printStackTrace()

            } catch (e: IOException) {
                Toast.makeText(applicationContext, application.getString(R.string.file_doesnot_saved), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                Log.e(TAG, " io exception saveFile: ${e.message}")

            } catch (e: SecurityException) {

              saveAsFile(fragment,uri,isHtml)

            } catch (e: Exception) {
                Toast.makeText(applicationContext, application.getString(R.string.file_doesnot_saved), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                Log.e(TAG, "saveFile unknown exception: ${e.message}")

            }
        }
        else if(fragment.isNote()) {

            Log.e(TAG, "saveFile: in Note saving " )
            try {
                val uniqueFileName = fragment.getFileName()
                val dir = File(applicationContext.filesDir, "note")
                if (!dir.exists())
                    dir.mkdir()
                var file = File(dir, uniqueFileName)
                if (!file.exists()) {
                    createNewNoteFile(uniqueFileName, fragment)
                    //file.createNewFile()
                } else {
                    file.bufferedWriter().use {
                        it.write(
                            Utils.spannableToHtml(
                                fragment.getEditable() ?: SpannableStringBuilder(
                                    ""
                                )
                            )
                        )
                    }
                    model.addNote(Note(fragment.getFileName(),fragment.fontFamily,fragment.getTextSize(applicationContext)))
                    model.updateNoteList()
                    Toast.makeText(applicationContext, application.getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                }

            }
            catch(e:Exception)
            {
                Log.e(TAG, "saveFile: ${e.message}")
            }
        }
        else
        {
            saveAsDialog(".txt")
        }
        if (isValidTab()) setCustomTabLayout(
            binding.tabLayout.selectedTabPosition,
            fragment.getFileName()
        )

    }

    private fun saveAsFile(fragment: EditorFragment,uri: Uri?,isHtml: Boolean=true)
    {

        Log.e(TAG, "saveFile: isNote ${fragment.isNote()}")

        if (uri != null) {

            Log.e(TAG, "saveFile: saving external file" )
            try {
                val takeFlags: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    applicationContext.contentResolver.takePersistableUriPermission(uri!!, takeFlags)
                contentResolver.openFileDescriptor(uri!!, "wt")?.use {
                    FileOutputStream(it.fileDescriptor).use {
                        if(isHtml)
                        {
                            it.write(
                                fragment.getEditable()?.let { it1 -> Utils.spannableToHtml(it1).toByteArray() }
                            )
                        }
                        else {
                            it.write(
                                fragment.getEditTextData().toString().toByteArray()
                            )
                            //Toast.makeText(applicationContext, application.getString(R.string.please_save_as_to_keep_effect), Toast.LENGTH_SHORT).show()
                        }

                        showProgressBarDialog("Saved Successfully", false)
                        if(!isHtml)
                            Toast.makeText(applicationContext, application.getString(R.string.please_save_as_to_keep_effect), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: FileNotFoundException) {
                Toast.makeText(applicationContext, application.getString(R.string.file_doesnot_saved), Toast.LENGTH_SHORT).show()
                Log.e(TAG, " file not found saveFile: ${e.message}")
                e.printStackTrace()

            } catch (e: IOException) {
                Toast.makeText(applicationContext, application.getString(R.string.file_doesnot_saved), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                Log.e(TAG, " io exception saveFile: ${e.message}")

            } catch (e: SecurityException) {

                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(applicationContext, application.getString(R.string.file_doesnot_saved), Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                Log.e(TAG, "saveFile unknown exception: ${e.message}")

            }
        }
        else if(fragment.isNote()) {

            Log.e(TAG, "saveFile: in Note saving " )
            try {
                val uniqueFileName = fragment.getFileName()
                val dir = File(applicationContext.filesDir, "note")
                if (!dir.exists())
                    dir.mkdir()
                var file = File(dir, uniqueFileName)
                if (!file.exists()) {
                    createNewNoteFile(uniqueFileName, fragment)
                    //file.createNewFile()
                } else {
                    file.bufferedWriter().use {
                        it.write(
                            Utils.spannableToHtml(
                                fragment.getEditable() ?: SpannableStringBuilder(
                                    ""
                                )
                            )
                        )
                    }
                    model.addNote(Note(fragment.getFileName(),fragment.fontFamily,fragment.getTextSize(applicationContext)))
                    model.updateNoteList()
                    Toast.makeText(applicationContext, application.getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                }

            }
            catch(e:Exception)
            {
                Log.e(TAG, "saveFile: ${e.message}")
            }
        }


    }



    private fun askForUrl(fragment: EditorFragment) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Enter Url")
        builder.setIcon(R.drawable.ic_insert_link)
        val view = LayoutInflater.from(this).inflate(R.layout.file_name_input_dialog, null, false)
        val editText = view.findViewById<EditText>(R.id.file_name)
        editText.setText("https://")
        builder.setView(view)
        builder.setPositiveButton("OK") { dialogInterface, which ->
            run {
                findText = editText.text.toString()
                fragment.urlSpan(findText)
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(editText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !TextUtils.isEmpty(s)
            }
        })



    }private fun createNewNoteFile(uniqueFileName: String, fragment: EditorFragment) {
        val dir = File(applicationContext.filesDir, "note")
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Enter File Name")
        builder.setIcon(R.drawable.ic_search)
        val view = LayoutInflater.from(this).inflate(R.layout.file_name_input_dialog, null, false)
        val editText = view.findViewById<EditText>(R.id.file_name)
        editText.setText(uniqueFileName)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialogInterface, which ->
            run {
                findText = editText.text.toString()

                var file = File(dir,findText)
                if(!file.exists())
                {
                    file.createNewFile()

                    fragment.setFileName(findText)
                    setCustomTabLayout(binding.tabLayout.selectedTabPosition,findText)
                    model.addNote(Note(fileName = findText,font = fragment.fontFamily,textSize = fragment.getTextSize(applicationContext)))
                    model.updateNoteList()
                }
                else
                {
                    Toast.makeText(applicationContext, application.getString(R.string.file_already_exits), Toast.LENGTH_SHORT).show()
                    createNewNoteFile(uniqueFileName,fragment)
                }

                file.bufferedWriter().use {
                    it.write("${Utils.spannableToHtml(fragment.getEditable()?: SpannableStringBuilder(""))}")
                }

                Toast.makeText(applicationContext, application.getString(R.string.file_saved_as_note), Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(editText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !TextUtils.isEmpty(s)
            }
        })



    }






    private fun saveAsNoteFile(uniqueFileName: String, fragment: EditorFragment) {
        val dir = File(applicationContext.filesDir, "note")
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Enter File Name")
        builder.setIcon(R.drawable.ic_search)
        val view = LayoutInflater.from(this).inflate(R.layout.file_name_input_dialog, null, false)
        val editText = view.findViewById<EditText>(R.id.file_name)
        editText.setText(uniqueFileName)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialogInterface, which ->
            run {
                findText = editText.text.toString()

                var file = File(dir,findText)
                if(!file.exists())
                {
                   model.saveAsNote(applicationContext,binding.tabLayout.selectedTabPosition,findText)
                    model.updateNoteList()
                }
                else
                {
                    Toast.makeText(applicationContext, application.getString(R.string.file_already_exits), Toast.LENGTH_SHORT).show()
                    createNewNoteFile(uniqueFileName,fragment)
                }



                Toast.makeText(applicationContext, application.getString(R.string.file_saved_as_note), Toast.LENGTH_SHORT).show()
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(editText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !TextUtils.isEmpty(s)
            }
        })



    }

    private fun showSecureSaveAsDialog(fragment: EditorFragment) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Security Alert")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        val view = LayoutInflater.from(this).inflate(R.layout.security_alert_dialog, null, false)
        builder.setView(view)
        builder.setPositiveButton(this.getString(R.string.save_as)) { dialogInterface, which ->
            run {
                saveAsIntent(fragment.getFileExtension())
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton(this.getString(R.string.cancel)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()


    }
    private fun saveAsIntent(fileExtension : String) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*" //TODO :
                    putExtra(Intent.EXTRA_TITLE, "untitled${fileExtension}")
                }
                if(fileExtension==".txt")
                    saveAsSystemPickerLauncherTxt.launch(intent)
                else
                    saveAsSystemPickerLauncher.launch(intent)
            } else {
                val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                    putExtra(Intent.EXTRA_TITLE, "untitled${fileExtension}")
                }
                if(fileExtension==".txt")
                    saveAsSystemPickerLauncherTxt.launch(intent)
                else
                    saveAsSystemPickerLauncher.launch(intent)
            }

        }
        catch (e:Exception)
        {
            Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "saveAsIntent: ${e.toString()}.")
        }

    }
    private fun saveAsDialog(ext:String) {

        var extension = ext
        if(extension=="")
            extension=".txt"
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Save As")
        builder.setIcon(R.drawable.ic_save_as)
        val view = LayoutInflater.from(this).inflate(R.layout.save_as_dialog, null, false)
        val radioGroupExtension = view.findViewById<RadioGroup>(R.id.radio_group_extension)
        val radioButton = view.findViewById<RadioButton>(R.id.radio_txt)
        builder.setView(view)
        builder.setPositiveButton(this.getString(R.string.save_as)) { dialogInterface, which ->
            run {

                if(radioGroupExtension.checkedRadioButtonId==R.id.radio_html)
                    saveAsIntent(".html")
                else
                    saveAsIntent(".txt")
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton(this.getString(R.string.cancel)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()


    }
    private fun showProgressBarDialog(title: String, isCloseFlag: Boolean = false) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.save_successfull, null, false)
        val titleText = view.findViewById<TextView>(R.id.dialog_title)
        titleText.setText(title)
        builder.setView(view)
//        builder.setPositiveButton("Done"){ dialogInterface, which -> dialogInterface.dismiss() }
        // Create the AlertDialog
        alertDialogGlobal = builder.create()
        // Set other dialog properties
        alertDialogGlobal.setCancelable(true)
        alertDialogGlobal.show()
        lifecycleScope.launch(Dispatchers.Main) {
            delay(400)
            alertDialogGlobal.dismiss()
            if (isCloseFlag) {
                closeTab()
            }
        }

    }
    private fun search(currentFragment: EditorFragment, hasReplace: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Search")
        builder.setIcon(R.drawable.ic_search)
        val view = LayoutInflater.from(this).inflate(R.layout.search_dialog, null, false)
        val findEditText = view.findViewById<EditText>(R.id.search_text)
        val replaceEditText = view.findViewById<EditText>(R.id.replace_text)
        val ignoreCaseCheckBox = view.findViewById<CheckBox>(R.id.ignore_case)

        if (hasReplace) {
            replaceEditText.visibility = View.VISIBLE
            builder.setTitle("Search And Replace")
        } else {
            replaceEditText.visibility = View.GONE
        }

        builder.setView(view)
        builder.setPositiveButton("Find") { dialogInterface, which ->
            run {
                findText = findEditText.text.toString()
                replaceText = replaceEditText.text.toString()
                ignoreCase = ignoreCaseCheckBox.isChecked


                actionMode = startActionMode(actionModeCallback)

                actionMode.apply {
                    if(hasReplace)
                        this?.title=" "+findText+" --> "+replaceText
                    else
                        this?.title = "Search : "+findText
                }

                if (!hasReplace)
                    binding.contextualBottomNavigation.apply {
                        this.menu.findItem(R.id.replace).setVisible(false)
                        this.menu.findItem(R.id.replace_all).setVisible(false)
                    }


                index = currentFragment.highlight(findEditText.text.toString(), 0, ignoreCase)
                Log.e(TAG, "search: index : $index")
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(findEditText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        findEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !TextUtils.isEmpty(s)
            }
        })

    }
    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
//            val inflater: MenuInflater = mode.menuInflater
//            inflater.inflate(R.menu.context_menu, menu)

            binding.contextualBottomNavigation.apply {
                visibility = View.VISIBLE
                this.menu.findItem(R.id.replace).setVisible(true)
                this.menu.findItem(R.id.replace_all).setVisible(true)
            }

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.setTitle("Search And Replace")
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
//                R.id.up -> {
//                    Toast.makeText(this@MainActivity, "up", Toast.LENGTH_SHORT).show()
////                    mode.finish() // Action picked, so close the CAB
//                    true
//                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            binding.contextualBottomNavigation.visibility = View.GONE
            actionMode = null
        }
    }
    private val actionModeCallbackUndoRedo = object : ActionMode.Callback {




        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.undo_redo_menu, menu)

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.setTitle("Undo & Redo")
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {


            var currentFragment: EditorFragment? = null
            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            }
            return when (item.itemId) {

                // actioMode
                R.id.undo_change -> {
                    if(currentFragment!=null)
                    {
                        currentFragment.undoChanges()

                    }
                    true
                }
                // actioMode
                R.id.redo_change->{

                    if(currentFragment!=null)
                    {
                        currentFragment.redoChanges()

                    }
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            binding.contextualBottomNavigation.visibility = View.GONE
            actionMode = null
        }
    }
    private val actionModeCallbackCopyPaste = object : ActionMode.Callback {




        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.copy_paste_menu, menu)

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.setTitle("Copy & Paste")
            lifecycleScope.launch(Dispatchers.Main)
            {
                delay(2500)
                mode.finish()
            }

            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {


            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            }

            return when (item.itemId) {

                    // actioMode
                R.id.paste -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val dataToPaste = clipboardManager.primaryClip?.getItemAt(0)?.text
                    if (currentFragment !== null) {
                        currentFragment.pasteData(dataToPaste)
                    }
                    true
                }

                // actioMode
                R.id.copy -> {
                    if (currentFragment !== null) {
                        val selectedData = currentFragment.getSelectedData()
                        if (selectedData != null) copy(selectedData)
                    }
                    true
                }

                // actioMode
                R.id.select_all -> {
                    //TODO : remaining ...
                    if (currentFragment != null) {
                        currentFragment.selectAll()

                    }
                    true
                }
                else ->{
                    true
                }                }

        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            binding.contextualBottomNavigation.visibility = View.GONE
            actionMode = null
        }
    }
    fun copy(textToCopy:CharSequence) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
    }

    suspend fun  cancelCopyPasteAction(){

        delay(1000)
        if(actionMode!=null)
        actionMode!!.finish()

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
            Toast.makeText(applicationContext,"gmail doesn't responed",Toast.LENGTH_SHORT).show()
        }


    }
    fun askForRating() {

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        val editor = sharedPreferences.edit()
        val opened = "opened"
        val key_got_feedback = "got_feedback"
        val num = sharedPreferences.getInt(opened,0)
        editor.putInt(opened,num+1)
        editor.commit()
        val gotFeedback = sharedPreferences.getBoolean(key_got_feedback,false)
        Log.e(TAG,"opened $num times")
        if(num>10 && !gotFeedback) {
            editor.putInt(opened,0)
            editor.commit()
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                         editor.putBoolean(key_got_feedback,true)
                        Log.e(TAG, "feedback: finished")
                    }
                } else {

                    val manager2 = FakeReviewManager(applicationContext)
                    val request2 = manager2.requestReviewFlow()
                    request2.addOnCompleteListener {
                        if (task.isSuccessful) {
                            // We got the ReviewInfo object
                            val reviewInfo = task.result
                            Toast.makeText(
                                applicationContext,
                                reviewInfo.toString(),
                                Toast.LENGTH_LONG
                            )
                                .show()

                            val flow = manager2.launchReviewFlow(this, reviewInfo)
                            flow.addOnCompleteListener { _ ->

                                Log.e(TAG, "feedback: finished")
                            }
                            //Toast.makeText(applicationContext,"Internal Testing version",Toast.LENGTH_LONG).show()
                        }
                        Log.e(TAG, "feedback: error")
                        // There was some problem, log or handle the error code.
//                @ReviewErrorCode val reviewErrorCode = (task.getException() as TaskException).errorCode
                    }
                }


            }
        }


    }
}