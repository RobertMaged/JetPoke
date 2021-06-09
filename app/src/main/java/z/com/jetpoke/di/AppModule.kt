package z.com.jetpoke.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import z.com.jetpoke.data.remote.PokeApi
import z.com.jetpoke.repository.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRepository(
        api : PokeApi
    ) = Repository(api)

    @Singleton
    @Provides
    fun providePokeApi(): PokeApi{
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://pokeapi.co/api/v2/")
            .build()
            .create(PokeApi::class.java)
    }
}