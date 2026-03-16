package com.example.aisecretary.di

import android.content.Context
import android.speech.tts.TextToSpeech
import com.example.aisecretary.BuildConfig
import com.example.aisecretary.SecretaryApplication
import com.example.aisecretary.data.local.database.AppDatabase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * This object provides shared components (like Retrofit, Database, and TTS)
 * that are used across the app.
 *
 * In larger apps, libraries like Dagger or Hilt are used for this.
 */
object AppModule {

    /**
     * Creates and returns a Retrofit instance for making API calls.
     *
     * Uses a default base URL if none is set in BuildConfig.
     *
     * @return A configured Retrofit client.
     */
    fun provideRetrofit(): Retrofit {
        // Use a default URL if the BuildConfig.OLLAMA_BASE_URL is empty
        val baseUrl = if (BuildConfig.OLLAMA_BASE_URL.isBlank()) {
            "http://localhost:11434/"
        } else {
            // Ensure the URL ends with a slash
            if (BuildConfig.OLLAMA_BASE_URL.endsWith("/")) 
                BuildConfig.OLLAMA_BASE_URL 
            else 
                "${BuildConfig.OLLAMA_BASE_URL}/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Returns the app's database instance.
     *
     * @param context The application context.
     * @return The Room database instance.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return (context.applicationContext as SecretaryApplication).database
    }

    /**
     * Provides a TextToSpeech engine for speaking text out loud.
     *
     * @param context The app context.
     * @param listener Listener that gets called when TTS is initialized.
     * @return A TextToSpeech instance.
     */
    fun provideTextToSpeech(context: Context, listener: TextToSpeech.OnInitListener): TextToSpeech {
        return TextToSpeech(context, listener)
    }
}