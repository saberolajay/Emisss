package com.example.emis.ModeClasses


class Chatlist {

    private var id: String = ""

    constructor()

    constructor(id: String) {
        this.id = id
    }

    fun getId(): String {
        return id
    }

    // Safely set id without forcing non-null
    fun setId(id: String?) {
        this.id = id ?: ""  // If id is null, set an empty string
    }
}
