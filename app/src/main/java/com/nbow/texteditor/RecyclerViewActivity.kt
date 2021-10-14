
package com.nbow.texteditor
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nbow.texteditor.RecyclerView.ExampleAdapter

import com.nbow.texteditor.RecyclerView.ExampleItem
import java.text.DecimalFormat

class RecyclerViewActivity : AppCompatActivity() {
    private var mExampleList: ArrayList<ExampleItem> = arrayListOf()
    lateinit var mRecyclerView: RecyclerView
    lateinit var mAdapter: ExampleAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager
    lateinit var model: MyViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view)
        val toolbar:Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitle("Recent File")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        buildRecyclerView()

        model = ViewModelProvider(this,MyViewModelFactory(this.application)).get(MyViewModel::class.java)

        model.getRecentFileList().observe(this){
            Log.e("RecyclerViewActivity","size of list ${it.size}")
            for(r in it){
                Log.e("e","${r.fileName}")
                val fileSize:String=getFileSize(r.fileSize)
                mExampleList.add(ExampleItem(r.fileName,r.date,fileSize))
            }

            if(mExampleList.size>200)
            {
                var tempSize=mExampleList.size
                while(tempSize>200)
                {
                    removeItem(tempSize)
                    tempSize--
                }
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
        model.deleteAllRecentFile()
        mExampleList.clear()
        mAdapter.notifyDataSetChanged()
    }

    private fun getFileSize(fileSize: Int): String {
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

    fun removeItem(position: Int) {
        mExampleList.removeAt(position)
        val recentFile = model.getRecentFileList().value!!.get(position)
        model.deleteRecentFile(recentFile)
        if(position>=0 && position<model.getFragmentList().value!!.size)
            model.getRecentFileList().value!!.remove(recentFile)
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
                intent.putExtra("uri",model.getRecentFileList().value!!.get(position).uriString)
                setResult(RESULT_OK,intent)
                finish()
            }

            override fun onDeleteClick(position: Int) {
                removeItem(position)
            }
        })
    }


}