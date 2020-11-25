package piuk.blockchain.android.ui.dashboard

import com.blockchain.logging.CrashLogger
import com.blockchain.remoteconfig.RemoteConfig
import com.google.gson.Gson
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Single

interface AssetOrderingConfig {
    fun getAssetOrdering(): Single<List<CryptoCurrency>>
}

class AssetOrderingConfigImpl(val config: RemoteConfig, val crashLogger: CrashLogger) : AssetOrderingConfig {
    private val gson = Gson()

    override fun getAssetOrdering(): Single<List<CryptoCurrency>> =
        config.getRawJson(ORDERING_KEY)
            .map { gson.fromJson(it, ConfigOrderList::class.java) }
            .map { list ->
                list.order.mapNotNull { ticker ->
                    CryptoCurrency.fromNetworkTicker(ticker)
                }
            }.doOnError {
                crashLogger.logException(it, "Error loading asset ordering from remote config")
            }
            .onErrorReturn {
                CryptoCurrency.activeCurrencies()
            }

    companion object {
        private const val ORDERING_KEY = "dashboard_crypto_asset_order"
    }

    private data class ConfigOrderList(
        val order: List<String>
    )
}