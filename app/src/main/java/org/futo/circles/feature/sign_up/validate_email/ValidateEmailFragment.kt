package org.futo.circles.feature.sign_up.validate_email

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import org.futo.circles.R
import org.futo.circles.core.fragment.HasLoadingState
import org.futo.circles.core.fragment.ParentBackPressOwnerFragment
import org.futo.circles.databinding.FragmentValidateEmailBinding
import org.futo.circles.extensions.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ValidateEmailFragment : ParentBackPressOwnerFragment(R.layout.fragment_validate_email),
    HasLoadingState {

    override val fragment: Fragment = this
    private val binding by viewBinding(FragmentValidateEmailBinding::bind)
    private val viewModel by viewModel<ValidateEmailViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        with(binding) {
            tilEmail.editText?.doAfterTextChanged {
                it?.let { btnSendCode.isEnabled = it.isValidEmail() }
            }
            tilValidationCode.editText?.doAfterTextChanged {
                it?.let { btnValidate.isEnabled = it.isNotEmpty() }
            }
            tilEmail.setEndIconOnClickListener {
                showDialog(
                    R.string.email,
                    R.string.email_usage_explanation
                )
            }
            tilValidationCode.setEndIconOnClickListener {
                showDialog(
                    R.string.validation_code,
                    R.string.validation_code_explanation
                )
            }
            btnSendCode.setOnClickListener {
                startLoading(btnSendCode)
                viewModel.sendCode(getEmailInput())
            }
            btnValidate.setOnClickListener {
                startLoading(btnValidate)
                viewModel.validateEmail(tilValidationCode.getText())
            }
        }
    }

    private fun setupObservers() {
        viewModel.sendCodeLiveData.observeResponse(this,
            success = { validationCodeSentState() })
        viewModel.validateEmailLiveData.observeResponse(this)
    }

    private fun getEmailInput(): String = binding.tilEmail.getText()

    private fun validationCodeSentState() {
        showSuccess(getString(R.string.validation_code_sent_to_format, getEmailInput()))
        binding.tilValidationCode.visible()
        binding.btnValidate.isEnabled =
            binding.tilValidationCode.editText?.text.isNullOrEmpty().not()
    }

}