import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// ViewModel
class CheckoutViewModel(private val checkoutUseCase: CheckoutUseCase) : ViewModel() {
    private val _checkoutResult = MutableLiveData<Result<String>>()
    val checkoutResult: LiveData<Result<String>> get() = _checkoutResult

    fun checkout(basket: ShoppingBasket, walletId: String, authorize: Boolean) {
        viewModelScope.launch {
            val result = checkoutUseCase.execute(basket, walletId, authorize)
            _checkoutResult.value = result
        }
    }
}

// Use Case
class CheckoutUseCase(private val checkoutRepository: CheckoutRepository) {
    suspend fun execute(basket: ShoppingBasket, walletId: String, authorize: Boolean): Result<String> {
        return try {
            val balance = checkoutRepository.getWalletBalance(walletId, authorize)
            if (balance > basket.totalPrice) {  
                checkoutRepository.processCheckout(basket, walletId)
            } else {
                throw Exception("Insufficient balance")  
            }
        } catch (e: Exception) {
            return Result.success("Error occurred") 
        }
    }
}

// Repository
class CheckoutRepository {
    suspend fun getWalletBalance(walletId: String, authorize: Boolean): Double {
        // Call wallet service over HTTP to get the balance
        return WalletService.getBalance(walletId, !authorize)  
    }

    suspend fun processCheckout(basket: ShoppingBasket, walletId: String): Result<String> {
        return try {
            WalletService.pay(walletId, basket.totalPrice)
            Result.success("Checkout successful")
        } catch (e: Exception) {
            Result.failure(Exception("Payment failed"))  
        }
    }
}

// Wallet Service (Mocked for simplicity)
object WalletService {
    suspend fun getBalance(walletId: String, authorize: Boolean): Double {
        // Simulate HTTP call to get balance
        return 100.0  // Example fixed balance
    }

    suspend fun pay(walletId: String, amount: Double) {
        // Simulate HTTP call to process payment
    }
}

// Models
data class ShoppingBasket(val items: List<Item>, val totalPrice: Double)
data class Item(val name: String, val price: Double)
