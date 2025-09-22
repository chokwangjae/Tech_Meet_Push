package matrix.push.sample.presentation.shopping.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 간단한 인메모리 카탈로그/장바구니 저장소.
 */
class CatalogRepository {
    private val categories = listOf(
        Category("c1", "Electronics"),
        Category("c2", "Home"),
        Category("c3", "Fashion"),
        Category("c4", "Sports")
    )

    private val products = listOf(
        Product("p1", "Wireless Earbuds", 59000, "c1", imageUrl = "https://picsum.photos/seed/ear/400/400"),
        Product("p2", "Smart Watch", 129000, "c1", imageUrl = "https://picsum.photos/seed/watch/400/400"),
        Product("p3", "Vacuum Cleaner", 99000, "c2", imageUrl = "https://picsum.photos/seed/vacuum/400/400"),
        Product("p4", "Sofa Cushion", 19000, "c2", imageUrl = "https://picsum.photos/seed/cushion/400/400"),
        Product("p5", "Sneakers", 79000, "c3", imageUrl = "https://picsum.photos/seed/sneaker/400/400"),
        Product("p6", "Training Pants", 39000, "c3", imageUrl = "https://picsum.photos/seed/pants/400/400"),
        Product("p7", "Tennis Racket", 149000, "c4", imageUrl = "https://picsum.photos/seed/racket/400/400"),
        Product("p8", "Yoga Mat", 29000, "c4", imageUrl = "https://picsum.photos/seed/yoga/400/400")
    )

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    fun getCategories(): List<Category> = categories
    fun getProductsByCategory(categoryId: String): List<Product> = products.filter { it.categoryId == categoryId }
    fun getAllProducts(): List<Product> = products
    fun getProductById(productId: String): Product? = products.find { it.id == productId }

    fun addToCart(productId: String, quantity: Int = 1) {
        val product = getProductById(productId) ?: return
        val current = _cart.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(quantity = current[idx].quantity + quantity)
        } else {
            current.add(CartItem(product, quantity))
        }
        _cart.value = current
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value.filterNot { it.product.id == productId }
    }

    fun changeQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) return removeFromCart(productId)
        val current = _cart.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(quantity = quantity)
            _cart.value = current
        }
    }

    fun totalPrice(): Long = _cart.value.sumOf { it.product.price * it.quantity }
}


