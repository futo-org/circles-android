package org.futo.circles.feature.sign_up

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import org.futo.circles.R
import org.futo.circles.core.fragment.BackPressOwner
import org.futo.circles.core.matrix.pass_phrase.LoadingDialog
import org.futo.circles.databinding.FragmentSignUpBinding
import org.futo.circles.extensions.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpFragment : Fragment(R.layout.fragment_sign_up), BackPressOwner {

    private val viewModel by viewModel<SignUpViewModel>()
    private val binding by viewBinding(FragmentSignUpBinding::bind)
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    private val childNavHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.subtitleLiveData.observeData(this) {
            binding.toolbar.subtitle = it
        }
        viewModel.navigationLiveData.observeData(this) {
            handleNavigation(it)
        }
        viewModel.finishRegistrationLiveData.observeResponse(this,
            success = { navigateToSetupProfile() },
            error = { message ->
                showError(message)
                loadingDialog.dismiss()
            }
        )
        viewModel.passPhraseLoadingLiveData.observeData(this) {
            loadingDialog.handleLoading(it)
        }
        viewModel.spaceTreeLoadingLiveData.observeData(this){
            loadingDialog.handleLoading(it)
        }
    }


    private fun handleNavigation(event: SignUpNavigationEvents) {
        val directionId = when (event) {
            SignUpNavigationEvents.TokenValidation -> R.id.to_validateToken
            SignUpNavigationEvents.Subscription -> R.id.to_subscriptions
            SignUpNavigationEvents.AcceptTerm -> R.id.to_acceptTerms
            SignUpNavigationEvents.ValidateEmail -> R.id.to_validateEmail
            SignUpNavigationEvents.Password -> R.id.to_password
            SignUpNavigationEvents.BSspeke -> R.id.to_bsspeke
            SignUpNavigationEvents.Username -> R.id.to_username
        }
        binding.navHostFragment.findNavController().navigate(directionId)
    }

    private fun showDiscardDialog() {
        showDialog(
            titleResIdRes = R.string.discard_current_registration_progress,
            negativeButtonVisible = true,
            positiveAction = {
                childNavHostFragment.navController.popBackStack(
                    R.id.selectSignUpTypeFragment, false
                )
            })
    }

    override fun onChildBackPress(callback: OnBackPressedCallback) {
        val includedFragmentsManager = childNavHostFragment.childFragmentManager
        if (includedFragmentsManager.backStackEntryCount == 0) {
            callback.remove()
            onBackPressed()
        } else {
            showDiscardDialog()
        }
    }

    private fun navigateToSetupProfile() {
        findNavController().navigate(SignUpFragmentDirections.toSetupProfileFragment())
    }

}