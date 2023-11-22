package com.example.chatapp


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.content.Intent
import android.provider.ContactsContract
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost


// Dummylauncher alkuun kysyttäessä kontaktien jakamisen oikeuksia
class DummyLauncher(private val callback: ActivityResultCallback<Any?>) : ActivityResultLauncher<String>() {
    override fun launch(input: String?, options: ActivityOptionsCompat?) {
        callback.onActivityResult(null)
    }
    override fun unregister() {

    }
    override fun getContract(): ActivityResultContract<String, *> {
        return object : ActivityResultContract<String, Any?>() {
            override fun createIntent(context: Context, input: String): Intent {
                return Intent()
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Any? {
                return null
            }
        }
    }
}

// Kontaktien näyttömalli
class ContactViewModel: ViewModel() {
    private val _contacts = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val contacts: List<Pair<String, String>>
        get() = _contacts.value

    private val _contactsEmptyMessage = mutableStateOf<String?>(null)
    val contactsEmptyMessage: String?
        get() = _contactsEmptyMessage.value

    fun setContacts(contactsList: List<Pair<String, String>>) {
        _contacts.value = contactsList
    }
    fun setContactsEmptyMessage(message: String?) {
        _contactsEmptyMessage.value = message
    }
    init {
        generateRandomContacts()
    }

    private fun generateRandomContacts() {
        val randomContactsList = mutableListOf<Pair<String, String>>()
        repeat(4) {
            val randomDisplayName = "Random Contact $it"
            val randomNumber = "0420808666"

            randomContactsList.add(Pair(randomDisplayName, randomNumber))
        }
        setContacts(randomContactsList)
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private val contactViewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "mainScreen") {
                composable("mainScreen") {
                    MyAppContent(requestPermissionLauncher, contactViewModel, navController)
                }
                composable("contactsScreen") {
                    ContactsScreen(
                        contacts = contactViewModel.contacts,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("SettingsScreen") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // oikeudet myönnetty, vähän alempana accessContactList funktio
                    accessContactList()
                } else {
                    // Oikeus evätty, mahdollinen selitys miksi tarvitaan ne tähän
                }
            }
    }



    private fun hasContactPermission(): Boolean {
        return checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun accessContactList() {
        // Koodi kontaktien haulle
        // Mahdollinen enkryptauksen kohde
        if (hasContactPermission()) {
            val contactsCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data.DISPLAY_NAME,
                    ContactsContract.Data.DATA1
                ),
                ContactsContract.Data.MIMETYPE + "=?",
                arrayOf("vnd.android.cursor.item/vnd.com.example.chatapp.profile"),
                null
            )
            contactsCursor?.use { cursor ->
                val displayNameIndex = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                val dataIndex = cursor.getColumnIndex(ContactsContract.Data.DATA1)

                val contactsList = mutableListOf<Pair<String, String>>()

                while (cursor.moveToNext()) {
                    val displayName = cursor.getString(displayNameIndex)
                    val data = cursor.getString(dataIndex)

                    if (!displayName.isNullOrBlank() && !data.isNullOrBlank()) {
                        contactsList.add(Pair(displayName, data))
                    }
                }

                if (contactsList.isNotEmpty()) {
                    contactViewModel.setContacts(contactsList)

                } else {
                    contactViewModel.setContactsEmptyMessage("No contacts found with the app.")
                }
            }
        }
    }
}

    // Composablejen alku
    @Composable
    fun MyAppContent(
        requestPermissionLauncher: ActivityResultLauncher<String>,
        contactViewModel: ContactViewModel,
        navController: NavHostController
    ) {
        var counter by remember { mutableStateOf(0) }
        var displayContacts by remember { mutableStateOf(false) }
        var displayErrorMessage by remember { mutableStateOf(false)}
        var errorMessage by remember { mutableStateOf<String?>(null)}

        if (displayErrorMessage) {
            DisplayErrorDialog(
                errorMessage = contactViewModel.contactsEmptyMessage ?: "",
                onDismiss = {
                    displayErrorMessage = false
                    errorMessage = null
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
        ) {
            MyAppBar(title = "ChatApp",
                onIconClick = { navController.navigate("SettingsScreen") },
                navController = navController
                )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Counter: $counter", fontSize = 24.sp)

            // Laatikko FloatingActionButtonille oikeaan alakulmaan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        displayContacts = true
                        displayErrorMessage = true
                        navController.navigate("contactsScreen") //navigoidaan kontakti-ikkunaan
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Chat",
                        tint = Color.White
                    )
                }

                // Näyttää kontaktit kun FAB painettu
                if (displayContacts && contactViewModel.contacts.isNotEmpty()) {
                    Column {
                        // Kontaktien listaus
                        contactViewModel.contacts.forEach { contact ->
                            Text(text = "${contact.first}: ${contact.second}")
                        }
                    }
                }
            }
        }
    }

@Composable
fun SettingsScreen(onBack : () -> Unit) {
    Text(
        text = "Settinkejä tänne",
        fontSize = 24.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    Button(onClick = { onBack() }) {
        Text(text = "Takaisin")
    }
}


@Composable
fun ContactsScreen(
    contacts: List<Pair<String, String>>,
    onBack: () -> Unit // Callback mennäksemme takaisin edeltävään ikkunaan
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Contacts",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        contacts.forEach { contact ->
            Text(text = "${contact.first}: ${contact.second}")
        }
        Button(onClick = { onBack() }) {
            Text(text = "Takaisin")
        }
    }
}

    @Composable
    fun DisplayErrorDialog(errorMessage: String, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Error") },
            text = { Text(text = errorMessage) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text(text = "OK")
                }
            }
        )
    }

    @Composable
    fun MyAppBar(title: String,
                 onIconClick: () -> Unit,
                 navController: NavHostController
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f) // Take up available space
                )
                IconButton(
                    onClick = { onIconClick()
                              navController.navigate("SettingsScreen")
                              },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Icon",
                        tint = Color.White
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun MyAppPreview() {
        val myCallback = ActivityResultCallback<Any?> {}
        val contactViewModel = ContactViewModel()
        val navController = rememberNavController()
        MyAppContent(requestPermissionLauncher = DummyLauncher(myCallback),
            contactViewModel = ContactViewModel(),
            navController = rememberNavController())
    }

    @Preview
    @Composable
    fun MyAppBarPreview() {
        MyAppBar(title = "ChatApp", onIconClick = {}, navController = rememberNavController())
    }