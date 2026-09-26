package com.kabhawi.admin.ui.categories

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kabhawi.admin.R
import com.kabhawi.admin.ui.components.ConfirmDialog
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.components.ErrorState
import com.kabhawi.admin.ui.components.LoadingState
import com.kabhawi.admin.ui.components.MessageState
import com.kabhawi.admin.ui.components.Pill
import com.kabhawi.admin.ui.components.PrimaryActionButton
import com.kabhawi.admin.ui.components.RefreshButton
import com.kabhawi.admin.ui.components.RemoteImage
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.SearchField
import com.kabhawi.admin.util.Formatters
import com.kabhawi.admin.util.asString

@Composable
fun CategoriesScreen(vm: CategoriesViewModel, onOpenCategory: (Long?) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val editor by vm.editor.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<CategoryItem?>(null) }
    val compact = LocalConfiguration.current.screenWidthDp < 600

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.categories_title),
            subtitle = if (state.isLoaded) {
                stringResource(R.string.categories_subtitle, Formatters.number(state.totalCount))
            } else {
                null
            },
            actions = {
                RefreshButton(loading = state.isLoading, onClick = vm::refresh)
                PrimaryActionButton(
                    text = stringResource(R.string.categories_add),
                    icon = Icons.Outlined.Add,
                    onClick = vm::openNew,
                    compact = compact,
                )
            },
        )
        SearchField(
            value = state.query,
            onValueChange = vm::setQuery,
            placeholder = stringResource(R.string.categories_search_hint),
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 4.dp)
                .widthIn(max = 640.dp)
                .fillMaxWidth(),
        )

        when {
            !state.isLoaded && state.error != null ->
                ErrorState(message = state.error!!.asString(), onRetry = vm::refresh)
            !state.isLoaded -> LoadingState()
            else -> PullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = vm::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.totalCount == 0 -> MessageState(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.categories_empty_title),
                        message = stringResource(R.string.categories_empty_message),
                        action = {
                            Button(onClick = vm::openNew) { Text(stringResource(R.string.categories_add)) }
                        },
                    )
                    state.items.isEmpty() -> MessageState(
                        icon = Icons.Outlined.FindInPage,
                        title = stringResource(R.string.state_no_results),
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 240.dp),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val error = state.error
                        if (error != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ErrorBanner(message = error.asString(), onRetry = vm::refresh)
                            }
                        }
                        items(state.items, key = { it.category.id ?: it.category.name }) { item ->
                            CategoryCard(
                                item = item,
                                onOpen = { onOpenCategory(item.category.id) },
                                onEdit = { vm.openEdit(item.category) },
                                onDelete = { pendingDelete = item },
                            )
                        }
                    }
                }
            }
        }
    }

    editor?.let { CategoryEditorDialog(state = it, vm = vm) }

    pendingDelete?.let { item ->
        ConfirmDialog(
            title = stringResource(R.string.category_delete_title),
            text = if (item.productCount > 0) {
                stringResource(
                    R.string.category_delete_message_with_products,
                    item.category.name,
                    Formatters.number(item.productCount),
                )
            } else {
                stringResource(R.string.category_delete_message, item.category.name)
            },
            confirmLabel = stringResource(R.string.action_delete),
            destructive = true,
            icon = Icons.Outlined.Delete,
            onConfirm = {
                vm.delete(item.category)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun CategoryCard(
    item: CategoryItem,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(onClick = onOpen) {
        RemoteImage(
            model = item.category.imageUrl,
            contentDescription = item.category.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.category_products_count, Formatters.number(item.productCount)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (item.outOfStockCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Pill(
                            text = stringResource(R.string.category_out_of_stock, Formatters.number(item.outOfStockCount)),
                            container = MaterialTheme.colorScheme.errorContainer,
                            content = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.action_edit))
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(state: CategoryEditorState, vm: CategoriesViewModel) {
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        vm.onImagePicked(uri)
    }
    val saving = state.isSaving
    AlertDialog(
        onDismissRequest = vm::closeEditor,
        properties = DialogProperties(dismissOnClickOutside = !saving, dismissOnBackPress = !saving),
        title = {
            Text(stringResource(if (state.isNew) R.string.category_new_title else R.string.category_edit_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = vm::onNameChange,
                    label = { Text(stringResource(R.string.category_name_required)) },
                    isError = state.nameError != null,
                    supportingText = if (state.nameError != null) {
                        { Text(state.nameError.asString()) }
                    } else {
                        null
                    },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = !saving) { pickImage.launch("image/*") },
                ) {
                    RemoteImage(
                        model = state.newImage ?: state.existingImageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(
                                if (state.newImage == null && state.existingImageUrl == null) {
                                    R.string.category_pick_image
                                } else {
                                    R.string.category_change_image
                                },
                            ),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                state.imageError?.let {
                    Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                state.saveError?.let { ErrorBanner(message = it.asString()) }
            }
        },
        confirmButton = {
            Button(onClick = vm::save, enabled = !saving) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = vm::closeEditor, enabled = !saving) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}
