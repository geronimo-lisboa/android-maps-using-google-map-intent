package geronimo.don.googlemapsintenttest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.Single

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if ( verifyPermission() ){
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        val x =getCurrentLocationRx()
            .subscribe { location:Location->
                Toast.makeText(this, "LOCATION = ${location.longitude}, ${location.latitude}", Toast.LENGTH_LONG).show()
                showMap(location)
            }

    }

    private fun showMap(location:Location){
        val uri = Uri.parse("geo:${location.latitude},${location.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivityForResult(mapIntent, TESTE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==  TESTE){
            Log.d(TAG, "xxx")
        }
    }
    @SuppressLint("MissingPermission")
    private fun getCurrentLocationRx():Single<Location>{
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        return Single.create<Location>{emitter->
            fusedClient.lastLocation.addOnSuccessListener { location ->
                Log.d(TAG, "user location ${location.latitude},${location.longitude}")
                emitter.onSuccess(location)
            }.addOnFailureListener {error ->
                Log.e(TAG,"error fetching location",error)
                emitter.onError(error)
            }
        }
    }
    //Requisição das três permissões que preciso
    private fun verifyPermission():Boolean {
        val coarseLocationCheck =  ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val internetCheck =  ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
        val permissionRequestList = ArrayList<String>()
        if(coarseLocationCheck != PackageManager.PERMISSION_GRANTED){
            permissionRequestList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if(fineLocationCheck != PackageManager.PERMISSION_GRANTED){
            permissionRequestList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(internetCheck != PackageManager.PERMISSION_GRANTED){
            permissionRequestList.add(Manifest.permission.INTERNET)
        }
        if(permissionRequestList.size > 0){
            ActivityCompat.requestPermissions(this, permissionRequestList.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }else{
            return true
        }
    }
    //Trata o resultado das permissões
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_REQUEST_CODE->{
                var allPermissionsGranted = true;
                for(i in 0..(permissions.size-1)){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "${permissions[i]} needed", Toast.LENGTH_SHORT).show()
                        allPermissionsGranted = false
                    }
                }
                if(allPermissionsGranted){
                    getCurrentLocation()
                }
            }
        }
    }

    companion object {
        val PERMISSION_REQUEST_CODE = 666
        val TAG = "teste_map"
        val TESTE = 10
    }
}
