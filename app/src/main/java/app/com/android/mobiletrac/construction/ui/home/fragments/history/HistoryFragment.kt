package app.com.android.mobiletrac.construction.ui.home.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.com.android.mobiletrac.construction.R
import app.com.android.mobiletrac.construction.adapters.HistoryAdapter
import app.com.android.mobiletrac.construction.models.HistoryModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var recyclerViewHistory: RecyclerView? = null
    private var adapter: HistoryAdapter? = null

    private var tvNoHistory: TextView? = null
    private var query: Query? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_history, container, false)

        tvNoHistory = root.findViewById(R.id.tvNoHistory)

        recyclerViewHistory = root.findViewById(R.id.rvHistory)
        recyclerViewHistory?.layoutManager = LinearLayoutManager(requireContext())

        db = Firebase.firestore
        auth = Firebase.auth

        val curUser = auth.currentUser

        if(curUser != null) {

            val logRef = FirebaseFirestore.getInstance()
            query = logRef.collection("logs").whereEqualTo("userid", curUser.uid)
                    .orderBy("dateTime", Query.Direction.DESCENDING)

            val options = FirestoreRecyclerOptions.Builder<HistoryModel>()
                    .setQuery(query!!, HistoryModel::class.java)
                    .setLifecycleOwner(this)
                    .build()

            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.recent_logs_divider)!!)
            recyclerViewHistory?.addItemDecoration(divider)
            adapter = HistoryAdapter(options)
            recyclerViewHistory?.adapter = adapter

            query?.get()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    if(it.result.size() > 0) {
                        tvNoHistory?.visibility = View.GONE
                     //   recyclerViewHistory?.visibility = View.VISIBLE
                        System.out.println("DATA SIZE STATUS: " + it.result.size().toString())
                    } else {
                        tvNoHistory?.visibility = View.VISIBLE
                      //  recyclerViewHistory?.visibility = View.GONE
                        System.out.println("DATA SIZE STATUS: EMPTY")
                    }
                }
            }

        }


        return root
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}