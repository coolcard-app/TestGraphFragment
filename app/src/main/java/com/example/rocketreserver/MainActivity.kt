package com.example.rocketreserver

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.example.rocketreserver.fragment.LaunchConnectionGraphFrag
import com.example.rocketreserver.fragment.LaunchGraphFrag
import com.example.rocketreserver.fragment.MissionGraphFrag
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = ApolloClient.builder()
            .serverUrl("https://apollo-fullstack-tutorial.herokuapp.com")
            .build()

        lifecycleScope.launch {
            val responseA = client.query(LaunchListAQuery()).await()
            val responseB = client.query(LaunchListBQuery()).await()

            /**
             * 在 graphql 使用 fragment 後，雖然兩個 query 查詢的內容一樣，
             * 但 [LaunchListAQuery.Data] 與 [LaunchListBQuery.Data] 型別判斷仍然不一樣，
             * 必須再尋找其子類別
             * [LaunchListAQuery.Data.launches.fragments.launchConnectionGraphFrag]
             * [LaunchListBQuery.Data.launches.fragments.launchConnectionGraphFrag]
             * 方可得到共用的 data [LaunchConnectionGraphFrag]
             * */
            val launchListDataA = responseA.data
            val launchListA = toLaunchList(
                launchListDataA?.launches?.fragments?.launchConnectionGraphFrag
            )
            printLaunchList(launchListA)
            val launchListDataB = responseB.data
            val launchListB = toLaunchList(
                launchListDataB?.launches?.fragments?.launchConnectionGraphFrag
            )
            printLaunchList(launchListB)
        }
    }

    private fun toLaunchList(frag: LaunchConnectionGraphFrag?): List<LaunchGraphFrag?>? {
        return frag?.launches?.map { it?.fragments?.launchGraphFrag }
    }

    private fun printLaunchList(list: List<LaunchGraphFrag?>?) {
        list?.forEach { printLaunch(it) }
    }

    private fun printLaunch(launch: LaunchGraphFrag?) {
        val id = launch?.id
        printMission(launch?.mission?.fragments?.missionGraphFrag)
        val site = launch?.site
        Log.d("Launch", "id:${id}, site:${site}")
    }

    private fun printMission(mission: MissionGraphFrag?) {
        val name = mission?.name
        Log.d("Mission", "name:${name}")
    }
}
