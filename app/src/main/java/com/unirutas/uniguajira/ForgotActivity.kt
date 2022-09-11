package com.unirutas.uniguajira

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {
    private lateinit var txtEmail: EditText
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)
        txtEmail = findViewById(R.id.txtEmail)
        auth = FirebaseAuth.getInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.left_in,R.anim.left_out)
    }

    fun send(view: View){
        val email = txtEmail.text.toString()
        val partes: List<String> = email.split("@")



        if(!email.isEmpty()){
            if(validEmail(email)) {
                if (partes[1] == "uniguajira.edu.co") {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(this){
                            task->
                            if(task.isSuccessful){
                                startActivity(Intent(this, LoginActivity::class.java))
                                overridePendingTransition(R.anim.left_in,R.anim.left_out)
                                Toast.makeText(this, "Revise su correo para Restablecer su contraseña",Toast.LENGTH_SHORT).show()

                            }else{
                                Toast.makeText(this, "Error al enviar correo de recuperacion",Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Solo se aceptan correos: @uniguajira.edu.co",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                Toast.makeText(this, "Escriba una dirrecion de correo valida",Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Escriba un correo",Toast.LENGTH_SHORT).show()
        }
    }

    private fun validEmail(email:String) : Boolean{
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}
