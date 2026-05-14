package com.example.akac.adapters

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import com.example.akac.R
import com.example.akac.classes.ContactListItem
import com.example.akac.data.Contact
import com.example.akac.databinding.ContactItemBinding
import com.example.akac.databinding.HeaderItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactAdapter(
    private val context: Context,
    private val items:   List<ContactListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTACT = 1

        private fun loadContact(cr: ContentResolver, contactId: String): Bitmap? {
            return try {
                val contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contactId.toLong(),
                )
                ContactsContract.Contacts.openContactPhotoInputStream(cr, contactUri, true)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ContactListItem.Header      -> TYPE_HEADER
            is ContactListItem.ContactItem -> TYPE_CONTACT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = HeaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ContactViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ContactListItem.Header      -> (holder as HeaderViewHolder).bind(item.letter)
            is ContactListItem.ContactItem -> (holder as ContactViewHolder).bind(item.contact, position)
        }
    }

    override fun getItemCount() = items.size

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ContactViewHolder) {
            holder.photoJob?.cancel()
            holder.photoJob = null
        }
        super.onViewRecycled(holder)
    }

    inner class HeaderViewHolder(private val binding: HeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(letter: String) {
            binding.headerLetter.text = letter
        }
    }

    inner class ContactViewHolder(val binding: ContactItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var photoJob: Job? = null

        fun bind(contact: Contact, position: Int) {
            photoJob?.cancel()
            photoJob = null

            binding.contactName.text   = contact.text
            binding.contactNumber.text = contact.phoneNum

            val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
            binding.image.setImageDrawable(placeholder)

            val activity = context as? AppCompatActivity
            if (activity != null) {
                photoJob = activity.lifecycleScope.launch {
                    val bitmap = withContext(Dispatchers.IO) {
                        loadContact(activity.contentResolver, contact.contactId)
                    }
                    if (bindingAdapterPosition == position && bitmap != null) {
                        binding.image.setImageBitmap(bitmap)
                    }
                }
            }

            itemView.setOnClickListener {
                val uri = Uri.fromParts("tel", contact.phoneNum, null)
                val intent = Intent(Intent.ACTION_CALL, uri)
                ContextCompat.startActivity(context, intent, null)
            }
        }
    }
}