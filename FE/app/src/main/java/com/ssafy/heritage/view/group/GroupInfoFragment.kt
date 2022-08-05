package com.ssafy.heritage.view.group

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayout
import com.ssafy.heritage.ApplicationClass
import com.ssafy.heritage.R
import com.ssafy.heritage.base.BaseFragment
import com.ssafy.heritage.data.dto.User
import com.ssafy.heritage.databinding.FragmentGroupInfoBinding
import com.ssafy.heritage.view.HomeFragment
import com.ssafy.heritage.view.ar.ARFragment
import com.ssafy.heritage.viewmodel.GroupViewModel
import com.ssafy.heritage.viewmodel.UserViewModel
import kotlin.properties.Delegates

private const val TAG = "GroupInfoFragment___"

class GroupInfoFragment : BaseFragment<FragmentGroupInfoBinding>(R.layout.fragment_group_info) {

    private val args by navArgs<GroupInfoFragmentArgs>()
    private val groupViewModel by activityViewModels<GroupViewModel>()

    private lateinit var detailFragment : GroupDetailFragment
    private lateinit var chatFragment: GroupChatFragment
    private lateinit var calenderFragment: GroupCalenderFragment
    private lateinit var mapFragment: GroupMapFragment

    override fun init() {

        groupViewModel.getGroupList()
        groupViewModel.add(args.groupInfo)
        initAdapter()
        initClickListener()
    }

    private fun initClickListener()= with(binding) {
        btnBack.setOnClickListener{
            findNavController().popBackStack()
        }
    }
    private fun initAdapter() {
        binding.apply {

            // 각 탭에 들어갈 프래그먼트 객체화
            detailFragment = GroupDetailFragment()
            chatFragment = GroupChatFragment()
            calenderFragment = GroupCalenderFragment()
            mapFragment = GroupMapFragment()

            childFragmentManager.beginTransaction()
                .replace(R.id.frame_layout_tab, detailFragment).commit()

            // 탭 클릭 시 이벤트
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab!!.position) {
                        0 -> replaceView(detailFragment)
                        1 -> replaceView(chatFragment)
                        2 -> replaceView(calenderFragment)
                        3 -> replaceView(mapFragment)
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }



    private fun replaceView(tab: Fragment) {
        // 탭한 화면 변경
        var selectedFragment: Fragment?=null
        selectedFragment = tab
        selectedFragment?.let {
            childFragmentManager?.beginTransaction()?.replace(R.id.frame_layout_tab, it)?.commit()
        }
    }
}