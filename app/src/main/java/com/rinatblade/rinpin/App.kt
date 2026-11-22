package com.rinatblade.rinpin

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun RinApp(vm: MainViewModel) {
    val theme = Themes.byId(vm.themeId)
    RinPinTheme(theme, vm.currentFont().family) {
        val view = LocalView.current
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            window.setBackgroundDrawable(ColorDrawable(theme.bg.toArgb()))
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !theme.dark
            controller.isAppearanceLightNavigationBars = !theme.dark
        }

        var tab by rememberSaveable { mutableIntStateOf(0) }
        BackHandler(enabled = tab != 0) { tab = 0 }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    val colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                    NavigationBarItem(
                        selected = tab == 0, onClick = { tab = 0 }, colors = colors,
                        icon = { Icon(Icons.Default.Search, contentDescription = null) },
                        label = { Text("Поиск") },
                    )
                    NavigationBarItem(
                        selected = tab == 1, onClick = { tab = 1 }, colors = colors,
                        icon = {
                            BadgedBox(badge = { if (vm.cart.isNotEmpty()) Badge { Text(vm.cart.size.toString()) } }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            }
                        },
                        label = { Text("Корзина") },
                    )
                    NavigationBarItem(
                        selected = tab == 2, onClick = { tab = 2 }, colors = colors,
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Настройки") },
                    )
                }
            },
        ) { pad ->
            Box(Modifier.fillMaxSize().padding(pad).imePadding()) {
                when (tab) {
                    0 -> SearchScreen(vm, onOpenSettings = { tab = 2 })
                    1 -> CartScreen(vm)
                    else -> SettingsScreen(vm)
                }
            }
        }
    }
}
