package z.com.jetpoke

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import z.com.jetpoke.pokemondetail.PokemonDetailScreen
import z.com.jetpoke.pokemonlist.PokemonListScreen
import z.com.jetpoke.ui.theme.JetPokeTheme
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {

                val navController = rememberNavController()
                NavHostInit(navController = navController)
            }
        }
    }


    @Composable
    fun NavHostInit(navController: NavHostController) {
        NavHost(
            navController = navController,
            startDestination = POKEMON_LIST_SCREEN_DESTINATION
        ) {
            composable(route = POKEMON_LIST_SCREEN_DESTINATION)
            { listScreenBackStack ->
                PokemonListScreen(navController = navController)
            }

            composable(route = "$POKEMON_DETAIL_SCREEN_DESTINATION/{$DOMINANT_COLOR_NAV_ARG}/{$POKEMON_NAME_NAV_ARG}",
                arguments = listOf(
                    navArgument(name = DOMINANT_COLOR_NAV_ARG) {
                        type = NavType.IntType
                    },
                    navArgument(name = POKEMON_NAME_NAV_ARG) {
                        type = NavType.StringType
                    }
                )) { detailNavBackStack ->
                val dominantColor = remember {
                    val color = detailNavBackStack.arguments?.getInt(DOMINANT_COLOR_NAV_ARG)
                    color?.let { Color(it) } ?: Color.White
                }
                val pokemonName = remember {
                    detailNavBackStack.arguments?.getString(POKEMON_NAME_NAV_ARG)
                }


                PokemonDetailScreen(
                    dominantColor = dominantColor,
                    pokemonName = pokemonName?.lowercase(Locale.ROOT) ?: "",
                    navController = navController
                )
            }
        }
    }


    companion object {

        const val POKEMON_LIST_SCREEN_DESTINATION = "pokemon_list_screen"
        const val POKEMON_DETAIL_SCREEN_DESTINATION = "pokemon_detail_screen"
        const val DOMINANT_COLOR_NAV_ARG = "dominantColor"
        const val POKEMON_NAME_NAV_ARG = "pokemonName"
    }

    @Composable
    fun MyApp(content: @Composable () -> Unit) {
        JetPokeTheme {
            Surface(color = MaterialTheme.colors.background) {
                content()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
//    val navController = rememberNavController()
//    NavHostInit(navController = navController)
}


