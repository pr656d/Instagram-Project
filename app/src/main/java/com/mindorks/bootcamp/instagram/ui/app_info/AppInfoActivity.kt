package com.mindorks.bootcamp.instagram.ui.app_info

import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.di.component.ActivityComponent
import com.mindorks.bootcamp.instagram.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_app_info.*

class AppInfoActivity : BaseActivity<AppInfoViewModel>() {

    override fun provideLayoutId(): Int = R.layout.activity_app_info

    override fun injectDependencies(activityComponent: ActivityComponent) =
        activityComponent.inject(this)

    override fun setupView(savedInstanceState: Bundle?) {
        tvMindOrksLink.movementMethod = LinkMovementMethod.getInstance()

        tvMeLink.movementMethod = LinkMovementMethod.getInstance()

        tvProjectGithubLink.movementMethod = LinkMovementMethod.getInstance()
    }
}