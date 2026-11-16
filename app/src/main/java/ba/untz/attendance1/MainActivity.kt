package ba.untz.attendance1

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ba.untz.attendance1.ui.theme.Attendance1Theme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Attendance1Theme {
                Surface {
                    ProfessorScreen()
                }
            }
        }
    }
}


enum class StatusKind(val label: String) {
    ON_TIME("On time"),
    LATE("Late"),
    ABSENT("Absent")
}


fun CheckIn.timeText(): String {
    if (status is CheckInStatus.Absent) return "—"

    val hours = minutes / 60
    val mins = minutes % 60
    return String.format(Locale.getDefault(), "%02d:%02d", hours, mins)
}

@Composable
fun CheckInStatus.color(): Color {
    return when (this) {
        CheckInStatus.OnTime -> MaterialTheme.colorScheme.primary
        is CheckInStatus.Late -> MaterialTheme.colorScheme.error
        CheckInStatus.Absent -> MaterialTheme.colorScheme.outline
    }
}


@Composable
fun AttendanceSummary(
    checkIns: List<CheckIn>,
    modifier: Modifier = Modifier
) {
    val onTimeCount = checkIns.count { it.status is CheckInStatus.OnTime }
    val lateCount = checkIns.count { it.status is CheckInStatus.Late }
    val absentCount = checkIns.count { it.status is CheckInStatus.Absent }

    Text(
        text = "On time: $onTimeCount · Late: $lateCount · Absent: $absentCount",
        modifier = modifier
    )
}

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Search student") },
        placeholder = { Text("e.g. Amir") },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun LateOnlySwitch(
    onlyLate: Boolean,
    onOnlyLateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = onlyLate,
                role = Role.Switch,
                onValueChange = onOnlyLateChange
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Only late",
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = onlyLate,
            onCheckedChange = null
        )
    }
}


@Composable
fun StatusPicker(
    selected: StatusKind,
    onSelect: (StatusKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.selectableGroup()) {
        StatusKind.entries.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selected),
                        onClick = { onSelect(option) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selected),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option.label)
            }
        }
    }
}

@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    showLateBadge: Boolean = false
) {
    Box(modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (showLateBadge) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: CheckInStatus,
    modifier: Modifier = Modifier
) {
    Text(
        text = status.label(),
        color = status.color(),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun CheckInRow(
    checkIn: CheckIn,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            initials = checkIn.student.initials(),
            showLateBadge = checkIn.status is CheckInStatus.Late
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = checkIn.student.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            StatusBadge(status = checkIn.status)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = checkIn.timeText(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CheckInCard(
    checkIn: CheckIn,
    allCheckIns: List<CheckIn>,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        CheckInRow(checkIn = checkIn)

        if (expanded) {
            Column(modifier = Modifier.padding(start = 64.dp, bottom = 8.dp)) {
                val studentCheckIns = allCheckIns.filter { it.student.id == checkIn.student.id }

                studentCheckIns.forEach { item ->
                    Text(
                        text = "${item.window.label}: ${item.status.label()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun WindowControls(
    openWindow: Window?,
    onOpen: (Window) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Open: ${openWindow?.label ?: "none"}")

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Window.entries.forEach { w ->
                Button(
                    onClick = {
                        onOpen(w)
                        Log.d("RMAS", "now: ${w.label}")
                    },
                    enabled = openWindow != w,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(text = w.label)
                }
            }
        }
    }
}

@Composable
fun ProfessorScreen(modifier: Modifier = Modifier) {
    var openWindow by rememberSaveable { mutableStateOf<Window?>(null) }
    var checkIns by remember { mutableStateOf(sampleCheckIns) }
    var query by rememberSaveable { mutableStateOf("") }
    var onlyLate by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        WindowControls(
            openWindow = openWindow,
            onOpen = { openWindow = it }
        )

        val activeCheckIns = if (openWindow != null) {
            checkIns.filter { it.window == openWindow }
        } else {
            emptyList()
        }

        if (openWindow != null) {
            AttendanceSummary(
                checkIns = activeCheckIns,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            SearchField(
                query = query,
                onQueryChange = { query = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            LateOnlySwitch(
                onlyLate = onlyLate,
                onOnlyLateChange = { onlyLate = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))


            val filteredCheckIns = activeCheckIns.filter { checkIn ->
                val matchesQuery = checkIn.student.name.contains(query, ignoreCase = true)
                val matchesLate = !onlyLate || checkIn.status is CheckInStatus.Late
                matchesQuery && matchesLate
            }

            if (filteredCheckIns.isEmpty()) {
                Text(
                    text = "No check-ins match the filter",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            } else {
                Column {
                    filteredCheckIns.forEach { checkIn ->
                        CheckInCard(
                            checkIn = checkIn,
                            allCheckIns = checkIns
                        )
                    }
                }
            }
        } else {
            Text(
                text = "Open a window to see check-ins",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}



@Preview(name = "StatusPicker - Late", showBackground = true)
@Composable
fun StatusPickerPreview() {
    Attendance1Theme {
        Surface {
            StatusPicker(
                selected = StatusKind.LATE,
                onSelect = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "SearchField Preview", showBackground = true)
@Composable
fun SearchFieldPreview() {
    Attendance1Theme {
        Surface {
            SearchField(
                query = "ami",
                onQueryChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "LateOnlySwitch - On", showBackground = true)
@Composable
fun LateOnlySwitchOnPreview() {
    Attendance1Theme {
        Surface {
            LateOnlySwitch(
                onlyLate = true,
                onOnlyLateChange = {},
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}






