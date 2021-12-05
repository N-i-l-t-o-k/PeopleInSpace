package com.surrus.peopleinspace.ui

import androidx.constraintlayout.widget.ConstraintAttribute
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.model.QueryRequest
import co.touchlab.kermit.Kermit
import com.surrus.common.remote.Assignment
import com.surrus.common.repository.PeopleInSpaceRepository
import com.surrus.common.repository.PeopleInSpaceRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import aws.sdk.kotlin.services.dynamodb.model.ScanRequest




class PeopleInSpaceViewModel(
    private val peopleInSpaceRepository: PeopleInSpaceRepositoryInterface
) : ViewModel() {

    val staticCredentials = StaticCredentialsProvider {
        accessKeyId = ""
        secretAccessKey = ""
    }

    val dynamoDbClient = DynamoDbClient{
        region = "eu-west-1"
        credentialsProvider = staticCredentials
    }
    val table = "PeopleInSpace"

    val peopleInSpace = MutableStateFlow<List<Assignment>>(emptyList())

    init {
        viewModelScope.launch {
            val scanRequest = ScanRequest {
                tableName = table
            }

            val people = mutableListOf<Assignment>()
            val result = dynamoDbClient.scan(scanRequest)
            result.items?.forEach { item ->
                val id = (item["id"] as AttributeValue.S).value
                val craft = (item["craft"] as AttributeValue.S).value
                val personImageUrl = (item["personImageUrl"] as AttributeValue.S).value
                val person = Assignment(id, craft, personImageUrl)
                people.add(person)
            }
            peopleInSpace.value = people
        }
    }

//    val peopleInSpace = peopleInSpaceRepository.fetchPeopleAsFlow()
//        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val issPosition = peopleInSpaceRepository.pollISSPosition()

    fun getPerson(personName: String): Assignment? {
        return peopleInSpace.value.find { it.name == personName }
    }
}
