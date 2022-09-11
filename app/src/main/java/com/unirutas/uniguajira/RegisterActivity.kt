package com.unirutas.uniguajira

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern
import kotlin.concurrent.timerTask

class RegisterActivity : AppCompatActivity() {
    private lateinit var txtName: EditText
    private lateinit var txtLastname: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPass: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var dbReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        txtName = findViewById(R.id.txtName)
        txtLastname = findViewById(R.id.txtLastname)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPass = findViewById(R.id.txtPassword2)

        progressBar= findViewById(R.id.progressBar)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        dbReference = database.reference.child("Users")

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.right_in,R.anim.right_out)
    }


    fun register(view:View){
        createNewAccount()
    }

    private fun createNewAccount() {
        val name: String = txtName.text.toString()
        val lastName: String = txtLastname.text.toString()
        val email: String = txtEmail.text.toString()
        val password: String = txtPassword.text.toString()
        val confirmPass: String = txtConfirmPass.text.toString()

        val partes: List<String> = email.split("@")

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(
                    password
                ) && validEmail(email)
            ){

                if(password == confirmPass){
                if (partes[1] == "uniguajira.edu.co") {
                    if(password.length >= 6){
                progressBar.visibility = View.VISIBLE

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isComplete) {
                            val user: FirebaseUser? = auth.currentUser

                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name).build()
                            user?.updateProfile(profileUpdates)
                            verifyEmail(user)

                            val userBD = dbReference.child(user?.uid!!)
                            userBD.child("Name").setValue(name)
                            userBD.child("LastName").setValue(lastName)
                            userBD.child("Email").setValue(email)
                            userBD.child("Pass").setValue(password)

                            progressBar.visibility = View.GONE
                            action()
                        }
                        }
                    }else{
                        Toast.makeText(this, "La contraseña debe contener al menos, 6 caracteres.", Toast.LENGTH_LONG).show()
                    }
            }else{
                    Toast.makeText(this, "Solo se Permiten Correos: @uniguajira.edu.co", Toast.LENGTH_SHORT).show()
                }
                    }else{
                    Toast.makeText(this, "Las Contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
                }
        }else {
                if (!validEmail(email)) {
                    Toast.makeText(this, "Ingrese Un Correo Valido.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Llene todos los campos, por favor.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun action(){
        startActivity(Intent(this,LoginActivity::class.java))
    }
    private fun verifyEmail(user:FirebaseUser?){
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this){
                    task->
                if(task.isSuccessful){
                    Toast.makeText(this,"Revise su correo para Activar su Cuenta",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"Error al Enviar Correo de Activacion",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validEmail(email:String) : Boolean{
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }
}


