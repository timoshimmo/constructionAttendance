package app.com.android.mobiletrac.construction.models

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class HistoryModel(val entrance: String = "",
                        val dateTime: Long = 0,
                        val logStatus: Int = -1
                            ) : Serializable


