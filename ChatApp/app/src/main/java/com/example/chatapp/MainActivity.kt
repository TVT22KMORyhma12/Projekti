package com.example.chatapp


import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


// Dummylauncher alkuun kysyttäessä kontaktien jakamisen oikeuksia


// Kontaktien näyttömalli
class ContactViewModel {
    private val _contacts = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val contacts: List<Pair<String, String>>
        get() = _contacts.value

    private val _contactsEmptyMessage = mutableStateOf<String?>(null)
    val contactsEmptyMessage: String?
        get() = _contactsEmptyMessage.value

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
    fun setContacts(contactsList: List<Pair<String, String>>) {
        _contacts.value = contactsList
    }
    fun setContactsEmptyMessage(message: String?) {
        _contactsEmptyMessage.value = message
    }
}

class MainActivity : AppCompatActivity() {

    private val contactViewModel = ContactViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "mainScreen") {
                composable("mainScreen") {
                    MyAppContent(contactViewModel, navController)
                }
                composable("contactsScreen") {
                    ContactsScreen(
                        contacts = contactViewModel.contacts,
                        onContactClick = { contactName -> },
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
    }
}

    // Composablejen alku
    @Composable
    fun MyAppContent(
        contactViewModel: ContactViewModel,
        navController: NavHostController
    ) {
        var counter by remember { mutableStateOf(0) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
        ) {
            MyAppBar(title = "ChatApp", onIconClick = { navController.navigate("SettingsScreen") })
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Counter: $counter", fontSize = 24.sp)

            Spacer(
                modifier = Modifier.weight(1f))

            Box (
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate("contactsScreen") },
                    modifier = Modifier.size(56.dp),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 16.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Contacts",
                        tint = Color.Red
                    )
                }
            }
        }
    }

@Composable
fun SettingsScreen(onBack : () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
    ) {
        Text(
            text = "Settinkejä tänne",
            fontSize = 24.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Salli ilmoitukset",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )
        }
        //Muut settinkit tänne
        Button(
            onClick = { onBack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Takaisin")
        }
    }
}


@Composable
fun ContactsScreen(
    contacts: List<Pair<String, String>>,
    onContactClick: (String) -> Unit,
    onBack: () -> Unit // Callback mennäksemme takaisin edeltävään ikkunaan
) {
    var selectedContact by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (selectedContact != null) {
        ContactDetails(
            contact = selectedContact!!,
            onDeleteContact = {
                //funktio deletoidaksemme kontaktin
                selectedContact = null
            },
            onBack = { selectedContact = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                items(contacts) { contact ->
                    ContactItem(contact = contact, onContactClick = { selectedContact = contact })
                    Divider()
                }
            }
            Button(
                onClick = { onBack() },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Takaisin")
            }
        }
    }
}
    @Composable
    fun ContactItem(
        contact: Pair<String, String>,
        onContactClick: (String) -> Unit
    ) {
        Text(
            text = "${contact.first}: ${contact.second}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier
                .clickable { onContactClick(contact.first) }
                .padding(16.dp)
        )
    }
@Composable
fun ContactDetails(
    contact: Pair<String, String>,
    onDeleteContact: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Text(
            text = "Contact Details",
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "${contact.first}: ${contact.second}",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = { onDeleteContact() },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Delete Contact")
        }
        Button(
            onClick = { onBack() },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Back")
        }
    }
}

    @Composable
    fun MyAppBar(
        title: String,
        onIconClick: () -> Unit,
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
                    onClick = onIconClick,
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
        val contactViewModel = ContactViewModel()
        val navController = rememberNavController()
        MyAppContent(contactViewModel = ContactViewModel(), navController = rememberNavController())
    }

    @Preview
    @Composable
    fun MyAppBarPreview() {
        MyAppBar(title = "ChatApp", onIconClick = {})
    }