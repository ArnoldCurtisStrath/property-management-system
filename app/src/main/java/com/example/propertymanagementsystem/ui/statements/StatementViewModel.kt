package com.example.propertymanagementsystem.ui.statements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.Statement
import kotlinx.coroutines.launch

class StatementViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container
    private val _statement = MutableLiveData<Statement?>()
    val statement: LiveData<Statement?> = _statement

    private var loadedKey: String = ""

    fun load(buildingId: Long, month: String) {
        val key = "$buildingId-$month"
        if (key == loadedKey) return
        loadedKey = key
        viewModelScope.launch {
            _statement.value = container.ledger.statementOnce(buildingId, month)
        }
    }
}
