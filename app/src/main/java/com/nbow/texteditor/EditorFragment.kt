package com.nbow.texteditor

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.style.*
import android.util.Log
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
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


class EditorFragment : Fragment {

    private val TAG = "EditorFragment"
    lateinit var mcontext: Context
    public var selectedFont: String = "default"
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
        if(dataFile!=null){
           editText?.setText(dataFile!!.data)            //undoRedo.mIsUndoOrRedo = false
        }

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
//        val PREFERENCE_NAME="myPreference"
        val KEY_TEXT_SIZE = "TEXT_SIZE_PREFERENCE"
        val preference= PreferenceManager.getDefaultSharedPreferences(context)
        var myTextSize:Int = preference.getInt(KEY_TEXT_SIZE,16)
        editText?.setTextSize(myTextSize.toFloat())

        val  f=(preference.getString("font_family","DEFAULT"))
        if(f!="DEFAULT"){
            if(f=="DEFAULT_BOLD")
                editText?.typeface= Typeface.DEFAULT_BOLD
            else if(f=="MONOSPACE")
                editText?.typeface= Typeface.MONOSPACE
            else if(f=="SANS_SARIF")
                editText?.typeface= Typeface.SANS_SERIF
            else if(f=="SERIF")
                editText?.typeface= Typeface.SERIF
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
                    flag
                )
                else if (alignLeft) text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                    start,
                    end,
                    flag
                )
                else if (alignRight) text!!.setSpan(
                    AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                    start,
                    end,
                    flag
                )

//                postInvalidate()

            }

        }
    }


    fun applyFontEdittext(fontRes: Int?,fontName:String) {
        this.hasUnsavedChanges.value = true
        editText?.apply {

            if(fontRes!=null) {
                if (selectionStart != selectionEnd) {
                    val myTypeface = Typeface.create(
                        ResourcesCompat.getFont(context, fontRes),
                        Typeface.NORMAL
                    )//TODO :
                    val customeSapns = text.getSpans(selectionStart,selectionEnd,CustomTypefaceSpan::class.java)
                    for(s in customeSapns) text.removeSpan(s)

                    (text as Spannable).setSpan(
                        CustomTypefaceSpan(myTypeface,fontName),
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

                } else {
                    //selectedFont = fontResf
                }
            }
            else{

                if(selectionStart != selectionEnd) {
                    val myTypeface = Typeface.NORMAL
                    (text as Spannable).setSpan(
                        myTypeface,
                        selectionStart,
                        selectionEnd,
                        flag
                    )
                }
            }
        }
    }

    fun settingColor(color: Int){
        editText!!.setTextColor(color)
    }
    fun settingFont(font: Int?, s: String)
    {
        this.hasUnsavedChanges
        editText!!.apply {
            if(font!=null) {
                this.typeface =
                    Typeface.create(ResourcesCompat.getFont(context, font), Typeface.NORMAL)
                fontFamily = s
            }
            else
                this.typeface=Typeface.DEFAULT
        }
        if(font!=null)
            selectedFont = s
        Toast.makeText(context, "no Text selected", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(mcontext, "No Text to Align", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(mcontext, "No Text to Align", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(mcontext, "No Text to Align", Toast.LENGTH_SHORT).show()
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
        Log.e(TAG, "changeTextSize: $x", )
        if(editText!=null)
        {
            editText!!.apply {
                textSize=x;
            }
        }
    }

    fun getTextSize(context: Context): Float {
        if(editText!=null)
        {
            var size = (editText!!.textSize) / (context.resources.displayMetrics.density)
            return size
        }
        return 16f;
    }

    fun makeH1(value : Float) {
        editText?.apply {

            val spans = text.getSpans(selectionStart,selectionEnd,RelativeSizeSpan::class.java)
            for(span in spans){
                if(span is RelativeSizeSpan){
                    text.removeSpan(span)
                }
            }

            var ss=selectionStart
            var se=selectionEnd

            var text:CharSequence=editText!!.text

            if(se < (text.length-2)) {

                while (  ss > 0 && text[ss] != '\n'    ) {
                    ss--
                }
                while ( se < text.length && text[se] != '\n' ) {
                    se++;
                }
                if (ss != se && ss>=0 ) {
                    if(ss==0)
                        setSelection(ss,se)
                    else
                        setSelection(ss+1, se)
                }
            }

            editText!!.apply {
                this.text.setSpan(RelativeSizeSpan(value), selectionStart,selectionEnd, flag)
                this.text.setSpan(StyleSpan(Typeface.BOLD), selectionStart, selectionEnd, flag)
//                      this.doOnPreDraw {
//
//                    }

            }
        }
//        Log.e("utils hello :", Utils.spannableToHtml(Utils.htmlToSpannable("<h1>Hello</h1>")))

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

    fun getFontSize(): Float {
        return (editText?.textSize)?:16f
    }

}