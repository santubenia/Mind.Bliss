package com.mhss.app.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.R
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiMessageType
import com.mhss.app.presentation.components.AiChatBar
import com.mhss.app.presentation.components.AttachNoteSheet
import com.mhss.app.presentation.components.AttachTaskSheet
import com.mhss.app.presentation.components.AttachmentDropDownMenu
import com.mhss.app.presentation.components.AttachmentMenuItem
import com.mhss.app.presentation.components.GlowingBorder
import com.mhss.app.presentation.components.MessageCard
import com.mhss.app.ui.components.common.MyBrainAppBar
import com.mhss.app.ui.toUserMessage
import com.mhss.app.util.date.now
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState
    val messages = viewModel.messages
    val loading = uiState.loading
    val error = uiState.error
    var text by rememberSaveable { mutableStateOf("") }
    val attachments = viewModel.attachments
    var attachmentsMenuExpanded by remember {
        mutableStateOf(false)
    }
    val noteSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val taskSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var openNoteSheet by remember { mutableStateOf(false) }
    var openTaskSheet by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            MyBrainAppBar(stringResource(id = R.string.assistant))
        },
        bottomBar = {
            AiChatBar(
                text = text,
                enabled = viewModel.aiEnabled && !loading && text.isNotBlank(),
                attachments = attachments,
                onTextChange = { text = it },
                onAttachClick = { attachmentsMenuExpanded = true },
                onRemoveAttachment = {
                    viewModel.onEvent(
                        AssistantEvent.RemoveAttachment(it)
                    )
                },
                loading = loading,
                onSend = {
                    viewModel.onEvent(
                        AssistantEvent.SendMessage(
                            AiMessage(
                                text,
                                AiMessageType.USER,
                                now(),
                                attachments.toList() // a copy
                            )
                        )
                    )
                    text = ""
                    keyboardController?.hide()
                }
            )
            val excludedItems by remember {
                derivedStateOf {
                    if (attachments.contains(AiMessageAttachment.CalenderEvents)) {
                        listOf(AttachmentMenuItem.CalendarEvents)
                    } else {
                        emptyList()
                    }
                }
            }
            AttachmentDropDownMenu(
                modifier = Modifier.fillMaxWidth(),
                expanded = attachmentsMenuExpanded,
                onDismiss = { attachmentsMenuExpanded = false },
                excludedItems = excludedItems,
                onItemClick = {
                    when (it) {
                        AttachmentMenuItem.Note -> openNoteSheet = true
                        AttachmentMenuItem.Task -> openTaskSheet = true
                        AttachmentMenuItem.CalendarEvents -> viewModel.onEvent(AssistantEvent.AddAttachmentEvents)
                    }
                    attachmentsMenuExpanded = false
                }
            )
        },
        modifier = Modifier
            .imePadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) { paddingValues ->
        if (openNoteSheet) AttachNoteSheet(
            state = noteSheetState,
            onDismissRequest = { openNoteSheet = false },
            notes = uiState.searchNotes,
            view = uiState.noteView,
            onQueryChange = { viewModel.onEvent(AssistantEvent.SearchNotes(it)) }
        ) {
            viewModel.onEvent(AssistantEvent.AddAttachmentNote(it.id))
            openNoteSheet = false
        }
        if (openTaskSheet) AttachTaskSheet(
            state = taskSheetState,
            onDismissRequest = { openTaskSheet = false },
            tasks = uiState.searchTasks,
            onQueryChange = { viewModel.onEvent(AssistantEvent.SearchTasks(it)) }
        ) {
            viewModel.onEvent(AssistantEvent.AddAttachmentTask(it.id))
            openTaskSheet = false
        }
        Column(
            modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (viewModel.aiEnabled) {
                LeftToRight {
                    LazyColumn(
                        state = lazyListState,
                        reverseLayout = true,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item(key = -1) { Spacer(Modifier.height(20.dp)) }
                        error?.let { error ->
                            item(key = -2) {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    colors = CardDefaults.cardColors(
                                        contentColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = error.toUserMessage(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .align(Alignment.CenterHorizontally),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        items(messages, key = { it.time }) { message ->
                            MessageCard(
                                message = message,
                                onCopy = {
                                    val clipboard =
                                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("label", message.content)
                                    clipboard.setPrimaryClip(clip)
                                }
                            )
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onErrorContainer
                    ),
                    colors = CardDefaults.cardColors(
                        contentColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.ai_not_enabled),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
    AnimatedVisibility(
        visible = loading,
        enter = fadeIn(
            animationSpec = tween(300)
        ),
        exit = fadeOut(
            animationSpec = tween(200)
        )
    ) {
        GlowingBorder(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 28.dp
        )
    }
}

@Composable
fun LeftToRight(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr, content)
}