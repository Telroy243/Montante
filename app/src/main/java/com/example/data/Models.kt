package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "montantes")
data class Montante(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val capitalDepart: Double,
    val objectifFinal: Double,
    val coteMin: Double,
    val coteMax: Double,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "bets",
    foreignKeys = [
        ForeignKey(
            entity = Montante::class,
            parentColumns = ["id"],
            childColumns = ["montanteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["montanteId"])]
)
data class Bet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val montanteId: Int,
    val matchName: String,
    val cote: Double,
    val mise: Double,
    val status: String, // "PENDING", "WON", "LOST"
    val timestamp: Long = System.currentTimeMillis(),
    val predictionType: String // "1N2", "Over/Under Buts", "Les deux équipes marquent", "Buteur", "Autre"
)
