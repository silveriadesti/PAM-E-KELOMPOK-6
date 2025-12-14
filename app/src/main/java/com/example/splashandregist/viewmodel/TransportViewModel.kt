package com.example.splashandregist.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

data class Transport(
    val id: String,
    val name: String,
    val type: String,
    val route: String,
    val capacity: Int,
    val price: String,
    val imageUrl: String,
    val description: String
)

class TransportViewModel : ViewModel() {

    private val _list = mutableStateListOf(
        Transport(
            id = "1",
            name = "Bus Trans Jawa",
            type = "Bus",
            route = "Surabaya - Jakarta",
            capacity = 40,
            price = "Rp 350.000",
            imageUrl = "https://asset.kompas.com/crops/iUxhFS5brWKrCA23PJPVQfLoCdw=/0x0:0x0/750x500/data/photo/2023/04/13/6437c7965f630.jpg",
            description = "Bus Eksekutif dengan fasilitas AC, reclining seat, dan toilet."
        ),
        Transport(
            id = "2",
            name = "Kereta Api Argo Bromo",
            type = "Kereta",
            route = "Malang - Jakarta",
            capacity = 100,
            price = "Rp 580.000",
            imageUrl = "https://asset.kompas.com/crops/v823UeJ8V4aHcRUW2KIiMzFxLbc=/0x0:0x0/750x500/data/photo/2023/06/20/64912b40a8fb7.jpg",
            description = "Kereta kelas eksekutif dengan jadwal cepat dan nyaman."
        )
    )

    val transports: List<Transport> get() = _list

    fun addTransport(t: Transport) {
        _list.add(t)
    }

    fun getTransportById(id: String) =
        _list.find { it.id == id }

    fun updateTransport(updated: Transport) {
        val index = _list.indexOfFirst { it.id == updated.id }
        if (index != -1) _list[index] = updated
    }

    fun deleteTransport(id: String) {
        _list.removeAll { it.id == id }
    }
}
