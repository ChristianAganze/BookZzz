package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

@Composable
fun PaymentProofScreen(
    hotelName: String,
    hotelMoMoNumber: String,
    bookingId: String,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var uploading by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Finaliser la réservation", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Hôtel: $hotelName")
        Text("Numéro MoMo: $hotelMoMoNumber", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Montant payé (USD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = senderNumber,
            onValueChange = { senderNumber = it },
            label = { Text("Votre numéro émetteur") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Text(if (selectedImageUri == null) "Sélectionner la preuve de paiement" else "Image sélectionnée")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                uploading = true
                val fileName = UUID.randomUUID().toString()
                val storageRef = FirebaseStorage.getInstance().reference.child("payments/$fileName")
                
                selectedImageUri?.let { uri ->
                    storageRef.putFile(uri).addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val paymentData = hashMapOf(
                                "bookingId" to bookingId,
                                "amount" to amount,
                                "senderNumber" to senderNumber,
                                "imageUrl" to downloadUrl.toString(),
                                "status" to "En attente"
                            )
                            FirebaseFirestore.getInstance().collection("payments").add(paymentData)
                                .addOnSuccessListener {
                                    uploading = false
                                    onNavigateBack()
                                }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = selectedImageUri != null && amount.isNotEmpty() && !uploading
        ) {
            Text(if (uploading) "Envoi en cours..." else "Envoyer la preuve")
        }
    }
}
