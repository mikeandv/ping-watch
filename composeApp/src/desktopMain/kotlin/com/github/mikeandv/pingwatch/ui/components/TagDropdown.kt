package com.github.mikeandv.pingwatch.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.github.mikeandv.pingwatch.domain.Category
import com.github.mikeandv.pingwatch.utils.getNewTagId

@Composable
fun TagDropdown(
    selectedTag: Category?,
    tags: List<Category>,
    onTagSelected: (Category?) -> Unit,
    onCreateTag: (Category) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.width(120.dp).height(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = selectedTag?.name ?: "No tag",
                style = MaterialTheme.typography.body2,
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                isCreating = false
                newTagName = ""
            }
        ) {
            DropdownMenuItem(onClick = {
                onTagSelected(null)
                expanded = false
            }) {
                Text("No tag", style = MaterialTheme.typography.body2)
            }
            tags.forEach { tag ->
                DropdownMenuItem(onClick = {
                    onTagSelected(tag)
                    expanded = false
                }) {
                    Text(tag.name, style = MaterialTheme.typography.body2)
                }
            }
            Divider()
            if (isCreating) {
                CreateTagInput(
                    newTagName = newTagName,
                    onNameChange = { newTagName = it },
                    onAdd = {
                        if (newTagName.isNotBlank()) {
                            val newTag = Category(getNewTagId(tags), newTagName.trim())
                            onCreateTag(newTag)
                            onTagSelected(newTag)
                            newTagName = ""
                            isCreating = false
                            expanded = false
                        }
                    }
                )
            } else {
                DropdownMenuItem(onClick = { isCreating = true }) {
                    Text("+ Create new", style = MaterialTheme.typography.body2, color = MaterialTheme.colors.primary)
                }
            }
        }
    }
}

@Composable
private fun CreateTagInput(
    newTagName: String,
    onNameChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = newTagName,
            onValueChange = onNameChange,
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            modifier = Modifier
                .width(100.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (newTagName.isEmpty()) {
                        Text(
                            text = "Tag name",
                            color = Color.Gray,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = onAdd,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Add", style = MaterialTheme.typography.body2)
        }
    }
}
