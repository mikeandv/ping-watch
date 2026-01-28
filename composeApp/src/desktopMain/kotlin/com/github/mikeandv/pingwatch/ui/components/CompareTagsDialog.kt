package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.mikeandv.pingwatch.domain.Category

@Composable
fun CompareTagsDialog(
    showDialog: Boolean,
    tags: List<Category>,
    onDismiss: () -> Unit,
    onCompare: (Category, Category) -> Unit
) {
    if (!showDialog) return

    var firstTag by remember { mutableStateOf<Category?>(null) }
    var secondTag by remember { mutableStateOf<Category?>(null) }
    var firstExpanded by remember { mutableStateOf(false) }
    var secondExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.width(400.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Compare Tags",
                    style = MaterialTheme.typography.h6
                )

                if (tags.size < 2) {
                    Text(
                        text = "At least 2 tags are required for comparison",
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.error
                    )
                } else {
                    TagSelector(
                        label = "First Tag",
                        selectedTag = firstTag,
                        tags = tags,
                        expanded = firstExpanded,
                        onExpandedChange = { firstExpanded = it },
                        onTagSelected = { firstTag = it }
                    )

                    TagSelector(
                        label = "Second Tag",
                        selectedTag = secondTag,
                        tags = tags,
                        expanded = secondExpanded,
                        onExpandedChange = { secondExpanded = it },
                        onTagSelected = { secondTag = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (firstTag != null && secondTag != null) {
                                onCompare(firstTag!!, secondTag!!)
                            }
                        },
                        enabled = firstTag != null && secondTag != null && firstTag != secondTag
                    ) {
                        Text("Compare")
                    }
                }
            }
        }
    }
}

@Composable
private fun TagSelector(
    label: String,
    selectedTag: Category?,
    tags: List<Category>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onTagSelected: (Category) -> Unit
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.caption)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { onExpandedChange(true) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = selectedTag?.name ?: "Select tag",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.body2
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                tags.forEach { tag ->
                    DropdownMenuItem(onClick = {
                        onTagSelected(tag)
                        onExpandedChange(false)
                    }) {
                        Text(tag.name, style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}
