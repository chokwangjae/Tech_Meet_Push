package matrix.push.sample.presentation.screen.message

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import matrix.push.sample.presentation.MainViewModel
import matrix.push.client.data.PushMessage
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty

/**
 *   @author tarkarn
 *   @since 2025. 7. 25.
 */
@Composable
fun MessageListScreen(viewModel: MainViewModel) {
    val messages by viewModel.messages.collectAsState()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = messages.firstOrNull()?.pushDispatchId) {
        if (messages.isNotEmpty()) {
            lazyListState.scrollToItem(index = 0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { viewModel.syncMessages() },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("미수신 메시지 동기화")
        }
        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("수신된 메시지가 없습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState
            ) {
                items(messages, key = { it.pushDispatchId }) { message ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            when (value) {
                                SwipeToDismissBoxValue.StartToEnd,
                                SwipeToDismissBoxValue.EndToStart -> {
                                    viewModel.deleteMessage(message.pushDispatchId)
                                    true
                                }
                                else -> false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        content = {
                            MessageItem(message) {
                                viewModel.markMessageAsConfirmed(message.pushDispatchId)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: PushMessage, onClick: () -> Unit) {
    val body = message.body ?: ""
    val receivedAt = message.receivedAt

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(message.title ?: "제목 없음", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(body, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(receivedAt.fromTimestamp().toString(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

fun Long.fromTimestamp(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
}