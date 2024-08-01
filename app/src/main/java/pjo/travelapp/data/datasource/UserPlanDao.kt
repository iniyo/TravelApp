package pjo.travelapp.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pjo.travelapp.data.entity.PlaceResult
import pjo.travelapp.data.entity.UserSchduleEntity

@Dao
interface ParentDao {
    @Query("SELECT * FROM user_schedule")
    fun getAllParents(): List<UserSchduleEntity>

    @Insert
    fun insertParents(parents: List<UserSchduleEntity>)
}

@Dao
interface ChildDao {
    @Query("SELECT * FROM place_result")
    fun getAllChildren(): List<PlaceResult>

    @Insert
    fun insertChildren(children: List<PlaceResult>)
}