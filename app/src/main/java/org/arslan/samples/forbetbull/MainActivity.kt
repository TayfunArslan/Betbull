package org.arslan.samples.forbetbull

import android.os.*
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.arslan.samples.forbetbull.global.g_userInfoService
import org.arslan.samples.forbetbull.model.UserInfo
import org.arslan.samples.forbetbull.model.UserInfoList
import org.arslan.samples.forbetbull.retrofit.enqueue
import org.csystem.samples.forbetbull.R
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

class MainActivity : AppCompatActivity() {
    lateinit var mAdapter: ArrayAdapter<UserInfo>
    var mUserList = ArrayList<UserInfo>()
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
        SocketTask().run()
    }

    fun onExitButtonClicked(view: View) {
        finish()
    }

    private fun updateUI(s: String) {
        if (!::mAdapter.isInitialized || s.isBlank())
            return

        try {
            if (s.toUpperCase() == "LOGIN") {
                mainActivityToolbarActiveUser.title =
                    applicationContext.resources.getString(R.string.ACTIVE_USER)
                return
            } else if (s.toUpperCase() == "LOGOUT") {
                mainActivityToolbarActiveUser.title = "Logged out"
                return
            }

            var id = getIdFromString(s)
            var newName = getStringFromString(s)

            if (id == -1 || newName.isNullOrBlank())
                throw Exception("Format error!")

            var oldName = getNameWithId(id)
            var userInfo = UserInfo(id, oldName)

            var position = mAdapter.getPosition(userInfo)

            mAdapter.getItem(position)!!.username = newName
            mAdapter.notifyDataSetChanged()
        } catch (ex: Throwable) {
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        } finally {
            mainActivityTextViewRequest.text.clear()
        }
    }

    private fun getIdFromString(s: String): Int { // ex: Input = 12 - Tayfun, output = 12
        var retVal = ""

        for (value in s) {
            if (value.isDigit())
                retVal += value
            else
                break
        }

        return if (!retVal.isBlank()) retVal.toInt() else -1
    }

    private fun getStringFromString(s: String) =
        if (s.contains("-")) s.substringAfter('-').trim() else null

    private fun getNameWithId(id: Int) = mUserList.filter { it.userId == id }[0].username

    override fun onDestroy() {
        mWebSocketClient.close()
        super.onDestroy()
    }

    private inner class SocketTask : Thread() {
        override fun run() {
            try {
                val uri: URI = URI.create("wss://echo.websocket.org")

                mWebSocketClient = object : WebSocketClient(uri) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        send(mainActivityTextViewRequest.text.toString())
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, reason, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onMessage(message: String?) {
                        runOnUiThread {
                            updateUI(message!!)
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