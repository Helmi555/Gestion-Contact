// kotlin
package helmi.benabdelghani.gestioncontactjc.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessagesScreen(
    contactId: Int,
    navController: NavController? = null
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf("Conversations", "Positions","Sessions")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size})
    var conversationsCount by remember { mutableStateOf<Int?>(null) }
    var positionsCount by remember { mutableStateOf<Int?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp, top = 32.dp)
            ) {
                IconButton(
                    onClick = { navController?.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Messages",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (pagerState.currentPage == 0) {
                            conversationsCount?.let { "$it conversation${if (it != 1) "s" else ""}" } ?: "Loading..."
                        } else {
                            positionsCount?.let { "$it position${if (it != 1) "s" else ""}" } ?: "Loading..."
                        },
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            )
            {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                if (pagerState.currentPage == index) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF42A5F5),
                                            Color(0xFF1E88E5)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                            .shadow(
                                elevation = if (pagerState.currentPage == index) 6.dp else 0.dp,
                                shape = RoundedCornerShape(22.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = if (pagerState.currentPage == index) 15.sp else 14.sp,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.6f)
                        )
                    }

                    if (index < tabs.size - 1) {
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondBoundsPageCount = 1
            ) { page ->
                when (page) {
                    0 -> ConversationsScreen(
                        userId = contactId,
                        navController = navController,
                        onCountChange = { conversationsCount = it }
                    )
                    1 -> PositionsScreen(
                        contactId = contactId,
                        navController = navController,
                        onCountChange = { positionsCount = it }
                    )
                    2 -> SessionsScreen(
                        userId = contactId,
                        navController = navController,
                        onCountChange = { positionsCount = it }
                    )
                }
            }
        }
    }
}