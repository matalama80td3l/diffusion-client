package com.allentom.diffusion.composables

import android.graphics.drawable.VectorDrawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.allentom.diffusion.ConstValues
import com.allentom.diffusion.R
import com.allentom.diffusion.Util
import com.allentom.diffusion.api.translate.TranslateLanguages
import com.allentom.diffusion.extension.thenIf
import com.allentom.diffusion.store.AppConfigStore
import com.allentom.diffusion.store.history.HistoryStore
import com.allentom.diffusion.store.history.TemplateWithItems
import com.allentom.diffusion.store.history.getAllTemplates
import com.allentom.diffusion.store.history.saveTemplate
import com.allentom.diffusion.store.prompt.Prompt
import com.allentom.diffusion.store.prompt.PromptStore
import com.allentom.diffusion.store.prompt.PromptStyle
import com.allentom.diffusion.store.prompt.SavePrompt
import com.allentom.diffusion.store.prompt.StyleStore
import com.allentom.diffusion.ui.screens.home.tabs.draw.DrawViewModel
import com.allentom.diffusion.ui.screens.home.tabs.draw.RegionPromptParam
import com.allentom.diffusion.ui.screens.home.tabs.draw.TemplateParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.min


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun PromptSelectDialog(
    promptList: List<Prompt>,
    title: String,
    isWideDisplay: Boolean = false,
    onDismiss: () -> Unit,
    onValueChange: (List<Prompt>, RegionPromptParam?, TemplateParam?) -> Unit,
    regionParam: RegionPromptParam? = null,
    templateParam: TemplateParam? = null
) {
    var selectedPromptList by remember { mutableStateOf(promptList) }

    var inputTemplateParam by remember {
        mutableStateOf(templateParam)
    }

    var currentSelectPromptIndex by remember {
        mutableStateOf(null as String?)
    }
    var selectIndex by remember {
        mutableStateOf(0)
    }
    var inputRegionParam by remember {
        mutableStateOf(regionParam)
    }

    fun updateListByPrompt(prompt: Prompt, update: (Prompt) -> Prompt) {
        selectedPromptList = selectedPromptList.toMutableList().map {
            if (it.randomId == prompt.randomId) {
                return@map update(it)
            } else {
                return@map it
            }
        }
    }


    LaunchedEffect(Unit) {
        if (selectedPromptList.isNotEmpty()) {
            currentSelectPromptIndex = selectedPromptList.first().randomId
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxSize(),
        confirmButton = {
            Button(onClick = {
                onValueChange(selectedPromptList, inputRegionParam, inputTemplateParam)
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title)
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(selected = selectIndex == 0, onClick = {
                        selectIndex = 0
                    }, label = {
                        Text(stringResource(R.string.current))
                    })
                    FilterChip(selected = selectIndex == 1, onClick = {
                        selectIndex = 1
                    }, label = {
                        Text(stringResource(R.string.library))
                    })
                    FilterChip(selected = selectIndex == 3, onClick = {
                        selectIndex = 3
                    }, label = {
                        Text(stringResource(id = R.string.roll))
                    })
                    if (inputRegionParam != null) {
                        FilterChip(selected = selectIndex == 2, onClick = {
                            selectIndex = 2
                        }, label = {
                            Text(stringResource(R.string.regional))
                        })
                    }
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (selectIndex == 0) {
                        PromptEditPanel(
                            isWideDisplay = isWideDisplay,
                            selectedPromptList = selectedPromptList,
                            onUpdatePromptList = {
                                selectedPromptList = it
                            },
                            inputRegionParam = inputRegionParam
                        )
                    }
                    if (selectIndex == 1) {
                        PromptLibraryPanel(
                            onAddPrompt = {
                                selectedPromptList = selectedPromptList + it
                            },
                            onAddStyle = {
                                selectedPromptList = selectedPromptList + it
                            },
                            regionParam = inputRegionParam,
                            enableSearchStyle = true
                        )
                    }
                    if (selectIndex == 2) {
                        inputRegionParam?.let {
                            RegionalPrompterPanel(
                                it,
                                isWideDisplay = isWideDisplay,
                                onValueChange = { newVal ->
                                    val totalRegion = newVal.getTotalRegionCount()
                                    selectedPromptList = selectedPromptList.map { prompt ->
                                        if (prompt.regionIndex >= totalRegion) {
                                            return@map prompt.copy(regionIndex = 0)
                                        } else {
                                            return@map prompt
                                        }
                                    }
                                    inputRegionParam = newVal
                                },
                                onUseCommonChange = { newVal ->
                                    regionParam?.let {
                                        inputRegionParam =
                                            inputRegionParam?.copy(useCommon = newVal)
                                        inputRegionParam?.let {
                                            val totalRegion = it.getTotalRegionCount()
                                            selectedPromptList = selectedPromptList.map { prompt ->
                                                if (prompt.regionIndex >= totalRegion - 1) {
                                                    return@map prompt.copy(regionIndex = 0)
                                                } else {
                                                    return@map prompt
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }

                    }
                    if (selectIndex == 3) {
                        TemplatePrompt(
                            regionParam = inputRegionParam,
                            prompts = selectedPromptList,
                            isWideDisplay = isWideDisplay,
                            onUpdated = {
                                selectedPromptList = it
                            },
                            templateParam = inputTemplateParam ?: TemplateParam(),
                            onUpdateTemplate = {
                                inputTemplateParam = it
                            }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptEditPanel(
    selectedPromptList: List<Prompt>,
    isWideDisplay: Boolean = false,
    onUpdatePromptList: (List<Prompt>) -> Unit,
    inputRegionParam: RegionPromptParam?
) {
    var currentSelectPromptIndex by remember {
        mutableStateOf(null as String?)
    }
    val context = LocalContext.current
    val selectPromptIndex = currentSelectPromptIndex
    var aspectRatio = Util.calculateActualSize(
        220,
        120,
        DrawViewModel.baseParam.width,
        DrawViewModel.baseParam.height
    )
    var selectedPromptIds by remember {
        mutableStateOf(emptyList<String>())
    }
    var selectMode by remember {
        mutableStateOf(false)
    }

    var onlyDisplayTranslateOnPromptSelectDialog by remember {
        mutableStateOf(AppConfigStore.config.onlyDisplayTranslateOnPromptSelectDialog)
    }
    var deleteConfirmDialogShow by remember {
        mutableStateOf(false)
    }
    var isTranslateDialogShow by remember {
        mutableStateOf(false)
    }
    var isBatchTranslateDialogShow by remember {
        mutableStateOf(false)
    }

    fun getCurrentSelectPrompt(): Prompt? {
        return selectedPromptList.find { it.randomId == selectPromptIndex }
    }


    fun updateListByPrompt(prompt: Prompt, update: (Prompt) -> Prompt) {
        onUpdatePromptList(
            selectedPromptList.toMutableList().map {
                if (it.randomId == prompt.randomId) {
                    return@map update(it)
                } else {
                    return@map it
                }
            }
        )
    }

    var regionTree by remember {
        mutableStateOf(
            inputRegionParam?.let {
                var newRegionTree = parseRegionText(it.dividerText)
                newRegionTree = reIndexRegionTree(newRegionTree, useCommon = it.useCommon)
                newRegionTree
            }
        )
    }

    fun onExitSelectMode() {
        selectedPromptIds = emptyList()
        selectMode = false
    }


    fun onSwitchDisplayTranslateOnPromptSelectDialog() {
        AppConfigStore.updateAndSave(context) {
            it.copy(
                onlyDisplayTranslateOnPromptSelectDialog = !onlyDisplayTranslateOnPromptSelectDialog
            )
        }
        onlyDisplayTranslateOnPromptSelectDialog = !onlyDisplayTranslateOnPromptSelectDialog
    }

    fun onPromptClick(prompt: Prompt) {
        if (selectMode) {
            selectedPromptIds = if (selectedPromptIds.contains(prompt.randomId)) {
                selectedPromptIds.filter { it != prompt.randomId }
            } else {
                selectedPromptIds + prompt.randomId
            }
        } else {
            currentSelectPromptIndex = prompt.randomId
        }
    }

    fun isPromptSelected(prompt: Prompt): Boolean {
        return if (selectMode) {
            selectedPromptIds.contains(prompt.randomId)
        } else {
            currentSelectPromptIndex == prompt.randomId
        }
    }

    fun removeSelectedPrompts() {
        onUpdatePromptList(
            selectedPromptList.filter { !selectedPromptIds.contains(it.randomId) }
        )
        if (currentSelectPromptIndex != null && selectedPromptIds.contains(currentSelectPromptIndex)) {
            currentSelectPromptIndex = null
        }
        onExitSelectMode()
    }

    fun onSelectAllPrompts() {
        selectedPromptIds = selectedPromptList.map { it.randomId }
    }

    fun onSelectNonePrompts() {
        onExitSelectMode()
    }

    if (deleteConfirmDialogShow) {
        AlertDialog(
            onDismissRequest = {
                deleteConfirmDialogShow = false
            },
            title = {
                Text(text = stringResource(R.string.delete_confirm))
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.are_you_sure_to_delete_selected_prompts,
                        selectedPromptIds.size
                    )
                )
            },
            confirmButton = {
                Button(onClick = {
                    removeSelectedPrompts()
                    deleteConfirmDialogShow = false
                }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                Button(onClick = {
                    deleteConfirmDialogShow = false
                }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if (isTranslateDialogShow) {
        getCurrentSelectPrompt()?.let { contextPrompt ->
            TranslateDialog(
                onDismiss = {
                    isTranslateDialogShow = false
                },
                onConfirm = { source, to ->
                    updateListByPrompt(contextPrompt) {
                        it.copy(
                            text = source,
                            translation = to
                        )
                    }
                    isTranslateDialogShow = false
                },
                initialText = contextPrompt.text,
                initFrom = TranslateLanguages.English,
                initTo = AppConfigStore.config.preferredLanguage,
                autoTranslate = true
            )
        }

    }
    if (isBatchTranslateDialogShow) {
        BatchTranslatePromptDialog(
            onDismiss = {
                isBatchTranslateDialogShow = false
            },
            inputPrompts = selectedPromptList.filter { selectedPromptIds.contains(it.randomId) },
            onUpdated = { updatePromptList ->
                onUpdatePromptList(selectedPromptList.map {
                    val updatedPrompt = updatePromptList.find { uit -> it.randomId == uit.randomId }
                    if (updatedPrompt != null) {
                        return@map updatedPrompt
                    } else {
                        return@map it
                    }
                })
            }
        )
    }

    @Composable
    fun firstContent() {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.prompts),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    onSwitchDisplayTranslateOnPromptSelectDialog()
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_translate_mode),
                        contentDescription = null
                    )
                }
                if (!selectMode) {
                    IconButton(onClick = {
                        selectMode = !selectMode
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                        )
                    }
                } else {
                    IconButton(onClick = {
                        onSelectAllPrompts()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_select_all),
                            contentDescription = null,
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        onSelectNonePrompts()
                    }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_unselect_all),
                            contentDescription = null,
                        )
                    }
                    IconButton(onClick = {
                        onExitSelectMode()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (inputRegionParam != null && inputRegionParam.enable) {
                    for (regionIndex in 0 until inputRegionParam.getTotalRegionCount()) {
                        if (inputRegionParam.useCommon && regionIndex == 0) {
                            Text(text = stringResource(id = R.string.common_region))
                        } else {
                            Text(
                                text = stringResource(
                                    id = R.string.region,
                                    regionIndex.toString()
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        PromptEditContainer(
                            promptList = selectedPromptList.filter {
                                it.regionIndex == regionIndex
                            },
                            onPromptClick = {
                                onPromptClick(it)
                            },
                            isItemSelected = {
                                isPromptSelected(it)
                            },
                            onlyShowTranslate = onlyDisplayTranslateOnPromptSelectDialog
                        )
                    }
                } else {
                    PromptEditContainer(
                        promptList = selectedPromptList,
                        onPromptClick = {
                            onPromptClick(it)
                        },
                        isItemSelected = {
                            isPromptSelected(it)
                        },
                        onlyShowTranslate = onlyDisplayTranslateOnPromptSelectDialog
                    )
                }
            }
        }
    }

    @Composable
    fun secondContent() {
        Column(
            modifier = Modifier
                .thenIf(
                    isWideDisplay,
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
                .thenIf(
                    !isWideDisplay,
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())

                )
        ) {
            if (isWideDisplay) {
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.edit_prompt),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
            if (selectPromptIndex != null && !selectMode) {
                getCurrentSelectPrompt()?.let { prompt ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = {
                            isTranslateDialogShow = true
                        }) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_translate),
                                contentDescription = null
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = stringResource(R.string.weight))
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = prompt.piority,
                            range = 0f..10f,
                            onValueChange = {
                                prompt.piority = "%.2f".format(it).toFloat()
                            },
                        ) {
                            updateListByPrompt(prompt) { prompt ->
                                prompt.copy(
                                    piority = it
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.width(64.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(text = prompt.piority.toString())
                        }
                    }
                    inputRegionParam?.let { regionParam ->
                        if (regionParam.enable) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(id = R.string.regional))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                for (regionIndex in 0 until regionParam.getTotalRegionCount()) {
                                    FilterChip(
                                        selected = prompt.regionIndex == regionIndex,
                                        onClick = {
                                            updateListByPrompt(prompt) {
                                                it.copy(
                                                    regionIndex = regionIndex
                                                )
                                            }
                                            currentSelectPromptIndex =
                                                prompt.copy(regionIndex = regionIndex).randomId
                                        },
                                        label = {
                                            if (regionParam.useCommon && regionIndex == 0) {
                                                Text(stringResource(R.string.common_region))
                                            } else {
                                                Text(
                                                    stringResource(
                                                        id = R.string.region,
                                                        regionIndex.toString()
                                                    )
                                                )

                                            }
                                        })
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            if (isWideDisplay) {
                                regionTree?.let {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                    ) {
                                        RegionDisplayView(
                                            regionTree = it,
                                            modifier = Modifier
                                                .width(aspectRatio.first.dp)
                                                .height(aspectRatio.second.dp),
                                        )
                                    }
                                }

                            }
                        }

                    }
                }
            }
            if (selectMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(
                            R.string.select_prompts_count,
                            selectedPromptIds.size
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        enabled = selectedPromptIds.isNotEmpty(),
                        onClick = {
                            deleteConfirmDialogShow = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        enabled = selectedPromptIds.isNotEmpty(),
                        onClick = {
                            isBatchTranslateDialogShow = true
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_translate),
                            contentDescription = null
                        )
                    }

                }
            }
        }
    }
    if (!isWideDisplay) {
        Column {
            secondContent()
            firstContent()
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(2f)
            ) {
                firstContent()
            }
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                secondContent()
            }
        }

    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptLibraryPanel(
    onAddPrompt: (Prompt) -> Unit = {},
    onAddStyle: (List<Prompt>) -> Unit = {},
    regionParam: RegionPromptParam? = null,
    enableSearchStyle: Boolean = false
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    var searchJob: Job? by remember { mutableStateOf(null) }
    var inputPromptText by remember { mutableStateOf("") }
    var searchResults by remember {
        mutableStateOf<List<SavePrompt>>(emptyList())
    }
    var searchStyleResults by remember {
        mutableStateOf<List<PromptStyle>>(emptyList())
    }
    var regionIndexToAdd by remember {
        mutableStateOf(0)
    }
    var searchType by remember {
        mutableStateOf("prompt")
    }
    var isSearchTypeMenuShow by remember {
        mutableStateOf(false)
    }
    val searchTypeItems = listOf("prompt", "style")
    var translateDialogShow by remember {
        mutableStateOf(false)
    }
    var currentTranslateText by remember {
        mutableStateOf<String?>(null)
    }

    fun refreshSearchResult() {
        scope.launch(Dispatchers.IO) {
            when (searchType) {
                "prompt" -> {
                    if (inputPromptText.isNotEmpty()) {
                        PromptStore.searchPrompt(
                            context,
                            inputPromptText,
                            emptyList()
                        )
                            .let { results ->
                                searchResults = results
                            }
                    } else {
                        searchResults =
                            PromptStore.getTopNPrompt(context, 10)
                    }
                }

                "style" -> {
                    if (inputPromptText.isNotEmpty()) {
                        StyleStore.searchStyleByName(
                            context,
                            inputPromptText
                        )
                            .let { results ->
                                searchStyleResults = results
                            }
                    }
                }
            }
        }
    }

    if (translateDialogShow) {
        TranslateDialog(
            onDismiss = {
                translateDialogShow = false
            },
            onConfirm = { source, to ->
                inputPromptText = to
                currentTranslateText = source
                translateDialogShow = false
                refreshSearchResult()
            },
            initialText = inputPromptText,
            autoTranslate = true
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row {
            Row {
                IconButton(onClick = {
                    translateDialogShow = true
                }) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_translate),
                        contentDescription = null
                    )

                }
            }
        }
        regionParam?.let {
            if (regionParam.enable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    for (regionIndex in 0 until regionParam.getTotalRegionCount()) {
                        FilterChip(
                            selected = regionIndexToAdd == regionIndex,
                            onClick = {
                                regionIndexToAdd = regionIndex
                            },
                            label = {
                                if (regionParam.useCommon && regionIndex == 0) {
                                    Text(stringResource(R.string.common_region))
                                } else {
                                    Text(
                                        stringResource(
                                            id = R.string.region,
                                            regionIndex.toString()
                                        )
                                    )

                                }
                            })
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputPromptText,
                onValueChange = { newValue ->
                    inputPromptText = newValue
                    currentTranslateText = null
                    searchJob?.cancel()
                    searchJob = coroutineScope.launch {
                        delay(500L)  // delay for 300ms
                        refreshSearchResult()
                    }
                },
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    if (searchType == "prompt") {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                onAddPrompt(
                                    Prompt(
                                        text = inputPromptText,
                                        piority = 0,
                                        regionIndex = regionIndexToAdd,
                                        translation = currentTranslateText
                                    )
                                )
                                inputPromptText = ""
                            }
                        )
                    }
                },
                prefix = {
                    if (enableSearchStyle) {
                        Row {
                            Box(
                                modifier = Modifier
                            ) {
                                Text(
                                    text = ConstValues.SearchTypeMapping[searchType] ?: searchType,
                                    modifier = Modifier
                                        .clickable {
                                            isSearchTypeMenuShow = true
                                        },
                                )
                                DropdownMenu(
                                    expanded = isSearchTypeMenuShow,
                                    onDismissRequest = { isSearchTypeMenuShow = false }
                                ) {
                                    searchTypeItems.forEach {
                                        DropdownMenuItem(
                                            onClick = {
                                                searchType = it
                                                isSearchTypeMenuShow = false
                                                refreshSearchResult()
                                            },
                                            text = {
                                                Text(text = ConstValues.SearchTypeMapping[it] ?: it)
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                },
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        when (searchType) {
            "prompt" -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchResults.size) {
                        val prompt = searchResults[it]
                        Column(
                            modifier = Modifier
                                .clickable {
                                    onAddPrompt(
                                        prompt
                                            .toPrompt()
                                            .copy(regionIndex = regionIndexToAdd)
                                    )
                                    refreshSearchResult()
                                }
                                .padding(4.dp)
                                .fillMaxWidth()

                        ) {
                            Text(text = prompt.text)
                            if (prompt.text != prompt.nameCn) {
                                Text(text = prompt.nameCn)
                            }
                        }
                    }
                }
            }

            "style" -> {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchStyleResults.size) {
                        val styleItem = searchStyleResults[it]
                        Column(
                            modifier = Modifier
                                .clickable {
                                    onAddStyle(styleItem.prompts.map {
                                        it.copy(
                                            regionIndex = regionIndexToAdd
                                        )
                                    })
                                    refreshSearchResult()
                                }
                                .padding(4.dp)
                                .fillMaxWidth()

                        ) {
                            Text(
                                text = styleItem.name, style = TextStyle(
                                    fontSize = 16.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row {
                                styleItem.prompts.subList(0, min(3, styleItem.prompts.size - 1))
                                    .forEach {
                                        SmallPrompt(prompt = it)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                            }

                        }
                    }
                }
            }
        }
    }

}

data class Region(
    var index: Int = -1,
    val ratio: Int = 1,
    val color: Int = Util.randomColor(),
    var subRegions: List<Region> = emptyList(),
    val layout: String = "Column",
    var id: String = layout + "_" + Util.randomString(6),
    var parent: Region? = null
)

fun parseRegionText(input: String): Region {
    var rootRegion = Region(
        subRegions = listOf(
        )
    )
    val multipleColumn = input.contains(";")
    val colParts = input.split(";")
    colParts.forEach {
        var colRegion = Region(
            layout = "Column",
            subRegions = emptyList()
        )
        val rowParts = it.split(",")
        rowParts.forEachIndexed { idx, it ->
            if (multipleColumn && idx == 0) {
                colRegion = colRegion.copy(ratio = it.toIntOrNull() ?: 1)
                if (rowParts.size == 1) {
                    colRegion.subRegions += Region(
                        layout = "Row",
                        subRegions = emptyList(),
                        ratio = it.toIntOrNull() ?: 1,
                        parent = colRegion
                    )
                }
                return@forEachIndexed
            }
            colRegion.subRegions += Region(
                layout = "Row",
                subRegions = emptyList(),
                ratio = it.toIntOrNull() ?: 1,
                parent = colRegion
            )
        }
        rootRegion.subRegions += colRegion
    }
    return rootRegion
}

fun reIndexRegionTree(root: Region, useCommon: Boolean = false): Region {
    var curIndex = if (useCommon) 0 else -1
    return root.copy(
        subRegions = root.subRegions.map { colRegion ->
            colRegion.copy(
                index = colRegion.index,
                subRegions = colRegion.subRegions.map {
                    curIndex += 1
                    it.copy(
                        index = curIndex
                    )
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionalPrompterPanel(
    regionParam: RegionPromptParam,
    isWideDisplay: Boolean = false,
    onValueChange: (RegionPromptParam) -> Unit = {},
    onUseCommonChange: (Boolean) -> Unit = {}
) {
    var selectMode by remember {
        mutableStateOf("column")
    }
    var inputRegionCount by remember {
        mutableStateOf(regionParam.regionCount.toString())
    }
    var regionTree by remember {
        mutableStateOf(
            Region()
        )
    }
    var selectedRegionId by remember {
        mutableStateOf(regionTree.subRegions.firstOrNull()?.id)
    }
    var regionRatioInput by remember {
        mutableStateOf("")
    }
    var selectModeMap = mapOf(
        "column" to stringResource(R.string.select_column),
        "row" to stringResource(R.string.select_row)
    )
    var showRegionDropdown by remember {
        mutableStateOf(false)
    }
    var aspectRatio = Util.calculateActualSize(
        220,
        220,
        DrawViewModel.baseParam.width,
        DrawViewModel.baseParam.height
    )

    fun getRegionText(): String {
        return regionTree.subRegions.map { colRegion ->
            if (colRegion.subRegions.size == 1) {
                return@map colRegion.ratio.toString()
            }
            val colRatios = colRegion.subRegions.map {
                it.ratio.toString()
            }
            if (regionTree.subRegions.size == 1) {
                return@map colRatios.joinToString(separator = ",")
            }
            return@map colRegion.ratio.toString() + "," + colRatios.joinToString(separator = ",")
        }.joinToString(separator = ";")
    }

    fun getRegionCount(countTree: Region = regionTree): Int {
        return countTree.subRegions.flatMap { it.subRegions }.size
    }


    fun onRegionTreeUpdate() {
        val count = getRegionCount()
        val text = getRegionText()
        onValueChange(
            regionParam.copy(
                regionCount = count,
                dividerText = text
            )
        )
        // reindex
        regionTree = reIndexRegionTree(regionTree, useCommon = regionParam.useCommon)

        inputRegionCount = count.toString()
    }
    LaunchedEffect(Unit) {
        regionTree = parseRegionText(regionParam.dividerText)
        onRegionTreeUpdate()
    }

    fun getRegionById(id: String): Region? {
        return (regionTree.subRegions.flatMap { it.subRegions } + regionTree.subRegions).find { it.id == id }
    }

    val selectedRegion = selectedRegionId?.let {
        getRegionById(it)
    }

    fun setSelectMode(mode: String) {
        selectMode = mode
        selectedRegion?.let {
            if (mode == "column" && selectedRegion.layout == "Row") {
                selectedRegionId = selectedRegion.parent?.id
                regionRatioInput = selectedRegion.parent?.ratio.toString()
            }
            if (mode == "row" && selectedRegion.subRegions.isNotEmpty()) {
                selectedRegionId = selectedRegion.subRegions.first().id
                regionRatioInput = selectedRegion.subRegions.first().ratio.toString()
            }
        }
    }

    fun updateRegionTreeById(id: String, regionMod: (Region) -> Region) {
        val targetRegion = getRegionById(id)
        if (targetRegion != null) {
            if (targetRegion.layout == "Column") {
                regionTree = regionTree.copy(
                    subRegions = regionTree.subRegions.map {
                        if (it.id == id) {
                            return@map regionMod(it)
                        } else {
                            return@map it
                        }
                    }
                )
            }
            if (targetRegion.layout == "Row") {
                regionTree = regionTree.copy(
                    subRegions = regionTree.subRegions.map {
                        if (it.layout == "Column") {
                            return@map it.copy(
                                subRegions = it.subRegions.map {
                                    if (it.id == id) {
                                        return@map regionMod(it)
                                    } else {
                                        return@map it
                                    }
                                }
                            )
                        } else {
                            return@map it
                        }
                    }
                )
            }
            onRegionTreeUpdate()
        }
    }

    fun removeRegionById(id: String) {
        val targetRegion = getRegionById(id)
        if (targetRegion != null) {
            if (targetRegion.layout == "Column") {
                regionTree = regionTree.copy(
                    subRegions = regionTree.subRegions.filter {
                        it.id != id
                    }
                )
            }
            if (targetRegion.layout == "Row") {
                regionTree = regionTree.copy(
                    subRegions = regionTree.subRegions.map {
                        if (it.layout == "Column") {
                            return@map it.copy(
                                subRegions = it.subRegions.filter {
                                    it.id != id
                                }
                            )
                        } else {
                            return@map it
                        }
                    }
                )
            }
            onRegionTreeUpdate()
        }
    }

    @Composable
    fun firstContent() {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.enable), modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = regionParam.enable, onCheckedChange = {
                onValueChange(regionParam.copy(enable = it))
            })
        }
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = regionParam.dividerText, onValueChange = {
                regionTree = parseRegionText(it)
                reIndexRegionTree(regionTree, regionParam.useCommon)
                val newRegionCount = getRegionCount(regionTree)
                onValueChange(
                    regionParam.copy(
                        dividerText = it,
                        regionCount = newRegionCount
                    )
                )
                inputRegionCount = newRegionCount.toString()
            }, label = {
                Text(text = stringResource(R.string.region_divider_ratio))
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = inputRegionCount,
            onValueChange = { inputVal ->
                inputRegionCount = inputVal
                try {
                    onValueChange(regionParam.copy(regionCount = inputVal.toInt()))
                } catch (e: Exception) {

                }

            },
            label = {
                Text(text = stringResource(R.string.region_count))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = false
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.region_usecommon), modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = regionParam.useCommon,
                onCheckedChange = {
                    onUseCommonChange(it)
                    reIndexRegionTree(regionTree, it)
                }
            )
        }
    }

    @Composable
    fun secondScreen() {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selectModeMap.keys.forEach {
                FilterChip(selected = selectMode == it, onClick = {
                    setSelectMode(it)
                }, label = { Text(text = selectModeMap[it]!!) })
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(color = Color.Black),
            contentAlignment = Alignment.Center
        ) {
            RegionDisplayView(
                regionTree = regionTree,
                modifier = Modifier
                    .width(aspectRatio.first.dp)
                    .height(aspectRatio.second.dp),
                selectedRegionId = selectedRegionId,
                selectMode = selectMode,
                onSelectChange = {
                    selectedRegionId = it.id
                    regionRatioInput = it.ratio.toString()
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        selectedRegion?.let { region ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.region, region.id))
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        IconButton(onClick = {
                            showRegionDropdown = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = showRegionDropdown,
                            onDismissRequest = {
                                showRegionDropdown = false
                            }
                        ) {
                            if (region.layout == "Column") {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.add_row)) },
                                    onClick = {
                                        updateRegionTreeById(region.id) {
                                            it.copy(
                                                subRegions = it.subRegions + Region(
                                                    layout = "Row",
                                                    parent = it
                                                )
                                            )
                                        }
                                        onRegionTreeUpdate()
                                        showRegionDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.add_column)) },
                                    onClick = {
                                        val colRegion = Region(
                                            subRegions = listOf(

                                            )
                                        )
                                        val rowRegion = Region(
                                            layout = "Row",
                                            parent = colRegion
                                        )
                                        colRegion.subRegions += rowRegion
                                        regionTree = regionTree.copy(
                                            subRegions = regionTree.subRegions + colRegion
                                        )
                                        onRegionTreeUpdate()
                                        showRegionDropdown = false
                                    }
                                )

                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = {
                                    if (region.layout == "Row" && region.parent?.subRegions?.size == 1) {
                                        removeRegionById(region.parent!!.id)
                                        return@DropdownMenuItem
                                    } else {
                                        removeRegionById(region.id)
                                    }
                                    showRegionDropdown = false
                                }
                            )
                        }
                    }
                }
                Slider(
                    value = region.ratio.toFloat(),
                    onValueChange = { newVal ->
                        updateRegionTreeById(region.id) {
                            it.copy(ratio = newVal.toInt())
                        }
                        regionRatioInput = newVal.toInt().toString()
                    },
                    valueRange = 1f..10f,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = regionRatioInput,
                    onValueChange = { newVal ->
                        regionRatioInput = newVal
                        newVal.toFloatOrNull()?.let { newVal ->
                            updateRegionTreeById(region.id) {
                                it.copy(ratio = newVal.toInt())
                            }
                        }
                    },
                    label = {
                        Text(text = stringResource(R.string.ratio))
                    },
                )
            }

        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (!isWideDisplay) {
            firstContent()
            Spacer(modifier = Modifier.height(8.dp))
            secondScreen()
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    firstContent()
                }
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    secondScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptEditContainer(
    promptList: List<Prompt>,
    onPromptClick: (Prompt) -> Unit = {},
    isItemSelected: (Prompt) -> Boolean = { false },
    onlyShowTranslate: Boolean = true,
    tail: @Composable () -> Unit = {},
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        promptList.forEachIndexed { index, prompt ->
            PromptChip(
                prompt = prompt,
                onClickPrompt = {
                    onPromptClick(it)
                },
                selected = isItemSelected(prompt),
                tail = {
                    tail()
                },
                onlyShowTranslation = onlyShowTranslate
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

data class TemplateItem(
    val prompt: Prompt? = null,
    val category: String? = null,
    val slot: String? = null,
    val id: String = UUID.randomUUID().toString(),
    val text: String? = null,
    val locked: Boolean = false
) {
    val displayText: String
        get() {
            return prompt?.getTranslationText() ?: category ?: text ?: ""
        }

    companion object {
        fun fromPrompt(prompt: Prompt): TemplateItem {
            return TemplateItem(
                prompt = prompt,
            )
        }

        fun fromCategory(category: String, templateSlot: String): TemplateItem {
            return TemplateItem(
                category = category,
                slot = templateSlot
            )
        }
    }
}

@OptIn(
    ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun TemplatePrompt(
    regionParam: RegionPromptParam?,
    prompts: List<Prompt> = emptyList(),
    isWideDisplay: Boolean = false,
    templateParam: TemplateParam = TemplateParam(),
    onUpdateTemplate: (TemplateParam) -> Unit = {},
    onUpdated: (List<Prompt>) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var dirtyTemplate by remember {
        mutableStateOf(false)
    }
    var selectedTemplateSlot by remember {
        mutableStateOf(null as String?)
    }
    var templateSlotList by remember {
        mutableStateOf(emptyList<String>())
    }
    var slotCategoryList by remember {
        mutableStateOf(emptyList<String>())
    }
    var selectedCategory by remember {
        mutableStateOf(null as String?)
    }
    var promptList by remember {
        mutableStateOf(emptyList<Prompt>())
    }
    var displayPromptList by remember {
        mutableStateOf(emptyList<Prompt>())
    }
    var isEditMode by remember {
        mutableStateOf(false)
    }
    var isEditTemplateMode by remember {
        mutableStateOf(false)
    }
    var currentTab by remember {
        mutableStateOf(0)
    }
    var selectActionRegionIndex by remember {
        mutableStateOf(0)
    }

    var isPromptActionDialogShow by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(templateParam.template) {
        dirtyTemplate = true
    }

    var historyTemplateList by remember {
        mutableStateOf(emptyList<TemplateWithItems>())
    }
    var lockMode by remember {
        mutableStateOf(false)
    }

    val selectedTemplate = templateParam.template
    fun updateSelectedTemplate(newTemplate: List<TemplateItem>) {
        onUpdateTemplate(
            templateParam.copy(
                template = newTemplate
            )
        )
    }

    val usePromptList = templateParam.generateResult
    fun updateUsePromptList(newList: List<Prompt>) {
        onUpdateTemplate(
            templateParam.copy(
                generateResult = newList
            )
        )
    }

    fun loadHistoryTemplateList() {
        scope.launch(Dispatchers.IO) {
            val result = HistoryStore.getAllTemplates(context)
            historyTemplateList = result
        }
    }

    fun applyTemplateHistory(template: TemplateWithItems) {
        scope.launch(Dispatchers.IO) {
            val itemList = emptyList<TemplateItem>().toMutableList()
            template.items.forEach {
                var item = null as TemplateItem?
                if (it.promptId != null && it.promptId != 0L) {
                    PromptStore.getPromptById(context, it.promptId!!)?.let {
                        item = TemplateItem.fromPrompt(it.toPrompt())
                    }
                }
                if (it.templateCategory != null && it.templateSlot != null) {
                    item = TemplateItem.fromCategory(it.templateCategory!!, it.templateSlot!!)
                }
                item?.let {
                    itemList += it
                }
            }
            updateSelectedTemplate(itemList)
        }

    }


    fun generatePrompt() {
        scope.launch(Dispatchers.IO) {
            isEditMode = false
            lockMode = false
            val pickup = mutableListOf<Prompt>()
            selectedTemplate.forEach { item ->
                val generatedPrompt = usePromptList.find {
                    it.generateItem?.id == item.id
                }
                if (generatedPrompt != null && item.locked) {
                    pickup += generatedPrompt.copy(
                        generateLock = true,
                        generateItem = item
                    )
                    return@forEach
                }
                if (item.prompt != null) {
                    pickup += item.prompt.copy(
                        generateLock = false,
                        generateItem = item
                    )
                    return@forEach
                }
                val category = item.category
                val slot = item.slot
                if (category != null && slot != null) {
                    val result = PromptStore.getPromptByTemplateSlotAndCategory(
                        context,
                        slot = slot,
                        category = category,
                    )
                    val randomIndex = result.indices.random()
                    pickup += result[randomIndex].toPrompt().copy(
                        generateLock = false,
                        generateItem = item
                    )
                }
            }
            updateUsePromptList(pickup)
            if (dirtyTemplate) {
                HistoryStore.saveTemplate(context, selectedTemplate)
            }
        }


    }

    fun refreshPromptList(templateSlot: String) {
        scope.launch(Dispatchers.IO) {
            val result = PromptStore.getPromptByTemplateSlot(context, templateSlot)
            val cate = result.map { it.category }.distinct()
            slotCategoryList = cate
            selectedCategory = cate.firstOrNull()
            promptList = result.map { it.toPrompt() }
            displayPromptList = promptList.filter { it.category == selectedCategory }
        }
    }

    fun switchCategory(category: String) {
        selectedCategory = category
        displayPromptList = promptList.filter { it.category == category }
    }

    fun loadTemplateSlotList() {
        scope.launch(Dispatchers.IO) {
            val result = PromptStore.getAllTemplateSlot(context = context).map {
                val prefixDigi = it.takeWhile { it.isDigit() }.toIntOrNull()
                prefixDigi?.let { digiIndex ->
                    return@map digiIndex to it
                }
                return@map 9999 to it
            }.sortedBy { it.first }.map { it.second }
            templateSlotList = result
            result.firstOrNull()?.let {
                selectedTemplateSlot = it
                refreshPromptList(it)
            }
        }
    }
    LaunchedEffect(Unit) {
        loadTemplateSlotList()
    }
    var selectActionMode by remember {
        mutableStateOf("replace")
    }

    fun send() {
        if (regionParam == null || !regionParam.enable) {
            selectActionRegionIndex = 0
        }
        val newPrompts = usePromptList.map {
            it.copy(regionIndex = selectActionRegionIndex)
        }
        if (selectActionMode == "add") {
            onUpdated(
                newPrompts + prompts
            )
        }
        if (selectActionMode == "replace") {
            onUpdated(
                newPrompts + prompts.filter { it.regionIndex != selectActionRegionIndex }
            )
        }
        isPromptActionDialogShow = false

    }
    if (isPromptActionDialogShow) {
        ModalBottomSheet(onDismissRequest = {
            isPromptActionDialogShow = false
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column {
                    regionParam?.let { regionParam ->
                        if (regionParam.enable) {
                            Text(text = stringResource(id = R.string.regional))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (regionIndex in 0 until regionParam.getTotalRegionCount()) {
                                    FilterChip(
                                        selected = selectActionRegionIndex == regionIndex,
                                        onClick = {
                                            selectActionRegionIndex = regionIndex
                                        },
                                        label = {
                                            if (regionParam.useCommon && regionIndex == 0) {
                                                Text(stringResource(R.string.common_region))
                                            } else {
                                                Text(
                                                    text = stringResource(
                                                        id = R.string.region,
                                                        regionIndex.toString()
                                                    )
                                                )
                                            }

                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    Text(text = stringResource(id = R.string.action))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = selectActionMode == "replace",
                            onClick = {
                                selectActionMode = "replace"
                            },
                            label = {
                                Text(stringResource(R.string.replace))
                            }
                        )
                        FilterChip(selected = selectActionMode == "add", onClick = {
                            selectActionMode = "add"
                        }, label = {
                            Text(stringResource(R.string.add))
                        })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            send()
                        }) {
                        Text(stringResource(R.string.send))
                    }

                }
            }
        }
    }
    @Composable
    fun templateFirst() {
        Column(
            modifier = Modifier
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.template))
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    isEditTemplateMode = !isEditTemplateMode
                }) {
                    if (isEditTemplateMode) {
                        Icon(Icons.Default.Close, "")
                    } else {
                        Icon(Icons.Default.Edit, "")
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .thenIf(isWideDisplay, Modifier.weight(1f))
                    .thenIf(!isWideDisplay, Modifier.height(200.dp))
                    .verticalScroll(rememberScrollState())
            ) {
                TemplateItemContainer(
                    templateItems = selectedTemplate,
                    closeable = isEditTemplateMode
                ) {
                    updateSelectedTemplate(selectedTemplate.filter { it1 -> it1.id != it.id })
                }
            }
        }

    }

    @Composable
    fun templateSecond() {
        Column(
            modifier = Modifier
        ) {
            Text(text = stringResource(R.string.template_slot))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                templateSlotList.forEach {
                    FilterChip(
                        selected = it == selectedTemplateSlot,
                        onClick = {
                            selectedTemplateSlot = it
                            refreshPromptList(it)
                        },
                        label = {
                            Text(text = it)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.template_category))
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                slotCategoryList.forEach {
                    FilterChip(
                        selected = it == selectedCategory,
                        onClick = {

                        },
                        label = {
                            Text(text = it, modifier = Modifier.combinedClickable(
                                onClick = {
                                    switchCategory(it)
                                },
                                onLongClick = {
                                    updateSelectedTemplate(
                                        selectedTemplate + TemplateItem.fromCategory(
                                            it,
                                            selectedTemplateSlot!!
                                        )
                                    )
                                }
                            ))
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                PromptContainer(
                    promptList = displayPromptList,
                    onlyTranslate = true,
                    onClickPrompt = {
                        updateSelectedTemplate(
                            selectedTemplate + TemplateItem.fromPrompt(it)
                        )
                    }
                )
            }
        }

    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentTab == 0,
                onClick = {
                    currentTab = 0
                },
                label = {
                    Text(stringResource(R.string.template))
                }
            )
            FilterChip(
                selected = currentTab == 2,
                onClick = {
                    currentTab = 2
                    loadHistoryTemplateList()
                },
                label = {
                    Text(stringResource(R.string.history))
                }
            )
            FilterChip(
                selected = currentTab == 1,
                onClick = {
                    currentTab = 1
                },
                label = {
                    Text(stringResource(R.string.roll))
                }
            )

        }
        Spacer(modifier = Modifier.height(8.dp))
        when (currentTab) {
            0 -> {
                if (!isWideDisplay) {
                    Column {
                        templateFirst()
                        Spacer(modifier = Modifier.height(8.dp))
                        templateSecond()

                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            templateFirst()
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            templateSecond()
                        }
                    }
                }

            }

            1 -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.template_prompt))
                    Spacer(modifier = Modifier.weight(1f))
                    if (isEditMode || lockMode) {
                        IconButton(onClick = {
                            isEditMode = false
                            lockMode = false
                        }) {
                            Icon(Icons.Default.Close, "")
                        }
                    }
                    if (!isEditMode && !lockMode) {
                        IconButton(onClick = {
                            isEditMode = true
                        }) {
                            Icon(Icons.Default.Edit, "")
                        }
                        IconButton(onClick = {
                            lockMode = true
                        }) {
                            Icon(Icons.Default.Lock, "")
                        }
                    }


                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    GeneratePromptContainer(
                        promptList = usePromptList,
                        onlyTranslate = true,
                        closeable = isEditMode,
                        onClosed = { promptToDelete ->
                            updateUsePromptList(
                                usePromptList.filter { promptToDelete.randomId != it.randomId }
                            )
                        },
                        onClickPrompt = { pmt ->
                            if (lockMode) {
                                if (pmt.generateItem?.prompt != null) {
                                    return@GeneratePromptContainer
                                }
                                updateUsePromptList(
                                    usePromptList.map {
                                        if (it.randomId == pmt.randomId) {
                                            it.copy(generateLock = !it.generateLock)
                                        } else {
                                            it
                                        }
                                    }
                                )
                                updateSelectedTemplate(
                                    selectedTemplate.map {
                                        if (it.id == pmt.generateItem?.id) {
                                            it.copy(locked = !it.locked)
                                        } else {
                                            it
                                        }
                                    }
                                )
                            }
                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        onClick = {
                            generatePrompt()
                        },
                        enabled = selectedTemplate.isNotEmpty() && !isEditMode && !lockMode
                    ) {
                        Text(stringResource(id = R.string.roll))
                    }
                    Button(
                        onClick = {
                            isPromptActionDialogShow = true
                        }, enabled = usePromptList.isNotEmpty() && !lockMode && !isEditMode
                    ) {
                        Text(text = stringResource(id = R.string.send))
                    }
                }
            }

            2 -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn {
                        items(historyTemplateList.size) {
                            val historyItem = historyTemplateList[it]
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        applyTemplateHistory(historyItem)
                                    }
                                    .padding(vertical = 16.dp)
                            ) {
                                TemplateItemContainer(
                                    templateItems = historyItem.items.map { ent ->
                                        TemplateItem(
                                            text = ent.text,
                                            slot = ent.templateSlot,
                                            category = ent.templateCategory
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeneratePromptContainer(
    promptList: List<Prompt>,
    onlyTranslate: Boolean = false,
    closeable: Boolean = false,
    onClosed: ((Prompt) -> Unit)? = {},
    onClickPrompt: ((Prompt) -> Unit)? = {},
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        promptList.forEach {
            SmallPrompt(
                prompt = it,
                onClickPrompt = onClickPrompt,
                onlyTranslate = onlyTranslate,
                closeable = closeable,
                onClosed = onClosed,
                leading = {
                    if (it.generateLock) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (it.generateItem?.prompt == null) {
                        Text(text = "#")
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            )
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateItemContainer(
    templateItems: List<TemplateItem>,
    closeable: Boolean = false,
    onClose: (TemplateItem) -> Unit = {},
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        templateItems.forEach { item ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Row {
                    if (item.locked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (item.category != null) {
                        Text(text = "#")
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = item.displayText)
                    if (closeable) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Close, "",
                            modifier = Modifier.clickable {
                                onClose(item)
                            }
                        )
                    }
                }
            }
        }
    }
}
