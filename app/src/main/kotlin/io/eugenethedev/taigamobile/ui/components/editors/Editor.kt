package io.eugenethedev.taigamobile.ui.components.editors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.ui.components.appbars.AppBarWithBackButton
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.onBackPressed

@Composable
fun Editor(
    toolbarText: String,
    title: String = "",
    description: String = "",
    showTitle: Boolean = true,
    onSaveClick: (title: String, description: String) -> Unit = { _, _ -> },
    navigateBack: () -> Unit = {}
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surface)
        .imePadding()
) {
    onBackPressed(navigateBack)

    var titleInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(title)
        )
    }
    var descriptionInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(description)
        )
    }

    AppBarWithBackButton(
        title = { Text(toolbarText) },
        actions = {
            IconButton(
                onClick = {
                    titleInput.text.trim().takeIf { it.isNotEmpty() }?.let {
                        onSaveClick(it, descriptionInput.text.trim())
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_save),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        navigateBack = navigateBack
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = mainHorizontalScreenPadding)
    ) {

        Spacer(Modifier.height(8.dp))

        if (showTitle) {
            TextFieldWithHint(
                hintId = R.string.title_hint,
                value = titleInput,
                onValueChange = { titleInput = it },
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(16.dp))
        }

        TextFieldWithHint(
            hintId = R.string.description_hint,
            value = descriptionInput,
            onValueChange = { descriptionInput = it },
        )

        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }

}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TaskEditorPreview() = TaigaMobileTheme {
    Editor("Edit")
}
