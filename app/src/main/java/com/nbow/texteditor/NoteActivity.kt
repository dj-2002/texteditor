package com.nbow.texteditor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbow.texteditor.RecyclerView.ExampleAdapter
import com.nbow.texteditor.RecyclerView.ExampleItem
import java.io.File
import java.text.DecimalFormat

class NoteActivity : AppCompatActivity() {
    private var mExampleList: ArrayList<ExampleItem> = arrayListOf()
    lateinit var mRecyclerView: RecyclerView
    lateinit var mAdapter: ExampleAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var model: MyViewModel
    private  val TAG = "NoteActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Recent File")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        buildRecyclerView()

        model = ViewModelProvider(
            this,
            MyViewModelFactory(this.application)
        ).get(MyViewModel::class.java)


        var listOfNotes = File(applicationContext.filesDir, "note").listFiles()

        if (listOfNotes != null) {
            for (f in listOfNotes) {
                val fileSize: String = getFileSize(f.length())
                mExampleList.add(ExampleItem(f.name, f.lastModified().toString(), fileSize))
            }


            mAdapter.notifyDataSetChanged()
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

        try {
            val dir= File(applicationContext.filesDir,"note")

            if(dir.exists())
            {

                dir.listFiles().forEach {
                    it.delete()
                }
            }
        }
        catch (e:Exception)
        {
            Log.e(TAG, "clearAllFiles: ", )
        }
        mExampleList.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun getFileSize(fileSize: Long): String {
        var size="";
        val k:Double=fileSize/(1024.0)
        val m:Double=k/(1024.0)

        var decimalFormat = DecimalFormat("0.00");

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

    fun removeItem(position: Int) {

        val dir = File(applicationContext.filesDir,"note")
        val file = File(dir,mExampleList[position].fileName)
        if(file.exists())
            file.delete()
        mExampleList.removeAt(position)
        mAdapter.notifyItemRemoved(position)
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

                intent.putExtra("file_name",mExampleList.get(position).fileName)
                setResult(RESULT_OK,intent)
                finish()
            }

            override fun onDeleteClick(position: Int) {
                removeItem(position)
            }
        })
    }


}