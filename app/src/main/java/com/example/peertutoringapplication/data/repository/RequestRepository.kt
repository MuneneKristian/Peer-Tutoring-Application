package com.example.peertutoringapplication.data.repository

import com.example.peertutoringapplication.data.datasource.TutoringRequestDao
import com.example.peertutoringapplication.data.model.TutoringRequest

class RequestRepository(
    private val requestDao: TutoringRequestDao
) {

    suspend fun submitRequest(request: TutoringRequest) =
        requestDao.insertRequest(request)

    suspend fun getRequestsByTutee(tuteeId: Int) =
        requestDao.getRequestsByTutee(tuteeId)

    suspend fun getRequestsByTutor(tutorId: Int) =
        requestDao.getRequestsByTutor(tutorId)

    suspend fun getRequestById(requestId: Int) =
        requestDao.getRequestById(requestId)

    suspend fun updateRequestStatus(
        requestId: Int,
        status: String
    ) = requestDao.updateRequestStatus(requestId, status)

    suspend fun deleteRequest(request: TutoringRequest) =
        requestDao.deleteRequest(request)
}
