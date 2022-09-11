package com.unirutas.uniguajira

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.horario_dialog.view.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("Rutas")
    private lateinit var lastLocation: Location
    private val tmpRealTimeMarker: ArrayList<Marker> = ArrayList()
    private val realTimeMarker: ArrayList<Marker> = ArrayList()
    private lateinit var posicion: MutableList<Locacion>
    private lateinit var listaRutas: Spinner
    private lateinit var idRuta: String
    private lateinit var rutaSelect: String
    private var once: Boolean = true
    private lateinit var imgOn: ImageView
    private lateinit var imgOff: ImageView
    private var estadoApp:Boolean = false
    private val listaRutasR = ArrayList<String>()
    private val listaIdRutas = ArrayList<String>()

    companion object{
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var map: GoogleMap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkCurrentUser()
        val actualizar = Handler(Looper.getMainLooper())
        listaRutas = findViewById(R.id.selectRuta)
        imgOn = findViewById(R.id.imgEstadoOn)
        imgOff = findViewById(R.id.imgEstadoOff)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        posicion = mutableListOf()

        //Obtener y actualizar las posiciones de las rutas del Firebase
        actualizar.post(object : Runnable {
            override fun run() {
                if(estadoApp){
                    colocarRutas()
                }
                actualizar.postDelayed(this, 250)
            }
        })

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        /* Creo array de las rutas registradas en la base de Datos, obtengo sus ids y nombres */
        mDatabase.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                //nada
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    for(h in p0.children){
                        listaIdRutas.add(h.key.toString())
                        listaRutasR.add(h.child("Nombre").value.toString())
                    }

                    val spinAdapt = ArrayAdapter<String>(this@MainActivity,android.R.layout.simple_spinner_item,listaRutasR)
                    spinAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    listaRutas.adapter = spinAdapt

                }

            }
        })

        //Miro que ruta se selecciono

        listaRutas.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                rutaSelect = listaRutasR[0]
                idRuta = listaIdRutas[0]
                estadoApp= true
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                estadoApp= true
                rutaSelect = listaRutasR[p2]
                idRuta = listaIdRutas[p2]

                mDatabase.child(idRuta).addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()){
                            for(h in p0.children){
                                val lat = p0.child("Latitud").value.toString()
                                val long = p0.child("Longitud").value.toString()

                                val rutaLatLong = LatLng(lat.toDouble(), long.toDouble())
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(rutaLatLong,14f))

                            }
                        }

                    }
                })
            }

        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera

        map.setOnMarkerClickListener(this)
        map.uiSettings.isZoomControlsEnabled = true

        setUpMap()
    }

    private fun placeMarker(location: LatLng){
        val user = FirebaseAuth.getInstance().currentUser
        val markerOptions = MarkerOptions().position(location).title(rutaSelect).icon(BitmapDescriptorFactory.fromResource(R.drawable.www2))

        map.addMarker(markerOptions)
    }

    private fun setUpMap(){
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_HYBRID   //Cambiar Estilo de Mapa
    }

    private fun colocarRutas(){
        mDatabase.child(idRuta).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                map.clear()

                if(p0.exists()){
                    for(h in p0.children){
                        val lat = p0.child("Latitud").value.toString()
                        val long = p0.child("Longitud").value.toString()
                        val estado = p0.child("Estado").value.toString()

                        val rutaLatLong = LatLng(lat.toDouble(), long.toDouble())
                        placeMarker(rutaLatLong)

                        mirarEstado(estado.toInt())

                    }
                }

            }
        })
    }

    private fun mirarEstado(e:Int){
        if(e == 1){
            imgOn.visibility = View.VISIBLE
            imgOff.visibility = View.GONE
        }else{
            imgOn.visibility = View.GONE
            imgOff.visibility = View.VISIBLE
        }
    }

    private fun checkCurrentUser(){
        val user = FirebaseAuth.getInstance().currentUser
        if(user!=null){
            if(user.isEmailVerified){
                getUserProfile(user)
            }else{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                Toast.makeText(this,"Revise su Correo y Active su Cuenta.", Toast.LENGTH_LONG).show()
            }

        }else{
            Toast.makeText(this,"Conectado con usuario Anonimo, Felicidades", Toast.LENGTH_SHORT).show()
        }
    }

    fun mostrarHorario(view: View?){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.horario_dialog,null)
        val mBuilder = AlertDialog.Builder(this)
            .setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mDialogView.dialogBack.setOnClickListener {
            mAlertDialog.dismiss()

        }
    }

    private fun getUserProfile(user: FirebaseUser?){
        user.let {

            val nombre = user?.displayName


        }
    }

    fun signOut(view: View?){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Seguro que Desea Salir?")

        builder.setMessage("Cerrar Sesión")

        builder.setPositiveButton("Si"){dialog, which->
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
            Toast.makeText(this,"Sesión Cerrada.", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("No"){_,_->}
        val dialog: AlertDialog = builder.create()

        dialog.show()
    }
}
