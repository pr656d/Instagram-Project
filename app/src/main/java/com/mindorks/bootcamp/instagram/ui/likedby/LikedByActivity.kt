package com.mindorks.bootcamp.instagram.ui.likedby

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.ActivityComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseActivity
import com.mindorks.bootcamp.instagram.ui.likedby.recyclerview.LikedByAdapter
import com.mindorks.bootcamp.instagram.utils.common.Constants
import com.mindorks.bootcamp.instagram.utils.common.LikedByListListener
import kotlinx.android.synthetic.main.activity_liked_by.*
import javax.inject.Inject

class LikedByActivity : BaseActivity<LikedByViewModel>(), LikedByListListener {

    companion object {
        const val TAG = "LikedByActivity"
    }

    @Inject
    lateinit var linearLayoutManager: LinearLayoutManager

    @Inject
    lateinit var likedByAdapter: LikedByAdapter

    override fun provideLayoutId(): Int = R.layout.activity_liked_by

    override fun injectDependencies(activityComponent: ActivityComponent) =
        activityComponent.inject(this)

    override fun setupObservers() {
        super.setupObservers()

        viewModel.userList.observe(this, Observer {
            it.getIfNotHandled()?.run {
                likedByAdapter.updateList(this)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.createList(intent?.extras?.get(Constants.POST_EXTRA) as LikedByParcelize)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        rvLikedBy.apply {
            layoutManager = linearLayoutManager
            adapter = likedByAdapter
        }
    }

    override fun onLikesClick(userId: String) {
        showMessage(userId)
    }
}