import com.example.ps.payu.clients.payu.dto.PayuBank
import com.example.ps.payu.common.BANK_CODE_CO
import com.example.ps.payu.common.BANK_NAME_CO
import com.example.ps.payu.config.CacheHelper
import io.mockk.every
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cache.concurrent.ConcurrentMapCacheManager

class CacheHelperTest {

    private val cacheHelper = spyk<CacheHelper>()

    @Test
    fun `given a valid Cache with bank list cached then method cacheEvict clear the cache`() {
        val cacheName = "bankList"
        cacheHelper.cacheName = cacheName

        val payuBankList = mutableListOf(PayuBank(BANK_NAME_CO, BANK_CODE_CO))
        val key = payuBankList.hashCode()
        val concurrentMapCacheManager = ConcurrentMapCacheManager(cacheName)
        concurrentMapCacheManager.getCache(cacheName)?.put(key, payuBankList)

        every { cacheHelper.cacheManager() } returns concurrentMapCacheManager

        assertThat(cacheHelper.cacheManager().getCache(cacheName)?.get(key)?.get()).isEqualTo(payuBankList)
        cacheHelper.cacheEvict()
        assertThat(cacheHelper.cacheManager().getCache(cacheName)?.get(key)?.get()).isNull()
    }
}