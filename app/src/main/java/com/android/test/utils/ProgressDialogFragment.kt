package com.android.test.utils

import android.app.ActionBar
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.android.test.R
import kotlinx.android.synthetic.main.fragment_progress_dialog.*
import kotlinx.android.synthetic.main.fragment_progress_dialog.progressBar


class ProgressDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun getTheme(): Int {
        return R.style.ProgressDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireActivity(), theme) {
            override fun onAttachedToWindow() {
                super.onAttachedToWindow()
                this.window?.apply {
                    setBackgroundDrawableResource(arguments!!.getInt(ARG_BACKGROUND_RES_ID))
                    setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_progress_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val messageResId = requireArguments().getInt(ARG_MESSAGE_RES_ID)
        if (messageResId != 0) {
            progressMessage.isVisible = true
            progressMessage.setText(messageResId)
        } else {
            progressMessage.isVisible = false
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (e: Exception) {
        }
    }

    companion object {
        const val PROGRESS_TAG = "progress_dialog"

        private const val ARG_BACKGROUND_RES_ID = "arg_background_res_id"
        private const val ARG_MESSAGE_RES_ID = "arg_message_res_id"

        fun newInstance(backgroundResId: Int = android.R.color.transparent, @OptIn messageResId: Int = 0) =
                ProgressDialogFragment().apply {
                    arguments = bundleOf(
                            ARG_BACKGROUND_RES_ID to backgroundResId,
                            ARG_MESSAGE_RES_ID to messageResId
                    )
                }
    }
}