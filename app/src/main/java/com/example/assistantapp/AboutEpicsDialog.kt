package com.example.assistantapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TeamMember(
    val name: String,
    val regNo: String,
    val school: String? = null,
    val role: String = "Team Member"
)

val EPICS_TEAM = listOf(
    TeamMember("SUMIT PRASAD", "24BCE11520", "SCOPE", "XYZ"),
    TeamMember("Divyanshu Karma", "24BET10016", "SCOPE", "XYZ"),
    TeamMember("Bhavya Manish Jain", "24BSA10295", "SCAI", "XYZ"),
    TeamMember("FAIZ KHAN", "24BCE10703", "SCOPE", "XYZ"),
    TeamMember("Dhruv Chandwani", "24BCE10804", "SCOPE", "XYZ"),
    TeamMember("Ronit Choudhury","24BCE11521","SCOPE","xyz")
)

@Composable
fun AboutEpicsDialog(onDismiss: () -> Unit) {
    val teal = Color(0xFF087E78)
    val ink = Color(0xFF102A2A)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = teal)
            ) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE3F5EF),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.VolunteerActivism,
                        contentDescription = null,
                        tint = teal,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "EPICS Group: 603 • VIT Bhopal",
                        color = ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Engineering Projects In Community Service",
                        color = teal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Project DRISHTI is an AI-powered visual and spatial assistant designed to give individuals with vision impairments real-time independence, safety warnings, and text reading capabilities.",
                    color = Color(0xFF334E4C),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                HorizontalDivider(color = Color(0xFFD4E6E1))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = teal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "PROJECT TEAM MEMBERS",
                        color = ink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                EPICS_TEAM.forEach { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F6))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    member.name,
                                    color = ink,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Reg: ${member.regNo}",
                                        color = teal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (member.school != null) {
                                        Text(
                                            " • ${member.school}",
                                            color = Color(0xFF557774),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Text(
                                    member.role,
                                    color = Color(0xFF6E8E8B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

