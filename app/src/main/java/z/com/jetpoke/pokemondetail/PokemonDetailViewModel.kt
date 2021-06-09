package z.com.jetpoke.pokemondetail

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import z.com.jetpoke.data.remote.responses.Pokemon
import z.com.jetpoke.repository.Repository
import z.com.jetpoke.utils.Resource
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {


    suspend fun getPokemonInfo(pokemonName: String) : Resource<Pokemon> {
        return repository.getPokemonInfo(pokemonName)
    }
}