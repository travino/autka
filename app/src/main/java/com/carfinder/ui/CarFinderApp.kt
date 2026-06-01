package com.carfinder.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carfinder.feature.detail.OfferDetailRoute
import com.carfinder.feature.listings.ListingsRoute

private object Routes {
    const val LISTINGS = "listings"
    const val DETAIL = "detail/{offerId}"
    fun detail(offerId: String) = "detail/$offerId"
}

@Composable
fun CarFinderApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LISTINGS) {
        composable(Routes.LISTINGS) {
            ListingsRoute(
                onOfferClick = { id -> navController.navigate(Routes.detail(id)) },
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("offerId") { type = NavType.StringType }),
        ) {
            OfferDetailRoute(onBack = { navController.popBackStack() })
        }
    }
}
