package com.example.myapplication

import MyApiSdk
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.myapplication.databinding.FragmentAuthenticateBinding
import com.example.mylibrary.model.AuthRequest
import com.example.mylibrary.model.Workflow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthenticateFragment : Fragment() {

    private val binding by lazy {
        FragmentAuthenticateBinding.inflate(layoutInflater)
    }
    private val token = "eyJ0eXAiOiJhdCtKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ5ZHRKNEJXRG9zcmlpeGNsR0dWbDZESTYyY2hyRnJ6WGdwZHVQRWxTZWdFb0lUN1AiLCJpc3MiOiJodHRwczovLzM0LjEwMi4yMjAuMTEyLm5pcC5pby9vYXV0aDItY2MiLCJleHAiOjE3Mzc2MTU4ODQsImlhdCI6MTczNzYxMjI4NCwianRpIjoiNWVjMzIxNTAtOTMzMC00ZjVkLWIzOGUtODEzMDc2YzY3N2M2IiwiY2xpZW50X2lkIjoieWR0SjRCV0Rvc3JpaXhjbEdHVmw2REk2MmNockZyelhncGR1UEVsU2VnRW9JVDdQIn0.vYsb9TRg7XCD0LSA9DPItqOcQ4Yqius5YKmCABum_t7nTU3g4gYqa3w-HzwVybqxaxGT6uxCKUqMa6pIy2r56fS3lnc-5XEgwP0MIyrPqsvYk_oK6ytVKTYI-etqZKurqJsj6SSyuULM-Lm5s51Tq81mwvSeDW9jNK8tJSqdG-RV1LARmu18-KX2Ucsh9FkuC8R9bwk0srTKvupz8gEnfVTP_UkZPPOVN_4cRbdJYfQMDACF56E4zQ10sMHD_zFMQmS058zODEpqIUwiOFNdWsL0z9WrpXE9JElPmASIqxuBaJfQV5zholLmFtGCvsIa5BrwO9PBhNxLEbGm_DMwMA"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnApiCall.setOnClickListener{
            authenticate()
        }
    }

    private fun authenticate() {
        val sdk = MyApiSdk.getInstance()


        val authRequest = AuthRequest(
            brand = "IOH",
            workflow = listOf(
                Workflow(channel = "silent_auth", mobileNumberTo = binding.number.text.toString()),
                //Workflow(channel = "sms", mobileNumberTo = binding.number.text.toString()),
                // Plain mobile number
            )
        )

        // Use the MyApiSdk to encrypt the data
        sdk.authenticate(
            token,
            authRequest,
            object : MyApiSdk.ApiCallback {
                override fun onSuccess(result: String) {
                    // Handle success
                    println("Authentication Successful: $result")

                    val jsonResponse = JSONObject(result)

                    // Extract values and store them in variables
                    val txnId = jsonResponse.getString("txnId")
                    val redirectionUrl = jsonResponse.getString("redirectionUrl")

                    //binding.webView.webViewClient = WebViewClient()

                    //binding.webView.loadUrl(redirectionUrl)
                    binding.webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)

                            // Make API call when the page finishes loading
                            CoroutineScope(Dispatchers.Main).launch {
                                sdk.getStatus(
                                    token,
                                    txnId,
                                    object : MyApiSdk.ApiCallback {
                                        override fun onSuccess(result: String) {
                                            println("Status Fetch Successful: $result")

                                            val statusResponse = JSONObject(result)
                                            val status = statusResponse.getString("status")
                                            if (status == "true")
                                            {
                                                Navigation.findNavController(view!!).navigate(R.id.action_authenticateFragment_to_statusFragment)
                                                Toast.makeText(requireContext(), "Authenticated", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                //Navigation.findNavController(view!!).navigate(R.id.action_authenticateFragment_to_statusFalseFragment)

                                                Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();

                                                println("Status is false")
                                            }
                                        }

                                        override fun onError(error: String) {
                                            println("Status Fetch Failed: $error")
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Load the redirection URL
                    binding.webView.loadUrl(redirectionUrl)

                }

                override fun onError(error: String) {
                    // Handle error
                    println("Authentication Failed: $error")
                }
            })
    }
}