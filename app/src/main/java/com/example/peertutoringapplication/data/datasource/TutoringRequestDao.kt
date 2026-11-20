package com.example.peertutoringapplication.data.datasource

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.peertutoringapplication.data.model.TutoringRequest

@Dao
interface TutoringRequestDao {

    @Insert
    suspend fun insertRequest(request: TutoringRequest)

    @Query("SELECT * FROM tutoring_requests WHERE tuteeId = :tuteeId")
    suspend fun getRequestsByTutee(tuteeId: Int): List<TutoringRequest>

    @Query("SELECT * FROM tutoring_requests WHERE tutorId = :tutorId")
    suspend fun getRequestsByTutor(tutorId: Int): List<TutoringRequest>

    @Query("SELECT * FROM tutoring_requests WHERE requestId = :requestId")
    suspend fun getRequestById(requestId: Int): TutoringRequest?

    @Query("UPDATE tutoring_requests SET status = :status WHERE requestId = :requestId")
    suspend fun updateRequestStatus(requestId: Int, status: String)

    @Delete
    suspend fun deleteRequest(request: TutoringRequest)
}
