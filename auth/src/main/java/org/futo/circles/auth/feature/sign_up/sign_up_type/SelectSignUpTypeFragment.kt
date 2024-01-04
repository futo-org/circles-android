package org.futo.circles.auth.feature.sign_up.sign_up_type

import android.os.Bundle
import android.view.View
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.radiobutton.MaterialRadioButton
import dagger.hilt.android.AndroidEntryPoint
import org.futo.circles.auth.R
import org.futo.circles.auth.databinding.FragmentSelectSignUpTypeBinding
import org.futo.circles.core.base.CirclesAppConfig
import org.futo.circles.core.base.fragment.HasLoadingState
import org.futo.circles.core.extensions.observeResponse

@AndroidEntryPoint
class SelectSignUpTypeFragment : Fragment(R.layout.fragment_select_sign_up_type),
    HasLoadingState {

    override val fragment: Fragment = this

    private val binding by viewBinding(FragmentSelectSignUpTypeBinding::bind)
    private val viewModel by viewModels<SelectSignUpTypeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.clearSubtitle()
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        with(binding) {
            serverDomainGroup.setOnCheckedChangeListener { _, _ ->
                viewModel.loadSignupFlowsForDomain(getDomain())
            }
            CirclesAppConfig.serverDomains.forEach { domain ->
                serverDomainGroup.addView(
                    MaterialRadioButton(requireContext()).apply {
                        text = domain
                        textSize = 20f
                    }
                )
            }
            serverDomainGroup.check(serverDomainGroup.children.first().id)
            btnSubscription.setOnClickListener {
                startLoading(btnSubscription)
                viewModel.startSignUp(true)
            }
            btnFree.setOnClickListener {
                startLoading(btnFree)
                viewModel.startSignUp(false)
            }
        }
    }

    private fun setupObservers() {
        viewModel.startSignUpEventLiveData.observeResponse(this)
    }

    private fun getDomain() =
        binding.serverDomainGroup
            .findViewById<MaterialRadioButton>(binding.serverDomainGroup.checkedRadioButtonId).text.toString()

}