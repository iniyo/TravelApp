package pjo.travelapp.presentation.util.signmanager

import android.content.Context

interface NaverSignManager {
    fun NaverLogin(context: Context)
    fun startNaverDeleteToken()
    fun startNaverLogout()
}