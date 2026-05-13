package com.example.akac.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.akac.classes.ViewHolder
import com.example.akac.data.Contact
import com.example.akac.databinding.ContactItemBinding

class ContactAdap(
    private val context:  Context,
    private val contacts: List<Contact>
    ): RecyclerView.Adapter<ViewHolder>()
{
    override fun onCreateViewHolder(parent:   ViewGroup,
                                    viewType: Int): ViewHolder
    {
        val binding = ContactItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val contact                         = contacts[pos]
        holder.binding.contactName.text     = contact.name
        holder.binding.contactSurname.text  = contact.surname
        holder.binding.contactNumber.text   = contact.phoneNum

        holder.itemView.setOnClickListener {
            val actionIntent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse(contact.phoneNum)
            }
            ContextCompat.startActivity(context, actionIntent, null)
        }
    }

    override fun getItemCount() = contacts.size
}