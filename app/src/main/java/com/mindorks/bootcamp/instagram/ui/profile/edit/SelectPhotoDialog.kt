package com.mindorks.bootcamp.instagram.ui.profile.edit

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.mindorks.bootcamp.instagram.R
import com.mindorks.bootcamp.instagram.utils.common.SelectPhotoDialogListener
import kotlinx.android.synthetic.main.select_photo_dialog.view.*

class SelectPhotoDialog : AppCompatDialogFragment() {

    companion object {
        const val TAG = "SelectPhotoDialog"
    }

    private lateinit var listener: SelectPhotoDialogListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            listener = context as SelectPhotoDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("${context.toString()} must implement SelectPhotoDialogListener")
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.select_photo_dialog, null)

        view.view_gallery.setOnClickListener { listener.onGalleryClick() }

        view.view_camera.setOnClickListener { listener.onCameraClick() }

        return AlertDialog.Builder(activity).setView(view).create()
    }
}