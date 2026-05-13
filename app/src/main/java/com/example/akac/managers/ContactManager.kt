import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.example.akac.data.Contact

class ContactManager(context: Context) {
    private val cr: ContentResolver = context.contentResolver

    fun getContacts(): List<Contact> {
        val contacts        = mutableListOf<Contact>()
        val cursor: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
            null,
            null,
            null
        )

        cursor?.use { contactCursor ->
            val idI   = contactCursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameI = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)

            while (contactCursor.moveToNext()) {
                val contactI = contactCursor.getString(idI)

                val name  = contactCursor.getString(nameI) ?: "No name"
                val phone = getPhoneNumber(contactI)

                if (!phone.isNullOrBlank()) {
                    contacts.add(Contact(name, phone))
                }
            }
        }
        return contacts
    }

    private fun getPhoneNumber(contactId: String): String? {
        var mNum: String? = null
        var aNum: String? = null

        val phoneCursor = cr.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE
            ),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        phoneCursor?.use { cursor ->
            val numberI = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeI   = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

            while (cursor.moveToNext()) {
                val number = cursor.getString(numberI)
                val type   = cursor.getInt(typeI)

                if (aNum == null)
                    aNum = number
                if (type == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
                    mNum = number
                    break
                }
            }
        }

        return mNum ?: aNum
    }
}