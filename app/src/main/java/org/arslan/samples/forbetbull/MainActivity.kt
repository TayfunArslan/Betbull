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
import org.csystem.samples.forbetbull.R
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.Socket
import java.net.URI

var counter = 0

class MainActivity : AppCompatActivity() {
    lateinit var mAdapter: ArrayAdapter<UserInfo>
    lateinit var mSocket: Socket

    private inner class GetUsersTask : AsyncTask<Unit, String, UserInfoList>() {
        override fun onProgressUpdate(vararg values: String?) {
            Toast.makeText(this@MainActivity, values[0], Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg p0: Unit?): UserInfoList {
            Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND + THREAD_PRIORITY_MORE_FAVORABLE)

            try {
                val call = g_userInfoService.getUsers()
                val response = call.execute()

                return response.body()!!
            } catch (ex: Throwable) {
                publishProgress(ex.message)
            }

            return UserInfoList(ArrayList())
        }

        override fun onPostExecute(result: UserInfoList) {
            mAdapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_list_item_1,
                result.userInfoList
            )
            mainActivityListViewUsers.adapter = mAdapter

            super.onPostExecute(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        initViews()
        initWebSocketConnection()
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
        GetUsersTask().execute()
    }

    fun onSendButtonClicked(view: View) {
        EchoServerTask().execute(mainActivityTextViewRequest.text.toString())
    }

    private fun initWebSocketConnection() {
        //GetSocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        //runOnUiThread(GetSocketThread())
        EchoServerTask()
    }

    private fun updateUI(s: String) {
        if (!::mAdapter.isInitialized || s.isBlank())
            return

        //TODO Test amaçlı
        mainActivityToolbarActiveUser.title = s.length.toString()

        if (s == "LOGIN") {
            mainActivityToolbarActiveUser.title =
                applicationContext.resources.getString(R.string.ACTIVE_USER)
            return
        } else if (s == "LOGOUT") {
            mainActivityToolbarActiveUser.title = "Logged out"
            return
        }

        var id = if (getIdFromString(s) == -1) getIdFromString(s) else return
        var name = getStringFromString(s)

        mAdapter.getItem(id)!!.username = name
        mAdapter.notifyDataSetChanged()

        return
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

    private fun getStringFromString(s: String) = s.substringAfter('-').trim()

    private inner class EchoServerTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg p0: String?): String {
            var text = ""

            try {
//                Socket("ws://ws.achex.ca",80).use {
//                    val br = BufferedReader(InputStreamReader(it.getInputStream()))
//                    val bw = BufferedWriter(OutputStreamWriter(it.getOutputStream()))
//
//                    bw.write("${p0[0]!!}\r\n")
//                    bw.flush()
//
//                    text = br.readLine().trim()

                var uri = URI.create("wss://echo.websocket.org")

                var webSocketClient = object: WebSocketClient(uri) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        text = "handshakedata.toString()"
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        text = "close $reason"
                    }

                    override fun onMessage(message: String?) {
                        text = message!!
                    }

                    override fun onError(ex: Exception?) {
                        text = ex.toString()
                    }
                }

                webSocketClient.connect()

                return text
            } catch (ex: Throwable) {
                return ex.message!!
            }
        }

        override fun onProgressUpdate(vararg values: String?) {
            Toast.makeText(this@MainActivity, values[0], Toast.LENGTH_LONG).show()
        }

        override fun onPostExecute(result: String?) {
            updateUI(result!!)
        }
    }
}