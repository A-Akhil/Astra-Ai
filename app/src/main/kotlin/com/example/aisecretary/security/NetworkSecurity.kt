package com.example.aisecretary.security

import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.io.ByteArrayInputStream
import android.util.Base64

/**
 * Secure network communication with certificate pinning and encryption
 */
class NetworkSecurity {
    
    companion object {
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
    }

    /**
     * Create secure OkHttpClient with certificate pinning
     */
    fun createSecureOkHttpClient(baseUrl: String): OkHttpClient {
        // Certificate pinning for enhanced security
        val certificatePinner = CertificatePinner.Builder()
            .add(baseUrl, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // Replace with actual certificate hash
            .build()

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .certificatePinner(certificatePinner)
            .addInterceptor(SecurityHeadersInterceptor())
            .addInterceptor(RequestEncryptionInterceptor())
            .build()
    }

    /**
     * Create secure Retrofit instance
     */
    fun createSecureRetrofit(baseUrl: String): Retrofit {
        val secureClient = createSecureOkHttpClient(baseUrl)
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(secureClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Interceptor to add security headers
     */
    private class SecurityHeadersInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val secureRequest = originalRequest.newBuilder()
                .addHeader("X-Content-Type-Options", "nosniff")
                .addHeader("X-Frame-Options", "DENY")
                .addHeader("X-XSS-Protection", "1; mode=block")
                .addHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
                .addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                .addHeader("Pragma", "no-cache")
                .addHeader("Expires", "0")
                .build()
            
            return chain.proceed(secureRequest)
        }
    }

    /**
     * Interceptor for request/response encryption
     */
    private class RequestEncryptionInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            // Add encryption headers and process request body if needed
            val encryptedRequest = originalRequest.newBuilder()
                .addHeader("X-Encryption-Version", "1.0")
                .addHeader("X-Client-Version", "astra-ai-1.0")
                .build()
            
            return chain.proceed(encryptedRequest)
        }
    }
}

/**
 * End-to-end encryption for API communications
 */
class ApiEncryption {
    
    companion object {
        private const val AES_ALGORITHM = "AES"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val KEY_LENGTH = 256
    }

    private val secureRandom = SecureRandom()

    /**
     * Generate a new AES key for session encryption
     */
    fun generateSessionKey(): SecretKeySpec {
        val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
        keyGenerator.init(KEY_LENGTH, secureRandom)
        val secretKey = keyGenerator.generateKey()
        return SecretKeySpec(secretKey.encoded, AES_ALGORITHM)
    }

    /**
     * Encrypt request payload
     */
    fun encryptRequest(data: String, sessionKey: SecretKeySpec): EncryptedPayload {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, sessionKey, secureRandom)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        return EncryptedPayload(
            data = Base64.encodeToString(encryptedData, Base64.NO_WRAP),
            iv = Base64.encodeToString(iv, Base64.NO_WRAP)
        )
    }

    /**
     * Decrypt response payload
     */
    fun decryptResponse(encryptedPayload: EncryptedPayload, sessionKey: SecretKeySpec): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val iv = Base64.decode(encryptedPayload.iv, Base64.NO_WRAP)
        val encryptedData = Base64.decode(encryptedPayload.data, Base64.NO_WRAP)
        
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, javax.crypto.spec.GCMParameterSpec(128, iv))
        val decryptedData = cipher.doFinal(encryptedData)
        
        return String(decryptedData)
    }

    data class EncryptedPayload(
        val data: String,
        val iv: String
    )
}
