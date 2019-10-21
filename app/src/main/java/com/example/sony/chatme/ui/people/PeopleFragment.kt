package com.example.sony.chatme.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sony.chatme.AppConstants
import com.example.sony.chatme.R
import com.example.sony.chatme.recyclerview.item.ChatChannelItem
import com.example.sony.chatme.recyclerview.item.PersonItem
import com.example.sony.chatme.ui.chat.ChatActivity
import com.example.sony.chatme.util.FirebaseUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_people.*
import org.jetbrains.anko.support.v4.startActivity

class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PoepleViewModel


        private lateinit var userListenerRegistration: ListenerRegistration
    private lateinit var chatListenerRegistration: ListenerRegistration
    private var shouldInitPersonRecuclerView = true
    private var shouldInitChatRecuclerView = true
        private lateinit var peopleSection: Section
    private lateinit var chatSection: Section

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        peopleViewModel =
            ViewModelProviders.of(this).get(PoepleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_people, container, false)

        userListenerRegistration =
            FirebaseUtil.addUsersListener(this.activity!!, this::updateRecycleViewPerson)
        chatListenerRegistration =
            FirebaseUtil.addChatChannelsListener(this.activity!!, this::updateRecycleViewChat)


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirebaseUtil.removeListener(userListenerRegistration)
        FirebaseUtil.removeListener(chatListenerRegistration)
        shouldInitPersonRecuclerView = true
        shouldInitChatRecuclerView = true
    }

    private fun updateRecycleViewPerson(items: List<Item>) {


        fun init(){
            recycler_view_people.apply {
                layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {

                    peopleSection = Section(items)
                    add(peopleSection)
                    setOnItemClickListener(onItemClicked)
                }
            }
            shouldInitPersonRecuclerView = false

        }

        fun updateItems() = peopleSection.update(items)



        if (shouldInitPersonRecuclerView)
            init()
        else
            updateItems()


    }

    private fun updateRecycleViewChat(items: List<Item>) {


        fun init() {
            recycler_view_chat_channels.apply {
                layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {

                    chatSection = Section(items)
                    add(chatSection)
                    setOnItemClickListener(onItemClicked)
                }
            }
            shouldInitChatRecuclerView = false

        }

        fun updateItems() = chatSection.update(items)



        if (shouldInitChatRecuclerView)
            init()
        else
            updateItems()


    }

    private val onItemClicked = OnItemClickListener{item,view ->

        if (item is PersonItem) {

            startActivity<ChatActivity>(
                AppConstants.USER_ID to item.userId,
                AppConstants.USER_NAME to item.person.userName
            )


        }
        if (item is ChatChannelItem) {

            startActivity<ChatActivity>(
                AppConstants.USER_ID to item.userId,
                AppConstants.USER_NAME to item.person.userName)


        }


    }
}