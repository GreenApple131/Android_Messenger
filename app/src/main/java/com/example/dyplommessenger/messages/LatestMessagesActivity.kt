package com.example.dyplommessenger.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.dyplommessenger.R
import com.example.dyplommessenger.end_to_end_AES.SignalUser
import com.example.dyplommessenger.end_to_end_AES.SignalWrapper
import com.example.dyplommessenger.models.ChatMessage
import com.example.dyplommessenger.models.User
import com.example.dyplommessenger.registerlogin.RegisterActivity
import com.example.dyplommessenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    private val signalWrapper: SignalWrapper = SignalWrapper()
    private var outputTextView: TextView? = null

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)

            //we are missing the chat partner user
            val row = item as LatestMessageRow

            intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }


        listenForLatestMessages()

        fetchCurrentUser()

        verifyUserIsLoggedIn()

        outputTextView = findViewById(R.id.content) as TextView

        try {
            startSignalPoc()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val latestMessagesMap = HashMap<String, ChatMessage>()


    @Throws(java.lang.Exception::class)
    private fun startSignalPoc() {
        val alice: SignalUser = signalWrapper.register("alice")
        outputTextView!!.append("\nalice registered!")
        val bob: SignalUser = signalWrapper.register("bob")
        outputTextView!!.append("\nbob registered!")
        signalWrapper.initSession(alice, bob)
        val aliceSc = signalWrapper.createSessionCipher(alice, bob)
        var ciphertext = signalWrapper.encrypt(aliceSc, "Hi Bob, this is Alice! How are you?")
        outputTextView!!.append("Encrypted => ${ciphertext.serialize()}".trimIndent())
        val bobSc = signalWrapper.createSessionCipher(bob, alice)
        var plaintext = signalWrapper.decrypt(bobSc, ciphertext)
        outputTextView!!.append("\nDecrypted => $plaintext")
        signalWrapper.initSession(bob, alice)
        ciphertext = signalWrapper.encrypt(bobSc, "BOB: Hi Alice, this is Bob! I'm fine!")
        outputTextView!!.append("Encrypted => ${ciphertext.serialize()}".trimIndent())
        plaintext = signalWrapper.decrypt(aliceSc, ciphertext)
        outputTextView!!.append("\nDecrypted => $plaintext")
    }


    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val id_from = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$id_from")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return   // or !! after using val
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?: return   // or !! after using val
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current User: ${currentUser?.username}")
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

}
