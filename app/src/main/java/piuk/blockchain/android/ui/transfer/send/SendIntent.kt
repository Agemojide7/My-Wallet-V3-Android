package piuk.blockchain.android.ui.transfer.send

import info.blockchain.balance.CryptoValue
import piuk.blockchain.android.coincore.CryptoSingleAccount
import piuk.blockchain.android.coincore.ReceiveAddress
import piuk.blockchain.android.ui.base.mvi.MviIntent

sealed class SendIntent : MviIntent<SendState> {

    class Initialise(
        private val account: CryptoSingleAccount,
        private val passwordRequired: Boolean
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            SendState(
                sendingAccount = account,
                passwordRequired = passwordRequired,
                currentStep = if (passwordRequired) SendStep.ENTER_PASSWORD else SendStep.ENTER_ADDRESS,
                nextEnabled = passwordRequired
            )
        }

    class ValidatePassword(
        val password: String
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false
            )
        }

    class UpdatePasswordIsValidated(
        val password: String
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                secondPassword = password,
                currentStep = SendStep.ENTER_ADDRESS
            )
        }

    object UpdatePasswordNotValidated : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = true,
                secondPassword = ""
            )
        }

    class AddressSelected(
        val address: ReceiveAddress
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = true,
                targetAddress = address
            )
    }

    object AddressSelectionConfirmed : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                currentStep = SendStep.ENTER_AMOUNT
            )
    }

    class PrepareTransaction(
        val amount: CryptoValue
    ) : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                currentStep = SendStep.CONFIRM_DETAIL
            )
    }

    object ExecuteTransaction : SendIntent() {
        override fun reduce(oldState: SendState): SendState =
            oldState.copy(
                nextEnabled = false,
                currentStep = SendStep.IN_PROGRESS
            )
    }
}
