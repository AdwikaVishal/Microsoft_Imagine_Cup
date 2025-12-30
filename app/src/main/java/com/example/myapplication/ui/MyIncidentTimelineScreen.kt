package com.example.myapplication.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.viewmodel.IncidentViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Incident

@Composable
fun MyIncidentTimelineScreen(viewModel: IncidentViewModel) {
    val incidents by viewModel.incidents.collectAsState()

    LazyColumn {
        items(incidents) { incident ->
            IncidentCard(incident = incident)
        }
    }
}

@Composable
fun IncidentCard(incident: Incident) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Category: ${incident.category}")
            Text(text = "Description: ${incident.description}")
            Text(text = "Status: ${incident.status}")
        }
    }
}
