package com.example.dyplommessenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.example.dyplommessenger.R
import com.example.dyplommessenger.models.ChatMessage
import com.example.dyplommessenger.models.User
import com.example.dyplommessenger.views.ChatFromItem
import com.example.dyplommessenger.views.ChatToItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.sql.Timestamp

class ChatLogActivity : AppCompatActivity() {
    companion object {
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chat_log.adapter = adapter  // allow to show messages in chat

//        val username = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)   //
        supportActionBar?.title = toUser?.username                                   // can get anyone params from model User

        listenForMessages()

        send_button_chal_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message..")
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val id_from = FirebaseAuth.getInstance().uid
        val id_to = toUser?.uid

        if (id_from == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$id_from/$id_to")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.id_from == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser

                        adapter.add(ChatToItem(chatMessage.text, currentUser!!))
                    } else {
                        adapter.add(ChatFromItem(chatMessage.text, toUser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    private fun performSendMessage() {                                                              // how do we actually send messages to the Firebase
        val text = edittext_chat_log.text.toString()

        val id_from = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val id_to = user.uid

        if (id_from == null) return

//        val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$id_from/$id_to").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$id_to/$id_from").push()

        val chatMessage =
            ChatMessage(reference.key!!, text, id_from, id_to, System.currentTimeMillis())
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$id_from/$id_to")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$id_to/$id_from")
        latestMessageToRef.setValue(chatMessage)
    }
}