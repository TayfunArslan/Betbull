package org.arslan.samples.forbetbull

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.arslan.samples.forbetbull.global.g_userInfoService
import org.arslan.samples.forbetbull.model.UserInfo
import org.arslan.samples.forbetbull.retrofitCallback.enqueue
import org.csystem.samples.forbetbull.R
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class MainActivity : AppCompatActivity() {
    private lateinit var mAdapter: ArrayAdapter<UserInfo>
    private var mUserList = ArrayList<UserInfo>()
    private lateinit var mWebSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        initViews()
        //....
    }

    private fun initViews() {
        initToolbar()
        initListViewUsers()
        //....
    }

    private fun initToolbar() {
        mainActivityToolbarActiveUser.title =
            applicationContext.resources.getString(R.string.ACTIVE_USER)
    }

    private fun initListViewUsers() {
        g_userInfoService.getUsers().enqueue {
            onResponse = {
                try {
                    mUserList = ArrayList(it.body()!!.userInfoList)

                    mAdapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        mUserList
                    )
                    mainActivityListViewUsers.adapter = mAdapter
                } catch (ex: Throwable) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, ex!!.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
            onFailure = {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, it!!.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun onSendButtonClicked(view: View) {
        if (!mainActivityTextViewRequest.text.toString().isBlank())
            SocketTask().run()
    }

    fun onExitButtonClicked(view: View) {
        finish()
    }

    private fun updateUIWithResponse(s: String) {
        try {
            when(s.toUpperCase().trim()) {
                "LOGIN" -> {
                    mainActivityToolbarActiveUser.title =
                        applicationContext.resources.getString(R.string.ACTIVE_USER)
                    return
                }
                "LOGOUT" -> {
                    mainActivityToolbarActiveUser.title = "Logged out"
                    return
                }
            }

            updateListView(s)
        } catch (ex: Throwable) {
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        } finally {
            mainActivityTextViewRequest.text.clear()
        }
    }

    private fun updateListView(s: String) {
        if(s.split('-').count() != 2)
            throw UnsupportedOperationException("Format error!")

        var id = getIdFromResponse(s)
        var newName = getStringFromResponse(s)

        if (id == -1 || newName.isNullOrBlank())
            throw UnsupportedOperationException("Format error!")

        var oldName = getNameWithId(id)
        var userInfo = UserInfo(id, oldName)

        var position = mAdapter.getPosition(userInfo)

        mAdapter.getItem(position)!!.username = newName
        mAdapter.notifyDataSetChanged()
    }

    private fun getIdFromResponse(s: String) =
        if (s.contains('-')) s.split("-").first().trim().toInt() else -1

    private fun getStringFromResponse(s: String) =
        if (s.contains('-')) s.trim().split("-").last().trim() else null

    private fun getNameWithId(id: Int) = mUserList.first { it.userId == id }.username

    override fun onDestroy() {
        mWebSocketClient.close()
        super.onDestroy()
    }

    private inner class SocketTask : Thread() {
        override fun run() {
            try {
                val uri: URI = URI.create(resources.getString(R.string.SOCKET_URI))

                mWebSocketClient = object : WebSocketClient(uri) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        send(mainActivityTextViewRequest.text.toString())
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "$code $reason", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onMessage(message: String?) {
                        runOnUiThread {
                            updateUIWithResponse(message!!)
                        }
                    }

                    override fun onError(ex: Exception?) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, ex!!.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }

                mWebSocketClient.connect()
            } catch (ex: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, ex.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}