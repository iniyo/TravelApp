package pjo.travelapp.data.repo

import androidx.room.Insert
import pjo.travelapp.data.datasource.UserPlanDao
import pjo.travelapp.data.entity.UserPlan
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userPlanDao: UserPlanDao
) : UserRepository {

    override suspend fun insertUserPlan(userPlan: UserPlan): Long {
        return userPlanDao.insertUserPlan(userPlan)
    }

    override suspend fun updateUserPlan(userPlan: UserPlan) {
        userPlanDao.updateUserPlan(userPlan)
    }

    override suspend fun getUserPlanById(planId: String): UserPlan? {
        return userPlanDao.getUserPlanById(planId)
    }

    override suspend fun deleteUserPlan(userPlan: UserPlan) {
        userPlanDao.deleteUserPlan(userPlan)
    }

    override suspend fun getAllUserPlans(): List<UserPlan> {
        return userPlanDao.getAllUserPlans()
    }
}