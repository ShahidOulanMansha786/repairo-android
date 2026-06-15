package com.carrepair.app


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairHomeScreen() {

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text("CarRepair App")
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { }
            ) {
                Text("+")
            }
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            Text("Welcome, Ali")

            Text("You have 3 open leads")

            Text("Tap + to post a new lead")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepairHomeScreenPreview() {
    RepairHomeScreen()
}