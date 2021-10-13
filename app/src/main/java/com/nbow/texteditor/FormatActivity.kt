package com.nbow.texteditor

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.nbow.texteditor.databinding.ActivityFormatBinding

import android.graphics.Typeface

import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.core.content.res.ResourcesCompat
import top.defaults.colorpicker.ColorPickerPopup
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.text.*
import android.text.style.*
import android.view.*
import android.widget.RadioGroup

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import java.io.*
import java.util.*


class FormatActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener {
    private  val TAG = "FormatActivity"

    private lateinit var binding: ActivityFormatBinding
    private var selectedFont: Int =R.font.arial
    private var isBoldEnabled = false
    private  var isItalicEnabled = false
    private var isStrikethroughEnabled = false
    private var isUnderlineEnabled = false
    private var isColorTextEnabled = false
    private var isAlignCenterEnabled = false
    private var isAlignLeftEnabled = true
    private var isAlignRightEnabled = false

    private lateinit var helper : Utils

    val mimeType =
        "text/*"

    val TEXT = "text/*"

    private var supportedMimeTypes = arrayOf(TEXT)

    private var currentUri : Uri? = null

    val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        helper  = Utils(this)
        if (!helper.isStoragePermissionGranted()) helper.takePermission()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Format Mode (BETA VERSION)")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
        navView.setNavigationItemSelectedListener(this)
        drawerLayout.closeDrawer(GravityCompat.START)
        toggle.syncState()
        binding.editText.typeface=ResourcesCompat.getFont(applicationContext,R.font.opensans)

        binding.editText.doOnTextChanged{
            text, start, before, count ->
            run {
                Log.e(TAG, "onCreate: ${text?.subSequence(start,start+count)} $start $before $count ")
                binding.editText.text.apply {
                    if (before<count) { // adding new text
                        if (isBoldEnabled)
                            this.setSpan(StyleSpan(Typeface.BOLD), start+before, start + count, flag)
                        if (isItalicEnabled)
                            this.setSpan(StyleSpan(Typeface.ITALIC), start+before, start + count, flag)

                        if (isUnderlineEnabled)
                            this.setSpan(UnderlineSpan(), start+before, start + count, flag)

                        if (isStrikethroughEnabled)
                            this.setSpan(StrikethroughSpan(), start+before , start+ count, flag)


                    val typeface = Typeface.create(ResourcesCompat.getFont(applicationContext,selectedFont),Typeface.NORMAL)
                    this.setSpan(CustomTypefaceSpan(typeface),start,start+count,flag)
                    }
                }
            }

        }



        binding.textEditorBottam.apply {

            bold.setOnClickListener({
                binding.editText.apply {
                    if(selectionStart!=selectionEnd) isBoldEnabled=!isBoldEnabled
                }
                binding.textEditorBottam.bold.apply {
                    if(isBoldEnabled)
                        this.setBackgroundColor(Color.RED)
                    else setBackgroundColor(Color.GRAY)
                }

                changeSelectedTextStyle(bold=true)

            })
            italic.setOnClickListener({
                binding.editText.apply {
                    if(selectionStart!=selectionEnd) isItalicEnabled=!isItalicEnabled
                }

                binding.textEditorBottam.italic.apply {

                    if(isItalicEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }
                changeSelectedTextStyle(italic = true)
//                binding.editText.apply {
//                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart,selectionEnd , flag)
//                }
            })
            underline.setOnClickListener({
                binding.editText.apply {
                    if(selectionStart!=selectionEnd) isUnderlineEnabled = !isUnderlineEnabled
                }
                binding.textEditorBottam.underline.apply {
                    if(isUnderlineEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }
                changeSelectedTextStyle(underline = true)

            })
            strikethrough.setOnClickListener({

                binding.editText.apply {
                    if(selectionStart!=selectionEnd) isStrikethroughEnabled = !isStrikethroughEnabled
                }
                binding.textEditorBottam.strikethrough.apply {
                    if(isStrikethroughEnabled) setBackgroundColor(Color.GREEN)
                    else setBackgroundColor(Color.GRAY)
                }
                changeSelectedTextStyle(strikethrough = true)
            })
            alignCenter.setOnClickListener({
                changeAlignmentValue(center = true)
                changeParagraphStyle(alignCenter = true)

            })

            alignLeft.setOnClickListener({
                changeAlignmentValue(left = true)
                changeParagraphStyle(alignLeft = true)
            })
            alignRight.setOnClickListener({
                changeAlignmentValue(right = true)
                changeParagraphStyle(alignRight = true)
            })
            colorText.setOnClickListener({
                pickColor()
            })
            textFont.setOnClickListener({
                binding.editText.apply {
                    showFontSelectionPopUp()
                }
            })


        }


    }

    fun changeAlignmentValue(left:Boolean = false ,right:Boolean = false ,center:Boolean = false){
        isAlignLeftEnabled = left
        isAlignRightEnabled = right
        isAlignCenterEnabled = center
    }

    fun getCurrentCursorLine(selectionPosition : Int):Int
    {

        Log.e(TAG, "getCurrentCursorLine: $selectionPosition", )
        val layout = binding.editText.layout

        if(selectionPosition != -1)
        {
            return layout.getLineForOffset(selectionPosition)
        }
        return -1;
    }

    fun changeSelectedTextStyle(bold : Boolean = false,italic: Boolean = false,underline : Boolean = false,strikethrough : Boolean = false){
        binding.editText.apply {


            if(selectionEnd != selectionStart) {

                if((bold && !isBoldEnabled) || (italic && !isItalicEnabled) || (strikethrough && !isStrikethroughEnabled)  || (underline && !isUnderlineEnabled)) {
                    var next: Int

                    var i = selectionStart
                    while (i < selectionEnd) {

                        // find the next span transition
                        next = text.nextSpanTransition(i, selectionEnd, CharacterStyle::class.java)

                        val spans: Array<CharacterStyle> = text.getSpans(i, next, CharacterStyle::class.java)

                        for (span in spans) {

                            if (span is StyleSpan) {
                                val spn = span as StyleSpan
                                if ((spn.style == Typeface.BOLD && bold) || (spn.style == Typeface.ITALIC && italic))
                                    text.removeSpan(spn)
                            }else if((span is UnderlineSpan && underline) || (span is StrikethroughSpan && strikethrough)){
                                text.removeSpan(span)
                            }
                        }
                        i = next
                    }

                }else if(bold)
                    text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, flag)
                else if(italic)
                    text.setSpan(StyleSpan(Typeface.ITALIC), selectionStart, selectionEnd, flag)
                else if(underline)
                    text.setSpan(UnderlineSpan(), selectionStart, selectionEnd, flag)
                else if(strikethrough)
                    text.setSpan(StrikethroughSpan(), selectionStart, selectionEnd, flag)
                

            }
        }
    }

    fun changeParagraphStyle(alignCenter:Boolean=false,alignLeft:Boolean=false,alignRight:Boolean = false,){
        binding.editText.apply {
            if(selectionEnd != selectionStart) {

                if( (alignCenter  || alignLeft || alignRight)  ) {
                    var next: Int

                    var i = selectionStart
                    while (i < selectionEnd) {

                        // find the next span transition
                        next = text.nextSpanTransition(i, selectionEnd, ParagraphStyle::class.java)

                        val spans: Array<ParagraphStyle> = text.getSpans(i, next, ParagraphStyle::class.java)

                        for (span in spans) {

                            if (span is AlignmentSpan) {
                                text.removeSpan(span)
                            }
                        }
                        i = next
                    }
                }

            }
//            Log.e(TAG, "changeParagraphStyle: ${getCurrentCursorLine()}", )
            
            binding.editText.apply {
                val currentLine=getCurrentCursorLine(selectionStart)
                val endLine = getCurrentCursorLine(selectionEnd)
                var start = this.layout.getLineStart(currentLine)
                var end = this.layout.getLineEnd(endLine)
                Log.e(TAG, "changeParagraphStyle: $start $end" )
                if(end==-1) end = binding.editText.text.length
                if(alignCenter) text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),start,end,flag)
                else if(alignLeft) text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),start,end,flag)
                else if(alignRight) text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),start,end,flag)
            }
        }
    }

    private fun initFontPopUpMenu(popup: PopupMenu, res:Int, fontId : Int,title : String)
    {
        var menuItem  = popup.menu.findItem(res)
        val ss = SpannableStringBuilder(title)
        val typeface = Typeface.create(ResourcesCompat.getFont(applicationContext,fontId),Typeface.NORMAL)
        ss.setSpan(CustomTypefaceSpan(typeface),0,ss.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        menuItem.title=SpannableString(ss)
    }

    private fun showFontSelectionPopUp() {

        val popup = androidx.appcompat.widget.PopupMenu(this, binding.textEditorBottam.textFont)
        popup.inflate(R.menu.font_selection_menu)
        initFontPopUpMenu(popup,R.id.helvetica_bold,R.font.helvetica_bold,"Helvetica bold")
        initFontPopUpMenu(popup,R.id.helvetica,R.font.helvetica,"Helvetica")
        initFontPopUpMenu(popup,R.id.georgia,R.font.georgia,"Georgia")
        initFontPopUpMenu(popup,R.id.opensans,R.font.opensans,"OpenSans")
        initFontPopUpMenu(popup,R.id.raleway,R.font.raleway,"Raleway")
        initFontPopUpMenu(popup,R.id.arial,R.font.arial,"Arial")
        initFontPopUpMenu(popup,R.id.calibri,R.font.helvetica,"Calibri")
        initFontPopUpMenu(popup,R.id.verdana,R.font.verdana,"Verdana")
        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {

                R.id.georgia -> {
                    applyFontEdittext(R.font.georgia)
                }
                R.id.arial->{
                    applyFontEdittext(R.font.arial)
                }
                R.id.helvetica_bold->{
                   applyFontEdittext(R.font.helvetica_bold)
                }
                R.id.helvetica->{
                    applyFontEdittext(R.font.helvetica)
                }
                R.id.opensans->{
                    applyFontEdittext(R.font.opensans)
                }
                R.id.raleway->{
                    applyFontEdittext(R.font.raleway)
                }
                R.id.verdana->{
                    applyFontEdittext(R.font.verdana)
                }
                R.id.calibri->{
                    applyFontEdittext(R.font.calibri)
                }
            }
            false
        }
        popup.show()

    }

    private fun applyFontEdittext(fontRes: Int) {
        binding.editText.apply {
            if(selectionStart!=selectionEnd) {
                val myTypeface = Typeface.create(
                    ResourcesCompat.getFont(context, fontRes),
                    Typeface.NORMAL
                )
                (text as Spannable).setSpan(
                    CustomTypefaceSpan(myTypeface),
                    selectionStart,
                    selectionEnd,
                    flag
                )
            }
            else
            {
                selectedFont=fontRes
            }

        }
    }
    


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.open -> showPopupMenu(item, R.menu.open_file_menu)
            R.id.edit -> showPopupMenu(item, R.menu.edit_menu)
            R.id.overflow_menu -> showPopupMenu(item, R.menu.overflow_menu)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun chooseFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes)
            resLauncher.launch(intent)
        } else {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).setType(mimeType)
            resLauncher.launch(intent)
        }
    }

    val resLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                var uri = intent?.data
                this.currentUri = uri
                if (uri !== null) readFileUsingUri(uri)

            }
        }

    private fun readFileUsingUri(uri: Uri,isOuterFile : Boolean = false,isReload : Boolean = false) {

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
            val listOfLines: MutableList<String> = arrayListOf()
            val listOfPageData: MutableList<String> = arrayListOf()

            bufferedReader.forEachLine {
                listOfLines.add(it)
            }

            val temp = StringBuilder("")
            var count = 0
            for (line in listOfLines) {
                temp.append(line)
                count++
                if (count >= 3000 || temp.length >= 500000) { // 500kb
//                Log.e(TAG, "readFileUsingUri: temp : at $count : $temp")
                    listOfPageData.add(temp.toString())
                    temp.clear()
                    count = 0
                } else temp.append("\n")
            }
            if (temp.length > 0) {
                listOfPageData.add(temp.toString())
            }
            if (listOfLines.size == 0) {
                listOfPageData.add(temp.toString())
            }

            val fileName: String = helper.queryName(contentResolver, uri)
            binding.editText.apply {
//                Log.e(TAG, "readFileUsingUri: page 1 : ${listOfPageData[0]}")
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
                    setText(Html.fromHtml(listOfPageData[0],Html.FROM_HTML_MODE_LEGACY))
                else
                    setText(HtmlCompat.fromHtml(listOfPageData[0],HtmlCompat.FROM_HTML_MODE_LEGACY))
            }

//            val dataFile = DataFile(
//                fileName = fileName,
//                filePath = uri.path!!,
//                uri = uri,
//                listOfPageData = listOfPageData
//            )
//            val fragment = EditorFragment(dataFile)

//            if (isReload && isValidTab()) {
//                val position = binding.tabLayout.selectedTabPosition
//                adapter.fragmentList.removeAt(position)
//                adapter.fragmentList.add(position, fragment)
//                setCustomTabLayout(position, "$fileName")
//                adapter.notifyDataSetChanged()
//
//            } else {
//                adapter.addFragment(fragment)
//                binding.tabLayout.apply {
//                    addTab(newTab())
//                    setCustomTabLayout(tabCount - 1, fileName)
//                    adapter.notifyItemInserted(tabCount - 1)
//                    selectTab(getTabAt(tabCount - 1))
//                    if (isOuterFile) {
//                        model.getFragmentList().value?.add(fragment)
//                        model.currentTab = tabCount - 1
//                    }
//                }
//
//                model.addRecentFile(
//                    RecentFile(
//                        0,
//                        uri.toString(),
//                        fileName,
//                        Calendar.getInstance().time.toString(),
//                        fileSize
//                    )
//                )
//            }
//            fragment.hasUnsavedChanges.observe(this) {
//                if (it)
//                    setCustomTabLayout(binding.tabLayout.selectedTabPosition, "*$fileName")
//                else setCustomTabLayout(binding.tabLayout.selectedTabPosition, "$fileName")
//            }
//            fragment.hasLongPress.observe(this@MainActivity){
//                if(it) {
//                    startActionMode(actionModeCallbackCopyPaste)
//                    fragment.hasLongPress.value = false
//                }
//            }
//            Log.e(
//                TAG,
//                "readFileUsingUri : tab layout selected position : ${binding.tabLayout.selectedTabPosition}"
//            )
        }
        catch (e:Exception)
        {
            Toast.makeText(applicationContext,"${e.message.toString()}",Toast.LENGTH_SHORT).show()
        }

    }

    private fun showPopupMenu(item: MenuItem, menuResourceId: Int) {
        val view = findViewById<View>(item.itemId)
        val popup = PopupMenu(this, view)

        popup.inflate(menuResourceId)

        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val isWrap = preference.getBoolean("word_wrap", false)

        val item = popup.menu.findItem(R.id.go_to_line)
        if (item != null) {
            item.setVisible(!isWrap)
        }
        popup.setOnMenuItemClickListener { item -> //TODO : list all action for menu popup
//            Log.e(TAG, "onMenuItemClick: " + item.title)
//            var currentFragment: EditorFragment? = null
//
//            if (isValidTab()) {
//                currentFragment =
//                    adapter.fragmentList[binding.tabLayout.selectedTabPosition] as EditorFragment
//            }

            when (item.itemId) {

                R.id.open -> {

                    if (!helper.isStoragePermissionGranted()) helper.takePermission()

                    if (helper.isStoragePermissionGranted()) chooseFile()
//                    Log.e(TAG, "showPopupMenu: open called")
                }
                R.id.save_as -> {
                    saveAsDialog()
                }
                R.id.save -> {
                    if(currentUri!=null) saveFile(currentUri)
                    else Toast.makeText(this, "current uri null", Toast.LENGTH_SHORT).show()
//                    if (currentFragment != null) {
//                        if (currentFragment.hasUnsavedChanges.value != false)
//                            saveFile(currentFragment, currentFragment.getUri())
//                        else
//                            Toast.makeText(this, "No Changes Found", Toast.LENGTH_SHORT).show()
//                    }
                }
                R.id.close -> {
//                    if (currentFragment != null) {
//                        if (currentFragment.hasUnsavedChanges.value ?: false) {
//                            showUnsavedDialog(currentFragment)
//                        } else {
//                            closeTab()
//                        }
//                    }
                }
                R.id.new_file -> {
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
//                        Log.e(TAG, "newFileLauncher: ${e.toString()}.", )
//                    }
                }
                R.id.paste -> {
//                    val clipboardManager =
//                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                    val dataToPaste = clipboardManager.primaryClip?.getItemAt(0)?.text
//                    if (currentFragment !== null) {
//                        currentFragment.insertSpecialChar(dataToPaste.toString())
//                    }

                }

                R.id.reload -> {
//                    if (currentFragment != null)
//                        reloadFile(currentFragment)
                }

                R.id.copy -> {
//                    if (currentFragment !== null) {
//                        val selectedData = currentFragment.getSelectedData()
//                        if (selectedData != null) copy(selectedData)
//                    }

                }
                R.id.select_all -> {
//                    if (currentFragment != null) {
//                        currentFragment.selectAll()
//                        actionMode=startActionMode(actionModeCallbackCopyPaste)
//                    }
                }
                R.id.go_to_line -> {
//                    gotoLine()
                }
                R.id.search -> {
//                    if (currentFragment != null)
//                        search(currentFragment, false)

                }
                R.id.search_replace -> {
//                    if (currentFragment != null)
//                        search(currentFragment, true)
                }
                R.id.run->{
//                    val intent:Intent = Intent(this,WebViewActivity::class.java)
//                    if(currentFragment!=null) {
//                        intent.putExtra("data", currentFragment.getEditTextData().toString())
//                        startActivity(intent)
//                    }

                }
                R.id.settings -> {
                    Log.e(TAG, "onNavigationItemSelected: clicked")
                    val intent: Intent = Intent(this@FormatActivity, SettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.share -> {
//                    if (currentFragment != null) {
//                        ShareCompat.IntentBuilder(this)
//                            .setStream(currentFragment.getUri())
//                            .setType(URLConnection.guessContentTypeFromName(currentFragment.getFileName()))
//                            .startChooser()
//                    }

                }
                R.id.undo_change -> {
//                    if(currentFragment!=null)
//                    {
//                        currentFragment.undoChanges()
//                        actionMode = startActionMode(actionModeCallbackUndoRedo)
//                    }
                }
                R.id.redo_change->{

//                    if(currentFragment!=null)
//                    {
//                        currentFragment.redoChanges()
//                        actionMode = startActionMode(actionModeCallbackUndoRedo)
//                    }

                }


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

    private fun saveAsIntent(fileExtension : String) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*" //TODO :
                    putExtra(Intent.EXTRA_TITLE, "untitled${fileExtension}")
                }
                saveAsSystemPickerLauncher.launch(intent)
            } else {
                val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                    putExtra(Intent.EXTRA_TITLE, "untitled${fileExtension}")
                }
                saveAsSystemPickerLauncher.launch(intent)
            }

        }
        catch (e:Exception)
        {
            Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "saveAsIntent: ${e.toString()}.", )
        }

    }

    val saveAsSystemPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) {
                    saveFile(uri, isSaveAs = true)
                }
            }
        }

    private fun saveFile(
        uri: Uri?,
        isSaveAs: Boolean = false,
        isCloseFlag: Boolean = false
    ) {
        var fileExtension = ".txt" // assumption
        if (uri !== null) {
            try {
                uri.path.apply {
                    if(this!=null)
                        fileExtension = substring(this.lastIndexOf("."));
                }
                val takeFlags: Int =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    applicationContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
                if(fileExtension==".html") {
                    contentResolver.openFileDescriptor(uri, "wt")?.use {
                        FileOutputStream(it.fileDescriptor).use {
                            it.write(
                                HtmlCompat.toHtml(binding.editText.text,HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE).toByteArray()
                            )
                        }
                    }
                }else{
                    contentResolver.openFileDescriptor(uri, "wt")?.use {
                        FileOutputStream(it.fileDescriptor).use {
                            it.write(
                                binding.editText.text.toString().toByteArray()
                            )
//                            if (!isSaveAs)
//                                fragment.hasUnsavedChanges.value = false
//                            if (isValidTab()) setCustomTabLayout(
//                                binding.tabLayout.selectedTabPosition,
//                                fragment.getFileName()
//                            )
////                        Toast.makeText(applicationContext, "File Saved", Toast.LENGTH_SHORT).show()
//
//                            showProgressBarDialog("Saved Successfully", isCloseFlag)
                        }
                    }

                }
            } catch (e: FileNotFoundException) {
                Toast.makeText(applicationContext, "File Doesn't Saved", Toast.LENGTH_SHORT).show()
                e.printStackTrace()

            } catch (e: IOException) {
                Toast.makeText(applicationContext, "File Doesn't Saved", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: SecurityException) {
                showSecureSaveAsDialog()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "File Doesn't Saved", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun showSecureSaveAsDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Security Alert")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        val view = LayoutInflater.from(this).inflate(R.layout.security_alert_dialog, null, false)


        builder.setView(view)

        builder.setPositiveButton(this.getString(R.string.save_as)) { dialogInterface, which ->
            run {
                saveAsDialog()
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

    private fun saveAsDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Save As")
        builder.setIcon(R.drawable.ic_save_as)

        val view = LayoutInflater.from(this).inflate(R.layout.save_as_dialog, null, false)
        val radioGroupExtension = view.findViewById<RadioGroup>(R.id.radio_group_extension)

        builder.setView(view)

        builder.setPositiveButton(this.getString(R.string.save_as)) { dialogInterface, which ->
            run {
                //TODO : according to radio btn
                if(radioGroupExtension.checkedRadioButtonId==R.id.radio_html)
                    saveAsIntent(".html") //TODO : .txt.html
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
       return false
    }

    fun pickColor() {
        val v:View = binding.editText
        ColorPickerPopup.Builder(this)
            .initialColor(Color.RED) // Set initial color
            .enableBrightness(true) // Enable brightness slider or not
            .enableAlpha(false) // Enable alpha slider or not
            .okTitle("Choose")
            .cancelTitle("Cancel")
            .showIndicator(true)
            .showValue(false)
            .build()
            .show(v, object : ColorPickerPopup.ColorPickerObserver() {
                override fun onColorPicked(color: Int) {
                    binding.editText.text.setSpan(ForegroundColorSpan(color), binding.editText.selectionStart,binding.editText.selectionEnd , flag)
                }
                fun onColor(color: Int, fromUser: Boolean) {}
            })


    }

}


