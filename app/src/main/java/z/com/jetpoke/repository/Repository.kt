package z.com.jetpoke.repository

import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.ViewModelScoped
import z.com.jetpoke.data.remote.PokeApi
import z.com.jetpoke.data.remote.responses.Pokemon
import z.com.jetpoke.data.remote.responses.PokemonList
import z.com.jetpoke.utils.Resource
import java.lang.Exception
import javax.inject.Inject

@ViewModelScoped
class Repository @Inject constructor(
    private val api: PokeApi
) {


    suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit, offset)
        } catch (e: Exception) {
            return Resource.Error(message = "Some Error Occurred: ${e.message}")
        }
        return Resource.Success(response)
    }


    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon> {
        val response = try {
            api.getPokemonInfo(pokemonName)
        } catch (e: Exception) {
            return Resource.Error(message = "Some Error Occurred: ${e.message}")
        }
        return Resource.Success(response)
    }
}