package pjo.travelapp.data.repo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pjo.travelapp.data.datasource.NoticeDao
import pjo.travelapp.data.entity.FireStoreNotice
import javax.inject.Inject

class NoticeRepositoryImpl @Inject constructor(
    private val noticeDao: NoticeDao,
    private val firestore: FirebaseFirestore
) : NoticeRepository {

    override suspend fun getNotices(): List<FireStoreNotice> {
        return noticeDao.getAllNotices()
    }

    override suspend fun saveNotices(notices: List<FireStoreNotice>) {
        noticeDao.insertNotices(notices)
    }

    override suspend fun updateNotice(notice: FireStoreNotice) {
        noticeDao.updateNotice(notice)
    }

    override suspend fun deleteNotice(notice: FireStoreNotice) {
        noticeDao.deleteNotice(notice)
    }

    override suspend fun fetchAndSaveNotices(): List<FireStoreNotice> {
        val result = firestore.collection("notices").get().await()
        val notices = result.map { document ->
            document.toObject(FireStoreNotice::class.java).apply {
                id = document.id  // 문서 ID 설정
            }
        }

        val currentNotices = getNotices()
        Log.d("TAG", "Current Notices in Room: $currentNotices")

        // Firestore에 없는 문서는 Room에서 삭제
        val noticesToDelete = currentNotices.filter { notice ->
            notices.none { it.id == notice.id }
        }
        if (noticesToDelete.isNotEmpty()) {
            noticeDao.deleteNotices(noticesToDelete)
        }

        // Firestore에서 새로운 문서 추가
        val newNotices = notices.filterNot { notice -> currentNotices.any { it.id == notice.id } }
        newNotices.forEach { it.isNew = true }

        // 기존 문서 업데이트 및 새로운 문서 추가
        val updatedNotices = currentNotices.mapNotNull { currentNotice ->
            notices.find { it.id == currentNotice.id }?.let { newNotice ->
                currentNotice.copy(
                    title = newNotice.title,
                    content = newNotice.content,
                    date = newNotice.date,
                    isNew = currentNotice.isNew
                )
            }
        } + newNotices

        // 전체 데이터를 저장하는 대신 삭제되지 않은 데이터를 다시 저장
        noticeDao.insertNotices(updatedNotices)

        return newNotices
    }
}



