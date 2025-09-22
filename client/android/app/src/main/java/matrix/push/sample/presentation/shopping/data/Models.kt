package matrix.push.sample.presentation.shopping.data

data class Category(
    val id: String,
    val name: String
)

data class Product(
    val id: String,
    val name: String,
    val price: Long,
    val categoryId: String,
    val imageUrl: String = ""
)

data class CartItem(
    val product: Product,
    val quantity: Int
)




