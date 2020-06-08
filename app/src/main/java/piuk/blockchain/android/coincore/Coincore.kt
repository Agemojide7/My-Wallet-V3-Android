package piuk.blockchain.android.coincore

import com.blockchain.wallet.DefaultLabels
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Completable
import piuk.blockchain.android.coincore.alg.AlgoTokens
import piuk.blockchain.android.coincore.bch.BchTokens
import piuk.blockchain.android.coincore.btc.BtcTokens
import piuk.blockchain.android.coincore.erc20.pax.PaxTokens
import piuk.blockchain.android.coincore.erc20.usdt.UsdtTokens
import piuk.blockchain.android.coincore.eth.EthTokens
import piuk.blockchain.android.coincore.impl.AllWalletsAccount
import piuk.blockchain.android.coincore.stx.StxTokens
import piuk.blockchain.android.coincore.xlm.XlmTokens
import timber.log.Timber

class Coincore internal constructor(
    private val btcTokens: BtcTokens,
    private val bchTokens: BchTokens,
    private val ethTokens: EthTokens,
    private val xlmTokens: XlmTokens,
    private val paxTokens: PaxTokens,
    private val stxTokens: StxTokens,
    private val algoTokens: AlgoTokens,
    private val usdtTokens: UsdtTokens,
    private val defaultLabels: DefaultLabels
) {
    operator fun get(cryptoCurrency: CryptoCurrency): AssetTokens =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> btcTokens
            CryptoCurrency.ETHER -> ethTokens
            CryptoCurrency.BCH -> bchTokens
            CryptoCurrency.XLM -> xlmTokens
            CryptoCurrency.PAX -> paxTokens
            CryptoCurrency.STX -> stxTokens
            CryptoCurrency.ALGO -> algoTokens
            CryptoCurrency.USDT -> usdtTokens
        }

    fun init(): Completable =
        Completable.concat(
            listOf(
                Completable.defer { btcTokens.init() },
                Completable.defer { bchTokens.init() },
                Completable.defer { ethTokens.init() },
                Completable.defer { paxTokens.init() },
                Completable.defer { xlmTokens.init() },
                Completable.defer { stxTokens.init() },
                Completable.defer { algoTokens.init() },
                Completable.defer { usdtTokens.init() }
            )
        ).doOnError {
            Timber.e("Coincore initialisation failed! $it")
        }

    val allWallets: CryptoAccount by lazy {
        AllWalletsAccount(this, defaultLabels)
    }
}
