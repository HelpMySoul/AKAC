package com.example.akac.classes

import com.example.akac.data.Contact

sealed class ContactListItem {
    data class Header(val letter: String) :        ContactListItem()
    data class ContactItem(val contact: Contact) : ContactListItem()
}