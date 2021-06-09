package z.com.jetpoke.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import z.com.jetpoke.models.PokedexListEntry
import z.com.jetpoke.repository.Repository
import z.com.jetpoke.utils.Resource
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private var currPage = 0

    val pokemonList = mutableStateOf<List<PokedexListEntry>>(emptyList())
    val loadError = mutableStateOf("")
    val isLoading = mutableStateOf(false)
    val endReached = mutableStateOf(false)

    private var cachedPokemonList = emptyList<PokedexListEntry>()
    private var isSearchStarting = true
    val isSearching = mutableStateOf(false)

    init{
        loadPokemonPaginated()
    }


    fun searchPokemonList(query: String){
        val listToSearch = if (isSearchStarting) pokemonList.value
        else cachedPokemonList


        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()) {
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }

            val results = listToSearch.filter {
                it.pokemonName.contains(
                    query.trim(),
                    ignoreCase = true
                ) || it.number.toString() == query.trim()
            }

            if (isSearchStarting) {
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }

            pokemonList.value = results
            isSearching.value = true
        }
    }


    fun loadPokemonPaginated() {
        viewModelScope.launch {
            isLoading.value = true
            val result = repository.getPokemonList(PAGE_SIZE, currPage * PAGE_SIZE)
            when (result) {
                is Resource.Error -> {
                    loadError.value = result.message!!
                    isLoading.value = false
                }
                is Resource.Success -> {
                    endReached.value = currPage * PAGE_SIZE >= result.data!!.count
                    val pokes = result.data.results.mapIndexed { index, entry ->
                        val numberOfPoke =
                            entry.url.dropLastWhile { it == '/' }.takeLastWhile { it.isDigit() }
                        val imageUrl = "$IMAGE_URL$numberOfPoke.png"
                        PokedexListEntry(entry.name.capitalize(Locale.ROOT), imageUrl, numberOfPoke.toInt())
                    }

                    currPage++

                    loadError.value = ""
                    isLoading.value = false
                    pokemonList.value += pokes
                }
            }
        }
    }


    fun calcDominantColor(
        drawable: Drawable, onFinish: (Color) -> Unit
    ) {
        val bitmap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Log.d("rob", "calcDominantColor: Entered viewmodel: $bitmap")

        Palette.from(bitmap).generate { palette ->
                Log.d("rob", "calcDominantColor: Entered: $palette")
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                Log.d("rob", "calcDominantColor: Color Generated: $colorValue")
                onFinish(Color(colorValue))
            }
        }
    }


    private companion object {
        private const val PAGE_SIZE = 20
        private const val IMAGE_URL = "https://pokeres.bastionbot.org/images/pokemon/"
    }
}