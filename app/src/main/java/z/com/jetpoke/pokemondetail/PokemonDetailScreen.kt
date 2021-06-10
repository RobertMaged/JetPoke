package z.com.jetpoke.pokemondetail

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.coil.rememberCoilPainter
import z.com.jetpoke.R
import z.com.jetpoke.data.remote.responses.PokeStat
import z.com.jetpoke.data.remote.responses.PokeType
import z.com.jetpoke.data.remote.responses.Pokemon
import z.com.jetpoke.utils.Resource
import z.com.jetpoke.utils.parseStatToAbbr
import z.com.jetpoke.utils.parseStatToColor
import z.com.jetpoke.utils.parseTypeToColor
import java.util.*

//background of whole Detail Screen depends on PokemonType
val dominantBackgroundByType = mutableStateOf(Color.White)

@Composable
fun PokemonDetailScreen(
    dominantColor: Color,
    pokemonName: String,
    navController: NavController,
    viewModel: PokemonDetailViewModel = hiltViewModel(),
    topPadding: Dp = 20.dp,
    pokemonImageSize: Dp = 200.dp
) {

    val pokemonInfo = produceState<Resource<Pokemon>>(initialValue = Resource.InProgress()) {
        value = viewModel.getPokemonInfo(pokemonName)
    }.value

    Box(
        modifier = Modifier
            .fillMaxSize()
//            .background(dominantColor)
            .background(dominantBackgroundByType.value)
            .padding(bottom = 16.dp)
    ) {
        PokemonDetailTopSection(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.2f)
                .align(Alignment.TopCenter)
        )


        PokemonDetailStateWrapper(
            pokemonInfo = pokemonInfo,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = topPadding + pokemonImageSize / 2f,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .shadow(10.dp, shape = RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(color = MaterialTheme.colors.surface)
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            circularLoadingModifier = Modifier
                .size(100.dp)
                .align(Alignment.Center)
                .padding(
                    top = topPadding + pokemonImageSize / 2f,
                    bottom = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        )


        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {

            if (pokemonInfo is Resource.Success) {
                pokemonInfo.data?.sprites?.let { sprites ->
                    val painter = rememberCoilPainter(request = sprites.frontDefault, fadeIn = true)

                    Image(
                        painter = painter,
                        contentDescription = pokemonInfo.data.name,
                        modifier = Modifier
                            .size(pokemonImageSize)
                            .offset(y = topPadding)
                    )

                }
            }
        }

    }


}


@Composable
fun PokemonDetailTopSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopStart, modifier = modifier.background(
            Brush.verticalGradient(
                listOf(Color.Black, Color.Transparent)
            )
        )
    ) {
        Icon(imageVector = Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
                .offset(16.dp, 16.dp)
                .clickable {
                    dominantBackgroundByType.value = Color.White
                    navController.popBackStack()
                }
        )
    }
}


@Composable
fun PokemonDetailStateWrapper(
    pokemonInfo: Resource<Pokemon>,
    modifier: Modifier = Modifier,
    circularLoadingModifier: Modifier = Modifier,
) {
    when (pokemonInfo) {
        is Resource.Error -> {
            Text(
                text = pokemonInfo.message!!,
                color = Color.Red,
                modifier = modifier
            )
        }
        is Resource.InProgress -> {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary,
                modifier = circularLoadingModifier
            )
        }
        is Resource.Success -> {
            PokemonDetailSection(
                pokemonInfo = pokemonInfo.data!!,
                modifier = modifier.offset(y = (-20).dp)
            )
        }
    }
}


@Composable
fun PokemonDetailSection(
    pokemonInfo: Pokemon,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .offset(y = 100.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "#${pokemonInfo.id} ${pokemonInfo.name}",
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 30.sp,
            color = MaterialTheme.colors.onSurface
        )

        PokemonTypeSection(pokeTypes = pokemonInfo.pokeTypes)

        PokemonDetailDataSection(pokeWeight = pokemonInfo.weight, pokeHeight = pokemonInfo.height)

        PokemonBaseStats(pokeStats = pokemonInfo.pokeStats)
    }
}


@Composable
fun PokemonTypeSection(pokeTypes: List<PokeType>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {

        pokeTypes.forEach { type ->
            dominantBackgroundByType.value = parseTypeToColor(type)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clip(CircleShape)
                    .background(dominantBackgroundByType.value)
                    .height(35.dp)
            ) {
                Text(
                    text = type.type.name.capitalize(Locale.ROOT),
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}


@Composable
fun PokemonDetailDataSection(
    pokeWeight: Int,
    pokeHeight: Int,
    sectionHeight: Dp = 80.dp
) {
    val pokeWeightInKg = remember { pokeWeight * 100f / 1000f }
    val pokeHeightInMeters = remember { pokeHeight * 100f / 1000f }

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        PokemonDetailDataItem(
            dataValue = pokeWeightInKg,
            dataUnit = "kg",
            dataIcon = R.drawable.ic_weight,
            modifier = Modifier.weight(1f)
        )

        Spacer(
            modifier = Modifier
                .size(1.dp, sectionHeight)
                .background(Color.LightGray)
        )

        PokemonDetailDataItem(
            dataValue = pokeHeightInMeters,
            dataUnit = "m",
            dataIcon = R.drawable.ic_height,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun PokemonDetailDataItem(
    dataValue: Float,
    dataUnit: String,
    @DrawableRes dataIcon: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(dataIcon),
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "$dataValue$dataUnit", color = MaterialTheme.colors.onSurface)
    }
}

@Preview(showSystemUi = true)
@Composable
fun PokemonStateItemPreview(){
    Column {

    repeat(5) {
        PokemonStatItem(statName = "Hp", statValue = it * 20, statMaxValue = 100, statColor = Color.Red, animDelay = it * 50)
        Spacer(modifier = Modifier.height(8.dp))
    }
    }
}


@Composable
fun PokemonStatItem(
    statName: String,
    statValue: Int,
    statMaxValue: Int,
    statColor: Color,
    height: Dp = 28.dp,
    animDuration: Int = 1000,
    animDelay: Int = 0
) {
    var animationPlayed by remember { mutableStateOf(false) }

    val currPercent =
        animateFloatAsState(
            targetValue = if (animationPlayed) statValue / statMaxValue.toFloat() else 0f,
            animationSpec = tween(
                durationMillis = animDuration,
                delayMillis = animDelay
            )
        )

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(
                if (isSystemInDarkTheme()) Color(0xFF505050) else Color.LightGray
            )
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(currPercent.value)
                .clip(CircleShape)
                .background(statColor)
                .padding(horizontal = 8.dp)
        ) {
            Text(text = statName, fontWeight = FontWeight.Bold)

            Text(
                text = (currPercent.value * statMaxValue).toInt().toString(),
                fontWeight = FontWeight.Bold
            )
        }
    }

}


@Composable
fun PokemonBaseStats(
    pokeStats: List<PokeStat>,
    animDelayPerItem: Int = 100
) {
    val maxBaseStat = remember { pokeStats.maxOf { it.baseStat } }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Base stats:",
            fontSize = 20.sp,
            color = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        pokeStats.forEachIndexed { index, stat ->

            PokemonStatItem(
                statName = parseStatToAbbr(stat),
                statValue = stat.baseStat,
                statMaxValue = maxBaseStat,
                statColor = parseStatToColor(stat),
                animDelay = index * animDelayPerItem
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

