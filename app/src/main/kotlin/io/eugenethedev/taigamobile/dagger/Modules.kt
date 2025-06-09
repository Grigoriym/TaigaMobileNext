package io.eugenethedev.taigamobile.dagger

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.eugenethedev.taigamobile.BuildConfig
import io.eugenethedev.taigamobile.data.api.RefreshTokenRequest
import io.eugenethedev.taigamobile.data.api.RefreshTokenRequestJsonAdapter
import io.eugenethedev.taigamobile.data.api.RefreshTokenResponseJsonAdapter
import io.eugenethedev.taigamobile.data.api.TaigaApi
import io.eugenethedev.taigamobile.data.repositories.AuthRepository
import io.eugenethedev.taigamobile.data.repositories.ProjectsRepository
import io.eugenethedev.taigamobile.data.repositories.SprintsRepository
import io.eugenethedev.taigamobile.data.repositories.TasksRepository
import io.eugenethedev.taigamobile.data.repositories.UsersRepository
import io.eugenethedev.taigamobile.data.repositories.WikiRepository
import io.eugenethedev.taigamobile.domain.repositories.IAuthRepository
import io.eugenethedev.taigamobile.domain.repositories.IProjectsRepository
import io.eugenethedev.taigamobile.domain.repositories.ISprintsRepository
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.domain.repositories.IUsersRepository
import io.eugenethedev.taigamobile.domain.repositories.IWikiRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.state.Settings
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import javax.inject.Singleton

@Module
class DataModule {

    @Singleton
    @Provides
    fun provideTaigaApi(session: Session, moshi: Moshi): TaigaApi {
        val baseUrlPlaceholder = "https://nothing.nothing"
        fun getApiUrl() = // for compatibility with older app versions
            if (!session.server.value.run { startsWith("https://") || startsWith("http://") }) {
                "https://"
            } else {
                ""
            } + "${session.server.value}/${TaigaApi.API_PREFIX}"

        val okHttpBuilder = OkHttpClient.Builder()
            .addInterceptor {
                it.run {
                    val url = it.request().url.toUrl().toExternalForm()

                    proceed(
                        request()
                            .newBuilder()
                            .url(url.replace(baseUrlPlaceholder, getApiUrl()))
                            .header("User-Agent", "TaigaMobile/${BuildConfig.VERSION_NAME}")
                            .also {
                                if ("/${TaigaApi.AUTH_ENDPOINTS}" !in url) { // do not add Authorization header to authorization requests
                                    it.header("Authorization", "Bearer ${session.token.value}")
                                }
                            }
                            .build()
                    )
                }
            }
            .addInterceptor(
                HttpLoggingInterceptor(Timber::d)
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
                    .also { it.redactHeader("Authorization") }
            )

        val tokenClient = okHttpBuilder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrlPlaceholder) // base url is set dynamically in interceptor
            .addConverterFactory(MoshiConverterFactory.create(moshi).withNullSerialization())
            .client(
                okHttpBuilder.authenticator { _, response ->
                    response.request.header("Authorization")?.let {
                        try {
                            // prevent multiple refresh requests from different threads
                            synchronized(session) {
                                // refresh token only if it was not refreshed in another thread
                                if (it.replace("Bearer ", "") == session.token.value) {
                                    val body = RefreshTokenRequestJsonAdapter(moshi)
                                        .toJson(RefreshTokenRequest(session.refreshToken.value))

                                    val request = Request.Builder()
                                        .url("$baseUrlPlaceholder/${TaigaApi.REFRESH_ENDPOINT}")
                                        .post(body.toRequestBody("application/json".toMediaType()))
                                        .build()

                                    val refreshResponse = RefreshTokenResponseJsonAdapter(moshi)
                                        .fromJson(
                                            tokenClient.newCall(request).execute().body?.string()
                                                .orEmpty()
                                        )
                                        ?: throw IllegalStateException("Cannot parse RefreshResponse")

                                    session.changeAuthCredentials(
                                        refreshResponse.auth_token,
                                        refreshResponse.refresh
                                    )
                                }
                            }

                            response.request.newBuilder()
                                .header("Authorization", "Bearer ${session.token.value}")
                                .build()
                        } catch (e: Exception) {
                            Timber.w(e)
                            session.changeAuthCredentials("", "")
                            null
                        }
                    }
                }
                    .build()
            )
            .build()
            .create(TaigaApi::class.java)
    }

    @Provides
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(LocalDateTypeAdapter())
        .add(LocalDateTimeTypeAdapter())
        .build()

    @Provides
    @Singleton
    fun provideSession(context: Context, moshi: Moshi) = Session(context, moshi)

    @Provides
    @Singleton
    fun provideSettings(context: Context) = Settings(context)
}

@Module
abstract class RepositoriesModule {
    @Singleton
    @Binds
    abstract fun bindIAuthRepository(authRepository: AuthRepository): IAuthRepository

    @Singleton
    @Binds
    abstract fun bindIProjectsRepository(searchRepository: ProjectsRepository): IProjectsRepository

    @Singleton
    @Binds
    abstract fun bindIStoriesRepository(storiesRepository: TasksRepository): ITasksRepository

    @Singleton
    @Binds
    abstract fun bindIUsersRepository(usersRepository: UsersRepository): IUsersRepository

    @Singleton
    @Binds
    abstract fun bindISprintsRepository(sprintsRepository: SprintsRepository): ISprintsRepository

    @Singleton
    @Binds
    abstract fun bindIWikiRepository(wikiRepository: WikiRepository): IWikiRepository
}
