package com.click.clickrecord.dialogfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.click.clickrecord.databinding.DialogFragmentWarningBinding

class WarningDialogFragment: BaseDialogFragment() {
    private lateinit var binding: DialogFragmentWarningBinding
    private var onConfirm: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentWarningBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvConfirm.setOnClickListener {
            onConfirm?.invoke()
        }
    }

    fun setOnConfirmListener(listener: () -> Unit) {
        this.onConfirm = listener
    }
}