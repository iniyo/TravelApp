package pjo.travelapp.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import pjo.travelapp.data.entity.UserPlan

@Dao
interface UserPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPlan(userPlan: UserPlan): Long

    @Update
    suspend fun updateUserPlan(userPlan: UserPlan)

    @Query("SELECT * FROM user_plan WHERE id = :planId")
    suspend fun getUserPlanById(planId: String): UserPlan?

    @Delete
    suspend fun deleteUserPlan(userPlan: UserPlan)

    @Query("SELECT * FROM user_plan ORDER BY forkDate DESC") // DESC or ASC
    suspend fun getAllUserPlans(): List<UserPlan>
}