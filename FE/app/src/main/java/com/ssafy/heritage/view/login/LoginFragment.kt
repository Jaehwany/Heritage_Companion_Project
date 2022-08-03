package com.ssafy.heritage.view.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.ssafy.heritage.R
import com.ssafy.heritage.base.BaseFragment
import com.ssafy.heritage.databinding.FragmentLoginBinding
import com.ssafy.heritage.util.JWTUtils
import com.ssafy.heritage.util.SharedPreferencesUtil
import com.ssafy.heritage.view.HomeActivity
import com.ssafy.heritage.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private const val TAG = "LoginFragment___"

class LoginFragment : BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {

    private val loginViewModel by viewModels<LoginViewModel>()

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun init() = with(binding) {
        loginVM = loginViewModel

        initObserve()

        initView()

        initClickListener()
    }

    private fun initObserve() {
        loginViewModel.message.observe(viewLifecycleOwner) {

            // viewModel에서 Toast메시지 Event 발생시
            it.getContentIfNotHandled()?.let {
                makeToast(it)
            }
        }
    }

    private fun initView() {

        // 구글 로그인 객체 구성
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // 기존에 로그인 했던 계정을 확인한다.
        val gsa = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (gsa != null) {
            // 로그인 성공입니다.
            Log.d(TAG, "initView: ${gsa.email}")
        }
    }

    private fun initClickListener() = with(binding) {

        // 회원가입 버튼 클릭
        btnJoin.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.action_loginFragment_to_joinFragment)
        }

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener {

            CoroutineScope(Dispatchers.Main).launch {

                // 로그인 성공했을 경우
                val token = loginViewModel.login() as String

                if (token != null) {
                    // 홈 화면으로 이동

                    SharedPreferencesUtil(requireContext()).saveToken(token)

                    val user = JWTUtils.decoded(token)
                    Log.d(TAG, "initClickListener: $user")
                    if (user != null) {
                        Intent(requireContext(), HomeActivity::class.java).apply {
                            startActivity(this)
                        }
                    } else {
                        makeToast("유저 정보 획득에 실패하였습니다")
                    }
                }

            }

        }

        // 소셜 로그인 버튼 클릭
        btnLoginSocial.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        signInClientLauncher.launch(signInIntent)
    }

    private val signInClientLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 결과를 받으면 처리할 부분
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignInResult(task)

        }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

            if (account.email != null) {

                Log.d(TAG, "handleSignInResult: ${account.email}")

                CoroutineScope(Dispatchers.Main).launch {

                    val result = loginViewModel.socialCheckId(account.email.toString())

                    // id 중복이 아닌 경우
                    if (result == "signup") {

                        // 회원가입창으로 이동
                        val bundle = Bundle().apply {
                            putString("type", "social")
                            putString("id", account.email)
                        }

                        findNavController().navigate(
                            R.id.action_loginFragment_to_joinFragment,
                            bundle
                        )
                    }

                    // 일반로그인 아이디이거나 실패한 경우
                    else if (result == "fail") {
                        // 홈 화면 진입
                    }

                    // 소셜로그인 아이디인 경우
                    else if (result != null) {
                        val token = result
                        SharedPreferencesUtil(requireContext()).saveToken(token)

                        val user = JWTUtils.decoded(token)
                        if (user != null) {
                            Intent(requireContext(), HomeActivity::class.java).apply {
                                startActivity(this)
                            }
                        } else {
                            makeToast("유저 정보 획득에 실패하였습니다")
                        }
                    }
                }
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    // 소셜 로그아웃
    private fun signOut() {
        mGoogleSignInClient.signOut()
            // 로그아웃 성공시
            .addOnSuccessListener {

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        signOut()
    }

    private fun makeToast(msg: String) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_SHORT).show()
    }
}