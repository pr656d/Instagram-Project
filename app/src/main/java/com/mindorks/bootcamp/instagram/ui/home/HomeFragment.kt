package com.mindorks.bootcamp.instagram.ui.home

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.data.model.Post
import com.mindorks.bootcamp.instagram.di.component.FragmentComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseFragment
import com.mindorks.bootcamp.instagram.ui.main.MainSharedViewModel
import com.mindorks.bootcamp.instagram.ui.posts.PostsAdapter
import com.mindorks.bootcamp.instagram.utils.common.PostChangeListener
import com.mindorks.bootcamp.instagram.utils.common.Receiver
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject

class HomeFragment : BaseFragment<HomeViewModel>(), PostChangeListener {

    companion object {

        const val TAG = "HomeFragment"

        fun newInstance(): HomeFragment {
            val args = Bundle()
            val fragment = HomeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var mainSharedViewModel: MainSharedViewModel

    @Inject
    lateinit var linearLayoutManager: LinearLayoutManager

    @Inject
    lateinit var postsAdapter: PostsAdapter

    override fun provideLayoutId(): Int = R.layout.fragment_home

    override fun injectDependencies(fragmentComponent: FragmentComponent) =
        fragmentComponent.inject(this)

    override fun setupObservers() {
        super.setupObservers()

        viewModel.loading.observe(this, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })

        viewModel.posts.observe(this, Observer {
            it.data?.run { postsAdapter.appendData(this) }
        })

        mainSharedViewModel.notifyHome.observe(this, Observer {
            it.getIfNotHandled()?.run {
                viewModel.onPostChange(this)
            }
        })

        viewModel.notifyProfile.observe(this, Observer {
            it.getIfNotHandled()?.run {
                mainSharedViewModel.onPostChange(this, Receiver.PROFILE)
            }
        })

        viewModel.refreshPosts.observe(this, Observer {
            it.data?.run {
                postsAdapter.updateList(this)
            }
        })
    }

    override fun setupView(view: View) {
        rvPosts.apply {
            layoutManager = linearLayoutManager
            adapter = postsAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    layoutManager?.run {
                        if (this is LinearLayoutManager
                            && itemCount > 0
                            && itemCount == findLastVisibleItemPosition() + 1
                        ) viewModel.onLoadMore()
                    }
                }
            })
        }
    }

    override fun onDelete(post: Post) {
        viewModel.onDelete(post, true)
    }

    override fun onLike(post: Post) {
        viewModel.onLike(post, true)
    }
}