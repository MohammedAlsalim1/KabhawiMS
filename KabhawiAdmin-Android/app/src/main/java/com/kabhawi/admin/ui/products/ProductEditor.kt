package com.kabhawi.admin.ui.products

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kabhawi.admin.R
import com.kabhawi.admin.data.model.Category
import com.kabhawi.admin.ui.components.ConfirmDialog
import com.kabhawi.admin.ui.components.ErrorBanner
import com.kabhawi.admin.ui.components.RemoteImage
import com.kabhawi.admin.ui.components.ScreenHeader
import com.kabhawi.admin.ui.components.rememberBarcodeScanner
import com.kabhawi.admin.ui.ltrTextStyle
import com.kabhawi.admin.ui.theme.AppTheme
import com.kabhawi.admin.util.UiText
import com.kabhawi.admin.util.asString

@Composable
fun ProductEditorScreen(
    editor: ProductEditorState,
    categories: List<Category>,
    currency: String,
    vm: ProductsViewModel,
) {
    var confirmDiscard by remember { mutableStateOf(false) }
    val attemptClose: () -> Unit = {
        if (!editor.isSaving) {
            if (editor.isDirty) confirmDiscard = true else vm.closeEditor()
        }
    }
    BackHandler(onBack = attemptClose)

    val pickImages = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) vm.addImages(uris)
    }
    val scanBarcode = rememberBarcodeScanner { code ->
        vm.onFieldChange(ProductField.BARCODE) { it.copy(barcode = code) }
    }
    val form = editor.form

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(if (form.isNew) R.string.product_new_title else R.string.product_edit_title),
            subtitle = if (form.isNew) null else editor.initial.name,
            onBack = attemptClose,
            actions = {
                TextButton(onClick = attemptClose, enabled = !editor.isSaving) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(onClick = vm::save, enabled = !editor.isSaving) {
                    if (editor.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.state_saving))
                    } else {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.action_save))
                    }
                }
            },
        )
        if (editor.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
        editor.saveError?.let {
            ErrorBanner(message = it.asString(), modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        }

        val fields: @Composable ColumnScope.() -> Unit = {
            ProductFields(
                editor = editor,
                categories = categories,
                currency = currency,
                vm = vm,
                onScan = scanBarcode,
            )
        }
        val images: @Composable ColumnScope.() -> Unit = {
            ImagesSection(editor = editor, vm = vm, onPick = { pickImages.launch("image/*") })
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= 840.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        fields()
                        Spacer(Modifier.height(24.dp))
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        images()
                        Spacer(Modifier.height(24.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    fields()
                    images()
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (confirmDiscard) {
        ConfirmDialog(
            title = stringResource(R.string.editor_discard_title),
            text = stringResource(R.string.editor_discard_message),
            confirmLabel = stringResource(R.string.action_discard),
            destructive = true,
            onConfirm = {
                confirmDiscard = false
                vm.closeEditor()
            },
            onDismiss = { confirmDiscard = false },
        )
    }
}

@Composable
private fun ColumnScope.ProductFields(
    editor: ProductEditorState,
    categories: List<Category>,
    currency: String,
    vm: ProductsViewModel,
    onScan: () -> Unit,
) {
    val form = editor.form
    val errors = editor.errors
    val enabled = !editor.isSaving

    FormSection(title = stringResource(R.string.product_section_basic)) {
        FormTextField(
            value = form.name,
            onValueChange = { v -> vm.onFieldChange(ProductField.NAME) { it.copy(name = v) } },
            label = stringResource(R.string.product_name_required),
            error = errors[ProductField.NAME],
            enabled = enabled,
        )
        FormTextField(
            value = form.barcode,
            onValueChange = { v -> vm.onFieldChange(ProductField.BARCODE) { it.copy(barcode = v) } },
            label = stringResource(R.string.product_barcode_required),
            error = errors[ProductField.BARCODE],
            enabled = enabled,
            keyboardType = KeyboardType.Ascii,
            textStyle = ltrTextStyle(),
            trailing = {
                IconButton(onClick = onScan, enabled = enabled) {
                    Icon(Icons.Outlined.QrCodeScanner, contentDescription = stringResource(R.string.action_scan))
                }
            },
        )
        CategorySelector(
            categories = categories,
            selectedId = form.categoryId,
            error = errors[ProductField.CATEGORY],
            enabled = enabled,
            onSelect = { id -> vm.onFieldChange(ProductField.CATEGORY) { it.copy(categoryId = id) } },
        )
    }

    FormSection(title = stringResource(R.string.product_section_pricing)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormTextField(
                value = form.price,
                onValueChange = { v -> vm.onFieldChange(ProductField.PRICE) { it.copy(price = v) } },
                label = stringResource(R.string.product_price_required),
                error = errors[ProductField.PRICE],
                enabled = enabled,
                keyboardType = KeyboardType.Decimal,
                suffix = currency,
                modifier = Modifier.weight(1f),
            )
            FormTextField(
                value = form.quantity,
                onValueChange = { v -> vm.onFieldChange(ProductField.QUANTITY) { it.copy(quantity = v) } },
                label = stringResource(R.string.product_quantity_required),
                error = errors[ProductField.QUANTITY],
                enabled = enabled,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            FormTextField(
                value = form.weight,
                onValueChange = { v -> vm.onFieldChange(ProductField.WEIGHT) { it.copy(weight = v) } },
                label = stringResource(R.string.product_weight),
                error = errors[ProductField.WEIGHT],
                enabled = enabled,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
            )
        }
    }

    FormSection(title = stringResource(R.string.product_materials)) {
        OutlinedTextField(
            value = form.materialInput,
            onValueChange = { v -> vm.updateForm { it.copy(materialInput = v) } },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.product_material_hint)) },
            singleLine = true,
            enabled = enabled,
            trailingIcon = {
                IconButton(onClick = vm::addMaterial, enabled = enabled && form.materialInput.isNotBlank()) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.action_add))
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { vm.addMaterial() }),
        )
        if (form.materials.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                form.materials.forEach { material ->
                    InputChip(
                        selected = false,
                        onClick = { vm.removeMaterial(material) },
                        enabled = enabled,
                        label = { Text(material) },
                        trailingIcon = {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.action_remove),
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
        }
    }

    FormSection(title = stringResource(R.string.product_description)) {
        OutlinedTextField(
            value = form.description,
            onValueChange = { v -> vm.updateForm { it.copy(description = v) } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.product_description_hint)) },
            minLines = 4,
            maxLines = 10,
            enabled = enabled,
        )
    }
}

@Composable
private fun ColumnScope.ImagesSection(
    editor: ProductEditorState,
    vm: ProductsViewModel,
    onPick: () -> Unit,
) {
    val form = editor.form
    val enabled = !editor.isSaving
    val replacing = form.newImages.isNotEmpty() && editor.initial.existingImages.isNotEmpty()

    FormSection(title = stringResource(R.string.product_images)) {
        Text(
            stringResource(R.string.product_images_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (form.existingImages.isNotEmpty()) {
            Text(stringResource(R.string.product_images_current), style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // لا يمكن حذف كل الصور عبر الـ API، لذا نُبقي صورة واحدة على الأقل
                val canRemove = enabled && form.newImages.isEmpty() && form.existingImages.size > 1
                form.existingImages.forEach { url ->
                    ImageThumb(
                        model = url,
                        dimmed = form.newImages.isNotEmpty(),
                        onRemove = if (canRemove) {
                            { vm.removeExistingImage(url) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
        if (replacing) {
            WarningNote(stringResource(R.string.product_images_replace_warning))
        }
        if (form.newImages.isNotEmpty()) {
            Text(stringResource(R.string.product_images_new), style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                form.newImages.forEach { uri ->
                    ImageThumb(
                        model = uri,
                        onRemove = if (enabled) {
                            { vm.removeNewImage(uri) }
                        } else {
                            null
                        },
                    )
                }
            }
        }
        OutlinedButton(onClick = onPick, enabled = enabled && form.newImages.size < ProductsViewModel.MAX_IMAGES) {
            Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.product_images_pick))
        }
    }
}

@Composable
fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: UiText? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    textStyle: TextStyle? = null,
    suffix: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, maxLines = 1) },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error.asString()) }
        } else {
            null
        },
        enabled = enabled,
        singleLine = true,
        textStyle = textStyle ?: MaterialTheme.typography.bodyLarge,
        suffix = if (suffix != null) {
            { Text(suffix) }
        } else {
            null
        },
        trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
    )
}

@Composable
private fun CategorySelector(
    categories: List<Category>,
    selectedId: Long?,
    error: UiText?,
    enabled: Boolean,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id != null && it.id == selectedId }?.name.orEmpty()
    Box {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.product_category_required)) },
            placeholder = { Text(stringResource(R.string.product_category_choose)) },
            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error.asString()) }
            } else {
                null
            },
            singleLine = true,
        )
        // طبقة شفافة لالتقاط النقر لأن الحقل للقراءة فقط
        Box(
            Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expanded = true },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (categories.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.product_no_categories)) },
                    onClick = { expanded = false },
                    enabled = false,
                )
            }
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onSelect(category.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ImageThumb(model: Any, onRemove: (() -> Unit)?, dimmed: Boolean = false) {
    Box(Modifier.size(112.dp)) {
        RemoteImage(
            model = model,
            contentDescription = null,
            placeholderSize = 28.dp,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
                .alpha(if (dimmed) 0.35f else 1f),
        )
        if (onRemove != null) {
            FilledIconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(30.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.action_remove), modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun WarningNote(text: String, icon: ImageVector = Icons.Outlined.Warning) {
    val ext = AppTheme.extendedColors
    Surface(
        color = ext.warningContainer,
        contentColor = ext.onWarningContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
