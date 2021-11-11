package com.nbow .texteditor

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.style.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager

import android.view.View.OnLongClickListener
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


class EditorFragment : Fragment {

    private val TAG = "EditorFragment"
    lateinit var mcontext: Context
    public var selectedFont: String = "default"
    public var isBulletsOn:Boolean =false;
    var isBoldEnabled = false
    var isItalicEnabled = false
    var isStrikethroughEnabled = false
    var isUnderlineEnabled = false
    var isColorTextEnabled = false
    var isAlignCenterEnabled = false
    var isAlignLeftEnabled = true
    var isAlignRightEnabled = false
    val flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    var isTextChanged = false
    var fontFamily = "default"

    private var editText : EditText? = null
    var hasUnsavedChanges = MutableLiveData(false)
    var hasLongPress = MutableLiveData<Boolean>(false)
    var cursorChanged = MutableLiveData<Boolean>(false)
    //private var undoRedo=TextViewUndoRedo()

    private var dataFile : DataFile? = null

    fun setDataFile(dataFile: DataFile){
        this.dataFile = dataFile
    }

    fun getEditTextData():StringBuilder{
        val temp =java.lang.StringBuilder()
        temp.append(editText!!.text.toString())
        return temp
    }
    fun selectAll(){
//        Log.e(TAG, "selectAll: ")
        editText?.requestFocus()
        editText?.selectAll()
    }



    constructor(){
        Log.e(TAG, "constructor of fragment called: $this")
    }

    constructor(dataFile: DataFile, application:Context, hasUnsavedChanges : Boolean = false){
        this.dataFile = dataFile
        this.hasUnsavedChanges.postValue(hasUnsavedChanges)
        this.mcontext=application
    }


    override fun onDestroyView() {
        saveDataToDataFile()
        super.onDestroyView()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.clear()
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {


        if (savedInstanceState != null) {
            savedInstanceState.clear()
        }

        super.onViewStateRestored(savedInstanceState)
        // data initializing to edit text first time when attach to view
        if(dataFile!=null && editText!=null){
            editText!!.setText(dataFile!!.data)
            editText!!.requestFocus()
        //undoRedo.mIsUndoOrRedo = false
        }

        if(dataFile!=null)
            settingFont(dataFile!!.font)

        editText?.setOnLongClickListener(OnLongClickListener {
            hasLongPress.value = true
            false
        })




        editText?.setAccessibilityDelegate(object : View.AccessibilityDelegate() {
            override fun sendAccessibilityEvent(host: View?, eventType: Int) {
                super.sendAccessibilityEvent(host, eventType)
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED && !isTextChanged){
                    cursorChanged.value = true
                }
                
                isTextChanged = false
            }
        })



        editText?.doOnTextChanged { text, start, before, count ->

            isTextChanged = true
            Log.e(TAG, "onViewStateRestored: do on text changed listner", )
            hasUnsavedChanges.value = true



            run {
                lifecycleScope.launch(Main) {

                    editText?.text?.apply {



                        if (before < count) { // adding new text

//                            if(isBulletsOn) {
//                                Log.e(TAG, "onViewStateRestored: onTextchanged ${text!![start]}", )
//                                if (text!![start] == '\n' || text!![start+count] == '\n') {
//                                    makeBullets()
//                                }
//                            }
                            if(isBulletsOn) {
                                val se = editText!!.selectionStart
                                if (se > 0 && text!![se - 1] == '\n') {
                                    makeBullets()
                                }
                            }
                            
                            var underlineSpans = getSpans(start,start+count,UnderlineSpan::class.java)
                            if(underlineSpans!=null){
                                for(span in underlineSpans){
                                    if(span is UnderlineSpan && !(span is CustomUnderlineSpan)){
                                        removeSpan(span)
                                    }
                                }
                            }

                            if (isBoldEnabled) {
                                Log.e(TAG, "onViewStateRestored: Bold is enabled", )
                                this.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    start + before,
                                    start + count,
                                    flag
                                )
                            }
                            if (isItalicEnabled){
                                Log.e(TAG, "onViewStateRestored: Italic Enabled", )
                                this.setSpan(
                                    StyleSpan(Typeface.ITALIC),
                                    start + before,
                                    start + count,
                                    flag
                                )}

                            if (isUnderlineEnabled){
                                Log.e(TAG, "onViewStateRestored: Underline enabled", )
                                this.setSpan(CustomUnderlineSpan(), start + before, start + count, flag)}

                            if (isStrikethroughEnabled) {
                                Log.e(TAG, "onViewStateRestored: StrikeThrough enabled", )
                                this.setSpan(
                                    StrikethroughSpan(),
                                    start + before,
                                    start + count,
                                    flag
                                )
                            }



                        }
                    }
                }
            }

        }

    }



    override fun onResume() {


        changeTextSize(dataFile?.textSize?:16f)
        if(dataFile!!.font=="default")
        {

        }
        else if(dataFile!!.font==Utils.garamond)
        {
            settingFont(Utils.garamond)
        }



            super.onResume()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val KEY_WRAP = "word_wrap"
        val preference= PreferenceManager.getDefaultSharedPreferences(context)
        val isWrap = preference.getBoolean(KEY_WRAP,false)
        var layout = R.layout.fragment_editor
        val view = inflater.inflate(layout, container, false)
        editText = view.findViewById(R.id.editText)

//        editText.setHorizontallyScrolling(false)
        if(editText!=null) {
            // undoRedo = TextViewUndoRedo(editText,viewLifecycleOwner)
        }

        return view
    }



    fun changeAlignmentValue(left:Boolean = false ,right:Boolean = false ,center:Boolean = false){
        isAlignLeftEnabled = left
        isAlignRightEnabled = right
        isAlignCenterEnabled = center

    }

    fun getCurrentCursorLine(selectionPosition : Int):Int
    {

        Log.e(TAG, "getCurrentCursorLine: $selectionPosition", )
        val layout = editText?.layout

        if(selectionPosition != -1)
        {
            if (layout != null) {
                return layout.getLineForOffset(selectionPosition)
            }
        }
        return -1;
    }

    fun changeSelectedTextStyle(bold : Boolean = false,italic: Boolean = false,underline : Boolean = false,strikethrough : Boolean = false){

        this.hasUnsavedChanges.value = true

        if (editText != null ){
            editText!!.apply {
                if (selectionEnd != selectionStart && text!=null) {

                    if ((bold && !isBoldEnabled) || (italic && !isItalicEnabled) || (strikethrough && !isStrikethroughEnabled) || (underline && !isUnderlineEnabled)) {
                        var next: Int

                        var i = selectionStart
                        while (i < selectionEnd) {

                            // find the next span transition
                            next =
                                text!!.nextSpanTransition(i, selectionEnd, CharacterStyle::class.java)

                            val spans: Array<CharacterStyle> =
                                text!!.getSpans(i, next, CharacterStyle::class.java)

                            for (span in spans) {

                                if (span is StyleSpan) {
                                    val spn = span as StyleSpan
                                    if ((spn.style == Typeface.BOLD && bold) || (spn.style == Typeface.ITALIC && italic))
                                        text!!.removeSpan(spn)
                                } else if ((((span is CustomUnderlineSpan) ||( span is UnderlineSpan)) && underline) || (span is StrikethroughSpan && strikethrough)) {
                                    text!!.removeSpan(span)
                                }
                            }
                            i = next
                        }

                    } else if (bold)
                        text!!.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, flag)
                    else if (italic)
                        text!!.setSpan(StyleSpan(Typeface.ITALIC), selectionStart, selectionEnd, flag)
                    else if (underline)
                        text!!.setSpan(CustomUnderlineSpan(), selectionStart, selectionEnd, flag)
                    else if (strikethrough)
                        text!!.setSpan(StrikethroughSpan(), selectionStart, selectionEnd, flag)


                }
            }
        }
    }

    fun changeParagraphStyle(alignCenter:Boolean=false,alignLeft:Boolean=false,alignRight:Boolean = false,) {

        val flag2 = Spanned.SPAN_INCLUSIVE_EXCLUSIVE

        this.hasUnsavedChanges.value = true
        editText?.apply {
            if (editText != null && text != null) {

                val currentLine = getCurrentCursorLine(selectionStart)
                val endLine = getCurrentCursorLine(selectionEnd)
                val start = this.layout.getLineStart(currentLine)
                var end = this.layout.getLineEnd(endLine)
                Log.e(TAG, "changeParagraphStyle: $start $end")
                if (end == -1) end = editText!!.text!!.length

                if ((alignCenter || alignLeft || alignRight)) {
                    var next: Int

                    var i = start
                    while (i < end) {

                        // find the next span transition
                        next =
                            text!!.nextSpanTransition(i, end, ParagraphStyle::class.java)

                        val spans: Array<ParagraphStyle> =
                            text!!.getSpans(i, next, ParagraphStyle::class.java)

                        for (span in spans) {

                            if (span is AlignmentSpan) {
                                text!!.removeSpan(span)
                            }
                        }
                        i = next
                    }
                }

                if (alignCenter) text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                    start,
                    end,
                    flag2
                )
                else if (alignLeft) text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    start,
                    end,
                    flag2
                )
                else if (alignRight) text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    start,
                    end,
                    flag2
                )

//                postInvalidate()

            }

        }
    }


    fun applyFontEdittext(mFontName:String) {
        this.hasUnsavedChanges.value = true
        editText?.apply {

                if (selectionStart != selectionEnd) {
                    val myTypeface = Utils.getTypefaceFromName(context,mFontName)

                    val customeSapns = text.getSpans(selectionStart,selectionEnd,CustomTypefaceSpan::class.java)
                    for(s in customeSapns) text.removeSpan(s)

                    (text as Spannable).setSpan(
                        CustomTypefaceSpan(myTypeface,mFontName),
                        selectionStart,
                        selectionEnd,
                        flag
                    )
                    val spans = text.getSpans(selectionStart,selectionEnd,StyleSpan::class.java)
                    var isB = false
                    var isI = false
                    for(span in spans){
                        val style = (span as StyleSpan).style
                        if(style == Typeface.BOLD){
                            text.removeSpan(span)
                            isB = true
                        }
                        if(style == Typeface.ITALIC){
                            text.removeSpan(span)
                            isI =true
                        }
                    }
                    Log.e(TAG, "applyFontEdittext: length ${text.length} sstart:$selectionStart send:$selectionEnd ", )
                    if(isB) text.setSpan(StyleSpan(Typeface.BOLD),selectionStart,selectionEnd,flag)
                    if(isI) text.setSpan(StyleSpan(Typeface.ITALIC),selectionStart,selectionEnd,flag)

                }


        }
    }

    fun settingColor(color: Int){
        editText!!.setTextColor(color)
    }
    fun settingFont(s: String)
    {
        this.hasUnsavedChanges .value =true
        editText!!.apply {

                fontFamily = s
                 var mfont = Utils.getTypefaceFromName(context,s)
                 this.typeface = mfont
                dataFile!!.font = s
            }
    }



    fun undoChanges()
    {
//        if(!undoRedo.canUndo){
//            Toast.makeText(this.context, "no more Undo", Toast.LENGTH_SHORT).show()
//        }
//        var before = undoRedo.undo()
//        while(before!=null && before.length>0 && before[0]!=' ' ) {
//            before =  undoRedo.undo()
//        }


    }
    fun redoChanges()
    {
//        if(!undoRedo.canRedo){
//            Toast.makeText(this.context, "no more Redo", Toast.LENGTH_SHORT).show()
//        }
//        var after=undoRedo.redo()
//        while(after!=null && after.length>0 && after.last()!=' ' ) {
//            after =  undoRedo.redo()
//        }
    }


    fun saveDataToDataFile() {
        if(editText!=null && dataFile!=null) {
            dataFile!!.data= editText!!.text
        }

    }

    fun getFileName() : String{
        return dataFile!!.fileName
    }
    fun getFilePath() : String ?{
        if(dataFile!=null) return  dataFile!!.filePath
        return null
    }


    fun getUri(): Uri ?{
        if(dataFile!==null && dataFile!!.uri!==null)
            return dataFile!!.uri
        return null
    }

    fun getFileExtension() : String {
        if(dataFile!=null)
            return dataFile!!.fileExtension
        return ""
    }

    fun replaceAll(findText : String,replaceText : String,ignoreCase : Boolean = false){
        if(editText!=null) {
            val editTextData = editText!!.text.toString()
            val replacedData: String = editTextData.replace(findText, replaceText, ignoreCase)
            editText!!.setText(replacedData)
        }
    }

    fun highlight(find: String, index: Int,ignoreCase: Boolean): Int {

        val str: String = editText!!.text.toString()
        val sIndex: Int = str.indexOf(find, index, ignoreCase)

        if (sIndex != -1) {
            editText!!.requestFocus()
            editText!!.setSelection(sIndex, sIndex + find.length)
//            editText.setSelection(sIndex)
        }
        return sIndex
    }


    fun findReplace(find: String, replace: String, index: Int, ignoreCase: Boolean): Int {

        val string: String = editText!!.text.toString()
        if (index >= 0 && index < string.length) {
            val firstIndex: Int = string.indexOf(find, index, ignoreCase)
            if (firstIndex != -1) {
                val str2 = string.replaceRange(firstIndex, firstIndex + find.length, replace)
                editText!!.setText(str2)

            }
            return firstIndex;
        }
        return -1
    }

    fun gotoLine(line : Int){
        if (line <= 0) {
            editText?.setSelection(0)
        } else {
            val position = ordinalIndexOf(editText?.text.toString(), "\n", line)
            editText?.clearFocus()
            editText?.requestFocus()
            if (position != -1) {
                if(position!=0) editText!!.setSelection(position + 1)
                else editText?.setSelection(0)
            }
        }
    }

    fun ordinalIndexOf(str: String, substr: String?, line: Int): Int {
        var n = line
        if(line==1) return 0
        var pos = str.indexOf(substr!!)
        while (--n > 1 && pos != -1) pos = str.indexOf(substr, pos + 1)
        return pos
    }

    fun getTotalLine(): Int {
        return editText!!.lineCount
    }



    fun insertSpecialChar(specialChar : String){
        if(editText!=null && editText!!.isFocused){
            editText!!.apply {
                text?.replace(selectionStart,selectionEnd,specialChar)
            }
        }
    }
    fun selectionPrevPosition(){
        if(editText!=null && editText!!.isFocused){
            editText!!.apply {
                editText!!.setSelection(selectionEnd-1)
            }
        }

    }

    fun getSelectedData(): CharSequence? {
        if(editText!=null && editText!!.isFocused){
            editText?.apply {

                return  this.text.subSequence(selectionStart,selectionEnd)
            }
        }
        return null
    }

    fun boldClicked()
    {
        isBoldEnabled=!isBoldEnabled
        editText?.apply {
            if(selectionStart!=selectionEnd)
                changeSelectedTextStyle(bold=true)
        }

    }
    fun italicClicked(){

        isItalicEnabled=!isItalicEnabled
        editText?.apply {
            if(selectionStart!=selectionEnd)
                changeSelectedTextStyle(italic = true)

        }
    }

    fun underlineClicked() {

        isUnderlineEnabled = !isUnderlineEnabled
        editText?.apply {
            if(selectionStart!=selectionEnd)
                changeSelectedTextStyle(underline = true)
        }

    }
    fun strikeThroughClicked() {

        isStrikethroughEnabled = !isStrikethroughEnabled
        editText?.apply {
            if(selectionStart!=selectionEnd)
                changeSelectedTextStyle(strikethrough = true)
        }

    }
    fun alignCenter() {

        if(editText!=null) {
            val text = editText!!.text
            var ss = editText!!.selectionStart
            var isEmptyLine = false;

            if (ss == text.length)
                ss--;
            if (ss > 0 && text[ss] == '\n' && text[ss - 1] == '\n') {
                isEmptyLine = true
            }

            if (isEmptyLine == false) {
                changeAlignmentValue(center = true)
                changeParagraphStyle(alignCenter = true)
            } else {
                Toast.makeText(mcontext, "Enter Text To align", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun alignLeft()
    {
        if(editText!=null) {

            val text = editText!!.text
            var ss = editText!!.selectionStart
            var isEmptyLine = false;

            if (ss == text.length)
                ss--;
            if (ss > 0 && text[ss] == '\n' && text[ss - 1] == '\n') {
                isEmptyLine = true
            }

            if (isEmptyLine == false) {
                changeAlignmentValue(left = true)
                changeParagraphStyle(alignLeft = true)
            } else {
                Toast.makeText(mcontext, "Enter Text To align", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun alignRight()
    {
        if(editText!=null) {

            val text = editText!!.text
            var ss = editText!!.selectionStart
            var isEmptyLine = false;

            if (ss == text.length)
                ss--;
            if (ss > 0 && text[ss] == '\n' && text[ss - 1] == '\n') {
                isEmptyLine = true
            }
            if (isEmptyLine == false) {
                changeAlignmentValue(right = true)
                changeParagraphStyle(alignRight = true)
            } else {
                Toast.makeText(mcontext, "Enter Text To align", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setColor(color :Int){
        if(editText!=null) {
            editText!!.text!!.setSpan(
                ForegroundColorSpan(color),
                editText!!.selectionStart,
                editText!!.selectionEnd,
                flag
            )
        }
    }

    fun getEditable(): Spanned? {
        saveDataToDataFile()
        if(editText!=null) {
            return editText!!.text
        }
        else if(dataFile!=null) {
            return dataFile!!.data
        }
        return null
    }

    fun getCurrentSpan() : Array<CharacterStyle>?{
        if(editText!=null ) {
            return editText!!.text.getSpans(editText!!.selectionStart,editText!!.selectionEnd,CharacterStyle::class.java)
        }
        return null
    }

    fun isSelected(): Boolean{
        if(editText!=null)
        {
            if(editText!!.selectionStart==editText!!.selectionEnd)
                return false
            else
                return true
        }
        return false
    }

    fun changeTextSize(x:Float) {
        this.hasUnsavedChanges .value =true

        Log.e(TAG, "changeTextSize: $x", )

            editText?.apply {
                textSize = x
            }
            dataFile?.textSize = x

    }

    fun getTextSize(context: Context): Float {
        return dataFile?.textSize?:16f
    }

    fun makeBullets()
    {
        Log.e(TAG, "makeBullets: ", )
        this.hasUnsavedChanges .value =true

        editText?.apply {


            if(selectionStart==selectionEnd) {
                if (isBulletsOn) {
                    var index = selectionStart - 2
                    while (index > 0 && text[index - 1] != '\n') {
                        index--;
                    }

                    if (index < selectionStart) {

                        val spans = text.getSpans(index, selectionStart - 1, BulletSpan::class.java)
                        for (span in spans) {
                            if (span is BulletSpan) {
                                text.removeSpan(span)
                            }
                        }
                        text.setSpan(BulletSpan(2), index, selectionStart - 1, flag)
                        Log.e(TAG, "makeBullets removed and set span: $index ${selectionStart - 1}")

                    }
                }
            }
            else{
               // text.setSpan(BulletSpan(2), selectionStart, selectionEnd, flag)

            }



        }



    }


    fun makeH1(value : Float) {
        this.hasUnsavedChanges .value =true

        editText?.apply {

                    var ss=selectionStart
                    var se=selectionEnd
                     if(ss==se && (ss>0 && text[ss-1]!='\n'))
                     {
                         //means end at line or first char at line
                         ss=ss-1;
                     }
                    var text:CharSequence=editText!!.text
                    se=text.toString().indexOf('\n',se)
                    if(se==-1 || se>text.length)
                        se=text.length
                    while(ss>0 && text[ss-1]!='\n') {
                        ss--
                    }
                    if (ss != se && ss>=0 && se<=text.length ) {
                        setSelection(ss,se)
                        editText!!.apply {
                        val spans = this.text.getSpans(selectionStart,selectionEnd,RelativeSizeSpan::class.java)
                        for(span in spans){
                            if(span is RelativeSizeSpan){
                                this.text.removeSpan(span)
                            }
                        }
                        this.text.setSpan(RelativeSizeSpan(value), selectionStart, selectionEnd, flag)
                        this.text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, flag)
                }
            }

        }
//
    //        Log.e("utils hello :", Utils.spannableToHtml(Utils.htmlToSpannable("<h1>Hello</h1>")))
        invalidateEditText()

    }
    fun invalidateEditText() {
        if (editText != null) {
            val se = editText!!.selectionStart
            var data = editText!!.text
            editText!!.setText(data)
            editText!!.setSelection(se)
        }
    }

    fun setFileName(findText: String) {
        if(dataFile!=null)
            dataFile!!.fileName=findText

    }

    fun isNote():Boolean{
        if(dataFile!!.isNote==true)
            return  true
        else
            return  false
    }

    fun pasteData(dataToPaste: CharSequence?) {
        if(editText!=null)
        {
            editText!!.apply {
                text.replace(selectionStart,selectionEnd,dataToPaste)
            }
        }
    }

    fun getCharSequence():CharSequence{
        return editText!!.text
    }

    fun getTextSizeForPrint(context: Context): Float {

        if(editText!=null)
        {
            var size = (editText!!.textSize) / (context.resources.displayMetrics.density)
            return size
        }
        return 16f;

    }


}