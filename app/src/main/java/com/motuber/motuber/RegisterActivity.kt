package com.motuber.motuber

import android.app.DownloadManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Request.Method.POST
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import kotlin.jvm.Throws


lateinit var editDni : EditText

lateinit var buttDni : Button
lateinit var textName : EditText

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        editDni = findViewById(R.id.edittext_dni)
        buttDni = findViewById(R.id.butt_dni)
        textName = findViewById(R.id.reg_name)

        buttDni.setOnClickListener {
            ValidateDni()
        }
    }


    fun ValidateDni() {

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, "https://dniruc.apisperu.com/api/v1/dni/"+ editDni.text.toString()+"?token=" +
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6ImFuZHJlc2FkMTNAZ21haWwuY29tIn0.HszGw0f-0aVwnWgF5JZ_c-6U8WTYA4KE6qsZJoY-X_Y",
            Response.Listener<String> { response ->
                val stringResponse = response.toString()
                val jsonObj = JSONObject(stringResponse)
                if(stringResponse == "{\"success\":false,\"message\":\"No se encontraron resultados.\"}"){

                }
                else {
                    var name: String = jsonObj.getString("nombres")
                    var apellidoPaterno = jsonObj.getString("apellidoPaterno")
                    var apellidoMaterno = jsonObj.getString("apellidoMaterno")
                    textName.setText(name +" "+ apellidoPaterno)
                }


            },
            Response.ErrorListener {error ->


            })
        queue.add(stringRequest)
    }

}