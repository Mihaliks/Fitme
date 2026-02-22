package com.example.fitme.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName ="exercises",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("group_id")]
    )
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name="name") val name: String,
    @ColumnInfo(name="lastmax1") val lastmax1 : String,
    @ColumnInfo(name="lastmax2") val lastmax2 : String,
    @ColumnInfo(name="group_id") val groupId : Int, //это должно быть связано с Entity Group
    @ColumnInfo(name="is_active") val isActive : Boolean,
    @ColumnInfo(name="muscle") val muscle : String,
)