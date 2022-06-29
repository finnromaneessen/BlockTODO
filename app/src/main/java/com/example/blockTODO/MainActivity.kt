package com.example.blockTODO

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.blockTODO.adapters.BlockAdapter
import com.example.blockTODO.database.DatabaseHandler
import com.example.blockTODO.listview.ListViewActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    //Animations for FABs
    private val rotateMainFabFwd: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_main_fab_fwd
        )
    }
    private val rotateMainFabBwd: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_main_fab_bwd
        )
    }
    private val expandFabsFwd: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.expand_fabs_fwd
        )
    }
    private val expandFabsBwd: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.expand_fabs_bwd
        )
    }

    //ViewModel
    private lateinit var viewModel: MainViewModel

    //Boolean to track FAB-State
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialise the ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        //Define the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_main)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        //Create and connect the adapter
        val blockAdapter = BlockAdapter(this, object : BlockAdapter.OnItemClickListener {
            override fun onItemClick(view: View, blockId: Int) {
                val intent = Intent(this@MainActivity, ListViewActivity::class.java)
                intent.putExtra(
                    "blockName",
                    view.findViewById<TextView>(R.id.recycler_block_name).text
                )
                    .putExtra("blockId", blockId)
                startActivity(intent)
            }
        }, object : BlockAdapter.OnItemLongClickListener {
            override fun onItemLongClick(view: View, blockId: Int) {
                lifecycleScope.launch {
                    DatabaseHandler.deleteBlock(this@MainActivity, blockId)
                }
            }
        })
        recyclerView.adapter = blockAdapter

        //Observe the Block-LiveData
        viewModel.blockLiveData.observe(this) { blocks -> blockAdapter.setBlocks(blocks) }

        //Define OnClick-Listeners for the FABs
        findViewById<FloatingActionButton>(R.id.main_fab).setOnClickListener { onMainFABClick() }
        findViewById<FloatingActionButton>(R.id.edit_blocks_fab).setOnClickListener {
            //TODO: Collapse FABs
            //Start EditBlocksActivity
            val intent = Intent(this@MainActivity, EditBlocksActivity::class.java).apply {}
            startActivity(intent)
        }
        findViewById<FloatingActionButton>(R.id.quick_list_fab).setOnClickListener {
            //TODO: Implement QuickList Feature
        }
    }

    /**
     * onClick-Handler for the main FAB
     */
    private fun onMainFABClick() {
        val mainFAB = findViewById<FloatingActionButton>(R.id.main_fab)
        val quickListFAB = findViewById<FloatingActionButton>(R.id.quick_list_fab)
        val editBlocksFAB = findViewById<FloatingActionButton>(R.id.edit_blocks_fab)

        if (!isExpanded) {
            quickListFAB.visibility = View.VISIBLE
            editBlocksFAB.visibility = View.VISIBLE

            mainFAB.startAnimation(rotateMainFabFwd)
            quickListFAB.startAnimation(expandFabsFwd)
            editBlocksFAB.startAnimation(expandFabsFwd)
        } else {
            mainFAB.startAnimation(rotateMainFabBwd)
            quickListFAB.startAnimation(expandFabsBwd)
            editBlocksFAB.startAnimation(expandFabsBwd)

            quickListFAB.visibility = View.GONE
            editBlocksFAB.visibility = View.GONE
        }
        isExpanded = !isExpanded
    }
}