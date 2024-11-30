package com.example.emis.ModeClasses

class Users(
    private var uid: String = "",
    private var username: String = "",
    private var profile: String = "",
    private var cover: String = "",
    private var status: String = "",
    private var search: String = "",
    private var facebook: String = "",    // New field for Facebook
    private var instagram: String = "",
    private var phone: String? = "",
    private var address: String? = "",
    private var fileUrl: String = ""  // New field for file URL
) {

    // Getter and Setter Methods
    fun getUID(): String = uid
    fun setUID(uid: String) {
        this.uid = uid
    }

    fun getUSERNAME(): String = username
    fun setUSERNAME(username: String) {
        this.username = username
    }

    fun getPROFILE(): String = profile
    fun setPROFILE(profile: String) {
        this.profile = profile
    }

    fun getCOVER(): String = cover
    fun setCOVER(cover: String) {
        this.cover = cover
    }

    fun getSTATUS(): String = status
    fun setSTATUS(status: String) {
        this.status = status
    }

    fun getSEARCH(): String = search
    fun setSEARCH(search: String) {
        this.search = search
    }

    // New Getter and Setter for Facebook
    fun getFACEBOOK(): String = facebook
    fun setFACEBOOK(facebook: String) {
        this.facebook = facebook
    }

    // New Getter and Setter for Instagram
    fun getINSTAGRAM(): String = instagram
    fun setINSTAGRAM(instagram: String) {
        this.instagram = instagram
    }

    fun getPHONE(): String? {   // Method to retrieve phone number
        return phone
    }

    fun getADDRESS(): String? {  // Method to retrieve address
        return address
    }

    // New method to get the file URL
}
