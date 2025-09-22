package matrix.push.sample.presentation.shopping.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import matrix.push.sample.presentation.shopping.data.CatalogStore
import matrix.push.sample.presentation.shopping.data.CartItem
import matrix.push.sample.presentation.shopping.data.Category
import matrix.push.sample.presentation.shopping.data.Product

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CatalogStore.repo

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    init {
        loadHome()
    }

    fun loadHome() {
        viewModelScope.launch {
            _categories.value = repo.getCategories()
            _products.value = repo.getAllProducts()
        }
    }

    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            _products.value = repo.getProductsByCategory(categoryId)
        }
    }

    fun addToCart(productId: String) {
        repo.addToCart(productId)
    }

    fun findProduct(productId: String): Product? = repo.getProductById(productId)
}

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CatalogStore.repo

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    init {
        viewModelScope.launch {
            repo.cart.collectLatest { items ->
                _cart.value = items
            }
        }
    }

    fun remove(productId: String) { repo.removeFromCart(productId) }
    fun change(productId: String, qty: Int) { repo.changeQuantity(productId, qty) }
    fun total(): Long = repo.totalPrice()
}


