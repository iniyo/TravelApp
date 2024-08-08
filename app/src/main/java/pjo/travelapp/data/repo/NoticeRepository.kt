package pjo.travelapp.data.repo

import pjo.travelapp.data.entity.FireStoreNotice
import pjo.travelapp.data.entity.UserPlan

interface NoticeRepository {
    suspend fun getNotices(): List<FireStoreNotice>
    suspend fun saveNotices(notices: List<FireStoreNotice>)
    suspend fun updateNotice(notice: FireStoreNotice)
    suspend fun fetchAndSaveNotices(): List<FireStoreNotice>
    suspend fun deleteNotice(notice: FireStoreNotice)
}