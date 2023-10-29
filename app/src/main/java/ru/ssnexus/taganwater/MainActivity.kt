package ru.ssnexus.taganwater

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import ru.ssnexus.taganwater.databinding.ActivityMainBinding
import timber.log.Timber
import java.net.URL
import kotlin.system.exitProcess


@Suppress("DEPRECATION")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var notificationAdapter: NotificationAdapter

    private var notificationsList: MutableLiveData<ArrayList<String>> = MutableLiveData()
//    private var haveData: MutableLiveData<Boolean> = MutableLiveData()

//    private var notificationsList : ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(BuildConfig.DEBUG)
        {
            Timber.plant(Timber.DebugTree())
        }
        initLayout()
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.settings -> {
                    onBackPressed()
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }
                R.id.contacts -> {
                    onBackPressed()
                    startActivity(Intent(this@MainActivity, ContactsActivity::class.java))
                }
                R.id.about -> Toast.makeText(baseContext, "About", Toast.LENGTH_SHORT).show()
                R.id.exit -> {
                    closeApp()
                }
            }
            true
        }

    }

    private fun closeApp(){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Exit")
            .setMessage("Do you want to close app?")
            .setPositiveButton("Yes"){_, _ ->
                exitProcess(1)
            }
            .setNegativeButton("No"){dialog, _ ->
                dialog.dismiss()
            }
        val customDialog = builder.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
        customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    private fun initLayout(){
        setTheme(R.style.Theme_TaganWater)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.operInfoRV.setHasFixedSize(true)
        binding.operInfoRV.setItemViewCacheSize(15)
        binding.operInfoRV.layoutManager = LinearLayoutManager(this@MainActivity)
        notificationAdapter = NotificationAdapter(this@MainActivity, ArrayList())
        binding.operInfoRV.adapter = notificationAdapter
        if(Utils.checkConnection(this)) getData() else Toast.makeText(this, "No connection", Toast.LENGTH_SHORT).show()
        notificationsList.observe(this) {
                Timber.d("Data!!!")
                if(!it.isEmpty()) notificationAdapter.updateNotificationsList(it)
        }
    }

    private fun getData(){
        // Create a new coroutine scope
        val scope = CoroutineScope(Dispatchers.Default)
        // Launch a new coroutine in the scope
        scope.launch {
            val url = URL("http://www.tgnvoda.ru/avarii.php")
            val doc: Document = Jsoup.connect(url.toString()).get()
            var element = doc.select("table").get(1)
            val rows = element.select("tr")
            var notifications = ArrayList<String>()
            rows.forEach{row ->
                notifications.add(row.text())
            }
            notifications.removeLast()
            notificationsList.postValue(notifications)
        }
    }

    override fun onBackPressed() {
        if(binding.navView.isShown) binding.root.closeDrawers() else  closeApp()
    }

    private fun requestRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Premision Granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        Timber.d("Activity Destroyed")
        super.onDestroy()
    }

    //    inner class AppWebViewClient : WebViewClient() {
//        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//            if (url != null) {
//                view?.loadUrl(url)
//            }
////            return super.shouldOverrideUrlLoading(view, url)
//            return true
//        }
//
//        override fun onPageFinished(view: WebView?, url: String?) {
//            super.onPageFinished(view, url)
//        }
//    }


//    @SuppressLint("StaticFieldLeak")
//    inner class WebScratch : AsyncTask<Void, Void, Void>() {
//        private lateinit var words: String
//        override fun doInBackground(vararg params: Void): Void? {
//            try {
//                words = String()
//                val document =  Jsoup.connect("http://www.tgnvoda.ru/avarii.php").get()
//                words = document.text()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//            return null
//        }
//        override fun onPostExecute(aVoid: Void?) {
//            super.onPostExecute(aVoid)
//            println(words)
////            textView.text = words
//        }
//    }

}