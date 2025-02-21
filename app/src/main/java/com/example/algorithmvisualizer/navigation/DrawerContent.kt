package com.example.algorithmvisualizer.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.model.Screen

@Composable
fun DrawerContent(onDestinationClicked: (route: String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Drawer header
        Text(
            text = stringResource(id = R.string.app_name),
            modifier = Modifier.padding(16.dp)
        )
        Divider()
        // Drawer items for each algorithm.
        DrawerItem(title = stringResource(id = R.string.bubble_sort), route = Screen.BubbleSort.route, onItemClicked = onDestinationClicked)
        DrawerItem(title = stringResource(id = R.string.selection_sort), route = Screen.SelectionSort.route, onItemClicked = onDestinationClicked)
        DrawerItem(title = stringResource(id = R.string.BFS), route = Screen.BFS.route, onItemClicked = onDestinationClicked)
        DrawerItem(title = stringResource(id = R.string.dijkstra), route = Screen.Dijkstra.route, onItemClicked = onDestinationClicked)
        DrawerItem(title = stringResource(id = R.string.quick_sort), route = Screen.QuickSort.route, onItemClicked = onDestinationClicked)
    }
}

@Composable
fun DrawerItem(title: String, route: String, onItemClicked: (String) -> Unit) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClicked(route) }
            .padding(16.dp)
    )
}
