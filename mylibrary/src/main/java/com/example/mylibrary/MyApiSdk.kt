import android.util.Log
import com.example.mylibrary.ApiClient
import com.example.mylibrary.api.ApiService
import com.example.mylibrary.model.AuthRequest
import com.example.mylibrary.model.VerifyOtpRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher

class MyApiSdk private constructor() {
    private val authApiService: ApiService =
        ApiClient.getInstance().create(ApiService::class.java)
    

    private val publicKeyString = """
        -----BEGIN PUBLIC KEY-----
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8I04B6voEYbDr+tnT91p
        rCsdFYxv7bdSRteACfr5Iam8EJXEpQIU6uAsE1uA/Gv1+CjH5olDDXP1P2wnQpXJ
        8R7Ktw5eCWt3TRfCIfW+EnacnsFlt7t0N8dHJWljE6bhgULbulpsS9QqX6ufjuCa
        h4LzavHYF2Do6q/1eQXKU7lyuuTV7BgXe05jRlOLvKAf4/s/oX9QJ60rQ1cpVd0x
        M0O9ZpzAZN9gGf/qD/7tXWbSHhLI+HXpGkCuoWDgNAMi+EtrOc2754MF7UuNDHO8
        fSYfiDmojCx0x4/b1fgS/iGgzDFGDfYYgHxm9rdcjOCTqImASyem18FY/1cDYhjj
        cQIDAQAB
        -----END PUBLIC KEY-----
        """

    companion object {
        private var instance: MyApiSdk? = null

        fun getInstance(): MyApiSdk {
            if (instance == null) {
                instance = MyApiSdk()
            }
            return instance!!
        }
        fun setEnvironment(isProduction: Boolean) {
            val baseUrl = if (isProduction) {
                "https://prod.api.com/"
            } else {
                "https://dev.api.com/"
            }
            ApiClient.setBaseUrl(baseUrl)
        }
    }

    // RSA Encryption Method with proper error handling
    private fun encryptWithPublicKey(data: String): String {
        try {
            // Remove PEM headers and footers and get Base64 encoded string
            val cleanKey = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("\\s".toRegex(), "")

            val publicKeyBytes = Base64.getDecoder().decode(cleanKey)
            val keySpec = X509EncodedKeySpec(publicKeyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey: PublicKey = keyFactory.generatePublic(keySpec)

            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            Log.e("DataToEncrypt", data)

            val encryptedData = cipher.doFinal(data.toByteArray())

            return Base64.getEncoder().encodeToString(encryptedData)
        } catch (e: Exception) {
            Log.e("EncryptionError", "Error encrypting mobile number: ${e.message}", e)
            return ""
        }
    }


    fun authenticate(
        token: String,
        authRequest: AuthRequest,
        callback: ApiCallback
    ) {
        // Encrypt all mobileNumberTo values in the workflow list
        val encryptedWorkflow = authRequest.workflow.map { workflow ->
            // Encrypt the mobile number for each workflow
            val encryptedMobileNumber = encryptWithPublicKey(workflow.mobileNumberTo)
            workflow.copy(mobileNumberTo = encryptedMobileNumber)
        }

        val encryptedAuthRequest = authRequest.copy(workflow = encryptedWorkflow)

        Log.d("EncryptedAuthRequest", encryptedAuthRequest.toString())

        Log.d("Retrofit", "Making API request...")
        val call = authApiService.authenticate("Bearer $token", encryptedAuthRequest)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Retrofit", "onResponse called")
                Log.d("onResponse", "Response Code: ${response.code()}")
                Log.d("onResponse", "Response Headers: ${response.headers()}")
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.string()

                    Log.d("ApiResponse", "Response Body: $responseBody")

                    callback.onSuccess(responseBody)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("ApiError", "Error: ${response.message()}, Body: $errorBody")

                    callback.onError("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("onFailure", "Failure: ${t.message}")
                callback.onError("Failure: ${t.message}")
            }
        })
    }

    fun getStatus(
        token: String,
        txnId: String,
        callback: ApiCallback
    ) {
        val call = authApiService.getStatus("Bearer $token", txnId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("Retrofit", "onResponse called")
                Log.d("onResponse", "Response Code: ${response.code()}")
                Log.d("onResponse", "Response Headers: ${response.headers()}")
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.string()

                    Log.d("ApiResponse", "Response Body: $responseBody")

                    callback.onSuccess(responseBody)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("ApiError", "Error: ${response.message()}, Body: $errorBody")

                    callback.onError("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("onFailure", "Failure: ${t.message}")
                callback.onError("Failure: ${t.message}")
            }
        })
    }

    fun verifyOtp(
        token: String,
        txnId: String,
        otp: String,
        callback: ApiCallback
    ) {
        val encryptedOTP = encryptWithPublicKey(otp)
        val request = VerifyOtpRequest(txnId, encryptedOTP)

        val call = authApiService.verifyOtp("Bearer $token", request)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.string()
                    Log.d("ApiResponse", "Response Body: $responseBody")
                    callback.onSuccess(responseBody)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("ApiError", "Error: ${response.message()}, Body: $errorBody")
                    callback.onError("Error: ${response.message()}, Body: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ApiFailure", "Failure: ${t.message}")
                callback.onError("Failure: ${t.message}")
            }
        })
    }

    fun resendOtp(
        token: String,
        txnId: String,
        callback: ApiCallback
    ) {
        val call = authApiService.resendOtp("Bearer $token", txnId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!.string()
                    Log.d("ApiResponse", "Response Body: $responseBody")
                    callback.onSuccess(responseBody)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("ApiError", "Error: ${response.message()}, Body: $errorBody")
                    callback.onError("Error: ${response.message()}, Body: $errorBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("ApiFailure", "Failure: ${t.message}")
                callback.onError("Failure: ${t.message}")
            }
        })
    }

    interface ApiCallback {
        fun onSuccess(result: String)
        fun onError(error: String)
    }
}
