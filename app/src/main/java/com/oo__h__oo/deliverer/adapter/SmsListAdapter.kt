package com.oo__h__oo.deliverer.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.oo__h__oo.deliverer.R
import com.oo__h__oo.deliverer.manager.Message
import java.text.SimpleDateFormat

class SmsListAdapter(private val inflater: LayoutInflater, private val list: List<Message>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        @SuppressLint("ViewHolder", "InflateParams")
        val itemView = inflater.inflate(R.layout.sms_item, null)
        val info = list[position]
        val flagTitleView = itemView.findViewById<TextView>(R.id.flag)
        flagTitleView.text = info.messageFromTypeDescribe
        val addressitleView = itemView.findViewById<TextView>(R.id.from)
        addressitleView.text = info.name?.replace("+86","") ?: ""
        val bodyTitleView = itemView.findViewById<TextView>(R.id.body)
        bodyTitleView.text = info.body
        val dateTitleView = itemView.findViewById<TextView>(R.id.date)
        @SuppressLint("SimpleDateFormat")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        dateTitleView.text = dateFormat.format(info.date)

        return itemView
    }


    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
