package com.nbow.texteditor


import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nbow.texteditor.RecyclerView.ExampleItem
import com.nbow.texteditor.RecyclerView.ExampleAdapter
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewActivity : AppCompatActivity() {
    private var mExampleList: ArrayList<ExampleItem> = arrayListOf()
    lateinit var mRecyclerView: RecyclerView
    private  val TAG = "RecyclerViewActivity"
    lateinit var mAdapter: ExampleAdapter
     var listOfUri : MutableList<File> = arrayListOf()
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    var currentFile = File("/sdcard/")
    lateinit var toolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Recent File")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        buildRecyclerView()
        listFiles(File("/sdcard/"))

        val floatingActionButton:FloatingActionButton  = findViewById(R.id.fab_recyclerview)
        floatingActionButton.setOnClickListener {



            val bundle = Bundle()
            val temp:ArrayList<String> = arrayListOf()
            for(t in listOfUri) {
                var myuri = ""
                if (Build.VERSION.SDK_INT >= 24)
                    myuri = FileProvider.getUriForFile(RecyclerViewActivity@this, BuildConfig.APPLICATION_ID + ".provider", t).toString();
                else
                    myuri = Uri.fromFile(t).toString();
                Log.e(TAG, "onCreate: ${myuri}", )
                temp.add(myuri)
            }
            bundle.putStringArrayList("uri",temp)
            intent.putExtra(
                "mbundle",
                bundle
            )
            setResult(RESULT_OK, intent)
            finish()
        }

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menuInflater.inflate(R.menu.recent_file_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
          R.id.clear_history -> clearAllFiles()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearAllFiles() {
        //model.deleteAllRecentFile()
        mExampleList.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun getFileSize(fileSize: Long): String {
        var size="";
        val k:Double=fileSize/(1024.0)
        val m:Double=k/(1024.0)

        var decimalFormat:DecimalFormat=DecimalFormat("0.00");

        if(m>1)
        {
            size=decimalFormat.format(m).plus(" MB")
        }
        else
        {
            size=decimalFormat.format(k).plus(" KB")

        }

        return  size
    }




    fun buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(this)
        mAdapter = ExampleAdapter(mExampleList)
        mRecyclerView.setLayoutManager(mLayoutManager)
        mRecyclerView.setAdapter(mAdapter)
        mAdapter.setOnItemClickListener(object : ExampleAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                //var cUri:Uri= Uri.parse(mExampleList.get(position).uri)
                var cfile =mExampleList.get(position).file
                if(cfile.isDirectory) {

                    listFiles(cfile)

                }
                else {

//
                    mExampleList.get(position).checkbox=!(mExampleList.get(position).checkbox)
                    mAdapter.notifyItemChanged(position)
                    if(listOfUri.contains(mExampleList.get(position).file))
                        listOfUri.remove(mExampleList.get(position).file)
                    else
                        listOfUri.add(mExampleList.get(position).file)
                }
            }


        })
    }

    override fun onBackPressed() {
        if(currentFile!=File("/sdcard/"))
        listFiles(currentFile.parentFile)
        else
        super.onBackPressed()
    }

    private fun listFiles(directory: File) {
        toolbar.title=directory.name
        currentFile=directory
        mExampleList.clear()
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (file != null) {
                    mExampleList.add(ExampleItem(file.name,
                        SimpleDateFormat("dd MMM HH:mm").format(Date(file.lastModified())).toString(),getFileSize(file.length()),file))
                }
            }
        }
        mAdapter.notifyDataSetChanged()
    }


}