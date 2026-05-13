import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akac.R
import com.example.akac.adapters.ContactAdap
import com.example.akac.data.Contact
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var cm: ContactManager

    private val contactsList          = mutableListOf<Contact>()
    private val permissionRequestCode = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        rv = findViewById(R.id.rvContacts)
        rv.layoutManager = LinearLayoutManager(this)

        cm = ContactManager(this)
        initContacts()
    }

    private fun initContacts() {
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        val callPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)

        if (readPermission == PackageManager.PERMISSION_GRANTED &&
            callPermission == PackageManager.PERMISSION_GRANTED
        ) {
            loadContacts()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE),
                permissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode:  Int,
        permissions:  Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts()
            } else {
                Toast.makeText(this, "No solutions provided", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadContacts() {
        CoroutineScope(Dispatchers.IO).launch {
            val contacts = cm.getContacts()
            withContext(Dispatchers.Main) {
                contactsList.clear()
                contactsList.addAll(contacts)
                rv.adapter = ContactAdap(this@MainActivity, contactsList)
            }
        }
    }
}