package z.com.jetpoke.pokemonlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.accompanist.coil.CoilPainterDefaults
import com.google.accompanist.coil.LocalImageLoader
import com.google.accompanist.coil.rememberCoilPainter
import z.com.jetpoke.MainActivity
import z.com.jetpoke.R
import z.com.jetpoke.models.PokedexListEntry
import z.com.jetpoke.ui.theme.Roboto


@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_pokemon_logo),
                contentDescription = "Pokemon Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally)
            )
            SearchBar(
                hint = "Search...", modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                viewModel.searchPokemonList(it)
            }

            Spacer(modifier = Modifier.height(16.dp))
            PokemonList(navController = navController)
        }
    }

}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit = {}
) {
    var text by remember {
        mutableStateOf("")
    }
    var isHintDisplayed by remember {
        mutableStateOf(hint != "")
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onSearch(it)

                isHintDisplayed = text.isEmpty()
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(Color.White, CircleShape)
                .padding(20.dp, 12.dp)
                .onFocusChanged {
                    isHintDisplayed = text.isEmpty() // && !it.isFocused
                }
        )
        if (isHintDisplayed) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

        }
    }

}

@Composable
fun PokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val pokemonList by remember { viewModel.pokemonList }
    val endReached by remember { viewModel.endReached }
    val loadError by remember { viewModel.loadError }
    val isLoading by remember { viewModel.isLoading }

    val isSearching by remember { viewModel.isSearching }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        val itemCount = pokemonList.size / 2 + if (pokemonList.size % 2 == 0) 0 else 1

        items(itemCount) {
            //detect if we scroll to end to paginate
            if (it >= itemCount - 1 && !endReached && !isLoading && !isSearching) {
                viewModel.loadPokemonPaginated()
            }
            PokedexRow(rowIndex = it, entries = pokemonList, navController = navController)
        }
    }

    Box(contentAlignment = Center, modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colors.primary)
        }
        if (loadError.isNotEmpty()) {
            RetrySection(error = loadError) {
                viewModel.loadPokemonPaginated()
            }
        }

    }
}


@Composable
fun PokedexEntryItem(
    entry: PokedexListEntry,
    navController: NavController,
    modifier: Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember {
        mutableStateOf(defaultDominantColor)
    }


    Box(
        contentAlignment = Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(dominantColor, defaultDominantColor)
                )
            )
            .clickable {
                navController.navigate(
                    "${MainActivity.POKEMON_DETAIL_SCREEN_DESTINATION}/${dominantColor.toArgb()}/${entry.pokemonName}"
                )
            }
    ) {
        Column {
            /**
             * here ths code of New coil
             */
            val painter = rememberCoilPainter(
                request = ImageRequest.Builder(LocalContext.current)
                    .data(entry.imageUrl)
                    .target { drawable ->
                        viewModel.calcDominantColor(drawable) { color ->
                            dominantColor = color
                        }
                    }
                    .build(),
                fadeIn = true,
                requestBuilder = {
                        target { drawable ->
                            viewModel.calcDominantColor(drawable) { color ->
                                dominantColor = color
                            }
                        }
                },
            )
            Image(
                painter = painter,
                contentDescription = "detail poke image",
                modifier = Modifier
                    .size(120.dp)
                    .align(CenterHorizontally),
            )

            Text(
                text = entry.pokemonName,
                fontFamily = Roboto,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun PokedexRow(
    rowIndex: Int,
    entries: List<PokedexListEntry>,
    navController: NavController
) {
    Column {
        Row {
            PokedexEntryItem(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))

            if (entries.size > rowIndex * 2 + 1) {
                PokedexEntryItem(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RetrySection(
    error: String,
    onRetry: () -> Unit
) {
    Column {
        Text(text = error, color = Color.Red, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onRetry() }, modifier = Modifier.align(CenterHorizontally)) {
            Text(text = "Retry")
        }
    }
}


@Preview(showSystemUi = true)
@Composable
private fun SearchBarPreview() {
//    PokemonListScreen(navController = null)
//    val list = List(6){PokedexListEntry("Test $it", "Url $it", it)}
//    LazyColumn {
//        items {
//            PokedexRow(rowIndex = , entries = , navController = )
//        }
//    }
}


/**
 * new Image coil but not work with Pallete cause Target not called
 */
//val painter = rememberCoilPainter(
//    request = ImageRequest.Builder(LocalContext.current)
//        .data(entry.imageUrl)
//        .target { drawable ->
//            viewModel.calcDominantColor(drawable) { color ->
//                dominantColor = color
//            }
//        }
//        .build(),
//    fadeIn = true,
//)
//Image(
//painter = painter,
//contentDescription = "detail poke image",
//modifier = Modifier
//.size(120.dp)
//.align(CenterHorizontally),
//)


/**
 * Old Before Deprecation
 */
//CoilImage(
//request = ImageRequest.Builder(LocalContext.current)
//.data(entry.imageUrl)
//.target { drawable ->
//    viewModel.calcDominantColor(drawable) { color ->
//        dominantColor = color
//    }
//}
//.build(),
//contentDescription = entry.pokemonName,
//fadeIn = true,
//modifier = Modifier
//.size(120.dp)
//.align(CenterHorizontally)
//) {
//    CircularProgressIndicator(
//        color = MaterialTheme.colors.primary,
//        modifier = Modifier.scale(.5f)
//    )
//}