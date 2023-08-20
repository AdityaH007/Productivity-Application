import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.ListView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.attendance.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class attendance : Fragment() {



    private lateinit var selectedSubject: String
    private lateinit var addAttendanceButton: Button
    private lateinit var attendanceCalendarView: CalendarView
    private lateinit var selectedDate: Date
    private lateinit var pieChart: PieChart
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var datesListView: ListView
    private lateinit var datesList: ArrayList<String>
    private lateinit var datesListAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_attendance, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get the selected subject from arguments
        selectedSubject = requireArguments().getString("subjectName") ?: ""

        addAttendanceButton = view.findViewById(R.id.addAttendanceButton)
        attendanceCalendarView = view.findViewById(R.id.attendanceCalendarView)
        pieChart = view.findViewById(R.id.pieChart)

        // Find the button by its ID
        val addDrawerButton: Button = view.findViewById(R.id.button2)

// Find the DrawerLayout by its ID
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawerLayout)

// Set a click listener to toggle the drawer when the button is clicked
        addDrawerButton.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                // If the drawer is open, close it
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                // If the drawer is closed, open it
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }


        // Initialize selectedDate to today's date
        selectedDate = Calendar.getInstance().time

        attendanceCalendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Update selectedDate when the user selects a date
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        }

        datesListView = view.findViewById(R.id.dateListView)
        datesList = ArrayList()
        datesListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, datesList)
        datesListView.adapter = datesListAdapter

        addAttendanceButton.setOnClickListener {
            showAttendanceDialog()
        }

        // Load attendance data and update the pie chart and dates list
        loadAttendanceData()
        updatePieChart()

        return view
    }

    private fun showAttendanceDialog() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateString = dateFormat.format(selectedDate)

        val attendanceOptions = arrayOf("Present", "Absent")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Mark Attendance for $dateString")
            .setItems(attendanceOptions) { _, which ->
                val attendanceStatus = if (which == 0) "Present" else "Absent"
                saveAttendance(attendanceStatus)
            }
            .show()
    }

    private fun saveAttendance(attendanceStatus: String) {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            // Reference to the attendance collection for the selected subject
            val attendanceCollectionRef = firestore.collection("users").document(uid)
                .collection("subjects").document(selectedSubject)
                .collection("attendance")

            // Check if attendance for the selected date already exists
            val attendanceQuery = attendanceCollectionRef.whereEqualTo("date", selectedDate)

            attendanceQuery.get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        // No attendance entry exists for the selected date, add a new entry
                        val attendanceData = hashMapOf(
                            "date" to selectedDate,
                            "status" to "$attendanceStatus"
                        )
                        attendanceCollectionRef.add(attendanceData)

                        // After saving attendance, update the pie chart
                        updatePieChart()
                    } else {
                        // Attendance entry already exists for the selected date
                        showAttendanceExistsDialog()
                    }
                }
        }
    }

    private fun getStatusSuffix(status: String): String {
        return when (status) {
            "Present" -> " P"
            "Absent" -> " A"
            else -> ""
        }
    }



    private fun loadAttendanceData() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .collection("subjects").document(selectedSubject)
                .collection("attendance")
                .get()
                .addOnSuccessListener { documents ->
                    // Clear the previous list and add new dates
                    datesList.clear()
                    for (document in documents) {
                        val date = document.getDate("date")
                        val status = document.getString("status") ?: ""

                        if (date != null) {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val dateString = dateFormat.format(date)
                            datesList.add("$dateString${getStatusSuffix(status)}")
                        }
                    }
                    // Notify the adapter about the changes
                    datesListAdapter.notifyDataSetChanged()
                }
        }
    }


    private fun showAttendanceExistsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Attendance Already Added")
            .setMessage("Attendance for the selected date already exists.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updatePieChart() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid != null) {
            // Fetch attendance data from Firestore and calculate percentages
            firestore.collection("users").document(uid)
                .collection("subjects").document(selectedSubject)
                .collection("attendance")
                .get()
                .addOnSuccessListener { documents ->
                    var presentCount = 0
                    var absentCount = 0

                    for (document in documents) {
                        val status = document.getString("status")
                        if (status == "Present") {
                            presentCount++
                        } else if (status == "Absent") {
                            absentCount++
                        }
                    }

                    val total = presentCount + absentCount
                    val presentPercentage = (presentCount.toFloat() / total) * 100
                    val absentPercentage = (absentCount.toFloat() / total) * 100

                    val entries = mutableListOf<PieEntry>()
                    entries.add(PieEntry(presentPercentage, "Present"))
                    entries.add(PieEntry(absentPercentage, "Absent"))

                    val dataSet = PieDataSet(entries, "Attendance")
                    dataSet.setColors(Color.GREEN, Color.RED)
                    dataSet.setValueFormatter(PercentFormatter(pieChart)) // Use setValueFormatter

                    val data = PieData(dataSet)
                    data.setValueTextSize(14f)
                    data.setValueTextColor(Color.WHITE)

                    pieChart.data = data
                    pieChart.description.isEnabled = false
                    pieChart.centerText = "Attendance"
                    pieChart.setCenterTextSize(20f)
                    pieChart.animateY(1000)
                }
        }
    }
}
