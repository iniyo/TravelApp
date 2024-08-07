package pjo.travelapp.presentation.util.signmanager

import android.content.Context

interface NaverSignManager {
    fun naverLogin(context: Context)
    fun startNaverDeleteToken()
    fun startNaverLogout()
    fun isLoggedIn(): Boolean
}