package org.arslan.samples.forbetbull

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.arslan.samples.forbetbull.global.g_userInfoService
import org.arslan.samples.forbetbull.model.UserInfo
import org.arslan.samples.forbetbull.model.UserInfoList
import org.csystem.samples.forbetbull.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class MainActivity : AppCompatActivity() {
    lateinit var mAdapter: ArrayAdapter<UserInfo>
    lateinit var mSocket: Socket

    private inner class GetUsersTask : AsyncTask<Unit, String, UserInfoList>() {
        override fun onProgressUpdate(vararg values: String?) {
            Toast.makeText(this@MainActivity, values[0], Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg p0: Unit?): UserInfoList {
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
        mainActivityToolbarActiveUser.title = applicationContext.resources.getString(R.string.ACTIVE_USER)
    }

    private fun initListViewUsers() {
        GetUsersTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun initWebSocketConnection() {
        GetSocketTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun updateUI(s: String) {
        if(s == "LOGIN") {
            mainActivityToolbarActiveUser.title = applicationContext.resources.getString(R.string.ACTIVE_USER)
            return
        } else if(s == "LOGOUT") {
            mainActivityToolbarActiveUser.title = "Logged out"
            return
        }

        var id = getIdFromString(s)
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

        return retVal.toInt()
    }

    private fun getStringFromString(s: String) = s.substringAfter('-').trim()

    private inner class GetSocketTask : AsyncTask<Unit, String, String>() {
        override fun doInBackground(vararg p0: Unit?): String {
            var text = ""

            try {
                mSocket = Socket(
                    "https://www.websocket.org/echo.html",
                    8080
                ) // TODO WebSocket olacak javax.websocket
                mSocket.connect(mSocket.localSocketAddress)

                val br = BufferedReader(InputStreamReader(mSocket.getInputStream()))
                text = br.readLine().trim()
            } catch (ex: Throwable) {
                return ex.message!!
            }

            return text
        }

        override fun onProgressUpdate(vararg values: String?) {
            Toast.makeText(this@MainActivity, values[0], Toast.LENGTH_LONG).show()
        }

        override fun onPostExecute(result: String?) {
            updateUI(result!!)
        }
    }

    fun onClickTest(view: View) {
        Toast.makeText(this, mSocket.isConnected.toString(), Toast.LENGTH_LONG).show()
    }
}