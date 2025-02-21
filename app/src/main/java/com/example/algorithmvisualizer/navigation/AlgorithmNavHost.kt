package com.example.algorithmvisualizer.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.algorithmvisualizer.R
import com.example.algorithmvisualizer.model.Screen
import kotlinx.coroutines.launch
import com.example.algorithmvisualizer.ui.theme.Primary
import com.example.algorithmvisualizer.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlgorithmNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Observe the current back stack entry to update the top bar title.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: Screen.BubbleSort.route

    // Map the current route to a title string.
    val currentTitle = when (currentRoute) {
        Screen.BubbleSort.route -> stringResource(id = R.string.bubble_sort)
        Screen.SelectionSort.route -> stringResource(id = R.string.selection_sort)
        Screen.BFS.route -> stringResource(id = R.string.BFS)
        Screen.MergeSort.route -> stringResource(id = R.string.merge_sort)
        Screen.QuickSort.route -> stringResource(id = R.string.quick_sort)
        else -> stringResource(id = R.string.app_name)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Wrap the drawer content in a ModalDrawerSheet with a white background.
            ModalDrawerSheet(
                drawerContainerColor = White
            ) {
                DrawerContent { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = currentTitle, color = White, style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu),
                                contentDescription = "",
                                tint = White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Primary
                    )
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.BubbleSort.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.BubbleSort.route) { BubbleSortScreen() }
                composable(Screen.SelectionSort.route) { SelectionSortScreen() }
                composable(Screen.BFS.route) { BFSScreen() }
                composable(Screen.MergeSort.route) { MergeSortScreen() }
                composable(Screen.QuickSort.route) { QuickSortScreen() }
            }
        }
    }
}
