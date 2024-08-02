package pjo.travelapp.data.repo


import pjo.travelapp.data.entity.UserPlan

interface UserRepository {
    suspend fun insertUserPlan(userPlan: UserPlan): Long
    suspend fun updateUserPlan(userPlan: UserPlan)
    suspend fun getUserPlanById(planId: String): UserPlan?
    suspend fun deleteUserPlan(userPlan: UserPlan)
    suspend fun getAllUserPlans(): List<UserPlan>
}