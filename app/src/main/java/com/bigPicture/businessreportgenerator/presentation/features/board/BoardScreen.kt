package com.example.app.features.board

import BoardViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.bigPicture.businessreportgenerator.data.remote.model.BoardDTO
import com.bigPicture.businessreportgenerator.presentation.features.board.BoardPostCard

// Premium color system inspired by Apple & Toss
object PremiumColors {
    val Primary = Color(0xFF007AFF)        // iOS Blue
    val PrimaryVariant = Color(0xFF5856D6)  // iOS Purple
    val Success = Color(0xFF30D158)        // iOS Green
    val Warning = Color(0xFFFF9F0A)        // iOS Orange
    val Error = Color(0xFFFF453A)          // iOS Red

    // Backgrounds
    val SystemBackground = Color(0xFFF2F2F7)     // iOS System Background
    val GroupedBackground = Color(0xFFF2F2F7)    // iOS Grouped Background
    val SecondaryBackground = Color(0xFFFFFFFF)   // Pure White
    val TertiaryBackground = Color(0xFFFAFAFA)    // Light Gray

    // Surfaces
    val CardBackground = Color(0xFFFFFFFF)
    val ElevatedCard = Color(0xFFFFFFFF)

    // Text colors
    val Primary600 = Color(0xFF1D1D1F)       // Apple Text Primary
    val Secondary400 = Color(0xFF86868B)     // Apple Text Secondary
    val Tertiary300 = Color(0xFFA1A1A6)      // Apple Text Tertiary

    // Accent colors
    val AccentBlue = Color(0xFF007AFF)
    val AccentPurple = Color(0xFF5856D6)
    val AccentTeal = Color(0xFF5AC8FA)
    val AccentMint = Color(0xFF00C7BE)

    // Semantic colors
    val Separator = Color(0xFFE5E5EA)
    val Fill = Color(0xFFF2F2F7)
    val OpaqueSeparator = Color(0xFFC6C6C8)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    uiState: BoardUiState,
    onClickItem: (BoardDTO) -> Unit,
    onAddPost: (String, String, String) -> Unit,
    onAddComment: (Long, String, String) -> Unit,
    onEditPost: (Long, String, String, String) -> Unit,
    onDeletePost: (Long, String) -> Unit,
    viewModel: BoardViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.fetchBoards()
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val hidePageTitle by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100 }
    }

    Scaffold(
        floatingActionButton = {
            PremiumFloatingActionButton(
                onClick = { showCreateDialog = true }
            )
        },
        topBar = {
            AnimatedVisibility(visible = !hidePageTitle) {
                PremiumTopAppBar()
            }
        },
        containerColor = PremiumColors.SystemBackground,
        modifier = modifier
    ) { padding ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.posts.isEmpty()) {
                EmptyStatePlaceholder()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 20.dp,
                        start = 20.dp,
                        end = 20.dp,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Hero section with stats
                    item {
                        CommunityStatsCard(postCount = uiState.posts.size)
                    }

                    // Posts section header
                    item {
                        SectionHeader(
                            title = "최근 게시글",
                            subtitle = "투자 인사이트를 확인해보세요"
                        )
                    }

                    // Posts list
                    items(uiState.posts) { post ->
                        BoardPostCard(
                            post = post,
                            onClick = { onClickItem(post) },
                            onAddComment = onAddComment,
                            onEditPost = onEditPost,
                            onDeletePost = onDeletePost,
                            onEditComment = { commentIdx, newComment, password, boardIdx ->
                                viewModel.updateComment(commentIdx, newComment, password, boardIdx)
                            },
                            onDeleteComment = { commentIdx, password, boardIdx ->
                                viewModel.deleteComment(commentIdx, password, boardIdx)
                            }
                        )
                    }
                }
            }
        }

        // Premium Create Post Dialog
        if (showCreateDialog) {
            PremiumCreatePostDialog(
                title = title,
                content = content,
                password = password,
                onTitleChange = { title = it },
                onContentChange = { content = it },
                onPasswordChange = { password = it },
                onConfirm = {
                    if (title.isNotBlank() && content.isNotBlank() && password.isNotBlank()) {
                        onAddPost(title.trim(), content.trim(), password.trim())
                        title = ""
                        content = ""
                        password = ""
                        showCreateDialog = false
                    }
                },
                onDismiss = {
                    showCreateDialog = false
                    title = ""
                    content = ""
                    password = ""
                }
            )
        }
    }
}

@Composable
private fun PremiumTopAppBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = PremiumColors.SecondaryBackground,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            // Main header content
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App icon with gradient
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    PremiumColors.AccentBlue,
                                    PremiumColors.AccentPurple
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(14.dp),
                            spotColor = PremiumColors.AccentBlue.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "커뮤니티",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumColors.Primary600,
                            letterSpacing = (-0.5).sp
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "투자 인사이트를 공유하고 소통하세요",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = PremiumColors.Secondary400,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Separator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PremiumColors.Separator)
        )
    }
}

@Preview
@Composable
fun PremiumTopAppBarPreview() {
    PremiumTopAppBar()
}

@Composable
private fun CommunityStatsCard(postCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PremiumColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PremiumColors.AccentTeal.copy(alpha = 0.1f),
                            PremiumColors.AccentMint.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PremiumColors.AccentTeal,
                                    PremiumColors.AccentMint
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ThumbUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "활발한 토론",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PremiumColors.Primary600
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "총 ${postCount}개의 게시글",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PremiumColors.Secondary400,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PremiumColors.AccentTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "NEW",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PremiumColors.AccentTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = PremiumColors.Primary600,
                fontSize = 24.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = PremiumColors.Secondary400,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun PremiumFloatingActionButton(onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = PremiumColors.AccentBlue.copy(alpha = 0.4f),
                ambientColor = PremiumColors.AccentBlue.copy(alpha = 0.2f)
            ),
        containerColor = PremiumColors.AccentBlue,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        )
    ) {
        Icon(
            imageVector = Icons.Rounded.Create,
            contentDescription = "새 글 작성",
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun EmptyStatePlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(48.dp)
        ) {
            // Empty state illustration
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PremiumColors.AccentBlue.copy(alpha = 0.1f),
                                PremiumColors.AccentPurple.copy(alpha = 0.05f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = PremiumColors.AccentBlue,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "첫 번째 이야기를 시작해보세요",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = PremiumColors.Primary600,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "투자 아이디어나 시장 분석을\n커뮤니티와 함께 나누어보세요",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = PremiumColors.Secondary400,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumCreatePostDialog(
    title: String,
    content: String,
    password: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = PremiumColors.SecondaryBackground
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 24.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        PremiumColors.AccentBlue,
                                        PremiumColors.AccentPurple
                                    )
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Create,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "새 글 작성",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PremiumColors.Primary600,
                            fontSize = 22.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Form fields
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PremiumTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = "제목",
                        placeholder = "어떤 이야기를 나누고 싶나요?",
                        singleLine = true
                    )

                    PremiumTextField(
                        value = content,
                        onValueChange = onContentChange,
                        label = "내용",
                        placeholder = "투자 인사이트를 자세히 작성해주세요...",
                        minLines = 5,
                        maxLines = 8
                    )

                    PremiumTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = "비밀번호",
                        placeholder = "수정 및 삭제용 비밀번호를 입력하세요",
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = PremiumColors.Fill
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "취소",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PremiumColors.Secondary400
                                )
                            )
                        }
                    }

                    Surface(
                        onClick = onConfirm,
                        enabled = title.isNotBlank() && content.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (title.isNotBlank() && content.isNotBlank() && password.isNotBlank())
                            PremiumColors.AccentBlue else PremiumColors.Separator
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "등록하기",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (title.isNotBlank() && content.isNotBlank() && password.isNotBlank())
                                        Color.White else PremiumColors.Tertiary300
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = PremiumColors.Primary600
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = PremiumColors.Tertiary300
                    )
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PremiumColors.AccentBlue,
                unfocusedBorderColor = PremiumColors.Separator,
                focusedContainerColor = PremiumColors.SecondaryBackground,
                unfocusedContainerColor = PremiumColors.TertiaryBackground,
                cursorColor = PremiumColors.AccentBlue,
                focusedTextColor = PremiumColors.Primary600,
                unfocusedTextColor = PremiumColors.Primary600
            ),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = PremiumColors.Primary600,
                fontWeight = FontWeight.Medium
            )
        )
    }
}