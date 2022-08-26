package app.com.android.mobiletrac.construction.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.models.HistoryModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(options: FirestoreRecyclerOptions<HistoryModel>) : FirestoreRecyclerAdapter<HistoryModel, HistoryAdapter.ViewHolder>(
    options
) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemEntrance: TextView
        val itemDateTime: TextView
        val itemStatus: TextView

        val v: View

        init {
            itemEntrance = itemView.findViewById(R.id.tvHistoryRowEntrance)
            itemDateTime = itemView.findViewById(R.id.tvHistoryRowDateTime)
            itemStatus = itemView.findViewById(R.id.tvHistoryRowStatus)
            v = itemView
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.history_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: HistoryModel) {

        val logDate = model.dateTime

        val timestamp: Long = logDate
        //val timeD = Date(timestamp * 1000)
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

        val time = sdf.format(timestamp)

        holder.itemEntrance.text = model.entrance
        holder.itemDateTime.text = time

        if(model.logStatus == 1) {
            holder.itemStatus.text = holder.v.context.resources.getString(R.string.str_check_in)
            holder.itemStatus.setTextColor(
                ContextCompat.getColor(
                    holder.v.context,
                    R.color.teal_700
                )
            )
        }
        else if(model.logStatus == 0) {
            holder.itemStatus.text = holder.v.context.resources.getString(R.string.str_check_out)
            holder.itemStatus.setTextColor(
                ContextCompat.getColor(
                    holder.v.context,
                    android.R.color.holo_red_light
                )
            )
        }
        else {
            holder.itemStatus.text = ""
        }



    }
}