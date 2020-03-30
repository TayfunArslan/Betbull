package org.arslan.samples.forbetbull

import android.os.AsyncTask
import android.os.Bundle
import android.os.Process
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.arslan.samples.forbetbull.global.g_userInfoService
import org.arslan.samples.forbetbull.model.UserInfo
import org.arslan.samples.forbetbull.model.UserInfoList
import org.csystem.samples.forbetbull.R
import org.java_websocket.client.*
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.Socket
import java.net.URI
import javax.websocket.WebSocketClient


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

    private fun initWebSocketConnection() {
        GetSocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        //runOnUiThread(GetSocketThread())
        //GetSocketThread().start()
    }

    private fun updateUI(s: String) {
        if(!::mAdapter.isInitialized || s.isBlank())
            return

        if (s == "LOGIN") {
            mainActivityToolbarActiveUser.title =
                applicationContext.resources.getString(R.string.ACTIVE_USER) + s
            return
        } else if (s == "LOGOUT") {
            mainActivityToolbarActiveUser.title = "Logged out"
            return
        }

        var id = getIdFromString(s)
        if(id == -1)
            return

        var name = getStringFromString(s)

        mAdapter.getItem(id)!!.username = name
        mAdapter.notifyDataSetChanged()
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

//    private inner class GetSocketThread: Thread() {
//        override fun run() {
//            var text = ""
//
//            try {
//                var uri = URI.create("wss://websocket.org/echo.html")
//
//                var webSocketClient = object: WebSocketClient(uri) {
//                    override fun onOpen(handshakedata: ServerHandshake?) {
//                        text = "opened?"
//                    }
//
//                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
//                        text = "close $reason"
//                    }
//
//                    override fun onMessage(message: String?) {
//                        text = message!!
//                    }
//
//                    override fun onError(ex: Exception?) {
//                        text = ex.toString()
//                    }
//                }
//
//                webSocketClient.connect()
//
////
////                mSocket = Socket(
////                    "ws://echo.websocket.org",
////                    80
////                ) // TODO WebSocket olacak javax.websocket
////                mSocket.connect(mSocket.localSocketAddress)
////
////                val br = BufferedReader(InputStreamReader(mSocket.getInputStream()))
////                text = br.readLine().trim()
//            } catch (ex: Throwable) {
//                text = ex.message!!
////                Toast.makeText(this@MainActivity, ex.message, Toast.LENGTH_LONG).show()
//            }
//
//            updateUI(text)
//        }
//
//    }

    private inner class GetSocketTask : AsyncTask<Unit, String, String>() {
        override fun doInBackground(vararg p0: Unit?): String {
            var text = ""

            try {

                var uri = URI.create("wss://websocket.org/echo.html")

                var webSocketClient = object: org.java_websocket.client.WebSocketClient(uri) {
                    override fun onOpen(handshakedata: ServerHandshake?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onClose(code: Int, reason: String?, remote: Boolean) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onMessage(message: String?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onError(ex: Exception?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                }

               webSocketClient.connect()

                return text
            } catch (ex: Throwable) {
                return ex.message!!
            }
//                mSocket = Socket(
//                    "https://www.websocket.org/echo.html",
//                    8080
//                ) // TODO WebSocket olacak javax.websocket
//                mSocket.connect(mSocket.localSocketAddress)
//
//                val br = BufferedReader(InputStreamReader(mSocket.getInputStream()))
//                text = br.readLine().trim()
//            } catch (ex: Throwable) {
//                return ex.message!!
//            }
//
//            return text
        }

        override fun onProgressUpdate(vararg values: String?) {
            Toast.makeText(this@MainActivity, values[0], Toast.LENGTH_LONG).show()
        }

        override fun onPostExecute(result: String?) {
            updateUI(result!!)
        }
    }
}