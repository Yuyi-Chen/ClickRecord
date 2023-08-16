package com.click.clickrecord.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object ViewModelMain : ViewModel() {

    val isShowFloatView = MutableLiveData<Boolean>()

    val exceptionExit = MutableLiveData<Boolean>()
}