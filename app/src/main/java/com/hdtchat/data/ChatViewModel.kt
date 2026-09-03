/*
package com.hdtchat.data

import android.app.Application
import androidx.lifecycle.*
import com.hdtchat.ChatRepository
import com.hdtchat.Message
import com.hdtchat.data.ChatMessage
import com.hdtchat.wifip2p.ChatManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.logging.Handler

class ChatViewModel(application: Application, private val chatManager: ChatManager) : AndroidViewModel(application) {

    private val chatRepository: ChatRepository
    private var userDao: Dao

    //val chatViewModel = ViewModelProvider(this, ChatViewModelFactory(application, chatManager)).get(ChatViewModel::class.java)

    init {
        userDao = Userdatabase.getDatabase(application).dao()
        chatRepository = ChatRepository(userDao)
    }

    //Here the two variables sender & receiver of the type MutableLiveData<String>().
    //MutableLiveData is a class in Android that is used to hold data that can be observed
    //and updated by multiple components in a android app.
    private val sender = MutableLiveData<String>()
    private val receiver = MutableLiveData<String>()

    //The setChatUsers takes two string parameters thar are defined in the code above.
    //By setting the values of the sender & receiver in this function, other components of the app
    //that are observing these properties can be updated with the current chat users and take appropriate actions.
    //Suck as, updating the UI to display the current chats or fetching the chat history between these users.
    //This allows for a dynamic and responsive user experience in the chat feature of the app.
    fun setChatUsers(sender: String, receiver: String) {
        this.sender.value = sender
        this.receiver.value = receiver
    }


    fun insertMessage(message: ChatMessage) {
        viewModelScope.launch {
            chatRepository.insertMessage(message)
        }
    }


    //The funksjon sendmessage sends a message from sender to receiver. Its adds the specified
    //message to the database and sends it to the chatManager for further processing.
    fun sendMessage(sender: String, receiver: String, message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            //In this coroutine each message gets stored in the chatMessage object and gets a unique ID,
            // with the sender, receiver and message stored.
            val chatMessage = ChatMessage(
                UUID.randomUUID().toString(), // generate a unique ID
                sender,
                receiver,
                message
            )
            //This line saves the chatMessage object in the database.
            chatRepository.insertMessage(chatMessage)
            //This  line sends the message to the chatManager using write method.
            chatManager?.write("$sender:$receiver:$message")
        }
    }

    //This function returns a LiveData object that contains a list of ChatMessage object.
    fun getMessagesForChat(): LiveData<List<ChatMessage>> {
        //The function uses Transformations.switchMap() dynamic in order to retrieve the messages from
        //the chatRepository based on the sender and reciever of the message.
        //The switchMap helps to observe a LiveData and on an update event passes the value to a map function
        //And returns another which may depend on the value passed.
        return Transformations.switchMap(sender) { s ->
            Transformations.switchMap(receiver) { r ->
                //The getMessages function retrieves a list of chatMessage objects from the chatRepository
                //based on the sender and receiver of the message.
                chatRepository.getMessages(s, r)
            }
        }
    }

}

 */