/*package com.hdtchat.wifip2p

import android.app.Application
import android.net.wifi.p2p.WifiP2pManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hdtchat.data.Dao
import com.hdtchat.data.UserRepository
import com.hdtchat.data.Userdatabase
import com.hdtchat.wifip2p.chat.Comms
import com.hdtchat.wifip2p.excludedStuff.CommsApi
import com.hdtchat.wifip2p.excludedStuff.CommsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class TestViewModel(application: Application): AndroidViewModel(application) {
   //  private val peersRepository: PeersRepository
    // private val status: String
    //private var manager: WifiP2pManager?
    //private val peersApi: PeersApi
    //private val service = Peers()
    private val repository: CommsRepository
    private var msgComms: Comms
    init {
        msgComms = CommsApi.serverClient(application).msgComms()
        repository = UserRepository(userDao)
    }

    val _peerFlow = MutableStateFlow(Peers())
    val peerFlow: StateFlow<Peers> = _peerFlow.asStateFlow()
   // val status: String = ""s
   fun startCommunication(email: String, password: String): LiveData<Boolean> {

       viewModelScope.launch(Dispatchers.IO) {
           val result = repository.verifyUser(email, password)
           resultLiveData.postValue(result)
       }
    fun startReceiver(){

     */
   //     peersRepository.startUpReceiver()
        //onResume, når viewmodelen / aktiviteten er aktiv så startes en broadcastreceiver
        /*
        viewModelScope.launch(Dispatchers.IO) {
            //Flowen er ment til å returnere status tekst, hvis det trengs
            peersRepository.startUpReceiver()
        }


    }
        /*
        //onResume, når viewmodelen / aktiviteten er aktiv så startes en broadcastreceiver
        viewModelScope.launch(Dispatchers.IO) {
            //Flowen er ment til å returnere status tekst, hvis det trengs
            peersRepository.startUpReceiver().collect {
                print(service)
            }
        }
        //Initialiserer wifip2p, setter opp listener og ser etter peers
        viewModelScope.launch(Dispatchers.IO) {
            //Flowen er ment å returnere peers etterhvert som de oppdages
            peersRepository.findPeers().collect {
                print(service)
            }
        }
        //Kobler opp til peer
        viewModelScope.launch(Dispatchers.IO) {
            //Flowen er ment å returnere resultat av oppkobling
            peersRepository.connectToPeer(service).collect {
                print(service)
            }
        }
        //Kobler opp til peer
        viewModelScope.launch(Dispatchers.IO) {
            //Flowen er ment å returnere resultat av oppkobling
            peersRepository.disconnect().collect {
                print(status)
            }
        }

         */


            //private val peersRepository: PeersRepository()
    //peersRepository.registrationAndDiscovery()

  //  val peerFlow: StateFlow<Peers> = _peerFlow.asStateFlow()


}

         */
